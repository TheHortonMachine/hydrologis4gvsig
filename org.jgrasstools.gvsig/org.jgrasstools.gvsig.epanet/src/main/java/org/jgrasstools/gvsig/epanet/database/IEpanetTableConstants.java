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

/**
 * The table and column names for epanet.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
@SuppressWarnings("nls")
public interface IEpanetTableConstants {

    /*
     * TABLES
     */
    public static final String EPANETRUN = "EPANETRUN";
    public static final String JUNCTIONS = "JUNCTIONS";
    public static final String JUNCTIONSRESULT = "JUNCTIONSRESULT";
    public static final String WORK_ID = "WORK_ID";
    public static final String PIPES = "PIPES";
    public static final String PIPESRESULT = "PIPESRESULT";
    public static final String PUMPS = "PUMPS";
    public static final String PUMPSRESULT = "PUMPSRESULT";
    public static final String RESERVOIRS = "RESERVOIRS";
    public static final String RESERVOIRSRESULT = "RESERVOIRSRESULT";
    public static final String TANKS = "TANKS";
    public static final String TANKSRESULT = "TANKSRESULT";
    public static final String VALVES = "VALVES";
    public static final String VALVESRESULT = "VALVESRESULT";

    /*
     * COLUMNS
     */
    public static final String ID = "ID";
    public static final String RUN_ID = "RUN_ID";
    public static final String OID = "OID";
    public static final String WKT = "WKT";
    public static final String LINKWKT = "LINKWKT";
    public static final String CRSCODE = "CRSCODE";
    public static final String DEMAND = "DEMAND";
    public static final String HEAD = "HEAD";
    public static final String PRESSURE = "PRESSURE";
    public static final String QUALITY = "QUALITY";
    public static final String UTCTIME = "UTCTIME";
    public static final String FLOW1 = "FLOW1";
    public static final String FLOW2 = "FLOW2";
    public static final String VELOCITY1 = "VELOCITY1";
    public static final String VELOCITY2 = "VELOCITY2";
    public static final String HEADLOSS = "HEADLOSS";
    public static final String STATUS = "STATUS";
    public static final String ENERGY = "ENERGY";
    public static final String TITLE = "TITLE";
    public static final String DESCRIPTION = "DESCRIPTION";
    public static final String USER = "USER";
    public static final String INP = "INP";

}
