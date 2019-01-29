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

var json_context = {
    "active_model": "", //information about the current model
    "active_menuID": "", //information about the current menu
    "active_schema": "", //information about the current schema
    "active_view": "", // information about the current type of view [list, form, graph, kanabn]
    "active_data_list": "", //Array with the all the objects of the current model
    "active_data_list_count": 0, //size of the active_data_list array
    "active_data_list_fields": [], // Fields of the active_data_list array
    "activeID": "", // ID of the current active object
    "mode": "view", // mode for the form : view/edit
    "create": false, // If the form is in mode create or not
    "failed_create": false, // if creation failed, to allow edit again for creation.

    "attrTabSystem": "", // Array with all the attributes system for the sidebar
    "attrTab": "", // Array with all the attributes for the sidebar
    "formData": {}, // FormData, data form the react json schema form
    "oldVersion": false, // Display old version or not
    "oldLength": 0, // Size of the array with just the oldVersions
    "searchFilter": "", // Information about the current filter
    "offset": 0, // Offset for data requested for the active_data_list
    "limit": -1, // Limit of number of data requested for the active_data_list
    "searchIndex": "VIRTUAL_TABLES_ACTIVE", // State of the data displayed
    "searchLimitMax": 500, // Limit of the data displayed
    "IDSCache": [], // All the ID usefull on the current view
    "SchemaCache": {}, // All the schemas usefull on the current view
    "HistoryCache": {}, // All the schemas usefull on the current view
    "menuData": "", // Data for the menu
    "breadcrumb": [], //list with the lates models viewed
    "selectedItem": "", // In list view, the selected row of the table
    "selectedItemList": [], // Array with all the selected items

    "PrimitiveAttributes": {},

    "navLeftNumber": 0, // Information about the navigation
    "navRightNumber": 0, // Information about the navigation
    "currentPageNumber": 0, // In list view, current page of the table
    "pageSize": 20, // Number of ellement in the datatab by page
    "firstPageSize": 0, // Information about the navigation, size of the first page
    "navSingleNavigationIndex": 0,

    /* modal section */

    //"modal_active_data_list_count": 0,
    "modal_oldLength": 0,
    "modal_active_data_list_fields": [],
    "modal_formData": {},

    "modalForm": false
};

var checkboxAttrSystem = false;
var AttrID_REGEX_PATTERN = "[0-9]+\\..+\\..+";
var loggedUser = ""; // name of the actual connected user
var loggedUserID = ""; // id of the actual connected user
var nodeID = "";

var initFlag = true;

/*
 * 
 * Clean main windows before displaying other view
 */
function clean_div() {
    $("#kanban1").remove();
    $(".div_orgchart").remove();
    $(".form").remove();
    $(".datamodel").remove();
    $(".container").remove();
    if ($("#dataTable")) {
        try {
            $("#dataTable").jqxTreeGrid('destroy');
        }
        catch (e) {
        }
    }

    $("#myModal").remove();
    $("#myModalForm").remove();
    $("#myModalProgress").remove();
}

function setIndex(IndexList) {
    if (IndexList !== "undefined") {
        $("#objectIndexSelect > option").each(function () {
            $(".index_option").remove();
        });

        for (let i = 0; i < IndexList.length; i++) {

            let selected = '';

            if (IndexList[i]["name"] === json_context.searchIndex) {
                selected = ' selected="selected" ';
            }

            $('#objectIndexSelect').append('<option class="index_option" value="' + IndexList[i]["name"] + '" ' + selected + '>' + IndexList[i]["displayName"] + '</option>');

        }
    }
}

/*
 *  
 * Function to launch when page is loaded
 */
$(document).ready(function () {

    document.getElementById("filterInput").value = "";
    $("#jqxLoader").jqxLoader({isModal: true, width: 150, height: 100});

    requestConfig();
    websocketConnect();

    if (!initFlag) {
        requestMenu(); // Launch jqxmenu
        displayMenu(json_context.menuData);
    }

    updateNavIndex();
    launch_layout(); // Launch jqxlayout for the structure of the page

    initNavbar();


});


function requestConfig() {

    let get = '/json/GetNodeStatus';
    let json = RequestGET(get, false);

    applyConfig(json);
}


function applyConfig(json) {

    if (json.IndexList) {
        setIndex(json.IndexList);
    }

    json_context.PrimitiveAttributes = json.PrimitiveAttributes;
    json_context.searchLimitMax = json.SearchLimitMax;
    AttrID_REGEX_PATTERN = json.AttrID_REGEX_PATTERN;
    loggedUser = json.loggedUser;
    loggedUserID = json.loggedUserID;
    nodeID = json.NodeID;

    if (!json.SyncComplete || !json.DatasetReady || json.InitFlag) {

        let message = "Initializing objects";

        if (!json.DatasetReady) {
            if (json.Percent >= 3) {
                message = "Loading Dataset : " + json.Percent + "%";
            }
            else {
                message = "Loading Dataset";
            }
        }
        else if (!json.SyncComplete) {
            if (json.Percent !== 0) {
                message = "Fetching blocks from peers  : " + json.Percent + "%";
            }
            else {
                message = "Fetching blocks from peers ";
            }
        }

        initFlag = true;
        $('#jqxLoader').jqxLoader('open');
        $("#jqxLoader").jqxLoader({text: message});
    }
    else {
        if (initFlag) {
            initFlag = false;
            $('#jqxLoader').jqxLoader('close');
            requestMenu(); // Launch jqxmenu
            displayMenu(json_context.menuData);
        }
    }
}


