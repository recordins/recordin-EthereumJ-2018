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


function CreateBtn() {


    let active_view = json_context.active_view;
    breadcrumbBack(0);
    json_context.active_view = active_view;

    json_context.mode = "create";
    json_context.create = true;
    initDataFormCreate();
    json_context.current_version = true;

    breadcrumbPush("New", json_context);
    json_context.activeID = false;

    display_Form();
    display_button();
}

function EditBtn() {

    let activeID = json_context.activeID;
    let current_version = json_context.current_version;
    let failed_create = json_context.failed_create;
    let formData = json_context.formData;
    let active_view = json_context.active_view;
    let navLeftNumber = json_context.navLeftNumber;
    let navRightNumber = json_context.navRightNumber;
    let navSingleNavigationIndex = json_context.navSingleNavigationIndex;
    let offset = json_context.offset;
    let limit = json_context.limit;
    let currentPageNumber = json_context.currentPageNumber;
    let pageSize = json_context.pageSize;
    let firstPageSize = json_context.firstPageSize;

    breadcrumbUpdateActiveID(json_context.activeID);

    breadcrumbBack(0);

    json_context.activeID = activeID;
    json_context.current_version = current_version;
    json_context.failed_create = failed_create;
    json_context.formData = formData;
    json_context.active_view = active_view;
    json_context.navLeftNumber = navLeftNumber;
    json_context.navRightNumber = navRightNumber;
    json_context.navSingleNavigationIndex = navSingleNavigationIndex;
    json_context.offset = offset;
    json_context.limit = limit;
    json_context.currentPageNumber = currentPageNumber;
    json_context.pageSize = pageSize;
    json_context.firstPageSize = firstPageSize;

    json_context.mode = "edit";

    if (!json_context.failed_create) {
        json_context.create = false;
    }
    else {
        json_context.create = true;
        json_context.mode = "create";
    }

    if (json_context.active_view === "form" || (json_context.active_view === "list" && json_context.mode === "create")) {

        if (json_context.mode === "edit") {
            let navIndex = document.getElementById("navIndex");
            navIndex.value = json_context.navLeftNumber;
        }

        if (json_context.mode === "create") {
            breadcrumbPush("New", json_context);
        }

        display_Form(json_context.active_schema);
        display_button();
        displayAttributeTable();
    } else {

        if (json_context.activeID) {
            if (parseDataForm() !== false) {

                let navIndex = document.getElementById("navIndex");
                navIndex.value = json_context.navSingleNavigationIndex + json_context.navLeftNumber;
                display_Form(json_context.active_schema);
                display_button();
                displayAttributeTable();
            }
        } else {
            $("#EditBTN").notify("Please select one object in the list", "warn");
        }
    }
}

function SaveBtn() {

    json_context.mode = "view";
    document.getElementById('submit').click();
}

function CancelBtn() {

    json_context.mode = "view";

    breadcrumbPop();
    if (json_context.active_view === "list") {
        breadcrumbPop();
        let currentPageNumber = json_context.currentPageNumber;
        let offset = json_context.offset;
        display_list();
        json_context.currentPageNumber = currentPageNumber;
        json_context.offset = offset;
        updateNavIndex();
    }
    else if (json_context.active_view === "kanban") {
        json_context.activeID = false;
        breadcrumbPop();
        display_kanban();
        updateNavIndex();
    }
    else if (json_context.active_view === "graph") {
        display_graph();
    }
    else if (json_context.active_view === "form") {
        parseDataForm();
        display_Form();
    }
    display_button();
    displayAttributeTable();
    json_context.failed_create = false;
    json_context.create = false;
}

//hide all the unfix button
function hide_btn() {
    $("div#create_edit").hide();
    $("div#save_cancel").hide();
    $("div#searchDiv").hide();
    $("div#export").hide();
    $("div#actions").hide();
    $("div#checkAllContainer").hide();
    $("div#print").hide();
    $("div#list_navigation").hide();
    $("div#btnBlock").hide();

}

//Display the differents button depending of the current mode and view
function display_button() {
    hide_btn();

    document.getElementById('list_navigation').style.display = "inline-block";
    document.getElementById('btnBlock').style.display = "inline-block";

    if (json_context.active_view === "form" || json_context.active_view === "graph") {
        $("div#searchDiv").hide();
        $("div#actions").show();
    } else if (json_context.active_view === "list" || json_context.active_view === "kanban") {
        $("div#actions").show();
        $("div#checkAllContainer").show();
        $("div#export").show();
        $("div#print").show();
        $("div#searchDiv").show();
        $("div#rowSelector").show();
    } else {
        $("div#searchDiv").show();
        $("div#rowSelector").show();
    }

    if (json_context.mode === "view") {
        $("div#create_edit").show();
        $("div#save_cancel").hide();
    }

    if (json_context.mode === "edit" || json_context.mode === "create") {
        $("div#create_edit").hide();
        $("div#save_cancel").show();
    }

    if (json_context.mode === "create") {
        $("div#actions").hide();
    }
}

function PrevPage() {

    if (json_context.mode === "edit") {
        json_context.active_view = "form";
    }

    if (json_context.active_view === "list" || json_context.active_view === "kanban") {

        json_context.currentPageNumber--;
        if (json_context.currentPageNumber <= 0) {
            json_context.currentPageNumber = 1;
            json_context.firstPageSize = 0;
        }

        updateNavIndex();
        nav_refresh_table();
    } else {

        json_context.navSingleNavigationIndex--;
        if (json_context.navSingleNavigationIndex < 0) {
            json_context.navSingleNavigationIndex = 0;
        }

        let navIndex = document.getElementById("navIndex");
        navIndex.value = json_context.navLeftNumber + json_context.navSingleNavigationIndex;

        let navMax = document.getElementById("navMax");
        navMax.textContent = "\u00A0/ " + json_context.navRightNumber;

        let object = json_context.active_data_list[navIndex.value - 1 - json_context.offset];

        if (json_context.oldVersion) {
            let data = [];
            for (let i = 0; i < json_context.active_data_list.length; i++) {
                object = json_context.active_data_list[i];
                if (!object[1].oldVersion) {
                    data.push(object);
                }
            }
            object = data[navIndex.value - 1 - json_context.offset];
        }

        json_context.activeID = object[1].id;
        breadcrumbPop();
        if (json_context.active_view === "form") {
            let schema = parseDataForm();
            display_Form(schema);
        } else if (json_context.active_view === "graph") {
            display_graph();
        }
        displayAttributeTable();
    }
}


function NextPage() {

    if (json_context.mode === "edit") {
        json_context.active_view = "form";
    }

    if (json_context.active_view === "list" || json_context.active_view === "kanban") {

        json_context.currentPageNumber++;
        if (1 + json_context.firstPageSize + (json_context.pageSize * json_context.currentPageNumber) > json_context.active_data_list_count + json_context.pageSize) {
            json_context.currentPageNumber--;
        }

        updateNavIndex();
        nav_refresh_table();
    } else {

        json_context.navSingleNavigationIndex++;

        if (json_context.navSingleNavigationIndex >= (json_context.navRightNumber - json_context.offset)) {
            json_context.navSingleNavigationIndex = json_context.navRightNumber - json_context.offset - 1;
        }

        let navIndex = document.getElementById("navIndex");
        navIndex.value = json_context.navLeftNumber + json_context.navSingleNavigationIndex;

        let navMax = document.getElementById("navMax");
        navMax.textContent = "\u00A0/ " + json_context.navRightNumber;

        let object = json_context.active_data_list[navIndex.value - 1 - json_context.offset];

        if (json_context.oldVersion) {
            let data = [];
            for (let i = 0; i < json_context.active_data_list.length; i++) {
                object = json_context.active_data_list[i];
                if (!object[1].oldVersion) {
                    data.push(object);
                }
            }
            object = data[navIndex.value - 1 - json_context.offset];
        }

        json_context.activeID = object[1].id;
        breadcrumbPop();
        if (json_context.active_view === "form") {
            let schema = parseDataForm();
            display_Form(schema);
        } else if (json_context.active_view === "graph") {
            display_graph();
        }
        displayAttributeTable();
    }
}

/* parses and validates the value entered for navigation */
function parseIndexVal(value) {
    if (json_context.active_view === "form" || json_context.active_view === "graph") {
        if (Number.isInteger(+value)) {
            if (+value - json_context.offset <= json_context.active_data_list.length && +value > json_context.offset) {

                json_context.navSingleNavigationIndex = +value - json_context.navLeftNumber;

                let navIndex = document.getElementById("navIndex");
                navIndex.value = json_context.navLeftNumber + json_context.navSingleNavigationIndex;

                let navMax = document.getElementById("navMax");
                navMax.textContent = "\u00A0/ " + json_context.navRightNumber;

                let object = json_context.active_data_list[navIndex.value - 1 - json_context.offset];
                json_context.activeID = object[1].id;
                breadcrumbPop();
                if (json_context.active_view === "form") {
                    parseDataForm();
                    display_Form();
                    displayAttributeTable();
                } else if (json_context.active_view === "graph") {
                    display_graph();
                }
            } else
                $("#navIndex").notify("Unvalid index, must be between " + (json_context.offset + 1) + " and " + (json_context.offset + json_context.active_data_list.length), "warn");
        } else
            $("#navIndex").notify("Unvalid format, should be a positive number", "warn");
    } else {
        let re = /^[0-9]+(-[0-9]+)*$/;
        let ret = value.match(re);
        if (ret !== null && ret[1] !== undefined && ret[0] !== undefined) {
            let left = ret[0].split('-')[0];
            let right = ret[0].split('-')[1];
            if (left < 1) {
                left = 1;
            }
            if (left > json_context.active_data_list_count) {
                left = json_context.active_data_list_count;
            }

            if (right < 1) {
                right = 1;
            }

            if (right > json_context.active_data_list_count) {
                right = json_context.active_data_list_count;
            }

            if (+left > +right) {
                $("#navIndex").notify("The left number must be smaller than the rigth one", "warn");
            } else {
                json_context.navLeftNumber = left;
                json_context.navRightNumber = right;
                if (json_context.navRightNumber > json_context.active_data_list_count) {
                    json_context.navRightNumber = json_context.active_data_list_count;
                }

                json_context.pageSize = right - left + 1;
                json_context.firstPageSize = (json_context.navLeftNumber - 1) % json_context.pageSize;
                if (json_context.firstPageSize < 0) {
                    json_context.firstPageSize = 0;
                }

                json_context.currentPageNumber = Math.ceil((json_context.navLeftNumber / json_context.pageSize));
                let navIndex = document.getElementById("navIndex");
                navIndex.value = json_context.navLeftNumber + "-" + json_context.navRightNumber;

                let navMax = document.getElementById("navMax");
                navMax.textContent = "\u00A0/ " + json_context.active_data_list_count;

                nav_refresh_table(json_context.navLeftNumber, json_context.navRightNumber);
            }
        } else {
            $("#navIndex").notify("Unvalid format, please use format \"1-9\"", "warn");
        }
    }
}

// computes the navigation indeses in the list view
function updateNavIndex() {

    let navIndex = document.getElementById("navIndex");
    let navMax = document.getElementById("navMax");
    if (json_context.currentPageNumber > 0) {
        if (json_context.navLeftNumber < 1) {
            json_context.navLeftNumber = 1;
            json_context.firstPageSize = 0;
            if (json_context.active_data_list_count > json_context.pageSize) {
                json_context.navRightNumber = json_context.pageSize;
            } else {
                json_context.navRightNumber = json_context.active_data_list_count;
            }
        } else {

            if (json_context.firstPageSize + (json_context.pageSize * json_context.currentPageNumber) < json_context.active_data_list_count) {

                json_context.navLeftNumber = 1 + json_context.firstPageSize + (json_context.pageSize * (json_context.currentPageNumber - 1));
                json_context.navRightNumber = json_context.firstPageSize + (json_context.pageSize * json_context.currentPageNumber);
            } else {
                json_context.navLeftNumber = 1 + json_context.firstPageSize + (json_context.pageSize * (json_context.currentPageNumber - 1));
                json_context.navRightNumber = json_context.active_data_list_count;
            }
        }

        navIndex.value = json_context.navLeftNumber + "-" + json_context.navRightNumber;
    } else {
        navIndex.value = "0-0";
    }

    if (json_context.active_view === "form" || json_context.active_view === "graph") {
        navMax.textContent = "\u00A0/ " + json_context.navRightNumber;
    }
    else {
        navMax.textContent = "\u00A0/ " + json_context.active_data_list_count;
    }
}

/* refreshed the list table according to navigation values */
function nav_refresh_table(forceBegin, forceEnd) {

    let new_offset = json_context.firstPageSize + json_context.pageSize * (json_context.currentPageNumber - 1);

    if (json_context.limit !== json_context.pageSize) {
        json_context.limit = json_context.pageSize;
    }
    if (json_context.offset !== new_offset) {
        json_context.offset = new_offset;
        let currentPageNumber = json_context.currentPageNumber;
        request_data();
        json_context.currentPageNumber = currentPageNumber;
        breadcrumbUpdateActiveData(json_context.active_data_list, json_context.active_data_list_count);
    }

    breadcrumbUpdateNavigation();

    let data = parseDataForTable(json_context.active_data_list);


    let begin = json_context.firstPageSize + json_context.pageSize * (json_context.currentPageNumber - 1) - json_context.offset;
    let end = json_context.firstPageSize + json_context.pageSize * (json_context.currentPageNumber) - json_context.offset;


    let datatableSize = json_context.pageSize;
    if (data.length < json_context.pageSize) {
        datatableSize = data.length;
    }

    let dataOrig = data;
    data = [];
    for (let i = begin; i < end; i++) {
        data[i - begin] = dataOrig[i];
    }

    if (json_context.oldVersion) {
        let dataOrig = data;
        data = [];
        for (let i = 0; i < datatableSize; i++) {
            data.push(dataOrig[i]);
        }
        datatableSize += json_context.oldLength;
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
        pageSize: datatableSize,
        pagerHeight: 0
    });

    $('#pagerdataTable').remove(); // Remove the pagerTable of the jqxTreeGrid, we will made our.

    let res = document.getElementById("columntabledataTable");
    if (res.firstChild !== null) {
        res.firstChild.firstChild.remove();
        res.firstChild.id = "Select";
        $('#Select').append('<input id="checkBoxMaster" onClick="checkBoxControl()" class="checkAllBox" type="checkbox">');
    }
}


function searchButton() {
    json_context.searchFilter = document.getElementById('filterInput').value;
    json_context.searchIndex = document.getElementById('objectIndexSelect').value;
    if (json_context.active_model) {
        breadcrumbClean();
        json_context.mode = "view";
        json_context.pageSize = 20;
        json_context.navLeftNumber = 0;
        json_context.offset = 0;
        json_context.limit = json_context.pageSize;
        display_list();
        updateNavIndex();
        json_context.activeID = false;
        displayAttributeTable();
    }
}

function isIE() {
    var ua = window.navigator.userAgent; //Check the userAgent property of the window.navigator object
    var msie = ua.indexOf('MSIE '); // IE 10 or older
    var trident = ua.indexOf('Trident/'); //IE 11

    return (msie > 0 || trident > 0);
}

function displayOldVersionToggleCompatibility() {

    $('#toggle-div').empty();
    $('#toggle-div').append('<span class="toggle-text">Old Versions</span><input id="toggle-checkbox" class="checkOldBox" type="checkbox" autocomplete="off">');

}

function initNavbar() {
    hide_btn();

    if (isIE()) {
        displayOldVersionToggleCompatibility();
    }


    $("#navIndex").keyup(function (event) {

        if (event.keyCode === 13) {

            let val = $('#navIndex').val();
            parseIndexVal(val);
        }
    });


    $("#filterInput").keyup(function (event) {

        if (event.keyCode === 13) {

            /*
            if (json_context.active_model) {
                breadcrumbClean();
                json_context.searchFilter = document.getElementById('filterInput').value;
                json_context.searchIndex = document.getElementById('objectIndexSelect').value;
                json_context.mode = "view";
                json_context.pageSize = 20;
                json_context.navLeftNumber = 0;
                json_context.offset = 0;
                //breadcrumbPop();

                console.log("display_list Filter");

                display_list();
                updateNavIndex();
                json_context.activeID = false;
                displayAttributeTable();
            }
            */
        }
    });

    $('#toggle-checkbox').change(function () {

        if (this.checked) {
            json_context.oldVersion = true;
        } else {
            json_context.oldVersion = false;
        }

        if (json_context.active_model) {
            breadcrumbClean();

            json_context.mode = "view";
            let currentPageNumber = json_context.currentPageNumber;
            let offset = json_context.offset;
            request_data();
            updateNavIndex();
            json_context.currentPageNumber = currentPageNumber;
            json_context.offset = offset;

            display_list(json_context.active_data_list);
            updateNavIndex();
            nav_refresh_table();
        }
    });

    $('#objectIndexSelect').change(function () {

        searchButton();

    });

//Prevent from windows reload after submitting formSearch
    $("#formSearch").submit(function (e) {
        e.preventDefault();
    });
}

function updateActions() {

    if (json_context.active_schema) {
        let check = document.getElementById("dropdownUL");

        let action_list = json_context.active_schema.ModelActionList;

        //console.log("action_list");
        //console.log(action_list);

        $('#dropdownUL').empty();

        for (let i = 0; i < action_list.length; i++) {
            $('#dropdownUL').append(' <li><a onclick="actionList(' + i + ')">' + action_list[i][1] + '</a></li>');
        }
    }
}

function actionList(i) {

    let action_list = json_context.active_schema.ModelActionList;
    let select = true;
    let model = "";

    if (action_list[i][3]) {
        if (action_list[i][3].hasOwnProperty("model")) {
            model = action_list[i][3].model;
        }
        if (action_list[i][3].hasOwnProperty("select")) {
            select = action_list[i][3].select;
        }
    }

    let checked = getSelectedUIDS();
    if (checked.length === 0) {
        if (json_context.activeID) {
            checked = [];
            checked.push(json_context.activeID.substring(json_context.activeID.lastIndexOf(".") + 1));
        }
    }

    if (select && checked.length === 0) {
        $("#dropdownMenu1").notify("Please select at least one object to perform action", "warn");
    } else {
        switch (action_list[i][2]) {
            case 'Execute':
                actionRequest(action_list[i][0]);
                break;
            case 'ExecuteConfirm':
                if (action_list[i][3].Message) {
                    let check = confirm(action_list[i][3].Message);
                    if (check === true) {
                        actionRequest(action_list[i][0]);
                    }
                } else {
                    actionRequest(action_list[i][0]);
                }
                break;
            case 'SelectSingle':
                launchModalList(true, false, model, function (dataModal) {
                    if (dataModal) {

                        console.log("datamodal: " + JSON.stringify(dataModal));
                        if (dataModal[0]) {
                            let args = [];
                            args.push(dataModal[0]);
                            actionRequest(action_list[i][0], args);
                        }
                    }
                });
                break;
            case 'SelectMulti':
                launchModalList(false, false, model, function (dataModal) {
                    if (dataModal) {
                        console.log("datamodal: " + JSON.stringify(dataModal));
                        //if (dataModal[0]) {
                        let args = [];
                        for (let i = 0; i < dataModal.length; i++) {
                            args.push(dataModal[i]);
                        }
                        actionRequest(action_list[i][0], args);
                        //}
                    }
                });
                break;
            case 'SelectSingleOld':
                launchModalList(true, true, model, function (dataModal) {
                    if (dataModal) {

                        console.log("datamodal: " + JSON.stringify(dataModal));
                        if (dataModal[0]) {
                            let args = [];
                            args.push(dataModal[0]);
                            actionRequest(action_list[i][0], args);
                        }
                    }
                });
                break;
            case 'SelectMultiOld':
                launchModalList(false, true, model, function (dataModal) {
                    if (dataModal) {

                        console.log("datamodal: " + JSON.stringify(dataModal));
                        //if (dataModal[0]) {
                        let args = [];
                        for (let i = 0; i < dataModal.length; i++) {
                            args.push(dataModal[i]);
                        }
                        actionRequest(action_list[i][0], args);
                        //}
                    }
                });
                break;
            case 'TransientObject':
                launchModalForm(model, function (dataModalForm) {
                    if (dataModalForm) {
                        actionRequest(action_list[i][0], dataModalForm);
                    }
                });
                break;
            default:
                break;
        }
    }
}

function breadcrumbClean() {
    json_context.breadcrumb = [];
}

function breadcrumbBack(index) {


    if (json_context.breadcrumb[index]) {

        let IDSCache = [];
        let SchemaCache = {};
        let HistoryCache = {};
        let searchIndex = json_context.searchIndex;
        let oldVersion = json_context.oldVersion;

        IDSCache = json_context.IDSCache;
        SchemaCache = json_context.SchemaCache;
        HistoryCache = json_context.HistoryCache;


        let context = JSON.parse(json_context.breadcrumb[index][1]);
        let view = context.active_view;

        json_context = context;

        json_context.IDSCache = IDSCache;
        json_context.SchemaCache = SchemaCache;
        json_context.HistoryCache = HistoryCache;
        json_context.searchIndex = searchIndex;
        json_context.oldVersion = oldVersion;

        let object = getObjectWithID(json_context.activeID);
        let schema = json_context.active_schema;

        if (object) {
            schema = getModelSchemaFromObject(object);
        }

        switch (view) {
            case "list":
                json_context.breadcrumb = [];
                display_list(json_context.active_data_list);
                updateNavIndex();
                display_button();
                break;
            case "form":
                parseDataForm(object, schema);
                display_Form(schema);
                break;
            case "graph":
                display_graph();
                break;
            default:
                ;
        }

        displayAttributeTable(object, schema);
    }
}

function breadcrumbUpdateNavigation() {

    if (json_context.breadcrumb.length >= 1) {
        let context = JSON.parse(json_context.breadcrumb[0][1]);

        context.navLeftNumber = json_context.navLeftNumber;
        context.navRightNumber = json_context.navRightNumber;
        context.navSingleNavigationIndex = json_context.navSingleNavigationIndex;
        context.offset = json_context.offset;
        context.limit = json_context.limit;
        context.currentPageNumber = json_context.currentPageNumber;
        context.pageSize = json_context.pageSize;
        context.firstPageSize = json_context.firstPageSize;

        json_context.breadcrumb[0][1] = JSON.stringify(context);
    }
}

function breadcrumbUpdateActiveData(active_data_list, active_data_list_count) {

    if (json_context.breadcrumb.length >= 1) {
        let context = JSON.parse(json_context.breadcrumb[0][1]);
        context.active_data_list = active_data_list;
        context.active_data_list_count = active_data_list_count;
        json_context.breadcrumb[0][1] = JSON.stringify(context);
    }
}

function breadcrumbUpdateActiveID(activeID) {

    if (json_context.breadcrumb.length === 1) {
        let context = JSON.parse(json_context.breadcrumb[0][1]);
        context.activeID = activeID;
        json_context.breadcrumb[0][1] = JSON.stringify(context);
    }


}

function breadcrumbPop() {

    json_context.breadcrumb.pop();
    $('.breadcrumb').remove();
    $('#breadcrumbContainer').append('<ol class="breadcrumb" id="breadcrumb"></ol>');
    for (let i = 0; i < json_context.breadcrumb.length; i++) {
        if (i + 1 !== json_context.breadcrumb.length || json_context.breadcrumb.length === 1) {
            $('#breadcrumb').append('<li class="breadForm bcFont"><a onclick="breadcrumbBack(' + i + ')">' + json_context.breadcrumb[i][0] + '</a></li>');
        } else {
            $('#breadcrumb').append('<li class="active breadForm bcFont">' + json_context.breadcrumb[i][0] + '</li>');
        }
    }
}


function breadcrumbPush(name, json_context) {

    if (json_context.breadcrumb.length > 5) {
        breadcrumbPop();
    }

    let data = [];
    data.push(name);
    data.push(JSON.stringify(json_context));

    json_context.breadcrumb.push(data);

    if (json_context.breadcrumb.length > 2 || json_context.hideButton) {
        $("#create_edit").hide();
        $("#save_cancel").hide();
        $("#actions").hide();
        $("#list_navigation").hide();
        $("#btnBlock").hide();
    }
    else {
        $("#list_navigation").show();
        $("#btnBlock").show();
    }

    $('#breadcrumb').remove();
    $('#breadcrumbContainer').append('<ol class="breadcrumb" id="breadcrumb"></ol>');


    for (let i = 0; i < json_context.breadcrumb.length; i++) {

        if (i < json_context.breadcrumb.length - 1 || i == 0) {
            $('#breadcrumb').append('<li class="breadForm bcFont"><a onclick="breadcrumbBack(' + i + ')">' + json_context.breadcrumb[i][0] + '</a></li>');
        } else {
            $('#breadcrumb').append('<li class="breadForm bcFont">' + json_context.breadcrumb[i][0] + '</li>');
        }
    }
}

function printBtn() {
    var divToPrint = document.getElementById("dataTable");
    divToPrint.childNodes[1].childNodes[0].childNodes[3].childNodes[0].childNodes[0].childNodes[0].remove();
    let Win = window.open("");
    Win.document.write(divToPrint.outerHTML);
    Win.print();
    Win.close();
}

function HTMLExport() {
    var divToPrint = document.getElementById("dataTable");
    divToPrint.childNodes[1].childNodes[0].childNodes[3].childNodes[0].childNodes[0].childNodes[0].remove();
    let Win = window.open("");
    Win.document.write(divToPrint.outerHTML);
}

function excelExport() {
    $('#dataTable').tableExport({
        type: 'excel',
        headings: true,
        escape: 'false',
        tableName: json_context.active_model + "_list",
        ignoreColumn: '[0]',
        JQWidget: true // Flag for making the export working with the JQXTREEGRID
    });
}

function CSVExport() {
    $('#dataTable').tableExport({
        type: 'csv',
        escape: 'false',
        tableName: json_context.active_model + "_list",
        ignoreColumn: '[0]',
        JQWidget: true // Flag for making the export working with the JQXTREEGRID
    });
}

function JSONExport() {
    $('#dataTable').tableExport({
        type: 'json',
        escape: 'false',
        tableName: json_context.active_model + "_list",
        ignoreColumn: '[0]',
        JQWidget: true // Flag for making the export working with the JQXTREEGRID
    });
}

function PDFExport() {
    let attr = getAttrVisible();
    var records = $("#dataTable").jqxTreeGrid("getCheckedRows");

    if (records.length === 0) {
        records = $("#dataTable").jqxTreeGrid("getRows");
    }

    let rows = [];
    let line = [];

    for (let i = 0; i < records.length; i++) {
        line = [];
        for (let j = 0; j < attr.length; j++) {
            line.push($("<div>").html(records[i][attr[j]]).text());
        }
        rows.push(line);
    }

    var doc = new jsPDF('p', 'pt');
    doc.autoTable(attr, rows);
    doc.save(json_context.active_model + '_list.pdf');
}

