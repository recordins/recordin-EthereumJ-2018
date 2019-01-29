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
import com.recordins.recordin.ApplicationContext;
import com.recordins.recordin.orm.action.ActionDefinition;
import com.recordins.recordin.orm.attribute.AttrDateTime;
import com.recordins.recordin.orm.attribute.AttrID;
import com.recordins.recordin.orm.attribute.AttrString;
import com.recordins.recordin.orm.attribute.AttrTimeZone;
import com.recordins.recordin.orm.core.BlockchainIndex;
import com.recordins.recordin.orm.exception.ORMException;
import com.recordins.recordin.orm.core.BlockchainObjectIndex;
import com.recordins.recordin.orm.core.BlockchainObjectReader;
import com.recordins.recordin.utils.DeepCopy;
import org.cheetah.webserver.CheetahWebserver;
import org.cheetah.webserver.authentication.BlockchainAuthenticatorFactory;
import org.cheetah.webserver.authentication.IBlockchainAuthenticator;
import org.cheetah.webserver.resources.upload.DownloadClient;
import org.ethereum.config.SystemProperties;
import org.ethereum.facade.Ethereum;
import org.ethereum.net.server.Channel;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

import javax.net.ssl.*;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.AbstractMap;
import java.util.Base64;
import java.util.Map;

import static com.recordins.recordin.Main.sendWebsocket;

public class Preferences extends BlockchainObject {

    /* Logger for console output */
    private static Logger logger = LoggerFactory.getLogger(Preferences.class);

    public Preferences() throws ORMException {
        super();

        actionList.clear();

        this.actionList.add(new ActionDefinition("CreatePreferences", "Create Preferences", "TransientObject", "{\"model\":\"CreatePreferences\", \"select\": false}"));
        this.actionList.add(new ActionDefinition("RefreshPassword", "Refresh Password", "Execute", ""));

        setModel(this.getClass().getSimpleName());
    }

    @Override
    public void create(User user) throws ORMException {
        logger.trace("START Preferences create()");

        if (getUserOwner() != null) {
            user = getUserOwner();
        }
        BlockchainObjectReader reader = null;
        BlockchainObjectReader.SearchResult searchResult = null;
        try {
            reader = BlockchainObjectReader.getAdminInstance();

            if (user.getId() != null) {
                searchResult = reader.search("Preferences", BlockchainObjectIndex.INDEX_TYPE.VIRTUAL_TABLES_ACTIVE, false, "[[\"userOwnerID\",\"=\",\"" + user.getId().toString() + "\"]]", 20, 0, "");
            }
        } catch (ORMException e) {
            logger.error("Error Searching for User Preferences of user '" + user.getLogin() + "'");
        }

        if (searchResult != null && searchResult.getCount() >= 1) {
            logger.error("Preferences already exists for user '" + user.getLogin() + "'");
            throw new ORMException("Preferences already exists for user '" + user.getLogin() + "'");
        }


        IBlockchainAuthenticator authenticator = BlockchainAuthenticatorFactory.getBlockchainAuthenticator();
        String password = this.get("Password").toString();

        if (!password.equals("")) {
            boolean result = authenticator.setPassword(user.getLogin(), authenticator.getPassword(user.getLogin()), password);
            if (result) {

                JSONObject jsonResult = new JSONObject();
                jsonResult.put("MessageType", "Success");
                jsonResult.put("MessageValue", "Password updated");

                sendWebsocket(jsonResult.toJSONString(), user);
            }
        }

        this.replace("Name", new AttrString(user.getDisplayName() + " Preferences"));
        this.replace("Password", new AttrString(""));


        logger.trace("END Preferences create()");
    }

    @Override
    public void write(User user) {
        logger.trace("START Preferences write()");

        IBlockchainAuthenticator authenticator = BlockchainAuthenticatorFactory.getBlockchainAuthenticator();
        String password = this.get("Password").toString();

        User owner = this.getUserOwner();

        if (!password.equals("")) {
            boolean result = authenticator.setPassword(owner.getLogin(), authenticator.getPassword(owner.getLogin()), password);
            if (result) {

                JSONObject jsonResult = new JSONObject();
                jsonResult.put("MessageType", "Success");

                if (user.getId().getUID().equals(owner.getId().getUID())) {
                    jsonResult.put("MessageValue", "Password updated");
                } else {
                    jsonResult.put("MessageValue", "Password updated for user: " + owner.getDisplayName());
                }

                sendWebsocket(jsonResult.toJSONString(), user);
            }
        }
        this.replace("Password", new AttrString(""));

        logger.trace("END Preferences write()");
    }


    @JsonIgnore
    public void setName(String name) {
        this.replace("Name", name);
    }

    @JsonIgnore
    public String getName() {
        String result = null;

        if (this.containsKey("Name")) {
            result = this.get("Name").toString();
        }

        return result;
    }

    @JsonIgnore
    public void setTimezone(AttrTimeZone timezone) {
        this.replace("Timezone", timezone);
    }

    @JsonIgnore
    public AttrTimeZone getTimezone() {
        AttrTimeZone result = null;

        if (this.containsKey("Timezone")) {
            result = (AttrTimeZone) this.get("Timezone");
        }

        return result;
    }


    @JsonIgnore
    public void setPassword(String password) {
        this.replace("Password", password);
    }

    @JsonIgnore
    public String getPassword() {
        String result = null;

        if (this.containsKey("Password")) {
            result = this.get("Password").toString();
        }

        return result;
    }
}
