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
package org.jgrasstools.gvsig.epanet.database;

import org.joda.time.DateTime;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import static org.jgrasstools.gvsig.epanet.database.IEpanetTableConstants.*;

/**
 * The persistent class representing an epanet run.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
@DatabaseTable(tableName = EPANETRUN)
public class EpanetRun {

    @DatabaseField(columnName = ID, canBeNull = false, generatedId = true)
    private Long id;

    @DatabaseField(columnName = TITLE, canBeNull = false)
    private String title;

    @DatabaseField(columnName = DESCRIPTION, canBeNull = true)
    private String description;

    @DatabaseField(columnName = USER, canBeNull = true)
    private String user;

    @DatabaseField(columnName = UTCTIME, canBeNull = false, dataType = DataType.DATE_TIME)
    private DateTime utcTime;

    @DatabaseField(columnName = INP, canBeNull = false)
    private String inp;

    public Long getId() {
        return id;
    }

    public void setId( Long id ) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle( String title ) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription( String description ) {
        this.description = description;
    }

    public String getUser() {
        return user;
    }

    public void setUser( String user ) {
        this.user = user;
    }

    public DateTime getUtcTime() {
        return utcTime;
    }

    public void setUtcTime( DateTime utcTime ) {
        this.utcTime = utcTime;
    }

    public String getInp() {
        return inp;
    }

    public void setInp( String inp ) {
        this.inp = inp;
    }

}
