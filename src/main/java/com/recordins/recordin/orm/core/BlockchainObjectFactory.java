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

import com.recordins.recordin.ApplicationContext;
import com.recordins.recordin.orm.BlockchainObject;
import com.recordins.recordin.orm.attribute.AttrPlatformVersion;
import com.recordins.recordin.orm.exception.ORMException;
import com.recordins.recordin.orm.core.objectfactory.FactoryPlatform;
import com.recordins.recordin.orm.core.objectfactory.FactoryPlatform_1_0;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import com.recordins.recordin.utils.DeepCopy;
import org.cheetah.webserver.CheetahClassLoader;
import org.cheetah.webserver.CheetahWebserver;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BlockchainObjectFactory {

    /* Logger for console output */
    private static final Logger logger = LoggerFactory.getLogger(BlockchainObjectFactory.class);

    private JSONArray jsonArray = null;
    private BlockchainObject blockchainObject = null;

    public static BlockchainObjectFactory getInstance(JSONArray jsonArray) {
        return new BlockchainObjectFactory(jsonArray);
    }

    private BlockchainObjectFactory() {
    }

    public BlockchainObjectFactory(JSONArray jsonArray) {
        logger.trace("START BlockchainObjectFactory(JSONArray)");
        this.jsonArray = jsonArray;
        logger.trace("END BlockchainObjectFactory()");
    }

    public BlockchainObject getBlockchainObject() throws ORMException {
        logger.trace("START getBlockchainObject()");
        BlockchainObject result = null;

        if (blockchainObject == null) {

            /* look for factory class depending on platform version */
            String platformVersion = readPlatformVersion();
            String defaultPlatformVersion = getDefaultPlatformVersion();
            String factoryPackage = "com.recordins.recordin.orm.core.objectfactory";

            ApplicationContext applicationContext = ApplicationContext.getInstance();

            if (applicationContext.containsKey("NodeDefaultPlatformVersion")) {
                defaultPlatformVersion = applicationContext.getString("NodeDefaultPlatformVersion").replaceAll("\\.", "_");
            }
            CheetahClassLoader cl = CheetahWebserver.getInstance().getClassLoader();

            FactoryPlatform factoryPlatform = null;
            Class c = null;

            switch (platformVersion) {

                case "1_0":
                    logger.trace("platformVersion: " + platformVersion);

                {
                    try {
                        c = cl.loadClass(factoryPackage + ".FactoryPlatform_" + platformVersion);
                        break;
                    } catch (ClassNotFoundException ex) {
                        logger.error("Error loading FactoryPlatform Class: " + ex.toString());
                    }
                }

                default:
                    logger.debug("platformVersion DEFAULT: " + defaultPlatformVersion);
                {
                    try {
                        c = cl.loadClass(factoryPackage + ".FactoryPlatform_" + defaultPlatformVersion);
                    } catch (ClassNotFoundException ex) {
                        logger.error("Error loading DefaultFactoryPlatform Class: " + ex.toString());
                    }
                }
            }

            if (c == null) {
                factoryPlatform = new FactoryPlatform_1_0();
            } else {
                try {
                    factoryPlatform = (FactoryPlatform) c.newInstance();
                } catch (Exception ex) {
                    logger.error("Error instanciating FactoryPlatform Class: " + ex.toString());
                    factoryPlatform = new FactoryPlatform_1_0();
                }
            }

            logger.trace("FactoryPlatform version: " + factoryPlatform.getClass().getSimpleName());

            factoryPlatform.setJsonArray(jsonArray);
            blockchainObject = factoryPlatform.getObject();
            result = blockchainObject;

        } else {
            result = blockchainObject;
        }

        logger.trace("END getBlockchainObject()");
        return (BlockchainObject) DeepCopy.copy(result);
    }

    private String readPlatformVersion() {
        logger.trace("START readPlatformVersion()");
        String result = "";

        if (jsonArray != null) {

            JSONObject jsonObject = (JSONObject) jsonArray.get(1);

            if (jsonObject.containsKey("platformVersion")) {
                result = ((JSONArray) jsonObject.get("platformVersion")).get(1).toString();
            }
        }

        logger.trace("END readPlatformVersion()");
        return result.replaceAll("\\.", "_");
    }

    private String getDefaultPlatformVersion() {
        logger.trace("START getDefaultPlatformVersion()");
        String result = "1.0";
        Properties properties = new Properties();
        InputStream resourceStream = AttrPlatformVersion.class.getResourceAsStream("/com/recordins/recordin/orm/platform_version.properties");

        try {
            properties.load(resourceStream);
            result = properties.getProperty("PlatformVersion");
        } catch (IOException ex) {
            logger.error("Error loading Platform Version properties file: " + ex.toString());
        }

        logger.trace("END getDefaultPlatformVersion()");
        return result;
    }
}
