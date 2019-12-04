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

package com.recordins.recordin.orm;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.recordins.recordin.Main;
import com.recordins.recordin.orm.action.ActionDefinition;
import com.recordins.recordin.orm.attribute.*;
import com.recordins.recordin.orm.core.BlockchainIndex;
import com.recordins.recordin.orm.exception.ORMException;
import com.recordins.recordin.orm.core.BlockchainObjectIndex;
import com.recordins.recordin.orm.core.BlockchainObjectReader;

import java.io.Serializable;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentSkipListMap;

import com.recordins.recordin.utils.DeepCopy;
import org.cheetah.webserver.CheetahClassLoader;
import org.cheetah.webserver.CheetahWebserver;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BlockchainObject implements Serializable, Comparable<BlockchainObject> {

    /* Logger for console output */
    private static Logger logger = LoggerFactory.getLogger(BlockchainObject.class);

    private AttrPlatformVersion platformVersion;

    protected String model;

    private AttrID modelID = null;

    /* Corresponds to the block id */
    private AttrID id;

    /* Corresponds to an object unique identifier along its lifecycle*/
    //change to string
    //private AttrString uid;
    private String uid;

    /* Corresponds to an object index */
    //change to index
    private String index = "active";

    protected ArrayList<ActionDefinition> actionList = null;

    private ConcurrentSkipListMap<String, Attr> attrMap;

    protected AttrIDList parentList;

    protected AttrIDList acl;

    private AttrID userOwnerID = null;

    private AttrID userCreateID = null;

    private AttrID userUpdateID = null;

    private AttrID userDeleteID = null;

    private AttrDateTime dateTimeCreate = null;

    private AttrDateTime dateTimeUpdate = null;

    private AttrDateTime dateTimeDelete = null;

    private AttrString nodeUpdateID = null;

    private String displayName = "";

    public BlockchainObject() throws ORMException {
        logger.trace("START BlockchainObject()");

        this.platformVersion = new AttrPlatformVersion();
        this.attrMap = new ConcurrentSkipListMap();
        this.parentList = new AttrIDList();
        this.acl = new AttrIDList();
        this.dateTimeCreate = new AttrDateTime();
        this.actionList = new ArrayList();

        // Name of action Class, Action display string, Action display beahviour, Action args
        this.actionList.add(new ActionDefinition("Archive", "Archive", "Execute", "{}"));
        this.actionList.add(new ActionDefinition("UnArchive", "UnArchive", "Execute", "{}"));
        this.actionList.add(new ActionDefinition("Delete", "Delete", "ExecuteConfirm", "{\"Message\":\"Would you like to delete selected object(s) ?\"}"));
        this.actionList.add(new ActionDefinition("ChangeOwner", "Change Owner", "SelectSingle", "{\"model\":\"User\"}"));
        this.actionList.add(new ActionDefinition("SetACL", "Set ACL", "SelectMulti", "{\"model\":\"ACL\"}"));

        this.actionList.add(new ActionDefinition("SelectSingle", "Select Single", "SelectSingle", "{\"model\":\"Box\"}"));
        this.actionList.add(new ActionDefinition("SelectMulti", "Select Multi", "SelectMulti", "{\"model\":\"Box\"}"));
        this.actionList.add(new ActionDefinition("SelectSingleOld", "Select SingleOld", "SelectSingleOld", "{\"model\":\"Box\"}"));
        this.actionList.add(new ActionDefinition("SelectMultiOld", "Select MultiOld", "SelectMultiOld", "{\"model\":\"Box\"}"));

        setModel(this.getClass().getSimpleName());

        logger.trace("END BlockchainObject()");
    }

    public AttrPlatformVersion getPlatformVersion() {
        return platformVersion;
    }

    public void setPlatformVersion(AttrPlatformVersion platformVersion) {
        this.platformVersion = platformVersion;
    }

    public AttrID getModelID() {
        return modelID;
    }

    public void setModelID(AttrID modelID) {
        this.modelID = modelID;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public ConcurrentSkipListMap<String, Attr> getAttrMap() {
        return attrMap;
    }

    public void setAttrMap(ConcurrentSkipListMap<String, Attr> attrMap) {
        this.attrMap = attrMap;
    }

    public AttrIDList getParentList() {
        return parentList;
    }

    public void setParentList(AttrIDList parentList) {
        this.parentList = parentList;
    }

    public AttrID getUserOwnerID() {
        return userOwnerID;
    }

    public void setUserOwnerID(AttrID userOwnerID) {
        this.userOwnerID = userOwnerID;
    }

    public AttrID getUserCreateID() {
        return userCreateID;
    }

    public void setUserCreateID(AttrID userCreateID) {
        this.userCreateID = userCreateID;
    }

    public AttrID getUserUpdateID() {
        return userUpdateID;
    }

    public void setUserUpdateID(AttrID userUpdateID) {
        this.userUpdateID = userUpdateID;
    }

    public AttrID getUserDeleteID() {
        return userDeleteID;
    }

    public void setUserDeleteID(AttrID userDeleteID) {
        this.userDeleteID = userDeleteID;
    }

    public AttrString getNodeUpdateID() {
        return nodeUpdateID;
    }

    public void setNodeUpdateID(AttrString nodeUpdateID) {
        this.nodeUpdateID = nodeUpdateID;
    }

    public enum ACTION {CREATE, DELETE, READ, WRITE, ATTACHMENT}

    @JsonIgnore
    public boolean isOwner(User user) {
        boolean result = false;

        if (getUserOwnerID().getUID().equals(user.getId().getUID())) {
            result = true;
        }

        return result;
    }

    @JsonIgnore
    public AttrID getId() {
        return id;
    }

    @JsonIgnore
    public void setId(AttrID id) {
        this.id = id;
    }

    @JsonIgnore
    public User getUserOwner() {
        if (userOwnerID != null) {
            User user = null;
            try {
                user = (User) BlockchainObjectReader.getAdminInstance().read(userOwnerID);
            } catch (ORMException ex) {
                logger.error("Error reading user with id: '" + userOwnerID + "': " + ex.toString());
            }
            return user;
        } else {
            return null;
        }
    }

    public void setUserOwner(User userOwner) {
        if (userOwner != null) {
            this.userOwnerID = userOwner.getId();
        }
    }

    @JsonIgnore
    public User getUserCreate() {
        if (userCreateID != null) {
            User user = null;
            try {
                user = (User) BlockchainObjectReader.getAdminInstance().read(userCreateID);
            } catch (ORMException ex) {
                logger.error("Error reading user with id: '" + userCreateID + "': " + ex.toString());
            }
            return user;
        } else {
            return null;
        }
    }

    public void setUserCreate(User userCreate) {
        if (userCreate != null) {
            this.userCreateID = userCreate.getId();
        }
    }

    @JsonIgnore
    public User getUserUpdate() {
        if (userUpdateID != null) {
            User user = null;
            try {
                user = (User) BlockchainObjectReader.getAdminInstance().read(userUpdateID);
            } catch (ORMException ex) {
                logger.error("Error reading user with id: '" + userUpdateID + "': " + ex.toString());
            }
            return user;
        } else {
            return null;
        }
    }

    public void setUserUpdate(User userUpdate) {
        if (userUpdate != null) {
            this.userUpdateID = userUpdate.getId();
        }
    }

    @JsonIgnore
    public User getUserDelete() {
        if (userDeleteID != null) {
            User user = null;
            try {
                user = (User) BlockchainObjectReader.getAdminInstance().read(userDeleteID);
            } catch (ORMException ex) {
                logger.error("Error reading user with id: '" + userDeleteID + "': " + ex.toString());
            }
            return user;
        } else {
            return null;
        }
    }

    public void setUserDelete(User userDelete) {
        if (userDelete != null) {
            this.userDeleteID = userDelete.getId();
        }
    }

    public AttrDateTime getDateTimeCreate() {
        return dateTimeCreate;
    }

    public void setDateTimeCreate(AttrDateTime dateTimeCreate) {
        this.dateTimeCreate = dateTimeCreate;
    }

    public AttrDateTime getDateTimeUpdate() {
        return dateTimeUpdate;
    }

    public void setDateTimeUpdate(AttrDateTime dateTimeUpdate) {
        this.dateTimeUpdate = dateTimeUpdate;
    }

    public AttrDateTime getDateTimeDelete() {
        return dateTimeDelete;
    }

    public void setDateTimeDelete(AttrDateTime dateTimeDelete) {
        this.dateTimeDelete = dateTimeDelete;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public int size() {
        return this.attrMap.size();
    }

    @JsonIgnore
    public boolean isDeleted() {

        BlockchainObject currentVersion = null;

        try {
            BlockchainIndex<String, String> currentVersionsIndex = BlockchainObjectIndex.getInstance().getIndex(BlockchainObjectIndex.INDEX_TYPE.CURRENT_VERSIONS);
            if (currentVersionsIndex.containsKey(getId().getUID())) {
                String idString = currentVersionsIndex.get(getId().getUID());
                currentVersion = BlockchainObjectReader.getAdminInstance().read(idString);

            }
            logger.info("current Version dateTimeDelete: " + currentVersion.dateTimeDelete);

        } catch (ORMException e) {
            logger.error("Error reading currentVersion: " + e.toString());
            e.printStackTrace();
        }

        if (currentVersion == null) {
            currentVersion = this;
        }

        return currentVersion.dateTimeDelete == null ? false : true;
    }

    @JsonIgnore
    public boolean isEmpty() {
        return this.attrMap.isEmpty();
    }

    public boolean containsKey(String key) {
        switch (key) {

            case "userOwnerID":
                return true;

        }

        return this.attrMap.containsKey(key);
    }

    public boolean containsValue(Attr value) {
        return this.attrMap.containsValue(value);
    }

    public Attr get(String key) {
        Attr result = null;

        switch (key) {

            case "userOwnerID":
                return this.userOwnerID;

        }

        result = this.attrMap.get(key);

        if (result == null && !Main.initDataModelFlag) {
            try {
                Model model = getModel(this);
                if (model != null) {
                    AttrList<AttrAttribute> attributesList = model.getAttributes();
                    AttrAttribute attribute = null;

                    for (Attr attr : attributesList) {
                        AttrAttribute attributeTMP = (AttrAttribute) attr;

                        if (attributeTMP.Name.equals(key)) {
                            attribute = attributeTMP;
                            break;
                        }
                    }

                    if (attribute != null) {
                        CheetahClassLoader cl;

                        if (CheetahWebserver.getInstance() != null) {
                            cl = CheetahWebserver.getInstance().getClassLoader();
                        } else {
                            cl = new CheetahClassLoader(Thread.currentThread().getContextClassLoader());
                        }

                        Class c = cl.loadClass("com.recordins.recordin.orm.attribute." + attribute.AttrType);
                        Attr attr = (Attr) c.newInstance();
                        this.put(key, attr);
                        return attr;
                    }
                }

            } catch (Exception e) {
                logger.error("Error getting model for object '" + this.getDisplayName() + "': " + e.toString());
            }
        }

        return result;
    }

    public final Attr put(String key, Attr value) {
        return this.attrMap.put(key, value);
    }

    public final Attr put(String key, String value) {
        return this.attrMap.put(key, new AttrString(value));
    }

    public Attr remove(String key) {
        return this.attrMap.remove(key);
    }

    public void replace(String key, Attr value) {
        if (this.attrMap.containsKey(key)) {
            this.attrMap.replace(key, value);
        } else {
            this.attrMap.put(key, value);
        }
    }

    public void replace(String key, String value) {
        if (this.attrMap.containsKey(key)) {
            this.attrMap.replace(key, new AttrString(value));
        } else {
            this.attrMap.put(key, new AttrString(value));
        }
    }

    public void putAll(Map<? extends String, ? extends Attr> m) {
        this.attrMap.putAll(m);
    }

    public void clear() {
        this.attrMap.clear();
    }

    public Set<String> keySet() {
        return this.attrMap.keySet();
    }

    public Collection<Attr> values() {
        return this.attrMap.values();
    }

    public Set<Entry<String, Attr>> entrySet() {
        return this.attrMap.entrySet();
    }

    @JsonIgnore
    public boolean isEmptyParentList() {
        return this.parentList.isEmpty();
    }

    /**
     * Returns {@code String} representation of current instance.
     *
     * @return {@code String} representation of current instance
     */
    @JsonIgnore
    @Override
    public String toString() {
        logger.trace("START toString()");

        String result = "";

        try {
            result = getJSON(this).toJSONString();
        } catch (ORMException ex) {
            logger.error("Error reading Object to Json: " + ex.toString());
        }

        return result;
    }

    @JsonIgnore
    public boolean matchSearchFilter(String filter) {
        boolean result = false;

        if (filter == null || filter.equals("")) {
            return true;
        }

        JSONArray jsonFilter = null;

        try {
            JSONParser parser = new JSONParser();
            jsonFilter = (JSONArray) parser.parse(filter);

        } catch (Exception e) {
        }

        if (jsonFilter == null) {
            if (this.toString().contains(filter)) {
                result = true;
            }
        } else {

            for (int i = 0; i < jsonFilter.size(); i++) {
                JSONArray criteria = (JSONArray) jsonFilter.get(i);
                if (criteria.size() == 3) {

                    String attributeName = (String) criteria.get(0);
                    String operator = (String) criteria.get(1);
                    String targetValue = (String) criteria.get(2);

                    //        logger.debug("attributeName: " + attributeName);
                    if (this.containsKey(attributeName)) {

                        Attr attribute = this.get(attributeName);

                        switch (operator) {

                            case "=":
                            case "equals":
                                //                    logger.debug("this.get(attributeName): " + this.get(attributeName));
                                //                    logger.debug("targetValue: " + targetValue);
                                if (attribute != null && attribute.toString().equals(targetValue)) {
                                    result = true;
                                    break;
                                }
                                break;

                            case "in":
                            case "contains":
                                if (attribute != null && attribute.toString().contains(targetValue)) {
                                    result = true;
                                    break;
                                }
                                break;
                        }
                    }
                }
            }
        }

        return result;
    }

    @JsonIgnore
    public ArrayList<ActionDefinition> getActionList() {
        return this.actionList;
    }

    @JsonIgnore
    public BlockchainObject clone() {

        return (BlockchainObject) DeepCopy.copy(this);
    }

    @JsonIgnore
    public static BlockchainObject clone(BlockchainObject blockchainObject) {

        return (BlockchainObject) DeepCopy.copy(blockchainObject);
    }

    public String getDisplayName() {

        String result = this.displayName;

        return result;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void validate() throws ORMException {

    }

    public boolean checkSecurity(ACTION action, User user) {

        if (!acl.isEmpty()) {

            boolean granted = false;
            switch (action) {

                case CREATE:

                    if (ACL.isCreateGranted(this, user, acl)) {
                        granted = true;
                    }

                    if (!granted) {
                        return false;
                    } else {
                        return true;
                    }

                case DELETE:

                    if (ACL.isDeleteAllGranted(this, user, acl)) {
                        granted = true;
                    }

                    if (!granted) {
                        if (this.isOwner(user) && ACL.isDeleteOwnGranted(this, user, acl)) {
                            granted = true;
                        }
                    }

                    if (!granted) {
                        return false;
                    } else {
                        return true;
                    }

                case READ:

                    if (ACL.isReadAllGranted(this, user, acl)) {
                        granted = true;
                    }

                    if (!granted) {
                        if (this.isOwner(user) && ACL.isReadOwnGranted(this, user, acl)) {
                            granted = true;
                        }
                    }

                    if (!granted) {
                        return false;
                    } else {
                        return true;
                    }

                case WRITE:

                    if (ACL.isWriteAllGranted(this, user, acl)) {
                        granted = true;
                    }

                    if (!granted) {
                        if (this.isOwner(user) && ACL.isWriteOwnGranted(this, user, acl)) {
                            granted = true;
                        }
                    }

                    if (!granted) {
                        return false;
                    } else {
                        return true;
                    }


                case ATTACHMENT:

                    if (ACL.isAttachmentAllGranted(this, user, acl)) {
                        granted = true;
                    }

                    if (!granted) {
                        if (this.isOwner(user) && ACL.isAttachmentOwnGranted(this, user, acl)) {
                            granted = true;
                        }
                    }

                    if (!granted) {
                        return false;
                    } else {
                        return true;
                    }
            }

            return false;
        }

        return true;
    }

    public void init(User user) throws ORMException {

    }

    public void create(User user) throws ORMException {

    }

    public void write(User user) throws ORMException {

    }

    public void delete(User user) throws ORMException {

    }

    /**
     * Compares current {@link BlockchainObject} value with {@link BlockchainObject} value
     * of given instance.
     *
     * @param object the object to be compared.
     * @return a negative {@code Integer}, zero, or a positive {@code Integer}
     * as this object is less than, equal to, or greater than the given
     * instance.
     */
    @Override
    public int compareTo(BlockchainObject object) {
        logger.trace("START compareTo(BlockchainObject)");


        AttrDateTime currentObject = this.getDateTimeCreate();
        AttrDateTime conparedObject = object.getDateTimeCreate();

        if (this.getDateTimeUpdate() != null) {
            currentObject = this.getDateTimeUpdate();
        }

        if (object.getDateTimeUpdate() != null) {
            conparedObject = object.getDateTimeUpdate();
        }

        logger.trace("END compareTo()");
        return currentObject.compareTo(conparedObject);
        //return this.getDisplayName().compareTo(object.getDisplayName());
    }

    @JsonIgnore
    public static Model getModel(BlockchainObject object) throws ORMException {
        logger.trace("START getModel(BlockchainObject)");

        Model result = null;

        if (object.getModelID() != null) {
            try {
                result = (Model) BlockchainObjectReader.getInstance(User.getAdminUser(false)).readWithoutCheck(object.getModelID());
            } catch (Exception ex) {
                logger.error("Error searching Meta model for modelID: '" + object.getModelID().toString() + "': " + ex.toString());
            }
        }

        if (result == null) {
            String modelName = object.getModel();

            BlockchainObjectReader.SearchResult searchResult = BlockchainObjectReader.getInstance(User.getAdminUser(false)).search("Model", BlockchainObjectIndex.INDEX_TYPE.VIRTUAL_TABLES_ACTIVE, false, "[[\"Name\",\"=\",\"" + modelName + "\"]]", -1, -1, "");
            ArrayList<BlockchainObject> objectList = searchResult.getBlockchainObjects();

            if (objectList.size() == 0) {
                throw new ORMException("No model found for name: '" + modelName + "'");
            }
            if (objectList.size() > 1) {
                for (BlockchainObject objectFromList : objectList) {
                    logger.debug("schema found for model: '" + modelName + "': " + object.getDisplayName() + ": " + object.getId().toString());
                }
                throw new ORMException("More than one model found for name: '" + modelName + "'");
            }
            result = (Model) objectList.get(0);
        }

        logger.trace("END getModel()");
        return result;
    }

    /**
     * Returns a {@link JSONArray} representation of the
     * {@link BlockchainObject}
     *
     * @param object {@link BlockchainObject} representation of the object
     * @throws ORMException
     */
    @JsonIgnore
    public static JSONArray getJSON(BlockchainObject object) throws ORMException {
        logger.trace("START getJSON(BlockchainObject)");

        JSONArray result = new JSONArray();
        String jsonString = "[]";

        JSONParser parser = new JSONParser();

        try {
            ObjectMapper mapper = new ObjectMapper().enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
            jsonString = mapper.writeValueAsString(object);

            JSONArray jsonArray = (JSONArray) parser.parse(jsonString);
            if (object.getId() != null) {
                ((JSONObject) jsonArray.get(1)).put("id", object.getId().toString());
            }
            result = jsonArray;
        } catch (Exception ex) {
            logger.error("Error processing Object to JSON : " + ex.toString());
            throw new ORMException("Error processing Object to JSON : " + ex.toString());
        }

        logger.trace("END getJSON()");
        return result;
    }


    @JsonIgnore
    public boolean containsObjectACL(AttrID aclID) {
        return this.acl.containsUID(aclID);
    }

    @JsonIgnore
    public boolean containsObjectACL(ACL acl) {
        return containsObjectACL(acl.getId());
    }

    @JsonIgnore
    public void addObjectACL(AttrID aclID) {
        if (!containsObjectACL(aclID)) {
            this.acl.add(aclID);
        }
    }

    @JsonIgnore
    public void addObjectACL(ACL acl) {
        addObjectACL(acl.getId());
    }

    @JsonIgnore
    public void removeObjectACL(AttrID aclID) {
        this.acl.removeUID(aclID);
    }

    @JsonIgnore
    public void removeObjectACL(ACL acl) {
        removeObjectACL(acl.getId());
    }

    @JsonIgnore
    public void setObjectACLs(List<AttrID> acls) {
        this.acl = new AttrIDList(acls);
    }

    @JsonIgnore
    public void setObjectACLs(AttrID... aclIDs) {
        this.acl = new AttrIDList(aclIDs);
    }

    public AttrIDList getAcl() {
        return acl;
    }

    public void setAcl(AttrIDList acl) {
        this.acl = acl;
    }

}
