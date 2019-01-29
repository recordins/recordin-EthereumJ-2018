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

// IE need this
String.prototype.endsWith = function (suffix) {
    return this.indexOf(suffix, this.length - suffix.length) !== -1;
};

function getMenuData(menuID) {

    let menuJson = json_context.menuData;

    for (let i = 0; i < menuJson.length; i++) {
        let menu = menuJson[i];
        if (menu.id === menuID) {
            return {
                'model': menu.model,
                'id': menu.menuID,
                'filter': menu.filter,
                'View List': menu['View List'],
                'View Form': menu['View Form'],
                'View Tree': menu['View Tree'],
                'View Kanban': menu['View Kanban'],
                'Own': menu['Own']
            };
        }
    }
    return {};
}

function displayMenu(menu) {


    $("#jqxMenu").remove();
    let menuDiv = document.createElement("div");
    menuDiv.id = "jqxMenu";
    menuDiv.className = "";
    document.getElementById('menuContainer').appendChild(menuDiv);

    var source = {
        datatype: "json",
        datafields: [
            {name: 'id'},
            {name: 'parentid'},
            {name: 'text'},
            {name: 'subMenuWidth'},
            {name: 'div'}
        ],
        id: 'id',
        localdata: menu
    };

    var dataAdapter = new $.jqx.dataAdapter(source);
    dataAdapter.dataBind();
    var records = dataAdapter.getRecordsHierarchy('id', 'parentid', 'items', [{name: 'text', map: 'label'}]);

    $('#jqxMenu').jqxMenu({
        source: records,
        height: '40px',
        rtl: true,
        width: 'auto',
        autoCloseOnClick: false
    });
    $("#jqxMenu").on('itemclick', function (event) {

        let menuData = getMenuData(event.args.id);

        let model = menuData.model;
        $("#myModal").remove();

        json_context.active_schema = false;
        if (model) {
            delete json_context.SchemaCache[model];
            json_context.active_schema = getModelSchema(model);
        }

        json_context.failed_create = false;
        json_context.create = false;

        if (json_context.active_schema) {

            json_context.active_menuID = menuData.id;

            if (menuData.Own) {
                if (!loggedUserID) {
                    requestConfig();
                }
                let object = request_own_object(model, loggedUserID);

                launchModalForm(model, function (dataModalForm) {
                    if (dataModalForm) {

                        if (object) {
                            object[1].attrMap[1] = dataModalForm[1].attrMap[1];
                            object[1].modelID = ["com.recordins.recordin.orm.attribute.AttrID", json_context.active_schema.ModelID];
                            requestSubmittedForm(object, model);
                        }
                        else {
                            requestSubmittedForm(dataModalForm, model);
                        }
                    }
                }, object);
            }
            else {
                json_context.active_model = model;
                if (json_context.active_model.endsWith("/")) {
                    json_context.active_model = json_context.active_model.substring(0, json_context.active_model.length - 1);
                }

                let index = json_context.active_model.lastIndexOf('/');
                if (index > 0) {
                    try {
                        json_context.active_model = json_context.active_model.substring(index + 1);
                    } catch (exception) {
                    }
                }
                json_context.searchFilter = menuData.filter;

                document.getElementById("filterInput").value = json_context.searchFilter;

                json_context.activeID = false;
                displayAttributeTable();

                updateActions();

                json_context.IDSCache = [];
                json_context.SchemaCache = {};
                json_context.HistoryCache = {};
                json_context.hideButton = false;

                init_list();
            }
        } else {
            json_context.active_model = false;

            console.log("menuData: " + JSON.stringify(menuData));

            if (menuData.id.startsWith("UserMenu_")) {
                if (menuData.id.endsWith("Disconnect")) {

                    const req = new XMLHttpRequest();
                    req.open('GET', "/admin/Logoff", false);
                    req.send(null);

                    location.reload(true);
                }
            }
        }
    });

    $(".jqx-menu-item-top").each(function (index) {

        let menuJson = json_context.menuData;
        let right = false;

        for (let i = 0; i < menuJson.length; i++) {
            let menu = menuJson[i];

            if (menu.id === $(this).text()) {
                if (menu.hasOwnProperty("align") && menu.align === "right") {
                    right = true;
                    break;
                }
            }
        }

        if (right) {
            $(this).addClass("jqx-menu-item-top-right-align");
        }
        else {
            $(this).addClass("jqx-menu-item-top-left-align");
        }
    });
}

