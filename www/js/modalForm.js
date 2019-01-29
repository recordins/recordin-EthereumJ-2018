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


function createModalForm() {
    if (isIE()) {
        $("body").append('<div class="modal fade" id="myModalForm"></div>');
    }
    else {
        $('#main-div').append('<div class="modal fade" id="myModalForm"></div>');
    }

    let content = '<div class="modal-content" id="modal-content-form">' +
        '<div class="modal-header" id="modal-header-form"><div><h3></h3></div>' +
        //'<div class="modal-body" id="modal-body-form"></div>' +
        '</div>';
    $('#myModalForm').append(content);
}

function closeModalForm() {
    json_context.modalForm = false;
    $('#myModalForm').modal('toggle');
}

let modalSchemaForm;
let modalFormReturnFunction;
let modalFormData;

function launchModalForm(model, returnFunctionCall, object) {

    modalSchemaForm = RequestGET('/model/JsonSchema?model=' + model, false);

    if (modalSchemaForm) {
        if (document.getElementById("modal-content-form") === null) {
            createModalForm();
        }


        ("#modal-header-form").remove;
        ("#modal-body-form").remove;
        /*let modalHTML = '<div>' +
         '<span class="close" onclick="closeModalForm()">&times;</span><br>' +
         '<nav id="navModalForm" class="navbar navbar-default">' +
         ' <div>' +
         ' <div class="block left_btn" id="saveCancelModal">' +
         '<button id="saveButtonCloseModal" type="submit" class="btn btn-success" onclick="submitModalForm()">Submit</button>&nbsp;' +
         '<button type="button" class="btn btn-primary" onclick="closeModalForm()">Cancel</button>' +
         '</div>' +
         ' </div>' +
         ' </nav>' +
         '<div>';*/
        let modalHTML = '<div id="ButtonFormModal">' +
            '<button id="saveButtonCloseModal" type="submit" class="btn btn-success" onclick="submitModalForm()">Submit</button>&nbsp;' +
            '<button type="button" class="btn btn-primary" onclick="closeModalForm()">Cancel</button>' +
            '<span class="close" onclick="closeModalForm()">&times;</span><br>' +
            '<div>';
        if (!document.getElementById('ButtonFormModal')) {
            $('#modal-header-form').append(modalHTML);
        }

        var modal = document.getElementById('myModalForm');
        $('#myModalForm').modal('toggle');
        window.onclick = function (event) {
            if (event.target !== modal) {
            }
        };

        cleanModal();
        modalFormReturnFunction = returnFunctionCall;
        displayFormModal(modalSchemaForm, model, object);
    }
}

function displayFormModal(schema, model, object) {

    let formModal = document.createElement("div");
    json_context.modalForm = true;
    formModal.id = "formModal";
    formModal.className = "formModal";
    document.getElementById('modal-content-form').appendChild(formModal);
    document.getElementById('modal-content-form').style.height = '80%';
    json_context.active_view = "form";
    json_context.mode = "edit";

    //console.log("OWN OBJECT: " + JSON.stringify(object));

    if (object) {
        schema = getModelSchemaFromObject(object);
        console.log("OWN schema: " + JSON.stringify(schema));

        parseDataForm(object, schema, true);

        modalFormData = json_context.modal_formData;
    }
    else {
        modalFormData = initDataFormCreate(model, modalSchemaForm, true);
    }

    let formSchemaFormatted = generate_form(schema, true);
    createFormModal(formSchemaFormatted, schema, modalFormData);

    display_button();
    document.getElementById('modal-content-form').style.overflow = ""; // Dont display two scrollBar for the treegrid
}

function createFormModal(formSchemaFormatted, formSchema, formData) {


    let displayData = changeIDToName(formSchema, formData);

    let uiSchema = getUISchema(formSchema);

    ReactDOM.render(
        React.createElement("div", {id: "container"},
            React.createElement(Form, {
                    schema: formSchemaFormatted,
                    //uiSchema: json_context.mode === "view" ? disabled : uiMain,
                    uiSchema: uiSchema,
                    //onSubmit: json_context.mode === "view" ? "" : onSubmit,
                    onSubmit: onSubmit,
                    noValidate: true,
                    formData: displayData,
                    onChange: onChangeForm
                },
                React.createElement("button", {
                    className: "btn btn-success right",
                    type: "submit",
                    id: "submitModal"
                }, "submit"))),
        document.getElementById("formModal"));
    //document.getElementById('submit').style.visibility = "hidden";
    $('#submitModal').hide();

    if (json_context.mode === 'edit') {
        setClickEvents(formSchema, modalFormData);
    } else {
        createFormFix(formSchema);
    }
}


function submitModalForm() {
    //json_context.create = true;
    submitted(modalFormData, modalFormReturnFunction);
}


function onChangeForm(data) {


    //console.log("BEFORE: ");
    //console.log(modalFormData);

    if (data.formData) {
        if (modalSchemaForm) {
            let attributeList = modalSchemaForm.ModelSchema[1].attrMap[1]['Attribute List'][1];
            for (let i = 0; i < attributeList.length; i++) {

                let attribute = attributeList[i][1];
                let key = attribute.Name;

                if (getAttrType(key, modalSchemaForm) === "AttrID" || getAttrType(key, modalSchemaForm) === "AttrIDList") {
                    // do nothing for specific attributes
                } else {
                    if (data.formData[attribute.Name]) {
                        modalFormData[attribute.Name] = data.formData[attribute.Name];
                    } else {
                        modalFormData[attribute.Name] = "";
                    }
                }
            }
        }

    }

    //console.log("AFTER: ");
    //console.log(modalFormData);
}