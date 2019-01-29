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


/* called when a link is clicked */
function linkCliked(id) {

    let object = getObjectWithID(id);

    if (typeof (object) !== "string") {
        let schema = getModelSchemaFromObject(object);

        json_context.activeID = id;
        json_context.current_version = true;
        if (parseDataForm(object, schema) !== false) {
            /*
            if (json_context.active_view !== "list") {
                breadcrumbPop();
            }
            */
            json_context.active_view = "form"

            json_context.hideButton = true;

            display_Form(schema);
            displayAttributeTable(object, schema);
            json_context.create = false;
        }
    }
}

/* used to display name of objects rather than their id, and make clickable */
function switchAttr(attr, view, key, id) {


    if (!attr || typeof (attr) === "undefined") {
        return "";
    }

    let ret;

    //console.log("attr[0]: " + attr[0]);
    let lastindex = attr[0].lastIndexOf(".");


    //console.log("switchAttr: " + attr[0].substring(lastindex+1));
    //console.log("switchAttr VALUE: " + attr);


    switch (getPrimitiveAttribute(attr[0].substring(lastindex + 1))) {
        case "AttrAbstractAttachment":

            if (attr[1][0]) {
                if (id) {
                    if (view === "list") {
                        ret = ('<a target=\\"_blank\\" href=\\"/orm/GetAttachment?nodeID=' + nodeID + '&id=' + id + '&attributeName=' + key + '\\">' + attr[1][0] + '</a>');
                    } else if (view === "panel" || view === "formfix") {
                        ret = '<a target="_blank" href="/orm/GetAttachment?nodeID=' + nodeID + '&id=' + id + '&attributeName=' + key + '">' + attr[1][0] + '</a>';
                    }
                }
                else {
                    ret = attr[1][0];
                }
            }
            break;
        case "AttrString":
            ret = attr[1];
            break;
        case "AttrBoolean":
            if (view !== "formfix") { // Formfix already keep the box, don't need a string with the value
                ret = attr[1];
            }
            break;
        case "AttrDateTime":
            ret = attr[1];
            break;
        case "AttrID":
            if (view === "list") {
                let name = getNameWithID(attr[1]);
                //if (name !== attr[1].replace(/\s/g, ''))
                ret = ('<a class=\\"cell\\" onclick=\\"linkCliked(this.id)\\" id=\\"' + attr[1] + '\\">' + name + '</a>');
                //else
                //    ret = getNameWithID(attr[1]);
            } else if (view === "panel" || view === "formfix") {
                let name = getNameWithID(attr[1]);
                //if (name !== attr[1].replace(/\s/g, ''))
                ret = ('<a class="cell" onclick="linkCliked(this.id)" id="' + attr[1] + '">' + name + '</a>');
                //else
                //    ret = getNameWithID(attr[1]);
            } else
                ret = getNameWithID(attr[1]);
            break;
        case "AttrIDList":
            if (view === "list") {
                let IDString = "";
                let name;
                let model;
                for (let j = 0; j < attr[1].length; j++) {
                    name = getNameWithID(attr[1][j][1]);
                    model = getModelWithName(name);
                    IDString += '<a class=\\"cell\\" onclick=\\"linkCliked(this.id)\\" id=\\"' + attr[1][j][1] + '\\">' + name + '</a>, ';
                }
                ret = IDString.substring(0, IDString.length - 2);

            } else if (view === "panel") {
                let IDString = "";
                let name;
                let model;
                for (let j = 0; j < attr[1].length; j++) {
                    name = getNameWithID(attr[1][j][1]);
                    model = getModelWithName(name);
                    IDString += '<a class="cell" onclick="linkCliked(this.id)" id="' + attr[1][j][1] + '">' + name + '</a>, ';
                }
                ret = IDString.substring(0, IDString.length - 2);
            } else if (view === "formfix") {
                let IDString = "";
                let name;
                let model;
                for (let j = 0; j < attr[1].length; j++) {
                    name = getNameWithID(attr[1][j][1]);
                    model = getModelWithName(name);
                    IDString += '<a class="cell" onclick="linkCliked(this.id)" id="' + attr[1][j][1] + '">' + name + '</a>, ';
                }
                ret = '<span>' + IDString.substring(0, IDString.length - 2) + '</span>';

            } else {
                let IDString = "";
                for (let j = 0; j < attr[1].length; j++)
                    IDString += '<a class="cell" onclick="linkCliked(this.id)" id="' + attr[1][j][1] + '">' + getNameWithID(attr[1][j][1]) + '</a>, ';
                ret = IDString.substring(0, IDString.length - 2);
            }
            break;
        case "AttrList":

            break;

        case "Number":
            ret = attr[1];
            break;
        case "AttrList":
            let IDStrings = "";
            for (let j = 0; j < attr[1].length; j++)
                IDStrings += attr[1][j][1];
            ret = IDStrings;
            break;
        case "AttrMap":
            ret = attr[1];
            break;
        default:
            ret = attr[1];
            break;
    }
    return ret;
}

function getObjectWithID(ID) {
    for (let i = 0; i < json_context.IDSCache.length; i++) {
        if (json_context.IDSCache[i]) {
            if (json_context.IDSCache[i][1].id === ID) {
                return json_context.IDSCache[i];
            }
        }
    }

    loadIDS([ID]);

    for (let i = 0; i < json_context.IDSCache.length; i++) {
        if (json_context.IDSCache[i]) {
            if (json_context.IDSCache[i][1].id === ID) {
                return json_context.IDSCache[i];
            }
        }
    }

    return ID;
}

function getObjectWithIDFromData(data, ID) {
    for (let i = 0; i < data.length; i++) {
        if (data[i][1].id === ID) {
            return data[i];
        }
    }
    return ID;
}

function getNameWithID(ID) {

    if (!ID) {
        return "";
    }

    try {
        if (!ID.match(AttrID_REGEX_PATTERN)) {
            return ID;
        }
    }
    catch (e) {
        return ID;
    }

    if (typeof (ID) !== "undefined") {
        try {
            ID = ID.replace(/\s/g, '');
        } catch (e) {

        }
    }
    for (let i = 0; i < json_context.IDSCache.length; i++) {
        if (typeof (json_context.IDSCache[i]) !== "undefined") {
            if (json_context.IDSCache[i][1].id === ID) {
                return json_context.IDSCache[i][1].displayName;
            }
        }
    }

    loadIDS([ID]);

    for (let i = 0; i < json_context.IDSCache.length; i++) {
        if (typeof (json_context.IDSCache[i]) !== "undefined") {
            if (json_context.IDSCache[i][1].id === ID) {
                return json_context.IDSCache[i][1].displayName;
            }
        }
    }

    return ID;
}

function getModelWithName(name) {
    let nameList;
    for (let attr in json_context.attrTab) {
        if (attr !== "Name") {
            try {
                json_context.attrTab[attr] += '';
                nameList = json_context.attrTab[attr].split(",");
                for (let i = 0; i < nameList.length; i++) {
                    if (nameList[i].trim() === name) {
                        return (attr);
                    }
                }
            } catch (e) {
            }
        }
    }
    return name;
}

function getPrimitiveAttribute(attribute) {
    let result = 'AttrString';
    if (json_context.PrimitiveAttributes !== {})
        if (json_context.PrimitiveAttributes.hasOwnProperty(attribute))
            result = json_context.PrimitiveAttributes[attribute];

    return result;
}

function getAttrVisible() {
    let attr = [];
    for (let i = 0; i < json_context.active_schema.ModelSchema[1].attrMap[1]["Attribute List"][1].length; i++) {
        if (json_context.active_schema.ModelSchema[1].attrMap[1]["Attribute List"][1][i][1].Visible === true) {
            attr.push(json_context.active_schema.ModelSchema[1].attrMap[1]["Attribute List"][1][i][1].Name);
        }
    }
    return attr;
}

function getAttrType(key, schema) {

    if (!schema) {
        schema = json_context.active_schema;
    }

    let result = "";
    let attributeList = schema.ModelSchema[1].attrMap[1]['Attribute List'][1];
    for (let i = 0; i < attributeList.length; i++) {

        let attribute = attributeList[i][1];
        if (attribute.Name === key) {
            result = attribute.AttrType;
            break;
        }
    }

    return getPrimitiveAttribute(result);
}

function createElementFromHTML(htmlString) {
    if (!htmlString) {
        return;
    }

    var div = document.createElement('div');
    div.innerHTML = htmlString.trim();

    // Change this to div.childNodes to support multiple top-level nodes
    return div.firstChild;
}

// For IE
if (!('remove' in Element.prototype)) {
    Element.prototype.remove = function () {
        if (this.parentNode) {
            this.parentNode.removeChild(this);
        }
    };
}