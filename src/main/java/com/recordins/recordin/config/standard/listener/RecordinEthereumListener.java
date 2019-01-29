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

package com.recordins.recordin.config.standard.listener;

import com.recordins.recordin.ApplicationContext;
import com.recordins.recordin.Main;
import com.recordins.recordin.PendingTransactionInformation;
import com.recordins.recordin.config.standard.mine.RecordinBlockMiner;
import com.recordins.recordin.orm.BlockchainObject;
import com.recordins.recordin.orm.User;
import com.recordins.recordin.orm.attribute.AttrID;
import com.recordins.recordin.orm.exception.ORMException;
import com.recordins.recordin.orm.core.BlockchainObjectIndex;
import com.recordins.recordin.orm.core.BlockchainObjectReader;
import com.recordins.recordin.orm.core.BlockchainObjectWriter;
import com.recordins.recordin.utils.DeepCopy;
import org.cheetah.webserver.CheetahWebserver;
import org.ethereum.core.*;
import org.ethereum.util.BIUtil;

import java.math.BigInteger;
import java.util.*;

import org.ethereum.config.SystemProperties;
import org.ethereum.facade.Ethereum;
import org.ethereum.listener.CompositeEthereumListener;
import org.ethereum.net.eth.message.StatusMessage;
import org.ethereum.net.message.Message;
import org.ethereum.net.p2p.HelloMessage;
import org.ethereum.net.rlpx.Node;
import org.ethereum.net.server.Channel;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

public class RecordinEthereumListener extends CompositeEthereumListener {
//public class RecordinEthereumListener implements EthereumListener {


    private static final Logger logger = LoggerFactory.getLogger(RecordinEthereumListener.class);

    //@Autowired Not working
    private Ethereum ethereum = null;

    //@Autowired not working
    private SystemProperties config = null;

    //private DbFlushManager dbFlushManager = (DbFlushManager) ApplicationContext.getInstance().get("dbFlushManager");

    private long counter = 1l;

    public RecordinEthereumListener() {
        super();

        logger.trace("START RecordinEthereumListener()");
        logger.trace("END RecordinEthereumListener()");
    }

    public static void sendJSONMenu() {
        logger.trace("START sendJSONMenu()");

        if (CheetahWebserver.getInstance() != null) {
            for (String username : CheetahWebserver.getInstance().getUserNames("org.cheetah.webserver.page.websocket.Recordin")) {

                logger.debug("User: " + username);

                User user = null;
                if (!CheetahWebserver.getInstance().isSessionAuthenticationEnabled()) {
                    user = User.getAdminUser();
                    user.setSessionCookie(username);
                } else {
                    user = User.getUser(username);
                }
                sendJSONMenu(user);
            }
        }

        logger.trace("END sendJSONMenu()");
    }

    public static void sendJSONMenu(User user) {
        logger.trace("START sendJSONMenu(User)");

        JSONObject jsonResult = new JSONObject();
        JSONArray jsonMenu = new JSONArray();

        try {
            BlockchainObjectReader reader = BlockchainObjectReader.getInstance(user);
            jsonMenu = reader.searchMenu();

        } catch (ORMException ex) {
            logger.error("Error building menu JSON: " + ex.toString());
        }

        jsonResult.put("MessageType", "MenuUpdate");
        jsonResult.put("MessageValue", jsonMenu);

        logger.debug("Websocket Message: " + jsonResult.toJSONString());

        Main.sendWebsocket(jsonResult.toJSONString(), user);

        logger.trace("END sendJSONMenu()");
    }

    public static void sendJSONStatus() {
        logger.trace("START sendJSONStatus()");

        JSONObject jsonResult = new JSONObject();

        jsonResult.put("MessageType", "Status");
        jsonResult.put("MessageValue", Main.getInstance().getNodeStatus());

        CheetahWebserver.getInstance().distributeToWebsocketServiceMessage("org.cheetah.webserver.page.websocket.Recordin", jsonResult.toJSONString());

        logger.trace("END sendJSONStatus()");
    }

    @Override
    public void trace(String output) {
        logger.trace("Listener trace: " + output);
    }

    @Override
    public void onSyncDone(org.ethereum.listener.EthereumListener.SyncState state) {
        logger.debug("Listener onSyncDone: " + state);
        if (!Main.platformStarted && state.equals(SyncState.COMPLETE)) {
            Main.platformStarted = true;
            Main.syncComplete = true;

            while (Main.getInstance() == null) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                }
            }
            Main.getInstance().startPlatform();
        }
    }

    @Override
    public void onNodeDiscovered(Node node) {
        logger.debug("Listener onNodeDiscovered: " + node);
    }

    @Override
    public void onEthStatusUpdated(Channel channel, StatusMessage statusMessage) {
        logger.debug("Listener onEthStatusUpdated: " + statusMessage);
        logger.debug("Listener onEthStatusUpdated: " + channel.getNode().getHost() + ":" + channel.getNode().getPort());
        Main.ethNodes.put(channel.getNode(), statusMessage);
    }

    @Override
    public void onPeerAddedToSyncPool(Channel peer) {
        logger.debug("Listener onPeerAddedToSyncPool: " + peer);

        if (Main.activePeers.size() < 100) {
            logger.debug("Adding channel: " + peer);
            Main.activePeers.add(peer);
        }

        /*
        String URLString = "{url = 'enode://" + peer.getPeerId() + "@" + peer.getInetSocketAddress().getHostString() + ":" + config.listenPort() + "'}";

        logger.debug("Adding node to properties file: " + URLString);
        ApplicationContext applicationcontext = ApplicationContext.getInstance();

        String peerActive = applicationcontext.getString("PeerActive");
        if (peerActive == null || peerActive.length() <= 2) {
            peerActive = "[" + URLString + "]";
        } else {
            if (!peerActive.contains(peer.getPeerId())) {
                peerActive = peerActive.substring(0, peerActive.length() - 1) + "," + URLString + "]";
            }
        }
        applicationcontext.replace("PeerActive", peerActive);
        applicationcontext.storeProperties();
         */
        //Main.syncPeers.add(peer.getNode());
    }

    @Override
    public void onBlock(BlockSummary blockSummary, boolean best) {
        onBlock(blockSummary);
    }


    @Override
    public void onBlock(BlockSummary blockSummary) {
        logger.debug("Listener START onBlock");

        Main.processingBlock = true;

        Block block = blockSummary.getBlock();
        HashMap<BlockchainObject, String> blockchainObjectsMap = new HashMap();
        ArrayList<BlockchainObject> blockchainObjectsList = new ArrayList();

        StringBuilder debugstring = new StringBuilder();
        debugstring.append(System.lineSeparator());

        debugstring.append("").append(System.lineSeparator());
        debugstring.append("======================================================").append(System.lineSeparator());
        debugstring.append("New block: " + block.getShortDescr()).append(System.lineSeparator());

        if (logger.isDebugEnabled()) {
            debugstring.append(calcNetHashRate(block)).append(System.lineSeparator());
        }

        long blockNumber = (long) DeepCopy.copy(block.getNumber());
        if (blockNumber > 0) {

            if (ethereum == null) {
                ethereum = (Ethereum) ApplicationContext.getInstance().get("ethereum");
            }

            if (config == null) {
                config = (SystemProperties) ApplicationContext.getInstance().get("config");
            }

            List<Transaction> transactionList = block.getTransactionsList();

            if (!Main.syncComplete && counter % 50 == 0) {

                Main.flushBlockchainDB();
                if (counter % 50 == 0) {
                    counter++;
                }

                Main.lastImported = blockNumber;

                //logger.debug("Sync running trigger menu refresh");

                if (logger.isDebugEnabled()) {
                    debugstring.append("Sync running trigger menu refresh");
                }

                sendJSONMenu();
                sendJSONStatus();

            } else {
                counter++;
            }

            if (!Main.syncComplete) {
                if (ethereum.getSyncStatus().getBlockBestKnown() == blockNumber) {

                    try {
                        BlockchainObjectIndex.setRebuildIndexflag(false);
                    } catch (ORMException e) {
                        logger.error("Error setting rebuild index flag to false: " + e.toString());
                    }

                    Main.lastImported = blockNumber;
                    Main.syncComplete = true;
                    Main.flushBlockchainDB();

                    if (ethereum.getBlockchain().getBestBlock().getNumber() > 0) {

                        //logger.debug("Sync done trigger menu refresh");

                        if (logger.isDebugEnabled()) {
                            debugstring.append("Sync done trigger menu refresh").append(System.lineSeparator());
                        }

                        sendJSONMenu();
                    }

                    sendJSONStatus();
                }
            } else {
                if (!Main.initFlag && !Main.initDataModelFlag) {
                    Main.flushBlockchainDB();
                }
            }

        /*
        if (synced) {
            ((CasperExtendedBlockchain) ethereum.getBlockchain()).flushDB();
        }
         */

            ORMException exception = null;
            for (Transaction transaction : transactionList) {

                String transactionID = Hex.toHexString(transaction.getHash());

                if (logger.isTraceEnabled()) {
                    debugstring.append("Transaction ID: " + transactionID).append(System.lineSeparator());
                }

                byte[] objectData = transaction.getData();
                String jsonString = new String(objectData);

                BlockchainObject object = null;
                try {

                    if (ethereum.getBlockchain().getBestBlock().getNumber() < 3) {
                        object = BlockchainObjectWriter.getAdminInstance().getObject(jsonString);
                    } else {
                        object = BlockchainObjectWriter.getGuestInstance().getObject(jsonString);
                    }

                    if (logger.isDebugEnabled()) {
                        debugstring.append("Object Name from block (" + blockNumber + "): '" + object.getDisplayName() + "' Model: '" + object.getModel() + "'").append(System.lineSeparator());
                    }
                    blockchainObjectsList.add(object);
                    blockchainObjectsMap.put(object, transactionID);


                } catch (ORMException ex) {
                    logger.error("Error updating indexes from block (read objects): " + ex.toString());
                    exception = ex;
                    break;
                }

            }

            Collections.sort(blockchainObjectsList);

            for (BlockchainObject object : blockchainObjectsList) {

                String transactionID = blockchainObjectsMap.get(object);

                if (exception == null) {
                    try {

                        AttrID id = new AttrID(blockNumber + "." + transactionID + "." + object.getUid());
                        object.setId(id);
                        BlockchainObjectIndex.getInstance().updateIndexes(object, id, false);

                    } catch (ORMException ex) {
                        logger.error("Error updating indexes from block: " + ex.toString());
                        exception = ex;
                    }
                }

                if (Main.pendingTransaction.containsKey(transactionID)) {

                    //logger.trace("Transaction NOTIFY: " + transactionID);
                    if (logger.isTraceEnabled()) {
                        debugstring.append("Transaction NOTIFY: " + transactionID).append(System.lineSeparator());
                    }
                    PendingTransactionInformation pendingTransactionInformation = Main.pendingTransaction.get(transactionID);
                    pendingTransactionInformation.blockID = blockNumber;
                    pendingTransactionInformation.exception = exception;

                    Transaction tx = pendingTransactionInformation.transaction;
                    synchronized (tx) {
                        tx.notify();
                    }
                }

                if (CheetahWebserver.getInstance() != null) {

                    if (object != null) {
                        if (object.getModel().equals("Menu")) {

                            //logger.debug("Trigger menu refresh");
                            if (logger.isDebugEnabled()) {
                                debugstring.append("Trigger menu refresh").append(System.lineSeparator());
                            }
                            sendJSONMenu();
                        }
                    }
                }
            }

            try {
                BlockchainObjectIndex.getInstance().commitAllIndexes();
            } catch (ORMException e) {
                logger.error("Error commit indexes from block: " + e.toString());
                exception = e;
                e.printStackTrace();
            }


            ((RecordinBlockMiner) ethereum.getBlockMiner()).setLastMiningBlockNumber(block.getNumber());

            Main.processingBlock = false;
        }

        debugstring.append("======================================================").append(System.lineSeparator());
        debugstring.append("").append(System.lineSeparator());

        if (logger.isDebugEnabled()) {
            logger.debug(debugstring.toString());
        } else {
            logger.info(debugstring.toString());
        }

        logger.trace("Listener END onBlock");
    }

    @Override
    public void onRecvMessage(Channel channel, Message message) {
        logger.trace("Listener onRecvMessage: " + message);
    }

    @Override
    public void onSendMessage(Channel channel, Message message) {
        logger.trace("Listener onSendMessage: " + message);
    }

    @Override
    public void onPeerDisconnect(String host, long port) {
        logger.debug("Listener onPeerDisconnect: " + host);
    }

    @Override
    public void onPendingTransactionsReceived(List<Transaction> transactions) {
        logger.debug("Listener onPendingTransactionsReceived - Transaction number: " + transactions.size());

        for (Transaction transaction : transactions) {
            String transactionID = Hex.toHexString(transaction.getHash());
            logger.debug("transactionID: " + transactionID);
        }
    }

    @Override
    public void onPendingStateChanged(PendingState pendingState) {
        logger.debug("Listener onPendingStateChanged - Transaction number: " + pendingState.getPendingTransactions().size());

        if (ethereum != null) {
            if (pendingState.getPendingTransactions().size() > 0) {
                ((RecordinBlockMiner) ethereum.getBlockMiner()).onPendingStateChanged();
            } else {
                ((RecordinBlockMiner) ethereum.getBlockMiner()).cancelIfMiningBlock();
            }
        }
    }

    @Override
    public void onHandShakePeer(Channel channel, HelloMessage helloMessage) {
        logger.debug("Listener onHandShakePeer: " + helloMessage);
    }

    @Override
    public void onNoConnections() {
        logger.debug("Listener onNoConnections");
    }

    @Override
    public void onVMTraceCreated(String transactionHash, String trace) {
        logger.debug("Listener onVMTraceCreated: " + trace);
    }

    @Override
    public void onTransactionExecuted(TransactionExecutionSummary summary) {
        logger.trace("Listener onTransactionExecuted: isFailed():" + summary.isFailed());
        logger.trace("Listener onTransactionExecuted: getHash() :" + Hex.toHexString(summary.getTransaction().getHash()));
    }

    @Override
    public void onPendingTransactionUpdate(TransactionReceipt txReceipt, org.ethereum.listener.EthereumListener.PendingTransactionState state, Block block) {
        logger.trace("Listener onPendingTransactionUpdate: " + state);

        if (state.toString().equals(org.ethereum.listener.EthereumListener.PendingTransactionState.DROPPED.toString())) {
            logger.debug("Listener onPendingTransactionUpdate DROPPED");


            Transaction transaction = txReceipt.getTransaction();
            String transactionID = Hex.toHexString(transaction.getHash());
            if (Main.pendingTransaction.containsKey(transactionID)) {

                logger.debug("Transaction NOTIFY: " + transactionID);
                PendingTransactionInformation pendingTransactionInformation = Main.pendingTransaction.get(transactionID);
                pendingTransactionInformation.exception = new ORMException("Transaction DROPPED: " + txReceipt.getError());

                Transaction tx = pendingTransactionInformation.transaction;
                synchronized (tx) {
                    tx.notify();
                }

                BigInteger nonce = new BigInteger(transaction.getNonce());
                if (txReceipt.getError().contains("Invalid nonce") && nonce.compareTo(BigInteger.valueOf(0)) > 0) {

                    // resend transaction with new nonce...
                    logger.warn("Resending transaction: " + transactionID);

                    try {

                        byte[] objectData = transaction.getData();
                        String jsonString = new String(objectData);

                        BlockchainObject object = BlockchainObjectWriter.getGuestInstance().getObject(jsonString);
                        BlockchainObjectWriter.getAdminInstance().write(object, true);

                    } catch (ORMException e) {
                        logger.error("Error resending transaction: " + e.toString());
                    }
                }
            } else {

                byte[] objectData = transaction.getData();
                String jsonString = new String(objectData);

                PendingTransactionInformation pendingTransactionInformation = new PendingTransactionInformation(transaction);

                try {
                    BlockchainObject object = BlockchainObjectWriter.getGuestInstance().getObject(jsonString);

                    if (ethereum == null) {
                        ethereum = (Ethereum) ApplicationContext.getInstance().get("ethereum");
                    }

                    if (ethereum.getBlockchain().getBestBlock().getNumber() < 2) {
                        object = BlockchainObjectWriter.getAdminInstance().getObject(jsonString);
                    } else {
                        object = BlockchainObjectWriter.getGuestInstance().getObject(jsonString);
                    }
                    pendingTransactionInformation.uid = object.getUid();
                    pendingTransactionInformation.object = object;

                } catch (ORMException ex) {
                    logger.error("Error updating pending Transaction from block: " + ex.toString());
                }

                Main.pendingTransaction.put(transactionID, pendingTransactionInformation);
                pendingTransactionInformation.exception = new ORMException("Transaction DROPPED: " + txReceipt.getError());
            }
        }
    }

    /**
     * Just small core to estimate total power off all miners on the net
     *
     * @param block
     */
    private String calcNetHashRate(Block block) {

        if (block.getNumber() > 10) {

            long avgTime = 1;
            long cumTimeDiff = 0;
            Block currBlock = block;

            if (ethereum == null) {
                ethereum = (Ethereum) ApplicationContext.getInstance().get("ethereum");
            }

            for (int i = 0; i < 10; ++i) {

                Block parent = ethereum.getBlockchain().getBlockByHash(currBlock.getParentHash());
                long diff = currBlock.getTimestamp() - parent.getTimestamp();
                cumTimeDiff += Math.abs(diff);
                currBlock = parent;
            }

            avgTime = cumTimeDiff / 10;

            BigInteger netHashRate = block.getDifficultyBI().divide(BIUtil.toBI(avgTime));
            double hashRate = netHashRate.divide(new BigInteger("1")).doubleValue();

            return "Net hash rate: " + hashRate + " H/s";
        }
        return "";
    }
}
