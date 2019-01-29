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

import com.recordins.recordin.ApplicationContext;
import com.recordins.recordin.Main;
import com.recordins.recordin.orm.BlockchainObject;
import com.recordins.recordin.orm.GDPRObject;
import com.recordins.recordin.orm.Preferences;
import com.recordins.recordin.orm.User;
import com.recordins.recordin.orm.attribute.Attr;
import com.recordins.recordin.orm.attribute.AttrAbstractAttachment;
import com.recordins.recordin.orm.attribute.AttrAttachment;
import com.recordins.recordin.orm.attribute.AttrID;

import static com.recordins.recordin.orm.core.BlockchainObjectReader.jsonCache;
import static com.recordins.recordin.orm.core.BlockchainObjectReader.objectCache;
import static com.recordins.recordin.orm.core.objectfactory.FactoryPlatform_1_0.setAttrMap;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import org.cheetah.webserver.CheetahWebserver;
import org.ethereum.config.SystemProperties;
import org.ethereum.core.Block;
import org.ethereum.core.Transaction;
import org.ethereum.facade.Ethereum;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.recordins.recordin.orm.exception.ORMException;
import org.spongycastle.util.encoders.Hex;

import java.util.Map.Entry;
import java.util.concurrent.ConcurrentSkipListMap;

public class BlockchainObjectIndex {

    /* Logger for console output */
    private static final Logger logger = LoggerFactory.getLogger(BlockchainObjectIndex.class);

    private static final ConcurrentHashMap<String, BlockchainIndex> indexMap = new ConcurrentHashMap();

    private static BlockchainObjectIndex instance = null;

    private Path rootFolderPath = null;

    private boolean rebuildIndexflag = false;
    private boolean rebuildIndexflagAdmin = false;

    //@Autowired not working
    private Ethereum ethereum = (Ethereum) ApplicationContext.getInstance().get("ethereum");

    //@Autowired Not working
    private SystemProperties config = (SystemProperties) ApplicationContext.getInstance().get("config");

    private BlockchainObjectIndex() {
        logger.trace("START BlockchainObjectIndex()");

        rootFolderPath = Paths.get(config.databaseDir());
        rootFolderPath = rootFolderPath.resolve("BlockchainObjectIndex");

        //check Index folder existance,, if not rebuild indexes...

        logger.debug("rootFolderPath: " + rootFolderPath.toString());
        logger.debug("rootFolderPath exists: " + Files.exists(rootFolderPath));

        if (!Files.exists(rootFolderPath)) {

            logger.debug("No index found !");

            rebuildIndexflag = true;

            try {
                Files.createDirectories(rootFolderPath);
            } catch (IOException ex) {
                logger.error("Error creating BlockchainObjectIndex root directory: " + ex.toString());
            }
        }

        String indexName = INDEX_TYPE.CURRENT_VERSIONS.toString();
        try {
            BlockchainIndex<String, String> index = new BlockchainIndex(indexName, indexName, rootFolderPath, false);
            indexMap.put(indexName, index);
        } catch (ORMException ex) {
            logger.error("Error creating index '" + indexName + "': " + ex.toString());
            ex.printStackTrace();
        }

        indexName = INDEX_TYPE.VIRTUAL_TABLES_ACTIVE.toString();
        try {
            BlockchainIndex<String, String> index = new BlockchainIndex(indexName, "Active", rootFolderPath, true);
            indexMap.put(indexName, index);
        } catch (ORMException ex) {
            logger.error("Error creating index '" + indexName + "': " + ex.toString());
            ex.printStackTrace();
        }

        indexName = INDEX_TYPE.VIRTUAL_TABLES_ARCHIVED.toString();
        try {
            BlockchainIndex<String, String> index = new BlockchainIndex(indexName, "Archived", rootFolderPath, true);
            indexMap.put(indexName, index);
        } catch (ORMException ex) {
            logger.error("Error creating index '" + indexName + "': " + ex.toString());
            ex.printStackTrace();
        }

        indexName = INDEX_TYPE.VIRTUAL_TABLES_DELETED.toString();
        try {
            BlockchainIndex<String, String> index = new BlockchainIndex(indexName, "Deleted", rootFolderPath, true);
            indexMap.put(indexName, index);
        } catch (ORMException ex) {
            logger.error("Error creating index '" + indexName + "': " + ex.toString());
            ex.printStackTrace();
        }

        indexName = INDEX_TYPE.USER_OBJECTS.toString();
        try {
            BlockchainIndex<String, String> index = new BlockchainIndex(indexName, indexName, rootFolderPath, false);
            indexMap.put(indexName, index);
        } catch (ORMException ex) {
            logger.error("Error creating index '" + indexName + "': " + ex.toString());
            ex.printStackTrace();
        }

        logger.trace("END BlockchainObjectIndex()");
    }

    public static void setRebuildIndexflag(boolean rebuildIndexflag) throws ORMException {

        getInstance().rebuildIndexflag = rebuildIndexflag;
    }

    public static BlockchainIndex createIndex(String name) throws ORMException {
        return createIndex(name, name);
    }

    public static BlockchainIndex createIndex(String name, String displayName) throws ORMException {
        BlockchainIndex result = null;

        if (indexMap.contains(name)) {
            throw new ORMException("Index name '" + name + "' already exists");
        }

        try {
            BlockchainIndex index = new BlockchainIndex(name, displayName, getInstance().rootFolderPath, false);
            indexMap.put(name, index);
        } catch (ORMException ex) {
            logger.error("Error creating index 'name': " + ex.toString());
            ex.printStackTrace();
        }

        return result;
    }

    public BlockchainIndex getIndex(INDEX_TYPE indexType) {
        logger.trace("START getIndex(INDEX_TYPE)");
        logger.trace("END getIndex()");

        return getIndex(indexType.toString());
    }

    public BlockchainIndex getIndex(String indexName) {
        logger.trace("START getIndex(String)");
        BlockchainIndex result = null;

        if (indexMap.containsKey(indexName)) {
            result = indexMap.get(indexName);
        } else {
            logger.warn("Index '" + indexName + "' not found !");
        }

        logger.trace("END getIndex()");
        return result;
    }

    public void removeUID(String uid) {
        logger.trace("START removeUID(String)");

        BlockchainIndex<String, String> index = getIndex(INDEX_TYPE.CURRENT_VERSIONS);
        if (index.containsKey(uid)) {
            index.remove(uid);
            index.commit();

        }
        logger.trace("END removeUID()");
    }

    /**
     * Returns an instance of {@link BlockchainObjectIndex}
     *
     * @return
     */
    public static BlockchainObjectIndex getInstance() throws ORMException {
        logger.trace("START getInstance()");
        logger.trace("END getInstance()");

        if (instance == null) {
            instance = new BlockchainObjectIndex();
        }

        return instance;
    }

    public static void checkIndexes() throws ORMException {
        logger.trace("START checkIndexes()");

        logger.debug("Rebuild Index Flag: " + getInstance().rebuildIndexflag);

        if (instance.rebuildIndexflag) {
            logger.info("Rebuilding indexes");

            instance.rebuildIndexes();
        }

        logger.trace("END checkIndexes()");
    }

    public void rebuildIndexes() {
        logger.debug("START rebuildIndexes()");

        Thread T = new Thread("Index Rebuild") {

            @Override
            public void run() {

                Main.initFlag = true;


                for (int i = 0; i <= ethereum.getSyncStatus().getBlockBestKnown(); i++) {
                    Block block = ethereum.getBlockchain().getBlockByNumber(i);

                    if (i == 0) {
                        rebuildIndexflagAdmin = true;
                    } else {
                        rebuildIndexflagAdmin = false;
                    }

                    HashMap<BlockchainObject, String> blockchainObjectsMap = new HashMap();
                    ArrayList<BlockchainObject> blockchainObjectsList = new ArrayList();

                    long blockNumber = block.getNumber();

                    logger.debug("** blockNumber: " + blockNumber);


                    ORMException exception = null;
                    for (Transaction transaction : block.getTransactionsList()) {
                        String transactionID = Hex.toHexString(transaction.getHash());
                        //        logger.debug("Transaction Hash: " + transactionID);

                        byte[] objectData = transaction.getData();
                        String jsonString = new String(objectData);

                        BlockchainObject object = null;
                        try {

                            User admin = User.getAdminUser();

                            object = BlockchainObjectWriter.getInstance(admin).getObject(jsonString);

                            //            logger.debug("PUT Object Name: " + object.getDisplayName());
                            blockchainObjectsList.add(object);
                            blockchainObjectsMap.put(object, transactionID);

                            //            logger.debug(" blockchainObjectsMap.size: " + blockchainObjectsList.size());


                        } catch (ORMException ex) {
                            logger.debug("Error updating indexes from block: " + ex.toString());
                            exception = ex;
                            break;
                        }

                    }

                    Collections.sort(blockchainObjectsList);
/*
                    for (BlockchainObject object : blockchainObjectsList) {
                        logger.debug("Object Create: " + object.getDateTimeCreate());
                        logger.debug("Object Update: " + object.getDateTimeUpdate());
                        logger.debug("Object Name: " + object.getDisplayName());
                    }
                    */

                    if (exception == null) {
                        for (BlockchainObject object : blockchainObjectsList) {

                            String transactionID = blockchainObjectsMap.get(object);

                            try {

                                logger.debug("   Updating indexes: " + object.getDisplayName());
                                boolean commit = false;
                                if (i % 50 == 0 || i == ethereum.getSyncStatus().getBlockBestKnown() - 1) {
                                    commit = true;
                                }

                                AttrID id = new AttrID(block.getNumber() + "." + transactionID + "." + object.getUid());
                                object.setId(id);
                                getInstance().updateIndexes(object, id, commit);

                            } catch (ORMException ex) {
                                logger.debug("Error updating indexes from block: " + ex.toString());
                                exception = ex;
                            }
                        }
                    }
                }

                Main.initFlag = false;
            }
        };

        T.start();
        try {
            T.join();
        } catch (InterruptedException e) {
            logger.debug("Error updating indexes Thread: " + e.toString());
        }

        instance.rebuildIndexflag = false;

        logger.debug("END rebuildIndexes()");
    }

    public void removeUIDfromModel(String uid, String model) {
        logger.trace("START removeUIDfromModel(String, String)");

        BlockchainIndex<String, ArrayList<String>> index = getIndex(INDEX_TYPE.VIRTUAL_TABLES_ACTIVE);
        if (index.containsKey(model)) {
            ArrayList modelList = index.get(model);
            if (modelList.contains(uid)) {
                modelList.remove(uid);
                index.replace(model, modelList);
                index.commit();
            }
        }

        removeUID(uid);
        logger.trace("END removeUIDfromModel()");
    }

    public void updateIndexes(BlockchainObject object, AttrID attrID, boolean commmit) throws ORMException {
        logger.trace("START updateIndexes(BlockchainObject, long, boolean)");

        if (attrID == null || attrID.toString().equals("")) {
            throw new ORMException("An AttrID must be provided to update the indexes");
        }
        //       logger.debug("--------  2");
        String uid = object.getUid().toString();

        //       logger.debug("--------  2.1");
        if (object.getDateTimeDelete() == null) {

            //           logger.debug("--------  2.2");
            if (object.getIndex().toString().toLowerCase().equals("archived")) {

                //               logger.debug("--------  2.3");
                BlockchainObjectIndex.getInstance().updateModelArchivedList(object.getModel(), uid, commmit);
            } else {
                //               logger.debug("--------  2.4");

                //               logger.debug("--------  2.4.0.0");
                BlockchainObjectIndex.getInstance().updateModelList(object.getModel(), uid, commmit);

                //               logger.debug("--------  2.4.0");
            }

            if (GDPRObject.class.isAssignableFrom(object.getClass())) {

                User user = null;
                if (ethereum.getBlockchain().getBestBlock().getNumber() > 0 && !this.rebuildIndexflagAdmin) {
                    user = User.getAdminUser();

                } else {
                    user = new User(Main.userAdminName);
                    user.setId(new AttrID("1.init.init"));
                }

                String attachmentContent = new String(((GDPRObject) object).getAttachment().readAttachment(attrID, "GDPRAttachment", user), CheetahWebserver.getInstance().findCharset());


                JSONParser parser = new JSONParser();
                if (!attachmentContent.equals("")) {

                    JSONObject jsonObjectAttachment = null;

                    try {
                        jsonObjectAttachment = (JSONObject) parser.parse(attachmentContent);
                        setAttrMap(object, jsonObjectAttachment);

                    } catch (ParseException e) {
                        logger.error("Error setting GDPR attachment: " + e.toString());
                    }
                }
            }

            if (Preferences.class.isAssignableFrom(object.getClass()) || User.class.isAssignableFrom(object.getClass())) {

                User user = null;
                if (ethereum.getBlockchain().getBestBlock().getNumber() > 0 && !this.rebuildIndexflagAdmin) {
                    user = User.getAdminUser();

                } else {
                    user = new User(Main.userAdminName);
                    user.setId(new AttrID("1.init.init"));
                }

                User.readKeyAndCredentials(object, user);
            }

            //           logger.debug("--------  2.4.1");
            for (Entry<String, Attr> entry : object.entrySet()) {

                if (AttrAbstractAttachment.class.isAssignableFrom(entry.getValue().getClass())) {
                    AttrAbstractAttachment attachment = (AttrAbstractAttachment) entry.getValue();
                    attachment.readAttachment(attrID, entry.getKey(), User.getAdminUser());
                }
            }

            //           logger.debug("--------  2.4.2");

        } else {
            //           logger.debug("--------  2.5");
            BlockchainObjectIndex.getInstance().updateModelDeletedList(object.getModel(), uid, commmit);

            if (GDPRObject.class.isAssignableFrom(object.getClass())) {
                Path uidFolder = AttachmentStore.getInstance().getStorePath(object.getUid().toString());
                try {
                    Files.walkFileTree(uidFolder, new SimpleFileVisitor<Path>() {
                                @Override
                                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                                    try {
                                        Files.delete(file);
                                    } catch (Exception e) {
                                        logger.error("Error deleting file: " + file.toString());
                                    }

                                    return FileVisitResult.CONTINUE;
                                }

                                @Override

                                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                                    try {
                                        Files.delete(dir);
                                    } catch (Exception e) {
                                        logger.error("Error deleting folder: " + dir.toString());
                                    }
                                    return FileVisitResult.CONTINUE;
                                }
                            }
                    );

                } catch (Exception ex) {
                    logger.error("Error deleting attachments: " + ex.toString());
                    throw new ORMException("Error deleting attachments: " + ex.toString());
                }
            }
            for (AttrID id : object.getParentList()) {
                logger.trace("removing from cache id: " + id);
                if (jsonCache.containsKey(id.toString())) {
                    jsonCache.remove(id.toString());
                }
                if (objectCache.containsKey(id.toString())) {
                    objectCache.remove(id.toString());
                }
            }
        }

        //      logger.debug("--------  3");
        BlockchainObjectIndex.getInstance().updateCurrentVersion(uid, attrID.toString(), commmit);

        if (ethereum.getBlockchain().getBestBlock().getNumber() > 0 && !this.rebuildIndexflagAdmin) {
            if (object.getUserUpdateID() != null) {
                BlockchainObjectIndex.getInstance().updateUserUID(object.getUserUpdateID().getUID(), uid, commmit);
            } else {
                if (object.getUserCreateID() != null && object.getUserCreateID().getUID() != null) {
                    BlockchainObjectIndex.getInstance().updateUserUID(object.getUserCreateID().getUID(), uid, commmit);
                }
            }
        } else {
            User user = User.getAdminUser();
            BlockchainObjectIndex.getInstance().updateUserUID(user.getUid(), uid, commmit);
        }

        //      logger.debug("--------  4");

        logger.trace("END updateIndexes()");
    }

    private void updateCurrentVersion(String uid, String blockID, boolean commit) {
        logger.trace("START updateCurrentVersion(String, String)");

        BlockchainIndex index = getIndex(INDEX_TYPE.CURRENT_VERSIONS);

        if (index.containsKey(uid)) {
            index.replace(uid, blockID);
        } else {
            index.put(uid, blockID);
        }

        if (commit) {
            index.commit();
        }
        logger.trace("END updateCurrentVersion()");
    }

    private void updateModelList(String model, String uid, boolean commit) {
        logger.trace("START updateModelList(String, String, boolean)");


        BlockchainIndex<String, ArrayList<String>> indexActive = getIndex(INDEX_TYPE.VIRTUAL_TABLES_ACTIVE);
        BlockchainIndex<String, ArrayList<String>> indexArchived = getIndex(INDEX_TYPE.VIRTUAL_TABLES_ARCHIVED);

        // logger.trace("model: " + model);
        // logger.trace("indexActive.containsKey(model): " + indexActive.containsKey(model));

        if (indexActive.containsKey(model)) {

            ArrayList modelList = indexActive.get(model);
            //        logger.debug("1 uid: " + uid);
            //        logger.debug("1 model: " + model);
            //        logger.debug("modelList.contains(uid): " + modelList.contains(uid));
            if (!modelList.contains(uid)) {
                modelList.add(0, uid);
                indexActive.replace(model, modelList);
            }
        } else {
            ArrayList modelList = new ArrayList();
            modelList.add(uid);
            //        logger.debug("2 uid: " + uid);
            //        logger.debug("2 model: " + model);
            indexActive.put(model, modelList);
        }


        if (commit) {

            //        logger.debug("indexActive.commit()");
            indexActive.commit();
        }

        //    for (String uidString : indexActive.get(model)) {
        //        logger.debug("indexActive uidString: " + uidString);
        //    }

        /* Removing from Archived index list */
//        logger.debug("model: " + model);
//        logger.debug("modelListArchivedTreeMap.containsKey(model): " + modelListArchivedTreeMap.containsKey(model));
        if (indexArchived.containsKey(model)) {

            ArrayList modelList = indexArchived.get(model);
//            logger.debug("uid: " + uid);
//            logger.debug("modelList.contains(uid): " + modelList.contains(uid));
            if (modelList.contains(uid)) {
                modelList.remove(uid);
                indexArchived.replace(model, modelList);

                if (commit) {
                    indexArchived.commit();
                }
            }

            //for (String uidString : modelListArchivedTreeMap.get(model)) {
            //            logger.debug("modelListArchivedTreeMap uidString: " + uidString);
            //}
        }

        logger.trace("END updateModelList()");
    }

    private void updateModelArchivedList(String model, String uid, boolean commit) {
        logger.trace("START updateModelArchivedList(String, String, boolean)");

//        logger.debug("model: " + model);
//        logger.debug("modelListArchivedTreeMap.containsKey(model): " + modelListArchivedTreeMap.containsKey(model));
        BlockchainIndex<String, ArrayList<String>> indexArchived = getIndex(INDEX_TYPE.VIRTUAL_TABLES_ARCHIVED);
        BlockchainIndex<String, ArrayList<String>> indexActive = getIndex(INDEX_TYPE.VIRTUAL_TABLES_ACTIVE);
        if (indexArchived.containsKey(model)) {

            ArrayList modelList = indexArchived.get(model);
//            logger.debug("uid: " + uid);
//            logger.debug("modelList.contains(uid): " + modelList.contains(uid));
            if (!modelList.contains(uid)) {
                modelList.add(0, uid);
                indexArchived.replace(model, modelList);
            }
        } else {
            ArrayList modelList = new ArrayList();
            modelList.add(uid);
            indexArchived.put(model, modelList);
        }


        if (commit) {
            indexArchived.commit();
        }

        //for (String uidString : modelListArchivedTreeMap.get(model)) {
        //        logger.debug("modelListArchivedTreeMap uidString: " + uidString);
        //}

        /* Removing from index list */
//        logger.debug("model: " + model);
//        logger.debug("modelListTreeMap.containsKey(model): " + modelListTreeMap.containsKey(model));
        if (indexActive.containsKey(model)) {

            ArrayList modelList = indexActive.get(model);
//            logger.debug("uid: " + uid);
//            logger.debug("modelList.contains(uid): " + modelList.contains(uid));
            if (modelList.contains(uid)) {
                modelList.remove(uid);
                indexActive.replace(model, modelList);

                if (commit) {
                    indexActive.commit();
                }
            }

            //for (String uidString : modelListTreeMap.get(model)) {
            //            logger.debug("modelListTreeMap uidString: " + uidString);
            //}
        }

        logger.trace("END updateModelArchivedList()");
    }

    private void updateModelDeletedList(String model, String uid, boolean commit) {
        logger.trace("START updateModelDeletedList(String, String, boolean)");

//        logger.debug("model: " + model);
//        logger.debug("modelListDeletedTreeMap.containsKey(model): " + modelListDeletedTreeMap.containsKey(model));
        BlockchainIndex<String, ArrayList<String>> indexDeleted = getIndex(INDEX_TYPE.VIRTUAL_TABLES_DELETED);
        BlockchainIndex<String, ArrayList<String>> indexArchived = getIndex(INDEX_TYPE.VIRTUAL_TABLES_ARCHIVED);
        BlockchainIndex<String, ArrayList<String>> indexActive = getIndex(INDEX_TYPE.VIRTUAL_TABLES_ACTIVE);
        if (indexDeleted.containsKey(model)) {

            ArrayList modelList = indexDeleted.get(model);
//            logger.debug("uid: " + uid);
//            logger.debug("modelList.contains(uid): " + modelList.contains(uid));
            if (!modelList.contains(uid)) {
                modelList.add(0, uid);
                indexDeleted.replace(model, modelList);
            }
        } else {
            ArrayList modelList = new ArrayList();
            modelList.add(uid);
            indexDeleted.put(model, modelList);
        }

        if (commit) {
            indexDeleted.commit();
        }

        //for (String uidString : modelListDeletedTreeMap.get(model)) {
        //        logger.debug("modelListDeletedTreeMap uidString: " + uidString);
        //}

        /* Removing from index list */
//        logger.debug("model: " + model);
//        logger.debug("modelListTreeMap.containsKey(model): " + modelListTreeMap.containsKey(model));
        if (indexActive.containsKey(model)) {

            ArrayList modelList = indexActive.get(model);
//            logger.debug("uid: " + uid);
//            logger.debug("modelList.contains(uid): " + modelList.contains(uid));
            if (modelList.contains(uid)) {
                modelList.remove(uid);
                indexActive.replace(model, modelList);


                if (commit) {
                    indexActive.commit();
                }
            }

            //for (String uidString : modelListTreeMap.get(model)) {
            //            logger.debug("modelListTreeMap uidString: " + uidString);
            //}
        }


        /* Removing from Archived index list */
//        logger.debug("model: " + model);
//        logger.debug("modelListArchivedTreeMap.containsKey(model): " + modelListArchivedTreeMap.containsKey(model));
        if (indexArchived.containsKey(model)) {

            ArrayList modelList = indexArchived.get(model);
//            logger.debug("uid: " + uid);
//            logger.debug("modelList.contains(uid): " + modelList.contains(uid));
            if (modelList.contains(uid)) {
                modelList.remove(uid);
                indexArchived.replace(model, modelList);

                indexArchived.commit();
            }

            //for (String uidString : modelListArchivedTreeMap.get(model)) {
            //            logger.debug("modelListArchivedTreeMap uidString: " + uidString);
            //}
        }

        logger.trace("END updateModelDeletedList()");
    }

    private void updateUserUID(String userUID, String uid, boolean commit) {
        logger.trace("START updateUserUID(String, String, boolean)");


//        logger.debug("1 updateUserUID");

//        logger.debug("userUIDTreeMap.containsKey(userBlockID): " + userUIDTreeMap.containsKey(userBlockID));

        BlockchainIndex<String, ArrayList<String>> indexActive = getIndex(INDEX_TYPE.USER_OBJECTS);
        if (indexActive.containsKey(userUID)) {


//            logger.debug("2.0 updateUserUID");

            ArrayList modelList = indexActive.get(userUID);
//            logger.debug("uid: " + uid);
//            logger.debug("modelList.contains(uid): " + modelList.contains(uid));
            if (!modelList.contains(uid)) {

//                logger.debug("2.1 updateUserUID");

                modelList.add(0, uid);
                indexActive.replace(userUID, modelList);

//                logger.debug("2.2 updateUserUID");
            }
        } else {

//            logger.debug("3.0 updateUserUID");

            ArrayList modelList = new ArrayList();
            modelList.add(uid);
            indexActive.put(userUID, modelList);

            //           logger.debug("3.1 updateUserUID");
        }


        if (commit) {
            indexActive.commit();

            //               logger.debug("4.0 updateUserUID");
        }

        //for (String uidString : userUIDTreeMap.get(userUID)) {
        //        logger.debug("userUIDTreeMap uidString: " + uidString);
        //}

        logger.trace("END updateUserUID()");
    }

    public JSONArray getIndexList() {
        JSONArray result = new JSONArray();

        for (Map.Entry<String, BlockchainIndex> entry : indexMap.entrySet()) {

            JSONObject jsonIndex = new JSONObject();

            if (entry.getValue().isPublic()) {
                jsonIndex.put("name", entry.getKey());
                jsonIndex.put("displayName", entry.getValue().getDisplayName());
                result.add(jsonIndex);
            }
        }

        return result;
    }

    public void commitAllIndexes() {

        //Thread T = new Thread(){

        //    @Override
        //    public void run(){

        for (BlockchainIndex index : indexMap.values()) {
            index.commit();
        }
        //     }

        logger.debug("Commiting all indexes done");
        // };

        // T.start();
    }

    public enum INDEX_TYPE {
        CURRENT_VERSIONS, VIRTUAL_TABLES_ACTIVE, VIRTUAL_TABLES_ARCHIVED, VIRTUAL_TABLES_DELETED, USER_OBJECTS;

        public static INDEX_TYPE getIndex(String name) {
            INDEX_TYPE result = VIRTUAL_TABLES_ACTIVE;

            for (INDEX_TYPE index : INDEX_TYPE.values()) {

                if (index.toString().equalsIgnoreCase(name)) {
                    result = index;
                    break;
                }

            }
            return result;
        }
    }


    /*
    public static class IndexUpdater{

        public IndexUpdater(long blocNumber, List<Transaction> transactionList){
           }

    }
    */
}
