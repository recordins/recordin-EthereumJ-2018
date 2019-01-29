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

import java.io.Serializable;

import org.cheetah.webserver.websocket.server.AbstractWebSocketServerWorker;
import org.slf4j.LoggerFactory;

public class WebSocketServerWorkerRecordin extends AbstractWebSocketServerWorker {

    private static org.slf4j.Logger logger = LoggerFactory.getLogger(WebSocketServerWorkerRecordin.class);

    @Override
    public void processString(String resquestString) {
        String response = null;
        logger.debug("Got TEXT: " + resquestString);
        /*
        response = resquestString;

        Frame replay = new DataFrame(FrameType.TEXT, response);
        String user = this.webSocketListener.getUserName(this.session);
        this.webSocketService.send(replay, user);
         */
    }

    @Override
    public void processObject(Serializable resquestObject) {
        Serializable response = null;
        logger.debug("Got BINARY");
        /*
        response = resquestObject;

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutput out = null;
        byte[] byteArray = new byte[0];

        try {
            out = new ObjectOutputStream(bos);
            out.writeObject(response);
            byteArray = bos.toByteArray();

            if (response != null) {
                Frame replay = new DataFrame(FrameType.BINARY, byteArray);
                String user = this.webSocketListener.getUserName(this.session);
                this.webSocketService.send(replay, user);
                
                // webSocketService.getWebserver().distributeToWebsocketServiceFrame("org.cheetah.webserver.page.websocket.Chat2", session, replay);
            }

        } catch (IOException ex) {

            logger.error("Error sending object response: " + ex.toString());
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException ex) {

            }
            try {
                bos.close();
            } catch (IOException ex) {
            }
        }
         */
    }
}
