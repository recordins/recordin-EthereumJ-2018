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

package com.recordins.recordin.config.standard;

import com.recordins.recordin.ApplicationContext;
import com.recordins.recordin.Main;
import com.typesafe.config.ConfigFactory;
import org.ethereum.config.CommonConfig;
import org.ethereum.config.DefaultConfig;
import org.ethereum.config.NoAutoscan;
import org.ethereum.config.SystemProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;

@Configuration
@ComponentScan(
        basePackages = "org.ethereum",
        excludeFilters = {
                @ComponentScan.Filter(NoAutoscan.class)
                ,
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = {CommonConfig.class, DefaultConfig.class})},
        includeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = {RecordinBeanConfig.class})}
)
@Import(RecordinBeanConfig.class)
//public abstract class StandaloneConfig extends DefaultConfig {
public abstract class StandaloneConfig {

    private static final Logger logger = LoggerFactory.getLogger(StandaloneConfig.class);

    @Autowired
    RecordinBeanConfig beanConfig;

    public StandaloneConfig() {
        logger.trace("START StandaloneConfig()");
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> logger.error("Uncaught exception", e));
        logger.trace("END StandaloneConfig()");
    }

    private SystemProperties props = null;

    private final String configString
            = // no need for discovery in that small network
            "peer.discovery.enabled = false \n"
                    + "peer.discovery.bind.ip = " + ApplicationContext.getInstance().getString("bind.ip") + " \n"
                    + "peer.discovery.external.ip = " + ApplicationContext.getInstance().getString("bind.ip") + " \n"
                    + "peer.listen.port = 0 \n"
                    // our private net ID
                    + "peer.networkId = 444 \n"
                    // must be true even in standalone becaseu it freezes...
                    + "sync.enabled = true \n"
                    + "sync.makeDoneByTimeout = 0 \n"
                    // genesis with a lower initial difficulty and some predefined known funded accounts
                    //+ "genesis = recordin-standalone-genesis.json \n"
                    + "genesis = recordin-genesis.json \n"
                    // two activePeers need to have separate database dirs
                    + "database.dir = database-recordin-standalone-standard \n"
                    + "ethash.dir = dataset-recordin-standard \n"
                    //+ "blockchain.config.name = 'main' \n"
                    + "mine.start = true \n"
                    // when more than 1 miner exist on the network extraData helps to identify the block creator
                    + "mine.extraDataHex = " + Hex.toHexString(new byte[]{0}) + " \n"
                    + "mine.cpuMineThreads = 2 \n"
                    + "mine.minGasPrice = 150 \n"
                    + "mine.fullDataSet = true \n"
                    + "database.reset = false \n"
                    + "cache.flush.blocks = 1000";

    public abstract Main sampleBean();

    @Bean
    public SystemProperties systemProperties() {
        logger.trace("START systemProperties()");

        if (props == null) {
            logger.debug("Building '" + StandaloneConfig.class.getSimpleName() + "' config");
            props = SystemProperties.getDefault();
            props.overrideParams(ConfigFactory.parseString(configString.replaceAll("'", "\"")));

            ApplicationContext applicationContext = ApplicationContext.getInstance();
            StringBuilder applicationContextString = new StringBuilder();

            applicationContextString.append("mine.fullDataSet=" + applicationContext.getString("MiningFullDataSet") + "\n");
            props.overrideParams(ConfigFactory.parseString(applicationContextString.toString().replaceAll("'", "\"")));
        }

        logger.trace("****************************************************************************************************");
        logger.trace("Ethereum Properties: \n" + props.dump());
        logger.trace("****************************************************************************************************");

        logger.trace("END systemProperties()");
        return props;
    }
}
