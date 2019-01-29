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


function init_graph() {

    if (json_context.active_view !== "graph") {
        if (json_context.activeID) {
            if (json_context.active_view === "form") {
                breadcrumbPop();
            } else if ((json_context.active_view === "list" || json_context.active_view === "kanban")) {
                let navIndex = document.getElementById("navIndex");
                navIndex.value = json_context.navSingleNavigationIndex + json_context.navLeftNumber;
            }
        }

        json_context.active_view = "graph";
        display_graph();
    }
}


function launch_graph() {
    let get = '/orm/ReadTree?id=' + json_context.activeID;
    let tmp = RequestGET(get, false);
    let chartData = JSON.parse(tmp);

    $('#chart-container').orgchart({
        'data': chartData,
        'nodeTitle': 'Model',
        'nodeContent': 'Name'
    });

    let nodes = document.getElementsByClassName('node'); // Onclick on every node of the graph
    for (let i = 0; i < nodes.length; i++) {
        let node = nodes[i];
        node.onclick = function () {
            let selectedNode = document.getElementsByClassName(('node focused'));
            if (selectedNode) {
                let title = selectedNode[0].childNodes[0].childNodes[1]; // Get the title
                let content = selectedNode[0].childNodes[1].childNodes[0]; // Get the content
                if (!title) {
                    title = selectedNode[0].childNodes[0].childNodes[0]; // Get the title when a node has no child
                }
                console.log(title.textContent);
                console.log(content.textContent);
            }
        }
    }
}

function display_graph() {

    //document.getElementById('main-div').style.overflow = "";
    if (!json_context.activeID) {
        $.notify("Please select one object to display the tree view", "warn");
        return;
    }

    let object = getObjectWithID(json_context.activeID);

    clean_div();
    let check = document.getElementById('orgchart');
    if (check === null) {
        json_context.create = false;
        //json_context.active_view = "graph";
        display_button();

        let graph = document.createElement("div");
        graph.id = "orgchart";
        graph.className = "div_orgchart";
        document.getElementById('panelContentmain-div').appendChild(graph);
        document.getElementById('main-div').style.height = '99.9%';
        $('#main-div').jqxPanel('scrollTo', 0, 0);
        let sec_graph = document.createElement("div");
        sec_graph.id = "chart-container";
        sec_graph.className = "chart-div";
        document.getElementById('orgchart').appendChild(sec_graph);
        launch_graph();

        breadcrumbPush(object[1].displayName, json_context);
    }
    document.getElementById('main-div').style.overflow = ""; // Dont display two scrollBar for the treegrid
}
