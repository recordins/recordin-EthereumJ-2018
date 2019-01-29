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

package com.recordins.recordin.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class BlockchainLock<T> {


    private static Logger logger = LoggerFactory.getLogger(BlockchainLock.class);
    private final ConcurrentHashMap<T, ConcurrentLinkedQueue<Long>> cookieQueues = new ConcurrentHashMap();

    public BlockchainLock() {

    }


    /**
     * Generates a lockCookie for a particular Thread.
     * <br>
     * <br>
     * Method is synchronized, so only one Thread (the very first coming) can
     * ask for a row record lock at a time, otherwise, locking system cannot
     * every time control which is the first to put into the record row lock
     * queue.
     *
     * @param recordID Record ID to get the lock
     * @return A cookie value that must be used when the record is unlocked,
     * updated, or deleted.
     */
    public synchronized Long addToQueue(T recordID) {

        Random random = new Random();

        Long lockCookie = random.nextLong();

        ConcurrentLinkedQueue<Long> cookieQueue = cookieQueues.get(recordID);

        if (cookieQueue == null) {
            cookieQueue = new ConcurrentLinkedQueue();
            cookieQueues.put(recordID, cookieQueue);
        }

        logger.trace("Thread " + lockCookie + " asks lock for: " + recordID);
        cookieQueue.add(lockCookie);

        return lockCookie;
    }

    /**
     * Locks a record so that it can only be updated or deleted by current
     * Thread. If the specified record is already locked by a different Thread,
     * the current Thread gives up the CPU and consumes no CPU cycles until the
     * record is unlocked.
     *
     * @param recordID Record ID to get the lock
     * @return A cookie value that must be used when the record is unlocked,
     * updated, or deleted
     */
    public long lock(T recordID) {
        logger.trace("START lock(T)");

        Long lockCookie = addToQueue(recordID);

        ConcurrentLinkedQueue<Long> cookieQueue = cookieQueues.get(recordID);

        if (cookieQueue.peek() != null) {

            if (!lockCookie.equals(cookieQueue.peek())) {
                logger.trace("Thread " + lockCookie + "           for: " + recordID + " wait");

                synchronized (lockCookie) {
                    try {
                        lockCookie.wait();
                    } catch (InterruptedException e) {

                        // Remove of the lockCookie inside the queue if waiting Thread is interrupted.
                        // Otherwise there will be zombie Thread attempts remaining in queue which will
                        // never unlock access for new comming Threads.
                        cookieQueue.remove(lockCookie);
                        logger.error("Thread " + lockCookie + "           for: " + recordID + " interrupted while waiting for lockCookie", e);
                    }
                }
            } else {
                logger.trace("Thread " + lockCookie + " gets lock for: " + recordID);
            }
        }

        logger.trace("END lock(T)");
        return lockCookie;
    }

    /**
     * Releases the lock on a record. lockCookie parameter must be the cookie
     * returned to the Thread when the record lock lockCookie was requested.
     * <br>
     * If another lockCookie is available in the record row ID queue, core
     * gives the lock to the next Thread awaiting in the queue.
     *
     * @param recordID   Record ID to update
     * @param lockCookie Access cookie for the record
     * @throws SecurityException If record is locked with an access cookie other
     *                           than given lockCookie
     */
    public synchronized void unlock(T recordID, long lockCookie) throws SecurityException {
        logger.trace("START lock(T, long)");

        // no further check required after successful check of record ID and lock owner
        checkLockOwner(recordID, lockCookie);

        ConcurrentLinkedQueue<Long> cookieQueue = cookieQueues.get(recordID);

        logger.trace("Thread " + lockCookie + " releases lock for: " + recordID);
        cookieQueue.poll();

        Long lockCookieQueued = cookieQueue.peek();

        if (lockCookieQueued != null) {

            synchronized (lockCookieQueued) {

                logger.trace("Thread " + lockCookieQueued + " gets lock for: " + recordID);
                lockCookieQueued.notify();
            }
        }

        logger.trace("END lock(int, long)");
    }


    /**
     * Checks if record row lock is owned by owner of lockCookie.
     *
     * @param recordID   Record ID to check
     * @param lockCookie Access cookie for the record
     * @throws SecurityException If record is locked with an access cookie other
     *                           than given lockCookie
     */
    protected synchronized void checkLockOwner(T recordID, long lockCookie) throws SecurityException {
        logger.trace("START checkLockOwner(int, long)");

        ConcurrentLinkedQueue<Long> cookieQueue;
        if (cookieQueues != null) {
            cookieQueue = cookieQueues.get(recordID);
        } else {
            throw new SecurityException("Thread \"" + lockCookie + "\" is not lock owner for record ID \"" + recordID + "\"");
        }

        if (cookieQueue == null) {
            throw new SecurityException("Thread \"" + lockCookie + "\" is not lock owner for record ID \"" + recordID + "\"");
        }

        if (cookieQueue.peek() != null) {

            if (lockCookie != cookieQueue.peek()) {
                throw new SecurityException("Thread \"" + lockCookie + "\" is not lock owner for record ID \"" + recordID + "\"");
            }
        } else {
            throw new SecurityException("Thread \"" + lockCookie + "\" is not lock owner for record ID \"" + recordID + "\"");
        }

        logger.trace("END checkLockOwner(int, long)");
    }


}
