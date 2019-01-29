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


function createModalProgress() {
    $('#main-div').append('<div class="modal fade" id="myModalProgress"></div>');
    let content = '<div class="modal-content" id="modal-content-progress">' +
        '<div class="modal-header" id="modal-header-progress"><div><h3></h3></div>' +
        '<div class="modal-body" id="modal-body-progress"></div>' +
        '</div>';
    $('#myModalProgress').append(content);
}

function closeModalProgress() {
    $('#myModalForm').modal('toggle');
}

let modalProgressMessage;
let modalProgressValue;

function launchModalProgress(message, value) {

    ("#modal-header-progress").remove;
    ("#modal-body-progress").remove;


    let modalHTML =
        '<div class="progressText block" id="progressText">' +
        '<span id="progressTextSpan" style="font-weight: bold;">progression : </span>' +
        '<div id="jqxProgressBar"></div>' +
        '</div>';

    if (!document.getElementById('jqxProgressBar')) {
        $('#modal-header-progress').append(modalHTML);
    }

    var modal = document.getElementById('myModalProgress');
    $('#myModalProgress').modal('toggle');
    window.onclick = function (event) {
        if (event.target !== modal) {
        }
    };

    let formModal = document.createElement("div");
    json_context.modalForm = true;
    formModal.id = "formModal";
    formModal.className = "formModal";
    document.getElementById('modal-content-progress').appendChild(formModal);
    document.getElementById('modal-content-progress').style.height = '80%';

    document.getElementById('modal-content-progress').style.overflow = "";

}