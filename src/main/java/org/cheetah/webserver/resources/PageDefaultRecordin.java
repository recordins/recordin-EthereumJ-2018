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
package org.cheetah.webserver.resources;

import org.cheetah.webserver.AbstractPageDefault;
import org.cheetah.webserver.MimeType;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;

/**
 * @author philou
 */
public class PageDefaultRecordin extends AbstractPageDefault {

    @Override
    public void handle(Request request, Response response) {

        response.setStatus(status);

        String mimeType = MimeType.getMimeType("html");
        response.setValue("Content-Type", mimeType);

        body.println("<!DOCTYPE html>");
        body.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"/css/Cheetah\">");

        body.println("<body>");
        body.println("<div id=\"page\" class=\"page-class\">");
        body.println("  <table id=\"cheetahTable\">");
        body.println("    <tr>");
        body.println("      <td width=\"70%\">");
        if (status.getCode() > 400) {
            body.println("        <h1>&nbsp;&nbsp;&nbsp;Error " + status.getCode() + " - " + status.getDescription() + "</h1>");
        } else {
            body.println("        <h1>&nbsp;&nbsp;&nbsp;" + status.getCode() + " - " + status.getDescription() + "</h1>");
        }
        body.println("      </td>");
        body.println("      <td width=\"30%\" style=\"text-align: center;\">");
        //body.println("        <img src=\"/login/Logo\" height=\"60\"/>");

        //<td width="50%" style="text-align: center;">
        body.println("            <a href =\"https://www.recordins.com\" target=\"_blank\"><img src=\"/login/Logo\" width=\"100%\"/></a>");
        //</td>

        body.println("      </td>");
        body.println("    </tr>");
        body.println("  </table>");
        body.println("  <hr>");

        if (request.getTarget().length() > 80) {
            body.println("<h2>&nbsp;&nbsp;&nbsp;" + request.getTarget().substring(0, 80) + " ...</h2>");
        } else {
            body.println("<h2>&nbsp;&nbsp;&nbsp;" + request.getTarget() + "</h2>");
        }

        if (this.e != null) {
            body.println("<div style=\"margin-left:20px\">");
            body.println("<p>");
            body.println(e.toString() + "<BR>");
            for (StackTraceElement element : e.getStackTrace()) {
                body.println(element.toString() + "<BR>");
            }
            body.println("</p>");
            body.println("</div>");
        }

        body.println("<div align=\"right\">");
        body.println("   <a href =\"https://www.recordins.com\" target=\"_blank\">www.recordins.com</a>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
        body.println("</div>");

        body.println("</div>");

        body.println("</body>");
        body.println("</html>");

    }
}
