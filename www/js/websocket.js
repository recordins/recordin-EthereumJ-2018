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

var websocket = null;

function websocketConnect() {

    if (websocket === null) {
        //try {
        let wsUri = getWebsocketRootURL();
        wsURL = wsUri + "/websocket/Recordin";
        websocket = new WebSocket(wsURL);

        websocket.onmessage = function (evt) {
            onMessage(evt);
        };

        websocket.onerror = function (evt) {
            console.log("Error WebSocket: " + evt.data.toString());
        };

        websocket.onclose = function (evt) {
            setTimeout(function () {
                $.notify("Server disconnected\n Please refresh your browser when the service is available again", {
                    className: 'error',
                    clickToHide: false,
                    autoHide: false,
                    globalPosition: 'top right'
                });
            }, 250);

            onClose(evt);
        };
        /*
    }
    catch(e){

    }
    */
    }
}

function onMessage(evt) {
    console.log("START : onMessage: " + evt.data.toString());

    let json = JSON.parse(evt.data.toString());

    switch (json.MessageType) {
        case "MenuUpdate":
            var menuData = json.MessageValue;
            $('#jqxMenu').jqxMenu('destroy');
            let jqxMenu = document.createElement("div");
            jqxMenu.id = "jqxMenu";
            jqxMenu.className = "jqxMenu";
            document.getElementById('menuContainer').appendChild(jqxMenu);
            json_context.menuData = menuData;
            displayMenu(json_context.menuData);
            break;

        case "Success":
            $.notify(json.MessageValue, "success");
            break;

        case "SuccessBlock":
            $.notify(json.MessageValue, "success");
            if (json_context.active_model === json.MessageModel) {

                request_data();
                breadcrumbUpdateActiveData(json_context.active_data_list, json_context.active_data_list_count);

                json_context.activeID = json.MessageObjectID;
                loadID(json_context.activeID);

                let object = getObjectWithID(json_context.activeID);

                json_context.current_version = true;

                if (parseDataForm(object, json_context.active_schema) !== false) {

                    let activeID = json_context.activeID;
                    let current_version = json_context.current_version;
                    let formData = json_context.formData;
                    let active_view = json_context.active_view;
                    let active_data_list = json_context.active_data_list;
                    let active_data_list_count = json_context.active_data_list_count;

                    breadcrumbBack(0);

                    json_context.activeID = activeID;
                    json_context.current_version = current_version;
                    json_context.formData = formData;
                    json_context.active_view = active_view;
                    json_context.active_data_list = active_data_list;
                    json_context.active_data_list_count = active_data_list_count;

                    breadcrumbUpdateActiveData(active_data_list, active_data_list_count);

                    delete json_context.HistoryCache[json_context.activeID.substring(json_context.activeID.lastIndexOf(".") + 1)];

                    if (active_view === "list") {
                        display_list(json_context.active_data_list, true);
                        updateNavIndex();
                        display_button();
                        break;
                    }
                    else if (active_view === "form") {
                        display_Form();
                        display_button();
                        displayAttributeTable();
                        let navIndex = document.getElementById("navIndex");
                        navIndex.value = 1;
                    }
                }
            }
            break;

        case "Warning":
            $.notify(json.MessageValue.replace(/: /g, ':\n'), "warn");
            break;

        case "Error":
            $.notify(json.MessageValue.replace(/: /g, ':\n'), "error");
            break;

        case "WarningStay":
            $.notify(json.MessageValue.replace(/: /g, ':\n'), {
                className: 'warn',
                clickToHide: true,
                autoHide: false,
                globalPosition: 'top right'
            });
            break;

        case "ErrorStay":
            $.notify(json.MessageValue.replace(/: /g, ':\n'), {
                className: 'error',
                clickToHide: true,
                autoHide: false,
                globalPosition: 'top right'
            });
            break;

        case "Status":

            applyConfig(json.MessageValue);

            break;
        default:
            $.notify(json.MessageValue.replace(/: /g, ':\n'), "warn");
    }
}

function onClose(evt) {
    mainWebsocket = null;
}

function goodbye() {
    console.log("START : goodbye");
    mainWebsocket.close();
    mainWebsocket = null;
    console.log(" END : goodbye");
}

