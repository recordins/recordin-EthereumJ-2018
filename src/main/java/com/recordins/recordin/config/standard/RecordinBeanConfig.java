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

import com.recordins.recordin.config.standard.core.RecordinBlockchainImpl;
import com.recordins.recordin.config.standard.listener.RecordinEthereumListener;
import com.recordins.recordin.config.standard.mine.RecordinBlockMiner;
import org.ethereum.config.DefaultConfig;
import org.ethereum.config.SystemProperties;
import org.ethereum.core.Blockchain;
import org.ethereum.core.BlockchainImpl;
import org.ethereum.core.PendingState;
import org.ethereum.core.PendingStateImpl;
import org.ethereum.listener.CompositeEthereumListener;
import org.ethereum.listener.EthereumListener;
import org.ethereum.mine.BlockMiner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RecordinBeanConfig extends DefaultConfig {

    public static final Logger logger = LoggerFactory.getLogger(RecordinBeanConfig.class);

    @Autowired
    protected EthereumListener ethereumListener;

    @Autowired
    SystemProperties config;

    private Blockchain blockchain = null;

    /*
        @Override
        @Bean
        public Blockchain blockchain() {
            return new CasperBlockchain(systemProperties());
        }

        @Bean
        @Override
        public WorldManager worldManager() {
            return new CasperWorldManager(systemProperties(), repository(), blockchain());
        }

        @Bean
        @Override
        public PendingState pendingState() {
            return new CasperPendingStateImpl(ethereumListener);
        }
    */
    @Bean(name = "EthereumListener")
    public CompositeEthereumListener ethereumListener() {
        return new RecordinEthereumListener();
    }

    @Bean
//    @Override
    public BlockMiner blockMiner() {
        logger.trace("START blockMiner()");

        BlockchainImpl blockchain = (BlockchainImpl) blockchain();


        //return new RecordinBlockMiner(systemProperties(), (CompositeEthereumListener) ethereumListener,
        //        blockchain, blockchain.getBlockStore(), pendingState());

        logger.trace("END blockMiner()");
        return new RecordinBlockMiner(config, (CompositeEthereumListener) ethereumListener,
                blockchain, blockchain.getBlockStore(), pendingState());
    }
/*
    @Bean
    @Override
    public SystemProperties systemProperties() {
        return CasperProperties.getDefault();
    }
*/

    @Bean
    public BlockchainImpl blockchainImpl() {
        logger.trace("START blockchainImpl()");
        logger.trace("END blockchainImpl()");
        return (BlockchainImpl) blockchain();
    }

    @Bean
    public Blockchain blockchain() {
        logger.trace("START blockchain()");
        //return new BlockchainImpl(systemProperties());
        if (blockchain == null) {
            config.dump();
            //blockchain = new BlockchainImpl(config);
            blockchain = new RecordinBlockchainImpl(config);
        }
        logger.trace("END blockchain()");
        return blockchain;
    }

    @Bean
    public PendingState pendingState() {
        logger.trace("START pendingState()");
        logger.trace("END pendingState()");
        return new PendingStateImpl(ethereumListener);
    }
}
