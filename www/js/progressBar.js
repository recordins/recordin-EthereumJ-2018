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


function display_progressBar() {
    var renderText = function (text, value) {
        if (value < 55)
            return "<span style='color: #333;'>" + text + "</span>";
        return "<span style='color: #fff;'>" + text + "</span>";
    };
    $("#jqxProgressBar").jqxProgressBar({
        animationDuration: 0,
        showText: true,
        renderText: renderText,
        template: "info",
        width: 250,
        height: 30,
        value: 0
    });
}