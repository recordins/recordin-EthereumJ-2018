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
import com.recordins.recordin.orm.action.ActionDefinition;
import com.recordins.recordin.orm.attribute.AttrID;
import com.recordins.recordin.orm.attribute.AttrIDList;
import com.recordins.recordin.orm.attribute.AttrString;
import com.recordins.recordin.orm.exception.ORMException;
import com.recordins.recordin.orm.core.BlockchainObjectReader;
import com.recordins.recordin.utils.DeepCopy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class Group extends BlockchainObject {

    /* Logger for console output */
    private static Logger logger = LoggerFactory.getLogger(Group.class);

    public Group() throws ORMException {
        super();

        actionList.clear();
        this.actionList.add(new ActionDefinition("Archive", "Archive", "Execute", "{}"));
        this.actionList.add(new ActionDefinition("UnArchive", "UnArchive", "Execute", "{}"));
        this.actionList.add(new ActionDefinition("Delete", "Delete", "ExecuteConfirm", "{\"Message\":\"Would you like to delete selected object(s) ?\"}"));
        this.actionList.add(new ActionDefinition("ChangeOwner", "Change Owner", "SelectSingle", "{\"model\":\"User\"}"));
        this.actionList.add(new ActionDefinition("SetACL", "Set ACL", "SelectMulti", "{\"model\":\"ACL\"}"));

        setModel(this.getClass().getSimpleName());
    }

    public Group(String name) throws ORMException {
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
    public boolean containsUser(AttrID userID) {
        AttrIDList users = (AttrIDList) this.get("Users");
        return users.containsUID(userID);
    }

    @JsonIgnore
    public boolean containsUser(User user) {
        return containsUser(user.getId());
    }

    @JsonIgnore
    public void addUser(AttrID userID) {
        if (!containsUser(userID)) {
            ((AttrIDList) this.get("Users")).add(userID);
        }
    }

    @JsonIgnore
    public void addUser(User user) {
        addUser(user.getId());
    }

    @JsonIgnore
    public void removeUser(AttrID userID) {
        ((AttrIDList) this.get("Users")).removeUID(userID);
    }

    @JsonIgnore
    public void removeUser(User user) {
        removeUser(user.getId());
    }

    @JsonIgnore
    public void setUsers(List<AttrID> users) {
        this.replace("Users", new AttrIDList(users));
    }

    @JsonIgnore
    public void setUsers(AttrID... userIDs) {
        this.replace("Users", new AttrIDList(userIDs));
    }

    @JsonIgnore
    public AttrIDList getUsers() {
        return (AttrIDList) this.get("Users");
    }

    @JsonIgnore
    public boolean containsGroup(AttrID groupID) {
        AttrIDList groups = (AttrIDList) this.get("Groups");
        return groups.containsUID(groupID);
    }

    @JsonIgnore
    public boolean containsGroup(Group group) {
        return containsGroup(group.getId());
    }

    @JsonIgnore
    public void addGroup(AttrID groupID) {
        if (!containsGroup(groupID)) {
            ((AttrIDList) this.get("Groups")).add(groupID);
        }
    }

    @JsonIgnore
    public void addGroup(Group group) {
        addGroup(group.getId());
    }

    @JsonIgnore
    public void removeGroup(AttrID groupID) {
        ((AttrIDList) this.get("Groups")).removeUID(groupID);
    }

    @JsonIgnore
    public void removeGroup(Group group) {
        removeGroup(group.getId());
    }

    @JsonIgnore
    public void setGroups(List<AttrID> groups) {
        this.replace("Groups", new AttrIDList(groups));
    }

    @JsonIgnore
    public void setGroups(AttrID... groupIDs) {
        this.replace("Groups", new AttrIDList(groupIDs));
    }

    @JsonIgnore
    public AttrIDList getGroups() {
        return (AttrIDList) this.get("Groups");
    }

    @JsonIgnore
    public AttrIDList getAllUserIDsFromGroup() {

        AttrIDList result = (AttrIDList) DeepCopy.copy(this.getUsers());
        AttrIDList groupsInGroup = this.getGroups();


        for (AttrID groupID : groupsInGroup) {
            try {
                BlockchainObjectReader reader = BlockchainObjectReader.getAdminInstance();
                Group group = (Group) reader.read(groupID);
                result.addAll(group.getAllUserIDsFromGroup());
            } catch (Exception e) {
                logger.error("Error reading groups inside group '" + this.getName() + "'");
            }
        }

        return result;
    }
}
