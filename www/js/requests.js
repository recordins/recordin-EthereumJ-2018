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

function RequestUpload(url, notification, formData) {
    const req = new XMLHttpRequest();

    console.log("SEND: " + url);

    req.addEventListener('progress', function (e) {
        var done = e.position || e.loaded, total = e.totalSize || e.total;
        console.log('xhr progress: ' + (Math.floor(done / total * 1000) / 10) + '%');
    }, false);

    if (req.upload) {
        req.upload.onprogress = function (e) {
            var done = e.position || e.loaded, total = e.totalSize || e.total;
            console.log('xhr.upload progress: ' + done + ' / ' + total + ' = ' + (Math.floor(done / total * 1000) / 10) + '%');
        };
    }

    req.open('POST', url, false);
    req.send(formData);

    if (req.status == 401) {
        $.notify("Error sending attachment: Insufficient privileges", "error");
        throw new Error("Error sending attachment: Insufficient privileges");
    }
    else if (req.status == 403) {
        let check = JSON.parse(req.responseText);
        if (check.MessageValue) {
            $.notify("Error sending attachment: " + check.MessageValue.replace(/: /g, ':\n'), "error");
        }
        else {
            $.notify("Error sending attachment: ", "error");
        }
        throw new Error("Error sending attachment: Insufficient privileges");
    }
    else if (req.status >= 400) {
        $.notify("Error sending attachment", "error");
    }
    else {
        console.log(['xhr upload complete']);
    }

    /*
    req.onreadystatechange = function (e) {
        if (4 === this.readyState) {
            if (req.status == 401) {
                $.notify("Error sending attachment: Insufficient privileges", "error");
                throw new Error("Error sending attachment: Insufficient privileges");
            }
            else if (req.status >= 400) {
                $.notify("Error sending attachment", "error");
            }
            else{
                console.log(['xhr upload complete', e]);
            }
        }
    };
    req.open('POST', url, false);
    req.send(formData);
    */
}

function RequestGET(url, notification) {
    const req = new XMLHttpRequest();
    req.open('GET', url, false);
    req.send(null);

    if (req.status < 400) {
        let check = JSON.parse(req.responseText);
        if (check.MessageType === "Success" || check.MessageType === "Status") {
            if (notification === true && check.MessageType === "Success") {
                $.notify(check.MessageValue.replace(/: /g, ':\n'), "success");
            }
            return check.MessageValue;

        } else if (check.MessageType === "Warning") {
            $.notify(check.MessageValue.replace(/: /g, ':\n'), "warn");

        } else {
            $.notify(check.MessageValue.replace(/: /g, ':\n'), "error");
        }
    } else if (req.status == 401) {

        location.reload(true);

    } else if (req.status >= 400) {
        $.notify.addStyle("errorHTML", {
            html: "<div>\n<span data-notify-html/></span>\n</div>",
            classes: {
                base: {
                    "font-weight": "bold",
                    "padding": "8px 15px 8px 14px",
                    "text-shadow": "0 1px 0 rgba(255, 255, 255, 0.5)",
                    "background-color": "#fcf8e3",
                    "border": "1px solid #fbeed5",
                    "border-radius": "4px",
                    "white-space": "nowrap",
                    "padding-left": "25px",
                    "background-repeat": "no-repeat",
                    "background-position": "3px 7px"
                },
                error: {
                    "color": "#B94A48",
                    "background-color": "#F2DEDE",
                    "border-color": "#EED3D7",
                    "background-image": "url(data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAAGXRFWHRTb2Z0d2FyZQBBZG9iZSBJbWFnZVJlYWR5ccllPAAAAtRJREFUeNqkVc1u00AQHq+dOD+0poIQfkIjalW0SEGqRMuRnHos3DjwAH0ArlyQeANOOSMeAA5VjyBxKBQhgSpVUKKQNGloFdw4cWw2jtfMOna6JOUArDTazXi/b3dm55socPqQhFka++aHBsI8GsopRJERNFlY88FCEk9Yiwf8RhgRyaHFQpPHCDmZG5oX2ui2yilkcTT1AcDsbYC1NMAyOi7zTX2Agx7A9luAl88BauiiQ/cJaZQfIpAlngDcvZZMrl8vFPK5+XktrWlx3/ehZ5r9+t6e+WVnp1pxnNIjgBe4/6dAysQc8dsmHwPcW9C0h3fW1hans1ltwJhy0GxK7XZbUlMp5Ww2eyan6+ft/f2FAqXGK4CvQk5HueFz7D6GOZtIrK+srupdx1GRBBqNBtzc2AiMr7nPplRdKhb1q6q6zjFhrklEFOUutoQ50xcX86ZlqaZpQrfbBdu2R6/G19zX6XSgh6RX5ubyHCM8nqSID6ICrGiZjGYYxojEsiw4PDwMSL5VKsC8Yf4VRYFzMzMaxwjlJSlCyAQ9l0CW44PBADzXhe7xMdi9HtTrdYjFYkDQL0cn4Xdq2/EAE+InCnvADTf2eah4Sx9vExQjkqXT6aAERICMewd/UAp/IeYANM2joxt+q5VI+ieq2i0Wg3l6DNzHwTERPgo1ko7XBXj3vdlsT2F+UuhIhYkp7u7CarkcrFOCtR3H5JiwbAIeImjT/YQKKBtGjRFCU5IUgFRe7fF4cCNVIPMYo3VKqxwjyNAXNepuopyqnld602qVsfRpEkkz+GFL1wPj6ySXBpJtWVa5xlhpcyhBNwpZHmtX8AGgfIExo0ZpzkWVTBGiXCSEaHh62/PoR0p/vHaczxXGnj4bSo+G78lELU80h1uogBwWLf5YlsPmgDEd4M236xjm+8nm4IuE/9u+/PH2JXZfbwz4zw1WbO+SQPpXfwG/BBgAhCNZiSb/pOQAAAAASUVORK5CYII=)"
                }
            }
        });
        if (req.status >= 500) {
            $.notify(req.responseText, {
                style: 'errorHTML',
                autoHide: false
            });
        } else {
            $.notify(req.responseText, {
                style: 'errorHTML'
            });
        }
        console.log("Response status: %d (%s)", req.status, req.statusText);
    }
}

function requestMenu() {
    json_context.menuData = RequestGET("/model/SearchMenu", false);
}

function getModelSchema(model) {


    //console.log("getModelSchemaFromObject: " + model);

    let res = json_context.SchemaCache[model];

    if (!res) {
        res = RequestGET('/model/JsonSchema?model=' + model, false);
    }
    if (res) {
        json_context.SchemaCache[model] = res;
        return res;
    }

    return json_context.active_schema;
}

function getModelSchemaFromObject(object) {


    if (object && object[1].modelID) {

        let modelID = object[1].modelID;
        if (typeof(modelID) == "object") {
            modelID = modelID[1];
        }

        if (modelID !== json_context.active_schema.ModelID) {

            let res = json_context.SchemaCache[modelID];

            if (!res) {

                let get = '/model/JsonSchema?model=' + modelID;
                //        console.log("1 getModelSchemaFromObject: " + get);
                res = RequestGET(get, false);
            }
            if (res) {
                json_context.SchemaCache[modelID] = res;
                return res;
            }
        }
    }
    else if (object && object[1].model) {

        let model = object[1].model;

        let res = json_context.SchemaCache[model];

        if (!res) {

            let get = '/model/JsonSchema?model=' + object[1].model;
            //    console.log("2 getModelSchemaFromObject: " + get);
            res = RequestGET(get, false);
        }
        if (res) {
            json_context.SchemaCache[model] = res;
            return res;
        }
    }

    return json_context.active_schema;
}

function request_own_object(model, userID) {

    if (userID) {
        let userUID = userID.substring(userID.lastIndexOf(".") + 1, userID.length);

        let filter = '[["userOwnerID","contains","' + userUID + '"]]';

        let url = '/orm/Search?menuID=' + json_context.active_menuID + '&model=' + model + "&index=VIRTUAL_TABLES_ACTIVE&oldVersion=false&filter=" + filter + "&offset=0&limit=1";
        let obj = RequestGET(url, false);

        if (obj) {
            json_context.active_data_list = obj.BlockchainObjects;
            json_context.active_data_list_count = obj.Count - obj.CountOld;

            if (obj.BlockchainObjects.length > 0) {
                return obj.BlockchainObjects[0];
            }
        }
    }

    return;
}

function request_data(offset, limit) {

    if (offset) {
        if (offset < 0) {
            offset = 0;
        }
        json_context.offset = offset;
    }

    if (limit) {
        if (limit > json_context.searchLimitMax) {
            limit = json_context.searchLimitMax;
        }
        if (limit < 0) {
            limit = 1;
        }
        json_context.limit = limit;
    }

    let url = '/orm/Search?menuID=' + json_context.active_menuID + '&model=' + json_context.active_model + "&index=" + json_context.searchIndex + "&oldVersion=" + json_context.oldVersion + "&filter=" + json_context.searchFilter + "&offset=" + json_context.offset + "&limit=" + json_context.limit;
    let obj = RequestGET(url, false);
    if (obj) {
        json_context.active_data_list = obj.BlockchainObjects;
        json_context.active_data_list_count = obj.Count - obj.CountOld;

        if (json_context.active_data_list.length === 0) {
            json_context.currentPageNumber = 0;
            $("#list_navigation").hide();
            $("#btnBlock").hide();
        } else {
            json_context.currentPageNumber = 1;
            //        document.getElementById('list_navigation').style.display = "inline-block";
            //        document.getElementById('btnBlock').style.display = "inline-block";
            $("#btnBlock").show();
            $("#list_navigation").show();
        }

        let ids = lookupIDS();
        loadIDS(ids);
    }
}

function actionRequest(action, args) {
    let checked = getSelectedUIDS();
    if (checked.length === 0) {
        if (json_context.activeID) {
            checked = [];
            checked.push(json_context.activeID.substring(json_context.activeID.lastIndexOf(".") + 1));
        }
    }

    breadcrumbPush("Action requested", json_context);
    let jsonResult = {};
    jsonResult.ids = checked.reverse();
    if (args) {
        jsonResult.args = (args);
    }
    RequestGET('/orm/Action?name=' + action + '&args=' + JSON.stringify(jsonResult), true);
}

function requestSubmittedForm(JSONResult, model) {

    console.log("SUBMIT");

    let url = '/orm/Write?model=' + model + '&vals=' + JSON.stringify(JSONResult);
    let notification = true;

    const req = new XMLHttpRequest();
    req.open('GET', url, false);
    req.send(null);

    if (req.status < 400) {
        let check = JSON.parse(req.responseText);
        if (check.MessageType === "Success") {
            if (notification === true) {
                $.notify(check.MessageValue, "success");
            }
            json_context.create = false;
            json_context.failed_create = false;

        } else if (check.MessageType === "Warning") {
            $.notify(check.MessageValue.replace(/: /g, ':\n'), "warn");

        } else {
            $.notify(check.MessageValue.replace(/: /g, ':\n'), "error");

            if (json_context.create) {
                json_context.failed_create = true;
            }
        }
    } else if (req.status == 401) {

        location.reload(true);

    } else if (req.status >= 400) {

        if (json_context.create) {
            json_context.failed_create = true;
        }

        $.notify.addStyle("errorHTML", {
            html: "<div>\n<span data-notify-html/></span>\n</div>",
            classes: {
                base: {
                    "font-weight": "bold",
                    "padding": "8px 15px 8px 14px",
                    "text-shadow": "0 1px 0 rgba(255, 255, 255, 0.5)",
                    "background-color": "#fcf8e3",
                    "border": "1px solid #fbeed5",
                    "border-radius": "4px",
                    "white-space": "nowrap",
                    "padding-left": "25px",
                    "background-repeat": "no-repeat",
                    "background-position": "3px 7px"
                },
                error: {
                    "color": "#B94A48",
                    "background-color": "#F2DEDE",
                    "border-color": "#EED3D7",
                    "background-image": "url(data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABQAAAAUCAYAAACNiR0NAAAAGXRFWHRTb2Z0d2FyZQBBZG9iZSBJbWFnZVJlYWR5ccllPAAAAtRJREFUeNqkVc1u00AQHq+dOD+0poIQfkIjalW0SEGqRMuRnHos3DjwAH0ArlyQeANOOSMeAA5VjyBxKBQhgSpVUKKQNGloFdw4cWw2jtfMOna6JOUArDTazXi/b3dm55socPqQhFka++aHBsI8GsopRJERNFlY88FCEk9Yiwf8RhgRyaHFQpPHCDmZG5oX2ui2yilkcTT1AcDsbYC1NMAyOi7zTX2Agx7A9luAl88BauiiQ/cJaZQfIpAlngDcvZZMrl8vFPK5+XktrWlx3/ehZ5r9+t6e+WVnp1pxnNIjgBe4/6dAysQc8dsmHwPcW9C0h3fW1hans1ltwJhy0GxK7XZbUlMp5Ww2eyan6+ft/f2FAqXGK4CvQk5HueFz7D6GOZtIrK+srupdx1GRBBqNBtzc2AiMr7nPplRdKhb1q6q6zjFhrklEFOUutoQ50xcX86ZlqaZpQrfbBdu2R6/G19zX6XSgh6RX5ubyHCM8nqSID6ICrGiZjGYYxojEsiw4PDwMSL5VKsC8Yf4VRYFzMzMaxwjlJSlCyAQ9l0CW44PBADzXhe7xMdi9HtTrdYjFYkDQL0cn4Xdq2/EAE+InCnvADTf2eah4Sx9vExQjkqXT6aAERICMewd/UAp/IeYANM2joxt+q5VI+ieq2i0Wg3l6DNzHwTERPgo1ko7XBXj3vdlsT2F+UuhIhYkp7u7CarkcrFOCtR3H5JiwbAIeImjT/YQKKBtGjRFCU5IUgFRe7fF4cCNVIPMYo3VKqxwjyNAXNepuopyqnld602qVsfRpEkkz+GFL1wPj6ySXBpJtWVa5xlhpcyhBNwpZHmtX8AGgfIExo0ZpzkWVTBGiXCSEaHh62/PoR0p/vHaczxXGnj4bSo+G78lELU80h1uogBwWLf5YlsPmgDEd4M236xjm+8nm4IuE/9u+/PH2JXZfbwz4zw1WbO+SQPpXfwG/BBgAhCNZiSb/pOQAAAAASUVORK5CYII=)"
                }
            }
        });
        if (req.status >= 500) {
            $.notify(req.responseText, {
                style: 'errorHTML',
                autoHide: false
            });
        } else {
            $.notify(req.responseText, {
                style: 'errorHTML'
            });
        }
        console.log("Response status: %d (%s)", req.status, req.statusText);
    }
}

function lookupIDS(data) {
    let ids = [];

    if (!data) {
        data = json_context.active_data_list;
    }
    for (let i = 0; i < data.length; i++) {

        if (typeof (data[i]) === "undefined") {
            ;
        } else {
            let object = data[i][1];
            for (let key in object) {


                if (object[key] !== null) {
                    if (typeof (object[key]) === "object") {

                        let lastindex = object[key][0].lastIndexOf(".");

                        let attrType = object[key][0].substring(lastindex + 1);

                        if (key !== "attrMap") {
                            if (getPrimitiveAttribute(attrType) === "AttrID") {
                                ids.push(object[key][1]);

                            } else if (getPrimitiveAttribute(attrType) === "AttrIDList") {
                                for (let j = 0; j < object[key][1].length; j++) {
                                    ids.push(object[key][1][j][1]);
                                }
                            }
                        } else {
                            for (let keyMap in object[key][1]) {
                                let valueMap = object[key][1][keyMap];

                                if (typeof (valueMap) === "undefined" || valueMap === null)
                                    ;
                                else {
                                    if (typeof (object[key]) === "object") {

                                        let lastindex = valueMap[0].lastIndexOf(".");

                                        let attrType = valueMap[0].substring(lastindex + 1);

                                        if (getPrimitiveAttribute(attrType) === "AttrID") {
                                            ids.push(valueMap[1]);

                                        } else if (getPrimitiveAttribute(attrType) === "AttrIDList") {
                                            for (let j = 0; j < valueMap[1].length; j++) {
                                                ids.push(valueMap[1][j][1]);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    ids = JSON.stringify(ids);

    return ids;
}


function loadID(id) {
    let tmp = [];
    tmp.push(id);
    loadIDS(JSON.stringify(tmp));
}

function IDSCacheContains(ID) {

    for (let i = 0; i < json_context.IDSCache.length; i++) {
        if (typeof (json_context.IDSCache[i]) !== "undefined")
            if (json_context.IDSCache[i][1].id === ID) {
                return true;
            }
    }

    return false;
}

function loadIDS(IDS) {

    try {
        IDS = JSON.parse(IDS);
    } catch (error) {
    }

    let uniqueIDS = [];

    // Jquery way to remove duplicated data in array because IE don't support include
    $.each(IDS, function (i, el) {
        if ($.inArray(el, uniqueIDS) === -1) {
            uniqueIDS.push(el);
        }
    });

    //Changed this code for IE. IE don't support includes
    /*for (let i = 0; i < IDS.length; i++) {
     if (IDS[i] !== "" && !uniqueIDS.includes(IDS[i])) {
     uniqueIDS.push(IDS[i]);
     }
     }*/

    let missingIDS = [];

    for (let i = 0; i < uniqueIDS.length; i++) {

        if (uniqueIDS[i] !== "" && !IDSCacheContains(uniqueIDS[i])) {
            missingIDS.push(uniqueIDS[i]);
        }
    }

    if (missingIDS.length > 0) {

        let get = '/orm/Read?ids=' + JSON.stringify(missingIDS);
        let res = RequestGET(get, false);
        if (typeof (res) !== "undefined") {
            //res = JSON.parse(res);

            for (let i = 0; i < res.length; i++) {

                if (res[i] != null && typeof (res[i]) !== "undefined") {
                    if (!IDSCacheContains(res[i][1].id)) {
                        json_context.IDSCache.push(res[i]);
                    }
                }
            }
        }
    }
}


function getCurrentVersionID(ID) {

    let get = '/orm/GetCurrentVersion?id=' + ID;
    let res = RequestGET(get, false);
    if (typeof (res) !== "undefined") {

        res = JSON.parse(res);

        if (!IDSCacheContains(res[1].id)) {
            json_context.IDSCache.push(res);
        }

        return res[1].id;
    }

    return ID;
}

