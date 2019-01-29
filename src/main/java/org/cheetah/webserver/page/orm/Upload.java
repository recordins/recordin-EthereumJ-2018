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

import com.recordins.recordin.Main;
import com.recordins.recordin.orm.User;
import com.recordins.recordin.orm.exception.ORMException;
import com.recordins.recordin.orm.core.AttachmentStore;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.concurrent.ConcurrentHashMap;

import org.cheetah.webserver.AbstractPageDefault;
import org.cheetah.webserver.resources.upload.UploadInformation;
import org.cheetah.webserver.resources.upload.WebSocketUploadPage;
import org.json.simple.JSONObject;
import org.simpleframework.http.ContentType;
import org.simpleframework.http.Part;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.recordins.recordin.Main.syncComplete;

public class Upload extends WebSocketUploadPage {

    private static final Logger logger = LoggerFactory.getLogger(Upload.class);
    public static ConcurrentHashMap<String, UploadInformation> uploadInformationMap = new ConcurrentHashMap();

    @Override
    public void handle(Request request, Response response) {

        String userName = this.webserver.getUsername(request);

        String sessionID = this.webserver.getSessionID(request);

        User user = User.getUser(userName);

        user.setSessionCookie(sessionID);

        String hostPort = webserver.getHostPort(request);
        String fileName = request.getParameter("fileName");

        JSONObject jsonResult = new JSONObject();

        logger.debug(" -- PARAMETERS -- ");
        logger.debug("fileName    : " + fileName);
        logger.debug(" -- ---------- -- ");

        this.debugString.append(" -- PARAMETERS -- ").append(System.lineSeparator());
        this.debugString.append("fileName    : " + fileName).append(System.lineSeparator());
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

        Path rootTempPath = null;

        try {
            rootTempPath = AttachmentStore.getInstance().getTempPath();
            rootTempPath = rootTempPath.toAbsolutePath().normalize();
        } catch (ORMException ex) {
            logger.error("Error getting temp directory: " + ex.toString());

            Status status = Status.INTERNAL_SERVER_ERROR;
            response.setStatus(status);
            try {
                handleDefaultPage(status, ex, request, response);
            } catch (Exception ex2) {
                debugString.append("Error generating :" + status.getDescription() + ": " + ex2.toString()).append(System.lineSeparator());
                logger.error("Error generating " + status.getDescription() + ": " + ex2.toString());
            }
        }

        String referer = request.getValue("Referer");
        String errorMessage = "";

        response.setStatus(Status.OK);

        logger.debug("hostPort: " + hostPort);

        if (!webserver.isFileUploadEnabled()) {
            jsonResult.put("MessageType", "Error");
            jsonResult.put("errorMessage", "Error uploading file: Insufficient privileges");
            logger.error("Error uploading file: Insufficient privileges");
            response.setStatus(Status.UNAUTHORIZED);

        } else {

            String contentType = request.getContentType().toString();

            String boundary = contentType.substring(contentType.indexOf("boundary=") + "boundary=".length());

            long headerLength = 0L;

            for (Part part : request.getParts()) {

                logger.debug("******************** PART " + part.getName());

            /*
            System.out.println("boundary    :" + boundary);
            System.out.println("File getFileName    :" + part.getFileName());
            System.out.println("File getContentType :" + part.getContentType());
            System.out.println("File getName        :" + part.getName());
            System.out.println("Content-Disposition :" + part.getHeader("Content-Disposition"));
             */
                headerLength += boundary.length() + 3;
                headerLength += part.getHeader("Content-Disposition").length() + 1 + "Content-Disposition: ".length();

                ContentType partContentType = part.getContentType();
                if (partContentType != null) {
                    String partContentTypeString = part.getContentType().toString();
                    headerLength += partContentTypeString.length() + 1 + "Content-Type: ".length();
                }

                InputStream is = null;
                long totalReadbytes = 0L;
                try {
                    if (!part.getName().equals("file")) {

                        is = part.getInputStream();

                        if (is != null) {

                            byte[] buffer = new byte[8192];
                            totalReadbytes = 0;
                            int readbytes = is.read(buffer);

                            while (readbytes != -1) {
                                totalReadbytes += readbytes;
                                readbytes = is.read(buffer);
                            }

                            headerLength += totalReadbytes + 2;
                        } else {
                            headerLength += part.getContent().length() + 2;
                        }
                    }

                } catch (IOException ex) {
                    logger.error("Error calculating part length: " + part.getFileName() + ": " + ex.toString());
                } catch (Exception ex) {
                    logger.error("Error calculating part length: " + part.getFileName() + ": " + ex.toString());
                } finally {
                    try {
                        is.close();
                    } catch (Exception ex) {
                    }
                }

                headerLength += 4;

            }

            headerLength += boundary.length() + 9;

            for (Part part : request.getParts()) {

                if (part.getName().equals("file")) {

                    Path destinationPath = rootTempPath.resolve(fileName).toAbsolutePath().normalize();

                    logger.debug("destinationPath: " + destinationPath);
                    logger.debug("rootTempPath   : " + rootTempPath);

                    if (!destinationPath.startsWith(rootTempPath)) {
                        jsonResult.put("MessageType", "Error");
                        jsonResult.put("MessageValue", "Error uploading file: " + part.getFileName() + ": Insufficient privileges");
                        logger.error("Error uploading file: " + part.getFileName() + ": Insufficient privileges");
                    } else if (!this.webserver.isSessionAuthenticationEnabled() || (this.webserver.isSessionAuthenticationEnabled() && !this.webserver.getUsername(request).equals(""))) {

                        if (!this.webserver.isSessionAuthenticationEnabled() || (this.webserver.isSessionAuthenticationEnabled() && !this.webserver.isFileUploadAdminOnly()) || (this.webserver.isSessionAuthenticationEnabled() && this.webserver.isFileUploadAdminOnly() && this.webserver.isAdminUser(user.getLogin()))) {

                            long fileSize = request.getContentLength() - headerLength;
                            long uploadLimit = this.webserver.getFileUploadLimit();

                            if (fileSize > uploadLimit) {
                                try {
                                    part.getInputStream().close();
                                } catch (Exception ex) {
                                }
                                response.setStatus(Status.FORBIDDEN);
                                jsonResult.put("MessageType", "Error");
                                jsonResult.put("MessageValue", "Error uploading file: " + destinationPath.getFileName().toString() + ": File size exeeds upload limit: " + uploadLimit + " (B)");
                                logger.error("Error uploading file: " + destinationPath.getFileName().toString() + ": File size exeeds upload limit: " + uploadLimit + " (B)");

                            } else {

                                UploadThread uploadThread = new UploadThread(destinationPath.toAbsolutePath().toString(), fileSize, part, user.getDisplayName(), referer);
                                uploadThread.start();
                                try {
                                    uploadThread.join();
                                } catch (InterruptedException ex) {

                                }
                            }
                        } else {
                            response.setStatus(Status.UNAUTHORIZED);
                            jsonResult.put("MessageType", "Error");
                            jsonResult.put("MessageValue", "Error uploading file: " + part.getFileName() + ": Insufficient privileges");
                            logger.error("Error uploading file: " + part.getFileName() + ": Insufficient privileges");
                        }
                    } else {
                        response.setStatus(Status.UNAUTHORIZED);
                        jsonResult.put("MessageType", "Error");
                        jsonResult.put("MessageValue", "Error uploading file: " + part.getFileName() + ": Insufficient privileges");
                        logger.error("Error uploading file: " + part.getFileName() + ": Insufficient privileges");
                    }

                    break;
                }
            }

            if (jsonResult.containsKey("MessageType")) {

                if (((String) jsonResult.get("MessageType")).equalsIgnoreCase("error")) {
                    response.setValue("Content-Type", "application/json");
                    body.print(jsonResult.toString());
                    return;
                } else {
                    response.setValue("Content-Type", "text/html");
                }
            } else {
                response.setValue("Content-Type", "text/html");
            }
        }
    }

    private class UploadThread extends Thread {

        private String destinationFile;
        private long fileSize;
        private Part part;
        private String user;
        public String referer;

        private UploadInformation uploadInformation;

        public UploadThread(String destinationFile, long fileSize, Part part, String user, String referer) {
            logger.debug("START UploadThread(String, long, Part, String, String)");
            logger.debug("destinationFile: " + destinationFile);

            this.destinationFile = destinationFile;
            this.fileSize = fileSize;
            this.part = part;
            this.user = user;
            this.referer = referer;

            uploadInformation = new UploadInformation(destinationFile, fileSize, 0L, user, referer);

            if (!uploadInformationMap.containsKey(destinationFile)) {
                uploadInformationMap.put(destinationFile, uploadInformation);
            } else {
                uploadInformationMap.replace(destinationFile, uploadInformation);
            }

            logger.debug("END UploadThread(String, long, Part, String, String)");
        }

        @Override
        public void run() {
            logger.debug("START run()");
            long totalReadbytes = 0L;
            uploadInformation.fileSent = totalReadbytes;
            InputStream is = null;
            FileOutputStream fos = null;
            try {

                is = part.getInputStream();
                fos = new FileOutputStream(destinationFile);

                byte[] buffer = new byte[1048576];
                totalReadbytes = 0;
                int readbytes = is.read(buffer);

                logger.debug("--------- readbytes: " + readbytes);

                while (readbytes != -1 && !uploadInformation.canceled) {
                    totalReadbytes += readbytes;
                    uploadInformation.fileSent = totalReadbytes;
                    fos.write(buffer, 0, readbytes);
                    readbytes = is.read(buffer);

                    logger.debug("--------- readbytes: " + readbytes);
                    // Thread.sleep(500);
                    logger.debug("---- totalReadbytes: " + totalReadbytes);
                }

            } catch (FileNotFoundException ex) {
                uploadInformation.errorMessage = "Error uploading file: " + part.getFileName() + ": " + ex.toString();
                logger.error("Error uploading file: " + part.getFileName() + ": " + ex.toString());
            } catch (IOException ex) {
                uploadInformation.errorMessage = "Error uploading file: " + part.getFileName() + ": " + ex.toString();
                logger.error("Error uploading file: " + part.getFileName() + ": " + ex.toString());
            } catch (Exception ex) {
                uploadInformation.errorMessage = "Error uploading file: " + part.getFileName() + ": " + ex.toString();
                logger.error("Error uploading file: " + part.getFileName() + ": " + ex.toString());
            } finally {
                try {
                    is.close();
                } catch (Exception ex) {
                }
                try {
                    fos.close();
                } catch (Exception ex) {
                }
            }
            //uploadInformation.errorMessage = "test23";
            uploadInformation.finished = true;

            logger.debug("END run()");
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
}
