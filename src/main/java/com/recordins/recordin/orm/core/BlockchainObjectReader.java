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
import com.recordins.recordin.orm.*;
import com.recordins.recordin.orm.attribute.*;
import com.recordins.recordin.orm.exception.ORMException;

import java.util.*;
import java.util.Map.Entry;

import com.recordins.recordin.Main;
import com.recordins.recordin.utils.DeepCopy;
import org.cheetah.webserver.CheetahWebserver;
import org.cheetah.webserver.authentication.BlockchainAuthenticatorFactory;
import org.cheetah.webserver.authentication.IBlockchainAuthenticator;
import org.ethereum.core.Block;
import org.ethereum.core.Transaction;
import org.ethereum.facade.Blockchain;
import org.ethereum.facade.Ethereum;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

import static com.recordins.recordin.Main.initIDSModels;
import static com.recordins.recordin.Main.initModelNameIDS;

public class BlockchainObjectReader<T extends BlockchainObject> {

    /* Logger for console output */
    private static final Logger logger = LoggerFactory.getLogger(BlockchainObjectReader.class);

    //@Autowired not working
    private Ethereum ethereum = (Ethereum) ApplicationContext.getInstance().get("ethereum");

    /* Mapper for Java <-> Json conversion */
    private ObjectMapper mapper = new ObjectMapper().enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);

    /* User for permissions evaluation */
    private User user;

    /* JSON cache */
    private static final int CACHE_SIZE = 10_000;
    public static LRUCache<String, JSONArray> jsonCache = new LRUCache(CACHE_SIZE);
    public static LRUCache<String, BlockchainObject> objectCache = new LRUCache(CACHE_SIZE);

    /**
     * Returns an instance of {@link BlockchainObjectReader} with admin
     * priviledges
     *
     * @return
     * @throws ORMException
     */
    public static BlockchainObjectReader getAdminInstance() throws ORMException {
        logger.trace("START getAdminInstance()");
        logger.trace("END getAdminInstance()");

        return new BlockchainObjectReader(User.getAdminUser());
    }

    /**
     * Returns an instance of {@link BlockchainObjectReader}
     *
     * @param user, the user for permissions evaluation
     * @return
     * @throws ORMException
     */
    public static BlockchainObjectReader getInstance(User user) {
        logger.trace("START getInstance(User)");
        logger.trace("END getInstance()");
        return new BlockchainObjectReader(user);
    }

    /**
     * Returns an instance of {@link BlockchainObjectReader}
     *
     * @param id, the user's {@link AttrID} id for permissions evaluation
     * @return
     * @throws ORMException
     */
    public static BlockchainObjectReader getInstance(AttrID id) throws ORMException {
        logger.trace("START getInstance(AttrID)");
        logger.trace("END getInstance()");
        return new BlockchainObjectReader(id);
    }

    /**
     * Returns an instance of {@link BlockchainObjectReader}
     *
     * @param id, the user's {@code Long} id for permissions evaluation
     * @return
     * @throws ORMException
     */
    public static BlockchainObjectReader getInstance(String id) throws ORMException {
        logger.trace("START getInstance(String)");
        logger.trace("END getInstance()");
        return new BlockchainObjectReader(id);
    }

    private BlockchainObjectReader() {
        logger.trace("START BlockchainObjectReader()");
        logger.trace("END BlockchainObjectReader()");
    }

    private BlockchainObjectReader(User user) {
        this();
        logger.trace("START BlockchainObjectReader(User)");
        this.user = user;
        logger.trace("END BlockchainObjectReader()");
    }

    private BlockchainObjectReader(String id) throws ORMException {
        this();
        logger.trace("START BlockchainObjectReader(String)");
        this.user = readUser(new AttrID(id));
        logger.trace("END BlockchainObjectReader()");
    }

    private BlockchainObjectReader(AttrID id) throws ORMException {
        this();
        logger.trace("START BlockchainObjectReader(AttrID)");
        this.user = readUser(id);
        logger.trace("END BlockchainObjectReader()");
    }

    protected JSONArray readObjectJsonFromBlockchain(AttrID id) throws ORMException {
        logger.trace("START readObjectJsonFromBlockchain(AttrID)");
        JSONArray result = new JSONArray();

        Blockchain blockchain = ethereum.getBlockchain();
        Block block = blockchain.getBlockByNumber(id.getBlockID());

        if (block.getTransactionsList().size() == 0) {
            throw new ORMException("Block '" + id.getBlockID() + "' does not contain any object to read");
        }

        for (Transaction transaction : block.getTransactionsList()) {
            String transcationID = Hex.toHexString(transaction.getHash());

            if (transcationID.equals(id.getTransactionID()) || id.getTransactionID().equals("init")) {
                String stringTemp = new String(transaction.getData());

                JSONParser parser = new JSONParser();
                try {
                    JSONArray jsonTemp = (JSONArray) parser.parse(stringTemp);

                    if (jsonTemp.size() > 0) {
                        result = jsonTemp;
                        break;
                    }

                } catch (ParseException ex) {
                    logger.error("Error reading JSON : " + ex.toString());
                    throw new ORMException("Error reading JSON : " + ex.toString());
                }
            }
        }

        logger.trace("END readObjectJsonFromBlockchain()");
        return result;
    }

    public JSONArray readGDPRObjectAttachment(JSONArray jsonArray, AttrID id) {
        logger.trace("START readGDPRObjectAttachment(JSONArray, AttrID)");

        try {
            String attachmentContent = new String(AttrAttachment.readGDPRAttachment(jsonArray, id, user), CheetahWebserver.getInstance().findCharset());

            JSONParser parser = new JSONParser();
            if (!attachmentContent.equals("")) {
                JSONObject jsonObjectAttachment = (JSONObject) parser.parse(attachmentContent);
                ((JSONArray) ((JSONObject) jsonArray.get(1)).get("attrMap")).set(1, jsonObjectAttachment);
            }

        } catch (Exception ex) {
            logger.error("Error retrieving Attachment: " + ex.toString());
            ex.printStackTrace();
        }

        logger.trace("END readGDPRObjectAttachment()");
        return jsonArray;
    }

    final User readUser(AttrID id) throws ORMException {
        logger.trace("START readUser(AttrID)");
        User result = null;

        result = (User) read(id);

        logger.trace("END readUser()");
        return result;
    }

    public User readUser(String login) {
        User result = null;


        //    logger.warn("READ User: '" + login + "'");

        try {
            SearchResult searchResult = search("User", BlockchainObjectIndex.INDEX_TYPE.VIRTUAL_TABLES_ACTIVE, false, "[[\"Login\",\"=\",\"" + login + "\"]]", 20, 0, "");

            if (searchResult.getCount() == 1) {
                result = (User) searchResult.getBlockchainObjects().get(0);
            }

        } catch (ORMException ex) {
            logger.error("Error searching '" + login + "' user: " + ex.toString());
        }

        return result;
    }

    /**
     * Returns a {@code JSONObject} used for {@code OrgChart} javascript library
     *
     * @param id, the object's {@code Long} id to read
     * @return
     * @throws ORMException
     */
    public JSONObject readJsonTree(String id) throws ORMException {
        logger.trace("START readJsonTree(String)");
        logger.trace("END readJsonTree()");
        return readJsonTree(new AttrID(id));
    }

    /**
     * Returns a {@code JSONObject} used for {@code OrgChart} javascript library
     *
     * @param id, the object's {@link AttrID} id to read
     * @return
     * @throws ORMException
     */
    public JSONObject readJsonTree(AttrID id) throws ORMException {
        logger.trace("START readJsonTree(AttrID)");
        JSONObject result = new JSONObject();
        BlockchainObject blockchainObject = null;

        Blockchain blockchain = ethereum.getBlockchain();

        if (id == null) {
            throw new ORMException("Provided id is Null: '" + id + "'");
        }

        if (blockchain == null) {
            throw new ORMException("Blockchain not found for ID: '" + id + "'");
        }

        long blockID = id.getBlockID();

        if (blockID < 0 || blockID > blockchain.getBestBlock().getNumber()) {
            throw new ORMException("Provided block id does not exists: '" + blockID + "'");
        }

        JSONArray jsonArray = jsonCache.get(id.toString());
        String jsonString = "[]";

        if (jsonArray == null) {
            jsonArray = readObjectJsonFromBlockchain(id);
            jsonString = jsonArray.toJSONString();
        }

        logger.trace("Read JSON id=" + id + " : " + jsonString);

        blockchainObject = BlockchainObjectFactory.getInstance(jsonArray).getBlockchainObject();
        blockchainObject.setId(id);
        result.put("Model", blockchainObject.getModel());
        result.put("Name", blockchainObject.getDisplayName());
        //result.put("relationship", "110");


        result.put("children", new JSONArray());

        for (Attr attribute : blockchainObject.getAttrMap().values()) {
            if (attribute != null && !attribute.toString().equals("")) {
                if (AttrID.class.isAssignableFrom(attribute.getClass())) {
                    JSONObject tmp = readJsonTree((AttrID) attribute);
                    ((JSONArray) result.get("children")).add(tmp);
                }
                if (AttrIDList.class.isAssignableFrom(attribute.getClass())) {
                    for (Attr attribute2 : (AttrIDList) attribute) {
                        JSONObject tmp = readJsonTree((AttrID) attribute2);
                        ((JSONArray) result.get("children")).add(tmp);
                    }
                }
            }
        }

        logger.trace("END readJsonTree()");
        return result;
    }

    /**
     * Returns a {@code JSONArray} representing the object read form the
     * blockchain
     *
     * @param id, the object's {@code Long} id to read
     * @return
     * @throws ORMException
     */
    public JSONArray readJson(String id) throws ORMException {
        logger.trace("START readJson(String)");
        logger.trace("END readJson()");
        return readJson(new AttrID(id));
    }


    public JSONArray readJson(AttrID id) throws ORMException {
        return readJson(id, true);
    }

    /**
     * Returns a {@code JSONArray} representing the object read form the
     * blockchain
     *
     * @param id, the object's {@link AttrID} id to read
     * @return
     * @throws ORMException
     */
    public JSONArray readJson(AttrID id, boolean readAttachment) throws ORMException {
        logger.trace("START readJson(AttrID)");
        JSONArray result = null;

        Blockchain blockchain = ethereum.getBlockchain();

        if (id == null) {
            throw new ORMException("Provided id is Null: " + id);
        }

        if (blockchain == null) {
            throw new ORMException("Blockchain not found for ID: '" + id + "'");
        }

        long blockID = id.getBlockID();

        if (blockID < 0 || blockID > blockchain.getBestBlock().getNumber()) {
            throw new ORMException("Provided block id does not exists: '" + blockID + "'");
        }

        result = jsonCache.get(id.toString());

        if (result == null) {

            result = readObjectJsonFromBlockchain(id);
            String jsonString = result.toJSONString();

            logger.trace("Read JSON id=" + id + " : " + jsonString);

            if (readAttachment && ((JSONObject) result.get(1)).containsKey("attachment") && ((JSONObject) result.get(1)).get("attachment") != null && ((JSONArray) ((JSONObject) result.get(1)).get("attachment")).size() > 1) {
                result = readGDPRObjectAttachment(result, id);
            }

            ((JSONObject) result.get(1)).put("id", id.toString());

            JSONArray parentIDListArray = ((JSONArray) ((JSONArray) ((JSONObject) result.get(1)).get("parentList")).get(1));
            JSONArray newParentID = new JSONArray();
            boolean containsParent = false;

            newParentID.add("AttrID");
            newParentID.add(id.toString());

            for (int i = 0; i < parentIDListArray.size(); i++) {
                if (((String) ((JSONArray) parentIDListArray.get(i)).get(1)).equals(id.toString())) {
                    containsParent = true;
                    break;
                }
            }

            if (!containsParent) {
                parentIDListArray.add(0, newParentID);
            }

            if (ApplicationContext.getInstance().getBoolean("NodeObjectCacheEnable")) {

                if (readAttachment) {
                    jsonCache.put(id.toString(), result);
                }
            }
        }

        logger.trace("END readJson()");
        return result;
    }

    public BlockchainObject readWithoutCheck(String id) throws ORMException {
        logger.trace("START read(String)");
        logger.trace("END read()");
        return readWithoutCheck(new AttrID(id));
    }

    /**
     * Returns a {@link BlockchainObject} representing the object read form the
     * blockchain
     *
     * @param id, the object's {@code Long} id to read
     * @return
     * @throws ORMException
     */
    public BlockchainObject read(String id) throws ORMException {
        logger.trace("START read(String)");
        logger.trace("END read()");
        return read(new AttrID(id));
    }


    public BlockchainObject readWithoutCheck(AttrID id) throws ORMException {
        return read(id, false);
    }

    public BlockchainObject read(AttrID id) throws ORMException {
        return read(id, true);
    }

    /**
     * Returns a {@link BlockchainObject} representing the object read form the
     * blockchain
     *
     * @param id, the object's {@link AttrID} id to read
     * @return
     * @throws ORMException
     */
    private BlockchainObject read(AttrID id, boolean checkACL) throws ORMException {
        logger.trace("START read(AttrID)");
        BlockchainObject result = null;

        if (!ApplicationContext.getInstance().getBoolean("ACLEnableAttribute")) {
            result = objectCache.get(id.toString());
        }

        if (result == null) {
            result = BlockchainObjectFactory.getInstance(readJson(id)).getBlockchainObject();
            result.setId(id);

            if (ApplicationContext.getInstance().getBoolean("NodeObjectCacheEnable") && !ApplicationContext.getInstance().getBoolean("ACLEnableAttribute")) {
                objectCache.put(id.toString(), result);
            }
        }

        if (result != null && !Main.initDataModelFlag && checkACL) {

            if (!checkReadAccess(result, user)) {
                result = null;
            }
        }

        if (checkACL) {
            result = filterAttributeAccess(result, user);
        }


        logger.trace("END read()");
        return result;
    }


    public BlockchainObject search(String model, String name) throws ORMException {
        BlockchainObject result = null;
        SearchResult searchResult;
        searchResult = search(model, BlockchainObjectIndex.INDEX_TYPE.VIRTUAL_TABLES_ACTIVE, false, "[[\"Name\",\"=\",\"" + name + "\"]]", 20, 0, "");

        if (searchResult.getCount() < 1) {
            logger.error("Error searching object '" + name + "' for model '" + model + "': No object found !");
        } else {
            if (searchResult.getCount() == 1) {
                result = searchResult.getBlockchainObjects().get(0);
            } else {
                logger.error("Error searching object '" + name + "' for model '" + model + "': More than one object found !");
                for (BlockchainObject object : searchResult.getBlockchainObjects()) {
                    logger.error("OBJECT: " + BlockchainObject.getJSON(object));
                }
            }
        }

        return result;
    }

    public SearchResult search(String model) throws ORMException {
        return search(model, BlockchainObjectIndex.INDEX_TYPE.VIRTUAL_TABLES_ACTIVE, false, "", -1, -1, "");
    }

    /**
     * Returns a {@link BlockchainObject} {@code ArrayList} representing the
     * search result of objects for specified model with corresponding search
     * criteria and ordering
     *
     * @param model,      the model name of objects to search for
     * @param indexType,  the index of objects to search in
     * @param oldVersion, specifies whether the old versions of objects must be
     *                    returned as well
     * @param filter,     the search filter criteria
     * @param limit,      the maximum number of records to return (default: max 500)
     * @param offset,     the number of results to ignore (default: 0)
     * @param order,      the sort attribute name and can specify "desc" to invert
     *                    the sorting result
     * @return an ordered {@code ArrayList} of objects found
     * @throws ORMException
     */
    public SearchResult search(String model, BlockchainObjectIndex.INDEX_TYPE indexType, boolean oldVersion, String filter, int limit, long offset, String order) throws ORMException {
        logger.trace("START search(String, INDEX_TYPE, boolean, String, int, long, String)");

        Long count = 0l;
        Long countOld = 0l;
        Long countFiltered = 0l;
        Long countFilteredOld = 0l;

        ArrayList<BlockchainObject> blockchainObjects = new ArrayList();

        int searchLimit = ApplicationContext.getInstance().getInteger("NodeObjectSearchResultMax");

        if (limit > searchLimit) {
            limit = searchLimit;
        }

        if (limit == -1) {
            limit = 20;
        }

        if (offset == -1) {
            offset = 0;
        }

        logger.trace("Search filter: \"" + filter + "\"");

        ArrayList<BlockchainObject> objectIDs = new ArrayList();

        BlockchainIndex<String, ArrayList<String>> searchIndex = BlockchainObjectIndex.getInstance().getIndex(indexType);
        BlockchainIndex<String, String> currentVersionsIndex = BlockchainObjectIndex.getInstance().getIndex(BlockchainObjectIndex.INDEX_TYPE.CURRENT_VERSIONS);

        if (searchIndex.containsKey(model)) {
            ArrayList<String> objectUIDs = searchIndex.get(model);

            if (filter.equals("") && !oldVersion) {
                count = (long) objectUIDs.size();
            }

            int objectLimitCount = 0;

            for (String uid : objectUIDs) {
                if (currentVersionsIndex.containsKey(uid)) {
                    String objectID = currentVersionsIndex.get(uid);
                    boolean skippedOffsetOld = false;
                    logger.trace("Search objectID: " + objectID);

                    BlockchainObject object = null;
                    object = read(objectID);

                    if (object != null) {
                        AttrID attrId = new AttrID(objectID);
                        object.setId(attrId);

                        AttrIDList parentIDs2 = object.getParentList();
                        logger.trace("Search parentIDList: " + parentIDs2);

                        logger.trace("Search object.matchSearchFilter(filter): " + object.matchSearchFilter(filter));
                        if (filter != null && object.matchSearchFilter(filter)) {
                            if (!(filter.equals("") && !oldVersion)) {
                                count++;
                            }
                            logger.trace("Search offset: " + offset);
                            if (offset <= 0) {

                                logger.trace("objectLimitCount: " + objectLimitCount);
                                logger.trace("limit: " + limit);
                                //if (objectIDs.size() < limit) {
                                if (objectLimitCount < limit) {
                                    objectIDs.add(object);
                                    objectLimitCount++;
                                    logger.trace("Search add: " + object.getDisplayName() + " id: " + objectID + " UID: " + object.getUid());
                                } else {
                                    if (filter.equals("") && !oldVersion) {
                                        break;
                                    }
                                }
                            } else {
                                offset--;
                                skippedOffsetOld = true;
                            }

                        }
                        if (oldVersion && !skippedOffsetOld) {
                            AttrIDList parentIDs = object.getParentList();
                            logger.trace("   Parent Search parentIDList: " + parentIDs);

                            for (AttrID parentID : parentIDs) {
                                if (objectID.equals(parentID.toString())) {
                                    continue;
                                }

                                BlockchainObject parentObject = null;
                                //if(model.equals("Model")) {
                                //    parentObject = readWithoutCheck(parentID);
                                //}
                                //else{
                                parentObject = read(parentID);
                                //}
                                if (parentObject != null) {
                                    parentObject.setId(parentID);
                                    logger.trace("   Parent Search parentID: " + parentID);
                                    logger.trace("   Parent Search object.matchSearchFilter(filter): " + parentObject.matchSearchFilter(filter));
                                    if (filter != null && parentObject.matchSearchFilter(filter)) {

                                        count++;
                                        countOld++;
                                        logger.trace("   Parent Search offset: " + offset);
                                        if (offset <= 0) {

                                            //logger.debug("   Parent objectIDs.size(): " + objectIDs.size());
                                            //logger.debug("   Parent limit: " + limit);
                                            //        if (objectIDs.size() < limit) {
                                            if (objectLimitCount < limit || objectIDs.contains(object)) {
                                                if (!objectIDs.contains(object)) {
                                                    objectIDs.add(object);
                                                    objectLimitCount++;
                                                    count++;
                                                }
                                                objectIDs.add(parentObject);
                                                logger.trace("   Parent Search add: " + parentObject.getDisplayName() + " id: " + parentID + " UID: " + parentObject.getUid());
                                            } else {
                                                if (filter.equals("") && !oldVersion) {
                                                    break;
                                                }
                                            }
                                            //        } else {
                                            // break;
                                            //        }
                                        } else {
                                            offset--;
                                        }
                                    }
                                } else {
                                    countFilteredOld++;
                                }
                            }
                        }
                    } else {
                        countFiltered++;
                    }
                }
                if (objectIDs.size() == limit) {
                    if (filter.equals("") && !oldVersion) {
                        break;
                    }
                }
            }
        }

        blockchainObjects = objectIDs;

        if (order != null && !order.equals("")) {

            logger.trace("Search Order before: " + order);
            boolean desc = false;

            Comparator comparator;

            if (order.endsWith("desc")) {
                order = order.substring(0, order.length() - 4);
                order = order.trim();
                desc = true;
            }

            final String finalOrder = order;
            logger.trace("Search Order after: " + finalOrder);

            if (!finalOrder.equals("")) {
                if (!desc) {
                    comparator = new Comparator<BlockchainObject>() {
                        @Override
                        public int compare(BlockchainObject object1, BlockchainObject object2) {
                            return object1.get(finalOrder).compareTo(object2.get(finalOrder));
                        }
                    };
                } else {
                    comparator = new Comparator<BlockchainObject>() {
                        @Override
                        public int compare(BlockchainObject object1, BlockchainObject object2) {
                            return object2.get(finalOrder).compareTo(object1.get(finalOrder));
                        }
                    };
                }
            } else {
                if (!desc) {
                    comparator = new Comparator<BlockchainObject>() {
                        @Override
                        public int compare(BlockchainObject object1, BlockchainObject object2) {
                            return object1.getDateTimeCreate().compareTo(object2.getDateTimeCreate());
                        }
                    };
                } else {
                    comparator = new Comparator<BlockchainObject>() {
                        @Override
                        public int compare(BlockchainObject object1, BlockchainObject object2) {
                            return object2.getDateTimeCreate().compareTo(object1.getDateTimeCreate());
                        }
                    };
                }
            }

            Collections.sort(blockchainObjects, comparator);
        }

        if (filter.equals("") && !oldVersion) {
            count = count - countFiltered;
            countOld = countOld - countFilteredOld;
        }

        logger.trace("END search()");
        return new SearchResult(blockchainObjects, count, countOld);
    }

    /**
     * Returns a {@link BlockchainObject} {@code ArrayList} representing the
     * search result of objects for specified user with corresponding search
     * criteria and ordering
     *
     * @param userID,     the user ID for objects to search
     * @param oldVersion, specifies whether the old versions of objects must be
     *                    returned as well
     * @param filter,     the search filter criteria
     * @param limit,      the maximum number of records to return (default: max 500)
     * @param offset,     the number of results to ignore (default: 0)
     * @param order,      the sort attribute name and can specify "desc" to invert
     *                    the sorting result
     * @return an ordered {@code ArrayList} of objects found
     * @throws ORMException
     */
    public SearchResult searchUserObjects(long userID, boolean oldVersion, String filter, int limit, long offset, String order) throws ORMException {
        logger.debug("START searchUserObjects(long, boolean, String, int, long, String)");

        Long count = 0l;
        Long countOld = 0l;
        ArrayList<BlockchainObject> blockchainObjects = new ArrayList();

        int searchLimit = ApplicationContext.getInstance().getInteger("NodeObjectSearchResultMax");

        if (limit > searchLimit) {
            limit = searchLimit;
        }

        if (limit == -1) {
            limit = 20;
        }

        if (offset == -1) {
            offset = 0;
        }

        logger.debug("Search filter: \"" + filter + "\"");

        ArrayList<BlockchainObject> objectIDs = new ArrayList();

        BlockchainIndex<String, ArrayList<String>> searchIndex = BlockchainObjectIndex.getInstance().getIndex(BlockchainObjectIndex.INDEX_TYPE.USER_OBJECTS);
        BlockchainIndex<String, String> currentVersionsIndex = BlockchainObjectIndex.getInstance().getIndex(BlockchainObjectIndex.INDEX_TYPE.CURRENT_VERSIONS);

        if (searchIndex.containsKey(userID)) {
            ArrayList<String> objectUIDs = searchIndex.get(userID);
            if (filter.equals("") && !oldVersion) {
                count = (long) objectUIDs.size();
            }

            int objectLimitCount = 0;

            for (String uid : objectUIDs) {
                String objectID = currentVersionsIndex.get(uid);
                boolean skippedOffsetOld = false;
                logger.debug("Search objectID: " + objectID);
                BlockchainObject object = read(objectID);
                if (object != null) {
                    AttrIDList parentIDs2 = object.getParentList();
                    logger.debug("Search parentIDList: " + parentIDs2);

                    logger.debug("Search object.matchSearchFilter(filter): " + object.matchSearchFilter(filter));
                    if (filter != null && object.matchSearchFilter(filter)) {
                        if (!(filter.equals("") && !oldVersion)) {
                            count++;
                        }
                        logger.debug("Search offset: " + offset);
                        if (offset <= 0) {

                            //logger.debug("objectIDs.size(): " + objectIDs.size());
                            logger.debug("objectLimitCount: " + objectLimitCount);
                            logger.debug("limit: " + limit);

                            if (objectLimitCount < limit) {
                                objectIDs.add(object);
                                objectLimitCount++;
                                logger.debug("Search add: " + object.getDisplayName() + " id: " + objectID + " UID: " + object.getUid());
                            } else {
                                if (filter.equals("") && !oldVersion) {
                                    break;
                                }
                            }
                        } else {
                            offset--;
                            skippedOffsetOld = true;
                        }

                    }
                    if (oldVersion && !skippedOffsetOld) {
                        AttrIDList parentIDs = object.getParentList();
                        logger.debug("   Parent Search parentIDList: " + parentIDs);

                        for (AttrID parentID : parentIDs) {
                            if (objectID.equals(parentID.toString())) {
                                continue;
                            }

                            BlockchainObject parentObject = read(parentID);
                            logger.debug("   Parent Search parentID: " + parentID);
                            logger.debug("   Parent Search object.matchSearchFilter(filter): " + parentObject.matchSearchFilter(filter));
                            if (filter != null && parentObject.matchSearchFilter(filter)) {

                                count++;
                                countOld++;
                                logger.debug("   Parent Search offset: " + offset);
                                if (offset <= 0) {

                                    //    logger.debug("   Parent objectIDs.size(): " + objectIDs.size());
                                    //    logger.debug("   Parent limit: " + limit);
                                    //    if (objectIDs.size() < limit) {
                                    if (objectLimitCount < limit || objectIDs.contains(object)) {
                                        if (!objectIDs.contains(object)) {
                                            objectIDs.add(object);
                                            objectLimitCount++;
                                            count++;
                                        }
                                        objectIDs.add(parentObject);
                                        logger.debug("   Parent Search add: " + parentObject.getDisplayName() + " id: " + parentID + " UID: " + parentObject.getUid());
                                    } else {
                                        if (filter.equals("") && !oldVersion) {
                                            break;
                                        }
                                    }
                                    //    } else {
                                    //break;
                                    //    }
                                } else {
                                    offset--;
                                }
                            }
                        }
                    }
                }
                if (objectIDs.size() == limit) {
                    if (filter.equals("") && !oldVersion) {
                        break;
                    }
                }
            }
        }

        blockchainObjects = objectIDs;

        if (order != null && !order.equals("")) {

            logger.debug("Search Order before: " + order);
            boolean desc = false;

            Comparator comparator;

            if (order.endsWith("desc")) {
                order = order.substring(0, order.length() - 4);
                order = order.trim();
                desc = true;
            }

            final String finalOrder = order;
            logger.debug("Search Order after: " + finalOrder);

            if (!finalOrder.equals("")) {
                if (!desc) {
                    comparator = new Comparator<BlockchainObject>() {
                        @Override
                        public int compare(BlockchainObject object1, BlockchainObject object2) {
                            return object1.get(finalOrder).compareTo(object2.get(finalOrder));
                        }
                    };
                } else {
                    comparator = new Comparator<BlockchainObject>() {
                        @Override
                        public int compare(BlockchainObject object1, BlockchainObject object2) {
                            return object2.get(finalOrder).compareTo(object1.get(finalOrder));
                        }
                    };
                }
            } else {
                if (!desc) {
                    comparator = new Comparator<BlockchainObject>() {
                        @Override
                        public int compare(BlockchainObject object1, BlockchainObject object2) {
                            return object1.getDateTimeCreate().compareTo(object2.getDateTimeCreate());
                        }
                    };
                } else {
                    comparator = new Comparator<BlockchainObject>() {
                        @Override
                        public int compare(BlockchainObject object1, BlockchainObject object2) {
                            return object2.getDateTimeCreate().compareTo(object1.getDateTimeCreate());
                        }
                    };
                }
            }

            Collections.sort(blockchainObjects, comparator);
        }

        logger.debug("END searchUserObjects()");
        return new SearchResult(blockchainObjects, count, countOld);
    }

    /**
     * Returns a {@code JSONArray} representing the menu hierarchy of models to
     * display in the web interface
     *
     * @return {@code JSONArray} representing the menu hierarchy of models
     * @throws ORMException
     */
    public JSONArray searchMenu() throws ORMException {
        logger.trace("START searchMenu()");

        JSONArray result = new JSONArray();

        BlockchainIndex<String, ArrayList<String>> indexActive = BlockchainObjectIndex.getInstance().getIndex(BlockchainObjectIndex.INDEX_TYPE.VIRTUAL_TABLES_ACTIVE);

        if (indexActive.containsKey("Menu")) {

            ArrayList<String> uidList = indexActive.get("Menu");

            TreeMap<String, Menu> menuMap = new TreeMap();
            for (String uid : uidList) {

                BlockchainIndex<String, String> currentVersionsIndex = BlockchainObjectIndex.getInstance().getIndex(BlockchainObjectIndex.INDEX_TYPE.CURRENT_VERSIONS);
                String blockID = currentVersionsIndex.get(uid);
                try {
                    Menu menu = (Menu) read(blockID);

                    if (menu != null && menu.isVisible()) {

                        if (!CheetahWebserver.getInstance().isAdminUser(user.getLogin())) {
                            if (ApplicationContext.getInstance().getBoolean("ACLEnableMenu") && menu.getMenuACLs().size() > 0) {
                                if (ACL.isReadAllGranted(menu, user, menu.getMenuACLs())) {
                                    menuMap.put(menu.get("Position").toString(), menu);
                                }
                            } else {
                                menuMap.put(menu.get("Position").toString(), menu);
                            }
                        } else {
                            menuMap.put(menu.get("Position").toString(), menu);
                        }
                    }

                } catch (Exception ex) {
                    logger.error("Error reading menu with uid '" + uid + "': " + ex.toString());
                    BlockchainObjectIndex.getInstance().removeUIDfromModel(uid, "Menu");
                }
            }

            // general menu section
            ArrayList<String> menuCreated = new ArrayList();
            for (String menuString : menuMap.keySet()) {

                try {
                    String filter = "";

                    if (menuMap.get(menuString).get("Search Filter") != null) {
                        filter = menuMap.get(menuString).get("Search Filter").toString();
                    }

                    if (!menuCreated.contains(menuString)) {

                        StringTokenizer stringtokenizer = new StringTokenizer(menuString, "/");
                        int tokenNumber = stringtokenizer.countTokens();
                        int tokencount = 1;
                        String totalmenu = "";
                        String submenu = "";
                        String model = "";
                        while (stringtokenizer.hasMoreTokens()) {

                            if (tokencount < tokenNumber) {
                                String superMenu = stringtokenizer.nextToken();
                                if (tokencount == tokenNumber) {
                                    model = menuMap.get(menuString).getMenuModelName();

                                } else {
                                    model = "";
                                }

                                if (!menuCreated.contains(superMenu)) {

                                    JSONObject jsonObject = new JSONObject();
                                    jsonObject.put("text", superMenu);
                                    if (totalmenu.equals("")) {
                                        jsonObject.put("parentid", -1);
                                    } else {
                                        jsonObject.put("parentid", totalmenu);
                                    }

                                    jsonObject.put("model", model);
                                    jsonObject.put("menuID", menuMap.get(menuString).getId().toString());
                                    jsonObject.put("filter", filter);
                                    jsonObject.put("View List", menuMap.get(menuString).get("View List") == null ? false : menuMap.get(menuString).get("View List"));
                                    jsonObject.put("View Form", menuMap.get(menuString).get("View Form") == null ? false : menuMap.get(menuString).get("View Form"));
                                    jsonObject.put("View Tree", menuMap.get(menuString).get("View Tree") == null ? false : menuMap.get(menuString).get("View Tree"));
                                    jsonObject.put("View Kanban", menuMap.get(menuString).get("View Kanban") == null ? false : menuMap.get(menuString).get("View Kanban"));
                                    jsonObject.put("Own", menuMap.get(menuString).get("Own") == null ? false : menuMap.get(menuString).get("Own"));

                                    if (superMenu.startsWith("Admin")) {
                                        jsonObject.put("align", "right");
                                    }

                                    if (totalmenu.equals("")) {
                                        totalmenu = superMenu;
                                    } else {
                                        totalmenu += "/" + superMenu;
                                    }

                                    jsonObject.put("id", totalmenu);

                                    if (totalmenu.startsWith("Admin")) {

                                        result.add(jsonObject);
                                        menuCreated.add(totalmenu);
                                        logger.trace("Found Menu (1): " + tokencount + ": " + jsonObject.toJSONString());
                                    } else {
                                        result.add(jsonObject);
                                        menuCreated.add(totalmenu);
                                        logger.trace("Found Menu (2): " + tokencount + ": " + jsonObject.toJSONString());
                                    }
                                } else {

                                    if (totalmenu.equals("")) {
                                        totalmenu = superMenu;
                                    } else {
                                        totalmenu += "/" + superMenu;
                                    }
                                }
                            } else {
                                submenu = stringtokenizer.nextToken();
                            }
                            tokencount++;
                        }

                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("id", menuString);
                        jsonObject.put("text", submenu);
                        if (totalmenu.equals("")) {
                            jsonObject.put("parentid", -1);
                        } else {
                            jsonObject.put("parentid", totalmenu);
                        }

                        jsonObject.put("model", menuMap.get(menuString).getMenuModelName());
                        jsonObject.put("menuID", menuMap.get(menuString).getId().toString());
                        jsonObject.put("filter", filter);
                        jsonObject.put("View List", menuMap.get(menuString).get("View List") == null ? false : menuMap.get(menuString).get("View List"));
                        jsonObject.put("View Form", menuMap.get(menuString).get("View Form") == null ? false : menuMap.get(menuString).get("View Form"));
                        jsonObject.put("View Tree", menuMap.get(menuString).get("View Tree") == null ? false : menuMap.get(menuString).get("View Tree"));
                        jsonObject.put("View Kanban", menuMap.get(menuString).get("View Kanban") == null ? false : menuMap.get(menuString).get("View Kanban"));
                        jsonObject.put("Own", menuMap.get(menuString).get("Own") == null ? false : menuMap.get(menuString).get("Own"));

                        if (totalmenu.startsWith("Admin")) {
                            jsonObject.put("align", "right");

                            result.add(jsonObject);
                            menuCreated.add(submenu);
                            logger.trace("Found Menu Leaf (1): " + jsonObject.toJSONString());
                        } else {
                            result.add(jsonObject);
                            menuCreated.add(submenu);
                            logger.trace("Found Menu Leaf (2): " + jsonObject.toJSONString());
                        }
                    }
                } catch (Exception ex) {
                    logger.error("Error reading menu '" + menuString + "': " + ex.toString());
                    ex.printStackTrace();
                }
            }

            // User menu section

            JSONObject jsonObject = new JSONObject();
            String menuName = user.getDisplayName();

            if (!CheetahWebserver.getInstance().isSessionAuthenticationEnabled()) {
                menuName = "Admin";
            }

            if (menuName.equals("admin")) {
                menuName = "Admin";
            }

            String superMenu = menuName;
            String totalmenu = superMenu;

            jsonObject.put("text", menuName);
            jsonObject.put("parentid", -1);
            jsonObject.put("model", "");
            jsonObject.put("menuID", "UserMenu_" + menuName);
            jsonObject.put("filter", "");
            jsonObject.put("id", totalmenu);
            jsonObject.put("align", "right");

            if (!menuCreated.contains(menuName)) {
                result.add(jsonObject);
            }

            Menu myPreferences = searchMenuMyPreferences();

            if (myPreferences != null && !user.getLogin().equals("guest")) {
                jsonObject = new JSONObject();
                menuName = myPreferences.getName();
                totalmenu = superMenu + "/" + menuName;

                jsonObject.put("text", menuName);
                jsonObject.put("parentid", superMenu);
                jsonObject.put("model", myPreferences.getMenuModelName());
                jsonObject.put("menuID", myPreferences.getId().toString());
                jsonObject.put("filter", "");
                jsonObject.put("id", totalmenu);
                jsonObject.put("View List", myPreferences.get("View List") == null ? false : myPreferences.get("View List"));
                jsonObject.put("View Form", myPreferences.get("View Form") == null ? false : myPreferences.get("View Form"));
                jsonObject.put("View Tree", myPreferences.get("View Tree") == null ? false : myPreferences.get("View Tree"));
                jsonObject.put("View Kanban", myPreferences.get("View Kanban") == null ? false : myPreferences.get("View Kanban"));
                jsonObject.put("Own", myPreferences.get("Own") == null ? false : myPreferences.get("Own"));
                jsonObject.put("align", "right");

                result.add(jsonObject);
            }


            jsonObject = new JSONObject();
            menuName = "Disconnect";
            totalmenu = superMenu + "/" + menuName;

            jsonObject.put("text", menuName);
            jsonObject.put("parentid", superMenu);
            jsonObject.put("model", "");
            jsonObject.put("menuID", "UserMenu_" + menuName);
            jsonObject.put("filter", "");
            jsonObject.put("id", totalmenu);
            jsonObject.put("align", "right");

            result.add(jsonObject);


        } else {
            throw new ORMException("Menu model not found in UID TreeMap");
        }

        logger.trace("END searchMenu()");
        return result;
    }

    private Menu searchMenuMyPreferences() {
        Menu result = null;
        String menuName = "My Preferences";

        try {
            BlockchainObjectReader reader = BlockchainObjectReader.getAdminInstance();
            SearchResult searchResult;
            searchResult = reader.search("Menu", BlockchainObjectIndex.INDEX_TYPE.VIRTUAL_TABLES_ACTIVE, false, "[[\"Name\",\"=\",\"" + menuName + "\"]]", 20, 0, "");

            IBlockchainAuthenticator authenticator = BlockchainAuthenticatorFactory.getBlockchainAuthenticator();

            if (searchResult.getCount() < 1) {
                logger.error("Error searching '" + menuName + "' menu: No menu found !");
            } else {
                if (searchResult.getCount() == 1) {
                    result = (Menu) searchResult.getBlockchainObjects().get(0);
                } else {
                    logger.error("Error searching '" + menuName + "' menu: More than one menu found !");
                    for (BlockchainObject object : searchResult.getBlockchainObjects()) {
                        logger.error("OBJECT: " + BlockchainObject.getJSON(object));
                    }
                }
            }

        } catch (ORMException ex) {
            logger.error("Error searching '" + menuName + "' menu: " + ex.toString());
        }

        return result;
    }

    public boolean isAdminInstance() {
        logger.trace("START isAdminInstance()");
        logger.trace("END isAdminInstance()");
        return user.getLogin().equals(Main.userAdminName);
    }

    private boolean checkReadAccess(BlockchainObject object, User user) {
        boolean result = false;

        if (!CheetahWebserver.getInstance().isAdminUser(user.getLogin())) {
            if (ApplicationContext.getInstance().getBoolean("ACLEnableModel")) {
                Model model = null;
                if (object.getModelID() != null) {
                    try {

                        BlockchainIndex<String, String> currentVersionsIndex = BlockchainObjectIndex.getInstance().getIndex(BlockchainObjectIndex.INDEX_TYPE.CURRENT_VERSIONS);
                        String idString = currentVersionsIndex.get(object.getModelID().getUID());

                        model = (Model) readWithoutCheck(new AttrID(idString));
                    } catch (Exception ex) {
                        logger.error("Error searching Meta model for modelID: '" + object.getModelID().toString() + "': " + ex.toString());
                    }
                } else {
                    logger.warn("Warning: No model ID found at ACL check for object: '" + object.getDisplayName() + "'");
                }

                if (model != null && !model.getModelACLs().isEmpty()) {
                    boolean granted = false;
                    if (ACL.isReadAllGranted(object, user, model.getModelACLs())) {
                        granted = true;
                    }

                    if (!granted) {
                        if (object.isOwner(user) && ACL.isReadOwnGranted(object, user, model.getModelACLs())) {
                            granted = true;
                        }
                    }

                    if (!granted) {
                        if (ApplicationContext.getInstance().getBoolean("ACLPrintDebugTraces")) {
                            logger.warn("'" + object.getDisplayName() + "': READ: " + user.getLogin() + " NOT granted by Model ACL (Model: " + model.getDisplayName() + ")");
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
                if (!result && !object.getAcl().isEmpty() && object.checkSecurity(BlockchainObject.ACTION.READ, user)) {
                    result = true;
                    if (ApplicationContext.getInstance().getBoolean("ACLPrintDebugTraces")) {
                        logger.warn("'" + object.getDisplayName() + "': READ: " + user.getLogin() + " GRANTED by Object ACL (objectID: " + object.getId() + ")");
                    }
                } else {
                    if (result && !object.checkSecurity(BlockchainObject.ACTION.READ, user)) {
                        result = false;
                        if (ApplicationContext.getInstance().getBoolean("ACLPrintDebugTraces")) {
                            logger.warn("'" + object.getDisplayName() + "': READ: " + user.getLogin() + " NOT granted by Object ACL (objectID: " + object.getId() + ")");
                        }
                    }
                }
            }
        } else {
            result = true;
        }

        return result;
    }

    private BlockchainObject filterAttributeAccess(BlockchainObject object, User user) throws ORMException {

        if (ApplicationContext.getInstance().getBoolean("ACLEnableAttribute") && !CheetahWebserver.getInstance().isAdminUser(user.getLogin()) && object != null) {
            Model model = null;

            if (!Main.initDataModelFlag || (Main.initDataModelFlag && !object.getModel().equals("User"))) {

                if (Main.initDataModelFlag) {

                    if (object.get("Name").toString().equals("Model")) {

                        model = initIDSModels.get(initModelNameIDS.get(object.get("Name").toString()));
                    } else if (object.getModel().equals("Model")) {
                        model = initIDSModels.get(initModelNameIDS.get("Model"));
                    }
                } else {

                    BlockchainIndex<String, String> currentVersionsIndex = BlockchainObjectIndex.getInstance().getIndex(BlockchainObjectIndex.INDEX_TYPE.CURRENT_VERSIONS);
                    if (object.getModelID() != null) {
                        String idString = currentVersionsIndex.get(object.getModelID().getUID());
                        model = (Model) readWithoutCheck(new AttrID(idString));
                    }
                }
            }

            if (model != null) {
                AttrList<AttrAttribute> attributesList = model.getAttributes();
                for (Attr attr : attributesList) {
                    AttrAttribute attribute = (AttrAttribute) attr;

                    if (!checkAttributeReadAccess(attribute, object, user)) {
                        object.remove(attribute.Name);
                    }
                }
            }
        }

        return object;
    }

    private boolean checkAttributeReadAccess(AttrAttribute attribute, BlockchainObject object, User user) {
        boolean result = false;

        if (!CheetahWebserver.getInstance().isAdminUser(user.getLogin())) {

            if (!attribute.ACL.isEmpty()) {
                boolean granted = false;

                if (ACL.isReadAllGranted(object, user, attribute.ACL)) {
                    granted = true;
                }
                /*
                else {
                    if (ApplicationContext.getInstance().getBoolean("ACLPrintDebugTraces")) {
                        logger.warn("'" + object.getDisplayName() + "': ATTRIBUTE: " + user.getLogin() + " NOT granted by Object ALL ACL (objectID: " + object.getId() + ")");
                    }
                }
                */

                if (!granted) {
                    if (object.isOwner(user) && ACL.isReadOwnGranted(object, user, attribute.ACL)) {
                        granted = true;
                    }
                    /*
                    else{
                        if (ApplicationContext.getInstance().getBoolean("ACLPrintDebugTraces")) {
                            logger.warn("'" + object.getDisplayName() + "': ATTRIBUTE: " + user.getLogin() + " NOT granted by Object OWN ACL (objectID: " + object.getId() + ")");
                        }
                    }
                    */
                }

                if (!granted) {
                    result = false;
                } else {
                    result = true;
                }
            } else {
                result = true;
            }
        } else {
            result = true;
        }

        return result;
    }

    public static class SearchResult {

        private Long count = 0l;
        private Long countOld = 0l;
        private ArrayList<BlockchainObject> blockchainObjects;

        public SearchResult(ArrayList<BlockchainObject> blockchainObjects, Long count, Long countOld) {
            this.blockchainObjects = blockchainObjects;
            this.count = count;
            this.countOld = countOld;
        }

        public Long getCount() {
            return count;
        }

        public Long getCountOld() {
            return countOld;
        }

        public ArrayList<BlockchainObject> getBlockchainObjects() {
            return blockchainObjects;
        }

    }

    ;

    public static class LRUCache<K, V> extends LinkedHashMap<K, V> {

        private int capacity; // Maximum number of items in the cache.

        public LRUCache(int capacity) {
            super(capacity + 1, 1.0f, true); // Pass 'true' for accessOrder.
            this.capacity = capacity;
        }

        @Override
        public V get(Object key) {
            return (V) DeepCopy.copy(super.get(key));
        }

        @Override
        protected boolean removeEldestEntry(Entry entry) {
            return (size() > this.capacity);
        }
    }
}
