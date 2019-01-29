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

package org.cheetah.webserver.page;

import com.recordins.recordin.orm.User;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Paths;

import org.cheetah.webserver.AbstractPageDefault;
import org.cheetah.webserver.Page;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class index extends Page {

    private static Logger logger = LoggerFactory.getLogger(index.class);

    @Override
    public void handle(Request request, Response response) {

        String userName = this.webserver.getUsername(request);

        String sessionID = this.webserver.getSessionID(request);

        User user = User.getUser(userName);

        user.setSessionCookie(sessionID);

        response.setValue("Content-Type", "text/html");

        String pageContent = "";

        try {

            //URL url = this.webserver.getClassLoader().getResource("org/cheetah/webserver/resources/index.html");

            //pageContent = readTextFileRessource(request, body, url, this.webserver.getClassLoader(), Charset.forName("utf-8"));

            String page = this.webserver.getDefaultUtilsClass().loadPage(request, Paths.get(this.webserver.getFileRoot(request) + "/index.html"), Charset.forName("utf-8"));

            body.println(page);

        } catch (Exception ex) {
            Status status = Status.INTERNAL_SERVER_ERROR;
            response.setStatus(status);
            logger.error("Error generating page: " + ex.toString());
            try {
                handleDefaultPage(status, ex, request, response);
            } catch (Exception ex2) {
                debugString.append("Error generating :" + status.getDescription() + ": " + ex2.toString()).append(System.lineSeparator());
                logger.error("Error generating " + status.getDescription() + ": " + ex2.toString());
            }
        }
    }

    private void handleDefaultPage(Status status, Exception e, Request request, Response response) throws Exception {

        Class lookupPage = null;
        lookupPage = this.webserver.getDefaultPageClass();
        AbstractPageDefault pageDefault = (AbstractPageDefault) lookupPage.newInstance();
        pageDefault.setRessources(body, webserver, debugString);
        pageDefault.setStatus(status);
        pageDefault.setException(e);
        pageDefault.handle(request, response);
    }

    public String readTextFileRessource(Request request, PrintStream body, URL url, ClassLoader classLoader, Charset charset) throws FileNotFoundException, IOException, URISyntaxException {

        StringBuilder builder = new StringBuilder();
        String fileName = url.toString();

        BufferedReader br = null;
        InputStream in = null;

        if (fileName.startsWith("jar")) {
            fileName = fileName.substring(fileName.lastIndexOf("!") + 1).substring(1);

            in = classLoader.getResourceAsStream(fileName);
        } else {
            fileName = fileName.substring(fileName.lastIndexOf(":") + 1);

            in = new FileInputStream(fileName);
        }

        InputStreamReader is = new InputStreamReader(in, charset);
        br = new BufferedReader(is);

        String line = "";
        line = br.readLine();

        while (line != null) {
            builder.append(line + System.getProperty("line.separator"));
            //        body.println(line);
            line = br.readLine();
        }

        in.close();

        return builder.toString();
    }
}
