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
import com.recordins.recordin.orm.core.BlockchainIndex;
import com.recordins.recordin.orm.exception.ORMException;
import com.recordins.recordin.orm.core.BlockchainObjectIndex;
import com.recordins.recordin.orm.core.BlockchainObjectReader;
import com.recordins.recordin.orm.core.BlockchainObjectReader.SearchResult;
import com.recordins.recordin.Main;
import com.recordins.recordin.utils.DeepCopy;
import org.cheetah.webserver.CheetahWebserver;
import org.cheetah.webserver.authentication.IBlockchainAuthenticator;
import org.cheetah.webserver.authentication.BlockchainAuthenticatorFactory;
import org.ethereum.config.SystemProperties;
import org.ethereum.crypto.ECKey;
import org.ethereum.facade.Ethereum;
import org.ethereum.net.server.Channel;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

import javax.net.ssl.*;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URL;
import java.net.URLConnection;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.AbstractMap;
import java.util.Base64;
import java.util.Map;

import static com.recordins.recordin.Main.sendWebsocket;

public class User extends GDPRObject {

    /* Logger for console output */
    private static Logger logger = LoggerFactory.getLogger(User.class);

    private AttrString address;

    private String login;

    private AttrString privateKey;

    private String sessionCookie = "";

    //private BigInteger nonce = BigInteger.valueOf(-1);

    private static User admin = null;

    private static User guest = null;

    public User() throws ORMException {
        super();

        actionList.clear();

        this.actionList.add(new ActionDefinition("Archive", "Archive", "Execute", "{}"));
        this.actionList.add(new ActionDefinition("UnArchive", "UnArchive", "Execute", "{}"));

        setModel(this.getClass().getSimpleName());

    }

    public User(String login) throws ORMException {
        this();

        IBlockchainAuthenticator authenticator = BlockchainAuthenticatorFactory.getBlockchainAuthenticator();

        if (authenticator.containsUser(login) && admin != null) {
            User user = BlockchainObjectReader.getAdminInstance().readUser(login);

            address = user.getAddress();
            privateKey = new AttrString(authenticator.getPrivateKey(login));
            this.setId(user.getId());

        } else {
            Ethereum ethereum = (Ethereum) ApplicationContext.getInstance().get("ethereum");

            ECKey key = new ECKey();

            byte[] addr = key.getAddress();
            byte[] priv = key.getPrivKeyBytes();

            address = new AttrString(Hex.toHexString(addr));
            privateKey = new AttrString(Hex.toHexString(priv));

            if (!authenticator.containsUser(login)) {
                authenticator.add(login, privateKey.toString());
            }
        }

        this.setAddress(address);
        this.setLogin(login);
        this.setFirstName(login);

        // Add user to Users group
        // when linked objects can be mined in the same transaction

        /*
        if (admin != null && !Main.initDataModelFlag) {

            BlockchainObjectReader reader = null;
            BlockchainObjectReader.SearchResult searchResult = null;
            try {
                reader = BlockchainObjectReader.getAdminInstance();


                Group groupUsers = (Group) reader.search("Group", "Users");
                groupUsers.setUsers(this.getId());
                groupUsers.setId(writer.write(groupUsers, false));


            } catch (ORMException e) {
                logger.error("Error Searching for User Preferences of user '" + this.getLogin() + "'");
            }
        }
        */
    }

    @Override
    public void create(User user) {

        String login = this.getLogin();
        String name = this.getFirstName();

        if (this.getLastName() != null) {
            name += " " + this.getLastName();
        }

        this.setName(name);

        IBlockchainAuthenticator authenticator = BlockchainAuthenticatorFactory.getBlockchainAuthenticator();

        if (!authenticator.containsUser(login)) {
            Ethereum ethereum = (Ethereum) ApplicationContext.getInstance().get("ethereum");

            //address = new AttrString("13d9fbd31b941347f7a00d993a2b47c1ecb05711");
            //privateKey = new AttrString("1c3eaa38a0983eeba090b63b06162ec9dca6a6d3cae448a78ab02ad085351ee5");

            SystemProperties config = (SystemProperties) ApplicationContext.getInstance().get("config");
            String localNodeID = Hex.toHexString(config.nodeId());


            ECKey key = new ECKey();

            byte[] addr = key.getAddress();
            byte[] priv = key.getPrivKeyBytes();

            address = new AttrString(Hex.toHexString(addr));
            privateKey = new AttrString(Hex.toHexString(priv));
            authenticator.add(login, privateKey.toString());
            this.setAddress(address);
        }
    }

    @JsonIgnore
    public static User getAdminUser() {
        if (admin == null) {
            return getAdminUser(false);
        }
        if (admin.getId().toString().contains("init")) {
            return getAdminUser(false);
        }

        return getAdminUser(true);
    }

    @JsonIgnore
    public static User getAdminUser(boolean search) {
        logger.trace("START getAdminUser()");
        User result = null;

        if (admin != null && !Main.initDataModelFlag && search) {

            if (admin.getId().toString().contains("init")) {
                admin = getUser("admin");
            }
        }

        if (admin != null) {
            return admin;
        }

        if (result == null) {
            try {
                result = new User(Main.userAdminName);
                result.setId(new AttrID("1.init.init"));
                String name = Main.userAdminName;
                String nameFormatted = name.substring(0, 1).toUpperCase() + name.substring(1);
                result.setName(nameFormatted);
                result.setFirstName(nameFormatted);

                admin = result;

            } catch (ORMException ex) {
                logger.error("Error building '" + Main.userAdminName + "' user: " + ex.toString());
            }
        }

        logger.trace("END getAdminUser()");
        return result;
    }


    @JsonIgnore
    public static User getGuestUser() {
        if (guest == null) {
            return getGuestUser(false);
        }
        if (guest.getId().toString().contains("init")) {
            return getGuestUser(false);
        }

        return getGuestUser(true);
    }

    @JsonIgnore
    public static User getGuestUser(boolean search) {
        User result = null;

        if (guest != null && !Main.initDataModelFlag && search) {

            if (guest.getId().toString().contains("init")) {
                guest = getUser("guest");
            }
        }

        if (guest != null) {
            return guest;
        }

        if (result == null) {
            try {

                result = new User(Main.userGuestName);
                result.setId(new AttrID("2.init.init"));
                String name = Main.userGuestName;
                String nameFormatted = name.substring(0, 1).toUpperCase() + name.substring(1);
                result.setName(nameFormatted);
                result.setFirstName(nameFormatted);

                guest = result;

            } catch (ORMException ex) {
                logger.error("Error building '" + Main.userGuestName + "' user: " + ex.toString());
            }
        }

        return result;
    }

    @JsonIgnore
    public static User getUser(String name) {
        User result = null;

        if (!CheetahWebserver.getInstance().isSessionAuthenticationEnabled()) {
            name = "admin";
        }

        if (admin != null) {
            try {
                BlockchainObjectReader reader = BlockchainObjectReader.getInstance(admin);

                SearchResult searchResult;
                searchResult = reader.search("User", BlockchainObjectIndex.INDEX_TYPE.VIRTUAL_TABLES_ACTIVE, false, "[[\"Login\",\"=\",\"" + name + "\"]]", 20, 0, "");

                IBlockchainAuthenticator authenticator = BlockchainAuthenticatorFactory.getBlockchainAuthenticator();

                if (searchResult.getCount() == 0) {
                    if (result == null && !CheetahWebserver.getInstance().isSessionAuthenticationEnabled()) {
                        result = getAdminUser();

                        if (authenticator.containsUser(result.getLogin())) {
                            result.setPrivateKey(new AttrString(authenticator.getPrivateKey(result.getLogin())));
                        }

                    } else {
                        logger.error("Error searching '" + name + "' user: user not found !");
                    }
                    return result;
                }

                if (searchResult.getCount() == 1) {
                    result = (User) searchResult.getBlockchainObjects().get(0);

                    if (authenticator.containsUser(result.getLogin())) {
                        result.setPrivateKey(new AttrString(authenticator.getPrivateKey(result.getLogin())));
                    }

                } else {
                    logger.error("Error searching '" + name + "' user: More than one user found !");
                    throw new ORMException("Error searching '" + name + "' user: More than one user found !");
                }

            } catch (ORMException ex) {
                logger.error("Error searching '" + name + "' user: " + ex.toString());
                ex.printStackTrace();
            }
        } else {
            if (!CheetahWebserver.getInstance().isSessionAuthenticationEnabled()) {
                return getAdminUser();
            } else {
                if (name.equals("admin")) {
                    return getAdminUser();
                } else {
                    getAdminUser(false);
                    return getUser(name);
                }
            }
        }

        return result;
    }

    @JsonIgnore
    public static User getUser(AttrID userID) {
        User result = null;

        if (admin != null) {
            try {
                BlockchainObjectReader reader = BlockchainObjectReader.getInstance(admin);

                result = (User) reader.read(userID);

                IBlockchainAuthenticator authenticator = BlockchainAuthenticatorFactory.getBlockchainAuthenticator();
                if (authenticator.containsUser(result.getLogin())) {
                    result.setPrivateKey(new AttrString(authenticator.getPrivateKey(result.getLogin())));
                }

            } catch (ORMException ex) {
                logger.error("Error searching user with ID '" + userID + "' user: " + ex.toString());
                ex.printStackTrace();
            }
        }

        return result;
    }

    @JsonIgnore
    public AttrString getPrivateKey() {
        return privateKey;
    }

    @JsonIgnore
    public void setPrivateKey(AttrString privateKey) {
        this.privateKey = privateKey;

        byte[] keyDecoded = Hex.decode(privateKey.toString());

        ECKey ecKey = ECKey.fromPrivate(keyDecoded);
        this.address = new AttrString(Hex.toHexString(ecKey.getAddress()));

        this.setAddress(this.address);
    }

    @JsonIgnore
    public String getSessionCookie() {
        return sessionCookie;
    }

    @JsonIgnore
    public void setSessionCookie(String sessionCookie) {
        this.sessionCookie = sessionCookie;
    }


    public AttrString getAddress() {
        return address;
    }

    public void setAddress(AttrString address) {
        this.address = address;
        this.replace("Address", this.address);
    }

    @JsonIgnore
    public String getPassword() {
        String result = null;

        IBlockchainAuthenticator authenticator = BlockchainAuthenticatorFactory.getBlockchainAuthenticator();

        if (authenticator.containsUser(this.getLogin())) {
            result = authenticator.getPassword(this.getLogin());
        }
        return result;
    }

    @JsonIgnore
    public void setPassword(String password) {
        IBlockchainAuthenticator authenticator = BlockchainAuthenticatorFactory.getBlockchainAuthenticator();

        if (!password.equals("")) {
            boolean result = authenticator.setPassword(getLogin(), authenticator.getPassword(getLogin()), password);
            if (result) {

                JSONObject jsonResult = new JSONObject();
                jsonResult.put("MessageType", "Success");
                jsonResult.put("MessageValue", "Password updated");

                sendWebsocket(jsonResult.toJSONString(), this);
            }
        }
    }

    public String getLogin() {
        if (login == null || login.equals("")) {
            AttrString attr = (AttrString) this.get("Login");
            if (attr != null) {
                login = attr.toString();
            }
        }
        if (login == null) {
            login = "";
        }
        return login;
    }

    public void setLogin(String login) {
        this.replace("Login", login);
        this.login = login;
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
    public void setFirstName(String firstName) {
        this.replace("First Name", firstName);
    }

    @JsonIgnore
    public String getFirstName() {
        String result = null;

        if (this.containsKey("First Name")) {
            result = this.get("First Name").toString();
        }

        return result;
    }

    @JsonIgnore
    public void setLastName(String firstName) {
        this.replace("Last Name", firstName);
    }

    @JsonIgnore
    public String getLastName() {
        String result = null;

        if (this.containsKey("Last Name")) {
            result = this.get("Last Name").toString();
        }

        return result;
    }

    /*
    @JsonIgnore
    public BigInteger getNonce() {
        return nonce;
    }

    public void setNonce(BigInteger nonce) {
        this.nonce = nonce;
    }
    */


    @JsonIgnore
    public static void readKeyAndCredentials(BlockchainObject object, User user) {
        logger.trace("START readKeyAndCredentials(User)");

        SystemProperties config = (SystemProperties) ApplicationContext.getInstance().get("config");
        String localNodeID = Hex.toHexString(config.nodeId());
        BlockchainObject objectCurrent = null;

        logger.trace("object.getNodeUpdateID(): " + object.getNodeUpdateID());
        logger.trace("localNodeID             : " + localNodeID);

        if (!object.getNodeUpdateID().toString().equals(localNodeID)) {

            AttrDateTime deleteDateTime = null;
            AttrID id = null;
            try {
                BlockchainIndex<String, String> currentVersionsIndex = BlockchainObjectIndex.getInstance().getIndex(BlockchainObjectIndex.INDEX_TYPE.CURRENT_VERSIONS);
                if (currentVersionsIndex.containsKey(object.getId().getUID())) {
                    String idString = currentVersionsIndex.get(object.getId().getUID());

                    id = new AttrID(idString);
                    objectCurrent = BlockchainObjectReader.getAdminInstance().read(idString);
                    if (objectCurrent.getDateTimeDelete() != null) {
                        deleteDateTime = objectCurrent.getDateTimeDelete();
                    }
                } else {
                    objectCurrent = object;
                    id = object.getId();
                }

                if (deleteDateTime == null && objectCurrent != null) {

                    Ethereum ethereum = (Ethereum) ApplicationContext.getInstance().get("ethereum");
                    int targetPort = CheetahWebserver.getInstance().getNetworkPort();


                    final AttrID finalID = id;
                    final BlockchainObject finalObject = objectCurrent;

                    for (Channel channel : Main.activePeers) {
                        if (!Hex.toHexString(channel.getNode().getId()).equals(object.getNodeUpdateID().toString())) {
                            continue;
                        } else {

                            Thread t = new Thread("readKeyAndCredentials") {

                                @Override
                                public void run() {

                                    String portString = "";

                                    if (targetPort != 80 && targetPort != 443) {
                                        portString = ":" + targetPort;
                                    }

                                    try {
                                        String URLString = "https://" + channel.getInetSocketAddress().getHostString() + portString;

                                        URLString = URLString + "/orm/GetUserKeyAndCredentials?nodeID=" + localNodeID + "&id=" + finalID.toString();

                                        URL url = new URL(URLString);

                                        logger.debug("Retrieving User Keys And Credentials from another node: " + url.toString());

                                        //waiting a few time to wait for eventual block propagation...
                                        try {
                                            Thread.sleep(2000);
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }

                                        URLConnection connection = url.openConnection();

                                        if (url.getProtocol().toLowerCase().startsWith("https")) {

                                            try {
                                                SSLContext sc;
                                                sc = SSLContext.getInstance("SSLv3");

                                                // Create a trust manager that does not validate certificate chains
                                                final TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
                                                    @Override
                                                    public void checkClientTrusted(final X509Certificate[] chain, final String authType) {
                                                    }

                                                    @Override
                                                    public void checkServerTrusted(final X509Certificate[] chain, final String authType) {
                                                    }

                                                    @Override
                                                    public X509Certificate[] getAcceptedIssuers() {
                                                        return null;
                                                    }
                                                }};

                                                if (CheetahWebserver.getInstance().isNetworkSecureSSLEnforceValidation()) {
                                                    sc.init(null, null, null);
                                                } else {
                                                    sc.init(null, trustAllCerts, null);
                                                }

                                                final SSLSocketFactory sslSocketFactory = sc.getSocketFactory();

                                                ((HttpsURLConnection) connection).setSSLSocketFactory(sslSocketFactory);
                                            } catch (NoSuchAlgorithmException | KeyManagementException e) {
                                                logger.error("SSL initialization failed", e);
                                            }

                                        }

                                        int timeout = ApplicationContext.getInstance().getInteger("AttachmentRetrieveDownloadTimeout") * 1000;
                                        connection.setConnectTimeout(timeout);
                                        connection.setReadTimeout(timeout);

                                        if (!user.getLogin().equals("")) {
                                            String userpass = user.getLogin() + ":" + user.getPassword();
                                            String basicAuth = "Basic " + new String(Base64.getEncoder().encode(userpass.getBytes()));
                                            connection.setRequestProperty("Authorization", basicAuth);
                                        }


                                        DataInputStream in = new DataInputStream(connection.getInputStream());

                                        byte[] buffer = new byte[1048576];

                                        ByteArrayOutputStream bout = new ByteArrayOutputStream();
                                        int readbytes = in.read(buffer);

                                        while (readbytes != -1) {
                                            bout.write(buffer, 0, readbytes);
                                            readbytes = in.read(buffer);
                                        }
                                        in.close();

                                        Map.Entry<String, String> entry = (AbstractMap.SimpleEntry) DeepCopy.unserialize(bout.toByteArray());

                                        User requestedUser = null;
                                        if (finalObject.getModel().equals("User")) {
                                            requestedUser = (User) finalObject;
                                        } else {
                                            requestedUser = finalObject.getUserOwner();
                                        }

                                        IBlockchainAuthenticator authenticator = BlockchainAuthenticatorFactory.getBlockchainAuthenticator();
                                        authenticator.setPrivateKey(requestedUser.getLogin(), entry.getKey());

                                        //if (entry.getValue() != null && !entry.getValue().equals("")) {
                                        if (entry.getValue() != null) {
                                            authenticator.setPassword(requestedUser.getLogin(), requestedUser.getPassword(), entry.getValue());
                                        }

                                        logger.debug("Retrieving User Keys And Credentials from another node: success");

                                    } catch (IOException e1) {
                                        try {
                                            String URLString = "http://" + channel.getInetSocketAddress().getHostString() + portString;

                                            URLString = URLString + "/orm/GetUserKeyAndCredentials?nodeID=" + localNodeID + "&id=" + finalID.toString();

                                            URL url = new URL(URLString);

                                            logger.debug("Retrieving User Keys And Credentials from another node: " + url.toString());

                                            URLConnection connection = url.openConnection();

                                            int timeout = ApplicationContext.getInstance().getInteger("AttachmentRetrieveDownloadTimeout") * 1000;
                                            connection.setConnectTimeout(timeout);
                                            connection.setReadTimeout(timeout);

                                            if (!user.getLogin().equals("")) {
                                                String userpass = user.getLogin() + ":" + user.getPassword();
                                                String basicAuth = "Basic " + new String(Base64.getEncoder().encode(userpass.getBytes()));
                                                connection.setRequestProperty("Authorization", basicAuth);
                                            }


                                            DataInputStream in = new DataInputStream(connection.getInputStream());

                                            byte[] buffer = new byte[1048576];

                                            ByteArrayOutputStream bout = new ByteArrayOutputStream();
                                            int readbytes = in.read(buffer);

                                            while (readbytes != -1) {
                                                bout.write(buffer, 0, readbytes);
                                                readbytes = in.read(buffer);
                                            }
                                            in.close();

                                            Map.Entry<String, String> entry = (AbstractMap.SimpleEntry) DeepCopy.unserialize(bout.toByteArray());

                                            User requestedUser = null;
                                            if (finalObject.getModel().equals("User")) {
                                                requestedUser = (User) finalObject;
                                            } else {
                                                requestedUser = finalObject.getUserOwner();
                                            }

                                            IBlockchainAuthenticator authenticator = BlockchainAuthenticatorFactory.getBlockchainAuthenticator();
                                            authenticator.setPrivateKey(requestedUser.getLogin(), entry.getKey());

                                            //if (entry.getValue() != null && !entry.getValue().equals("")) {
                                            if (entry.getValue() != null) {
                                                authenticator.setPassword(requestedUser.getLogin(), requestedUser.getPassword(), entry.getValue());
                                            }

                                            logger.debug("Retrieving User Keys And Credentials from another node: success");
                                        } catch (IOException e2) {
                                            logger.error("Error retrieving User Keys And Credentials from another node: " + e2.toString());
                                            e2.printStackTrace();
                                        }
                                    }
                                }
                            };
                            t.start();

                            if (!Main.syncComplete) {
                                t.join(ApplicationContext.getInstance().getInteger("AttachmentRetrieveDownloadTimeout") * 1000 * 3);
                            }

                            break;
                        }
                    }
                }
            } catch (Exception e) {
                logger.error("Error retrieving User Keys And Credentials from another node: " + e.toString());
                e.printStackTrace();
            }
        }
    }
}
