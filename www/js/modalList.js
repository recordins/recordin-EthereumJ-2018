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

function createModalList() {
    if (isIE()) {
        $("body").append('<div class="modal fade" id="myModal"></div>');
    }
    else {
        $('#main-div').append('<div class="modal fade" id="myModal"></div>');
    }
    let content = '<div class="modal-content" id="modal-content">' +
        '<div class="modal-header" id="modal-header"><div><h3></h3></div>' +
        '<span class="close closebtn" data-dismiss="modal" data-backdrop="false">&times;</span></div><br>' +
        '<div class="modal-body" id="modal-body"></div>' +
        '</div>';
    $('#myModal').append(content);
}

function cleanModal() {
    $("#idModal").remove();
    $("#navModal").remove();
}

let modalReturnFunction = null;
let modalFilter = "";
let modalIndex = "";
let modalOldVersion = false;
let modalModel = "";
let modalSingle = true;
let modalOld = false;
let modalData = [];
let modalDataListCount = 0;
let modalSchema = {};


let modalLeftNumber = 0;
let modalRightNumber = 0;
let modalCurrentPageNumber = 1;
let modalPageSize = 20;
let modalFirstPageSize = 0;
let modalOffset = 0;
let modalLimit = modalPageSize;

let modalDataCount = 1;

let modalSelectedItemList = [];
let modalCheckAllFlag = false;

let modalSelectedIDS = [];

let preventBlur = false;


function launchModalList(single, old, model, returnFunctionCall, filterCall, indexCall, selectedIDSCall) {

    modalOffset = 0;
    if (document.getElementById("modal-content") === null) {
        createModalList();
        modalOldVersion = false;
        modalLeftNumber = 0;
        modalRightNumber = 0;
        modalCurrentPageNumber = 1;
        modalPageSize = 20;
        modalFirstPageSize = 0;
        modalLimit = modalPageSize;

        modalDataCount = 1;

        modalSelectedItemList = [];
        modalCheckAllFlag = false;

        modalSelectedIDS = [];
    }
    if (selectedIDSCall) {
        modalSelectedIDS = selectedIDSCall;
    }
    modalOldVersion = false;
    modalSingle = single;
    modalOld = old;
    modalModel = model;
    modalReturnFunction = returnFunctionCall;
    modalFilter = "";
    if (filterCall) {
        modalFilter = filterCall;
    }
    if (indexCall) {
        modalIndex = indexCall;
    }

    var modal = document.getElementById('myModal');
    var span = document.getElementsByClassName("closebtn")[0];
    $('#myModal').modal('toggle');

    span.onclick = function () {
        $('#myModal').modal('toggle');

    };
    window.onclick = function (event) {
        if (event.target === modal) {
        }
    };

    cleanModal();

    let modalHTML = '<nav id="navModal" class="navbar navbar-default">' +
        '<div>' +
        '<div class="collapse navbar-collapse block" id="searchDivModal" style="float: right">' +
        '<form id="formSearchModal" class="navbar-form navbar-left">';

    if (old) {
        modalHTML += '<div id="toggle-divModal" class="block toggle-div">' +
            '<span class="toggle-text">Old Versions</span>' +
            '<label class="switch toggle-btn">' +
            '<input id="toggle-checkboxModal" type="checkbox" autocomplete="off">' +
            '<span class="slider round"></span>' +
            '</label>' +
            '</div>';
    }
    modalHTML += '<select id="objectIndexSelectModal" class="form-control index_list">';

    $("#objectIndexSelect > option").each(function () {
        let selected = '';
        if (this.value === modalIndex) {
            selected = ' selected="selected" ';
        }

        modalHTML += '<option value="' + this.value + '" ' + selected + '>' + this.text + '</option>';
    });

    modalHTML += '</select>' +
        '<div class="form-group">' +
        '<input id="filterInputModal" type="text" class="form-control searchbar" placeholder="Search">' +
        ' </div>' +
        '<button type="button" class="btn btn-default" id="searchBtnModal" onClick="searchButtonModal()"><i class="fas fa-search" ></i></button>' +
        '</form>' +
        ' </div>' +
        ' </div>' +
        ' <div>' +
        ' <div class="block left_btn" id="saveCancelModal">' +
        '<button id="saveButtonCloseModal" type="button" class="btn btn-success" onclick="SaveBtnCloseModal(' + modalSingle + ',' + modalOld + ',\'' + modalModel + '\')">Select and Close</button>&nbsp;' +
        '<button type="button" class="btn btn-danger" onclick="ClearBtnModal()">Clear Values</button>&nbsp;' +
        '<button type="button" class="btn btn-primary" onclick="CancelBtnModal()">Cancel</button>' +
        '</div>' +
        ' </div>' +
        ' <div class="view-btn block">' +
        ' <div id="list_navigation" class="block">' +
        '   <input id="navIndexModal" class="block listIndexModal" type="text"/>' +
        '   <span id="navMaxModal"/>' +
        '</div>' +
        '<div class="block pagin">' +
        '<button class="btn btn-default" onclick="PrevPageModal()"><i class="fas fa-chevron-left fa-lg" ></i></button>' +
        '<button class="btn btn-default" onclick="NextPageModal()"><i class="fas fa-chevron-right fa-lg"></i></button>' +
        ' </div>' +
        ' </div>' +
        '</div>' +
        '</div>' +
        ' </nav>';

    $('#modal-header').append(modalHTML);

    $('#modal-body').append('<div class="idModal" id="idModal"></div>');


    if (isIE()) {
        displayModalOldVersionToggleCompatibility();
    }
    modalSchema = RequestGET('/model/JsonSchema?model=' + modalModel, false);

    document.getElementById('filterInputModal').value = modalFilter;
    document.getElementById('objectIndexSelectModal').value = modalIndex;

    listenEventsListModal(modalSingle, modalOld, modalModel);

    displayListModal();

}

function displayModalOldVersionToggleCompatibility() {

    $('#toggle-divModal').empty();
    $('#toggle-divModal').append('<span class="toggle-text">Old Versions</span><input id="toggle-checkboxModal" class="checkOldBox" type="checkbox" autocomplete="off">');

}

function requestDataModal() {

    let get = '/orm/Search?menuID=' + json_context.active_menuID + '&model=' + modalModel + "&index=" + modalIndex + "&oldVersion=" + modalOldVersion + "&filter=" + modalFilter + "&offset=" + modalOffset + "&limit=" + modalLimit;
    var obj = RequestGET(get, false);
    if (obj) {
        modalData = obj.BlockchainObjects;
        modalDataListCount = obj.Count - obj.CountOld;
    }

    let ids = lookupIDS(modalData);
    loadIDS(ids);
}

function displayListModal() {

    requestDataModal();

    let dataParsed = parseDataForTable(modalData, modalSchema, true);

    let source = {
        dataType: "json",
        dataFields: json_context.modal_active_data_list_fields,
        hierarchy: {root: 'old'},
        localData: dataParsed
    };

    let dataAdapter = new $.jqx.dataAdapter(source);
    modalDataCount = dataParsed.length;

    if (modalDataCount === 0) {
        modalCurrentPageNumber = 0;
    } else {
        modalCurrentPageNumber = 1;
    }

    let selectionMode = "singleRow";

    if (!modalSingle) {
        selectionMode = "multipleRows"
    }

    if (modalSingle) {

        $("#idModal").jqxTreeGrid({
            width: '100%',
            height: "100%",
            source: dataAdapter,
            sortable: true, // Make the collumns sortable (not the checkbox, see attrCollumn)
            columns: attrCollumns,
            pageable: true, // True but the pager navigation is totally remade
            pagerMode: 'advanced',
            altRows: true,
            hierarchicalCheckboxes: false,
            checkboxes: true,
            columnsResize: true,
            pageSize: modalPageSize,
            pagerHeight: 0,
            selectionMode: selectionMode
        });
    } else {

        $("#idModal").jqxTreeGrid({
            width: '100%',
            height: "100%",
            source: dataAdapter,
            sortable: true, // Make the collumns sortable (not the checkbox, see attrCollumn)
            columns: attrCollumns,
            pageable: true, // True but the pager navigation is totally remade
            pagerMode: 'advanced',
            altRows: true,
            hierarchicalCheckboxes: false,
            checkboxes: true,
            columnsResize: true,
            pageSize: modalPageSize,
            pagerHeight: 0,
            selectionMode: selectionMode
        });
    }
    document.getElementById('idModal').className = "hideTable";

    $('#idModal').jqxPanel('scrollTo', 0, 0);

    if (!modalSingle) {
        try {
            let res = document.getElementById("columntableidModal");

            if (res.firstChild !== null) {
                res.firstChild.firstChild.remove();
                res.firstChild.id = "SelectModal";
                $('#SelectModal').append('<input id="checkBoxMasterModal" onClick="checkBoxControlModal()" class="checkAllBoxModal" type="checkbox">');
            }

            modalCheckAllFlag = false;
        } catch (e) {
        }
    }
    updateNavIndexModal();
    $('#pageridModal').remove(); // Remove the pagerTable of the jqxTreeGrid, we will made our.

    if (!modalOld) {
        let modalSelectedIDSCurrent = [];
        for (let i = 0; i < modalSelectedIDS.length; i++) {
            modalSelectedIDSCurrent.push(getCurrentVersionID(modalSelectedIDS[i]));
        }
        modalSelectedIDS = modalSelectedIDSCurrent;
    }

    console.log(modalSelectedIDS);
    for (let i = 0; i < modalSelectedIDS.length; i++) {


        var rows = $("#idModal").jqxTreeGrid('getRows');

        for (let j = 0; j < rows.length; j++) {

            if (rows[j].blockID === modalSelectedIDS[i]) {
                $("#idModal").jqxTreeGrid('checkRow', j);
            }

            if (rows[j].records) {
                let oldRecords = rows[j].records;
                for (let k = 0; k < oldRecords.length; k++) {
                    if (oldRecords[k].blockID === modalSelectedIDS[i]) {
                        $("#idModal").jqxTreeGrid('checkRow', j + "_" + k);
                    }
                }
            }
        }
    }

    setTimeout(function () {
        document.getElementById('idModal').className = "showTable";
    }, 500);

}

function CancelBtnModal() {
    $('#myModal').modal('toggle');
}

function searchButtonModal() {
    modalFilter = document.getElementById('filterInputModal').value;
    modalIndex = document.getElementById('objectIndexSelectModal').value;

    displayListModal();
}

/*Remove and create the checkbox checked or not, cause the checkbox don't check itself*/
function checkBoxControlModal() {
    if (modalCheckAllFlag === false) {
        if (!modalSingle) {
            $(".checkAllBox").remove();
            $('#SelectModal').append('<input id="checkBoxMasterModal" onClick="checkBoxControlModal()" class="checkAllBoxModal" type="checkbox" checked>');
        }
        modalCheckAllFlag = true;
        checkAllModal();
    } else {
        if (!modalSingle) {
            $(".checkAllBox").remove();
            $('#SelectModal').append('<input id="checkBoxMasterModal" onClick="checkBoxControlModal()" class="checkAllBoxModal" type="checkbox">');
        }
        modalCheckAllFlag = false;
        uncheckAllModal();
    }
}

function getSelectedIDSModal() {

    var rows = $("#idModal").jqxTreeGrid('getRows');
    let result = [];

    for (let i = 0; i < rows.length; i++) {
        if (rows[i].checked) {
            result.push(rows[i].blockID);
        }
        if (rows[i].records) {
            let oldRecords = rows[i].records;
            for (let j = 0; j < oldRecords.length; j++) {
                if (oldRecords[j].checked) {
                    result.push(oldRecords[j].blockID);
                }
            }
        }
    }

    return result;
}

function uncheckAllModal() {
    var rows = $("#idModal").jqxTreeGrid('getRows');
    for (let i = 0; i < rows.length; i++) {
        $("#idModal").jqxTreeGrid('uncheckRow', i);

        if (rows[i].records) {
            let oldRecords = rows[i].records;
            for (let j = 0; j < oldRecords.length; j++) {
                $("#idModal").jqxTreeGrid('uncheckRow', i + "_" + j);
            }
        }
    }
    modalSelectedIDS = [];
}

function checkAllModal() {
    var rows = $("#idModal").jqxTreeGrid('getRows');
    for (let i = 0; i < rows.length; i++) {
        $("#idModal").jqxTreeGrid('checkRow', i);

        if (rows[i].records) {
            let oldRecords = rows[i].records;
            for (let j = 0; j < oldRecords.length; j++) {
                $("#idModal").jqxTreeGrid('checkRow', i + "_" + j);
            }
        }
    }
}

var eventFlag = true;

function listenEventsListModal(single, old, model) {

    $("#navIndexModal").blur(function () {

        if (!preventBlur) {
            let val = $('#navIndexModal').val();
            parseIndexValModal(val);
        } else {
            preventBlur = false;
        }
    });

    $("#navIndexModal").keyup(function (event) {
        if (event.keyCode === 13) {

            let val = $('#navIndexModal').val();
            parseIndexValModal(val);
        }
    });

    function searchButton() {
        modalFilter = document.getElementById('filterInputModal').value;
        modalIndex = document.getElementById('objectIndexSelectModal').value;

        modalPageSize = 20;
        modalLeftNumber = 0;
        modalOffset = 0;
        modalLimit = modalPageSize;
        displayListModal();
        updateNavIndexModal();
    }

    $("#filterInputModal").keyup(function (event) {
        if (event.keyCode === 13) {
            searchButton();
        }
    });

    $('#objectIndexSelectModal').change(function () {
        searchButton();
    });

    $('#idModal').on('rowDoubleClick', function (event) {
        preventBlur = true;

        var args = event.args;
        var row = args.row;
        if (!modalSingle) {
            if (!row.checked) {
                modalCheckAllFlag = false;
            }
        } else {
            if (eventFlag) {
                eventFlag = false;
                uncheckAllModal();
                $("#idModal").jqxTreeGrid('checkRow', row.uid);
                eventFlag = true;
            }
            SaveBtnCloseModal(single, old, model);
        }
    });

    $('#idModal').on('rowUncheck', function (event) {
        //    console.log("UNrowCheck");
        preventBlur = true;

        var args = event.args;
        var row = args.row;

        if (eventFlag) {
            eventFlag = false;

            let updatedSelectedList = [];
            for (let i = 0; i < modalSelectedIDS.length; i++) {
                if (modalSelectedIDS[i] !== row.blockID) {
                    updatedSelectedList.push(modalSelectedIDS[i]);
                }
            }
            modalSelectedIDS = updatedSelectedList;

            eventFlag = true;
        }
        modalCheckAllFlag = false;

        SaveBtnModal();
    });

    $('#idModal').on('rowCheck', function (event) {
        preventBlur = true;

        var args = event.args;
        var row = args.row;


        if (eventFlag) {
            eventFlag = false;

            if (modalSingle) {
                uncheckAllModal();
                $("#idModal").jqxTreeGrid('checkRow', row.uid);
            }
            modalSelectedIDS.push(row.blockID);

            eventFlag = true;
        }

        SaveBtnModal();
    });

    $('#toggle-checkboxModal').change(function () {
        if (this.checked) {
            modalOldVersion = true;
        } else {
            modalOldVersion = false;
        }

        let tmpModalCurrentPageNumber = modalCurrentPageNumber;
        let tmpOffset = modalOffset;
        displayListModal();
        modalCurrentPageNumber = tmpModalCurrentPageNumber;
        modalOffset = tmpOffset;

        nav_refresh_table_modal();

    });

    //Prevent from windows reload after submitting formSearch
    $("#formSearchModal").submit(function (e) {
        e.preventDefault();
    });

}

function PrevPageModal() {
    modalCurrentPageNumber--;

    if (modalCurrentPageNumber <= 0) {
        modalCurrentPageNumber = 1;
        modalFirstPageSize = 0;
    }

    updateNavIndexModal();
    nav_refresh_table_modal();
}

function NextPageModal() {
    modalCurrentPageNumber++;

    if (1 + modalFirstPageSize + (modalPageSize * modalCurrentPageNumber) > modalDataListCount + modalPageSize) {
        modalCurrentPageNumber--;
    }

    updateNavIndexModal();
    nav_refresh_table_modal();
}

function parseIndexValModal(value) {

    let re = /^[0-9]+(-[0-9]+)*$/;
    let ret = value.match(re);
    if (ret !== null && ret[1] !== undefined && ret[0] !== undefined) {
        let left = ret[0].split('-')[0];
        let right = ret[0].split('-')[1];

        if (left < 1) {
            left = 1;
        }
        if (left > modalDataListCount) {
            left = modalDataListCount;
        }

        if (right < 1) {
            right = 1;
        }

        if (right > modalDataListCount) {
            right = modalDataListCount;
        }

        if (+left > +right) {
            $.notify("The left number must be smaller than the rigth one", "warn");

        } else {
            modalLeftNumber = left;
            modalRightNumber = right;

            if (modalRightNumber > modalDataListCount) {
                modalRightNumber = modalDataListCount;
            }

            modalPageSize = right - left + 1;

            modalFirstPageSize = (modalLeftNumber - 1) % modalPageSize;

            if (modalFirstPageSize < 0) {
                modalFirstPageSize = 0;
            }

            modalCurrentPageNumber = Math.ceil((modalLeftNumber / modalPageSize));

            let navIndexModal = document.getElementById("navIndexModal");
            navIndexModal.value = modalLeftNumber + "-" + modalRightNumber;
            nav_refresh_table_modal(modalLeftNumber, modalRightNumber);

        }
    } else {
        $("#navIndexModal").notify("Unvalid format, please use format \"1-9\"", "warn");
    }
}

//get the index in list view
function updateNavIndexModal() {

    let navIndexModal = document.getElementById("navIndexModal");
    let navMaxModal = document.getElementById("navMaxModal");

    if (modalCurrentPageNumber > 0) {
        if (modalLeftNumber < 1) {
            modalLeftNumber = 1;
            modalFirstPageSize = 0;

            if (modalDataListCount > modalPageSize) {
                modalRightNumber = modalPageSize;
            } else {
                modalRightNumber = modalDataListCount;
            }
        } else {

            if (modalFirstPageSize + (modalPageSize * modalCurrentPageNumber) < modalDataListCount) {

                modalLeftNumber = 1 + modalFirstPageSize + (modalPageSize * (modalCurrentPageNumber - 1));
                modalRightNumber = modalFirstPageSize + (modalPageSize * modalCurrentPageNumber);

            } else {
                modalLeftNumber = 1 + modalFirstPageSize + (modalPageSize * (modalCurrentPageNumber - 1));
                modalRightNumber = modalDataListCount;
            }
        }

        navIndexModal.value = modalLeftNumber + "-" + modalRightNumber;
    } else {
        navIndexModal.value = "0-0";
    }
    navMaxModal.textContent = "\u00A0/ " + modalDataListCount;
}


function nav_refresh_table_modal(forceBegin, forceEnd) {

    let new_offset = modalFirstPageSize + modalPageSize * (modalCurrentPageNumber - 1);

    if (modalLimit !== modalPageSize) {
        modalLimit = modalPageSize;
    }
    if (modalOffset !== new_offset) {
        modalOffset = new_offset;
        let currentPageNumber = modalCurrentPageNumber;
        requestDataModal();
        modalCurrentPageNumber = currentPageNumber;
    }

    let data = parseDataForTable(modalData, modalSchema, true);

    let begin = modalFirstPageSize + modalPageSize * (modalCurrentPageNumber - 1) - modalOffset;
    let end = modalFirstPageSize + modalPageSize * (modalCurrentPageNumber) - modalOffset;

    let datatableSize = modalPageSize;
    if (data.length < modalPageSize) {
        datatableSize = data.length;
    }
    let dataOrig = data;

    data = [];
    for (let i = begin; i < end; i++) {
        data[i - begin] = dataOrig[i];
    }

    if (modalOldVersion) {
        let dataOrig = data;
        data = [];
        for (let i = 0; i < datatableSize; i++) {
            data.push(dataOrig[i]);
        }
        datatableSize += json_context.modal_oldLength;

    } else {

    }

    var source = {
        dataType: "json",
        dataFields: json_context.modal_active_data_list_fields,
        hierarchy: {root: 'old'},
        localData: data
    };

    var dataAdapter = new $.jqx.dataAdapter(source);

    $("#idModal").jqxTreeGrid({'source': dataAdapter, 'pageSize': datatableSize});


    if (!modalSingle) {
        try {
            let res = document.getElementById("columntableidModal");

            if (res.firstChild !== null) {
                res.firstChild.firstChild.remove();
                res.firstChild.id = "SelectModal";
                $('#SelectModal').append('<input id="checkBoxMasterModal" onClick="checkBoxControlModal()" class="checkAllBoxModal" type="checkbox">');
            }

            modalCheckAllFlag = false;
        } catch (e) {
        }
    }

    for (let i = 0; i < modalSelectedIDS.length; i++) {

        var rows = $("#idModal").jqxTreeGrid('getRows');

        for (let j = 0; j < rows.length; j++) {

            if (rows[j].blockID === modalSelectedIDS[i]) {
                $("#idModal").jqxTreeGrid('checkRow', j);
            }

            if (rows[j].records) {
                let oldRecords = rows[j].records;
                for (let k = 0; k < oldRecords.length; k++) {
                    if (oldRecords[k].blockID === modalSelectedIDS[i]) {
                        $("#idModal").jqxTreeGrid('checkRow', j + "_" + k);
                    }
                }
            }
        }
    }
}

function SaveBtnModal() {

    let selectedIDS = getSelectedIDSModal();


    for (let i = 0; i < selectedIDS.length; i++) {
        modalSelectedIDS.push(selectedIDS[i]);
    }

    let uniqueIDS = [];

    //Jquery way to remove duplicated data in array for IE
    $.each(modalSelectedIDS, function (i, el) {
        if ($.inArray(el, uniqueIDS) === -1 && el !== "")
            uniqueIDS.push(el);
    });

    modalSelectedIDS = uniqueIDS;
}


function SaveBtnCloseModal(single, old, index) {

    let selectedIDS = getSelectedIDSModal();
    for (let i = 0; i < selectedIDS.length; i++) {
        modalSelectedIDS.push(selectedIDS[i]);
    }
    let uniqueIDS = [];

    //Jquery way to remove duplicated data in array for IE
    $.each(modalSelectedIDS, function (i, el) {
        if ($.inArray(el, uniqueIDS) === -1 && el !== "") {
            uniqueIDS.push(el);
        }
    });

    if (single && selectedIDS.length > 1) {
        $("#saveButtonCloseModal").notify("Please select one object", "warn");
    } else {

        for (let i = 0; i < selectedIDS.length; i++) {
            let obj = getObjectWithIDFromData(modalData, uniqueIDS[i]);
            json_context.IDSCache.push(obj);
        }

        if (modalReturnFunction) {
            modalReturnFunction(uniqueIDS);
        }

        $('#myModal').modal('toggle');

    }
}

function ClearBtnModal() {

    if (modalReturnFunction) {
        modalReturnFunction([]);
    }

    $('#myModal').modal('toggle');
}


