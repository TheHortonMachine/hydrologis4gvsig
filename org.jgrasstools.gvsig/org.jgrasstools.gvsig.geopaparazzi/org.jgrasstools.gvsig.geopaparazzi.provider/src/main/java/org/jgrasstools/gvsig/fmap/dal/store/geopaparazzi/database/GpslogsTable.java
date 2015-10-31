/*
 * JGrass - Free Open Source Java GIS http://www.jgrass.org 
 * (C) HydroloGIS - www.hydrologis.com 
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.jgrasstools.gvsig.fmap.dal.store.geopaparazzi.database;

import org.joda.time.DateTime;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * The persistent class representing the gpslogs table
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
@DatabaseTable(tableName = "gpslogs")
public class GpslogsTable {

    /**
     * An automatic generated sequential id for the db.
     */
    @DatabaseField(columnName = "_id", canBeNull = false, generatedId = true)
    private long id;

    @DatabaseField(columnName = "startts", canBeNull = false, dataType = DataType.DATE_TIME)
    private DateTime startts;

    @DatabaseField(columnName = "endts", canBeNull = false, dataType = DataType.DATE_TIME)
    private DateTime endts;

    @DatabaseField(columnName = "lengthm", canBeNull = false)
    private double lengthm;
    
    @DatabaseField(columnName = "isdirty", canBeNull = false)
    private int isdirty;

    @DatabaseField(columnName = "text", canBeNull = false)
    private String text;

    


}
