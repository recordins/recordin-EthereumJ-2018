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

var windowWidth;
var windowHeight;


var mainWindow = {
    type: 'tabbedGroup',
    orientation: 'vertical',
    width: '80%',
//    allowClose: false,
    items: [{
        title: "Main window",
        type: 'layoutPanel',
        height: '80%',
        minHeight: 200,
        contentContainer: 'mainWindow'
    }]
};

var leftPanel = {
    type: 'tabbedGroup',
    width: '20%',
    minWidth: 150,
//    allowClose: false,
    items: [{
        type: 'layoutPanel',
        title: 'Attributes',
        contentContainer: 'sidenav1'
    }, {
        type: 'layoutPanel',
        title: 'History',
        contentContainer: 'sidenav2'
    }]
};

var scrollDiv;

function launch_layout() {
    //   let windowWidth = $(window).width();
    //   let windowHeight = $(window).height();
    //   json_context.layout_width = windowWidth / 100 * 80 - 3;
    //   json_context.layout_height = windowHeight - 142;

    let height;
    if ($(window).width() <= 1024)
        height = "80%";
    else
        height = "85%";
    let layout = [{
        type: 'layoutGroup',
        orientation: 'horizontal',
        items: [mainWindow, leftPanel]
    }];

    $('#jqxDockingLayout').jqxDockingLayout({
        width: '100%',
        height: '100%',
        layout: layout
    });
    $("#main-div").jqxPanel({
        scrollBarSize: 15,
        width: '99.7%',
        height: '99.7%',
        autoUpdate: true
    });
    $("#attributes").jqxPanel({
        scrollBarSize: 15,
        width: '99.5%',
        height: '99.7%',
        autoUpdate: true
    });
    $("#objectHistory").jqxPanel({
        scrollBarSize: 15,
        width: '99.5%',
        height: '99.7%',
        autoUpdate: true
    });


//$('#main-div').css("cssText", "overflow: hidden !important;"); // Dont display two scrollBar for the treegrid
    //$('#main-div').parent().css("cssText", "overflow: hidden !important;"); // Dont display two scrollBar for the treegrid

    //let mainDiv = document.getElementById('main-div');
    //mainDiv.style.overflow = "hidden";
    //mainDiv.offsetParent.style.overflow = "hidden";
    //document.getElementById('panelWrappermain-div').style.height = "180%";
    //document.getElementById('panelWrapperattributes').style.height = "200%";

    //let attrContainer = document.getElementById('attrMapContainer');


    let panel = document.getElementById('jqxDockingLayout');
    panel.childNodes[0].childNodes[1].childNodes[1].childNodes[0].childNodes[0].childNodes[0].className = "jqx-widget-content jqx-ribbon-content-section jqx-ribbon-content-section-bottom sideTable";


//panel.childNodes[0].childNodes[1].childNodes[1].childNodes[0].childNodes[0].childNodes[0].id = 'sideTable';
    //panel.childNodes[0].childNodes[1].childNodes[1].id = 'sideTable';
    //$("#sideTable").jqxPanel({width: $(window).width(), height: $(window).height() * 20 / 100});
//    json_context.savedLayout = $('#jqxDockingLayout').jqxDockingLayout('saveLayout');
    //$('#jqxDockingLayout').jqxDockingLayout('loadLayout', json_context.savedLayout);
//    scrollDiv = document.getElementById("jqxDockingLayout").childNodes[0].childNodes[0].childNodes[1];
//    scrollDiv.id = "scrollDiv";
//    scrollDiv.onscroll = function () {
//    };

}