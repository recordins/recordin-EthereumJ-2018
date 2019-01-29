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

package com.recordins.recordin;

import com.fasterxml.jackson.databind.ObjectMapper;

import static com.recordins.recordin.config.standard.listener.RecordinEthereumListener.sendJSONStatus;

import com.recordins.recordin.config.standard.PrivateNetworkConfig;
import com.recordins.recordin.config.standard.StandaloneConfig;
import com.recordins.recordin.config.standard.mine.RecordinMinerListener;
import com.recordins.recordin.orm.*;
import com.recordins.recordin.orm.attribute.*;
import com.recordins.recordin.orm.exception.ORMException;
import com.recordins.recordin.orm.core.BlockchainIndex;
import com.recordins.recordin.orm.core.BlockchainObjectIndex;
import com.recordins.recordin.orm.core.BlockchainObjectReader;
import com.recordins.recordin.orm.core.BlockchainObjectReader.SearchResult;
import com.recordins.recordin.orm.core.BlockchainObjectWriter;
import com.recordins.recordin.utils.DateFormat;

import static com.recordins.recordin.utils.ReflexionUtils.getClassesFromPackage;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import org.cheetah.webserver.WebServerContext;
import org.cheetah.webserver.authentication.BlockchainAuthenticatorFactory;
import org.cheetah.webserver.authentication.IBlockchainAuthenticator;
import org.ethereum.config.SystemProperties;
import org.ethereum.facade.Ethereum;
import org.ethereum.net.eth.message.StatusMessage;
import org.ethereum.net.rlpx.Node;
import org.ethereum.net.server.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import javax.annotation.PostConstruct;

import org.cheetah.webserver.CheetahWebserver;
import org.ethereum.core.Block;
import org.ethereum.core.Transaction;
import org.ethereum.crypto.ECKey;
import org.ethereum.db.DbFlushManager;
import org.ethereum.facade.Blockchain;
import org.ethereum.facade.EthereumFactory;
import org.ethereum.facade.SyncStatus;
import org.ethereum.mine.BlockMiner;
import org.ethereum.mine.Ethash;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.simpleframework.http.core.Controller;
import org.spongycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;

public class Main {

    /* This must be the very first statement */
    static {
        System.setProperty("logback.configurationFile", "etc/logback.xml");

        /* For disabling spring logging */
        System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.NoOpLog");
    }

    public static final Logger logger = LoggerFactory.getLogger(Main.class);

    /**
     * **** For Ethereum node configuration *********
     */
    private static Main instance = null;

    @Autowired
    private Ethereum ethereum;

    @Autowired
    private SystemProperties config;

    @Autowired
    DbFlushManager dbFlushManager;

    public static boolean miningStarted = false;

    public static List<Channel> activePeers = new Vector<>();
    public static Map<Node, StatusMessage> ethNodes = new Hashtable<>();
    public static List<Node> syncPeers = new Vector<>();

    /**
     * **** For Recordin runtime *********
     */
    public static ConcurrentHashMap<String, PendingTransactionInformation> pendingTransaction = new ConcurrentHashMap();
    private CheetahWebserver webserver = null;

    public static boolean syncComplete = false;
    public static boolean datasetReady = true;
    public static boolean processingBlock = false;
    public static long lastImported = 0l;
    public static LocalDateTime datasetGenerateStart = null;

    private static boolean postConstruct = false;
    public static boolean platformStarted = false;

    public static boolean initFlag = false;
    public static boolean initDataModelFlag = true;

    public static final String userAdminName = "admin";
    public static final String userGuestName = "guest";

    public static ConcurrentHashMap<String, String> initModelNameIDS = new ConcurrentHashMap();
    public static ConcurrentHashMap<String, Model> initIDSModels = new ConcurrentHashMap();

    // Spring config class which add this class as a bean to the components collections
    // and make it possible for autowiring other components
    private static class StandaloneConfigImpl extends StandaloneConfig {

        @Bean
        @Override
        public Main sampleBean() {
            instance = new Main();
            return instance;
        }
    }

    // Spring config class which add this class as a bean to the components collections
    // and make it possible for autowiring other components
    private static class PrivateNetworkConfigImpl extends PrivateNetworkConfig {

        @Bean
        @Override
        public Main sampleBean() {
            instance = new Main();
            return instance;
        }
    }

    public static Main getInstance() {
        return instance;
    }

    public static void main(String[] args) throws Exception {
        logger.trace("START main(String[])");

        ApplicationContext applicationContext = ApplicationContext.getInstance();

        applicationContext.put("ACLEnableAttribute", true);
        applicationContext.put("ACLEnableMenu", true);
        applicationContext.put("ACLEnableModel", true);
        applicationContext.put("ACLEnableObject", true);
        applicationContext.put("ACLPrintDebugTraces", true);

        applicationContext.put("AttachmentRetrieveDownloadTimeout", 10);
        applicationContext.put("AttachmentRetrieveFromExternal", true);

        applicationContext.put("AttachmentSendAttachments", true);
        applicationContext.put("AttachmentSendAttachmentsAuthorizedNodes", "[]");
        applicationContext.put("AttachmentSendGDPRObjects", true);
        applicationContext.put("AttachmentSendGDPRObjectsAuthorizedNodes", "[]");
        applicationContext.put("AttachmentSendUserCredentials", true);
        applicationContext.put("AttachmentSendUserCredentialsAuthorizedNodes", "[]");

        applicationContext.put("AttachmentSignatureFailPolicy", "error");

        applicationContext.put("NodeDefaultPlatformVersion", "1.0");
        applicationContext.put("NodeObjectCacheEnable", true);
        applicationContext.put("NodeObjectCacheSize", 10000);
        applicationContext.put("NodeObjectSearchResultMax", 500);
        applicationContext.put("NodeReadOnly", false);
        applicationContext.put("NodeStandalone", true);
        applicationContext.put("NodeSyncTimeout", 60);

        applicationContext.put("MiningEnabled", true);
        applicationContext.put("MiningFullDataSet", true);

        applicationContext.put("PeerActive", "[]");
        applicationContext.put("PeerListenPort", 50000);
        applicationContext.put("PeerNetworkId", 444);

        applicationContext.loadProperties();
        applicationContext.printProperties();

        //applicationContext.put("NodeClassLoader", new CheetahClassLoader(Main.class.getClassLoader()));


        WebServerContext webserverContext = new WebServerContext(Paths.get("etc/webserver.properties"));
        webserverContext.loadProperties();

        String hostname = "";
        if (webserverContext.containsKey("NetworkInterface")) {
            hostname = webserverContext.getString("NetworkInterface");
        }

        if (hostname.equals("")) {

            hostname = "localhost";

            try {
                java.net.InetAddress addr = java.net.InetAddress.getLocalHost();
                hostname = addr.getHostAddress();

            } catch (Exception e) {
            }

            if (hostname.equals("127.0.1.1")) {
                hostname = "127.0.0.1";
            }
        }

        applicationContext.put("bind.ip", hostname);

        logger.info("Starting EthereumJ!");

        Ethash.fileCacheEnabled = true;

        if (ApplicationContext.getInstance().getBoolean("NodeStandalone")) {

            syncComplete = true;

            //EthereumFactory.createEthereum(new Class[]{StandaloneCasperExtendedConfigImpl.class});
            EthereumFactory.createEthereum(new Class[]{StandaloneConfigImpl.class});
        } else {

            //EthereumFactory.createEthereum(new Class[]{PrivateNetworkCasperExtendedConfigImpl.class});
            EthereumFactory.createEthereum(new Class[]{PrivateNetworkConfigImpl.class});
        }

        logger.trace("END main()");
    }

    /**
     * The core is called after all EthereumJ instances are created
     */
    @PostConstruct
    private void init() {
        logger.trace("START init()");

        ApplicationContext.getInstance().put("config", config);
        ApplicationContext.getInstance().put("ethereum", ethereum);
        ApplicationContext.getInstance().put("dbFlushManager", dbFlushManager);

        RecordinMinerListener minerListener = new RecordinMinerListener();
        ethereum.getBlockMiner().addListener(minerListener);

        logger.info("Sample component created. Listening for ethereum events...");

        logger.info("Starting webserver");

        webserver = new CheetahWebserver();
        webserver.setStopStrategy(Controller.STOP_STRATEGY.KILL);
        webserver.printProperties();
        webserver.setPrintURLResolvingTraces(false);

        if (CheetahWebserver.getInstance() == null) {
            logger.error("Failed to start CheetahWebserver ! Aborting...");
            System.exit(1);
        }

        if (ApplicationContext.getInstance().getBoolean("NodeStandalone")) {
            logger.info("Starting in standalone mode");
        }

        BlockMiner blockMiner = ethereum.getBlockMiner();


        postConstruct = true;

        if (config.minerStart()) {

            new Thread("BlockMiner") {
                @Override
                public void run() {


                    while (!syncComplete) {
                        try {
                            Thread.sleep(50);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    logger.info("Start mining");
                    blockMiner.startMining();

                }
            }.start();

        }
        logger.trace("END init()");
    }

    public void startPlatform() {
        logger.trace("START startPlatform()");


        while (!postConstruct) {
            try {

                logger.debug("Waiting for construct...");
                Thread.sleep(333);
            } catch (InterruptedException ex) {
            }
        }


        Thread t = new Thread("startPlatform") {

            @Override
            public void run() {

                SyncStatus syncStatus = ethereum.getSyncStatus();
                if (syncStatus.getStage().name().equals("Complete")) {
                    syncComplete = true;
                }

                logger.debug("syncStatus: " + syncStatus.toString());

                BlockMiner blockMiner = ethereum.getBlockMiner();

                ApplicationContext applicationContext = ApplicationContext.getInstance();

                while (config.minerStart() && !blockMiner.isMining()) {
                    try {

                        logger.debug("Waiting for Miner...");
                        Thread.sleep(333);
                    } catch (InterruptedException ex) {
                    }
                }

                Block bestblock = ethereum.getBlockchain().getBestBlock();

                //printBlockchain();
                if (bestblock.isGenesis()) {

                    IBlockchainAuthenticator authenticator = BlockchainAuthenticatorFactory.getBlockchainAuthenticator();
                    authenticator.clear();

                    initDataModelFlag = true;
                    initFlag = true;
                    initUsers();
                    initDataModel("com/recordins/recordin/orm/DataModel.json");
                    updateUsers();
                    initGroups();
                    initACLs();
                    initMenu();

                    /*
                    initDataModel("com/recordins/recordin/orm/DataModelDemo.json");

                    testInsertBlock();
                    testReadWriteBlock();
                    testNewBox();
                    testDelete();
                    */

                    initFlag = false;

                    //insertBoxes(1000);

                    //printBlockchain();
                } else {

                    initDataModelFlag = false;
                    initFlag = false;

                    //testReadWriteModel();

                    if (syncComplete) {
                        try {
                            BlockchainObjectIndex.checkIndexes();
                        } catch (ORMException e) {
                            logger.error("Error loading indexes: " + e.toString());
                        }
                    }
                }

                //printBlockchain();
                logger.info("Recordin started");
            }
        };
        t.start();

        Runtime.getRuntime().addShutdownHook(new Thread() {

            @Override
            public void run() {

                webserver.stop();

                while (processingBlock) {
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {

                    }
                }

                logger.debug("Flushing Blockchain Database");
                Main.flushBlockchainDBSync();

                logger.debug("Commiting all indexes");
                try {
                    BlockchainObjectIndex.getInstance().commitAllIndexes();
                } catch (ORMException e) {
                    logger.error("Error commit indexes from block: " + e.toString());
                    e.printStackTrace();
                    Runtime.getRuntime().halt(2);
                }

                Runtime.getRuntime().halt(0);
            }
        });
        logger.trace("END startPlatform()");
    }

    public static void flushBlockchainDB() {

        //((CasperExtendedBlockchain) ethereum.getBlockchain()).flushDB();
        //ethereum.getBlockchain().flush();

        DbFlushManager dbFlushManager = (DbFlushManager) ApplicationContext.getInstance().get("dbFlushManager");
        dbFlushManager.commit();
        dbFlushManager.flush();
    }

    public static void flushBlockchainDBSync() {

        //((CasperExtendedBlockchain) ethereum.getBlockchain()).flushDB();
        //ethereum.getBlockchain().flush();

        DbFlushManager dbFlushManager = (DbFlushManager) ApplicationContext.getInstance().get("dbFlushManager");
        dbFlushManager.commit();
        dbFlushManager.flushSync();
    }


    private void initUsers() {
        logger.debug("START initUsers()");

        BlockchainObjectWriter writer = null;
        User user = null;
        try {
            user = User.getAdminUser();
            writer = BlockchainObjectWriter.getInstance(user);
            user.setId(writer.write(user, false));
        } catch (ORMException ex) {
            logger.error("Error writing '" + user.getDisplayName() + "' user: " + ex.toString());
        }

        try {
            user = User.getGuestUser();
            user.setId(writer.write(user, false));
        } catch (ORMException ex) {
            logger.error("Error writing '" + user.getDisplayName() + "' user: " + ex.toString());
        }

        logger.debug("END initUsers()");
    }

    String metamodelID = "";
    String menumodelID = "";

    public void initDataModel(String dataModelResource) {
        logger.debug("START initDataModel()");

        initDataModelFlag = true;
        BlockchainObjectReader reader = null;
        User admin = null;
        try {
            reader = BlockchainObjectReader.getAdminInstance();
            admin = User.getAdminUser();
        } catch (ORMException ex) {
            logger.error("Error reading 'admin' user: " + ex.toString());
        }
        if (admin != null) {

            BlockchainObjectWriter writer = BlockchainObjectWriter.getInstance(admin);
            try {
                URL url = webserver.getClassLoader().getResource(dataModelResource);
                String jsonDataModel = readTextFileRessource(url, webserver.getClassLoader(), CheetahWebserver.getInstance().findCharset());

                JSONParser parser = new JSONParser();
                JSONArray jsonArray = (JSONArray) parser.parse(jsonDataModel);

                for (int i = 0; i < jsonArray.size(); i++) {
                    JSONObject jsonObject = (JSONObject) jsonArray.get(i);

                    String modelName = jsonObject.get("Name").toString();
                    logger.debug("");
                    logger.debug("******************************************************");
                    logger.debug("MODEL : " + modelName);
                    //logger.debug("jsonObject : " + jsonObject);

                    Model model = null;
                    SearchResult searchResult = reader.search("Model");

                    for (BlockchainObject object : searchResult.getBlockchainObjects()) {
                        Model modelTmp = (Model) object;
                        if (modelTmp.getName().equals(modelName)) {
                            model = modelTmp;
                            break;
                        }
                    }

                    if (model == null) {
                        model = new Model();
                    }
                    model.setName(modelName);
                    model.setModel("Model");

                    for (Object object : jsonObject.entrySet()) {
                        Map.Entry entry = (Map.Entry) object;
                        String key = (String) entry.getKey();
                        Object value = entry.getValue();

                        if (JSONArray.class.isAssignableFrom(value.getClass())) {

                            if (key.equals("Attribute List")) {
                                JSONArray attributeArray = (JSONArray) value;

                                AttrList attrListAttribute = new AttrList();

                                for (int j = 0; j < attributeArray.size(); j++) {
                                    JSONObject attributeObject = (JSONObject) attributeArray.get(j);

                                    AttrAttribute attr = new AttrAttribute(attributeObject.toJSONString());
                                    attr.ContextData = attr.ContextData.replaceAll("'", "\"");
                                    attrListAttribute.add(attr);
                                }
                                model.put(key, attrListAttribute);
                            }
                            /*
                            if (key.equals("ACL")) {
                                JSONArray attributeArray = (JSONArray) value;

                                AttrList attrListACL = new AttrList();

                                for (int j = 0; j < attributeArray.size(); j++) {
                                    // AttrACL
                                }
                            }
                            */

                        } else if (key.equals("Transient") || key.equals("Immutable")) {
                            model.put(key, new AttrBoolean(value.toString()));
                        } else {
                            model.put(key, value.toString());
                        }
                    }
                    model.setId(writer.write(model, false));

                    if (initModelNameIDS.containsKey(model.getName())) {
                        initModelNameIDS.remove(model.getName());
                    }
                    if (initIDSModels.containsKey(model.getId().toString())) {
                        initIDSModels.remove(model.getId().toString());
                    }
                    initModelNameIDS.put(model.getName(), model.getId().toString());
                    initIDSModels.put(model.getId().toString(), model);

                    logger.debug("******************************************************");
                    logger.debug("");
                }

            } catch (Exception ex) {
                logger.error("Error init of DataModel: " + ex.toString());
                ex.printStackTrace();
            }

        }

        initDataModelFlag = false;
        logger.debug("END initDataModel()");
    }


    private void updateUsers() {
        User admin = User.getAdminUser();
        BlockchainObjectReader reader = BlockchainObjectReader.getInstance(admin);
        BlockchainObjectWriter writer = BlockchainObjectWriter.getInstance(admin);

        try {
            admin = (User) reader.search("User", "Admin");

            admin.setModel("User");
            admin.setName("Admin");
            User guest = (User) reader.search("User", "Guest");
            guest.setModel("User");
            guest.setName("Guest");

            writer.write(admin);
            writer.write(guest, false);

        } catch (ORMException e) {
            logger.error("Error updating users with Model");
        }
    }

    private void initGroups() {
        logger.debug("START initGroups()");

        User admin = User.getAdminUser();
        if (admin != null) {

            BlockchainObjectReader reader = BlockchainObjectReader.getInstance(admin);
            BlockchainObjectWriter writer = BlockchainObjectWriter.getInstance(admin);

            User guest = User.getGuestUser();

            try {
                Group groupAdmin = new Group("Admins");
                groupAdmin.setUsers(admin.getId());
                groupAdmin.setId(writer.write(groupAdmin, false));

                Group groupUsers = new Group("Users");
                groupUsers.setUsers(admin.getId(), guest.getId());
                groupUsers.setGroups(groupAdmin.getId());
                groupUsers.setId(writer.write(groupUsers, false));

            } catch (ORMException e) {
                logger.error("Error init Groups: " + e.toString());
            }
        }

        logger.debug("END initGroups()");
    }

    private void initACLs() {
        logger.debug("START initACLs()");

        User admin = User.getAdminUser();
        if (admin != null) {

            BlockchainObjectReader reader = BlockchainObjectReader.getInstance(admin);
            BlockchainObjectWriter writer = BlockchainObjectWriter.getInstance(admin);

            User guest = User.getGuestUser();

            ACL aclAdmins = null;
            ACL aclUsers = null;
            ACL aclUsersOwn = null;
            try {

                Group groupAdmins = (Group) reader.search("Group", "Admins");

                aclAdmins = new ACL("Admins");
                if (groupAdmins != null) {
                    aclAdmins.addGrantedGroup(groupAdmins);
                }
                aclAdmins.setCreate(true);
                aclAdmins.setDeleteAll(true);
                aclAdmins.setReadAll(true);
                aclAdmins.setWriteAll(true);
                aclAdmins.setAttachmentAll(true);
                //aclAdmins.setUpload(true);

                aclAdmins.setId(writer.write(aclAdmins, false));

                Group groupUsers = (Group) reader.search("Group", "Users");

                aclUsersOwn = new ACL("Users Own");
                if (groupUsers != null) {
                    aclUsersOwn.addGrantedGroup(groupUsers);
                }
                aclUsersOwn.setCreate(true);
                aclUsersOwn.setDeleteAll(false);
                aclUsersOwn.setDeleteOwn(true);
                aclUsersOwn.setReadAll(false);
                aclUsersOwn.setReadOwn(true);
                aclUsersOwn.setWriteAll(false);
                aclUsersOwn.setWriteOwn(true);
                aclUsersOwn.setAttachmentAll(false);
                aclUsersOwn.setAttachmentOwn(true);
                //aclUsersOwn.setUpload(true);
                aclUsersOwn.setId(writer.write(aclUsersOwn, false));

                aclUsers = new ACL("Users");
                if (groupUsers != null) {
                    aclUsers.addGrantedGroup(groupUsers);
                }
                aclUsers.setCreate(true);
                aclUsers.setDeleteAll(true);
                aclUsers.setReadAll(true);
                aclUsers.setWriteAll(true);
                aclUsers.setAttachmentAll(true);
                //aclUsers.setUpload(true);

                writer.write(aclUsers, false);

            } catch (ORMException e) {
                logger.error("Error init ACLs: " + e.toString());
            }

            if (aclAdmins != null) {
                ArrayList<BlockchainObject> modelList = new ArrayList();

                try {
                    SearchResult searchResult = reader.search("Model");
                    modelList = searchResult.getBlockchainObjects();

                } catch (ORMException ex) {
                    logger.error("Error searching Models: " + ex.toString());
                }

                for (int i = 0; i < modelList.size(); i++) {
                    Model model = (Model) modelList.get(i);
                    if (model.getDisplayName().equals("Menu")) {
                        model.put("ACL", new AttrIDList(aclUsers.getId()));
                    } else if (model.getDisplayName().equals("Preferences")) {
                        model.put("ACL", new AttrIDList(aclUsersOwn.getId(), aclAdmins.getId()));
                    } else {
                        model.put("ACL", new AttrIDList(aclAdmins.getId()));
                    }
                    try {
                        if (i != modelList.size() - 1) {
                            writer.write(model);
                        } else {
                            writer.write(model, false);
                        }
                    } catch (ORMException e) {
                        logger.error("Error adding admin ACL to base models: " + e.toString());
                    }
                }
            }
        }

        logger.debug("END initACLs()");
    }

    private void initMenu() {
        logger.debug("START initMenu()");

        BlockchainObjectReader reader = null;
        User admin = null;

        admin = User.getAdminUser();

        if (admin != null) {

            reader = BlockchainObjectReader.getInstance(admin);
            ArrayList<BlockchainObject> modelList = new ArrayList();

            try {
                SearchResult searchResult = reader.search("Model");
                modelList = searchResult.getBlockchainObjects();

            } catch (ORMException ex) {
                logger.error("Error searching Models: " + ex.toString());
            }

            BlockchainObjectWriter writer = BlockchainObjectWriter.getInstance(admin);
            Model preferences = null;

            for (BlockchainObject object : modelList) {
                Model model = (Model) object;
                if (model.getDisplayName().equals("Preferences")) {
                    preferences = model;
                }

                if (!model.isTransient()) {
                    try {

                        ACL aclAdmins = (ACL) reader.search("ACL", "Admins");

                        Menu menu = new Menu(model.getDisplayName());
                        menu.setPosition("Admin");
                        menu.setMenuModel(model.getId());
                        menu.setViewList(true);
                        menu.setViewForm(true);
                        menu.setViewTree(true);
                        menu.setViewKanban(true);
                        menu.setOwn(false);
                        menu.setVisible(true);
                        menu.setMenuACLs(aclAdmins.getId());

                        writer.write(menu);
                    } catch (ORMException ex) {
                        logger.error("Error creating menu: " + ex.toString());
                    }
                }
            }

            try {
                ACL aclUsers = (ACL) reader.search("ACL", "Users");

                Menu menu = new Menu("My Preferences");
                menu.setPosition("");
                menu.setMenuModel(preferences.getId());
                menu.setViewList(false);
                menu.setViewForm(false);
                menu.setViewTree(false);
                menu.setViewKanban(false);
                menu.setOwn(true);
                menu.setVisible(false);
                menu.setMenuACLs(aclUsers.getId());

                writer.write(menu, false);
            } catch (ORMException ex) {
                logger.error("Error creating menu: " + ex.toString());
            }
        }

        logger.debug("END initMenu()");
    }

    /*
    private void testReadWriteModel() {
        logger.debug("START testReadWriteModel()");

        BlockchainObject model = null;
        BlockchainObjectReader reader = null;
        try {
            reader = BlockchainObjectReader.getAdminInstance();

            //String modelUID = BlockchainObjectIndex.getInstance().modelListTreeMap.get("model").get(0);
            //long modelID = BlockchainObjectIndex.getInstance().highestUIDTreeMap.get(modelUID);
            model = reader.read(metamodelID);
            model.setModel("model");

            logger.debug("Read model: " + model.getDisplayName());

        } catch (ORMException ex) {
            logger.error("Error reading model: " + ex.toString());
        }

        try {

            URL url = this.webserver.getClassLoader().getResource("org/cheetah/webserver/resources/DataModelJSONSchema.json");
            String jsonSchema = readTextFileRessource(url, this.webserver.getClassLoader(), Charset.forName("utf-8"));

            //model.replace("Name", new AttrString("model"));
            model.replace("JSONSchema", new AttrString(jsonSchema));
            //model.put("Menu", new AttrString("model"));
            //model.replace("Java Class", new AttrString("BlockchainObject"));
            //model.replace("Immutable", new AttrBoolean(false));

            BlockchainObjectWriter writer = BlockchainObjectWriter.getAdminInstance();

            writer.write(model);
            logger.debug("JSON Array  : " + reader.readJson(model.getId()));

        } catch (Exception ex) {
            logger.error("Error writing model: " + ex.toString());
        }

        logger.debug("END testReadWriteModel()");
    }
    */

    public void testInsertBlock() {
        logger.debug("START testInsertBlock()");

        ECKey key = new ECKey();

        byte[] addr = key.getAddress();
        byte[] priv = key.getPrivKeyBytes();

        logger.debug("address : " + Hex.toHexString(addr));
        logger.debug("privateKey : " + Hex.toHexString(priv));

        BlockchainObjectReader reader = null;
        User admin = null;
        try {
            reader = BlockchainObjectReader.getAdminInstance();
            admin = User.getAdminUser();
        } catch (ORMException ex) {
            logger.error("Error reading 'admin' user: " + ex.toString());
        }
        if (admin != null) {

            User user1 = null;
            User user2 = null;

            try {
                user1 = new User("user1");
                user1.setFirstName("User 1");

                user2 = new User("user2");
                user2.setFirstName("User 2");

                BlockchainObjectWriter writer = BlockchainObjectWriter.getInstance(admin);

                user1.setId(writer.write(user1, false));
                user2.setId(writer.write(user2, false));

                Group groupUsers = (Group) reader.search("Group", "Users");

                //groupUsers.setModel("Group");
                AttrIDList users = (AttrIDList) groupUsers.getUsers();

                if (users != null) {
                    users.add(user1.getId(), user2.getId());
                } else {
                    groupUsers.setUsers(user1.getId(), user2.getId());
                }
                groupUsers.setId(writer.write(groupUsers, false));


            } catch (ORMException ex) {
                logger.error("Error writing users: " + ex.toString());
            }

            BlockchainObject box = null;
            try {
                box = new BlockchainObject();
                box.put("Name", new AttrString("testbox"));
                box.setModel("Box");

                box.put("Reseller", user1.getId());
                box.put("Quality MGT", user2.getId());

                BlockchainObjectWriter writer = BlockchainObjectWriter.getInstance(admin);

                writer.write(box, false);
            } catch (ORMException ex) {
                logger.error("Error writing box: " + ex.toString());
            }

            if (box != null) {
                try {

                    box = reader.read(box.getId());
                    logger.debug("Read Box: " + box.getDisplayName());

                    User reseller = (User) reader.read((AttrID) box.get("Reseller"));
                    User quality = (User) reader.read((AttrID) box.get("Quality MGT"));

                    logger.debug("reseller : " + reseller.getDisplayName());
                    logger.debug("quality  : " + quality.getDisplayName());
                } catch (ORMException ex) {
                    logger.error("Error reading box: " + ex.toString());
                }
            }
        }

        logger.debug("END testInsertBlock()");
    }

    private void testReadWriteBlock() {
        logger.debug("START testReadWriteBlock()");

        BlockchainObject box = null;
        BlockchainObjectReader reader = null;
        try {
            reader = BlockchainObjectReader.getAdminInstance();

            BlockchainIndex<String, ArrayList<String>> indexActive = BlockchainObjectIndex.getInstance().getIndex(BlockchainObjectIndex.INDEX_TYPE.VIRTUAL_TABLES_ACTIVE);
            String boxUID = indexActive.get("Box").get(0);

            BlockchainIndex<String, String> currentVersionsIndex = BlockchainObjectIndex.getInstance().getIndex(BlockchainObjectIndex.INDEX_TYPE.CURRENT_VERSIONS);
            String boxID = currentVersionsIndex.get(boxUID);
            box = reader.read(boxID);

            logger.debug("Read Box: " + box.getDisplayName());

            User reseller = (User) reader.read((AttrID) box.get("Reseller"));
            User quality = (User) reader.read((AttrID) box.get("Quality MGT"));

            logger.debug("reseller : " + reseller.getDisplayName());
            logger.debug("quality  : " + quality.getDisplayName());

            logger.debug("JSON Array  : " + reader.readJson(box.getId()));

        } catch (ORMException ex) {
            logger.error("Error reading box: " + ex.toString());
        }

        try {
            box.replace("Name", new AttrString("new box"));
            BlockchainObjectWriter writer = BlockchainObjectWriter.getAdminInstance();

            logger.debug("Box ID before write  : " + box.getId().toString());
            logger.debug("Box Parent ID before write  : " + box.getParentList().toString());
            writer.write(box, false);
            logger.debug("Box ID after  write  : " + box.getId().toString());
            logger.debug("JSON Array  : " + reader.readJson(box.getId()));

        } catch (ORMException ex) {
            logger.error("Error writing box: " + ex.toString());
        }

        logger.debug("END testReadWriteBlock()");
    }

    private void testNewBox() {
        logger.debug("START testNewBox()");
        BlockchainObject box = null;
        BlockchainObjectReader reader = null;
        BlockchainObjectWriter writer = null;

        try {

            reader = BlockchainObjectReader.getAdminInstance();
            User admin = User.getAdminUser();
            writer = BlockchainObjectWriter.getInstance(admin);

            box = new BlockchainObject();
            box.put("Name", new AttrString("test box"));
            box.setModel("Box");

            writer.write(box);

        } catch (ORMException ex) {
            logger.error("Error writing box: " + ex.toString());
        }

        logger.debug("END testNewBox()");
    }

    private void testDelete() {
        logger.debug("START testDelete()");

        BlockchainObject box = null;
        BlockchainObjectReader reader = null;
        try {
            reader = BlockchainObjectReader.getAdminInstance();

            BlockchainIndex<String, ArrayList<String>> indexActive = BlockchainObjectIndex.getInstance().getIndex(BlockchainObjectIndex.INDEX_TYPE.VIRTUAL_TABLES_ACTIVE);
            String boxUID = indexActive.get("Box").get(0);

            BlockchainIndex<String, String> currentVersionsIndex = BlockchainObjectIndex.getInstance().getIndex(BlockchainObjectIndex.INDEX_TYPE.CURRENT_VERSIONS);
            String boxID = currentVersionsIndex.get(boxUID);
            box = reader.read(boxID);

            reader = BlockchainObjectReader.getAdminInstance();
            User admin = User.getAdminUser();
            BlockchainObjectWriter writer = BlockchainObjectWriter.getAdminInstance();

            writer.delete(box);
            logger.debug("JSON Array  : " + reader.readJson(box.getId()));

        } catch (ORMException ex) {
            logger.error("Error writing box: " + ex.toString());
        }

        logger.debug("END testDelete()");
    }

    private void insertBoxes(int count) {
        logger.debug("START insertBoxes()");
        logger.debug("=============================================================");
        logger.debug("INSERT BOXES");

        BlockchainObject box = null;
        BlockchainObjectReader reader = null;
        BlockchainObjectWriter writer = null;

        try {

            reader = BlockchainObjectReader.getAdminInstance();
            User admin = User.getAdminUser();
            writer = BlockchainObjectWriter.getInstance(admin);

            for (int i = 1; i <= count; i++) {
                logger.debug("CREATE box (" + i + ")");
                box = new BlockchainObject();
                box.put("Name", new AttrString("box (" + i + ")"));
                box.setModel("Box");

                writer.write(box, true);
            }

        } catch (ORMException ex) {
            logger.error("Error writing box: " + ex.toString());
        }

        logger.debug("END insertBoxes()");
    }

    private void printBlockchain() {
        logger.trace("START printBlockchain()");

        if (logger.isDebugEnabled() || logger.isTraceEnabled()) {

            Blockchain blockchain = ethereum.getBlockchain();

            long blocknumber = blockchain.getBestBlock().getNumber();
            StringBuilder chainString = new StringBuilder();
            chainString.append("[");

            logger.debug("=============================================================");
            logger.debug("READ BLOCKCHAIN");

            for (long i = 0; i <= blocknumber; i++) {
                Block block = blockchain.getBlockByNumber(i);

                String hash = Hex.toHexString(block.getHash());
                logger.debug("BLOCK '" + block.getNumber() + "': " + hash);
                for (Transaction tx : block.getTransactionsList()) {
                    logger.debug("Transaction '" + new String(tx.getData()) + "'");

                    try {
                        BlockchainObjectWriter writer = BlockchainObjectWriter.getGuestInstance();
                        BlockchainObject object = writer.getObject(new String(tx.getData()));
                        JSONArray jsonArray = BlockchainObject.getJSON(object);

                        ((JSONObject) jsonArray.get(1)).put("id", i + "." + Hex.toHexString(tx.getHash()) + "." + (String) ((JSONObject) jsonArray.get(1)).get("uid"));

                        logger.debug("Object toString: '" + jsonArray.toJSONString() + "'");

                        chainString.append(jsonArray.toJSONString());
                        if (i != blocknumber) {
                            chainString.append(",").append(System.lineSeparator());
                        }
                    } catch (ORMException ex) {
                        java.util.logging.Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }

            chainString.append("]");

            /*
            logger.debug("=============================================================");
            logger.debug("Entire ChainJSON: '" + chainString.toString() + "'");
            logger.debug("=============================================================");
            */
        }

        logger.trace("END printBlockchain()");
    }

    public void printJSONArray(BlockchainObject object) {
        logger.debug("START printJSONArray()");

        ObjectMapper mapper = new ObjectMapper().enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        String jsonString = "";
        try {
            jsonString = mapper.writeValueAsString(object);
        } catch (Exception ex) {
            logger.error("Error processing Object to JSON : " + ex.toString());
        }

        logger.debug("Object to JSON : " + jsonString);

        logger.debug("END printJSONArray()");
    }

    public static String readTextFileRessource(URL url, ClassLoader classLoader, Charset charset) throws FileNotFoundException, IOException, URISyntaxException {
        logger.trace("START readTextFileRessource()");

        StringBuilder builder = new StringBuilder();
        String fileName = url.toString();

        BufferedReader br = null;
        InputStream in = null;

        if (fileName.startsWith("jar")) {
            fileName = fileName.substring(fileName.lastIndexOf("!") + 1).substring(1);

            in = classLoader.getResourceAsStream(fileName);
        } else {
            fileName = fileName.substring(fileName.lastIndexOf(":") + 1);

            in = new FileInputStream(fileName);
        }

        InputStreamReader is = new InputStreamReader(in, charset);
        br = new BufferedReader(is);

        String line = "";
        line = br.readLine();

        while (line != null) {
            builder.append(line + System.getProperty("line.separator"));
            line = br.readLine();
        }

        in.close();

        logger.trace("END readTextFileRessource()");
        return builder.toString();
    }

    public JSONObject getNodeStatus() {
        logger.trace("START getNodeStatus()");
        JSONObject result = new JSONObject();

        SyncStatus syncStatus = ethereum.getSyncStatus();

        if (syncStatus.getStage().equals(SyncStatus.SyncStage.Complete)) {
            syncComplete = true;
        }

        result.put("SyncComplete", syncComplete);
        result.put("DatasetReady", datasetReady);
        result.put("BlockBestKnown", syncStatus.getBlockBestKnown());
        result.put("BlockLastImported", lastImported);

        if (!datasetReady && syncComplete) {
            if (datasetGenerateStart != null) {

                long percent = 0;
                long startTimestamp = DateFormat.DatetoLong(datasetGenerateStart);
                long endTimestamp = DateFormat.DatetoLong(datasetGenerateStart.plusMinutes(12));
                long nowTimestamp = LocalDateTime.now(ZoneOffset.UTC).toInstant(ZoneOffset.UTC).toEpochMilli();

                if (!config.isMineFullDataset()) {
                    DateFormat.DatetoLong(datasetGenerateStart.plusSeconds(30));
                }

                long diff = endTimestamp - startTimestamp; //100
                long now = nowTimestamp - startTimestamp; //x

                percent = now * 100 / diff;

                if (percent < 100) {
                    result.put("Percent", percent);
                } else {
                    result.put("Percent", -1);
                }
            } else {
                result.put("Percent", -1);
            }
        } else if (!syncComplete) {

            long percent = 0;
            long end = syncStatus.getBlockBestKnown();
            long now = lastImported;

            if (end == 0) {
                percent = 0;
            } else {
                percent = now * 100 / end;
            }

            //        logger.debug("percent: " + percent);
            if (percent < 100) {
                result.put("Percent", percent);
            } else {
                result.put("Percent", -1);
            }
        } else {
            result.put("Percent", 100);
        }

        result.put("PrimitiveAttributes", getPrimitiveAttributes());
        result.put("SearchLimitMax", ApplicationContext.getInstance().getInteger("NodeObjectSearchResultMax"));
        try {
            result.put("IndexList", BlockchainObjectIndex.getInstance().getIndexList());
        } catch (ORMException ex) {
            logger.error("Error getting index list: " + ex.toString());
        }

        result.put("AttrID_REGEX_PATTERN", AttrID.REGEX_PATTERN);

        result.put("NodeID", Hex.toHexString(config.nodeId()));

        result.put("InitFlag", Main.initDataModelFlag || Main.initFlag);

        if (!datasetReady || !syncComplete || Main.initDataModelFlag || Main.initFlag) {

            Thread t2 = new Thread() {
                @Override
                public void run() {

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ex) {
                    }

                    logger.trace("datasetReady: " + datasetReady);
                    logger.trace("syncComplete: " + syncComplete);
                    logger.trace("initDataModelFlag: " + initDataModelFlag);
                    logger.trace("initFlag: " + initFlag);

                    sendJSONStatus();
                }
            };
            t2.start();
        }

        logger.trace("END getNodeStatus()");
        return result;
    }

    public static void sendWebsocket(String message, User user) {

        if (!CheetahWebserver.getInstance().isSessionAuthenticationEnabled()) {
            CheetahWebserver.getInstance().distributeToWebsocketServiceMessage("org.cheetah.webserver.page.websocket.Recordin", message, user.getSessionCookie());
        } else {
            CheetahWebserver.getInstance().distributeToWebsocketServiceMessage("org.cheetah.webserver.page.websocket.Recordin", message, user.getLogin());
        }
    }

    public static JSONObject getPrimitiveAttributes() {
        JSONObject result = new JSONObject();

        for (Class c : getClassesFromPackage("com.recordins.recordin.orm.attribute", CheetahWebserver.getInstance().getClassLoader())) {

            if (AttrAbstractAttachment.class.isAssignableFrom(c)) {
                result.put(c.getSimpleName(), AttrAbstractAttachment.class.getSimpleName());
            } else if (AttrBoolean.class.isAssignableFrom(c)) {
                result.put(c.getSimpleName(), AttrBoolean.class.getSimpleName());
            } else if (AttrDateTime.class.isAssignableFrom(c)) {
                result.put(c.getSimpleName(), AttrDateTime.class.getSimpleName());
            } else if (AttrID.class.isAssignableFrom(c)) {
                result.put(c.getSimpleName(), AttrID.class.getSimpleName());
            } else if (AttrIDList.class.isAssignableFrom(c)) {
                result.put(c.getSimpleName(), AttrIDList.class.getSimpleName());
            } else if (AttrList.class.isAssignableFrom(c)) {
                result.put(c.getSimpleName(), AttrList.class.getSimpleName());
            } else if (AttrMap.class.isAssignableFrom(c)) {
                result.put(c.getSimpleName(), AttrMap.class.getSimpleName());
            } else if (AttrPassword.class.isAssignableFrom(c)) {
                result.put(c.getSimpleName(), AttrPassword.class.getSimpleName());
            } else if (Number.class.isAssignableFrom(c)) {
                result.put(c.getSimpleName(), Number.class.getSimpleName());
            } else {
                result.put(c.getSimpleName(), AttrString.class.getSimpleName());
            }
        }

        return result;
    }
}
