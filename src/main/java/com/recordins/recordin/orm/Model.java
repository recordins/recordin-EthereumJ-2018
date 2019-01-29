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
import com.recordins.recordin.orm.attribute.*;
import com.recordins.recordin.orm.exception.ORMException;
import com.recordins.recordin.orm.core.BlockchainObjectReader;
import com.recordins.recordin.orm.core.BlockchainObjectReader.SearchResult;

import java.util.List;

import com.recordins.recordin.Main;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Model extends BlockchainObject {

    /* Logger for console output */
    private static Logger logger = LoggerFactory.getLogger(Model.class);

    public Model() throws ORMException {
        super();
        setJavaClass("BlockchainObject");
        setImmutable(false);

        actionList.clear();

        this.actionList.add(new ActionDefinition("Archive", "Archive", "Execute", "{}"));
        this.actionList.add(new ActionDefinition("UnArchive", "UnArchive", "Execute", "{}"));

        setModel(this.getClass().getSimpleName());
    }

    public Model(String name) throws ORMException {
        this();

        setName(name);
    }

    @Override
    public void validate() throws ORMException {

        boolean isTransient = false;

        AttrList<AttrAttribute> attributesList = this.getAttributes();
        AttrBoolean attrTransient = (AttrBoolean) this.get("Transient");

        if (attrTransient != null) {
            isTransient = attrTransient.isBoolValue();
        }

        if (!this.getDisplayName().equals("Model") && this.getJavaClass().equals("Model")) {
            logger.error("Cannot set 'Model' java class to another model than 'Model'");
            throw new ORMException("Cannot set 'Model' java class to another model than 'Model'");
        }

        if (this.getDisplayName().equals("Model") && !this.getJavaClass().equals("Model")) {
            logger.error("Cannot set other java class than 'Model' to 'Model' model");
            throw new ORMException("Cannot set other java class than 'Model' to 'Model' model");
        }

        BlockchainObjectReader reader = BlockchainObjectReader.getAdminInstance();

        SearchResult searchResult = reader.search("Model");
        List models = searchResult.getBlockchainObjects();

        if (!isTransient) {
            boolean isDisplayNameRequired = false;
            for (Attr attr : attributesList) {
                AttrAttribute attribute = (AttrAttribute) attr;

                if (attribute.DisplayName && attribute.Required) {
                    isDisplayNameRequired = true;
                    break;
                }
            }

            if (!isDisplayNameRequired) {
                logger.error("'model' object must have one 'required' + 'displayName' attribute");
                throw new ORMException("'model' object must have one 'required' + 'displayName' attribute");
            }
        }

        if (!Main.initDataModelFlag) {
            for (Attr attr : attributesList) {
                AttrAttribute attribute = (AttrAttribute) attr;

                if (attribute.AttrType.equals("AttrID") || attribute.AttrType.equals("AttrIDList")) {
                    if (attribute.AttrTypeModel.equals("")) {
                        logger.error("'AttrTypeModel' must be set for '" + attribute.Name + "' attribute");
                        throw new ORMException("'AttrTypeModel' must be set for '" + attribute.Name + "' attribute");
                    } else {
                        try {
                            Model linkedModel = (Model) reader.read(attribute.AttrTypeModel);
                        } catch (Exception ex) {
                            logger.error("Error reading 'AttrTypeModel' for ID: '" + attribute.AttrTypeModel + ": " + ex.toString());
                            throw new ORMException("Error reading 'AttrTypeModel' for ID: '" + attribute.AttrTypeModel + ": " + ex.toString());
                        }
                    }
                }

                if (!attribute.ACL.equals("")) {
                    try {

                        for (AttrID aclID : attribute.ACL) {
                            ACL acl = (ACL) reader.read(aclID);
                        }
                    } catch (Exception ex) {
                        logger.error("Error reading 'ACL' attribute: " + ex.toString());
                        throw new ORMException("Error reading 'ACL' attribute: " + ex.toString());
                    }
                }
            }
        }
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
    public void setDescription(String description) {
        this.replace("Description", new AttrString(description));
    }

    @JsonIgnore
    public String getDescription() {
        return this.get("Description").toString();
    }

    @JsonIgnore
    public void setJavaClass(String javaClass) {
        this.replace("Java Class", new AttrString(javaClass));
    }

    @JsonIgnore
    public String getJavaClass() {
        return this.get("Java Class").toString();
    }

    @JsonIgnore
    public boolean isTransient() {
        boolean result = false;
        if (this.get("Transient") != null) {
            result = ((AttrBoolean) this.get("Transient")).isBoolValue();
        }
        return result;
    }

    @JsonIgnore
    public void setTransient(boolean istransient) {
        this.replace("Transient", new AttrBoolean(istransient));
    }

    @JsonIgnore
    public boolean isImmutable() {
        boolean result = false;
        if (this.get("Immutable") != null) {
            result = ((AttrBoolean) this.get("Immutable")).isBoolValue();
        }
        return result;
    }

    @JsonIgnore
    public void setImmutable(boolean isImmutable) {
        this.replace("Immutable", new AttrBoolean(isImmutable));
    }

    @JsonIgnore
    public void setDisplayNameSeparator(String displayNameSeparator) {
        this.replace("Display Name Separator", new AttrString(displayNameSeparator));
    }

    @JsonIgnore
    public String getDisplayNameSeparator() {
        return this.get("Display Name Separator").toString();
    }

    @JsonIgnore
    public void setDefaultFormUI(String defaultFormUI) {
        this.replace("Default Form UI", new AttrString(defaultFormUI));
    }

    @JsonIgnore
    public String getDefaultFormUI() {
        return this.get("Default Form UI").toString();
    }


    @JsonIgnore
    public boolean containsModelACL(AttrID aclID) {
        AttrIDList ACLs = (AttrIDList) this.get("ACL");
        return ACLs.containsUID(aclID);
    }

    @JsonIgnore
    public boolean containsModelACL(ACL acl) {
        return containsModelACL(acl.getId());
    }

    @JsonIgnore
    public void addModelACL(AttrID aclID) {
        if (!containsModelACL(aclID)) {
            ((AttrIDList) this.get("ACL")).add(aclID);
        }
    }

    @JsonIgnore
    public void addModelACL(ACL acl) {
        addModelACL(acl.getId());
    }

    @JsonIgnore
    public void removeModelACL(AttrID aclID) {
        ((AttrIDList) this.get("ACL")).removeUID(aclID);
    }

    @JsonIgnore
    public void removeModelACL(ACL acl) {
        removeModelACL(acl.getId());
    }

    @JsonIgnore
    public void setModelACLs(List<AttrID> acls) {
        this.replace("ACL", new AttrIDList(acls));
    }

    @JsonIgnore
    public void setModelACLs(AttrID... aclIDs) {
        this.replace("ACL", new AttrIDList(aclIDs));
    }

    @JsonIgnore
    public AttrIDList getModelACLs() {
        return (AttrIDList) this.get("ACL");
    }

    @JsonIgnore
    public boolean containsAttribute(AttrAttribute attribute) {
        AttrList attributesList = (AttrList) this.get("Attribute List");
        return attributesList.contains(attribute);
    }

    @JsonIgnore
    public void addAttribute(AttrAttribute attribute) {
        if (!containsAttribute(attribute)) {
            ((AttrList) this.get("Attribute List")).add(attribute);
        }
    }

    @JsonIgnore
    public void removeAttribute(AttrAttribute attribute) {
        ((AttrList) this.get("Attribute List")).remove(attribute);
    }


    @JsonIgnore
    public void setAttributes(List<AttrAttribute> attributes) {
        this.replace("Attribute List", new AttrList(attributes));
    }

    @JsonIgnore
    public AttrList<AttrAttribute> getAttributes() {
        return (AttrList<AttrAttribute>) this.get("Attribute List");
    }


}
