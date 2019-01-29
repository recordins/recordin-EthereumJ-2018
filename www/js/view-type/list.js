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


//'use strict';


// Parameter for the jqxTreeGrid
var attrCollumns = [];
var checkAllFlag = false;

function init_list() {

    if (json_context.active_model) {

        json_context.breadcrumb = [];
        json_context.mode = "view";
        json_context.pageSize = 20;
        json_context.navLeftNumber = 0;
        json_context.offset = 0;
        json_context.limit = json_context.pageSize;

        json_context.active_view = "list"

        display_list();
        updateNavIndex();
        display_button();
        json_context.create = false;

    }
}

function display_list(data, nobreadcrumb) {
    let check = document.getElementById('list');
    if (check === null) {

        clean_div();
        json_context.create = false;
        let list = document.createElement("div");
        list.id = "dataTable";
        list.className = "div_list";
        document.getElementById('panelContentmain-div').appendChild(list);
        document.getElementById('main-div').style.height = '99.9%';
        //    $('#main-div').jqxPanel('scrollTo', 0, 0);
        //json_context.active_view = "list";

        launch_list(data);
        listen_event_list();
        display_button();
        $('#main-div').css("overflow", "hidden"); // Dont display two scrollBar for the treegrid
        $('#main-div').parent().css("overflow", "hidden"); // Dont display two scrollBar for the treegrid

        if (!nobreadcrumb) {
            breadcrumbPush(json_context.active_model, json_context);
        }
    }
}


function launch_list(data) {
    data = parseDataForTable(data);
    let offset = json_context.pageSize;


    if (json_context.oldVersion) {
        let dataOrig = data;
        data = [];
        for (let i = 0; i < offset; i++) {
            data.push(dataOrig[i]);
        }
        offset += json_context.oldLength;

    }

    var source = {
        dataType: "json",
        dataFields: json_context.active_data_list_fields,
        hierarchy: {root: 'old'},
        localData: data
    };

    var dataAdapter = new $.jqx.dataAdapter(source);

    $("#dataTable").jqxTreeGrid({
        exportSettings: {
            columnsHeader: true,
            characterSet: "utf-8",
            recordsInView: true,
            fileName: json_context.active_model
        },
        width: '99.8%', // No 100% to provide the scrollbar when the resolution of the windows is smaller
        height: "99.8%", // No 100% to provide the scrollbar when the resolution of the windows is smaller
        source: dataAdapter,
        sortable: true, // Make the collumns sortable (not the checkbox, see attrCollumn)
        columns: attrCollumns,
        pageable: true, // True but the pager navigation is totally remade
        pagerMode: 'advanced',
        altRows: true,
        hierarchicalCheckboxes: true,
        checkboxes: true,
        columnsResize: true,
        pageSize: offset,
        pagerHeight: 0
    });

    //$('#dataTable').jqxPanel('scrollTo', 0, 0);

    /* JQXTreeGrid doesn't support the collumn checkbox, so let's do it manually */
    let res = document.getElementById("columntabledataTable");
    if (res.firstChild !== null) {
        res.firstChild.firstChild.remove();
        res.firstChild.id = "Select";
        $('#Select').append('<input id="checkBoxMaster" onClick="checkBoxControl()" class="checkAllBox" type="checkbox">');

    }
    $('#pagerdataTable').remove(); // Remove the pagerTable of the jqxTreeGrid, we will made our.
    //$('#verticalScrollBardataTable').remove();
    $('#main-div').css("overflow", "hidden"); // Dont display two scrollBar for the treegrid
    $('#main-div').parent().css("overflow", "hidden"); // Dont display two scrollBar for the treegrid
}

//Create the columns jqxTreeGrid.
function createDatafields(modal) {
    let active_data_list_fields = [];
    attrCollumns = [];
    let res;

    res = '{"name": "old", "type": "array"}';
    res = JSON.parse(res);
    active_data_list_fields.push(res);
    for (let attr in json_context.attrTab) {
        res = '{"name": "' + attr + '", "type": "string"}';
        res = JSON.parse(res);
        active_data_list_fields.push(res);
    }
    if (!jQuery.isEmptyObject(json_context.attrTab)) {
        //res = '{"text":"Select", "datafield":"Select", "type":"string", "width": 60, "sortable": false}';
        res = '{"text":"", "datafield":"Select", "type":"string", "width": 60, "sortable": false}';
        res = JSON.parse(res);
        attrCollumns.push(res);
    }
    for (let attr in json_context.attrTab) {
        if (attr !== "old" && attr !== "isOld" && attr !== "attrUid" && attr !== "blockID") {
            res = '{"text": "' + attr + '", "dataField": "' + attr + '", "type": "string"}';
            res = JSON.parse(res);
            attrCollumns.push(res);
        }
    }

    if (modal) {
        json_context.modal_active_data_list_fields = active_data_list_fields;
    } else {
        json_context.active_data_list_fields = active_data_list_fields;
    }
}

/* Remove and create the checkbox checked or not, cause the checkbox don't check itself*/
function checkBoxControl() {
    if (checkAllFlag === false) {
        $(".checkAllBox").remove();
        $('#Select').append('<input id="checkBoxMaster" onClick="checkBoxControl()" class="checkAllBox" type="checkbox" checked>');
        checkAllFlag = true;
        checkAll();

    } else {
        $(".checkAllBox").remove();
        $('#Select').append('<input id="checkBoxMaster" onClick="checkBoxControl()" class="checkAllBox" type="checkbox">');
        checkAllFlag = false;
        uncheckAll();
    }
}

function getSelectedUIDS() {
    var rows = $("#dataTable").jqxTreeGrid('getRows');
    var result = [];

    if (rows) {
        for (let i = 0; i < rows.length; i++) {
            if (rows[i].checked) {
                result.push(rows[i].attrUid);
            }
        }
    }
    return result;
}

// When an old a row is selected, select the current row and old the other old with the same uid
function checkfamilyRow(attrUid) {
    let rows = $("#dataTable").jqxTreeGrid('getRows');

    for (let i = 0; i < rows.length; i++) {
        if (rows[i].attrUid === attrUid) {
            $("#dataTable").jqxTreeGrid('checkRow', i);
        }
    }
}

function uncheckfamilyRow(attrUid) {
    let rows = $("#dataTable").jqxTreeGrid('getRows');

    for (let i = 0; i < rows.length; i++) {
        if (rows[i].attrUid === attrUid) {
            $("#dataTable").jqxTreeGrid('uncheckRow', i);
        }
    }
}

function uncheckAll() {
    let rows = $("#dataTable").jqxTreeGrid('getRows');
    for (let i = 0; i < rows.length; i++) {
        $("#dataTable").jqxTreeGrid('uncheckRow', i);
    }
}

function checkAll() {
    let rows = $("#dataTable").jqxTreeGrid('getRows');
    for (let i = 0; i < rows.length; i++) {
        $("#dataTable").jqxTreeGrid('checkRow', i);
    }
}

var eventFlag = true;

function listen_event_list() {
    $('#dataTable').on('rowUncheck', function (event) {
        var args = event.args;
        var row = args.row;

        if (eventFlag) {
            eventFlag = false;
            uncheckfamilyRow(row.attrUid);
            eventFlag = true;
        }
        checkAllFlag = false;
    });

    $('#dataTable').on('rowCheck', function (event) {
        var args = event.args;
        var row = args.row;
        if (eventFlag) {
            eventFlag = false;
            checkfamilyRow(row.attrUid);
            eventFlag = true;
        }

        if (!json_context.activeID) {
            json_context.navSingleNavigationIndex = args.key;
            json_context.activeID = args.row.blockID;
            if (args.row.isOld === "true") {
                json_context.current_version = false;
            } else {
                json_context.current_version = true;
            }

            displayAttributeTable();
            /*
            if ($("#objectHistory").is(":visible"))
                display_history();
                */
            breadcrumbUpdateActiveID(json_context.activeID);
        }
    });

    $("#dataTable").on("rowSelect", function (event) {
        let args = event.args;

        json_context.navSingleNavigationIndex = args.boundIndex;
        json_context.activeID = args.row.blockID;

        if (args.row.isOld === "true") {
            json_context.current_version = false;
        } else {
            json_context.current_version = true;
        }

        displayAttributeTable();
        breadcrumbUpdateActiveID(json_context.activeID);

        /*
        if ($("#objectHistory").is(":visible")) {
            display_history();
        }
        */
    });


    $('#dataTable').on('rowDoubleClick', function (event) {

        json_context.create = false;
        json_context.mode = "view";

        let args = event.args;

        if (args.row.isOld === "true") {
            json_context.current_version = false;
        } else {
            json_context.current_version = true;
        }

        json_context.navSingleNavigationIndex = args.boundIndex;
        json_context.activeID = args.row.blockID;

        let schema = parseDataForm();
        if (schema !== false) {

            // does not support destroy during event execution
            setTimeout(function () {

                let navIndex = document.getElementById("navIndex");
                navIndex.value = json_context.navSingleNavigationIndex + json_context.navLeftNumber;

                let navMax = document.getElementById("navMax");
                navMax.textContent = "\u00A0/ " + json_context.navRightNumber;

                json_context.active_view = "form"
                display_Form(schema);
                display_button();
            }, 10);
        }
        breadcrumbUpdateActiveID(json_context.activeID);
    });
}

/* build of table column and cell content */
function getAttrObject(data, schema) {

    let formSchema = json_context.active_schema;
    if (schema) {
        formSchema = schema;
    }

    let attrObject = {};

    attrObject.isOld = [];
    attrObject.attrUid = [];
    attrObject.blockID = [];

    if (formSchema) {
        let attributeList = formSchema.ModelSchema[1].attrMap[1]['Attribute List'][1];
        for (let i = 0; i < attributeList.length; i++) {

            let attribute = attributeList[i][1];
            if (attribute.Visible) {
                let key = attribute.Name;
                attrObject[key] = [];
            }
        }

        for (let i = 0; i < data.length; i++) {
            if (data[i][1].oldVersion !== undefined) {
                attrObject.isOld.push(true);
            } else {
                attrObject.isOld.push(false);
            }
            attrObject.attrUid.push(data[i][1].uid);
            for (let j = 0; j < attributeList.length; j++) {
                let attribute = attributeList[j][1];
                if (attribute.Visible) {
                    let key = attribute.Name;

                    if (data[i][1].attrMap[1][key]) {
                        attrObject[key].push(switchAttr(data[i][1].attrMap[1][key], "list", key, data[i][1].id));
                    } else {

                        if (getPrimitiveAttribute(attribute.AttrType) === "AttrBoolean") {
                            attrObject[key].push("false");
                        } else if (key === "Name") {
                            attrObject[key].push("UID:" + data[i][1].uid);
                        } else {
                            attrObject[key].push("");
                        }
                    }
                }
            }
            attrObject.blockID.push(data[i][1].id);
        }
    }
    return (attrObject);
}

/* Creating table lines for datatable list */
function parseOld(lineTab, modal) {
    let old = [];
    for (let i = 0; i < lineTab.length; i++) {
        if (lineTab[i].isOld === "true") {
            old.push(lineTab[i]);
            delete lineTab[i];
        }
    }
    for (let i = 0; i < old.length; i++) {
        for (let j = 0; j < lineTab.length; j++) {
            if (lineTab[j] !== undefined) {
                if (old[i].attrUid === lineTab[j].attrUid) {
                    if (lineTab[j].old === undefined) {
                        lineTab[j].old = [];
                    }
                    lineTab[j]["old"].push(old[i]);
                }
            }
        }
    }
    lineTab = $.grep(lineTab, function (n) {
        return n === 0 || n;
    }); // Remove empty slot from array

    if (modal) {
        json_context.modal_oldLength = old.length;
    } else {
        json_context.oldLength = old.length;
    }

    return (lineTab);
}

/* Creating table lines for datatable list */
function parseDataForTable(data, schema, modal) {

    if (!data) {
        request_data();
        data = json_context.active_data_list;
        let ids = lookupIDS(data);
        loadIDS(ids);
    }

    let attrObject = getAttrObject(data, schema);
    let count = 0;
    for (let item in attrObject) {
        if (item !== "attrUid" && item !== "isOld")
            count++;
    }
    let lineTab = [];
    let line = "";
    for (let i = 0; i < data.length && i < json_context.searchLimitMax; i++) {
        //line = '{"number":' + i + ',';
        line = '{';
        for (let attr in attrObject) {
            line += '\n"' + attr + '":"' + attrObject[attr][i] + '",';
        }
        line += line.substring(1, line.length - 1);
        line += '}';

        lineTab.push(JSON.parse(line));
    }

    let dataUpdatedOld = parseOld(lineTab, modal);

    /*
    if (modal) {
        json_context.modal_active_data_list_count = dataUpdatedOld.length;
    } else {
        json_context.active_data_list_count = dataUpdatedOld.length;
    }
    */

    /*
    console.log("1 json_context.modal_active_data_list_count:" + json_context.modal_active_data_list_count);
    console.log("1 json_context.active_data_list_count:" + json_context.active_data_list_count);
    console.log("modal:" + modal);
    if (modal) {
        json_context.modal_active_data_list_count = json_context.active_data_list_count;
    }

    console.log("2 json_context.modal_active_data_list_count:" + json_context.modal_active_data_list_count);
    */

    json_context.collumnsNumber = count;
    json_context.attrTab = attrObject;
    createDatafields(modal);
    return (dataUpdatedOld);
}

