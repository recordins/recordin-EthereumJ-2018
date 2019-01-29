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
// schema will stock the json with the informations for the form.
const Form = JSONSchemaForm.default; // Mandatory if you use react-jsonschema-form without node_modules and npm

// Replace const submit, IE don't work with "=>"
var onSubmit = function onSubmit(e) {
    submitted(e.formData);
};

function init_form() {

    if (json_context.active_view !== "form") {

        if (!json_context.activeID) {
            $.notify("Please select one object to display the form view", "warn");
            return;
        }

        if (json_context.activeID) {
            if (json_context.active_view === "graph") {
                breadcrumbPop();
            } else if ((json_context.active_view === "list" || json_context.active_view === "kanban")) {
                let navIndex = document.getElementById("navIndex");
                navIndex.value = json_context.navSingleNavigationIndex + json_context.navLeftNumber;
            }
        }

        json_context.active_view = "form";
        parseDataForm();
        display_Form();
    }
}

/* used to set the user interface context and displays the form */
function display_Form(schema) {

    clean_div();

    let object = getObjectWithID(json_context.activeID);
    if (!schema) {
        schema = getModelSchemaFromObject(object);
    }

    //console.log("display_Form schema");
    //console.log(schema);

    let form = document.createElement("div");
    form.id = "form";
    form.className = "form";
    document.getElementById('panelContentmain-div').appendChild(form);
    document.getElementById('main-div').style.height = '99.7%';
    $('#main-div').jqxPanel('scrollTo', 0, 0);
    //json_context.active_view = "form";

    generate_form(schema, false, json_context.activeID);
    display_button();

    document.getElementById('main-div').style.overflow = "hidden"; // Dont display two scrollBar for the treegrid
    if (object) {
        breadcrumbPush(object[1].displayName, json_context);
    }
}

/* Used to create the json schema needed for the form diaplay library */
function formatSchemaForm(schema) {

    let attributeList = schema.ModelSchema[1].attrMap[1]['Attribute List'][1];
    let required = [];
    let properties = {};
    for (let i = 0; i < attributeList.length; i++) {

        let attribute = attributeList[i][1];
        let attributeName = attribute.Name;
        if (attribute.Required) {
            required.push(attributeName);
        }

        let property = {};
        if (getAttrType(attributeName, schema) === "AttrBoolean") {
            property.title = attributeName;
            property.type = "boolean";


        } else if (getAttrType(attributeName, schema) === "AttrAbstractAttachment") {
            property.title = attributeName;
            property.type = "string";
            property.format = "data-url";

        } else {
            property.title = attributeName;
            property.type = "string";

        }

        properties[attributeName] = property;
    }

    let result = {};
    result.title = schema.ModelSchema[1].displayName + " Form";
    result.type = "object";
    result.required = required;
    result.properties = properties;
    return result;
}

/* generates the additionnal piece of jonschema for Model nested forms */
function generate_nested_list() {
    let AttributeProperties = json_context.active_schema.AttributeProperties;

    let properties = {};
    for (let i = 0; i < AttributeProperties.length; i++) {
        let attributePropertyName = AttributeProperties[i].Name;
        let attributePropertyType = AttributeProperties[i].Type;
        let property = {};
        property.type = "string";
        property.title = attributePropertyName;
        if (attributePropertyName === 'AttrType') {
            property['$ref'] = "#/definitions/attributes";
            property.default = "AttrString";
        } else if (attributePropertyName === 'Visible') {
            property.type = "boolean";
            property.default = true;
        } else if (attributePropertyType === 'boolean') {
            property.type = "boolean";
            property.default = false;
        }

        properties[attributePropertyName] = property;
    }

    let nested_list = {
        "type": "array",
        "title": "",
        "items": {
            "type": "object",
            "title": "Attribute",
            "required": ["Name"],
            "properties": properties
        }
    };

    return nested_list;
}

/* Creation of the form and display with the library */
function getUISchema(schema) {

    let formSchema = json_context.active_schema;
    if (schema) {
        formSchema = schema;
    }

    let model = formSchema.ModelSchema[1].displayName;

    var uiResult = {};

    let classes = "col-lg-2 col-md-4 col-sm-6 col-xs-12";
    //let classes = "col-lg-1 col-md-2 col-sm-3 col-xs-6";

    let attributeList = formSchema.ModelSchema[1].attrMap[1]['Attribute List'][1];
    for (let i = 0; i < attributeList.length; i++) {

        let attribute = attributeList[i][1];
        let attributeName = attribute.Name;

        uiResult[attributeName] = {classNames: classes};

        if (getAttrType(attributeName, schema) === "AttrPassword") {
            uiResult[attributeName] = {classNames: classes, "ui:widget": "password"};
        }

    }

    if (model === "Model") {

        uiResult["Attribute List"] = {classNames: classes};

        let AttributeProperties = formSchema.AttributeProperties;

        let items = {};
        for (let i = 0; i < AttributeProperties.length; i++) {
            let attributePropertyName = AttributeProperties[i].Name;

            items[attributePropertyName] = {classNames: classes};
        }

        let nested_list = {
            "items": items
        };

        uiResult.nestedList = nested_list;
    }

    //console.log("uiResult: ");
    //console.log(uiResult);

    return uiResult;
}

/* Creation of the form and display with the library */
function generate_form(schema, modal, objectID) {

    let formSchema = json_context.active_schema;
    if (schema) {
        formSchema = schema;
    }

    let formSchemaFormatted = formatSchemaForm(formSchema);

    if (formSchema.ModelSchema[1].displayName === 'Model') {

        formSchemaFormatted.properties["Java Class"]["default"] = "BlockchainObject",
            formSchemaFormatted.properties["Java Class"]["$ref"] = "#/definitions/javaclasses";
        let definitions = {};

        let javaclasses = {
            "title": "javaclasses",
            "type": "string",
            "anyOf": []
        };

        for (let i = 0; i < formSchema.JavaClasses.length; i++) {

            javaclasses.anyOf.push({
                "type": "string",
                "enum": [formSchema.JavaClasses[i]],
                "title": formSchema.JavaClasses[i]
            });
        }

        let attributes = {
            "title": "attributes",
            "type": "string",
            "anyOf": []
        };

        for (let i = 0; i < formSchema.Attributes.length; i++) {

            attributes.anyOf.push({
                "type": "string",
                "enum": [formSchema.Attributes[i]],
                "title": formSchema.Attributes[i]
            });
        }

        definitions.javaclasses = javaclasses;
        definitions.attributes = attributes;

        formSchemaFormatted = Object.assign({"definitions": definitions}, formSchemaFormatted);

        formSchemaFormatted.properties.nestedList = generate_nested_list();

        delete formSchemaFormatted.properties["Attribute List"];
    }


    //console.log("Data");
    //console.log(json_context.formData);

    let displayData = changeIDToName(formSchema, json_context.formData);

    //console.log("formSchema");
    //console.log(formSchema);

    //console.log("json_context.formData");
    //console.log(json_context.formData);

    //console.log("displayData");
    //console.log(displayData);

    let uiSchema = getUISchema(formSchema);

    //render the form with reactDom
    if (!modal) {
        ReactDOM.render(
            React.createElement("div", {id: "container"},
                React.createElement(Form, {
                        schema: formSchemaFormatted,
                        uiSchema: uiSchema,
                        onSubmit: json_context.mode === "view" ? "" : onSubmit,
                        noValidate: true,
                        formData: displayData,
                        onChange: onChange,
                        //        FieldTemplate: CustomFieldTemplate
                    },
                    React.createElement("button", {
                        className: "btn btn-success right",
                        type: "submit",
                        id: "submit"
                    }, "submit"))),
            document.getElementById("form"));
        document.getElementById('submit').style.visibility = "hidden";

        $(".form-group.field.field-array").before('<div class="row"></div>');

        if (json_context.mode === 'edit' || json_context.mode === 'create') {
            setClickEvents(json_context.active_schema, false, objectID);
        } else {
            createFormFix(formSchema, objectID);
        }
    } else {
        return formSchemaFormatted;
    }
}

/* add click listeners to corresponding attributes fields */
function setClickEvents(schema, formData, objectID) {

    if (!objectID) {
        objectID = json_context.activeID;
    }

    let forcedSchema = false;
    let formSchema = json_context.active_schema;
    if (schema) {
        formSchema = schema;
        forcedSchema = true;
    }
    if (!formData) {
        formData = json_context.formData;
    }


    //console.log("CLICK EVENTS BEFORE json_context.formData");
    //console.log(json_context.formData);


    let attributeList = formSchema.ModelSchema[1].attrMap[1]['Attribute List'][1];

    if (forcedSchema) {
        for (let i = 0; i < json_context.active_schema.ModelSchema[1].attrMap[1]['Attribute List'][1]; i++) {
            let attributeOLD = json_context.active_schema.ModelSchema[1].attrMap[1]['Attribute List'][1][i][1];
            let attributeNameOLD = attributeOLD.Name;

            let containsAttrinute = false;

            for (let i = 0; i < attributeList.length; i++) {
                let attribute = attributeList[i][1];
                let attributeName = attribute.Name;

                if (attributeName === attributeNameOLD) {
                    containsAttrinute = true;
                }
            }

            if (!containsAttrinute) {
                attributeList.push(attributeOLD);
            }
        }
    }

    for (let i = 0; i < attributeList.length; i++) {

        let attribute = attributeList[i][1];
        let attributeName = attribute.Name;

        if (getPrimitiveAttribute(attribute.AttrType) === "AttrAbstractAttachment") {

            let attrInput = document.getElementById("root_" + attributeName);

            if (attrInput) {

                attrInput.style = "margin-top:5px;padding-left:8px;max-width:200px";

                let object = getObjectWithID(objectID);

                if (object) {
                    if (object[1].attrMap[1][attributeName]) {
                        let attrID = object[1].attrMap[1][attributeName][1];
                        if (attrID) {

                            let elementLink = createElementFromHTML('<span style="display: block">' + '<button id="removeAttachement" class="btn-danger" type="button" style="padding:0px" title="remove attachment">&times;</button>' + "&nbsp;" + switchAttr([attribute.AttrType, attrID], "formfix", attributeName, objectID) + '</span>');

                            if (elementLink) {
                                attrInput.parentNode.parentNode.parentNode.childNodes[0].appendChild(elementLink);

                                document.getElementById("removeAttachement").onclick = function () {
                                    elementLink.style = "display:none";
                                    formData[attributeName] = "remove";
                                };
                            }
                        }
                    }
                }
            }
        }
        else if (getPrimitiveAttribute(attribute.AttrType) === "AttrID") {

            document.getElementById("root_" + attributeName).autocomplete = "off";
            document.getElementById("root_" + attributeName).onclick = function () {

                let jsonContext;
                try {
                    jsonContext = JSON.parse(attribute.ContextData);
                } catch (error) {
                }

                let oldVersion = false;
                let filter = "";
                let index = $("#objectIndexSelect option:selected").val();

                if (jsonContext) {
                    if (jsonContext.oldVersion) {
                        oldVersion = jsonContext.oldVersion;
                    }
                    if (jsonContext.filter) {
                        filter = jsonContext.filter;
                    }
                    if (jsonContext.index) {
                        index = jsonContext.index;
                    }
                }

                let selectedIndex = [];

                if (formData[attributeName]) {
                    selectedIndex.push(formData[attributeName]);
                }

                let model = getNameWithID(attribute.AttrTypeModel);

                launchModalList(true, oldVersion, model, function (dataModal) {

                    console.log("datamodal: " + JSON.stringify(dataModal));

                    if (dataModal) {
                        if (dataModal[0]) {
                            let name = getNameWithID(dataModal[0]);
                            formData[attributeName] = dataModal[0];
                            document.getElementById("root_" + attributeName).value = name;

                        } else {
                            formData[attributeName] = "";
                            document.getElementById("root_" + attributeName).value = "";
                        }
                    } else {
                        formData[attributeName] = "";
                        document.getElementById("root_" + attributeName).value = "";
                    }
                }, filter, index, selectedIndex);
            };
        } else if (getPrimitiveAttribute(attribute.AttrType) === "AttrIDList") {

            document.getElementById("root_" + attributeName).autocomplete = "off";
            document.getElementById("root_" + attributeName).onclick = function () {

                let jsonContext;
                try {
                    jsonContext = JSON.parse(attribute.ContextData);
                } catch (error) {
                }

                let oldVersion = false;
                let filter = "";
                let index = $("#objectIndexSelect option:selected").val();

                if (jsonContext) {
                    if (jsonContext.oldVersion) {
                        oldVersion = jsonContext.oldVersion;
                    }
                    if (jsonContext.filter) {
                        filter = jsonContext.filter;
                    }
                    if (jsonContext.index) {
                        index = jsonContext.index;
                    }
                }

                let selectedIndex = [];
                if (formData[attributeName]) {
                    let idTab = formData[attributeName].split(",");
                    for (let j = 0; j < idTab.length; j++) {
                        if (idTab[j]) {
                            selectedIndex.push(idTab[j].trim());
                        }
                    }
                }

                let model = getNameWithID(attribute.AttrTypeModel);
                launchModalList(false, oldVersion, model, function (dataModal) {

                    console.log("datamodal: " + JSON.stringify(dataModal));

                    if (dataModal) {

                        formData[attributeName] = '';
                        let stringValue = "";
                        for (let i = 0; i < dataModal.length; i++) {

                            let name = getNameWithID(dataModal[i]);

                            console.log("name: " + name);

                            if (i === 0) {
                                formData[attributeName] += dataModal[i];
                                stringValue += name;
                            } else {
                                formData[attributeName] += ',' + dataModal[i];
                                stringValue += ', ' + name;
                            }
                        }

                        document.getElementById("root_" + attributeName).value = stringValue;
                    } else {
                        document.getElementById("root_" + attributeName).value = "";
                    }
                }, filter, index, selectedIndex);
            };
        }
    }

    if (formData.nestedList) {

        for (let i = 0; i < formData.nestedList.length; i++) {

            if (getPrimitiveAttribute(formData.nestedList[i].AttrType) === "AttrID" ||
                getPrimitiveAttribute(formData.nestedList[i].AttrType) === 'AttrIDList' ||
                getPrimitiveAttribute(formData.nestedList[i].AttrType) === 'AttrMap') {


                document.getElementById("root_nestedList_" + i + "_AttrTypeModel").autocomplete = "off";
                document.getElementById("root_nestedList_" + i + "_AttrTypeModel").onclick = function () {

                    let selectedIndexAttrTypeModel = [];
                    if (getPrimitiveAttribute(formData.nestedList[i].AttrType) === "AttrID") {
                        if (formData.nestedList[i].AttrTypeModel) {
                            selectedIndexAttrTypeModel.push(formDatanestedList[i].AttrTypeModel);
                        }
                    } else if (getPrimitiveAttribute(formData.nestedList[i].AttrType) === 'AttrIDList') {
                        let idTab = formData.nestedList[i].AttrTypeModel.split(",");
                        for (let j = 0; j < idTab.length; j++) {
                            if (idTab[j]) {
                                selectedIndexAttrTypeModel.push(idTab[j].trim());
                            }
                        }
                    }
                    launchModalList(true, false, "Model", function (dataModal) {

                        console.log("datamodal: " + JSON.stringify(dataModal));

                        if (dataModal) {
                            if (dataModal[0]) {
                                let name = getNameWithID(dataModal[0]);
                                formData.nestedList[i].AttrTypeModel = dataModal[0];
                                document.getElementById("root_nestedList_" + i + "_AttrTypeModel").value = name;
                            } else {
                                formData.nestedList[i].AttrTypeModel = "";
                                document.getElementById("root_nestedList_" + i + "_AttrTypeModel").value = "";
                            }
                        } else {
                            formData.nestedList[i].AttrTypeModel = "";
                            document.getElementById("root_nestedList_" + i + "_AttrTypeModel").value = "";
                        }
                    }, "", "", selectedIndexAttrTypeModel);
                };
            }
            else {
                document.getElementById("root_nestedList_" + i + "_AttrTypeModel").disabled = true;
                document.getElementById("root_nestedList_" + i + "_AttrTypeModel").autocomplete = "off";
            }

            document.getElementById("root_nestedList_" + i + "_ACL").autocomplete = "off";
            document.getElementById("root_nestedList_" + i + "_ACL").onclick = function () {

                let selectedIndexACL = [];
                if (formData.nestedList[i].ACL) {
                    let idTab = formData.nestedList[i].ACL.split(",");
                    for (let j = 0; j < idTab.length; j++) {
                        if (idTab[j]) {
                            selectedIndexACL.push(idTab[j].trim());
                        }
                    }
                }
                launchModalList(false, false, "ACL", function (dataModal) {

                    console.log("datamodal: " + JSON.stringify(dataModal));

                    if (dataModal) {

                        formData.nestedList[i].ACL = '';
                        let stringValue = "";
                        for (let j = 0; j < dataModal.length; j++) {

                            let name = getNameWithID(dataModal[j]);
                            if (j === 0) {
                                formData.nestedList[i].ACL += dataModal[j];
                                stringValue += name;
                            } else {
                                formData.nestedList[i].ACL += ',' + dataModal[j];
                                stringValue += ', ' + name;
                            }
                        }
                        document.getElementById("root_nestedList_" + i + "_ACL").value = stringValue;
                    } else {
                        formData.nestedList[i].ACL = "";
                        document.getElementById("root_nestedList_" + i + "_ACL").value = "";
                    }
                }, "", "", selectedIndexACL);
            };
        }
    }

    //console.log("CLICK EVENTS AFTER json_context.formData");
    //console.log(json_context.formData);
}

/* trigerred when a value is updated, serves to add click events to new AttrTypeModel fields in Model forms */
function onChange(data) {

    //console.log("ON CHANGE");

    if (typeof (data.formData) !== "undifined" && data.formData.nestedList) {

        //    console.log("BEFORE formData.nestedList");
        //    console.log(json_context.formData.nestedList);

        //    console.log("BEFORE data.formData.nestedList");
        //    console.log(data.formData.nestedList);

        let tmpNestedList = json_context.formData.nestedList;
        json_context.formData.nestedList = [];

        for (let i = 0; i < data.formData.nestedList.length; i++) {

            let attrTypeModel = "";
            let acl = "";

            if (tmpNestedList && tmpNestedList[i]) {
                attrTypeModel = tmpNestedList[i].AttrTypeModel;
                acl = tmpNestedList[i].ACL;
            }

            if (!json_context.formData.nestedList[i]) {
                json_context.formData.nestedList[i] = data.formData.nestedList[i];
            }

            if (tmpNestedList[i]) {
                json_context.formData.nestedList[i].AttrTypeModel = attrTypeModel;
                json_context.formData.nestedList[i].ACL = acl;
            }

            if (getPrimitiveAttribute(data.formData.nestedList[i].AttrType) === "AttrID" ||
                getPrimitiveAttribute(data.formData.nestedList[i].AttrType) === 'AttrIDList' ||
                getPrimitiveAttribute(data.formData.nestedList[i].AttrType) === 'AttrMap') {

                let primitiveAttribute = getPrimitiveAttribute(data.formData.nestedList[i].AttrType);
                let attributeValue = json_context.formData.nestedList[i].AttrTypeModel;

                if (attributeValue) {
                    if (primitiveAttribute === 'AttrIDList') {

                        let arrayAttr = attributeValue.split(",");
                        let tmp_result = "";
                        for (let i = 0; i < arrayAttr.length; i++) {

                            if (i == 0) {
                                tmp_result += getNameWithID(arrayAttr[i].trim());

                            } else {
                                tmp_result += ", " + getNameWithID(arrayAttr[i].trim());
                            }
                        }

                        document.getElementById("root_nestedList_" + i + "_AttrTypeModel").value = tmp_result;

                    } else if (primitiveAttribute === 'AttrID') {
                        document.getElementById("root_nestedList_" + i + "_AttrTypeModel").value = getNameWithID(attributeValue);

                    } else {
                        document.getElementById("root_nestedList_" + i + "_AttrTypeModel").value = attributeValue;
                    }
                }

                document.getElementById("root_nestedList_" + i + "_AttrTypeModel").disabled = false;
                document.getElementById("root_nestedList_" + i + "_AttrTypeModel").onclick = function () {

                    let index = i;

                    let selectedIndexAttrTypeModel = [];
                    if (getPrimitiveAttribute(data.formData.nestedList[index].AttrType) === "AttrID") {
                        if (json_context.formData.nestedList[index].AttrTypeModel) {
                            selectedIndexAttrTypeModel.push(json_context.formData.nestedList[index].AttrTypeModel);
                        }
                    } else if (getPrimitiveAttribute(data.formData.nestedList[index].AttrType) === 'AttrIDList') {
                        if (json_context.formData.nestedList[index].AttrTypeModel) {
                            let idTab = json_context.formData.nestedList[index].AttrTypeModel.split(",");
                            for (let j = 0; j < idTab.length; j++) {
                                if (idTab[j]) {
                                    selectedIndexAttrTypeModel.push(idTab[j].trim());
                                }
                            }
                        }
                    }

                    launchModalList(true, false, "Model", function (dataModal) {

                        console.log("datamodal: " + JSON.stringify(dataModal));

                        if (dataModal) {
                            if (dataModal[0]) {
                                let name = getNameWithID(dataModal[0]);
                                json_context.formData.nestedList[i].AttrTypeModel = dataModal[0];
                                document.getElementById("root_nestedList_" + i + "_AttrTypeModel").value = name;
                            } else {
                                json_context.formData.nestedList[i].AttrTypeModel = "";
                                document.getElementById("root_nestedList_" + i + "_AttrTypeModel").value = "";
                            }
                        } else {
                            json_context.formData.nestedList[i].AttrTypeModel = "";
                            document.getElementById("root_nestedList_" + i + "_AttrTypeModel").value = "";
                        }
                    }, "", "Active", selectedIndexAttrTypeModel);
                };
            }
            else {
                document.getElementById("root_nestedList_" + i + "_AttrTypeModel").disabled = true;
                document.getElementById("root_nestedList_" + i + "_AttrTypeModel").autocomplete = "off";
            }

            document.getElementById("root_nestedList_" + i + "_ACL").autocomplete = "off";
            document.getElementById("root_nestedList_" + i + "_ACL").onclick = function () {

                let selectedIndexACL = [];
                if (json_context.formData.nestedList[i].ACL) {
                    let idTab = json_context.formData.nestedList[i].ACL.split(",");
                    for (let j = 0; j < idTab.length; j++) {
                        if (idTab[j]) {
                            selectedIndexACL.push(idTab[j].trim());
                        }
                    }
                }
                launchModalList(false, false, "ACL", function (dataModal) {

                    console.log("datamodal: " + JSON.stringify(dataModal));

                    if (dataModal) {

                        json_context.formData.nestedList[i].ACL = '';
                        let stringValue = "";
                        for (let j = 0; j < dataModal.length; j++) {

                            let name = getNameWithID(dataModal[j]);
                            if (j === 0) {
                                json_context.formData.nestedList[i].ACL += dataModal[j];
                                stringValue += name;
                            } else {
                                json_context.formData.nestedList[i].ACL += ',' + dataModal[j];
                                stringValue += ', ' + name;
                            }
                        }
                        document.getElementById("root_nestedList_" + i + "_ACL").value = stringValue;
                    } else {
                        json_context.formData.nestedList[i].ACL = "";
                        document.getElementById("root_nestedList_" + i + "_ACL").value = "";
                    }
                }, "", "", selectedIndexACL);
            };
        }

        //    console.log("AFTER formData.nestedList");
        //    console.log(json_context.formData.nestedList);
    }

    if (data.formData) {
        if (json_context.active_schema) {
            let schema = json_context.active_schema;
            let attributeList = schema.ModelSchema[1].attrMap[1]['Attribute List'][1];
            for (let i = 0; i < attributeList.length; i++) {

                let attribute = attributeList[i][1];

                let key = attribute.Name;

                if (getAttrType(key, schema) === "AttrID" || getAttrType(key, schema) === "AttrIDList") {
                    // do nothing for specific attributes
                } else {

                    if (data.formData[attribute.Name]) {
                        json_context.formData[attribute.Name] = data.formData[attribute.Name];
                    } else {
                        json_context.formData[attribute.Name] = "";
                    }
                }
            }
        }

    }

    //console.log("END ON CHANGE");
}

/* used to initialize the form with default values at object creation */
function initDataFormCreate(model, schema, modal) {
    let res = {};

    if (json_context.active_schema) {

        if (!model) {
            res.model = json_context.active_model;
        } else {
            res.model = model;
        }
        let attributeList;
        if (!schema) {
            schema = json_context.active_schema;
        }

        attributeList = schema.ModelSchema[1].attrMap[1]['Attribute List'][1];

        for (let i = 0; i < attributeList.length; i++) {

            let attribute = attributeList[i][1];

            let key = attribute.Name;

            if (getAttrType(key, schema) === "AttrAbstractAttachment") {
                // do nothing for files
            } else if (getAttrType(key, schema) === "AttrBoolean") {
                res[key] = false;
                if (attribute.DefaultValue === "true") {
                    res[key] = true;
                } else {
                    res[key] = false;
                }
            } else {
                res[key] = attribute.DefaultValue;
            }
        }
        if (!modal) {
            json_context.formData = res;
        }
    }
    if (modal) {
        return (res);
    }
}

/* parsing of data to fill the form with values */
function parseDataForm(object, schema, modal) {

    let res = {};

    if (!object) {
        object = getObjectWithID(json_context.activeID);
    }

    if (!schema) {
        //schema = json_context.active_schema;
        schema = getModelSchemaFromObject(object);
    }

    res.model = object[1].model;

    let tmp = [];
    tmp.push(object);
    let ids = lookupIDS(tmp);
    loadIDS(ids);

    let attrMap = object[1].attrMap[1];

    let attributeList = schema.ModelSchema[1].attrMap[1]['Attribute List'][1];

    for (let i = 0; i < attributeList.length; i++) {

        let attribute = attributeList[i][1];

        let key = attribute.Name;

        if (attrMap[key]) {

            let value = attrMap[key][1];

            if (getAttrType(key, schema) === "AttrAbstractAttachment") {
                // do nothing for files
                //res[key] = '';
            } else if (getAttrType(key, schema) === "AttrID") {
                res[key] = value;

            } else if (getAttrType(key, schema) === "AttrIDList") {
                let IDString = '';
                for (let j = 0; j < value.length; j++) {

                    if (j === 0) {
                        IDString += value[j][1];
                    } else {
                        IDString += ", " + value[j][1];
                    }
                }
                res[key] = IDString;

            } else if (getAttrType(key, schema) === "AttrBoolean" || getAttrType(key, schema) === "boolean") {
                res[key] = value;
                if (value === "true") {
                    res[key] = true;
                } else if (value === "false") {
                    res[key] = false;
                }
            } else {
                res[key] = value;
            }
        } else {
            if (getAttrType(key, schema) === "AttrAbstractAttachment") {
                // do nothing for files
            } else if (getAttrType(key, schema) === "AttrBoolean" || getAttrType(key, schema) === "boolean") {
                res[key] = false;
            } else {
                res[key] = "";
            }
        }
    }


    if (object[1].model === "Model") {

        res.nestedList = [];

        if (object[1].attrMap[1]["Attribute List"]) {
            let attributeList = object[1].attrMap[1]["Attribute List"][1];
            if (attributeList) {
                for (let i = 0; i < attributeList.length; i++) {
                    let obj = {};
                    let attribute = attributeList[i][1];

                    for (let key in attribute) {
                        if (key === "ACL") {
                            let IDString = '';
                            for (let j = 0; j < attribute[key][1].length; j++) {
                                IDString += (attribute[key][1][j][1]) + ", ";
                            }
                            obj[key] = IDString.substring(0, IDString.length - 2);

                        } else {
                            obj[key] = attribute[key];
                        }
                    }
                    res.nestedList.push(obj);
                }
            }
        }
    }

    if (modal) {
        json_context.modal_formData = res;
    }
    else {
        json_context.formData = res;
    }

    return schema;
}


//submits the data the server
function submitted(data, modal) {

    let Jres = [];
    let object = {};
    let JSONResult;

    if (!modal) {
        data = json_context.formData;
    }

    let model = data.model;
    let schema = getModelSchema(model);

    if (json_context.create === true || modal) {
        Jres[0] = 'com.recordins.recordin.orm.' + schema.ModelJavaClass;
        Jres[1] = {};
        Jres[1].attrMap = [];
        Jres[1].attrMap[0] = "java.util.concurrent.ConcurrentSkipListMap";
    } else {
        object = getObjectWithID(json_context.activeID);
    }

    let attrMap;
    attrMap = getAttrMapForm(data, object, schema);

    if (model === "Model") {
        attrMap["Attribute List"] = getAttrMapAttributes(data, schema);
    }

    if (json_context.create === true || modal) {
        Jres[1].attrMap[1] = attrMap;
        Jres[1].model = model;
        Jres[1].modelID = ["com.recordins.recordin.orm.attribute.AttrID", schema.ModelID];
        JSONResult = Jres;
    } else {
        object[1].attrMap[1] = attrMap;
        object[1].modelID = ["com.recordins.recordin.orm.attribute.AttrID", schema.ModelID];
        Jres = object;
        JSONResult = Jres;
    }

    if (!modal) {

        if (!json_context.create) {
            breadcrumbPop();
        }

        json_context.create == false;
        requestSubmittedForm(JSONResult, model);
        parseDataForm(JSONResult, schema);

        display_Form(schema);
        display_button();
        displayAttributeTable();

        //    console.log("Submitted");
        //    console.log(JSONResult);
    } else {

        //    console.log("CREATE: " + json_context.create);

        modal(JSONResult);
        closeModalForm();
        $('.modal-backdrop').remove();
    }
}

/* formats data for submit to the server */
function getAttrMapForm(data, oldObject, schema) {
    let attrMap = {};
    let arrayAttr = "";
    let arrayList;
    let tmpArray = [];
    let attributeList = schema.ModelSchema[1].attrMap[1]['Attribute List'][1];
    let model = schema.ModelSchema[1].displayName;
    for (let i = 0; i < attributeList.length; i++) {

        let attribute = attributeList[i][1];
        let attributeName = attribute.Name;
        arrayList = [];
        let attributeValue = data[attributeName];
        let primitiveAttribute = getPrimitiveAttribute(attribute.AttrType);

        if (attributeValue) {

            if (primitiveAttribute === 'AttrAbstractAttachment') {

                if (attributeValue !== "remove") {
                    let tmp = attributeValue.split(';');
                    let filename = tmp[1].substring(5);
                    filename = "new_" + new Date().getTime() + "_" + filename;
                    attributeValue = [];
                    attributeValue.push(filename);
                    attributeValue.push(attributeName);
                    attributeValue.push("");
                    var formData = new FormData();
                    formData.append("file", new Blob([dataURItoBlob(data[attributeName])], {type: "application/octet-stream"}));
                    RequestUpload("/orm/Upload?fileName=" + filename, true, formData);


                    attrMap[attributeName] = [];
                    attrMap[attributeName].push("com.recordins.recordin.orm.attribute." + attribute.AttrType);
                    attrMap[attributeName].push(attributeValue);
                }
            } else if (primitiveAttribute === 'AttrIDList') {

                arrayAttr = attributeValue.split(",");
                attrMap[attributeName] = [];
                attrMap[attributeName].push("com.recordins.recordin.orm.attribute." + attribute.AttrType);
                for (let i = 0; i < arrayAttr.length; i++) {
                    tmpArray = [];
                    tmpArray.push("com.recordins.recordin.orm.attribute.AttrID");
                    tmpArray.push(arrayAttr[i].trim());
                    arrayList.push(tmpArray);
                }
                attrMap[attributeName].push(arrayList);
            } else if (primitiveAttribute === 'AttrID') {

                attrMap[attributeName] = [];
                attrMap[attributeName].push("com.recordins.recordin.orm.attribute." + attribute.AttrType);
                attrMap[attributeName].push(attributeValue);
            } else {

                attrMap[attributeName] = [];
                attrMap[attributeName].push("com.recordins.recordin.orm.attribute." + attribute.AttrType);
                attrMap[attributeName].push((attributeValue));
            }
        } else {

            if (primitiveAttribute === "AttrAbstractAttachment") {
                if (oldObject) {
                    if (oldObject[1]) {
                        attrMap[attributeName] = oldObject[1].attrMap[1][attributeName];
                    }
                }

            } else if (primitiveAttribute === "AttrID") {
                attrMap[attributeName] = [];
                attrMap[attributeName].push("com.recordins.recordin.orm.attribute." + attribute.AttrType);
                attrMap[attributeName].push("");

            } else if (primitiveAttribute === "AttrIDList") {
                attrMap[attributeName] = [];
                attrMap[attributeName].push("com.recordins.recordin.orm.attribute." + attribute.AttrType);
                attrMap[attributeName].push([]);

            } else if (primitiveAttribute === "AttrBoolean") {

                attrMap[attributeName] = [];
                attrMap[attributeName].push("com.recordins.recordin.orm.attribute." + attribute.AttrType);
                attrMap[attributeName].push(false);
            } else if (primitiveAttribute === "AttrDateTime") {

            } else {
                attrMap[attributeName] = [];
                attrMap[attributeName].push("com.recordins.recordin.orm.attribute." + attribute.AttrType);
                attrMap[attributeName].push("");
            }
        }
    }
    return attrMap;
}

/* formats data for submit to the server, for Model nested values */
function getAttrMapAttributes(data, schema) {

    let JSONResult = [];
    if (data.nestedList) {
        for (let i = 0; i < data.nestedList.length; i++) {
            let attributeValue = {};
            let nested = data.nestedList[i];
            let AttributeProperties = schema.AttributeProperties;
            for (let i = 0; i < AttributeProperties.length; i++) {

                let attributeProperty = AttributeProperties[i];

                if (attributeProperty.Name in nested) {

                    let propertyValue = "";
                    if (nested[attributeProperty.Name]) {
                        propertyValue = nested[attributeProperty.Name];
                    }

                    if (attributeProperty.Name === "Name") {
                        attributeValue[attributeProperty.Name] = propertyValue;
                    } else {

                        if (getPrimitiveAttribute(attributeProperty.Type) === 'AttrID') {

                            attributeValue[attributeProperty.Name] = [];
                            attributeValue[attributeProperty.Name].push("com.recordins.recordin.orm.attribute.AttrID");
                            attributeValue[attributeProperty.Name].push(propertyValue);

                        } else if (getPrimitiveAttribute(attributeProperty.Type) === 'AttrIDList') {

                            let arrayAttr = propertyValue.split(",");
                            attributeValue[attributeProperty.Name] = [];
                            attributeValue[attributeProperty.Name].push("com.recordins.recordin.orm.attribute.AttrIDList");
                            let arrayList = [];
                            for (let i = 0; i < arrayAttr.length; i++) {
                                let tmpArray = [];
                                tmpArray.push("com.recordins.recordin.orm.attribute.AttrID");
                                tmpArray.push(arrayAttr[i].trim());
                                arrayList.push(tmpArray);
                            }
                            attributeValue[attributeProperty.Name].push(arrayList);

                        } else if (attributeProperty.Type === 'boolean') {
                            if (propertyValue === "") {
                                propertyValue = false;
                            } else if (propertyValue === "true") {
                                propertyValue = true;
                            }

                            attributeValue[attributeProperty.Name] = propertyValue;
                        } else {
                            attributeValue[attributeProperty.Name] = propertyValue;
                        }
                    }
                } else {
                    if (attributeProperty.Type === 'boolean') {
                        attributeValue[attributeProperty.Name] = false;
                    } else {
                        attributeValue[attributeProperty.Name] = "";
                    }
                }
            }
            JSONResult.push(["com.recordins.recordin.orm.attribute.AttrAttribute", attributeValue]);
        }
    }
    JSONResult = ["com.recordins.recordin.orm.attribute.AttrList", JSONResult];
    return JSONResult;
}

/* converts attachment data for submit to the server */
function dataURItoBlob(dataURI) {
// convert base64 to raw binary data held in a string
// doesn't handle URLEncoded DataURIs - see SO answer #6850276 for code that does this
    var byteString = atob(dataURI.split(',')[1]);
    // separate out the mime component
    var mimeString = dataURI.split(',')[0].split(':')[1].split(';')[0];
    // write the bytes of the string to an ArrayBuffer
    var ab = new ArrayBuffer(byteString.length);
    // create a view into the buffer
    var ia = new Uint8Array(ab);
    // set the bytes of the buffer to the correct values
    for (var i = 0; i < byteString.length; i++) {
        ia[i] = byteString.charCodeAt(i);
    }

// write the ArrayBuffer to a blob, and you're done
    var blob = new Blob([ab], {type: mimeString});
    return blob;
}

/* prepares the fields to display a form in read only */
function createFormFix(schema, objectID) {

    let model = schema.ModelSchema[1].displayName;
    let object = getObjectWithID(objectID);
    let form = document.getElementById('form');
    form.childNodes[0].childNodes[0].childNodes[0].id = "formInputs";
    let formInputs = form.childNodes[0].childNodes[0].childNodes[0].childNodes[0].childNodes;
    let value;


    $(".col-xs-3.array-item-toolbox").hide();
    $(".col-xs-3.col-xs-offset-9.array-item-add.text-right").parent().hide();

    for (let i = 1; i < formInputs.length; i++) {


        if (formInputs[i].childNodes[0]) {
            if (formInputs[i].childNodes[0].childNodes[3]) {
                formInputs[i].childNodes[0].childNodes[3].className = "hide";

            }

            if (formInputs[i].childNodes[0].childNodes[0].childNodes[0]) {
                formInputs[i].childNodes[0].childNodes[0].childNodes[0].disabled = true;
            }
        }

        if (formInputs[i].childNodes[2]) {
            formInputs[i].childNodes[2].className = "input-fixed";
            formInputs[i].id = "span" + i;
            value = formInputs[i].childNodes[2].value;
            formInputs[i].childNodes[2].disabled = true;
        }
        else {
            if (formInputs[i].childNodes[0]) {
                formInputs[i].childNodes[0].childNodes[2].className = "input-fixed";
                formInputs[i].childNodes[0].id = "span" + i;
                value = formInputs[i].childNodes[0].childNodes[2].value;
                formInputs[i].childNodes[0].childNodes[2].disabled = true;
            }
        }

        if (formInputs[i].childNodes[0]) {
            if (formInputs[i].childNodes[0].childNodes[1]) {

                let length = formInputs[i].childNodes[0].childNodes[1].childNodes.length;

                for (let j = 0; j < length; j++) {

                    if (formInputs[i].childNodes[0].childNodes[1].childNodes[j]) {
                        if (formInputs[i].childNodes[0].childNodes[1].childNodes[j].childNodes[0].childNodes[0].childNodes[0]) {
                            let rootElement = formInputs[i].childNodes[0].childNodes[1].childNodes[j].childNodes[0].childNodes[0].childNodes[0];

                            let attributesPropertiesLength = rootElement.childNodes.length;

                            for (let k = 0; k < attributesPropertiesLength; k++) {

                                if (rootElement.childNodes[k] && k === 0) {

                                    let textContent = rootElement.childNodes[k].textContent;
                                    if (textContent.endsWith("*")) {
                                        textContent = textContent.substring(0, textContent.length - 1);
                                    }

                                    rootElement.childNodes[k].innerHTML = textContent;
                                    rootElement.childNodes[k].textContent = textContent;
                                    rootElement.childNodes[k].text = textContent;
                                    rootElement.childNodes[k].value = textContent;
                                }

                                if (rootElement.childNodes[k].childNodes[0].childNodes[3]) {
                                    rootElement.childNodes[k].childNodes[0].childNodes[3].className = "hide"
                                }

                                if (rootElement.childNodes[k].childNodes[0].childNodes[0]) {
                                    if (rootElement.childNodes[k].childNodes[0].childNodes[0].childNodes[0]) {
                                        if (rootElement.childNodes[k].childNodes[0].childNodes[0].childNodes[0]) {
                                            rootElement.childNodes[k].childNodes[0].childNodes[0].childNodes[0].className = "input-fixed";
                                            rootElement.childNodes[k].childNodes[0].childNodes[0].childNodes[0].disabled = true;
                                        }
                                    }
                                }

                                if (rootElement.childNodes[k].childNodes[2]) {


                                    rootElement.childNodes[k].childNodes[2].className = "input-fixed";
                                    rootElement.childNodes[k].childNodes[2].disabled = true;
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (object) {

        let attributeList = schema.ModelSchema[1].attrMap[1]['Attribute List'][1];

        for (let i = 0; i < attributeList.length; i++) {

            let attribute = attributeList[i][1];
            let attributeName = attribute.Name;
            let primitiveAttribute = getPrimitiveAttribute(attribute.AttrType);
            let attrInput = document.getElementById("root_" + attributeName);

            if (primitiveAttribute === 'AttrIDList' || primitiveAttribute === 'AttrID' || primitiveAttribute === 'AttrAbstractAttachment') {

                attrInput.className = "hide";

                if (object[1].attrMap[1][attributeName]) {
                    let attrID = object[1].attrMap[1][attributeName][1];
                    if (attrID) {
                        let element = createElementFromHTML(switchAttr([attribute.AttrType, attrID], "formfix", attributeName, objectID));
                        if (element) {
                            if (primitiveAttribute === 'AttrAbstractAttachment') {
                                element.style = "margin-top:-10px;padding-left:8px";
                                attrInput.parentNode.parentNode.parentNode.appendChild(element);
                            }
                            else {
                                element.style = "padding-top:10px;padding-left:8px;display:block";
                                attrInput.parentNode.appendChild(element);
                            }
                        }
                    }
                }
            }
        }

        if (model === "Model") {

            if (object[1].attrMap[1]["Attribute List"]) {
                let attributeList = object[1].attrMap[1]["Attribute List"][1];
                if (attributeList) {
                    for (let i = 0; i < attributeList.length; i++) {
                        let obj = {};
                        let attribute = attributeList[i][1];
                        if (attribute) {
                            let attributeName = attribute.Name;

                            let attrInput = document.getElementById("root_nestedList_" + i + "_AttrTypeModel")

                            if (attrInput) {
                                attrInput.className = "hide";

                                let attrType = attribute.AttrType;

                                if (getPrimitiveAttribute(attrType) === 'AttrIDList') {
                                    attrType = 'AttrID';
                                }

                                let element = createElementFromHTML(switchAttr([attrType, attribute.AttrTypeModel], "formfix", "", ""));
                                if (element) {
                                    element.style = "padding-top:10px;padding-left:8px;display:block";
                                    attrInput.parentNode.appendChild(element);
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

function changeIDToName(schema, data) {

    let result = {};
    let attributeList = schema.ModelSchema[1].attrMap[1]['Attribute List'][1];
    let model = schema.ModelSchema[1].displayName;

    for (let i = 0; i < attributeList.length; i++) {

        let attribute = attributeList[i][1];
        let attributeName = attribute.Name;
        let attributeValue = data[attributeName];
        let primitiveAttribute = getPrimitiveAttribute(attribute.AttrType);

        if (attributeValue) {

            if (primitiveAttribute === 'AttrIDList') {

                let arrayAttr = attributeValue.split(",");
                let tmp_result = "";
                for (let i = 0; i < arrayAttr.length; i++) {

                    if (i == 0) {
                        tmp_result += getNameWithID(arrayAttr[i].trim());
                    } else {
                        tmp_result += ", " + getNameWithID(arrayAttr[i].trim());
                    }
                }

                result[attributeName] = tmp_result;

            } else if (primitiveAttribute === 'AttrID') {

                result[attributeName] = getNameWithID(attributeValue);

            }

            else {
                result[attributeName] = attributeValue;
            }
        }
    }


    if (model === "Model") {

        if (data.nestedList) {
            result.nestedList = JSON.parse(JSON.stringify(data.nestedList));

            for (let i = 0; i < result.nestedList.length; i++) {

                if (getPrimitiveAttribute(result.nestedList[i].AttrType) === "AttrID" || getPrimitiveAttribute(result.nestedList[i].AttrType) === "AttrIDList") {
                    if (result.nestedList[i].AttrTypeModel) {
                        result.nestedList[i].AttrTypeModel = getNameWithID(result.nestedList[i].AttrTypeModel);
                    }
                }
                if (result.nestedList[i].ACL) {

                    let attributeValue = result.nestedList[i].ACL;
                    attributeValue = attributeValue.split(",");

                    let stringValue = "";
                    for (let j = 0; j < attributeValue.length; j++) {

                        let name = getNameWithID(attributeValue[j]);
                        if (j === 0) {
                            stringValue += name;
                        } else {
                            stringValue += ', ' + name;
                        }
                    }
                    result.nestedList[i].ACL = stringValue;
                }
            }
        }
    }

    return result;
}


/*   UI : SEE  ChangeCollumnSize FUNCTION */