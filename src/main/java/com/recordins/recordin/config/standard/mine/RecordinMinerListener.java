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

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.recordins.recordin.config.standard.mine;

import static com.recordins.recordin.config.standard.listener.RecordinEthereumListener.sendJSONStatus;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

import com.recordins.recordin.ApplicationContext;
import com.recordins.recordin.config.standard.listener.RecordinEthereumListener;
import com.recordins.recordin.Main;
import org.ethereum.config.SystemProperties;
import org.ethereum.core.Block;
import org.ethereum.core.Transaction;
import org.ethereum.facade.Ethereum;
import org.ethereum.util.ByteUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;
import org.ethereum.mine.EthashListener;

/**
 * @author in'teractive SAS - Philippe Schweitzer - 2018 - www.in-teractive.com
 */
public class RecordinMinerListener implements EthashListener {

    public static final Logger logger = LoggerFactory.getLogger(RecordinMinerListener.class);

    //@Autowired not working
    private Ethereum ethereum = (Ethereum) ApplicationContext.getInstance().get("ethereum");

    //@Autowired not working
    private SystemProperties config = (SystemProperties) ApplicationContext.getInstance().get("config");

    private boolean alreadyPrepared = false;

    //@Autowired not working ? -> to test
    //private Ethereum ethereum = (Ethereum) ApplicationContext.getInstance().get("ethereum");

    /*
    public RecordinMinerListener() {
        super();
    }
     */
    @Override
    public void miningStarted() {
        logger.debug("===   Mining Started");
        //Main.getInstance().startPlatform();
    }

    @Override
    public void miningStopped() {
        logger.debug("===   Mining Stopped");
    }

    @Override
    public void blockMiningStarted(Block block) {
        logger.debug("===   BLOCK Mining Started: " + blockInfo(block));
    }

    @Override
    public void blockMined(Block block) {
        logger.debug("===   BLOCK Mined: " + blockInfo(block));

        //RecordinEthereumListener ethereumListener = (RecordinEthereumListener) ApplicationContext.getInstance().get("ethereumListener");

        //ethereumListener.onBlock(block);

        //RecordinBlockMiner.isMining = false;

        /*
        for (Transaction transaction : block.getTransactionsList()) {
            //logger.debug("Transaction : " + transaction.toString());
            //byte[] object = block.getTransactionsList().get(0).getData();
            String jsonString = new String(transaction.getData());

            logger.debug("Read JSON for blockID '" + block.getNumber() + "': " + jsonString);
        }
        */
    }

    @Override
    public void blockMiningCanceled(Block block) {
        logger.debug("===   BLOCK Mining Canceled: " + blockInfo(block));
    }

    @Override
    public void onDatasetUpdate(DatasetStatus ds) {
        logger.debug("===   BLOCK Dataset Update: " + ds);

        if (ds == DatasetStatus.DATASET_PREPARE) {
            if (!alreadyPrepared) {
                Main.datasetGenerateStart = LocalDateTime.now(ZoneOffset.UTC);
                Main.datasetReady = false;

                sendJSONStatus();
            }
            alreadyPrepared = true;
        }

        if (config.isMineFullDataset()) {
            if (ds == DatasetStatus.FULL_DATASET_LOADED || ds == DatasetStatus.FULL_DATASET_GENERATED) {
                Main.datasetReady = true;
            }
        } else {
            if (ds == DatasetStatus.LIGHT_DATASET_LOADED || ds == DatasetStatus.LIGHT_DATASET_GENERATED) {
                Main.datasetReady = true;
            }
        }
    }

    private String blockInfo(Block b) {
        logger.trace("START blockInfo(Block)");
        boolean ours = Hex.toHexString(b.getExtraData()).startsWith("cccccccccc");
        String txs = "Tx[";
        for (Transaction tx : b.getTransactionsList()) {
            txs += ByteUtil.byteArrayToLong(tx.getNonce()) + ", ";
        }
        txs = txs.substring(0, txs.length() - 2) + "]";

        logger.trace("END blockInfo()");
        return (ours ? "##" : "  ") + b.getShortDescr() + " " + txs;
    }
}
