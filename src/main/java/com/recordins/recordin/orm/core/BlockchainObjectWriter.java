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

package com.recordins.recordin.orm.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.recordins.recordin.ApplicationContext;
import com.recordins.recordin.PendingTransactionInformation;
import com.recordins.recordin.config.standard.mine.RecordinBlockMiner;
import com.recordins.recordin.orm.*;
import com.recordins.recordin.orm.attribute.Attr;
import com.recordins.recordin.orm.attribute.AttrAbstractAttachment;
import com.recordins.recordin.orm.attribute.AttrAttachment;
import com.recordins.recordin.orm.attribute.AttrAttribute;
import com.recordins.recordin.orm.attribute.AttrBoolean;
import com.recordins.recordin.orm.attribute.AttrDateTime;
import com.recordins.recordin.orm.attribute.AttrID;
import com.recordins.recordin.orm.attribute.AttrIDList;
import com.recordins.recordin.orm.attribute.AttrList;
import com.recordins.recordin.orm.attribute.AttrString;
import com.recordins.recordin.orm.exception.ORMException;
import com.recordins.recordin.utils.BlockchainLock;
import com.recordins.recordin.utils.DateFormat;

import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Path;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.Future;

import com.recordins.recordin.Main;
import com.recordins.recordin.utils.DeepCopy;
import org.apache.commons.lang3.ArrayUtils;
import org.cheetah.webserver.CheetahWebserver;
import org.cheetah.webserver.authentication.BlockchainAuthenticatorFactory;
import org.cheetah.webserver.authentication.IBlockchainAuthenticator;
import org.ethereum.config.SystemProperties;
import org.ethereum.core.Transaction;
import org.ethereum.crypto.ECKey;
import org.ethereum.crypto.HashUtil;
import org.ethereum.facade.Blockchain;
import org.ethereum.facade.Ethereum;
import org.ethereum.facade.Repository;
import org.ethereum.net.client.Capability;
import org.ethereum.net.server.Channel;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

import static com.recordins.recordin.Main.initIDSModels;
import static com.recordins.recordin.Main.initModelNameIDS;
import static com.recordins.recordin.Main.sendWebsocket;
import static org.ethereum.util.BIUtil.toBI;

public class BlockchainObjectWriter {

    /* Logger for console output */
    private static final Logger logger = LoggerFactory.getLogger(BlockchainObjectWriter.class);

    /* Mapper for Java <-> Json conversion */
    private ObjectMapper mapper = new ObjectMapper().enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);

    /* User for permissions evaluation */
    private User user;

    //@Autowired not working
    private Ethereum ethereum = (Ethereum) ApplicationContext.getInstance().get("ethereum");

    //@Autowired not working
    private SystemProperties config = (SystemProperties) ApplicationContext.getInstance().get("config");

    private ConcurrentHashMap<String, BigInteger> addressNonces = new ConcurrentHashMap();

    private static BlockchainLock<String> writerLockNonce = new BlockchainLock();

    /**
     * Returns an blockchainObjectIndex of {@link BlockchainObjectWriter} with
     * admin priviledges
     *
     * @return
     */
    public static BlockchainObjectWriter getAdminInstance() throws ORMException {
        logger.trace("START getAdminInstance()");
        logger.trace("END getAdminInstance()");
        return new BlockchainObjectWriter(User.getAdminUser());
    }

    /**
     * Returns an blockchainObjectIndex of {@link BlockchainObjectWriter} with
     * guest priviledges
     *
     * @return
     */
    public static BlockchainObjectWriter getGuestInstance() throws ORMException {
        logger.trace("START getGuestInstance()");
        logger.trace("END getGuestInstance()");
        return new BlockchainObjectWriter(User.getGuestUser());
    }

    /**
     * Returns an blockchainObjectIndex of {@link BlockchainObjectWriter}
     *
     * @param user, the user for permissions evaluation
     * @return
     */
    public static BlockchainObjectWriter getInstance(User user) {
        logger.trace("START getInstance(User)");
        logger.trace("END getInstance()");
        return new BlockchainObjectWriter(user);
    }

    /**
     * Returns an blockchainObjectIndex of {@link BlockchainObjectWriter}
     *
     * @param id, the user's {@link AttrID} id for permissions evaluation
     * @return
     * @throws ORMException
     */
    public static BlockchainObjectWriter getInstance(AttrID id) throws ORMException {
        logger.trace("START getInstance(AttrID)");
        logger.trace("END getInstance()");
        return new BlockchainObjectWriter(id);
    }

    /**
     * Returns an blockchainObjectIndex of {@link BlockchainObjectWriter}
     *
     * @param id, the user's {@link Long} id for permissions evaluation
     * @return
     * @throws ORMException
     */
    public static BlockchainObjectWriter getInstance(String id) throws ORMException {
        logger.trace("START getInstance(String)");
        logger.trace("END getInstance()");
        return new BlockchainObjectWriter(id);
    }

    private BlockchainObjectWriter() {
        logger.trace("START BlockchainObjectWriter()");
        logger.trace("END BlockchainObjectWriter()");
    }

    private BlockchainObjectWriter(User user) {
        this();
        logger.trace("START BlockchainObjectWriter(User)");
        this.user = user;
        logger.trace("END BlockchainObjectWriter()");
    }

    private BlockchainObjectWriter(String id) throws ORMException {
        this();
        logger.trace("START BlockchainObjectWriter(String)");
        this.user = BlockchainObjectReader.getAdminInstance().readUser(new AttrID(id));
        logger.trace("END BlockchainObjectWriter()");
    }

    private BlockchainObjectWriter(AttrID id) throws ORMException {
        this();
        logger.trace("START BlockchainObjectWriter(AttrID)");
        this.user = BlockchainObjectReader.getAdminInstance().readUser(id);
        logger.trace("END BlockchainObjectWriter()");
    }

    /**
     * Writes a {@link BlockchainObject} into the blockchain
     *
     * @param object {@link BlockchainObject} representation of the object
     * @throws ORMException
     */
    public AttrID write(BlockchainObject object) throws ORMException {
        logger.trace("START write(BlockchainObject)");
        logger.trace("END write()");
        return write(object, true);
    }

    public String writeAsync(BlockchainObject object) throws ORMException {
        write(object, true);


        try {
            synchronized (object) {
                object.wait();
            }
        } catch (InterruptedException ex) {
        }

        for (PendingTransactionInformation pendingTransactionInformation : Main.pendingTransaction.values()) {
            if (pendingTransactionInformation.uid.equals(object.getUid())) {

                if (pendingTransactionInformation.exception != null) {
                    throw pendingTransactionInformation.exception;
                }
                return Hex.toHexString(pendingTransactionInformation.transaction.getHash());
            }
        }

        return "";
    }

    public AttrID write(BlockchainObject object, boolean asynchronous) throws ORMException {
        logger.trace("START write(BlockchainObject, Boolean)");
        String jsonString = "";
        String newUid = "";

        if (ApplicationContext.getInstance().getBoolean("NodeReadOnly")) {
            throw new ORMException("Blockchain node is Read Only");
        }

        //Set a new UID if it is a new object
        if (object.isEmptyParentList()) {
            newUid = UUID.randomUUID().toString();

            // If platform is initiating
            // Keep models in creation inside a temp hashmap
            if (Main.initDataModelFlag) {
                if (object.getModel().equals("Model")) {

                    Model model = (Model) object;

                    String id = "" + (ethereum.getBlockchain().getBestBlock().getNumber() + 1) + "." + "init" + "." + newUid;

                    initModelNameIDS.put(model.getName(), id);
                    initIDSModels.put(id, model);
                }
            }
        }

        // If platform is initiating
        // replace their text name with their new ID instead
        if (Main.initDataModelFlag) {
            if (object.getModel().equals("Model")) {

                Model model = (Model) object;

                AttrList<AttrAttribute> attributesList = model.getAttributes();

                for (Attr attr : attributesList) {
                    AttrAttribute attribute = (AttrAttribute) attr;

                    if (attribute.AttrTypeModel != null && !attribute.AttrTypeModel.equals("")) {

                        String modelID = initModelNameIDS.get(attribute.AttrTypeModel);
                        if (modelID != null) {
                            attribute.AttrTypeModel = modelID;
                        }
                    }
                }
                object = model;
            }
        }

        Model model = null;

        if (!Main.initDataModelFlag || (Main.initDataModelFlag && !object.getModel().equals("User"))) {

            if (Main.initDataModelFlag) {

                if (object.get("Name").toString().equals("Model")) {
                    //logger.debug("modelID: " + initModelNameIDS.get(object.get("Name").toString()));
                    //logger.debug("model: " + initIDSModels.get(initModelNameIDS.get(object.get("Name").toString())));

                    model = initIDSModels.get(initModelNameIDS.get(object.get("Name").toString()));
                } else if (object.getModel().equals("Model")) {
                    model = initIDSModels.get(initModelNameIDS.get("Model"));
                }
            } else {
                model = BlockchainObject.getModel(object);
            }
        }

        if (model != null && object.getModelID() == null) {
            object.setModelID(model.getId());
        }

        object.setDisplayName(buildDisplayName(object, model));

        logger.debug("");
        logger.debug("Write object: " + object.getDisplayName());
        logger.debug("Write validate object with datamodel");


        if (!Main.initDataModelFlag || (Main.initDataModelFlag && !object.getModel().equals("User"))) {
            validateObjectWithDataModel(object, model);
        }

        logger.debug("Write call object method 'validate'");
        object.validate();

        AttrDateTime time = new AttrDateTime();

        AttrID newID = null;
        try {

            if (object.isEmptyParentList()) {

                object.setUid(newUid);
                if (ethereum.getBlockchain().getBestBlock().getNumber() == 0) {

                    if (object.getDisplayName().equals(Main.userAdminName)) {
                        user.setId(new AttrID("1.init." + object.getUid()));
                    }
                }
                if (object.getUserOwnerID() == null) {
                    object.setUserOwner(user);
                }
                object.setUserCreate(user);
                object.setDateTimeCreate(time);

                if (!Main.initDataModelFlag && !checkCreateAccess(object, user)) {
                    throw new ORMException("You are not granted to perform object creation");
                }

                logger.debug("Write call object method 'create'");
                object.create(user);

            } else {
                object.setUserUpdate(user);
                object.setDateTimeUpdate(time);

                if (!Main.initDataModelFlag && !checkWriteAccess(object, user)) {
                    throw new ORMException("You are not granted to perform object write");
                }
                logger.debug("Write call object method 'write'");
                object.write(user);
            }

            object.setNodeUpdateID(new AttrString(Hex.toHexString(config.nodeId())));
            object.setDisplayName(buildDisplayName(object, model));

            Path filePath = null;

            for (Entry<String, Attr> entry : object.entrySet()) {
                Attr attr = entry.getValue();
                if (AttrAbstractAttachment.class.isAssignableFrom(attr.getClass())) {
                    AttrAbstractAttachment attachement = (AttrAbstractAttachment) attr;

                    String attrString = attachement.toString();
                    logger.debug(entry.getKey() + ": " + attrString);

                    if (attachement.size() > 0) {

                        String filename = attachement.get(0);
                        if (filename.startsWith("new_")) {
                            attachement = attachement.writeAttachment(attachement, time, object.getUid(), user);
                            object.replace(entry.getKey(), attachement);
                        }
                    }
                }
            }

            if (GDPRObject.class.isAssignableFrom(object.getClass())) {

                Path rootPath = AttachmentStore.getInstance().getRootPath();
                Path uidFolder = AttachmentStore.getInstance().getStorePath(object.getUid());
                String filename = DateFormat.LongtoStorageDate(time.longValue()) + "-attrMap.json";
                filePath = uidFolder.resolve(filename);
                filePath = rootPath.relativize(filePath);

                String originNode = "";

                originNode = Hex.toHexString(config.nodeId());
                jsonString = mapper.writeValueAsString(object);

                JSONArray jsonArray = (JSONArray) new JSONParser().parse(jsonString);
                JSONObject jsonObjectAttr = (JSONObject) ((JSONArray) ((JSONObject) jsonArray.get(1)).get("attrMap")).get(1);

                String signatureString = AttrAbstractAttachment.getSignature(jsonObjectAttr.toJSONString().getBytes(), user);
                AttrAttachment attachment = new AttrAttachment(filename, filePath.toString(), signatureString, user.getId().toString(), originNode);

                ((GDPRObject) object).setAttachment(attachment);
            }


            jsonString = mapper.writeValueAsString(object);

            JSONArray jsonArray = (JSONArray) new JSONParser().parse(jsonString);

            if (GDPRObject.class.isAssignableFrom(object.getClass())) {

                JSONObject jsonObjectAttr = (JSONObject) ((JSONArray) ((JSONObject) jsonArray.get(1)).get("attrMap")).get(1);
                ((JSONArray) ((JSONObject) jsonArray.get(1)).get("attrMap")).set(1, new JSONObject());

                logger.debug("Write GDPRObject attrMap: " + jsonObjectAttr);

                if (filePath != null) {
                    Path rootPath = AttachmentStore.getInstance().getRootPath();
                    FileWriter writer = new FileWriter(rootPath.resolve(filePath).toFile());

                    writer.write(jsonObjectAttr.toJSONString());
                    writer.flush();
                    writer.close();
                }
            }

            logger.debug("Write jsonString: " + jsonArray.toJSONString());

            newID = write(jsonArray, object, asynchronous);
        } catch (ORMException | IOException | ParseException ex) {
            logger.error("Error writing Object: " + ex.toString());
            throw new ORMException("Error writing Object: " + ex.toString());
        }

        if (newID != null) {
            logger.debug("Write set id: " + newID + " to object: " + object.getDisplayName());
            object.setId(newID);
        }

        logger.trace("END write()");
        return newID;
    }

    /**
     * Inserts an object into the blockchain using its {@code JSONArray} value
     * representation
     *
     * @param jsonArray {@link JSONArray} representation of the object
     * @return the new {@link AttrID} of the inserted object
     * @throws ORMException
     */
    private AttrID write(JSONArray jsonArray, BlockchainObject blockchainObject, boolean asynchronous) throws ORMException {
        logger.trace("START write(JSONArray, BlockchainObject, boolean)");

        AttrID result = null;

        if (!asynchronous) {
            result = writeData(jsonArray, blockchainObject);

            if (CheetahWebserver.getInstance() != null) {

                JSONObject jsonResult = new JSONObject();
                jsonResult.put("MessageType", "SuccessBlock");
                jsonResult.put("MessageValue", "Object succesfully mined: " + blockchainObject.getDisplayName());
                jsonResult.put("MessageModel", blockchainObject.getModel());
                jsonResult.put("MessageObjectID", result.toString());

                sendWebsocket(jsonResult.toJSONString(), user);

            }
        } else {


            Thread t = new Thread() {

                public void run() {

                    //String lockItem = user.getUid().toString();
                    //long cookie = writerLockAsync.lock(1);
                    try {
                        AttrID asyncResult = writeData(jsonArray, blockchainObject);
                        if (CheetahWebserver.getInstance() != null) {

                            JSONObject jsonResult = new JSONObject();
                            jsonResult.put("MessageType", "SuccessBlock");
                            jsonResult.put("MessageValue", "Object succesfully mined: " + blockchainObject.getDisplayName());
                            jsonResult.put("MessageModel", blockchainObject.getModel());
                            jsonResult.put("MessageObjectID", asyncResult.toString());

                            sendWebsocket(jsonResult.toJSONString(), user);

                        }
                    } catch (ORMException ex) {

                        JSONObject jsonResult = new JSONObject();
                        jsonResult.put("MessageType", "Error");
                        jsonResult.put("MessageValue", "Error during object writing: " + ex.toString());

                        if (ex.transaction != null) {

                            String transactionID = Hex.toHexString(ex.transaction.getHash());
                            if (Main.pendingTransaction.containsKey(transactionID)) {
                                logger.trace("Transaction REMOVE: " + transactionID);
                                Main.pendingTransaction.remove(transactionID);
                            }
                        }

                        sendWebsocket(jsonResult.toJSONString(), user);
                        logger.error("Error during asynchronous object writing: " + ex.toString());
                    }

                    //writerLockAsync.unlock(1, cookie);
                }
            };
            t.start();
        }

        logger.trace("END write()");
        return result;
    }

    private BigInteger getNonce(byte[] address) {
        logger.trace("START getNonce(byte[])");

        if (address != null) {
            Repository repo = ethereum.getPendingState();

            return repo.getNonce(address);
        }
        return BigInteger.valueOf(0);
    }

    private Map.Entry<Transaction, String> createTransaction(JSONArray jsonArray) throws ORMException {
        logger.trace("START createAndSubmitTransaction(String)");

        Transaction transaction = null;
        Future<Transaction> future = null;

        String jsonString = jsonArray.toJSONString();

        ECKey sender = ECKey.fromPrivate(Hex.decode(user.getPrivateKey().toString()));
        byte[] senderAddr = sender.getAddress();
        byte[] senderKey = sender.getPrivKeyBytes();
        byte[] receiverAddr = Hex.decode(user.get("Address").toString());

        String model = (String) ((JSONObject) jsonArray.get(1)).get("model");
        String displayName = (String) ((JSONObject) jsonArray.get(1)).get("displayName");

        if (ethereum.getBlockchain().getBestBlock().getNumber() < 1 || (model != null && model.equals("Preferences")) || (!CheetahWebserver.getInstance().isSessionAuthenticationEnabled())) {
            sender = ECKey.fromPrivate(Hex.decode("1c3eaa38a0983eeba090b63b06162ec9dca6a6d3cae448a78ab02ad085351ee5"));
            senderAddr = sender.getAddress();
            senderKey = sender.getPrivKeyBytes();
        }

        String addressString = Hex.toHexString(senderAddr);
        long lockCookie = writerLockNonce.lock(addressString);

        logger.trace("Lock cookie: " + lockCookie);
        BigInteger nonce = getNonce(senderAddr);

        if (model != null && model.equals("Preferences")) {
            Repository repo = ethereum.getPendingState();
            nonce = repo.getNonce(senderAddr);
        }

        int transactionCost = config.getBlockchainConfig().getConfigForBlock(0).getGasCost().getTRANSACTION();
        int nonZeroCost = config.getBlockchainConfig().getConfigForBlock(0).getGasCost().getTX_NO_ZERO_DATA();
        long dataLength = jsonString.getBytes().length;

        logger.trace("Submit     length : " + jsonString.getBytes().length);


        BigInteger gasPrice = BigInteger.valueOf(10_000);
        BigInteger gas = BigInteger.valueOf(transactionCost + dataLength * nonZeroCost);
        BigInteger value = BigInteger.valueOf(0);

        Ethereum ethereum = (Ethereum) ApplicationContext.getInstance().get("ethereum");
        Repository repo = ethereum.getPendingState();
        BigInteger balance = repo.getBalance(senderAddr);

        logger.debug("createTransaction     user: " + user.getDisplayName() + ", address: " + addressString + ", Balance: " + balance.toString());

        transaction = ethereum.createTransaction(nonce, gasPrice, gas, receiverAddr, value, jsonString.getBytes());
        transaction.sign(senderKey);

        return new AbstractMap.SimpleEntry<>(transaction, lockCookie + "|" + addressString);
    }

    private Future<Transaction> submitTransaction(Transaction transaction, long lockCookie, String addressString) throws ORMException {

        boolean mining = ethereum.getBlockMiner().isMining();
        Collection<Channel> activePeers = ethereum.getChannelManager().getActivePeers();

        boolean isOnePeerMining = false;
        String message = "No peer available for mining";
        if (mining) {
            isOnePeerMining = true;
        } else {
            if (activePeers.size() > 0) {

                isOnePeerMining = true;
                for (Channel c : activePeers) {
                    for (Capability capability : c.getNodeStatistics().capabilities) {
                        logger.debug("Peer capability: " + capability.getName());
                    }
                }
            }
        }

        if (!isOnePeerMining) {
            throw new ORMException(message);
        }

        long nonZeroes = transaction.nonZeroDataBytes();
        long zeroVals = ArrayUtils.getLength(transaction.getData()) - nonZeroes;

        logger.trace("Submit  nonZeroes : " + nonZeroes);
        logger.trace("Submit  zeroVals : " + zeroVals);

        logger.debug("Submit  submitting transaction from address: " + addressString);

        Future<Transaction> future = null;
        try {
            future = ethereum.submitTransaction(transaction);
        } catch (Exception e) {
            logger.trace("Unlock cookie: " + lockCookie);
            writerLockNonce.unlock(addressString, lockCookie);
            throw e;
        }
        logger.trace("Unlock cookie: " + lockCookie);
        writerLockNonce.unlock(addressString, lockCookie);

        logger.trace("END createAndSubmitTransaction()");
        return future;
    }

    private AttrID writeData(JSONArray jsonArray, BlockchainObject blockchainObject) throws ORMException {
        logger.trace("START writeData(JSONArray, BlockchainObject)");

        Blockchain blockchain = ethereum.getBlockchain();

        if (blockchain == null) {
            throw new ORMException("Blockchain not found for writing");
        } else {

            Map.Entry<Transaction, String> result = createTransaction(jsonArray);
            Transaction transaction = result.getKey();

            String transactionID = Hex.toHexString(transaction.getHash());
            PendingTransactionInformation pendingTransactionInformation = null;

            if (!Main.pendingTransaction.containsKey(transactionID)) {

                pendingTransactionInformation = new PendingTransactionInformation(transaction);
                pendingTransactionInformation.uid = (String) ((JSONObject) jsonArray.get(1)).get("uid");
                pendingTransactionInformation.object = blockchainObject;
                Main.pendingTransaction.put(transactionID, pendingTransactionInformation);

                synchronized (blockchainObject) {
                    blockchainObject.notify();
                }

                long lockCookie = Long.parseLong(result.getValue().split("\\|")[0]);

                ECKey sender = ECKey.fromPrivate(Hex.decode(user.getPrivateKey().toString()));
                byte[] senderAddr = sender.getAddress();
                String addressString = result.getValue().split("\\|")[1];

                Future<Transaction> future = submitTransaction(transaction, lockCookie, addressString);

                logger.trace("After submit transaction");
                try {
                    logger.trace(future.get().toString());
                } catch (Exception ex) {
                    logger.error("Error during transaction submit: " + ex.toString());
                }

                if (pendingTransactionInformation.exception == null) {
                    try {
                        synchronized (transaction) {
                            logger.debug("Transaction WAIT: " + transactionID);
                            transaction.wait();
                        }
                    } catch (InterruptedException ex) {
                    }
                }
            } else {
                pendingTransactionInformation = Main.pendingTransaction.get(transactionID);
            }

            if (Main.pendingTransaction.containsKey(transactionID)) {
                Main.pendingTransaction.remove(transactionID);
            }

            // find the highest id for transcation's UID
            if (pendingTransactionInformation.exception != null) {
                throw pendingTransactionInformation.exception;
            }

            String newID = pendingTransactionInformation.blockID + "." + transactionID + "." + pendingTransactionInformation.uid;

            logger.trace("END writeData()");
            return new AttrID(newID);
        }
    }

    public Model getModel(String modelName) throws ORMException {
        logger.trace("START getModel(String)");

        BlockchainObjectReader.SearchResult searchResult = BlockchainObjectReader.getInstance(user).search("Model", BlockchainObjectIndex.INDEX_TYPE.VIRTUAL_TABLES_ACTIVE, false, "[[\"Name\",\"=\",\"" + modelName + "\"]]", -1, -1, "");
        ;
        ArrayList<BlockchainObject> objectList = searchResult.getBlockchainObjects();

        if (objectList.size() == 0) {
            throw new ORMException("No model found for name: " + modelName);
        }
        if (objectList.size() > 1) {
            for (BlockchainObject objectFromList : objectList) {
                logger.debug("schema found for model: '" + modelName + "'");
            }
            throw new ORMException("More than one model found for name: " + modelName);
        }

        logger.trace("END getModel()");
        return (Model) objectList.get(0);
    }

    private String buildDisplayName(BlockchainObject object, Model model) throws ORMException {
        logger.trace("START buildDisplayName(BlockchainObject, model)");
        String result = "";

        if (!Main.initDataModelFlag) {
            AttrList<AttrAttribute> attributesList = model.getAttributes();
            String separator = "";

            if (model.get("Display Name Separator") != null) {
                separator = model.get("Display Name Separator").toString();
            }

            int i = 0;
            for (Attr attr : attributesList) {
                AttrAttribute attribute = (AttrAttribute) attr;

                if (attribute.DisplayName) {

                    Object tmp = object.get(attribute.Name);
                    String tmpString = tmp.toString();

                    if (attribute.AttrType.equals("AttrID")) {
                        tmpString = BlockchainObjectReader.getAdminInstance().read(tmpString).getDisplayName();

                    } else if (attribute.AttrType.equals("AttrIDList")) {
                        StringTokenizer tokenizer = new StringTokenizer(tmpString, "[,]");

                        if (!tmpString.equals("[]")) {

                            StringBuilder string = new StringBuilder();
                            string.append("[");

                            int counter = 0;
                            int total = tokenizer.countTokens();
                            BlockchainObjectReader reader = BlockchainObjectReader.getAdminInstance();
                            while (tokenizer.hasMoreElements()) {
                                counter++;

                                tmpString = reader.read(tokenizer.nextToken()).getDisplayName();
                                string.append(tmpString);

                                if (counter < total) {
                                    string.append(",");
                                }
                            }
                            string.append("]");

                            tmpString = string.toString();
                        } else {
                            tmpString = null;
                        }
                    }

                    if (tmp != null && tmpString != null) {
                        if (i == 0) {
                            result += tmpString;
                        } else {
                            if (!tmpString.equals("")) {
                                result += separator + tmpString;
                            }
                        }
                        i++;
                    }
                }
            }
        } else {
            if (result.equals("")) {
                if (object.containsKey("Name")) {
                    if (object.get("Name") != null) {
                        result = ((AttrString) object.get("Name")).toString();
                    }
                }
            }
        }

        logger.trace("END buildDisplayName()");
        return result;
    }

    public void validateObjectWithDataModel(BlockchainObject object, Model model) throws ORMException {
        logger.trace("START validateObject(BlockchainObject, model)");
        validateObjectWithDataModel(object, model, true);
        logger.trace("END validateObject()");
    }

    public void validateObjectWithDataModel(BlockchainObject object, Model model, boolean checkTransient) throws ORMException {
        logger.trace("START validateObject(BlockchainObject, model, boolean)");

        if (object.getDateTimeDelete() != null) {
            throw new ORMException("You cannot update a deleted object !");
        }

        BlockchainObjectIndex blockchainObjectIndex = BlockchainObjectIndex.getInstance();
        BlockchainIndex<String, String> currentVersionsIndex = blockchainObjectIndex.getIndex(BlockchainObjectIndex.INDEX_TYPE.CURRENT_VERSIONS);
        BlockchainIndex<String, ArrayList<String>> indexActive = blockchainObjectIndex.getIndex(BlockchainObjectIndex.INDEX_TYPE.VIRTUAL_TABLES_ACTIVE);
        BlockchainIndex<String, ArrayList<String>> indexArchived = blockchainObjectIndex.getIndex(BlockchainObjectIndex.INDEX_TYPE.VIRTUAL_TABLES_ARCHIVED);
        BlockchainObjectReader reader;

        HashMap<AttrID, BlockchainObject> pendingObjects = ((RecordinBlockMiner) ethereum.getBlockMiner()).getPendingObjects(model.getDisplayName());

        if (ethereum.getBlockchain().getBestBlock().isGenesis()) {
            reader = BlockchainObjectReader.getInstance(user);
        } else {
            reader = BlockchainObjectReader.getAdminInstance();
        }

        if (checkTransient) {
            if (model.isTransient()) {
                throw new ORMException("Transient objects cannot be recorded !");
            }
        }

        // Check for last parent, if another write is already pending and immutability but admin account can update immutables anyway...
        if (!object.isEmptyParentList()) {

            String uidString = object.getUid().toString();
            AttrIDList objectParentList = object.getParentList();

            logger.trace("objectParentList: " + objectParentList.toString());
            String lastParentID = objectParentList.getLast().toString();

            String blockchainLastID = currentVersionsIndex.get(uidString);

            logger.trace("lastParentID    : " + lastParentID);
            logger.trace("blockchainLastID: " + blockchainLastID);

            if (!lastParentID.equals(blockchainLastID)) {
                logger.error("The last parent ID does not correspond to the last instance for uid '" + uidString + "'");
                throw new ORMException("The last parent ID does not correspond to the last instance for uid '" + uidString + "'");
            }

            AttrID attrID = new AttrID(lastParentID);

            if (pendingObjects.containsKey(attrID)) {
                logger.error("A transcation is already mining for uid '" + uidString + "'");
                throw new ORMException("A transcation is already mining for uid '" + uidString + "'");
            }

            BlockchainObject parentObject = reader.read(blockchainLastID);

            if (!reader.isAdminInstance() && parentObject.containsKey("Immutable") && parentObject.get("Immutable") != null && ((AttrBoolean) parentObject.get("Immutable")).equals(new AttrBoolean(true))) {
                //if (parentObject.containsKey("Immutable") && parentObject.get("Immutable") != null && ((AttrBoolean)parentObject.get("Immutable")).equals(new AttrBoolean(true))) { 

                logger.debug("(AttrBoolean)parentObject.get(\"Immutable\") : " + parentObject.get("Immutable").toString());
                logger.debug("((AttrBoolean)parentObject.get(\"Immutable\")).equals(new AttrBoolean(true)) : " + ((AttrBoolean) parentObject.get("Immutable")).equals(new AttrBoolean(true)));
                throw new ORMException("You cannot update an immutable object !");
            }

        }

        AttrList<AttrAttribute> attributesList = model.getAttributes();
        for (Attr attr : attributesList) {
            AttrAttribute attribute = (AttrAttribute) attr;

            if (attribute.AttrTypeModel != null && !attribute.AttrTypeModel.equals("") && !attribute.AttrTypeModel.equals("Attribute")) {
                Model linkedModel = null;

                if (Main.initDataModelFlag) {
                    linkedModel = initIDSModels.get(attribute.AttrTypeModel);
                } else {
                    linkedModel = (Model) reader.read(attribute.AttrTypeModel);
                }

                String attrTypeModel = linkedModel.getDisplayName();

                try {
                    Class c = CheetahWebserver.getInstance().getClass("com.recordins.recordin.orm.attribute." + attribute.AttrType);
                    if (AttrID.class.isAssignableFrom(c)) {
                        AttrID attributeValue = (AttrID) object.get(attribute.Name);

                        if (attributeValue != null && !attributeValue.equals("")) {
                            BlockchainObject blockchainObject = reader.read(attributeValue.toString());
                            if (!blockchainObject.getModel().equals(attrTypeModel)) {
                                logger.error("Attribute: '" + attribute.Name + "' must be linked to an object of model '" + attrTypeModel + "'");
                                throw new ORMException("Attribute: '" + attribute.Name + "' must be linked to an object of model '" + attrTypeModel + "'");
                            }
                        }
                    } else if (AttrIDList.class.isAssignableFrom(c)) {
                        AttrIDList attributeValue = (AttrIDList) object.get(attribute.Name);

                        if (attributeValue != null) {
                            for (AttrID attrID : attributeValue) {
                                BlockchainObject blockchainObject = reader.read(attrID.toString());
                                if (!blockchainObject.getModel().equals(attrTypeModel)) {
                                    logger.error("Attribute: '" + attribute.Name + "' must be linked to an object of model '" + attrTypeModel + "'");
                                    throw new ORMException("Attribute: '" + attribute.Name + "' must be linked to an object of model '" + attrTypeModel + "'");
                                }
                            }
                        }
                    }
                } catch (ClassNotFoundException ex) {
                    logger.error("Error finding class for Attribute: " + attribute.AttrType + ": " + ex.toString());
                    throw new ORMException("Error finding class for Attribute: " + attribute.AttrType + ": " + ex.toString());
                }

            }
            if (attribute.Required) {
                Attr attributeValue = object.get(attribute.Name);

                if (attributeValue == null || attributeValue.toString().equals("")) {
                    logger.error("'" + attribute.Name + "' cannot be empty for model '" + object.getModel() + "'");
                    throw new ORMException("'" + attribute.Name + "' cannot be empty for model '" + object.getModel() + "'");
                }
            }

            if (attribute.Unique) {

                Attr attributeValue = object.get(attribute.Name);

                if (attributeValue != null) {
                    // Check for name unicity in current and archived indexes        
                    if (indexActive.containsKey(object.getModel())) {

                        for (String uidString : indexActive.get(object.getModel())) {

                            if (object.getUid() != null && uidString.equals(object.getUid())) {
                                continue;
                            }

                            String blockID = currentVersionsIndex.get(uidString);

                            if (blockID != null) {
                                BlockchainObject objectChain = reader.read(blockID);

                                if (objectChain != null) {

                                    Attr objectChainAttributeValue = objectChain.get(attribute.Name);
                                    if (objectChainAttributeValue != null) {
                                        if (attributeValue.compareTo(objectChainAttributeValue) == 0) {
                                            logger.error("'" + attribute.Name + "' value: '" + attributeValue + "' already exists for model '" + object.getModel() + "'");
                                            throw new ORMException("'" + attribute.Name + "' value: '" + attributeValue + "' already exists for model '" + object.getModel() + "'");
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (indexArchived.containsKey(object.getModel())) {

                        for (String uidString : indexArchived.get(object.getModel())) {

                            if (object.getUid() != null && uidString.equals(object.getUid())) {
                                continue;
                            }

                            String blockID = currentVersionsIndex.get(uidString);
                            BlockchainObject objectChain = reader.read(blockID);

                            if (objectChain != null) {

                                Attr objectChainAttributeValue = objectChain.get(attribute.Name);
                                if (objectChainAttributeValue != null) {
                                    if (attributeValue.compareTo(objectChainAttributeValue) == 0) {
                                        logger.error("'" + attribute.Name + "' value: '" + attributeValue + "' already exists for model '" + object.getModel() + "'");
                                        throw new ORMException("'" + attribute.Name + "' value: '" + attributeValue + "' already exists for model '" + object.getModel() + "'");
                                    }
                                }
                            }
                        }
                    }

                    for (BlockchainObject objectChain : pendingObjects.values()) {
                        if (object.getDateTimeUpdate() == null && object.getDateTimeDelete() == null) {
                            Attr objectChainAttributeValue = objectChain.get(attribute.Name);
                            if (objectChainAttributeValue != null) {
                                if (attributeValue.compareTo(objectChainAttributeValue) == 0) {
                                    logger.error("'" + attribute.Name + "' value: '" + attributeValue + "' already exists in a mining transaction for model '" + object.getModel() + "'");
                                    throw new ORMException("'" + attribute.Name + "' value: '" + attributeValue + "' already exists in a mining transaction for model '" + object.getModel() + "'");
                                }
                            }
                        }
                    }
                }
            }
        }

        logger.trace("END validateObject()");
    }

    /**
     * Updates a {@link BlockchainObject} to mark it as deleted into the
     * blockchain
     *
     * @param object {@link BlockchainObject} representation of the object
     * @throws ORMException
     */
    public void delete(BlockchainObject object) throws ORMException {
        logger.trace("START delete(BlockchainObject)");
        delete(object, false);
        logger.trace("END delete()");
    }

    public void delete(BlockchainObject object, boolean asynchronous) throws ORMException {
        logger.trace("START delete(BlockchainObject, boolean)");
        String jsonString = "";

        if (object != null && !Main.initDataModelFlag) {

            if (!checkDeleteAccess(object, user)) {
                throw new ORMException("You are not granted to perform object deletion");
            }
        }

        Model model = BlockchainObject.getModel(object);

        logger.debug("");
        logger.debug("Delete object: " + object.getDisplayName());

        logger.debug("Delete validate object with datamodel");

        validateObjectWithDataModel(object, model);

        if (object.isEmptyParentList()) {
            logger.error("You cannot mark and non-exixting object in the blockchain for deletion");
            throw new ORMException("You cannot mark and non-exixting object in the blockchain for deletion");
        }
        AttrDateTime time = new AttrDateTime();
        object.setUserUpdate(user);
        object.setDateTimeUpdate(time);
        object.setUserDelete(user);
        object.setDateTimeDelete(time);
        object.setIndex("deleted");

        logger.debug("Delete call object method 'delete'");
        object.delete(user);

        object.setNodeUpdateID(new AttrString(Hex.toHexString(config.nodeId())));
        object.setDisplayName(buildDisplayName(object, model));

        AttrID newID = null;
        try {
            if (GDPRObject.class.isAssignableFrom(object.getClass())) {
                ((GDPRObject) object).setAttachment(new AttrAttachment());
                object.setAttrMap(new ConcurrentSkipListMap());
            }
            jsonString = mapper.writeValueAsString(object);

            logger.debug("Delete jsonString: " + jsonString);

            newID = write((JSONArray) new JSONParser().parse(jsonString), object, asynchronous);
        } catch (Exception ex) {
            logger.error("Error processing Object to JSON : " + ex.toString());
            throw new ORMException("Error processing Object to JSON : " + ex.toString());
        }
        if (newID != null) {
            logger.debug("Delete set id: " + newID + " to object: " + object.getDisplayName());
            object.setId(newID);
        }

        logger.trace("END delete()");
    }

    /**
     * Returns a {@link BlockchainObject} representation of the
     * {@link JSONArray}
     *
     * @param jsonArray {@link JSONArray} representation of the object
     * @return {@link BlockchainObject} representation of the {@link JSONArray}
     * @throws ORMException
     */
    public BlockchainObject getObject(JSONArray jsonArray) throws ORMException {
        logger.trace("START getObject(JSONArray)");
        BlockchainObject result = null;

        result = BlockchainObjectFactory.getInstance(jsonArray).getBlockchainObject();

        logger.trace("END getObject()");
        return result;
    }

    /**
     * Returns a {@link BlockchainObject} representation of the {@code String}
     *
     * @param jsonString {@code String} representation of the object
     * @return {@link BlockchainObject} representation of the {@code String}
     * @throws ORMException
     */
    public BlockchainObject getObject(String jsonString) throws ORMException {
        logger.trace("START getObject(String)");

        BlockchainObject result = null;

        JSONParser parser = new JSONParser();
        JSONArray jsonArray;

        try {
            jsonArray = (JSONArray) parser.parse(jsonString);

            if (((JSONObject) jsonArray.get(1)).containsKey("attachment") && ((JSONObject) jsonArray.get(1)).get("attachment") != null && ((JSONArray) ((JSONObject) jsonArray.get(1)).get("attachment")).size() > 1) {
                if (((JSONObject) jsonArray.get(1)).containsKey("id") && ((JSONObject) jsonArray.get(1)).get("id") != null) {

                    AttrID id = new AttrID((String) ((JSONObject) jsonArray.get(1)).get("id"));
                    jsonArray = BlockchainObjectReader.getAdminInstance().readGDPRObjectAttachment(jsonArray, id);
                }
            }

            result = getObject(jsonArray);
        } catch (ParseException ex) {
            logger.error("Error parsing JSON : " + ex.toString());
        }

        logger.trace("END getObject()");
        return result;
    }

    private boolean checkDeleteAccess(BlockchainObject object, User user) {
        boolean result = false;

        if (!CheetahWebserver.getInstance().isAdminUser(user.getLogin())) {
            if (ApplicationContext.getInstance().getBoolean("ACLEnableModel")) {
                Model model = null;
                if (object.getModelID() != null) {
                    try {

                        BlockchainIndex<String, String> currentVersionsIndex = BlockchainObjectIndex.getInstance().getIndex(BlockchainObjectIndex.INDEX_TYPE.CURRENT_VERSIONS);
                        String idString = currentVersionsIndex.get(object.getModelID().getUID());

                        model = (Model) BlockchainObjectReader.getAdminInstance().readWithoutCheck(new AttrID(idString));
                    } catch (Exception ex) {
                        logger.error("Error searching Meta model for modelID: '" + object.getModelID().toString() + "': " + ex.toString());
                    }
                } else {
                    logger.warn("Warning: No model ID found at ACL check for object: '" + object.getDisplayName() + "'");
                }

                if (model != null && !model.getModelACLs().isEmpty()) {
                    boolean granted = false;
                    if (ACL.isDeleteAllGranted(object, user, model.getModelACLs())) {
                        granted = true;
                    }

                    if (!granted) {
                        if (object.isOwner(user) && ACL.isDeleteOwnGranted(object, user, model.getModelACLs())) {
                            granted = true;
                        }
                    }

                    if (!granted) {
                        if (ApplicationContext.getInstance().getBoolean("ACLPrintDebugTraces")) {
                            logger.warn("'" + object.getDisplayName() + "': DELETE: " + user.getLogin() + " NOT granted by Model ACL (Model: " + model.getDisplayName() + ")");
                        }
                        result = false;
                    } else {
                        result = true;
                    }
                } else {
                    result = true;
                }
            }

            if (ApplicationContext.getInstance().getBoolean("ACLEnableObject")) {
                if (!result && !object.getAcl().isEmpty() && object.checkSecurity(BlockchainObject.ACTION.DELETE, user)) {
                    result = true;
                    if (ApplicationContext.getInstance().getBoolean("ACLPrintDebugTraces")) {
                        logger.warn("'" + object.getDisplayName() + "': DELETE: " + user.getLogin() + " GRANTED by Object ACL (objectID: " + object.getId() + ")");
                    }
                } else {
                    if (result && !object.checkSecurity(BlockchainObject.ACTION.DELETE, user)) {
                        result = false;
                        if (ApplicationContext.getInstance().getBoolean("ACLPrintDebugTraces")) {
                            logger.warn("'" + object.getDisplayName() + "': DELETE: " + user.getLogin() + " NOT granted by Object ACL (objectID: " + object.getId() + ")");
                        }
                    }
                }
            }
        } else {
            result = true;
        }

        return result;
    }

    private boolean checkWriteAccess(BlockchainObject object, User user) {
        boolean result = false;

        if (!CheetahWebserver.getInstance().isAdminUser(user.getLogin())) {
            if (ApplicationContext.getInstance().getBoolean("ACLEnableModel")) {
                Model model = null;
                if (object.getModelID() != null) {
                    try {

                        BlockchainIndex<String, String> currentVersionsIndex = BlockchainObjectIndex.getInstance().getIndex(BlockchainObjectIndex.INDEX_TYPE.CURRENT_VERSIONS);
                        String idString = currentVersionsIndex.get(object.getModelID().getUID());

                        model = (Model) BlockchainObjectReader.getAdminInstance().readWithoutCheck(new AttrID(idString));
                    } catch (Exception ex) {
                        logger.error("Error searching Meta model for modelID: '" + object.getModelID().toString() + "': " + ex.toString());
                    }
                } else {
                    logger.warn("Warning: No model ID found at ACL check for object: '" + object.getDisplayName() + "'");
                }

                if (model != null && !model.getModelACLs().isEmpty()) {
                    boolean granted = false;
                    if (ACL.isWriteAllGranted(object, user, model.getModelACLs())) {
                        granted = true;
                    }

                    if (!granted) {
                        if (object.isOwner(user) && ACL.isWriteOwnGranted(object, user, model.getModelACLs())) {
                            granted = true;
                        }
                    }

                    if (!granted) {
                        if (ApplicationContext.getInstance().getBoolean("ACLPrintDebugTraces")) {
                            logger.warn("'" + object.getDisplayName() + "': WRITE: " + user.getLogin() + " NOT granted by Model ACL (Model: " + model.getDisplayName() + ")");
                        }
                        result = false;
                    } else {
                        result = true;
                    }
                } else {
                    result = true;
                }
            }

            if (ApplicationContext.getInstance().getBoolean("ACLEnableObject")) {
                if (!result && !object.getAcl().isEmpty() && object.checkSecurity(BlockchainObject.ACTION.WRITE, user)) {
                    result = true;
                    if (ApplicationContext.getInstance().getBoolean("ACLPrintDebugTraces")) {
                        logger.warn("'" + object.getDisplayName() + "': WRITE: " + user.getLogin() + " GRANTED by Object ACL (objectID: " + object.getId() + ")");
                    }
                } else {
                    if (result && !object.checkSecurity(BlockchainObject.ACTION.WRITE, user)) {
                        result = false;
                        if (ApplicationContext.getInstance().getBoolean("ACLPrintDebugTraces")) {
                            logger.warn("'" + object.getDisplayName() + "': WRITE: " + user.getLogin() + " NOT granted by Object ACL (objectID: " + object.getId() + ")");
                        }
                    }
                }
            }
        } else {
            result = true;
        }

        return result;
    }

    private boolean checkCreateAccess(BlockchainObject object, User user) {
        boolean result = false;

        if (!CheetahWebserver.getInstance().isAdminUser(user.getLogin())) {
            if (ApplicationContext.getInstance().getBoolean("ACLEnableModel")) {
                Model model = null;
                if (object.getModelID() != null) {
                    try {

                        BlockchainIndex<String, String> currentVersionsIndex = BlockchainObjectIndex.getInstance().getIndex(BlockchainObjectIndex.INDEX_TYPE.CURRENT_VERSIONS);
                        String idString = currentVersionsIndex.get(object.getModelID().getUID());

                        model = (Model) BlockchainObjectReader.getAdminInstance().readWithoutCheck(new AttrID(idString));
                    } catch (Exception ex) {
                        logger.error("Error searching Meta model for modelID: '" + object.getModelID().toString() + "': " + ex.toString());
                    }
                } else {
                    logger.warn("Warning: No model ID found at ACL check for object: '" + object.getDisplayName() + "'");
                }

                if (model != null && !model.getModelACLs().isEmpty()) {
                    boolean granted = false;
                    if (ACL.isCreateGranted(object, user, model.getModelACLs())) {
                        granted = true;
                    }

                    if (!granted) {
                        if (ApplicationContext.getInstance().getBoolean("ACLPrintDebugTraces")) {
                            logger.warn("'" + object.getDisplayName() + "': CREATE: " + user.getLogin() + " NOT granted by Model ACL (Model: " + model.getDisplayName() + ")");
                        }
                        result = false;
                    } else {
                        result = true;
                    }
                } else {
                    result = true;
                }
            }

            if (ApplicationContext.getInstance().getBoolean("ACLEnableObject")) {
                if (!result && !object.getAcl().isEmpty() && object.checkSecurity(BlockchainObject.ACTION.CREATE, user)) {
                    result = true;
                    if (ApplicationContext.getInstance().getBoolean("ACLPrintDebugTraces")) {
                        logger.warn("'" + object.getDisplayName() + "': CREATE: " + user.getLogin() + " GRANTED by Object ACL (objectID: " + object.getId() + ")");
                    }
                } else {
                    if (result && !object.checkSecurity(BlockchainObject.ACTION.CREATE, user)) {
                        result = false;
                        if (ApplicationContext.getInstance().getBoolean("ACLPrintDebugTraces")) {
                            logger.warn("'" + object.getDisplayName() + "': CREATE: " + user.getLogin() + " NOT granted by Object ACL (objectID: " + object.getId() + ")");
                        }
                    }
                }
            }
        } else {
            result = true;
        }

        return result;
    }

}
