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

(function ($) {
    let defaults = {
        callback: function () {
        },
        runOnLoad: true,
        frequency: 100,
        previousVisibility: null
    };

    let methods = {};
    methods.checkVisibility = function (element, options) {
        if (jQuery.contains(document, element[0])) {
            let wasVisible = options.previousVisibility;
            let visible = element.is(':visible');
            options.previousVisibility = visible;
            let initialLoad = wasVisible === null;
            if (initialLoad) {
                if (options.runOnLoad) {
                    options.callback(element, visible, initialLoad);
                }
            } else if (wasVisible !== visible) {
                options.callback(element, visible, initialLoad);
            }

            setTimeout(function () {
                methods.checkVisibility(element, options);
            }, options.frequency);
        }
    };

    $.fn.visibilityChanged = function (options) {
        let settings = $.extend({}, defaults, options);
        return this.each(function () {
            methods.checkVisibility($(this), settings);
        });
    };
})(jQuery);
