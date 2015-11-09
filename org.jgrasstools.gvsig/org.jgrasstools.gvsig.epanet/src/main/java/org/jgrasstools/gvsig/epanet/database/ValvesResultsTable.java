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
 * The persistent class representing the epanet valve result table.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
@DatabaseTable(tableName = VALVESRESULT)
public class ValvesResultsTable implements ILinkResults {

    @DatabaseField(columnName = OID, canBeNull = false, generatedId = true)
    private Long oid;

    /**
     * The {@link EpanetRun} this result belongs to.
     */
    @DatabaseField(foreign = true, columnName = RUN_ID, canBeNull = false, uniqueCombo = true)
    private EpanetRun run;

    /**
     * The {@link ValvesTable valve} this result is generated from.
     */
    @DatabaseField(foreign = true, columnName = WORK_ID, canBeNull = false, uniqueCombo = true, foreignAutoRefresh = true, maxForeignAutoRefreshLevel = 1)
    private ValvesTable work;

    /**
     * The {@link DateTime time} of the simulation at which this result is created.
     */
    @DatabaseField(columnName = UTCTIME, canBeNull = false, dataType = DataType.DATE_TIME, uniqueCombo = true)
    private DateTime utcTime;

    /**
     * @see LinkParameters#EN_FLOW;
     */
    @DatabaseField(columnName = FLOW1, canBeNull = false)
    private float flow1;

    /**
     * @see LinkParameters#EN_FLOW;
     */
    @DatabaseField(columnName = FLOW2, canBeNull = false)
    private float flow2;

    /**
     * @see LinkParameters#EN_VELOCITY;
     */
    @DatabaseField(columnName = VELOCITY1, canBeNull = false)
    private float velocity1;

    /**
     * @see LinkParameters#EN_VELOCITY;
     */
    @DatabaseField(columnName = VELOCITY2, canBeNull = false)
    private float velocity2;

    /**
     * @see LinkParameters#EN_HEADLOSS;
     */
    @DatabaseField(columnName = HEADLOSS, canBeNull = false)
    private float headloss;

    /**
     * @see LinkParameters#EN_STATUS;
     */
    @DatabaseField(columnName = STATUS, canBeNull = false)
    private float status;

    public String getId() {
        return work.getId();
    }

    public float getEnergy() {
        return Float.NaN;
    }

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

    public ValvesTable getWork() {
        return work;
    }

    public void setWork( ValvesTable work ) {
        this.work = work;
    }

    public DateTime getUtcTime() {
        return utcTime;
    }

    public void setUtcTime( DateTime utcTime ) {
        this.utcTime = utcTime;
    }

    public float getFlow1() {
        return flow1;
    }

    public void setFlow1( float flow1 ) {
        this.flow1 = flow1;
    }

    public float getFlow2() {
        return flow2;
    }

    public void setFlow2( float flow2 ) {
        this.flow2 = flow2;
    }

    public float getVelocity1() {
        return velocity1;
    }

    public void setVelocity1( float velocity1 ) {
        this.velocity1 = velocity1;
    }

    public float getVelocity2() {
        return velocity2;
    }

    public void setVelocity2( float velocity2 ) {
        this.velocity2 = velocity2;
    }

    public float getHeadloss() {
        return headloss;
    }

    public void setHeadloss( float headloss ) {
        this.headloss = headloss;
    }

    public float getStatus() {
        return status;
    }

    public void setStatus( float status ) {
        this.status = status;
    }

}
