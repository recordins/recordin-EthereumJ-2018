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

package com.recordins.recordin.orm.action;

import com.recordins.recordin.orm.BlockchainObject;
import com.recordins.recordin.orm.Preferences;
import com.recordins.recordin.orm.User;
import com.recordins.recordin.orm.attribute.AttrID;
import com.recordins.recordin.orm.exception.ORMException;
import com.recordins.recordin.orm.core.BlockchainIndex;
import com.recordins.recordin.orm.core.BlockchainObjectIndex;
import com.recordins.recordin.orm.core.BlockchainObjectReader;
import com.recordins.recordin.orm.core.BlockchainObjectWriter;

import java.util.ArrayList;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CreatePreferences implements Action {

    private static Logger logger = LoggerFactory.getLogger(CreatePreferences.class);

    ArrayList<String> arrayUID = new ArrayList();
    JSONArray jsonArrayObject = new JSONArray();
    BlockchainObject transientObject = null;

    private CreatePreferences() {
    }

    public CreatePreferences(String args) {
        logger.trace("START: CreatePreferences(String)");

        JSONParser parser = new JSONParser();
        try {
            JSONObject jsonArgs = (JSONObject) parser.parse(args);
            JSONArray jsonArrayUID = (JSONArray) jsonArgs.get("ids");
            jsonArrayObject = (JSONArray) jsonArgs.get("args");

            this.arrayUID = new ArrayList(jsonArrayUID);

            for (String uid : arrayUID) {
                logger.debug("UID: " + uid);
            }

        } catch (ParseException ex) {
            logger.error("Error parsing Action arguments: " + ex.toString());
        }

        logger.trace("END: CreatePreferences()");
    }

    @Override
    public void execute(User user) throws ORMException {
        logger.trace("START: execute(User)");

        BlockchainObjectReader reader = BlockchainObjectReader.getInstance(user);
        BlockchainObjectWriter writer = BlockchainObjectWriter.getInstance(user);
        BlockchainIndex<String, String> currentVersionsIndex = BlockchainObjectIndex.getInstance().getIndex(BlockchainObjectIndex.INDEX_TYPE.CURRENT_VERSIONS);

        transientObject = writer.getObject(jsonArrayObject);

        AttrID userID = (AttrID) transientObject.get("User");

        if (userID != null) {

            User userOwner = User.getUser(userID);

            BlockchainObjectReader.SearchResult searchResult = null;
            try {

                if (userOwner.getId() != null) {
                    searchResult = reader.search("Preferences", BlockchainObjectIndex.INDEX_TYPE.VIRTUAL_TABLES_ACTIVE, false, "[[\"userOwnerID\",\"=\",\"" + userOwner.getId().toString() + "\"]]", 20, 0, "");
                }

            } catch (ORMException e) {
                logger.error("Error searching for user preferences of user '" + userOwner.getLogin() + "'");
            }
            try {
                if (searchResult == null || searchResult.getCount() == 0) {
                    Preferences preferences = new Preferences();
                    preferences.setName("My Preferences");
                    preferences.setPassword(transientObject.get("Password").toString());
                    preferences.setUserOwner(userOwner);
                    BlockchainObjectWriter.getInstance(user).write(preferences);
                }
            } catch (ORMException e) {
                logger.error("Error creating user preferences of user '" + userOwner.getLogin() + "'");
            }
        } else {
            logger.error("Error executing Action: No user found !");
            throw new ORMException("Error executing Action: No user found !");
        }

        /*
        AttrIDList userIDs = (AttrIDList)transientObject.get("Users");

        if(userIDs.size()>0) {
            AttrID newOwnerID = (AttrID) userIDs.get(0);

            if (newOwnerID == null || newOwnerID.toString().equals("")) {
                logger.error("Error executing Action: No 'New Owner' attribute found !");
                throw new ORMException("Error executing Action: No 'New Owner' attribute found !");
            }

            Model model = writer.getModel("TransientObjectTest");
            writer.validateObjectWithDataModel(transientObject, model, false);

            for (String uid : arrayUID) {

                if (currentVersionsIndex.containsKey(uid)) {
                    String id = currentVersionsIndex.get(uid);

                    BlockchainObject object = reader.read(id);
                    object.setUserOwnerID(newOwnerID);
                    writer.write(object, true);
                }
            }
        }
        else{
            logger.error("Error executing Action: No 'New Owner' attribute found !");
            throw new ORMException("Error executing Action: No 'New Owner' attribute found !");
        }
        */

        logger.trace("END: execute()");
    }
}
