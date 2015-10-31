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

import static org.jgrasstools.gvsig.epanet.database.IEpanetTableConstants.*;

import org.joda.time.DateTime;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
/**
 * The persistent class representing the epanet tanks result table.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
@DatabaseTable(tableName = TANKSRESULT)
public class TanksResultsTable implements INodeResults {

    @DatabaseField(columnName = OID, canBeNull = false, generatedId = true)
    private Long oid;

    /**
     * The {@link EpanetRun} this result belongs to.
     */
    @DatabaseField(foreign = true, columnName = RUN_ID, canBeNull = false, uniqueCombo = true)
    private EpanetRun run;

    /**
     * The {@link TanksTable tank} this result is generated from.
     */
    @DatabaseField(foreign = true, columnName = WORK_ID, canBeNull = false, uniqueCombo = true)
    private TanksTable work;

    /**
     * The {@link DateTime time} of the simulation at which this result is created.
     */
    @DatabaseField(columnName = UTCTIME, canBeNull = false, dataType = DataType.DATE_TIME, uniqueCombo = true)
    private DateTime utcTime;

    /**
     * @see NodeParameters#EN_BASEDEMAND;
     */
    @DatabaseField(columnName = DEMAND, canBeNull = false)
    private float demand;

    /**
     * @see NodeParameters#EN_HEAD;
     */
    @DatabaseField(columnName = HEAD, canBeNull = false)
    private float head;

    /**
     * @see NodeParameters#EN_PRESSURE;
     */
    @DatabaseField(columnName = PRESSURE, canBeNull = false)
    private float pressure;

    /**
     * @see NodeParameters#EN_QUALITY;
     */
    @DatabaseField(columnName = QUALITY, canBeNull = false)
    private float quality;

    public Long getOid() {
        return oid;
    }

    public void setOid( Long oid ) {
        this.oid = oid;
    }

    public EpanetRun getRun() {
        return run;
    }

    public void setRun( EpanetRun run ) {
        this.run = run;
    }

    public TanksTable getWork() {
        return work;
    }

    public void setWork( TanksTable work ) {
        this.work = work;
    }

    public DateTime getUtcTime() {
        return utcTime;
    }

    public void setUtcTime( DateTime utcTime ) {
        this.utcTime = utcTime;
    }

    public float getDemand() {
        return demand;
    }

    public void setDemand( float demand ) {
        this.demand = demand;
    }

    public float getHead() {
        return head;
    }

    public void setHead( float head ) {
        this.head = head;
    }

    public float getPressure() {
        return pressure;
    }

    public void setPressure( float pressure ) {
        this.pressure = pressure;
    }

    public float getQuality() {
        return quality;
    }

    public void setQuality( float quality ) {
        this.quality = quality;
    }

}
