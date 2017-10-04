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
package org.hortonmachine.gvsig.epanet.database;

import static org.hortonmachine.gvsig.epanet.database.IEpanetTableConstants.*;

import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.vividsolutions.jts.geom.Point;
/**
 * The persistent class representing the epanet tanks table.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
@DatabaseTable(tableName = TANKS)
public class TanksTable implements IWork {

    /**
     * An automatic generated sequential id for the db.
     */
    @DatabaseField(columnName = OID, canBeNull = false, generatedId = true)
    private Long oid;

    /**
     * The id of the tank. Unique for each {@link EpanetRun}.
     */
    @DatabaseField(columnName = ID, canBeNull = false, uniqueCombo = true)
    private String id;

    /**
     * The {@link EpanetRun} to which the tank belongs to.
     */
    @DatabaseField(foreign = true, columnName = RUN_ID, canBeNull = false, uniqueCombo = true)
    private EpanetRun run;

    /**
     * The WKT of the geometry of the tank ( {@link Point} ).
     */
    @DatabaseField(columnName = WKT, canBeNull = true)
    private String wkt;

    /**
     * The AUTHORITY:CODE string to define the {@link CoordinateReferenceSystem crs}.
     */
    @DatabaseField(columnName = CRSCODE, canBeNull = true)
    private String crsCode;

    public Long getOid() {
        return oid;
    }

    public void setOid( Long oid ) {
        this.oid = oid;
    }

    public String getId() {
        return id;
    }

    public void setId( String id ) {
        this.id = id;
    }

    public EpanetRun getRun() {
        return run;
    }

    public void setRun( EpanetRun run ) {
        this.run = run;
    }

    public String getWkt() {
        return wkt;
    }

    public void setWkt( String wkt ) {
        this.wkt = wkt;
    }

    public String getCrsCode() {
        return crsCode;
    }

    public void setCrsCode( String crsCode ) {
        this.crsCode = crsCode;
    }

}
