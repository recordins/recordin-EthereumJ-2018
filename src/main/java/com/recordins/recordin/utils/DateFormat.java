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

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DateFormat {

    /* Logger for console output */
    private static final Logger logger = LoggerFactory.getLogger(DateFormat.class);

    private static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy H:mm:ss");

    private static DateTimeFormatter formatterStorage = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    //static SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy H:mm:ss");
    public static Long DatetoLong(LocalDateTime date) {
        logger.trace("START DatetoLong(Date)");
        logger.trace("END DatetoLong()");

        return date.toInstant(ZoneOffset.UTC).toEpochMilli();
    }

    //static SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy H:mm:ss");
    public static String DateTimetoString(LocalDateTime date) {
        logger.trace("START DatetoString(Date)");
        logger.trace("END DatetoString()");

        return date.format(formatter); //formatter. date. formatter.format(date);
    }

    public static LocalDateTime StringtoDateTime(String value) {
        logger.trace("START StringtoDate(String)");
        LocalDateTime result = LocalDateTime.now();
        //try {
        if (!value.equals("")) {
            result = LocalDateTime.parse(value, formatter);
        }
        /*
        } catch (ParseException ex) {
            logger.error("Error parsing date: '" + value + "': " + ex.toString());
            throw new AttrException("Error parsing date: '" + value + "': " + ex.toString());
        }
         */

        logger.trace("END StringtoDate()");
        return result;
    }


    public static LocalDateTime LongtoDate(Long timestamp) {
        logger.trace("START LongtoDate(Long)");
        //LocalDateTime result = LocalDateTime.now();
        //try {
        LocalDateTime result = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.of("Z"));
        /*
        } catch (ParseException ex) {
            logger.error("Error parsing date: '" + value + "': " + ex.toString());
            throw new AttrException("Error parsing date: '" + value + "': " + ex.toString());
        }
         */

        logger.trace("END LongtoDate()");
        return result;
    }

    public static String LongtoStorageDate(Long timestamp) {
        logger.trace("START LongtoStorageDate(Long)");
        //LocalDateTime result = LocalDateTime.now();
        //try {
        LocalDateTime result = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.of("Z"));
        /*
        } catch (ParseException ex) {
            logger.error("Error parsing date: '" + value + "': " + ex.toString());
            throw new AttrException("Error parsing date: '" + value + "': " + ex.toString());
        }
         */

        logger.trace("END LongtoStorageDate()");
        return result.format(formatterStorage);
    }
}
