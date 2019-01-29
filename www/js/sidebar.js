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

function clean_sidebarAttributes() {
    $(".rowText").remove();
    $(".attrMapContainer").remove();
    $(".attrSystemContainer").remove();
    $(".checkAttrSystem").remove();
}

function clean_sidebar_history() {
    $("#objectHistoryTables").remove();
    //$("#panelContentobjectHistory").remove();
}


/* Sidebar 1 => display attributs */

function displayAttrSystem(object) {
    if (typeof (object) !== "undefined") {
        getAttrTabSystem(object);
    }
    else {
        getAttrTabSystem();
    }

    let i = 0;
    let table2 = document.createElement("table");
    table2.className = "SideTable2 table table-striped";
    table2.id = "SideTable2";
    table2.style = "margin-bottom:0";
    document.getElementById('attrSystemContainer').appendChild(table2);
    for (var key in json_context.attrTabSystem) {
        if (json_context.attrTabSystem.hasOwnProperty(key) && key !== "oldVersion") {
            i++;
            let trid = "tr2" + i;
            $('#SideTable2').append('<tr id="' + trid + '"></tr>');
            $('#' + trid).append('<th>' + key + '</th>');
            $('#' + trid).append('<td>' + json_context.attrTabSystem[key] + '</td>');
        }
    }
    $('#attrSystemContainer').append('<hr class="hrTab">');
}

function displayAttributeTable(object, schema) {
    clean_sidebarAttributes();

    if (json_context.active_data_list !== "" && json_context.create === false) {

        if (!object) {
            if (json_context.activeID) {
                object = getObjectWithID(json_context.activeID);
            }
        }

        if (!schema && object) {
            schema = getModelSchemaFromObject(object);
        }

        if (object) {

            $('#panelContentattributes').append('<div class="checkAttrSystem"><input onClick="checkChange()" type="checkbox" id="checkboxAttrSystem" class="checkboxAttrSystem" name="attrSystem" value="attrSystem"> <span onclick="changeCheck()" class="checkText">Display system attributes</span><br></div>');
            $('#panelContentattributes').append('<div id="attrSystemContainer" class="attrSystemContainer"></div><div id="attrMapContainer" class="attrMapContainer"></div>');

            if (checkboxAttrSystem === true) {
                document.getElementById("checkboxAttrSystem").checked = true;
            }

            getAttrTab(object, schema);
            checkChange(object);

            let i = 0;
            let table = document.createElement("table");
            table.className = "SideTable table table-striped";
            table.id = "SideTable";
            document.getElementById('attrMapContainer').appendChild(table);

            for (var key in json_context.attrTab) {
                if (json_context.attrTab.hasOwnProperty(key) && key !== "uid" && key !== "number") {
                    i++;
                    let trid = "tr" + i;
                    $('#SideTable').append('<tr id="' + trid + '"></tr>');
                    $('#' + trid).append('<th>' + key + '</th>');
                    $('#' + trid).append('<td>' + json_context.attrTab[key] + '</td>');
                }
            }
        }
    }
}

function checkChange(object) {
    if (document.getElementById('checkboxAttrSystem').checked) {
        checkboxAttrSystem = true;
        if (typeof (object) !== "undefined") {
            displayAttrSystem(object);
        } else {
            displayAttrSystem();
        }
    } else {
        checkboxAttrSystem = false;
        $(".SideTable2").remove();
        $(".hrTab").remove();
    }

    display_history(object);
}

function changeCheck() {
    if (document.getElementById('checkboxAttrSystem').checked) {
        document.getElementById("checkboxAttrSystem").checked = false;
    }
    else {
        document.getElementById("checkboxAttrSystem").checked = true;
    }
    checkChange();
}

/* Sidebar 2 => display differences in object history */

function display_history(object, schema) {

    if (json_context.activeID) {
        clean_sidebar_history();
        if (!object) {
            if (json_context.activeID) {
                object = getObjectWithID(json_context.activeID);
            }
        }

        if (!schema && object) {
            schema = getModelSchemaFromObject(object);
        }

        if (object) {


            let uid = object[1].uid;
            let res = json_context.HistoryCache[uid];

            if (!res) {
                let get = '/orm/History?id=' + object[1].uid;
                res = RequestGET(get, false);

                if (res) {
                    json_context.HistoryCache[uid] = res;
                }
            }

            if (typeof (res) !== "undefined") {

                let objectHistoryTables = document.createElement("table");

                objectHistoryTables.className = "table table-striped table-history-tables";
                objectHistoryTables.id = "objectHistoryTables";

                document.getElementById('panelContentobjectHistory').appendChild(objectHistoryTables);

                for (let i = 0; i < res.length; i++) {

                    let table = document.createElement("table");
                    table.className = "table-history";
                    table.id = "historyTable_" + i;

                    let objectHistoryTablesTitleLine = document.createElement("tr");
                    let objectHistoryTablesTitleColumn = document.createElement("td");
                    let action = "created";

                    if (!res[i].header.created) {
                        action = "updated";
                    }

                    objectHistoryTablesTitleColumn.appendChild(createElementFromHTML('<span class="history-operation">' + switchAttr(["AttrDateTime", res[i].header.date], "panel", "", object[1].id) + " " + action + " by: " + switchAttr(["AttrID", res[i].header.user], "panel", "", object[1].id) + '</span>'));

                    if (i > 0) {
                        objectHistoryTablesTitleLine.style = "border-top: 1px solid #909090;";
                    }

                    if (i % 2 === 1) {
                        objectHistoryTablesTitleLine.style = "background-color: #f2f2f2; border-top: 1px solid #909090;";
                    }

                    objectHistoryTablesTitleLine.appendChild(objectHistoryTablesTitleColumn);

                    let objectHistoryTablesLine = document.createElement("tr");
                    let objectHistoryTablesColumn = document.createElement("td");
                    objectHistoryTablesColumn.appendChild(table);
                    if (i % 2 === 1) {
                        objectHistoryTablesLine.style = "background-color: #f2f2f2";
                    }
                    objectHistoryTablesLine.appendChild(objectHistoryTablesColumn);

                    document.getElementById('objectHistoryTables').appendChild(objectHistoryTablesTitleLine);
                    document.getElementById('objectHistoryTables').appendChild(objectHistoryTablesLine);

                    for (let j = 0; j < res[i].history.length; j++) {

                        let historyData = res[i].history[j];

                        let content = "";
                        let trid = "tr_history_" + i + "_" + j;
                        let operation = historyData.op;
                        let path = historyData.path;

                        let value = historyData.value;

                        if (typeof(value) === "object") {

                            if (path === "Attribute List" && object[1].model === "Model") {
                                value = JSON.stringify(value);
                            }
                            else {
                                value = switchAttr(value, "panel", path.substring(path.indexOf("/") + 1), object[1].id);
                            }
                        }

                        if (typeof(value) !== "undefined") {
                            if (checkboxAttrSystem) {
                                $('#historyTable_' + i).append('<tr id="' + trid + '"></tr>');
                                $('#' + trid).append('<th>' + path.substring(path.indexOf("/") + 1) + ': <BR><span class="history-operation">&nbsp;&nbsp;&nbsp;(' + operation + ')</span>' + '</th>');
                                $('#' + trid).append('<td>' + value + '</td>');
                            }
                            else {
                                if (!path.startsWith("system")) {
                                    $('#historyTable_' + i).append('<tr id="' + trid + '"></tr>');
                                    $('#' + trid).append('<th>' + path.substring(path.indexOf("/") + 1) + ': <BR><span class="history-operation">&nbsp;&nbsp;&nbsp;(' + operation + ')</span>' + '</th>');
                                    $('#' + trid).append('<td>' + value + '</td>');
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


/* gets the information for the system part of the attribute window   */
function getAttrTabSystem(updated) {
    let attrTabSystem = {};

    let object;
    if (typeof (updated) !== "undefined")
        object = updated;
    else
        object = getObjectWithID(json_context.activeID);
    if (!object) {
        return;
    }


    let keys = Object.keys(object[1]);
    keys.sort();

    for (let i = 0; i < keys.length; i++) {
        let key = keys[i];

        if (key !== "attrMap") {
            if (object[1][key] === null) {
                attrTabSystem[key] = "";
            } else if (typeof (object[1][key]) === "object") {
                let value = object[1][key];
                attrTabSystem[key] = switchAttr(value, "panel", key, object[1].id);
            } else {
                attrTabSystem[key] = (object[1][key]);
            }
        }
    }
    json_context.attrTabSystem = attrTabSystem;
}

/* gets the information for the object part of the attribute window   */
function getAttrTab(updated, updatedSchema) {

    let attrTab = {};

    let object;
    let schema;

    if (typeof (updated) !== "undefined") {
        object = updated;
    } else {
        object = getObjectWithID(json_context.activeID);
    }

    if (typeof (updatedSchema) !== "undefined") {
        schema = updatedSchema;
    } else {
        schema = json_context.active_schema;
    }


    if (typeof (object) === "undefined" || object === "") {
        return ({});
    }

    let printed = [];
    if (schema.ModelSchema[1]) {
        let attributeList = schema.ModelSchema[1].attrMap[1]['Attribute List'][1];
        if (object[1].attrMap) {

            attributeList = jQuery.map(attributeList, function (n, i) {
                return (n[1].Name);
            });
            ;
            attributeList.sort();

            for (let i = 0; i < attributeList.length; i++) {
                let attributeName = attributeList[i];

                if (attributeName in object[1].attrMap[1]) {
                    if (attributeName === "Attribute List" && object[1].model === "Model") {
                        attrTab[attributeName] = JSON.stringify(object[1].attrMap[1][attributeName]);
                    }
                    else {
                        attrTab[attributeName] = switchAttr(object[1].attrMap[1][attributeName], "panel", attributeName, object[1].id);
                    }
                } else {
                    attrTab[attributeName] = "";
                }

                printed.push(attributeName);
            }

            for (var key in object[1].attrMap[1]) {

                if (printed.indexOf(key) < 0) {
                    attrTab[key] = switchAttr(object[1].attrMap[1][key], "panel", key, object[1].id);
                }
            }
        }
    }
    json_context.attrTab = attrTab;
}
