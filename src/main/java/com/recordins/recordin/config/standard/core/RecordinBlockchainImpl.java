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

package com.recordins.recordin.config.standard.core;

import org.ethereum.config.BlockchainConfig;
import org.ethereum.config.SystemProperties;
import org.ethereum.core.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.ethereum.core.ImportResult.*;
import static org.ethereum.core.ImportResult.IMPORTED_BEST;
import static org.ethereum.core.ImportResult.NO_PARENT;
import static org.ethereum.util.BIUtil.toBI;
import static org.ethereum.util.ByteUtil.toHexString;

public class RecordinBlockchainImpl extends BlockchainImpl {

    private static final Logger logger = LoggerFactory.getLogger(RecordinBlockchainImpl.class);


    @Autowired
    public RecordinBlockchainImpl(final SystemProperties config) {
        super(config);
    }


    @Override
    protected Map<byte[], BigInteger> addReward(Repository track, Block block, List<TransactionExecutionSummary> summaries) {

        Map<byte[], BigInteger> rewards = new HashMap<>();

        /*
        BigInteger blockReward = config.getBlockchainConfig().getConfigForBlock(block.getNumber()).getConstants().getBLOCK_REWARD();
        BigInteger inclusionReward = blockReward.divide(BigInteger.valueOf(32));

        // Add extra rewards based on number of uncles
        if (block.getUncleList().size() > 0) {
            for (BlockHeader uncle : block.getUncleList()) {
                BigInteger uncleReward = blockReward
                        .multiply(BigInteger.valueOf(MAGIC_REWARD_OFFSET + uncle.getNumber() - block.getNumber()))
                        .divide(BigInteger.valueOf(MAGIC_REWARD_OFFSET));

                track.addBalance(uncle.getCoinbase(),uncleReward);
                BigInteger existingUncleReward = rewards.get(uncle.getCoinbase());
                if (existingUncleReward == null) {
                    rewards.put(uncle.getCoinbase(), uncleReward);
                } else {
                    rewards.put(uncle.getCoinbase(), existingUncleReward.add(uncleReward));
                }
            }
        }

        BigInteger minerReward = blockReward.add(inclusionReward.multiply(BigInteger.valueOf(block.getUncleList().size())));

        BigInteger totalFees = BigInteger.ZERO;
        for (TransactionExecutionSummary summary : summaries) {
            totalFees = totalFees.add(summary.getFee());
        }

        rewards.put(block.getCoinbase(), minerReward.add(totalFees));
        track.addBalance(block.getCoinbase(), minerReward); // fees are already given to the miner during tx execution
        */
        return rewards;
    }


    @Override
    protected BlockSummary applyBlock(Repository track, Block block) {

        //    logger.warn("APPLY BLOCK, repo: " + track.toString());

        logger.trace("applyBlock: block: [{}] tx.list: [{}]", block.getNumber(), block.getTransactionsList().size());

        BlockchainConfig blockchainConfig = config.getBlockchainConfig().getConfigForBlock(block.getNumber());
        blockchainConfig.hardForkTransfers(block, track);

        long saveTime = System.nanoTime();
        int i = 1;
        long totalGasUsed = 0;
        List<TransactionReceipt> receipts = new ArrayList<>();
        List<TransactionExecutionSummary> summaries = new ArrayList<>();

        JSONParser parser = new JSONParser();

        for (Transaction tx : block.getTransactionsList()) {
            stateLogger.debug("apply block: [{}] tx: [{}] ", block.getNumber(), i);

            Repository txTrack = track.startTracking();
            TransactionExecutor executor = new TransactionExecutor(
                    tx, block.getCoinbase(),
                    txTrack, blockStore, programInvokeFactory, block, listener, totalGasUsed, vmHook)
                    .withCommonConfig(commonConfig);

            executor.init();
            executor.execute();
            executor.go();

            // if new user, then give some money
            String jsonString = new String(tx.getData());

            try {
                JSONArray jsonArray = (JSONArray) parser.parse(jsonString);

                //logger.info("jsonObject BLOCK: " + jsonArray);

                if (((JSONObject) jsonArray.get(1)).get("model").toString().equals("User")) {


                    String addressString = ((JSONArray) ((JSONObject) jsonArray.get(1)).get("address")).get(1).toString();
                    String uidString = ((JSONObject) jsonArray.get(1)).get("uid").toString();
                    String userNanme = ((JSONObject) jsonArray.get(1)).get("displayName").toString();
                    byte[] address = Hex.decode(addressString);

                    String user = ((JSONObject) jsonArray.get(1)).get("login").toString();
                    if (!user.equals("guest") && (txTrack.getAccountState(address) == null || txTrack.getAccountState(address).getBalance().equals(BigInteger.ZERO))) {
                        logger.debug("Init account balance for user: '" + user + "'");
                        txTrack.addBalance(address, BigInteger.valueOf(1_000_000_000_000_000_000l).multiply(BigInteger.valueOf(1_000_000l)));
                    }
                }
                //JSONObject jsonObjectAttr = (JSONObject) ((JSONArray) ((JSONObject) jsonArray.get(1)).get("attrMap")).get(1);


            } catch (ParseException e) {
                logger.error("Error updating user accounts from block (read objects): " + e.toString());
                e.printStackTrace();
            }


            TransactionExecutionSummary summary = executor.finalization();

            totalGasUsed += executor.getGasUsed();

            // refund the transaction sender
            BigInteger txGasLimit = toBI(tx.getGasLimit());
            BigInteger txGasCost = toBI(tx.getGasPrice()).multiply(txGasLimit);

            logger.trace("REFUND TX: " + Hex.toHexString(tx.getHash()));
            logger.trace("REFUND Address: " + Hex.toHexString(tx.getSender()));

            txTrack.addBalance(tx.getSender(), txGasCost);

            txTrack.commit();

            final TransactionReceipt receipt = executor.getReceipt();

            if (blockchainConfig.eip658()) {
                receipt.setTxStatus(receipt.isSuccessful());
            } else {
                receipt.setPostTxState(track.getRoot());
            }

            if (stateLogger.isInfoEnabled())
                stateLogger.info("block: [{}] executed tx: [{}] \n  state: [{}]", block.getNumber(), i,
                        toHexString(track.getRoot()));

            stateLogger.info("[{}] ", receipt.toString());

            if (stateLogger.isInfoEnabled())
                stateLogger.info("tx[{}].receipt: [{}] ", i, toHexString(receipt.getEncoded()));

            // TODO
//            if (block.getNumber() >= config.traceStartBlock())
//                repository.dumpState(block, totalGasUsed, i++, tx.getHash());

            receipts.add(receipt);
            if (summary != null) {
                summaries.add(summary);
            }
        }

        Map<byte[], BigInteger> rewards = addReward(track, block, summaries);
        //Map<byte[], BigInteger> rewards = new HashMap<>();

        if (stateLogger.isInfoEnabled())
            stateLogger.info("applied reward for block: [{}]  \n  state: [{}]",
                    block.getNumber(),
                    toHexString(track.getRoot()));


        // TODO
//        if (block.getNumber() >= config.traceStartBlock())
//            repository.dumpState(block, totalGasUsed, 0, null);

        long totalTime = System.nanoTime() - saveTime;
        adminInfo.addBlockExecTime(totalTime);
        logger.trace("block: num: [{}] hash: [{}], executed after: [{}]nano", block.getNumber(), block.getShortHash(), totalTime);

        return new BlockSummary(block, rewards, receipts, summaries);
    }

    /*
    public synchronized ImportResult tryToConnect(final Block block) {

        if (logger.isDebugEnabled())
            logger.debug("Try connect block hash: {}, number: {}",
                    toHexString(block.getHash()).substring(0, 6),
                    block.getNumber());

        logger.info("blockStore.getMaxNumber()               : " + blockStore.getMaxNumber());
        logger.info("block.getNumber()                       : " + block.getNumber());
        logger.info("blockStore.isBlockExist(block.getHash()): " + blockStore.isBlockExist(block.getHash()));

        if (blockStore.getMaxNumber() >= block.getNumber()) {

            if (logger.isDebugEnabled())
                logger.debug("Block already exist hash: {}, number: {}",
                        toHexString(block.getHash()).substring(0, 6),
                        block.getNumber());

            // retry of well known block
            return EXIST;
        }

        return super.tryToConnect(block);
    }
    */
}
