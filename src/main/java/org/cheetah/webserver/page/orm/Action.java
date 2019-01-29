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

package org.cheetah.webserver.page.orm;

import com.recordins.recordin.ApplicationContext;
import com.recordins.recordin.Main;
import com.recordins.recordin.orm.User;

import static com.recordins.recordin.Main.syncComplete;
import static com.recordins.recordin.utils.ReflexionUtils.getClassesFromPackage;

import java.lang.reflect.Constructor;
import java.util.TreeMap;

import org.cheetah.webserver.CheetahClassLoader;
import org.cheetah.webserver.CheetahWebserver;
import org.cheetah.webserver.Page;
import org.json.simple.JSONObject;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Action extends Page {

    private static Logger logger = LoggerFactory.getLogger(Action.class);

    @Override
    public void handle(Request request, Response response) {
        logger.debug("START: Handle Request");

        String userName = this.webserver.getUsername(request);

        String sessionID = this.webserver.getSessionID(request);

        User user = User.getUser(userName);

        user.setSessionCookie(sessionID);

        response.setValue("Content-Type", "application/json");

        JSONObject jsonResult = new JSONObject();
        jsonResult.put("MessageType", "Success");
        jsonResult.put("MessageValue", "Action request transmitted");

        String name = request.getParameter("name");
        String args = request.getParameter("args");

        this.debugString.append(" -- PARAMETERS -- ").append(System.lineSeparator());
        this.debugString.append("name        : " + name).append(System.lineSeparator());
        this.debugString.append("args        : " + args).append(System.lineSeparator());
        this.debugString.append(" -- ---------- -- ").append(System.lineSeparator());

        if (!syncComplete) {
            jsonResult.put("MessageType", "Error");
            jsonResult.put("MessageValue", "Error at object writing: Sync must be complete before asking for an object write");
            body.println(jsonResult);
            return;
        }
        if (Main.initDataModelFlag || Main.initFlag) {
            jsonResult.put("MessageType", "Error");
            jsonResult.put("MessageValue", "Error at object writing: Platform init must be complete before asking for an object write");
            body.println(jsonResult);
            return;
        }

        TreeMap<String, Class> classMap = new TreeMap();
        for (Class c : getClassesFromPackage("com.recordins.recordin.orm.action", CheetahWebserver.getInstance().getClassLoader())) {
            classMap.put(c.getSimpleName(), c);
        }

        if (classMap.containsKey(name)) {

            com.recordins.recordin.orm.action.Action actionObject = null;

            try {
                Class parameterClass = classMap.get(name);

                Class[] stringArgsClass = new Class[]{String.class};
                Constructor stringArgsConstructor = parameterClass.getConstructor(stringArgsClass);

                Object[] listArgs = new Object[]{args};

                actionObject = (com.recordins.recordin.orm.action.Action) stringArgsConstructor.newInstance(listArgs);

            } catch (Exception ex) {
                jsonResult.put("MessageType", "Error");
                jsonResult.put("MessageValue", "Error instanciating class for action name: '" + name + "': " + ex.toString());
                logger.error("Error instanciating class for action name: '" + name + "': " + ex.toString());
                ex.printStackTrace();
            }

            try {
                actionObject.execute(user);
            } catch (Exception ex) {
                jsonResult.put("MessageType", "Error");
                jsonResult.put("MessageValue", "Error executing action: '" + name + "': " + ex.toString());
                logger.error("Error executing action: '" + name + "': " + ex.toString());
                ex.printStackTrace();
            }
        } else {
            jsonResult.put("MessageType", "Warning");
            jsonResult.put("MessageValue", "Warining: No action class found for action name: '" + name + "'");
            logger.warn("Warining: No action class found for action name: '" + name + "'");
        }

        body.println(jsonResult);
        logger.debug(" END : Handle Request");
    }
}
