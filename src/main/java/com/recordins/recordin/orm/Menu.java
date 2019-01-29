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
import com.recordins.recordin.orm.attribute.AttrBoolean;
import com.recordins.recordin.orm.attribute.AttrID;
import com.recordins.recordin.orm.attribute.AttrIDList;
import com.recordins.recordin.orm.attribute.AttrString;
import com.recordins.recordin.orm.exception.ORMException;
import com.recordins.recordin.orm.core.BlockchainObjectReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class Menu extends BlockchainObject {

    /* Logger for console output */
    private static Logger logger = LoggerFactory.getLogger(Menu.class);

    public Menu() throws ORMException {
        super();

        actionList.clear();

        // Name of action Class, Action display string, Action display beahviour, Action args
        this.actionList.add(new ActionDefinition("Archive", "Archive", "Execute", "{}"));
        this.actionList.add(new ActionDefinition("UnArchive", "UnArchive", "Execute", "{}"));
        this.actionList.add(new ActionDefinition("Delete", "Delete", "ExecuteConfirm", "{\"Message\":\"Would you like to delete selected object(s) ?\"}"));

        setModel(this.getClass().getSimpleName());
    }


    public Menu(String name) throws ORMException {
        this();

        setName(name);
    }

    @Override
    public void create(User user) {
        logger.trace("START Menu create()");

        String name = this.getDisplayName();
        String position = null;

        if (this.get("Position") != null) {
            position = this.get("Position").toString();
        }

        if (position == null) {
            position = "";
        }

        if (!position.endsWith(name)) {
            if (position.endsWith("/")) {
                position += name;
            } else {
                if (position.equals("")) {
                    position = name;
                } else {
                    position += "/" + name;
                }
            }
        }

        this.replace("Position", new AttrString(position));

        logger.trace("END Menu create()");
    }

    @Override
    public void write(User user) {
        logger.trace("START Menu write()");

        String name = this.getDisplayName();
        String position = null;

        if (this.get("Position") != null) {
            position = this.get("Position").toString();
        }

        if (position == null) {
            position = "";
        }

        if (!position.endsWith(name)) {
            if (position.endsWith("/")) {
                position += name;
            } else {
                if (position.equals("")) {
                    position = name;
                } else {
                    position += "/" + name;
                }
            }
        }

        this.replace("Position", new AttrString(position));

        logger.trace("END Menu write()");
    }

    @JsonIgnore
    public String getMenuModelName() throws ORMException {

        AttrID modelID = getMenuModel();

        logger.trace("Menu: " + this.get("Position") + ": modelID: " + modelID);

        Model model = (Model) BlockchainObjectReader.getAdminInstance().read(modelID);

        return model.getDisplayName();
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
    public void setMenuModel(AttrID modelID) {
        this.replace("Model", modelID);
    }

    @JsonIgnore
    public AttrID getMenuModel() {
        return (AttrID) this.get("Model");
    }

    @JsonIgnore
    public void setPosition(String position) {
        this.replace("Position", new AttrString(position));
    }

    @JsonIgnore
    public String getPosition() {
        return this.get("Position").toString();
    }

    @JsonIgnore
    public void setSearchFilter(String searchFilter) {
        this.replace("Search Filter", new AttrString(searchFilter));
    }

    @JsonIgnore
    public String getSearchFilter() {
        return this.get("Search Filter").toString();
    }

    @JsonIgnore
    public void setDomainFilter(String domainFilter) {
        this.replace("Domain Filter", new AttrString(domainFilter));
    }

    @JsonIgnore
    public String getDomainFilter() {
        return this.get("Domain Filter").toString();
    }

    @JsonIgnore
    public void setFormUI(String formUI) {
        this.replace("Form UI", new AttrString(formUI));
    }

    @JsonIgnore
    public String getFormUI() {
        return this.get("Form UI").toString();
    }

    @JsonIgnore
    public boolean containsMenuACL(AttrID aclID) {
        AttrIDList ACLs = (AttrIDList) this.get("ACL");
        return ACLs.containsUID(aclID);
    }

    @JsonIgnore
    public boolean containsMenuACL(ACL acl) {
        return containsMenuACL(acl.getId());
    }

    @JsonIgnore
    public void addMenuACL(AttrID aclID) {
        if (!containsMenuACL(aclID)) {
            ((AttrIDList) this.get("ACL")).add(aclID);
        }
    }

    @JsonIgnore
    public void addMenuACL(ACL acl) {
        addMenuACL(acl.getId());
    }

    @JsonIgnore
    public void removeMenuACL(AttrID aclID) {
        ((AttrIDList) this.get("ACL")).removeUID(aclID);
    }

    @JsonIgnore
    public void removeMenuACL(ACL acl) {
        removeMenuACL(acl.getId());
    }

    @JsonIgnore
    public void setMenuACLs(List<AttrID> acls) {
        this.replace("ACL", new AttrIDList(acls));
    }

    @JsonIgnore
    public void setMenuACLs(AttrID... aclIDs) {
        this.replace("ACL", new AttrIDList(aclIDs));
    }

    @JsonIgnore
    public AttrIDList getMenuACLs() {
        return (AttrIDList) this.get("ACL");
    }

    @JsonIgnore
    public boolean isVisible() {
        boolean result = false;
        if (this.get("Visible") != null) {
            result = ((AttrBoolean) this.get("Visible")).isBoolValue();
        }
        return result;
    }

    @JsonIgnore
    public void setVisible(boolean isVisible) {
        this.replace("Visible", new AttrBoolean(isVisible));
    }

    @JsonIgnore
    public boolean isViewList() {
        boolean result = false;
        if (this.get("View List") != null) {
            result = ((AttrBoolean) this.get("View List")).isBoolValue();
        }
        return result;
    }

    @JsonIgnore
    public void setViewList(boolean isViewList) {
        this.replace("View List", new AttrBoolean(isViewList));
    }

    @JsonIgnore
    public boolean isViewForm() {
        boolean result = false;
        if (this.get("View Form") != null) {
            result = ((AttrBoolean) this.get("View Form")).isBoolValue();
        }
        return result;
    }

    @JsonIgnore
    public void setViewForm(boolean isViewForm) {
        this.replace("View Form", new AttrBoolean(isViewForm));
    }

    @JsonIgnore
    public boolean isViewTree() {
        boolean result = false;
        if (this.get("View Tree") != null) {
            result = ((AttrBoolean) this.get("View Tree")).isBoolValue();
        }
        return result;
    }

    @JsonIgnore
    public void setViewTree(boolean isViewTree) {
        this.replace("View Tree", new AttrBoolean(isViewTree));
    }

    @JsonIgnore
    public boolean isViewKanban() {
        boolean result = false;
        if (this.get("View Kanban") != null) {
            result = ((AttrBoolean) this.get("View Kanban")).isBoolValue();
        }
        return result;
    }

    @JsonIgnore
    public void setViewKanban(boolean isViewKanban) {
        this.replace("View Kanban", new AttrBoolean(isViewKanban));
    }

    @JsonIgnore
    public boolean isOwn() {
        boolean result = false;
        if (this.get("Own") != null) {
            result = ((AttrBoolean) this.get("Own")).isBoolValue();
        }
        return result;
    }

    @JsonIgnore
    public void setOwn(boolean isOwn) {
        this.replace("Own", new AttrBoolean(isOwn));
    }
}
