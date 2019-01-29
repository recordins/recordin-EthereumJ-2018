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
//public abstract class PrivateNetworkConfig  extends DefaultConfig{
public abstract class PrivateNetworkConfig {

    public static final Logger logger = LoggerFactory.getLogger(PrivateNetworkConfig.class);

    public PrivateNetworkConfig() {
        logger.trace("START PrivateNetworkConfig()");
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> logger.error("Uncaught exception", e));
        logger.trace("END PrivateNetworkConfig()");
    }

    private SystemProperties props = null;

    private final String config
            = // no need for discovery in that small network
            "peer.discovery.enabled = false \n"
                    + "peer.discovery.bind.ip = " + ApplicationContext.getInstance().getString("bind.ip") + " \n"
                    + "peer.discovery.external.ip = " + ApplicationContext.getInstance().getString("bind.ip") + " \n"
                    + "peer.listen.port = 40000 \n"
                    // our private net ID
                    + "peer.networkId = 4444 \n"
                    // we have no activePeers to sync with
                    + "sync.enabled = true \n"
                    + "sync.makeDoneByTimeout = " + ApplicationContext.getInstance().getInteger("NodeSyncTimeout") + " \n"
                    // genesis with a lower initial difficulty and some predefined known funded accounts
                    //+ "genesis = recordin-private-network-genesis.json \n"
                    + "genesis = recordin-genesis.json \n"
                    // two activePeers need to have separate database dirs
                    + "database.dir = database-recordin-private-network-standard \n"
                    + "ethash.dir = dataset-recordin-standard \n"
                    //+ "blockchain.config.name = 'casper' \n"
                    + "mine.start = false \n"
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
            logger.debug("Building '" + PrivateNetworkConfig.class.getSimpleName() + "' config");
            //SystemProperties props = new SystemProperties();
            props = SystemProperties.getDefault();
            props.overrideParams(ConfigFactory.parseString(config.replaceAll("'", "\"")));

            //logger.trace("Config BEFORE :" + props.dump());
            ApplicationContext applicationContext = ApplicationContext.getInstance();
            StringBuilder applicationContextString = new StringBuilder();

            // peer.active
            // { url = "enode://<hex nodeID>@hostname.com:30303" }
            // { url = "enode://0480fbb64c462a370ad4a2a58139dbf5645d67989d1474dbccd2727de3c62547845b486df92be9cb12427870003f80b2473b8850f167928752d82dce862a097c@10.0.2.1:50000" }
            //
            applicationContextString.append("mine.start=" + applicationContext.getString("MiningEnabled") + "\n");
            applicationContextString.append("mine.fullDataSet=" + applicationContext.getString("MiningFullDataSet") + "\n");
            applicationContextString.append("peer.active=" + applicationContext.getString("PeerActive") + "\n");
            applicationContextString.append("peer.listen.port=" + applicationContext.getString("PeerListenPort") + "\n");
            applicationContextString.append("peer.networkId=" + applicationContext.getString("PeerNetworkId") + "\n");

            props.overrideParams(ConfigFactory.parseString(applicationContextString.toString().replaceAll("'", "\"")));

            logger.trace("****************************************************************************************************");
            logger.trace("Ethereum Properties: \n" + props.dump());
            logger.trace("****************************************************************************************************");
        }


        logger.trace("END systemProperties()");
        return props;
    }
}
