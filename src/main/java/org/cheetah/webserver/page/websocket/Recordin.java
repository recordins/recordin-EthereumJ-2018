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

package org.cheetah.webserver.page.websocket;

import com.recordins.recordin.WebSocketServerWorkerRecordin;
import org.cheetah.webserver.websocket.server.WebSocketServerPage;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Recordin extends WebSocketServerPage {

    private static Logger logger = LoggerFactory.getLogger(Recordin.class);

    public Recordin() {
        super(WebSocketServerWorkerRecordin.class);
    }

    public void handle(Request request, Response response) {
        logger.trace("START: handle(Request, Response)");
        logger.trace(" END : handle(Request, Response)");
    }
}