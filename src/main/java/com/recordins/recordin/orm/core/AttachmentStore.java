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
import com.recordins.recordin.orm.exception.ORMException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.stream.Stream;

import org.ethereum.config.SystemProperties;
import org.ethereum.facade.Ethereum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AttachmentStore {

    /* Logger for console output */
    private static final Logger logger = LoggerFactory.getLogger(AttachmentStore.class);

    private static final AttachmentStore instance = new AttachmentStore();

    //@Autowired not working
    private Ethereum ethereum = (Ethereum) ApplicationContext.getInstance().get("ethereum");

    //@Autowired Not working
    private SystemProperties config = (SystemProperties) ApplicationContext.getInstance().get("config");

    private Path rootFolderPath = null;

    private static long MAX_FILE_FOLDER = (new Double(Math.pow(2, 30) - 1)).longValue(); // NTFS is 2^32-1 becareful Fat32 is 65535 only...

    private AttachmentStore() {
        logger.trace("START AttachmentStore()");

        rootFolderPath = Paths.get(config.databaseDir());
        rootFolderPath = rootFolderPath.resolve("AttachmentStore");

        try {
            Files.createDirectories(rootFolderPath);
        } catch (IOException ex) {
            logger.error("Error creating AttachmentStore root directory: " + ex.toString());
        }

        logger.trace("END AttachmentStore()");
    }

    /**
     * Returns an instance of {@link BlockchainObjectIndex}
     *
     * @return
     */
    public static AttachmentStore getInstance() throws ORMException {
        logger.trace("START getInstance()");
        logger.trace("END getInstance()");
        return instance;
    }

    public Path getRootPath() {
        logger.trace("START getRootPath()");
        logger.trace("END getRootPath()");
        return rootFolderPath;
    }

    public Path getTempPath() {
        logger.trace("START getTempPath()");
        logger.trace("END getTempPath()");

        Path tempPath = rootFolderPath.resolve("temp");
        if (!Files.exists(tempPath)) {
            try {
                Files.createDirectories(tempPath);
            } catch (IOException ex) {
                logger.error("Error creating temp directory: " + ex.toString());
            }
        }

        return tempPath;
    }

    public Path getStorePath(String uid) {
        logger.trace("START getStorePath(String)");
        Path result = null;

        try {
            Stream<Path> stream = Files.find(rootFolderPath, 2,
                    (path, basicFileAttributes) -> {
                        File file = path.toFile();
                        return file.isDirectory() && file.getName().equals(uid);
                    });
            Iterator<Path> streamIterator = stream.iterator();

            while (streamIterator.hasNext()) {
                result = streamIterator.next();
                break;
            }
            logger.trace("Found path result: " + result);

        } catch (IOException ex) {
            logger.error("Error getting root directory for UID '" + uid + "': " + ex.toString());
        }

        if (result == null) {
            result = getNewStorePath(uid);
        }

        logger.trace("END getStorePath(String)");
        return result;
    }

    private Path getNewStorePath(String uid) {
        logger.trace("START getNewStorePath(String)");
        Path result = rootFolderPath.resolve(uid);

        try {
            Stream<Path> folders = Files.list(rootFolderPath);
            long count = folders.count();

            if (count > 0) {
                count--;
                if (count > 0) {
                    if (Files.exists(this.getTempPath())) {
                        if (this.getTempPath().startsWith(rootFolderPath)) {
                            count--;
                        }
                    }
                }
            }

            Path folder = rootFolderPath.resolve("store-" + count);
            if (!Files.exists(folder)) {
                Files.createDirectories(folder);
            } else {
                Stream<Path> innerUIDFolders = Files.list(folder);
                if (innerUIDFolders.count() >= MAX_FILE_FOLDER) {
                    count++;
                    folder = rootFolderPath.resolve("store-" + count);
                    Files.createDirectories(folder);
                }
            }

            result = folder.resolve(uid);
            Files.createDirectories(result);

        } catch (IOException ex) {
            logger.error("Error creating root directory for UID '" + uid + "': " + ex.toString());
        }

        logger.trace("END getNewStorePath()");
        return result;
    }
}
