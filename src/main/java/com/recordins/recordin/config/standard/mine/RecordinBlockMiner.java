/*
 * Record'in
 *
 * Copyright (C) 2019 Blockchain Record'in Solutions
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.recordins.recordin.config.standard.mine;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

import com.recordins.recordin.Main;
import com.recordins.recordin.PendingTransactionInformation;
import com.recordins.recordin.orm.BlockchainObject;
import com.recordins.recordin.orm.attribute.AttrID;
import com.recordins.recordin.utils.BlockchainLock;
import org.apache.commons.collections4.CollectionUtils;
import org.ethereum.config.SystemProperties;
import org.ethereum.core.*;
import org.ethereum.listener.CompositeEthereumListener;
import org.ethereum.mine.BlockMiner;
import org.springframework.beans.factory.annotation.Autowired;

import org.ethereum.db.BlockStore;
import org.ethereum.facade.Ethereum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RecordinBlockMiner extends BlockMiner {

    /* Logger for console output */
    private static final Logger logger = LoggerFactory.getLogger(RecordinBlockMiner.class);

    @Autowired
    private Ethereum ethereum;

    @Autowired
    private Blockchain blockchain;

    //@Autowired not working ? -> to test
    //private Ethereum ethereum = (Ethereum) ApplicationContext.getInstance().get("ethereum");

    int id = 0;

    public List<Transaction> getPendingTransactions() {

        return getAllPendingTransactions();
    }

    // the time of the last block mining start
    private long lastMiningTime = 0l;

    // the max time the system waits between a first transaction coming for new block and this actual block's start of mining
    private long maxBlockWaitingTime = 1000;

    // the interval time between comming transactions that triggers a new block start of mining (not waiting until the maxWaitingTime).
    private long maxTimeBetweenTransactions = 200;

    // the time of the last block mining start
    private long lastTransactionTime = 0l;

    private AwaitingThread awaitingThreadBlock = null;
    private AwaitingThread awaitingThreadTransaction = null;

    private boolean newAwaitingThreadBlockFlag = false;

    private BlockchainLock lock = new BlockchainLock();
    private long lastMiningBlockNumber = 0l;
    ;

    @Autowired
    public RecordinBlockMiner(final SystemProperties config, final CompositeEthereumListener listener,
                              final Blockchain blockchain, final BlockStore blockStore,
                              final PendingState pendingState) {
        super(config, listener, blockchain, blockStore, pendingState);

        logger.trace("START RecordinBlockMiner()");
        logger.trace("END RecordinBlockMiner()");
    }

    protected void stopAllAwaitingThreads() {

        if (awaitingThreadBlock != null) {
            awaitingThreadBlock.setWaiting(false);
            awaitingThreadBlock.interrupt();
        }

        if (awaitingThreadTransaction != null) {
            awaitingThreadTransaction.setWaiting(false);
            awaitingThreadTransaction.interrupt();
        }
    }

    public void cancelIfMiningBlock() {
        if (miningBlock != null) {
            synchronized (this) {
                this.cancelCurrentBlock();
            }
        }
    }

    public void setLastMiningBlockNumber(long lastMiningBlockNumber) {
        this.lastMiningBlockNumber = lastMiningBlockNumber;
    }

    @Override
    public synchronized void onPendingStateChanged() {
        if (!isLocalMining && externalMiner == null) return;

        logger.debug("onPendingStateChanged()");


        if (miningBlock != null) {
            lastMiningBlockNumber = miningBlock.getNumber();
            logger.trace("miningBlock               : " + miningBlock.getShortDescr());
        } else {
            logger.trace("miningBlock               : null");
        }

        if (miningBlock != null && (miningBlock.getNumber() <= ((PendingStateImpl) pendingState).getBestBlock().getNumber())) {
            logger.debug("Restart mining: new best block: " + blockchain.getBestBlock().getShortDescr());
            restartMining();
        } else if (((lastMiningBlockNumber == ((PendingStateImpl) pendingState).getBestBlock().getNumber()) && miningBlock == null) || (miningBlock != null && !CollectionUtils.isEqualCollection(miningBlock.getTransactionsList(), getAllPendingTransactions()))) {

            // Extension to support higher workload.
            // Stay including more transactions for some additional time before start mining, rather cancelling and restarting
            // mining a new block for each new coming transaction...

            logger.debug("Pending transactions changed");

            //if(!Main.initDataModelFlag && !Main.initFlag) {
            if (!Main.initDataModelFlag) {
                long now = LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli();

                if (miningBlock == null && !getAllPendingTransactions().isEmpty()) {

                    long cookie = lock.lock(1);

                    if (awaitingThreadBlock == null || (awaitingThreadBlock != null && !awaitingThreadBlock.isWaiting())) {
                        //                    logger.warn("Start awaiting thread for Block");
                        awaitingThreadBlock = new AwaitingThread(maxBlockWaitingTime, "AwaitingBlock");
                        awaitingThreadBlock.start();
                        newAwaitingThreadBlockFlag = true;
                    }


                    if (now - lastTransactionTime < maxTimeBetweenTransactions) {
                        if (awaitingThreadTransaction == null) {
                            awaitingThreadTransaction = new AwaitingThread(maxTimeBetweenTransactions, "AwaitingTransaction_" + id);
                            //                        logger.warn("Start(1) awaiting thread for Transaction");
                            awaitingThreadTransaction.start();
                        } else {
                            awaitingThreadTransaction.setWaiting(false);
                            awaitingThreadTransaction.interrupt();
                            awaitingThreadTransaction = new AwaitingThread(maxTimeBetweenTransactions, "AwaitingTransaction_" + id);
                            //                        logger.warn("Restart(1) awaiting thread for Transaction");
                            awaitingThreadTransaction.start();
                        }
                        id++;
                    } else if (newAwaitingThreadBlockFlag) {
                        if (awaitingThreadTransaction == null) {
                            awaitingThreadTransaction = new AwaitingThread(maxTimeBetweenTransactions, "AwaitingTransaction_" + id);
                            //                        logger.warn("Start(2) awaiting thread for Transaction");
                            awaitingThreadTransaction.start();
                        } else {
                            awaitingThreadTransaction.setWaiting(false);
                            awaitingThreadTransaction.interrupt();
                            awaitingThreadTransaction = new AwaitingThread(maxTimeBetweenTransactions, "AwaitingTransaction_" + id);
                            //                       logger.warn("Restart(2) awaiting thread for Transaction");
                            awaitingThreadTransaction.start();
                        }
                        id++;

                        newAwaitingThreadBlockFlag = false;
                    }

                    lastTransactionTime = now;
                    lock.unlock(1, cookie);
                } else {

                }
            } else {
                logger.debug("Restart mining: pending transactions changed");
                restartMining();
            }

        } else {
            if (logger.isDebugEnabled()) {
                String s = "onPendingStateChanged() event, but pending Txs the same as in currently mining block: ";
            }
        }
    }

    @Override
    protected Block getNewBlockForMining() {
        logger.trace("START getNewBlockForMining()");


        while (getAllPendingTransactions().isEmpty()) {
            try {

                Thread.sleep(50);

            } catch (InterruptedException ex) {
            }
        }

        List<Transaction> allPendingTransactions = getAllPendingTransactions();
        List<Transaction> pendingTransactions = new ArrayList();

        if (Main.initDataModelFlag) {
            if (allPendingTransactions.size() > 0) {
                pendingTransactions = new ArrayList();
                pendingTransactions.add(allPendingTransactions.get(0));
            }
        } else {
            pendingTransactions = allPendingTransactions;
        }

        Block bestPendingState = ethereum.getBlockchain().getBestBlock();

        logger.debug("");
        logger.debug("------------------------------------------------------");
        logger.trace("BEST PENDING STATE  : " + bestPendingState);
        logger.debug("PENDING TRANSACTIONS: " + pendingTransactions.size());


        if (logger.isTraceEnabled()) {
            for (Transaction transaction : pendingTransactions) {
                logger.debug("transaction data  : " + new String(transaction.getData()));
            }
        }


        logger.debug("------------------------------------------------------");
        logger.debug("");

        Block newMiningBlock = blockchain.createNewBlock(bestPendingState, pendingTransactions,
                getUncles(bestPendingState));

        logger.trace("END getNewBlockForMining()");
        return newMiningBlock;
    }

    /*

    @Override
    protected void blockMined(Block newBlock) throws InterruptedException {
        long t = System.currentTimeMillis();
        if (t - lastBlockMinedTime < minBlockTimeout) {
            long sleepTime = minBlockTimeout - (t - lastBlockMinedTime);
            logger.debug("Last block was mined " + (t - lastBlockMinedTime) + " ms ago. Sleeping " +
                    sleepTime + " ms before importing...");
            Thread.sleep(sleepTime);
        }

        fireBlockMined(newBlock);
        logger.info("Wow, block mined !!!: {}", newBlock.toString());

        lastBlockMinedTime = t;

        miningBlock = null;
        // cancel all tasks
        cancelCurrentBlock();

        // broadcast the block
        logger.debug("Importing newly mined block {} {} ...", newBlock.getShortHash(), newBlock.getNumber());
        ImportResult importResult = ((EthereumImpl) ethereum).addNewMinedBlock(newBlock);
        logger.debug("Mined block import result is " + importResult);

    }
    */

    public HashMap<AttrID, BlockchainObject> getPendingObjects(String model) {
        logger.trace("START getPendingObjects(String)");

        HashMap<AttrID, BlockchainObject> result = new HashMap();

        for (Map.Entry<String, PendingTransactionInformation> entry : Main.pendingTransaction.entrySet()) {
            PendingTransactionInformation pendingTransactionInformation = entry.getValue();
            String transactionID = entry.getKey();

            if (pendingTransactionInformation.object.getModel().equals(model)) {

                logger.trace("transactionID: " + transactionID);
                logger.trace("Last Parent  : " + pendingTransactionInformation.object.getParentList().getLast());

                result.put(pendingTransactionInformation.object.getParentList().getLast(), pendingTransactionInformation.object);
            }
        }

        logger.trace("END getPendingObjects()");
        return result;
    }

    private class AwaitingThread extends Thread {
        private boolean waiting = false;
        private long maxWaitingTime = Long.MAX_VALUE;
        private String name;

        public AwaitingThread(long maxWaitingTime, String name) {
            super(name);
            this.maxWaitingTime = maxWaitingTime;
            this.name = name;
        }

        public boolean isWaiting() {
            return this.waiting;
        }

        public void setWaiting(boolean waiting) {
            this.waiting = waiting;
            //         logger.warn("Set waiting " + waiting + ": " + name);
        }

        @Override
        public void run() {

            waiting = true;
            long startTime = LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli();
            long now = startTime;

            while (waiting && now - startTime < maxWaitingTime) {

                try {
                    Thread.sleep(24);
                } catch (InterruptedException e) {
                    waiting = false;
                    /*
                    logger.error("Error in waiting thread: " + e.toString());
                    e.printStackTrace();
                    */
                }


                //logger.info("wait : " + name);
                now = LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli();
            }

            if (waiting && now - startTime >= maxWaitingTime) {

                if (miningBlock == null) {
                    /*
                    logger.debug("waiting: " + waiting);
                    logger.debug("now      : " + now);
                    logger.debug("startTime: " + startTime);
                    */
                    //                 logger.warn("Pending transactions changed: Max waiting time reached (" + name + "), start mining");
                    lastMiningTime = LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli();
                    //triggerRestartMining();
                    restartMining();
                    stopAllAwaitingThreads();
                }
            } else {
                //             logger.warn("STOP awaiting thread for " + name);
            }

            waiting = false;
        }
    }

}
