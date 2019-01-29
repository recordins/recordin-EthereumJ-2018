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
import com.recordins.recordin.orm.attribute.AttrBoolean;
import com.recordins.recordin.orm.attribute.AttrID;
import com.recordins.recordin.orm.attribute.AttrIDList;
import com.recordins.recordin.orm.attribute.AttrString;
import com.recordins.recordin.orm.exception.ORMException;
import com.recordins.recordin.orm.core.BlockchainIndex;
import com.recordins.recordin.orm.core.BlockchainObjectIndex;
import com.recordins.recordin.orm.core.BlockchainObjectReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class ACL extends BlockchainObject {

    /* Logger for console output */
    private static Logger logger = LoggerFactory.getLogger(ACL.class);

    public ACL() throws ORMException {
        super();

        actionList.clear();
        setModel(this.getClass().getSimpleName());
    }

    public ACL(String name) throws ORMException {
        this();

        setName(name);
    }

    @JsonIgnore
    public void setName(String name) {
        this.replace("Name", new AttrString(name));
    }

    @JsonIgnore
    public String getName() {
        return this.get("Name").toString();
    }

    @JsonIgnore
    public boolean containsGrantedUsers(AttrID userID) {
        AttrIDList users = (AttrIDList) this.get("Granted Users");
        return users.containsUID(userID);
    }

    @JsonIgnore
    public boolean containsGrantedUsers(User user) {
        return containsGrantedUsers(user.getId());
    }

    @JsonIgnore
    public void addGrantedUser(AttrID userID) {
        if (!containsGrantedUsers(userID)) {
            ((AttrIDList) this.get("Granted Users")).add(userID);
        }
    }

    @JsonIgnore
    public void addGrantedUser(User user) {
        addGrantedUser(user.getId());
    }

    @JsonIgnore
    public void removeGrantedUser(AttrID userID) {
        ((AttrIDList) this.get("Granted Users")).removeUID(userID);
    }

    @JsonIgnore
    public void removeGrantedUser(User user) {
        removeGrantedUser(user.getId());
    }

    @JsonIgnore
    public void setGrantedUsers(List<AttrID> users) {
        this.replace("Granted Users", new AttrIDList(users));
    }

    @JsonIgnore
    public void setGrantedUsers(AttrID... userIDs) {
        this.replace("Granted Users", new AttrIDList(userIDs));
    }

    @JsonIgnore
    public AttrIDList getGrantedUsers() {
        return (AttrIDList) this.get("Granted Users");
    }

    @JsonIgnore
    public boolean containsGrantedGroups(AttrID groupID) {
        AttrIDList groups = (AttrIDList) this.get("Granted Groups");
        return groups.containsUID(groupID);
    }

    @JsonIgnore
    public boolean containsGrantedGroups(Group group) {
        return containsGrantedGroups(group.getId());
    }

    @JsonIgnore
    public void addGrantedGroup(AttrID groupID) {
        if (!containsGrantedGroups(groupID)) {
            ((AttrIDList) this.get("Granted Groups")).add(groupID);
        }
    }

    @JsonIgnore
    public void addGrantedGroup(Group group) {
        addGrantedGroup(group.getId());
    }

    @JsonIgnore
    public void removeGrantedGroup(AttrID userID) {
        ((AttrIDList) this.get("Granted Groups")).removeUID(userID);
    }

    @JsonIgnore
    public void removeGrantedGroup(Group group) {
        removeGrantedGroup(group.getId());
    }

    @JsonIgnore
    public void setGrantedGroups(List<AttrID> groups) {
        this.replace("Granted Groups", new AttrIDList(groups));
    }

    @JsonIgnore
    public void setGrantedGroups(AttrID... groupIDs) {
        this.replace("Granted Groups", new AttrIDList(groupIDs));
    }

    @JsonIgnore
    public AttrIDList getGrantedGroups() {
        return (AttrIDList) this.get("Granted Groups");
    }

    @JsonIgnore
    public boolean containsDeniedUsers(AttrID userID) {
        AttrIDList users = (AttrIDList) this.get("Denied Users");
        return users.containsUID(userID);
    }

    @JsonIgnore
    public boolean containsDeniedUsers(User user) {
        return containsDeniedUsers(user.getId());
    }

    @JsonIgnore
    public void addDeniedUser(AttrID userID) {
        if (!containsDeniedUsers(userID)) {
            ((AttrIDList) this.get("Denied Users")).add(userID);
        }
    }

    @JsonIgnore
    public void addDeniedUser(User user) {
        addDeniedUser(user.getId());
    }

    @JsonIgnore
    public void removeDeniedUser(AttrID userID) {
        ((AttrIDList) this.get("Denied Users")).removeUID(userID);
    }

    @JsonIgnore
    public void removeDeniedUser(User user) {
        removeDeniedUser(user.getId());
    }

    @JsonIgnore
    public void setDeniedUsers(List<AttrID> users) {
        this.replace("Denied Users", new AttrIDList(users));
    }

    @JsonIgnore
    public void setDeniedUsers(AttrID... userIDs) {
        this.replace("Denied Users", new AttrIDList(userIDs));
    }

    @JsonIgnore
    public AttrIDList getDeniedUsers() {
        return (AttrIDList) this.get("Denied Users");
    }

    @JsonIgnore
    public boolean containsDeniedGroups(AttrID groupID) {
        AttrIDList groups = (AttrIDList) this.get("Denied Groups");
        return groups.containsUID(groupID);
    }

    @JsonIgnore
    public boolean containsDeniedGroups(Group group) {
        return containsDeniedGroups(group.getId());
    }

    @JsonIgnore
    public void addDeniedGroup(AttrID groupID) {
        if (!containsDeniedGroups(groupID)) {
            ((AttrIDList) this.get("Denied Groups")).add(groupID);
        }
    }

    @JsonIgnore
    public void addDeniedGroup(Group group) {
        addDeniedGroup(group.getId());
    }

    @JsonIgnore
    public void removeDeniedGroup(AttrID userID) {
        ((AttrIDList) this.get("Denied Groups")).removeUID(userID);
    }

    @JsonIgnore
    public void removeDeniedGroup(Group group) {
        removeDeniedGroup(group.getId());
    }

    @JsonIgnore
    public void setDeniedGroups(List<AttrID> groups) {
        this.replace("Denied Groups", new AttrIDList(groups));
    }

    @JsonIgnore
    public void setDeniedGroups(AttrID... groupIDs) {
        this.replace("Denied Groups", new AttrIDList(groupIDs));
    }

    @JsonIgnore
    public AttrIDList getDeniedGroups() {
        return (AttrIDList) this.get("Denied Groups");
    }


    @JsonIgnore
    public void setFilter(String name) {
        this.replace("Filter", new AttrString(name));
    }

    @JsonIgnore
    public String getFilter() {
        return this.get("Filter").toString();
    }

    @JsonIgnore
    public boolean isCreate() {
        boolean result = false;
        if (this.get("Create") != null) {
            result = ((AttrBoolean) this.get("Create")).isBoolValue();
        }
        return result;
    }

    @JsonIgnore
    public void setCreate(boolean isCreate) {
        this.replace("Create", new AttrBoolean(isCreate));
    }

    @JsonIgnore
    public boolean isDeleteAll() {
        boolean result = false;
        if (this.get("Delete All") != null) {
            result = ((AttrBoolean) this.get("Delete All")).isBoolValue();
        }
        return result;
    }

    @JsonIgnore
    public void setDeleteAll(boolean isDeleteAll) {
        this.replace("Delete All", new AttrBoolean(isDeleteAll));
    }

    @JsonIgnore
    public boolean isDeleteOwn() {
        boolean result = false;
        if (this.get("Delete Own") != null) {
            result = ((AttrBoolean) this.get("Delete Own")).isBoolValue();
        }
        return result;
    }

    @JsonIgnore
    public void setDeleteOwn(boolean isDeleteOwn) {
        this.replace("Delete Own", new AttrBoolean(isDeleteOwn));
    }

    @JsonIgnore
    public boolean isReadAll() {
        boolean result = false;
        if (this.get("Read All") != null) {
            result = ((AttrBoolean) this.get("Read All")).isBoolValue();
        }
        return result;
    }

    @JsonIgnore
    public void setReadAll(boolean isReadAll) {
        this.replace("Read All", new AttrBoolean(isReadAll));
    }

    @JsonIgnore
    public boolean isReadOwn() {
        boolean result = false;
        if (this.get("Read Own") != null) {
            result = ((AttrBoolean) this.get("Read Own")).isBoolValue();
        }
        return result;
    }

    @JsonIgnore
    public void setReadOwn(boolean isReadOwn) {
        this.replace("Read Own", new AttrBoolean(isReadOwn));
    }

    @JsonIgnore
    public boolean isWriteAll() {
        boolean result = false;
        if (this.get("Write All") != null) {
            result = ((AttrBoolean) this.get("Write All")).isBoolValue();
        }
        return result;
    }

    @JsonIgnore
    public void setWriteAll(boolean isWrite) {
        this.replace("Write All", new AttrBoolean(isWrite));
    }

    @JsonIgnore
    public boolean isWriteOwn() {
        boolean result = false;
        if (this.get("Write Own") != null) {
            result = ((AttrBoolean) this.get("Write Own")).isBoolValue();
        }
        return result;
    }

    @JsonIgnore
    public void setWriteOwn(boolean isWriteOwn) {
        this.replace("Write Own", new AttrBoolean(isWriteOwn));
    }

    @JsonIgnore
    public boolean isAttachmentAll() {
        boolean result = false;
        if (this.get("Attachment All") != null) {
            result = ((AttrBoolean) this.get("Attachment All")).isBoolValue();
        }
        return result;
    }

    @JsonIgnore
    public void setAttachmentAll(boolean isAttachmentAll) {
        this.replace("Attachment All", new AttrBoolean(isAttachmentAll));
    }

    @JsonIgnore
    public boolean isAttachmentOwn() {
        boolean result = false;
        if (this.get("Attachment Own") != null) {
            result = ((AttrBoolean) this.get("Attachment Own")).isBoolValue();
        }
        return result;
    }

    @JsonIgnore
    public void setAttachmentOwn(boolean isAttachmentOwn) {
        this.replace("Attachment Own", new AttrBoolean(isAttachmentOwn));
    }

    /*
    @JsonIgnore
    public boolean isUpload() {
        boolean result = false;
        if (this.get("Upload") != null) {
            result = ((AttrBoolean) this.get("Upload")).isBoolValue();
        }
        return result;
    }

    @JsonIgnore
    public void setUpload(boolean isUpload) {
        this.replace("Upload", new AttrBoolean(isUpload));
    }
    */


    @JsonIgnore
    public static boolean isReadAllGranted(BlockchainObject object, User user, AttrIDList aclIDs) {

        BlockchainObjectReader reader = BlockchainObjectReader.getInstance(user);

        for (AttrID aclID : aclIDs) {
            try {
                BlockchainIndex<String, String> currentVersionsIndex = BlockchainObjectIndex.getInstance().getIndex(BlockchainObjectIndex.INDEX_TYPE.CURRENT_VERSIONS);
                String aclUID = currentVersionsIndex.get(aclID.getUID());

                ACL acl = (ACL) reader.readWithoutCheck(aclUID);

                if (!acl.getFilter().equals("") && object != null) {
                    if (object.matchSearchFilter(acl.getFilter())) {
                        if (acl.isReadAll()) {
                            AttrIDList deniedUsers = acl.getDeniedUsers();
                            AttrIDList deniedGroups = acl.getDeniedGroups();

                            for (AttrID groupID : deniedGroups) {
                                String groupUID = currentVersionsIndex.get(groupID.getUID());
                                Group group = (Group) reader.readWithoutCheck(groupUID);
                                deniedUsers.addAll(group.getAllUserIDsFromGroup());
                            }

                            if (deniedUsers.containsUID(user.getId())) {
                                return false;
                            }
                        }
                    }
                } else {
                    if (acl.isReadAll()) {
                        AttrIDList deniedUsers = acl.getDeniedUsers();
                        AttrIDList deniedGroups = acl.getDeniedGroups();

                        for (AttrID groupID : deniedGroups) {
                            String groupUID = currentVersionsIndex.get(groupID.getUID());
                            Group group = (Group) reader.readWithoutCheck(groupUID);
                            deniedUsers.addAll(group.getAllUserIDsFromGroup());
                        }

                        if (deniedUsers.containsUID(user.getId())) {
                            return false;
                        }
                    }
                }
            } catch (ORMException e) {
                logger.error("Error reading ACL with ID: '" + aclID + "': " + e.toString());
            }
        }

        for (AttrID aclID : aclIDs) {
            try {
                BlockchainIndex<String, String> currentVersionsIndex = BlockchainObjectIndex.getInstance().getIndex(BlockchainObjectIndex.INDEX_TYPE.CURRENT_VERSIONS);
                String aclUID = currentVersionsIndex.get(aclID.getUID());

                ACL acl = (ACL) reader.readWithoutCheck(aclUID);


                if (!acl.getFilter().equals("") && object != null) {
                    if (object.matchSearchFilter(acl.getFilter())) {
                        if (acl.isReadAll()) {
                            AttrIDList grantedUsers = acl.getGrantedUsers();
                            AttrIDList grantedGroups = acl.getGrantedGroups();

                            for (AttrID groupID : grantedGroups) {
                                String groupUID = currentVersionsIndex.get(groupID.getUID());
                                Group group = (Group) reader.readWithoutCheck(groupUID);
                                grantedUsers.addAll(group.getAllUserIDsFromGroup());
                            }

                            if (grantedUsers.containsUID(user.getId()) || user.getId().equals("1.init.init")) {
                                return true;
                            }
                        }
                    }
                } else {
                    if (acl.isReadAll()) {
                        AttrIDList grantedUsers = acl.getGrantedUsers();
                        AttrIDList grantedGroups = acl.getGrantedGroups();

                        for (AttrID groupID : grantedGroups) {
                            String groupUID = currentVersionsIndex.get(groupID.getUID());
                            Group group = (Group) reader.readWithoutCheck(groupUID);
                            grantedUsers.addAll(group.getAllUserIDsFromGroup());
                        }

                        if (grantedUsers.containsUID(user.getId()) || user.getId().equals("1.init.init")) {
                            return true;
                        }
                    }

                }

            } catch (ORMException e) {
                logger.error("Error reading ACL with ID: '" + aclID + "': " + e.toString());
            }
        }

        return false;
    }


    @JsonIgnore
    public static boolean isReadOwnGranted(BlockchainObject object, User user, AttrIDList aclIDs) {

        BlockchainObjectReader reader = BlockchainObjectReader.getInstance(user);

        for (AttrID aclID : aclIDs) {
            try {
                BlockchainIndex<String, String> currentVersionsIndex = BlockchainObjectIndex.getInstance().getIndex(BlockchainObjectIndex.INDEX_TYPE.CURRENT_VERSIONS);
                String aclUID = currentVersionsIndex.get(aclID.getUID());

                ACL acl = (ACL) reader.readWithoutCheck(aclUID);

                if (!acl.getFilter().equals("") && object != null) {
                    if (object.matchSearchFilter(acl.getFilter())) {
                        if (acl.isReadOwn()) {
                            AttrIDList deniedUsers = acl.getDeniedUsers();
                            AttrIDList deniedGroups = acl.getDeniedGroups();

                            for (AttrID groupID : deniedGroups) {
                                String groupUID = currentVersionsIndex.get(groupID.getUID());
                                Group group = (Group) reader.readWithoutCheck(groupUID);
                                deniedUsers.addAll(group.getAllUserIDsFromGroup());
                            }

                            if (deniedUsers.containsUID(user.getId())) {
                                return false;
                            }
                        }
                    }
                } else {
                    if (acl.isReadOwn()) {
                        AttrIDList deniedUsers = acl.getDeniedUsers();
                        AttrIDList deniedGroups = acl.getDeniedGroups();

                        for (AttrID groupID : deniedGroups) {
                            String groupUID = currentVersionsIndex.get(groupID.getUID());
                            Group group = (Group) reader.readWithoutCheck(groupUID);
                            deniedUsers.addAll(group.getAllUserIDsFromGroup());
                        }

                        if (deniedUsers.containsUID(user.getId())) {
                            return false;
                        }
                    }
                }
            } catch (ORMException e) {
                logger.error("Error reading ACL with ID: '" + aclID + "': " + e.toString());
            }
        }

        for (AttrID aclID : aclIDs) {

            try {
                BlockchainIndex<String, String> currentVersionsIndex = BlockchainObjectIndex.getInstance().getIndex(BlockchainObjectIndex.INDEX_TYPE.CURRENT_VERSIONS);
                String aclUID = currentVersionsIndex.get(aclID.getUID());

                ACL acl = (ACL) reader.readWithoutCheck(aclUID);

                if (!acl.getFilter().equals("") && object != null) {
                    if (object.matchSearchFilter(acl.getFilter())) {
                        if (acl.isReadOwn()) {
                            AttrIDList grantedUsers = acl.getGrantedUsers();
                            AttrIDList grantedGroups = acl.getGrantedGroups();

                            for (AttrID groupID : grantedGroups) {
                                String groupUID = currentVersionsIndex.get(groupID.getUID());
                                Group group = (Group) reader.readWithoutCheck(groupUID);
                                grantedUsers.addAll(group.getAllUserIDsFromGroup());
                            }

                            if (grantedUsers.containsUID(user.getId()) || user.getId().equals("1.init.init")) {
                                return true;
                            }
                        }
                    }
                } else {
                    if (acl.isReadOwn()) {
                        AttrIDList grantedUsers = acl.getGrantedUsers();
                        AttrIDList grantedGroups = acl.getGrantedGroups();

                        for (AttrID groupID : grantedGroups) {
                            String groupUID = currentVersionsIndex.get(groupID.getUID());
                            Group group = (Group) reader.readWithoutCheck(groupUID);
                            grantedUsers.addAll(group.getAllUserIDsFromGroup());
                        }

                        if (grantedUsers.containsUID(user.getId()) || user.getId().equals("1.init.init")) {
                            return true;
                        }
                    }
                }

            } catch (ORMException e) {
                logger.error("Error reading ACL with ID: '" + aclID + "': " + e.toString());
            }
        }

        return false;
    }

    @JsonIgnore
    public static boolean isAttachmentAllGranted(BlockchainObject object, User user, AttrIDList aclIDs) {

        BlockchainObjectReader reader = BlockchainObjectReader.getInstance(user);

        for (AttrID aclID : aclIDs) {
            try {
                BlockchainIndex<String, String> currentVersionsIndex = BlockchainObjectIndex.getInstance().getIndex(BlockchainObjectIndex.INDEX_TYPE.CURRENT_VERSIONS);
                String aclUID = currentVersionsIndex.get(aclID.getUID());

                ACL acl = (ACL) reader.readWithoutCheck(aclUID);

                if (!acl.getFilter().equals("") && object != null) {
                    if (object.matchSearchFilter(acl.getFilter())) {
                        if (acl.isAttachmentAll()) {
                            AttrIDList deniedUsers = acl.getDeniedUsers();
                            AttrIDList deniedGroups = acl.getDeniedGroups();

                            for (AttrID groupID : deniedGroups) {
                                String groupUID = currentVersionsIndex.get(groupID.getUID());
                                Group group = (Group) reader.readWithoutCheck(groupUID);
                                deniedUsers.addAll(group.getAllUserIDsFromGroup());
                            }

                            if (deniedUsers.containsUID(user.getId())) {
                                return false;
                            }
                        }
                    }
                } else {
                    if (acl.isAttachmentAll()) {
                        AttrIDList deniedUsers = acl.getDeniedUsers();
                        AttrIDList deniedGroups = acl.getDeniedGroups();

                        for (AttrID groupID : deniedGroups) {
                            String groupUID = currentVersionsIndex.get(groupID.getUID());
                            Group group = (Group) reader.readWithoutCheck(groupUID);
                            deniedUsers.addAll(group.getAllUserIDsFromGroup());
                        }

                        if (deniedUsers.containsUID(user.getId())) {
                            return false;
                        }
                    }
                }
            } catch (ORMException e) {
                logger.error("Error reading ACL with ID: '" + aclID + "': " + e.toString());
            }
        }

        for (AttrID aclID : aclIDs) {
            try {
                BlockchainIndex<String, String> currentVersionsIndex = BlockchainObjectIndex.getInstance().getIndex(BlockchainObjectIndex.INDEX_TYPE.CURRENT_VERSIONS);
                String aclUID = currentVersionsIndex.get(aclID.getUID());

                ACL acl = (ACL) reader.readWithoutCheck(aclUID);


                if (!acl.getFilter().equals("") && object != null) {
                    if (object.matchSearchFilter(acl.getFilter())) {
                        if (acl.isAttachmentAll()) {
                            AttrIDList grantedUsers = acl.getGrantedUsers();
                            AttrIDList grantedGroups = acl.getGrantedGroups();

                            for (AttrID groupID : grantedGroups) {
                                String groupUID = currentVersionsIndex.get(groupID.getUID());
                                Group group = (Group) reader.readWithoutCheck(groupUID);
                                grantedUsers.addAll(group.getAllUserIDsFromGroup());
                            }

                            if (grantedUsers.containsUID(user.getId()) || user.getId().equals("1.init.init")) {
                                return true;
                            }
                        }
                    }
                } else {
                    if (acl.isAttachmentAll()) {
                        AttrIDList grantedUsers = acl.getGrantedUsers();
                        AttrIDList grantedGroups = acl.getGrantedGroups();

                        for (AttrID groupID : grantedGroups) {
                            String groupUID = currentVersionsIndex.get(groupID.getUID());
                            Group group = (Group) reader.readWithoutCheck(groupUID);
                            grantedUsers.addAll(group.getAllUserIDsFromGroup());
                        }

                        if (grantedUsers.containsUID(user.getId()) || user.getId().equals("1.init.init")) {
                            return true;
                        }
                    }

                }

            } catch (ORMException e) {
                logger.error("Error reading ACL with ID: '" + aclID + "': " + e.toString());
            }
        }

        return false;
    }


    @JsonIgnore
    public static boolean isAttachmentOwnGranted(BlockchainObject object, User user, AttrIDList aclIDs) {

        BlockchainObjectReader reader = BlockchainObjectReader.getInstance(user);

        for (AttrID aclID : aclIDs) {
            try {
                BlockchainIndex<String, String> currentVersionsIndex = BlockchainObjectIndex.getInstance().getIndex(BlockchainObjectIndex.INDEX_TYPE.CURRENT_VERSIONS);
                String aclUID = currentVersionsIndex.get(aclID.getUID());

                ACL acl = (ACL) reader.readWithoutCheck(aclUID);

                if (!acl.getFilter().equals("") && object != null) {
                    if (object.matchSearchFilter(acl.getFilter())) {
                        if (acl.isAttachmentOwn()) {
                            AttrIDList deniedUsers = acl.getDeniedUsers();
                            AttrIDList deniedGroups = acl.getDeniedGroups();

                            for (AttrID groupID : deniedGroups) {
                                String groupUID = currentVersionsIndex.get(groupID.getUID());
                                Group group = (Group) reader.readWithoutCheck(groupUID);
                                deniedUsers.addAll(group.getAllUserIDsFromGroup());
                            }

                            if (deniedUsers.containsUID(user.getId())) {
                                return false;
                            }
                        }
                    }
                } else {
                    if (acl.isAttachmentOwn()) {
                        AttrIDList deniedUsers = acl.getDeniedUsers();
                        AttrIDList deniedGroups = acl.getDeniedGroups();

                        for (AttrID groupID : deniedGroups) {
                            String groupUID = currentVersionsIndex.get(groupID.getUID());
                            Group group = (Group) reader.readWithoutCheck(groupUID);
                            deniedUsers.addAll(group.getAllUserIDsFromGroup());
                        }

                        if (deniedUsers.containsUID(user.getId())) {
                            return false;
                        }
                    }
                }
            } catch (ORMException e) {
                logger.error("Error reading ACL with ID: '" + aclID + "': " + e.toString());
            }
        }

        for (AttrID aclID : aclIDs) {

            try {
                BlockchainIndex<String, String> currentVersionsIndex = BlockchainObjectIndex.getInstance().getIndex(BlockchainObjectIndex.INDEX_TYPE.CURRENT_VERSIONS);
                String aclUID = currentVersionsIndex.get(aclID.getUID());

                ACL acl = (ACL) reader.readWithoutCheck(aclUID);

                if (!acl.getFilter().equals("") && object != null) {
                    if (object.matchSearchFilter(acl.getFilter())) {
                        if (acl.isAttachmentOwn()) {
                            AttrIDList grantedUsers = acl.getGrantedUsers();
                            AttrIDList grantedGroups = acl.getGrantedGroups();

                            for (AttrID groupID : grantedGroups) {
                                String groupUID = currentVersionsIndex.get(groupID.getUID());
                                Group group = (Group) reader.readWithoutCheck(groupUID);
                                grantedUsers.addAll(group.getAllUserIDsFromGroup());
                            }

                            if (grantedUsers.containsUID(user.getId()) || user.getId().equals("1.init.init")) {
                                return true;
                            }
                        }
                    }
                } else {
                    if (acl.isAttachmentOwn()) {
                        AttrIDList grantedUsers = acl.getGrantedUsers();
                        AttrIDList grantedGroups = acl.getGrantedGroups();

                        for (AttrID groupID : grantedGroups) {
                            String groupUID = currentVersionsIndex.get(groupID.getUID());
                            Group group = (Group) reader.readWithoutCheck(groupUID);
                            grantedUsers.addAll(group.getAllUserIDsFromGroup());
                        }

                        if (grantedUsers.containsUID(user.getId()) || user.getId().equals("1.init.init")) {
                            return true;
                        }
                    }
                }

            } catch (ORMException e) {
                logger.error("Error reading ACL with ID: '" + aclID + "': " + e.toString());
            }
        }

        return false;
    }


    @JsonIgnore
    public static boolean isCreateGranted(BlockchainObject object, User user, AttrIDList aclIDs) {

        BlockchainObjectReader reader = BlockchainObjectReader.getInstance(user);

        for (AttrID aclID : aclIDs) {
            try {
                BlockchainIndex<String, String> currentVersionsIndex = BlockchainObjectIndex.getInstance().getIndex(BlockchainObjectIndex.INDEX_TYPE.CURRENT_VERSIONS);
                String aclUID = currentVersionsIndex.get(aclID.getUID());

                ACL acl = (ACL) reader.readWithoutCheck(aclUID);

                if (!acl.getFilter().equals("") && object != null) {
                    if (object.matchSearchFilter(acl.getFilter())) {
                        if (acl.isCreate()) {
                            AttrIDList deniedUsers = acl.getDeniedUsers();
                            AttrIDList deniedGroups = acl.getDeniedGroups();

                            for (AttrID groupID : deniedGroups) {
                                String groupUID = currentVersionsIndex.get(groupID.getUID());
                                Group group = (Group) reader.readWithoutCheck(groupUID);
                                deniedUsers.addAll(group.getAllUserIDsFromGroup());
                            }

                            if (deniedUsers.containsUID(user.getId())) {
                                return false;
                            }
                        }
                    }
                } else {
                    if (acl.isCreate()) {
                        AttrIDList deniedUsers = acl.getDeniedUsers();
                        AttrIDList deniedGroups = acl.getDeniedGroups();

                        for (AttrID groupID : deniedGroups) {
                            String groupUID = currentVersionsIndex.get(groupID.getUID());
                            Group group = (Group) reader.readWithoutCheck(groupUID);
                            deniedUsers.addAll(group.getAllUserIDsFromGroup());
                        }

                        if (deniedUsers.containsUID(user.getId())) {
                            return false;
                        }
                    }
                }
            } catch (ORMException e) {
                logger.error("Error reading ACL with ID: '" + aclID + "': " + e.toString());
            }
        }

        for (AttrID aclID : aclIDs) {
            try {
                BlockchainIndex<String, String> currentVersionsIndex = BlockchainObjectIndex.getInstance().getIndex(BlockchainObjectIndex.INDEX_TYPE.CURRENT_VERSIONS);
                String aclUID = currentVersionsIndex.get(aclID.getUID());

                ACL acl = (ACL) reader.readWithoutCheck(aclUID);


                if (!acl.getFilter().equals("") && object != null) {
                    if (object.matchSearchFilter(acl.getFilter())) {
                        if (acl.isCreate()) {
                            AttrIDList grantedUsers = acl.getGrantedUsers();
                            AttrIDList grantedGroups = acl.getGrantedGroups();

                            for (AttrID groupID : grantedGroups) {
                                String groupUID = currentVersionsIndex.get(groupID.getUID());
                                Group group = (Group) reader.readWithoutCheck(groupUID);
                                grantedUsers.addAll(group.getAllUserIDsFromGroup());
                            }

                            if (grantedUsers.containsUID(user.getId()) || user.getId().equals("1.init.init")) {
                                return true;
                            }
                        }
                    }
                } else {
                    if (acl.isCreate()) {
                        AttrIDList grantedUsers = acl.getGrantedUsers();
                        AttrIDList grantedGroups = acl.getGrantedGroups();

                        for (AttrID groupID : grantedGroups) {
                            String groupUID = currentVersionsIndex.get(groupID.getUID());
                            Group group = (Group) reader.readWithoutCheck(groupUID);
                            grantedUsers.addAll(group.getAllUserIDsFromGroup());
                        }

                        if (grantedUsers.containsUID(user.getId()) || user.getId().equals("1.init.init")) {
                            return true;
                        }
                    }

                }

            } catch (ORMException e) {
                logger.error("Error reading ACL with ID: '" + aclID + "': " + e.toString());
            }
        }

        return false;
    }

    @JsonIgnore
    public static boolean isDeleteAllGranted(BlockchainObject object, User user, AttrIDList aclIDs) {

        BlockchainObjectReader reader = BlockchainObjectReader.getInstance(user);

        for (AttrID aclID : aclIDs) {
            try {
                BlockchainIndex<String, String> currentVersionsIndex = BlockchainObjectIndex.getInstance().getIndex(BlockchainObjectIndex.INDEX_TYPE.CURRENT_VERSIONS);
                String aclUID = currentVersionsIndex.get(aclID.getUID());

                ACL acl = (ACL) reader.readWithoutCheck(aclUID);

                if (!acl.getFilter().equals("") && object != null) {
                    if (object.matchSearchFilter(acl.getFilter())) {
                        if (acl.isDeleteAll()) {
                            AttrIDList deniedUsers = acl.getDeniedUsers();
                            AttrIDList deniedGroups = acl.getDeniedGroups();

                            for (AttrID groupID : deniedGroups) {
                                String groupUID = currentVersionsIndex.get(groupID.getUID());
                                Group group = (Group) reader.readWithoutCheck(groupUID);
                                deniedUsers.addAll(group.getAllUserIDsFromGroup());
                            }

                            if (deniedUsers.containsUID(user.getId())) {
                                return false;
                            }
                        }
                    }
                } else {
                    if (acl.isDeleteAll()) {
                        AttrIDList deniedUsers = acl.getDeniedUsers();
                        AttrIDList deniedGroups = acl.getDeniedGroups();

                        for (AttrID groupID : deniedGroups) {
                            String groupUID = currentVersionsIndex.get(groupID.getUID());
                            Group group = (Group) reader.readWithoutCheck(groupUID);
                            deniedUsers.addAll(group.getAllUserIDsFromGroup());
                        }

                        if (deniedUsers.containsUID(user.getId())) {
                            return false;
                        }
                    }
                }
            } catch (ORMException e) {
                logger.error("Error reading ACL with ID: '" + aclID + "': " + e.toString());
            }
        }

        for (AttrID aclID : aclIDs) {
            try {
                BlockchainIndex<String, String> currentVersionsIndex = BlockchainObjectIndex.getInstance().getIndex(BlockchainObjectIndex.INDEX_TYPE.CURRENT_VERSIONS);
                String aclUID = currentVersionsIndex.get(aclID.getUID());

                ACL acl = (ACL) reader.readWithoutCheck(aclUID);


                if (!acl.getFilter().equals("") && object != null) {
                    if (object.matchSearchFilter(acl.getFilter())) {
                        if (acl.isDeleteAll()) {
                            AttrIDList grantedUsers = acl.getGrantedUsers();
                            AttrIDList grantedGroups = acl.getGrantedGroups();

                            for (AttrID groupID : grantedGroups) {
                                String groupUID = currentVersionsIndex.get(groupID.getUID());
                                Group group = (Group) reader.readWithoutCheck(groupUID);
                                grantedUsers.addAll(group.getAllUserIDsFromGroup());
                            }

                            if (grantedUsers.containsUID(user.getId()) || user.getId().equals("1.init.init")) {
                                return true;
                            }
                        }
                    }
                } else {
                    if (acl.isDeleteAll()) {
                        AttrIDList grantedUsers = acl.getGrantedUsers();
                        AttrIDList grantedGroups = acl.getGrantedGroups();

                        for (AttrID groupID : grantedGroups) {
                            String groupUID = currentVersionsIndex.get(groupID.getUID());
                            Group group = (Group) reader.readWithoutCheck(groupUID);
                            grantedUsers.addAll(group.getAllUserIDsFromGroup());
                        }

                        if (grantedUsers.containsUID(user.getId()) || user.getId().equals("1.init.init")) {
                            return true;
                        }
                    }

                }

            } catch (ORMException e) {
                logger.error("Error reading ACL with ID: '" + aclID + "': " + e.toString());
            }
        }

        return false;
    }


    @JsonIgnore
    public static boolean isDeleteOwnGranted(BlockchainObject object, User user, AttrIDList aclIDs) {

        BlockchainObjectReader reader = BlockchainObjectReader.getInstance(user);

        for (AttrID aclID : aclIDs) {
            try {
                BlockchainIndex<String, String> currentVersionsIndex = BlockchainObjectIndex.getInstance().getIndex(BlockchainObjectIndex.INDEX_TYPE.CURRENT_VERSIONS);
                String aclUID = currentVersionsIndex.get(aclID.getUID());

                ACL acl = (ACL) reader.readWithoutCheck(aclUID);

                if (!acl.getFilter().equals("") && object != null) {
                    if (object.matchSearchFilter(acl.getFilter())) {
                        if (acl.isDeleteOwn()) {
                            AttrIDList deniedUsers = acl.getDeniedUsers();
                            AttrIDList deniedGroups = acl.getDeniedGroups();

                            for (AttrID groupID : deniedGroups) {
                                String groupUID = currentVersionsIndex.get(groupID.getUID());
                                Group group = (Group) reader.readWithoutCheck(groupUID);
                                deniedUsers.addAll(group.getAllUserIDsFromGroup());
                            }

                            if (deniedUsers.containsUID(user.getId())) {
                                return false;
                            }
                        }
                    }
                } else {
                    if (acl.isDeleteOwn()) {
                        AttrIDList deniedUsers = acl.getDeniedUsers();
                        AttrIDList deniedGroups = acl.getDeniedGroups();

                        for (AttrID groupID : deniedGroups) {
                            String groupUID = currentVersionsIndex.get(groupID.getUID());
                            Group group = (Group) reader.readWithoutCheck(groupUID);
                            deniedUsers.addAll(group.getAllUserIDsFromGroup());
                        }

                        if (deniedUsers.containsUID(user.getId())) {
                            return false;
                        }
                    }
                }
            } catch (ORMException e) {
                logger.error("Error reading ACL with ID: '" + aclID + "': " + e.toString());
            }
        }

        for (AttrID aclID : aclIDs) {

            try {
                BlockchainIndex<String, String> currentVersionsIndex = BlockchainObjectIndex.getInstance().getIndex(BlockchainObjectIndex.INDEX_TYPE.CURRENT_VERSIONS);
                String aclUID = currentVersionsIndex.get(aclID.getUID());

                ACL acl = (ACL) reader.readWithoutCheck(aclUID);

                if (!acl.getFilter().equals("") && object != null) {
                    if (object.matchSearchFilter(acl.getFilter())) {
                        if (acl.isDeleteOwn()) {
                            AttrIDList grantedUsers = acl.getGrantedUsers();
                            AttrIDList grantedGroups = acl.getGrantedGroups();

                            for (AttrID groupID : grantedGroups) {
                                String groupUID = currentVersionsIndex.get(groupID.getUID());
                                Group group = (Group) reader.readWithoutCheck(groupUID);
                                grantedUsers.addAll(group.getAllUserIDsFromGroup());
                            }

                            if (grantedUsers.containsUID(user.getId()) || user.getId().equals("1.init.init")) {
                                return true;
                            }
                        }
                    }
                } else {
                    if (acl.isDeleteOwn()) {
                        AttrIDList grantedUsers = acl.getGrantedUsers();
                        AttrIDList grantedGroups = acl.getGrantedGroups();

                        for (AttrID groupID : grantedGroups) {
                            String groupUID = currentVersionsIndex.get(groupID.getUID());
                            Group group = (Group) reader.readWithoutCheck(groupUID);
                            grantedUsers.addAll(group.getAllUserIDsFromGroup());
                        }

                        if (grantedUsers.containsUID(user.getId()) || user.getId().equals("1.init.init")) {
                            return true;
                        }
                    }
                }

            } catch (ORMException e) {
                logger.error("Error reading ACL with ID: '" + aclID + "': " + e.toString());
            }
        }

        return false;
    }

    @JsonIgnore
    public static boolean isWriteAllGranted(BlockchainObject object, User user, AttrIDList aclIDs) {

        BlockchainObjectReader reader = BlockchainObjectReader.getInstance(user);

        for (AttrID aclID : aclIDs) {
            try {
                BlockchainIndex<String, String> currentVersionsIndex = BlockchainObjectIndex.getInstance().getIndex(BlockchainObjectIndex.INDEX_TYPE.CURRENT_VERSIONS);
                String aclUID = currentVersionsIndex.get(aclID.getUID());

                ACL acl = (ACL) reader.readWithoutCheck(aclUID);

                if (!acl.getFilter().equals("") && object != null) {
                    if (object.matchSearchFilter(acl.getFilter())) {
                        if (acl.isWriteAll()) {
                            AttrIDList deniedUsers = acl.getDeniedUsers();
                            AttrIDList deniedGroups = acl.getDeniedGroups();

                            for (AttrID groupID : deniedGroups) {
                                String groupUID = currentVersionsIndex.get(groupID.getUID());
                                Group group = (Group) reader.readWithoutCheck(groupUID);
                                deniedUsers.addAll(group.getAllUserIDsFromGroup());
                            }

                            if (deniedUsers.containsUID(user.getId())) {
                                return false;
                            }
                        }
                    }
                } else {
                    if (acl.isWriteAll()) {
                        AttrIDList deniedUsers = acl.getDeniedUsers();
                        AttrIDList deniedGroups = acl.getDeniedGroups();

                        for (AttrID groupID : deniedGroups) {
                            String groupUID = currentVersionsIndex.get(groupID.getUID());
                            Group group = (Group) reader.readWithoutCheck(groupUID);
                            deniedUsers.addAll(group.getAllUserIDsFromGroup());
                        }

                        if (deniedUsers.containsUID(user.getId())) {
                            return false;
                        }
                    }
                }
            } catch (ORMException e) {
                logger.error("Error reading ACL with ID: '" + aclID + "': " + e.toString());
            }
        }

        for (AttrID aclID : aclIDs) {
            try {
                BlockchainIndex<String, String> currentVersionsIndex = BlockchainObjectIndex.getInstance().getIndex(BlockchainObjectIndex.INDEX_TYPE.CURRENT_VERSIONS);
                String aclUID = currentVersionsIndex.get(aclID.getUID());

                ACL acl = (ACL) reader.readWithoutCheck(aclUID);


                if (!acl.getFilter().equals("") && object != null) {
                    if (object.matchSearchFilter(acl.getFilter())) {
                        if (acl.isWriteAll()) {
                            AttrIDList grantedUsers = acl.getGrantedUsers();
                            AttrIDList grantedGroups = acl.getGrantedGroups();

                            for (AttrID groupID : grantedGroups) {
                                String groupUID = currentVersionsIndex.get(groupID.getUID());
                                Group group = (Group) reader.readWithoutCheck(groupUID);
                                grantedUsers.addAll(group.getAllUserIDsFromGroup());
                            }

                            if (grantedUsers.containsUID(user.getId()) || user.getId().equals("1.init.init")) {
                                return true;
                            }
                        }
                    }
                } else {
                    if (acl.isWriteAll()) {
                        AttrIDList grantedUsers = acl.getGrantedUsers();
                        AttrIDList grantedGroups = acl.getGrantedGroups();

                        for (AttrID groupID : grantedGroups) {
                            String groupUID = currentVersionsIndex.get(groupID.getUID());
                            Group group = (Group) reader.readWithoutCheck(groupUID);
                            grantedUsers.addAll(group.getAllUserIDsFromGroup());
                        }

                        if (grantedUsers.containsUID(user.getId()) || user.getId().equals("1.init.init")) {
                            return true;
                        }
                    }

                }

            } catch (ORMException e) {
                logger.error("Error reading ACL with ID: '" + aclID + "': " + e.toString());
            }
        }

        return false;
    }


    @JsonIgnore
    public static boolean isWriteOwnGranted(BlockchainObject object, User user, AttrIDList aclIDs) {

        BlockchainObjectReader reader = BlockchainObjectReader.getInstance(user);

        for (AttrID aclID : aclIDs) {
            try {
                BlockchainIndex<String, String> currentVersionsIndex = BlockchainObjectIndex.getInstance().getIndex(BlockchainObjectIndex.INDEX_TYPE.CURRENT_VERSIONS);
                String aclUID = currentVersionsIndex.get(aclID.getUID());

                ACL acl = (ACL) reader.readWithoutCheck(aclUID);

                if (!acl.getFilter().equals("") && object != null) {
                    if (object.matchSearchFilter(acl.getFilter())) {
                        if (acl.isWriteOwn()) {
                            AttrIDList deniedUsers = acl.getDeniedUsers();
                            AttrIDList deniedGroups = acl.getDeniedGroups();

                            for (AttrID groupID : deniedGroups) {
                                String groupUID = currentVersionsIndex.get(groupID.getUID());
                                Group group = (Group) reader.readWithoutCheck(groupUID);
                                deniedUsers.addAll(group.getAllUserIDsFromGroup());
                            }

                            if (deniedUsers.containsUID(user.getId())) {
                                return false;
                            }
                        }
                    }
                } else {
                    if (acl.isWriteOwn()) {
                        AttrIDList deniedUsers = acl.getDeniedUsers();
                        AttrIDList deniedGroups = acl.getDeniedGroups();

                        for (AttrID groupID : deniedGroups) {
                            String groupUID = currentVersionsIndex.get(groupID.getUID());
                            Group group = (Group) reader.readWithoutCheck(groupUID);
                            deniedUsers.addAll(group.getAllUserIDsFromGroup());
                        }

                        if (deniedUsers.containsUID(user.getId())) {
                            return false;
                        }
                    }
                }
            } catch (ORMException e) {
                logger.error("Error reading ACL with ID: '" + aclID + "': " + e.toString());
            }
        }

        for (AttrID aclID : aclIDs) {

            try {
                BlockchainIndex<String, String> currentVersionsIndex = BlockchainObjectIndex.getInstance().getIndex(BlockchainObjectIndex.INDEX_TYPE.CURRENT_VERSIONS);
                String aclUID = currentVersionsIndex.get(aclID.getUID());

                ACL acl = (ACL) reader.readWithoutCheck(aclUID);

                if (!acl.getFilter().equals("") && object != null) {
                    if (object.matchSearchFilter(acl.getFilter())) {
                        if (acl.isWriteOwn()) {
                            AttrIDList grantedUsers = acl.getGrantedUsers();
                            AttrIDList grantedGroups = acl.getGrantedGroups();

                            for (AttrID groupID : grantedGroups) {
                                String groupUID = currentVersionsIndex.get(groupID.getUID());
                                Group group = (Group) reader.readWithoutCheck(groupUID);
                                grantedUsers.addAll(group.getAllUserIDsFromGroup());
                            }

                            if (grantedUsers.containsUID(user.getId()) || user.getId().equals("1.init.init")) {
                                return true;
                            }
                        }
                    }
                } else {
                    if (acl.isWriteOwn()) {
                        AttrIDList grantedUsers = acl.getGrantedUsers();
                        AttrIDList grantedGroups = acl.getGrantedGroups();

                        for (AttrID groupID : grantedGroups) {
                            String groupUID = currentVersionsIndex.get(groupID.getUID());
                            Group group = (Group) reader.readWithoutCheck(groupUID);
                            grantedUsers.addAll(group.getAllUserIDsFromGroup());
                        }

                        if (grantedUsers.containsUID(user.getId()) || user.getId().equals("1.init.init")) {
                            return true;
                        }
                    }
                }

            } catch (ORMException e) {
                logger.error("Error reading ACL with ID: '" + aclID + "': " + e.toString());
            }
        }

        return false;
    }

    /*
    @JsonIgnore
    public static boolean isUploadGranted(BlockchainObject object, User user, AttrIDList aclIDs) {

        BlockchainObjectReader reader = BlockchainObjectReader.getInstance(user);

        for (AttrID aclID : aclIDs) {
            try {
                BlockchainIndex<String, String> currentVersionsIndex = BlockchainObjectIndex.getInstance().getIndex(BlockchainObjectIndex.INDEX_TYPE.CURRENT_VERSIONS);
                String aclUID = currentVersionsIndex.get(aclID.getUID());

                ACL acl = (ACL) reader.readWithoutCheck(aclUID);

                if (!acl.getFilter().equals("") && object != null) {
                    if (object.matchSearchFilter(acl.getFilter())) {
                        if (acl.isUpload()) {
                            AttrIDList deniedUsers = acl.getDeniedUsers();
                            AttrIDList deniedGroups = acl.getDeniedGroups();

                            for (AttrID groupID : deniedGroups) {
                                String groupUID = currentVersionsIndex.get(groupID.getUID());
                                Group group = (Group) reader.readWithoutCheck(groupUID);
                                deniedUsers.addAll(group.getAllUserIDsFromGroup());
                            }

                            if (deniedUsers.containsUID(user.getId())) {
                                return false;
                            }
                        }
                    }
                } else {
                    if (acl.isUpload()) {
                        AttrIDList deniedUsers = acl.getDeniedUsers();
                        AttrIDList deniedGroups = acl.getDeniedGroups();

                        for (AttrID groupID : deniedGroups) {
                            String groupUID = currentVersionsIndex.get(groupID.getUID());
                            Group group = (Group) reader.readWithoutCheck(groupUID);
                            deniedUsers.addAll(group.getAllUserIDsFromGroup());
                        }

                        if (deniedUsers.containsUID(user.getId())) {
                            return false;
                        }
                    }
                }
            } catch (ORMException e) {
                logger.error("Error reading ACL with ID: '" + aclID + "': " + e.toString());
            }
        }

        for (AttrID aclID : aclIDs) {
            try {
                BlockchainIndex<String, String> currentVersionsIndex = BlockchainObjectIndex.getInstance().getIndex(BlockchainObjectIndex.INDEX_TYPE.CURRENT_VERSIONS);
                String aclUID = currentVersionsIndex.get(aclID.getUID());

                ACL acl = (ACL) reader.readWithoutCheck(aclUID);


                if (!acl.getFilter().equals("") && object != null) {
                    if (object.matchSearchFilter(acl.getFilter())) {
                        if (acl.isUpload()) {
                            AttrIDList grantedUsers = acl.getGrantedUsers();
                            AttrIDList grantedGroups = acl.getGrantedGroups();

                            for (AttrID groupID : grantedGroups) {
                                String groupUID = currentVersionsIndex.get(groupID.getUID());
                                Group group = (Group) reader.readWithoutCheck(groupUID);
                                grantedUsers.addAll(group.getAllUserIDsFromGroup());
                            }

                            if (grantedUsers.containsUID(user.getId()) || user.getId().equals("1.init.init")) {
                                return true;
                            }
                        }
                    }
                } else {
                    if (acl.isUpload()) {
                        AttrIDList grantedUsers = acl.getGrantedUsers();
                        AttrIDList grantedGroups = acl.getGrantedGroups();

                        for (AttrID groupID : grantedGroups) {
                            String groupUID = currentVersionsIndex.get(groupID.getUID());
                            Group group = (Group) reader.readWithoutCheck(groupUID);
                            grantedUsers.addAll(group.getAllUserIDsFromGroup());
                        }

                        if (grantedUsers.containsUID(user.getId()) || user.getId().equals("1.init.init")) {
                            return true;
                        }
                    }

                }

            } catch (ORMException e) {
                logger.error("Error reading ACL with ID: '" + aclID + "': " + e.toString());
            }
        }

        return false;
    }
    */

}
