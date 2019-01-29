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

function init_kanban() {

    if (json_context.active_model) {

        json_context.breadcrumb = [];
        json_context.mode = "view";
        json_context.pageSize = 20;
        json_context.navLeftNumber = 0;

        json_context.active_view = "kanban"

        display_kanban();
        updateNavIndex();
        display_button();
        json_context.create = false;

    }
}


function display_kanban() {
    clean_div();

    let form = document.createElement("div");
    form.id = "kanban1";
    form.className = "kanban1";
    document.getElementById('main-div').appendChild(form);
    document.getElementById('main-div').style.height = '80%';
    $('#main-div').jqxPanel('scrollTo', 0, 0);
    //json_context.active_view = "kanban";

    document.getElementById('main-div').style.overflow = ""; // Dont display two scrollBar for the treegrid


    var fields = [
        {name: "id", type: "string"},
        {name: "status", map: "state", type: "string"},
        {name: "text", map: "label", type: "string"},
        {name: "tags", type: "string"},
        {name: "color", map: "hex", type: "string"},
        {name: "resourceId", type: "number"}
    ];
    var source =
        {
            localData: [
                {
                    id: "1161",
                    state: "new",
                    label: "Make a new Dashboard",
                    tags: "dashboard",
                    hex: "#36c7d0",
                    resourceId: 3
                },
                {
                    id: "1645",
                    state: "work",
                    label: "Prepare new release",
                    tags: "release",
                    hex: "#ff7878",
                    resourceId: 1
                },
                {
                    id: "9213",
                    state: "new",
                    label: "One item added to the cart",
                    tags: "cart",
                    hex: "#96c443",
                    resourceId: 3
                },
                {
                    id: "6546",
                    state: "done",
                    label: "Edit Item Price",
                    tags: "price, edit",
                    hex: "#ff7878",
                    resourceId: 4
                },
                {id: "9034", state: "new", label: "Login 404 issue", tags: "issue, login", hex: "#96c443"}
            ],
            dataType: "array",
            dataFields: fields
        };
    var dataAdapter = new $.jqx.dataAdapter(source);
    var resourcesAdapterFunc = function () {
        var resourcesSource =
            {
                localData: [
                    {id: 0, name: "No name", image: "/jqwidgets/styles/images/common.png", common: true},
                    {id: 1, name: "Andrew Fuller", image: "/jqwidgets/styles/images/andrew.png"},
                    {id: 2, name: "Janet Leverling", image: "/jqwidgets/styles/images/janet.png"},
                    {id: 3, name: "Steven Buchanan", image: "/jqwidgets/styles/images/steven.png"},
                    {id: 4, name: "Nancy Davolio", image: "/jqwidgets/styles/images/nancy.png"},
                    {id: 5, name: "Michael Buchanan", image: "/jqwidgets/styles/images/Michael.png"},
                    {id: 6, name: "Margaret Buchanan", image: "/jqwidgets/styles/images/margaret.png"},
                    {id: 7, name: "Robert Buchanan", image: "/jqwidgets/styles/images/robert.png"},
                    {id: 8, name: "Laura Buchanan", image: "/jqwidgets/styles/images/Laura.png"},
                    {id: 9, name: "Laura Buchanan", image: "/jqwidgets/styles/images/Anne.png"}
                ],
                dataType: "array",
                dataFields: [
                    {name: "id", type: "number"},
                    {name: "name", type: "string"},
                    {name: "image", type: "string"},
                    {name: "common", type: "boolean"}
                ]
            };
        var resourcesDataAdapter = new $.jqx.dataAdapter(resourcesSource);
        return resourcesDataAdapter;
    }
    var getIconClassName = function () {
        switch (theme) {
            case "darkblue":
            case "black":
            case "shinyblack":
            case "ui-le-frog":
            case "metrodark":
            case "orange":
            case "darkblue":
            case "highcontrast":
            case "ui-sunny":
            case "ui-darkness":
                return "jqx-icon-plus-alt-white ";
        }
        return "jqx-icon-plus-alt";
    }
    $('#kanban1').jqxKanban({
        template: "<div class='jqx-kanban-item' id=''>"
            + "<div class='jqx-kanban-item-color-status'></div>"
            + "<div style='display: none;' class='jqx-kanban-item-avatar'></div>"
            + "<div class='jqx-icon jqx-icon-close jqx-kanban-item-template-content jqx-kanban-template-icon'></div>"
            + "<div class='jqx-kanban-item-text'></div>"
            + "<div style='display: none;' class='jqx-kanban-item-footer'></div>"
            + "</div>",
        resources: resourcesAdapterFunc(),
        source: dataAdapter,
        // render items.
        itemRenderer: function (item, data, resource) {
            $(item).find(".jqx-kanban-item-color-status").html("<span style='line-height: 23px; margin-left: 5px;'>" + resource.name + "</span>");
            $(item).find(".jqx-kanban-item-text").css('background', item.color);
            item.on('dblclick', function (event) {
                var input = $("<textarea placeholder='(No Title)' style='border: none; width: 100%;' class='jqx-input'></textarea>");
                var addToHeader = false;
                var header = null;
                if (event.target.nodeName == "SPAN" && $(event.target).parent().hasClass('jqx-kanban-item-color-status')) {
                    var input = $("<input placeholder='(No Title)' style='border: none; background: transparent; width: 80%;' class='jqx-input'/>");
                    // add to header
                    header = event.target;
                    header.innerHTML = "";
                    input.val($(event.target).text());
                    $(header).append(input);
                    addToHeader = true;
                }
                if (!addToHeader) {
                    var textElement = item.find(".jqx-kanban-item-text");
                    input.val(textElement.text());
                    textElement[0].innerHTML = "";
                    textElement.append(input);
                }
                input.mousedown(function (event) {
                    event.stopPropagation();
                });
                input.mouseup(function (event) {
                    event.stopPropagation();
                });
                input.blur(function () {
                    var value = input.val();
                    if (!addToHeader) {
                        $("<span>" + value + "</span>").appendTo(textElement);
                    } else {
                        header.innerHTML = value;
                    }
                    input.remove();
                });
                input.keydown(function (event) {
                    if (event.keyCode == 13) {
                        if (!header) {
                            $("<span>" + $(event.target).val() + "</span>").insertBefore($(event.target));
                            $(event.target).remove();
                        } else {
                            header.innerHTML = $(event.target).val();
                        }
                    }
                });
                input.focus();
            });
        },
        columns: [
            {text: "Backlog", iconClassName: getIconClassName(), dataField: "new", maxItems: 6},
            {text: "In Progress", iconClassName: getIconClassName(), dataField: "work", maxItems: 6},
            {text: "Done", iconClassName: getIconClassName(), dataField: "done", maxItems: 6}
        ],
        // render column headers.
        columnRenderer: function (element, collapsedElement, column) {
            var columnItems = $("#kanban1").jqxKanban('getColumnItems', column.dataField).length;
            // update header's status.
            element.find(".jqx-kanban-column-header-status").html(" (" + columnItems + "/" + column.maxItems + ")");
            // update collapsed header's status.
            collapsedElement.find(".jqx-kanban-column-header-status").html(" (" + columnItems + "/" + column.maxItems + ")");
        }
    });
    // handle item clicks.
    $('#kanban1').on("itemAttrClicked", function (event) {
        var args = event.args;
        if (args.attribute == "template") {
            $('#kanban1').jqxKanban('removeItem', args.item.id);
        }
    });
    // handle column clicks.
    var itemIndex = 0;
    $('#kanban1').on('columnAttrClicked', function (event) {
        var args = event.args;
        if (args.attribute == "button") {
            args.cancelToggle = true;
            if (!args.column.collapsed) {
                var colors = ['#f19b60', '#5dc3f0', '#6bbd49', '#dddddd']
                $('#kanban1').jqxKanban('addItem', {
                    status: args.column.dataField,
                    text: "<textarea placeholder='(No Title)' style='width: 96%; margin-top:2px; border-radius: 3px; border:none; line-height:20px; height: 50px;' class='jqx-input' id='newItem" + itemIndex + "' value=''></textarea>",
                    tags: "new task",
                    color: colors[Math.floor(Math.random() * 4)],
                    resourceId: null
                });
                var input = $("#newItem" + itemIndex);
                input.mousedown(function (event) {
                    event.stopPropagation();
                });
                input.mouseup(function (event) {
                    event.stopPropagation();
                });
                input.keydown(function (event) {
                    if (event.keyCode == 13) {
                        $("<span>" + $(event.target).val() + "</span>").insertBefore($(event.target));
                        $(event.target).remove();
                    }
                });
                input.focus();
                itemIndex++;
            }
        }
    });
    document.getElementById('main-div').style.overflow = ""; // Dont display two scrollBar for the treegrid
}
