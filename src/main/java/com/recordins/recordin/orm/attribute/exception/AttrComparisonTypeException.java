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

package com.recordins.recordin.orm.attribute.exception;

public class AttrComparisonTypeException extends RuntimeException {


    private AttrComparisonTypeException(String message) {
        super(message);
        this.printStackTrace();
    }

    public AttrComparisonTypeException(Class attr1, Class attr2) {
        super("'" + attr1.getClass().getSimpleName() + "' is not assignable from '" + attr2.getClass().getSimpleName() + "'");
        this.printStackTrace();
    }
}
