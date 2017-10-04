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
package org.hortonmachine.gvsig.epanet.core;

import java.awt.Color;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import org.gvsig.fmap.mapcontext.exceptions.LegendLayerException;
import org.gvsig.fmap.mapcontext.layers.FLayer;
import org.gvsig.fmap.mapcontext.layers.FLayers;
import org.gvsig.fmap.mapcontext.layers.vectorial.FLyrVect;
import org.gvsig.fmap.mapcontext.rendering.legend.IVectorLegend;
import org.hortonmachine.gvsig.base.StyleUtilities;
import org.hortonmachine.gvsig.epanet.CreateProjectFilesExtension;
import org.hortonmachine.gvsig.epanet.database.EpanetRun;
import org.hortonmachine.gvsig.epanet.database.IEpanetTableConstants;
import org.hortonmachine.gvsig.epanet.database.ILinkResults;
import org.hortonmachine.gvsig.epanet.database.INodeResults;
import org.hortonmachine.gvsig.epanet.database.IResult;
import org.hortonmachine.gvsig.epanet.database.IWork;
import org.hortonmachine.gvsig.epanet.database.JunctionsResultsTable;
import org.hortonmachine.gvsig.epanet.database.JunctionsTable;
import org.hortonmachine.gvsig.epanet.database.PipesTable;
import org.hortonmachine.gvsig.epanet.database.PumpsTable;
import org.hortonmachine.gvsig.epanet.database.ReservoirsTable;
import org.hortonmachine.gvsig.epanet.database.TanksTable;
import org.hortonmachine.gvsig.epanet.database.ValvesTable;
import org.hortonmachine.hmachine.modules.networktools.epanet.OmsEpanet;
import org.hortonmachine.hmachine.modules.networktools.epanet.core.EpanetFeatureTypes;
import org.hortonmachine.hmachine.modules.networktools.epanet.core.ResultsLinkParameters;
import org.hortonmachine.hmachine.modules.networktools.epanet.core.ResultsNodeParameters;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;
import com.j256.ormlite.support.ConnectionSource;

/**
 * Utilities around Epanet.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
@SuppressWarnings({"nls", "unchecked", "rawtypes"})
public class EpanetUtilities {

    public static final String SELECTED_POSTFIX = "_sel";
    public static HashMap<String, String> type2ImageMap = new HashMap<String, String>();
    static {
        type2ImageMap.put(EpanetFeatureTypes.Junctions.ID.getName(), "images/styles/junctions.svg");
        type2ImageMap.put(EpanetFeatureTypes.Tanks.ID.getName(), "images/styles/tanks.svg");
        type2ImageMap.put(EpanetFeatureTypes.Pumps.ID.getName(), "images/styles/pumps.svg");
        type2ImageMap.put(EpanetFeatureTypes.Valves.ID.getName(), "images/styles/valves.svg");
        type2ImageMap.put(EpanetFeatureTypes.Reservoirs.ID.getName(), "images/styles/reservoirs.svg");
        type2ImageMap.put(EpanetFeatureTypes.Junctions.ID.getName() + SELECTED_POSTFIX, "images/styles/junctions_sel.svg");
        type2ImageMap.put(EpanetFeatureTypes.Tanks.ID.getName() + SELECTED_POSTFIX, "images/styles/tanks_sel.svg");
        type2ImageMap.put(EpanetFeatureTypes.Pumps.ID.getName() + SELECTED_POSTFIX, "images/styles/pumps_sel.svg");
        type2ImageMap.put(EpanetFeatureTypes.Valves.ID.getName() + SELECTED_POSTFIX, "images/styles/valves_sel.svg");
        type2ImageMap.put(EpanetFeatureTypes.Reservoirs.ID.getName() + SELECTED_POSTFIX, "images/styles/reservoirs_sel.svg");
    }

    /**
     * The rainbow colormap .
     */
    public final static int[][] rainbow = new int[][]{//
            {255, 255, 0}, //
            {0, 255, 0}, //
            {0, 255, 255}, //
            {0, 0, 255}, //
            {255, 0, 255}, //
            {255, 0, 0}//
    };

    // public static void getGeometries( Session session, EpanetRun run,
    // HashMap<String, NodeRenderProperties> nodesRenderingProperties,
    // HashMap<String, LinkRenderProperties> linksRenderingProperties ) throws Exception {
    // if (run == null) {
    // return;
    // }
    // float pointWidth = 8f;
    // float lineWidth = 4f;
    //
    // nodesRenderingProperties.clear();
    // linksRenderingProperties.clear();
    //
    // CoordinateReferenceSystem crs = null;
    // WKTReader reader = new WKTReader();
    // // junctions
    // Criteria c = session.createCriteria(JunctionsTable.class);
    // c.add(Restrictions.eq("run", run));
    // List<JunctionsTable> jList = c.list();
    // for( JunctionsTable jt : jList ) {
    // Geometry geometry = reader.read(jt.getWkt());
    // if (crs == null) {
    // crs = CRS.decode(jt.getCrsCode());
    // }
    // NodeRenderProperties rp = new NodeRenderProperties();
    // rp.crs = crs;
    // rp.geometry = geometry;
    // rp.width = pointWidth;
    // nodesRenderingProperties.put(jt.getId(), rp);
    // }
    // // tanks
    // c = session.createCriteria(TanksTable.class);
    // c.add(Restrictions.eq("run", run));
    // List<TanksTable> tList = c.list();
    // for( TanksTable tt : tList ) {
    // Geometry geometry = reader.read(tt.getWkt());
    // if (crs == null) {
    // crs = CRS.decode(tt.getCrsCode());
    // }
    // NodeRenderProperties rp = new NodeRenderProperties();
    // rp.crs = crs;
    // rp.geometry = geometry;
    // rp.width = pointWidth;
    // nodesRenderingProperties.put(tt.getId(), rp);
    // }
    // // reservoirs
    // c = session.createCriteria(ReservoirsTable.class);
    // c.add(Restrictions.eq("run", run));
    // List<ReservoirsTable> rList = c.list();
    // for( ReservoirsTable rt : rList ) {
    // Geometry geometry = reader.read(rt.getWkt());
    // if (crs == null) {
    // crs = CRS.decode(rt.getCrsCode());
    // }
    // NodeRenderProperties rp = new NodeRenderProperties();
    // rp.crs = crs;
    // rp.geometry = geometry;
    // rp.width = pointWidth;
    // nodesRenderingProperties.put(rt.getId(), rp);
    // }
    //
    // // pipes
    // c = session.createCriteria(PipesTable.class);
    // c.add(Restrictions.eq("run", run));
    // List<PipesTable> piList = c.list();
    // for( PipesTable pit : piList ) {
    // Geometry geometry = reader.read(pit.getWkt());
    // if (crs == null) {
    // crs = CRS.decode(pit.getCrsCode());
    // }
    // LinkRenderProperties rp = new LinkRenderProperties();
    // rp.crs = crs;
    // Line line = rp.new Line();
    // rp.line = line;
    // line.geometry = geometry;
    // line.width = lineWidth;
    // linksRenderingProperties.put(pit.getId(), rp);
    // }
    // // pumps
    // c = session.createCriteria(PumpsTable.class);
    // c.add(Restrictions.eq("run", run));
    // List<PumpsTable> puList = c.list();
    // for( PumpsTable put : puList ) {
    // Geometry geometry = reader.read(put.getLinkWkt());
    // Geometry pointGeometry = reader.read(put.getWkt());
    // if (crs == null) {
    // crs = CRS.decode(put.getCrsCode());
    // }
    // LinkRenderProperties rp = new LinkRenderProperties();
    // rp.crs = crs;
    // Line line = rp.new Line();
    // rp.line = line;
    // line.geometry = geometry;
    // line.width = lineWidth;
    // Point point = rp.new Point();
    // rp.point = point;
    // point.geometry = pointGeometry;
    // point.width = pointWidth;
    // linksRenderingProperties.put(put.getId(), rp);
    // }
    // // valves
    // c = session.createCriteria(ValvesTable.class);
    // c.add(Restrictions.eq("run", run));
    // List<ValvesTable> vList = c.list();
    // for( ValvesTable vt : vList ) {
    // Geometry geometry = reader.read(vt.getLinkWkt());
    // Geometry pointGeometry = reader.read(vt.getWkt());
    // if (crs == null) {
    // crs = CRS.decode(vt.getCrsCode());
    // }
    // LinkRenderProperties rp = new LinkRenderProperties();
    // rp.crs = crs;
    // Line line = rp.new Line();
    // rp.line = line;
    // line.geometry = geometry;
    // line.width = lineWidth;
    // Point point = rp.new Point();
    // rp.point = point;
    // point.geometry = pointGeometry;
    // point.width = pointWidth;
    // linksRenderingProperties.put(vt.getId(), rp);
    // }
    // }

    // /**
    // * Deletes a {@link EpanetRun run} from the database.
    // *
    // * @param run the run to remove.
    // * @param session the session to use.
    // * @throws IOException
    // */
    // public static void removeRun( EpanetRun run, Session session ) throws IOException {
    // Transaction transaction = session.beginTransaction();
    // try {
    // // junctions results
    // Criteria c = session.createCriteria(JunctionsResultsTable.class);
    // c.add(Restrictions.eq("run", run));
    // List<JunctionsResultsTable> jrT = c.list();
    // for( JunctionsResultsTable junctionsResultsTable : jrT ) {
    // session.delete(junctionsResultsTable);
    // }
    // // junctions
    // c = session.createCriteria(JunctionsTable.class);
    // c.add(Restrictions.eq("run", run));
    // List<JunctionsTable> jT = c.list();
    // for( JunctionsTable junctionsTable : jT ) {
    // session.delete(junctionsTable);
    // }
    // // pipes results
    // c = session.createCriteria(PipesResultsTable.class);
    // c.add(Restrictions.eq("run", run));
    // List<PipesResultsTable> prT = c.list();
    // for( PipesResultsTable pipesResultsTable : prT ) {
    // session.delete(pipesResultsTable);
    // }
    // // pipes
    // c = session.createCriteria(PipesTable.class);
    // c.add(Restrictions.eq("run", run));
    // List<PipesTable> pT = c.list();
    // for( PipesTable pipesTable : pT ) {
    // session.delete(pipesTable);
    // }
    // // pumps results
    // c = session.createCriteria(PumpsResultsTable.class);
    // c.add(Restrictions.eq("run", run));
    // List<PumpsResultsTable> purT = c.list();
    // for( PumpsResultsTable pumpsResultsTable : purT ) {
    // session.delete(pumpsResultsTable);
    // }
    // // pumps
    // c = session.createCriteria(PumpsTable.class);
    // c.add(Restrictions.eq("run", run));
    // List<PumpsTable> puT = c.list();
    // for( PumpsTable pumpsTable : puT ) {
    // session.delete(pumpsTable);
    // }
    // // Reservoirs results
    // c = session.createCriteria(ReservoirsResultsTable.class);
    // c.add(Restrictions.eq("run", run));
    // List<ReservoirsResultsTable> rrT = c.list();
    // for( ReservoirsResultsTable reservoirsResultsTable : rrT ) {
    // session.delete(reservoirsResultsTable);
    // }
    // // Reservoirs
    // c = session.createCriteria(ReservoirsTable.class);
    // c.add(Restrictions.eq("run", run));
    // List<ReservoirsTable> rT = c.list();
    // for( ReservoirsTable reservoirsTable : rT ) {
    // session.delete(reservoirsTable);
    // }
    // // Tanks results
    // c = session.createCriteria(TanksResultsTable.class);
    // c.add(Restrictions.eq("run", run));
    // List<TanksResultsTable> trT = c.list();
    // for( TanksResultsTable tanksResultsTable : trT ) {
    // session.delete(tanksResultsTable);
    // }
    // // Tanks
    // c = session.createCriteria(TanksTable.class);
    // c.add(Restrictions.eq("run", run));
    // List<TanksTable> tT = c.list();
    // for( TanksTable tanksTable : tT ) {
    // session.delete(tanksTable);
    // }
    // // Valves results
    // c = session.createCriteria(ValvesResultsTable.class);
    // c.add(Restrictions.eq("run", run));
    // List<ValvesResultsTable> vrT = c.list();
    // for( ValvesResultsTable valvesResultsTable : vrT ) {
    // session.delete(valvesResultsTable);
    // }
    // // Valves
    // c = session.createCriteria(ValvesTable.class);
    // c.add(Restrictions.eq("run", run));
    // List<ValvesTable> vT = c.list();
    // for( ValvesTable valvesTable : vT ) {
    // session.delete(valvesTable);
    // }
    //
    // session.delete(run);
    //
    // transaction.commit();
    // } catch (Exception e) {
    // transaction.rollback();
    // throw new IOException("An error occurred while removing a run.");
    // }
    // }

    public static void styleEpanetLayers( FLayers layers ) throws Exception {
        String name = EpanetFeatureTypes.Junctions.ID.getName();
        FLayer layer = layers.getLayer(name);
        styleEpanetPointLayer(name, layer);
        name = EpanetFeatureTypes.Pumps.ID.getName();
        layer = layers.getLayer(name);
        styleEpanetPointLayer(name, layer);
        name = EpanetFeatureTypes.Valves.ID.getName();
        layer = layers.getLayer(name);
        styleEpanetPointLayer(name, layer);
        name = EpanetFeatureTypes.Tanks.ID.getName();
        layer = layers.getLayer(name);
        styleEpanetPointLayer(name, layer);
        name = EpanetFeatureTypes.Reservoirs.ID.getName();
        layer = layers.getLayer(name);
        styleEpanetPointLayer(name, layer);

        // pipes
        layer = layers.getLayer(EpanetFeatureTypes.Pipes.ID.getName());
        if (layer instanceof FLyrVect) {
            FLyrVect vlayer = (FLyrVect) layer;
            IVectorLegend pipesLegend = StyleUtilities.createSimpleLineLegend(Color.CYAN, 3, 255);
            vlayer.setLegend(pipesLegend);
        }
    }

    private static void styleEpanetPointLayer( String name, FLayer layer ) throws IOException, LegendLayerException {
        if (layer instanceof FLyrVect) {
            FLyrVect vlayer = (FLyrVect) layer;
            String imgPath = EpanetUtilities.type2ImageMap.get(name);
            String selImgPath = EpanetUtilities.type2ImageMap.get(name + EpanetUtilities.SELECTED_POSTFIX);
            IVectorLegend imagePointLegend = StyleUtilities.createImagePointLegend(CreateProjectFilesExtension.class, imgPath,
                    selImgPath, 15);
            vlayer.setLegend(imagePointLegend);
        }
    }

    /**
    * Returns the id of all the nodes.
    *
     * @param junctionsDao
     * @param tanksDao
     * @param reservoirsDao
     * @param run the current run.
     * @return the array of id string of the nodes.
     * @throws SQLException 
     */
    public static String[] getNodes( Dao<JunctionsTable, Long> junctionsDao, Dao<TanksTable, Long> tanksDao,
            Dao<ReservoirsTable, Long> reservoirsDao, EpanetRun run ) throws SQLException {
        if (run == null) {
            return new String[]{null};
        }
        List<String> nodesIdsList = new ArrayList<String>();
        nodesIdsList.add(null);

        JunctionsTable jt = new JunctionsTable();
        jt.setRun(run);
        List<JunctionsTable> jtList = junctionsDao.queryForMatching(jt);
        for( JunctionsTable junctionsTable : jtList ) {
            nodesIdsList.add(junctionsTable.getId());
        }
        TanksTable tt = new TanksTable();
        tt.setRun(run);
        List<TanksTable> ttList = tanksDao.queryForMatching(tt);
        for( TanksTable tankTable : ttList ) {
            nodesIdsList.add(tankTable.getId());
        }
        ReservoirsTable rt = new ReservoirsTable();
        rt.setRun(run);
        List<ReservoirsTable> rtList = reservoirsDao.queryForMatching(rt);
        for( ReservoirsTable reservoirTable : rtList ) {
            nodesIdsList.add(reservoirTable.getId());
        }
        String[] ids = new String[nodesIdsList.size()];
        for( int i = 0; i < ids.length; i++ ) {
            ids[i] = nodesIdsList.get(i);
        }
        return ids;
    }

    /**
    * Returns the id of all the links.
    *
     * @param pipesDao
     * @param pumpsDao
     * @param valvesDao
     * @param run the current run.
     * @return the array of id string of the links.
     * @throws SQLException
     */
    public static String[] getLinks( Dao<PipesTable, Long> pipesDao, Dao<PumpsTable, Long> pumpsDao,
            Dao<ValvesTable, Long> valvesDao, EpanetRun run ) throws SQLException {
        if (run == null) {
            return new String[]{null};
        }
        List<String> linksIdsList = new ArrayList<String>();
        linksIdsList.add(null);

        PipesTable pipt = new PipesTable();
        pipt.setRun(run);
        List<PipesTable> jtList = pipesDao.queryForMatching(pipt);
        for( PipesTable pipesTable : jtList ) {
            linksIdsList.add(pipesTable.getId());
        }
        PumpsTable pt = new PumpsTable();
        pt.setRun(run);
        List<PumpsTable> ttList = pumpsDao.queryForMatching(pt);
        for( PumpsTable pumpTable : ttList ) {
            linksIdsList.add(pumpTable.getId());
        }
        ValvesTable vt = new ValvesTable();
        vt.setRun(run);
        List<ValvesTable> rtList = valvesDao.queryForMatching(vt);
        for( ValvesTable valveTable : rtList ) {
            linksIdsList.add(valveTable.getId());
        }
        String[] ids = new String[linksIdsList.size()];
        for( int i = 0; i < ids.length; i++ ) {
            ids[i] = linksIdsList.get(i);
        }
        return ids;
    }

    public static float[] getLinksMinMax( Dao<ILinkResults, Long> pipesResultsDao, Dao<ILinkResults, Long> pumpsResultsDao,
            Dao<ILinkResults, Long> valvesResultsDao, EpanetRun run, DateTime time, ResultsLinkParameters linkVar )
                    throws Exception {
        // Pipes
        float max = Float.NEGATIVE_INFINITY;
        float min = Float.POSITIVE_INFINITY;

        String var = linkVar.getKey().toLowerCase();
        if (linkVar != ResultsLinkParameters.ENERGY) {
            String v1 = var;
            String v2 = null;
            if (linkVar == ResultsLinkParameters.FLOW || linkVar == ResultsLinkParameters.VELOCITY) {
                v1 = var + "1";
                v2 = var + "2";
            }

            ILinkResults[] minMax = getMinMax4Link(pipesResultsDao, run, time, v1);

            float[] tmpMinMax = getMinMax4LinkType(linkVar, minMax);
            min = tmpMinMax[0];
            max = tmpMinMax[1];
            if (v2 != null) {
                ILinkResults[] minMax2 = getMinMax4Link(pipesResultsDao, run, time, v2);
                tmpMinMax = getMinMax4LinkType(linkVar, minMax2);
                min = tmpMinMax[0];
                max = tmpMinMax[1];
            }
        }

        String v1 = var;
        String v2 = null;
        if (linkVar == ResultsLinkParameters.FLOW || linkVar == ResultsLinkParameters.VELOCITY) {
            v1 = var + "1";
            v2 = var + "2";
        }
        float tmpMax = Float.NEGATIVE_INFINITY;
        float tmpMin = Float.POSITIVE_INFINITY;
        ILinkResults[] minMax = getMinMax4Link(pumpsResultsDao, run, time, v1);
        if (minMax != null) {
            float[] tmpMinMax = getMinMax4LinkType(linkVar, minMax);
            tmpMin = tmpMinMax[0];
            tmpMax = tmpMinMax[1];
            if (v2 != null) {
                ILinkResults[] minMax2 = getMinMax4Link(pumpsResultsDao, run, time, v2);
                tmpMinMax = getMinMax4LinkType(linkVar, minMax2);
                tmpMin = tmpMinMax[0];
                tmpMax = tmpMinMax[1];
            }
            min = Math.min(tmpMin, min);
            max = Math.max(tmpMax, max);
        }
        if (linkVar != ResultsLinkParameters.ENERGY) {
            v1 = var;
            v2 = null;
            if (linkVar == ResultsLinkParameters.FLOW || linkVar == ResultsLinkParameters.VELOCITY) {
                v1 = var + "1";
                v2 = var + "2";
            }

            tmpMax = Float.NEGATIVE_INFINITY;
            tmpMin = Float.POSITIVE_INFINITY;
            minMax = getMinMax4Link(valvesResultsDao, run, time, v1);
            if (minMax != null) {
                float[] tmpMinMax = getMinMax4LinkType(linkVar, minMax);
                tmpMin = tmpMinMax[0];
                tmpMax = tmpMinMax[1];
                if (v2 != null) {
                    ILinkResults[] minMax2 = getMinMax4Link(valvesResultsDao, run, time, v2);
                    tmpMinMax = getMinMax4LinkType(linkVar, minMax2);
                    tmpMin = tmpMinMax[0];
                    tmpMax = tmpMinMax[1];
                }
                min = Math.min(tmpMin, min);
                max = Math.max(tmpMax, max);
            }
        }

        return new float[]{min, max};
    }

    public static float[] getNodesMinMax( Dao<INodeResults, Long> junctionsResultsDao, Dao<INodeResults, Long> tanksResultsDao,
            Dao<INodeResults, Long> reservoirsResultsDao, EpanetRun run, DateTime time, ResultsNodeParameters nodeVar )
                    throws Exception {
        // Pipes
        float max = Float.NEGATIVE_INFINITY;
        float min = Float.POSITIVE_INFINITY;

        String var = nodeVar.getKey().toLowerCase();
        INodeResults[] minMax = getMinMaxObject4Node(junctionsResultsDao, run, time, var);
        if (minMax != null) {
            float[] tmpMinMax = getMinMax4NodeType(nodeVar, minMax);
            min = tmpMinMax[0];
            max = tmpMinMax[1];
        }
        INodeResults[] minMaxTanks = getMinMaxObject4Node(tanksResultsDao, run, time, var);
        if (minMaxTanks != null) {
            float[] tmpMinMax = getMinMax4NodeType(nodeVar, minMaxTanks);
            min = Math.min(tmpMinMax[0], min);
            max = Math.max(tmpMinMax[1], max);
        }
        if (nodeVar != ResultsNodeParameters.PRESSURE) {
            INodeResults[] minMaxReservoirs = getMinMaxObject4Node(reservoirsResultsDao, run, time, var);
            if (minMaxReservoirs != null) {
                float[] tmpMinMax = getMinMax4NodeType(nodeVar, minMaxReservoirs);
                min = Math.min(tmpMinMax[0], min);
                max = Math.max(tmpMinMax[1], max);
            }
        }

        return new float[]{min, max};
    }

    private static float[] getMinMax4LinkType( ResultsLinkParameters linkVar, ILinkResults[] minMax ) {
        float[] tmpMinMax = new float[2];
        switch( linkVar ) {
        case FLOW:
            tmpMinMax[0] = minMax[0].getFlow1();
            tmpMinMax[1] = minMax[1].getFlow1();
            break;
        case VELOCITY:
            tmpMinMax[0] = minMax[0].getVelocity1();
            tmpMinMax[1] = minMax[1].getVelocity1();
            break;
        case ENERGY:
            tmpMinMax[0] = minMax[0].getEnergy();
            tmpMinMax[1] = minMax[1].getEnergy();
            break;
        case HEADLOSS:
            tmpMinMax[0] = minMax[0].getHeadloss();
            tmpMinMax[1] = minMax[1].getHeadloss();
            break;
        case STATUS:
            tmpMinMax[0] = minMax[0].getStatus();
            tmpMinMax[1] = minMax[1].getStatus();
            break;
        default:
            throw new RuntimeException("Undefined variable.");
        }
        return tmpMinMax;
    }

    private static float[] getMinMax4NodeType( ResultsNodeParameters nodeVar, INodeResults[] minMax ) {
        float[] tmpMinMax = new float[2];
        switch( nodeVar ) {
        case DEMAND:
            tmpMinMax[0] = minMax[0].getDemand();
            tmpMinMax[1] = minMax[1].getDemand();
            break;
        case HEAD:
            tmpMinMax[0] = minMax[0].getHead();
            tmpMinMax[1] = minMax[1].getHead();
            break;
        case PRESSURE:
            tmpMinMax[0] = minMax[0].getPressure();
            tmpMinMax[1] = minMax[1].getPressure();
            break;
        default:
            throw new RuntimeException("Undefined variable.");
        }
        return tmpMinMax;
    }

    private static ILinkResults[] getMinMax4Link( Dao<ILinkResults, Long> resultsDao, EpanetRun run, DateTime time,
            String valueField ) throws SQLException {
        ILinkResults[] minMax = new ILinkResults[2];
        QueryBuilder<ILinkResults, Long> qb = resultsDao.queryBuilder();
        Where<ILinkResults, Long> where = qb.where();
        where.eq(IEpanetTableConstants.RUN_ID, run).and().eq(IEpanetTableConstants.UTCTIME, time);
        qb.orderBy(valueField, false); // false for descending order
        qb.limit(1l);
        PreparedQuery<ILinkResults> preparedQuery = qb.prepare();
        List<ILinkResults> maxList = resultsDao.query(preparedQuery);
        if (maxList.size() == 0) {
            return null;
        }
        ILinkResults maxResultsTable = maxList.get(0);

        qb = resultsDao.queryBuilder();
        where = qb.where();
        where.eq(IEpanetTableConstants.RUN_ID, run).and().eq(IEpanetTableConstants.UTCTIME, time);
        qb.orderBy(valueField, true);
        qb.limit(1l);
        preparedQuery = qb.prepare();
        List<ILinkResults> minList = resultsDao.query(preparedQuery);
        if (minList.size() == 0) {
            return null;
        }
        ILinkResults minResultsTable = minList.get(0);

        minMax[0] = minResultsTable;
        minMax[1] = maxResultsTable;
        return minMax;
    }

    private static INodeResults[] getMinMaxObject4Node( Dao<INodeResults, Long> resultsDao, EpanetRun run, DateTime time,
            String valueField ) throws SQLException {
        INodeResults[] minMax = new INodeResults[2];
        QueryBuilder<INodeResults, Long> qb = resultsDao.queryBuilder();
        Where<INodeResults, Long> where = qb.where();
        where.eq(IEpanetTableConstants.RUN_ID, run).and().eq(IEpanetTableConstants.UTCTIME, time);
        qb.orderBy(valueField, false); // false for descending order
        qb.limit(1l);
        PreparedQuery<INodeResults> preparedQuery = qb.prepare();
        List<INodeResults> maxList = resultsDao.query(preparedQuery);
        if (maxList.size() == 0) {
            return null;
        }
        INodeResults maxResultsTable = maxList.get(0);

        qb = resultsDao.queryBuilder();
        where = qb.where();
        where.eq(IEpanetTableConstants.RUN_ID, run).and().eq(IEpanetTableConstants.UTCTIME, time);;
        qb.orderBy(valueField, true);
        qb.limit(1l);
        preparedQuery = qb.prepare();
        List<INodeResults> minList = resultsDao.query(preparedQuery);
        if (minList.size() == 0) {
            return null;
        }
        INodeResults minResultsTable = minList.get(0);

        minMax[0] = minResultsTable;
        minMax[1] = maxResultsTable;
        return minMax;
    }

    /**
     * Get results for links a given timestep.
     * 
     * @param linksResultDao the dao to use.
     * @param run the run to consider.
     * @param time the timestep.
     * @return the list of data for each pipe piece.
     * @throws SQLException
     */
    public static List<ILinkResults> getResults4Links( Dao<ILinkResults, Long> linksResultDao, EpanetRun run, DateTime time )
            throws SQLException {
        QueryBuilder<ILinkResults, Long> qb = linksResultDao.queryBuilder();
        Where<ILinkResults, Long> where = qb.where();
        where.eq(IEpanetTableConstants.RUN_ID, run)//
                .and().eq(IEpanetTableConstants.UTCTIME, time);
        qb.orderBy(IEpanetTableConstants.WORK_ID, true); // false for descending order
        PreparedQuery<ILinkResults> preparedQuery = qb.prepare();
        List<ILinkResults> resultsList = linksResultDao.query(preparedQuery);
        return resultsList;
    }

    public static List<INodeResults> getResults4Nodes( Dao<INodeResults, Long> nodeResultDao, EpanetRun run, DateTime time )
            throws SQLException {
        QueryBuilder<INodeResults, Long> qb = nodeResultDao.queryBuilder();
        Where<INodeResults, Long> where = qb.where();
        where.eq(IEpanetTableConstants.RUN_ID, run)//
                .and().eq(IEpanetTableConstants.UTCTIME, time);
        qb.orderBy(IEpanetTableConstants.WORK_ID, true); // false for descending order
        PreparedQuery<INodeResults> preparedQuery = qb.prepare();
        List<INodeResults> resultsList = nodeResultDao.query(preparedQuery);
        return resultsList;
    }

    public static String[] getTimesList( Dao<INodeResults, Long> junctionsResultDao, EpanetRun run ) throws SQLException {
        if (run == null) {
            return new String[0];
        }

        JunctionsResultsTable queryJrt = new JunctionsResultsTable();
        queryJrt.setRun(run);

        List<INodeResults> jrtList = junctionsResultDao.queryForMatching(queryJrt);

        DateTimeFormatter formatter = OmsEpanet.formatter;
        String[] times = new String[jrtList.size() + 1];
        times[0] = null;
        int i = 1;
        for( INodeResults junctionsResultsTable : jrtList ) {
            DateTime utcTime = junctionsResultsTable.getUtcTime();
            times[i++] = utcTime.toString(formatter);
        }
        return times;
    }

    /**
    * Get the values from a certain work at a given {@link EpanetRun run}.
    *
    * @param session
    * @param run the run to consider.
    * @param work the work to fetch the data from.
    * @param nodeVar the node variable to query. One of linkVar or this has to be != null.
    * @param linkVar the link variable to query. One of nodeVar or this has to be != null.
    * @param clazz the class of results to query.
    * @return the ordered map of date-vale
     * @throws SQLException 
    */
    public static LinkedHashMap<DateTime, float[]> getValuesById( ConnectionSource connectionSource, EpanetRun run, IWork work,
            ResultsNodeParameters nodeVar, ResultsLinkParameters linkVar, Class<IResult> clazz ) throws SQLException {

        Dao<IResult, ? > dao = DaoManager.createDao(connectionSource, clazz);
        QueryBuilder<IResult, ? > qb = dao.queryBuilder();
        Where<IResult, ? > where = qb.where();
        where.eq(IEpanetTableConstants.RUN_ID, run);
        where.and();
        where.eq(IEpanetTableConstants.WORK_ID, work);
        // where.eq("work", work);
        PreparedQuery<IResult> preparedQuery = qb.prepare();

        List<IResult> list = dao.query(preparedQuery);

        LinkedHashMap<DateTime, float[]> resultMap = new LinkedHashMap<DateTime, float[]>();
        if (nodeVar != null) {
            for( IResult iResult : list ) {
                INodeResults result = (INodeResults) iResult;
                DateTime utcTime = result.getUtcTime().withZone(DateTimeZone.UTC);
                float[] value = new float[1];
                switch( nodeVar ) {
                case DEMAND:
                    value[0] = result.getDemand();
                    break;
                case HEAD:
                    value[0] = result.getHead();
                    break;
                case PRESSURE:
                    value[0] = result.getPressure();
                    break;
                default:
                    break;
                }

                resultMap.put(utcTime, value);
            }
        } else if (linkVar != null) {
            for( IResult iResult : list ) {
                ILinkResults result = (ILinkResults) iResult;
                DateTime utcTime = result.getUtcTime();
                float[] value = new float[2];
                switch( linkVar ) {
                case FLOW:
                    value[0] = result.getFlow1();
                    value[1] = result.getFlow2();
                    break;
                case HEADLOSS:
                    value[0] = result.getHeadloss();
                    break;
                case VELOCITY:
                    value[0] = result.getVelocity1();
                    value[1] = result.getVelocity2();
                    break;
                case STATUS:
                    value[0] = result.getStatus();
                    break;
                case ENERGY:
                    value[0] = result.getEnergy();
                    break;

                default:
                    break;
                }
                resultMap.put(utcTime, value);
            }
        }

        return resultMap;
    }
    //
    // public static void makeJunctions( Session session, ColorArrayInterpolator interp, EpanetRun
    // currentSelectedRun,
    // DateTime time, HashMap<String, NodeRenderProperties> nodesRenderingProperties,
    // ResultsNodeParameters currentSelectedNodeVar ) {
    // // get values
    // Criteria c = session.createCriteria(JunctionsResultsTable.class);
    // c.add(Restrictions.eq("run", currentSelectedRun));
    // c.add(Restrictions.eq("utcTime", time));
    // List<JunctionsResultsTable> jList = c.list();
    // for( JunctionsResultsTable jr : jList ) {
    // String id = jr.getWork().getId();
    // NodeRenderProperties nodeRenderProperties = nodesRenderingProperties.get(id);
    // float value = 0;
    // switch( currentSelectedNodeVar ) {
    // case DEMAND:
    // value = jr.getDemand();
    // break;
    // case HEAD:
    // value = jr.getHead();
    // break;
    // case PRESSURE:
    // value = jr.getPressure();
    // break;
    // default:
    // break;
    // }
    // int[] rbg = interp.interpolate(value);
    // nodeRenderProperties.color = new Color(rbg[0], rbg[1], rbg[2]);
    // }
    // }
    //
    // public static void makeTanks( Session session, ColorArrayInterpolator interp, EpanetRun
    // currentSelectedRun, DateTime time,
    // HashMap<String, NodeRenderProperties> nodesRenderingProperties, ResultsNodeParameters
    // currentSelectedNodeVar ) {
    // Criteria c = session.createCriteria(TanksResultsTable.class);
    // c.add(Restrictions.eq("run", currentSelectedRun));
    // c.add(Restrictions.eq("utcTime", time));
    // List<TanksResultsTable> jList = c.list();
    // for( TanksResultsTable tr : jList ) {
    // String id = tr.getWork().getId();
    // NodeRenderProperties nodeRenderProperties = nodesRenderingProperties.get(id);
    // float value = 0;
    // switch( currentSelectedNodeVar ) {
    // case DEMAND:
    // value = tr.getDemand();
    // break;
    // case HEAD:
    // value = tr.getHead();
    // break;
    // case PRESSURE:
    // value = tr.getPressure();
    // break;
    // default:
    // break;
    // }
    // int[] rbg = interp.interpolate(value);
    // nodeRenderProperties.color = new Color(rbg[0], rbg[1], rbg[2]);
    // }
    // }
    // public static void makeReservoirs( Session session, ColorArrayInterpolator interp, EpanetRun
    // currentSelectedRun,
    // DateTime time, HashMap<String, NodeRenderProperties> nodesRenderingProperties,
    // ResultsNodeParameters currentSelectedNodeVar ) {
    // Criteria c = session.createCriteria(ReservoirsResultsTable.class);
    // c.add(Restrictions.eq("run", currentSelectedRun));
    // c.add(Restrictions.eq("utcTime", time));
    // List<ReservoirsResultsTable> jList = c.list();
    // for( ReservoirsResultsTable tr : jList ) {
    // String id = tr.getWork().getId();
    // NodeRenderProperties nodeRenderProperties = nodesRenderingProperties.get(id);
    // float value = 0;
    // switch( currentSelectedNodeVar ) {
    // case DEMAND:
    // value = tr.getDemand();
    // break;
    // case HEAD:
    // value = tr.getHead();
    // break;
    // case PRESSURE:
    // value = -9999f;
    // break;
    // default:
    // break;
    // }
    // int[] rbg = interp.interpolate(value);
    // nodeRenderProperties.color = new Color(rbg[0], rbg[1], rbg[2]);
    // }
    // }
    // public static void makePipes( Session session, ColorArrayInterpolator interp, EpanetRun
    // currentSelectedRun, DateTime time,
    // HashMap<String, LinkRenderProperties> linksRenderingProperties, ResultsLinkParameters
    // currentSelectedLinkVar ) {
    // Criteria c = session.createCriteria(PipesResultsTable.class);
    // c.add(Restrictions.eq("run", currentSelectedRun));
    // c.add(Restrictions.eq("utcTime", time));
    // List<PipesResultsTable> jList = c.list();
    // for( PipesResultsTable pir : jList ) {
    // String id = pir.getWork().getId();
    // LinkRenderProperties linkRenderProperties = linksRenderingProperties.get(id);
    // linkRenderProperties.resetProps();
    // float value = 0;
    // switch( currentSelectedLinkVar ) {
    // case ENERGY:
    // value = -9999;
    // break;
    // case FLOW:
    // float flow1 = pir.getFlow1();
    // float flow2 = pir.getFlow2();
    // int[] flowRgb1 = interp.interpolate(Math.abs(flow1));
    // int[] flowRgb2 = interp.interpolate(Math.abs(flow2));
    // linkRenderProperties.hasArrows = true;
    // Line flowLine = linkRenderProperties.line;
    // flowLine.color1 = new Color(flowRgb1[0], flowRgb1[1], flowRgb1[2]);
    // flowLine.color2 = new Color(flowRgb2[0], flowRgb2[1], flowRgb2[2]);
    // flowLine.firstIsOut = flow1 >= 0;
    // flowLine.secondIsOut = flow2 >= 0;
    // continue;
    // case HEADLOSS:
    // value = pir.getHeadloss();
    // break;
    // case STATUS:
    // value = pir.getStatus();
    // break;
    // case VELOCITY:
    // float vel1 = pir.getVelocity1();
    // float vel2 = pir.getVelocity2();
    // int[] velRgb1 = interp.interpolate(Math.abs(vel1));
    // int[] velRgb2 = interp.interpolate(Math.abs(vel2));
    // Line velLine = linkRenderProperties.line;
    // velLine.color1 = new Color(velRgb1[0], velRgb1[1], velRgb1[2]);
    // velLine.color2 = new Color(velRgb2[0], velRgb2[1], velRgb2[2]);
    // continue;
    // default:
    // break;
    // }
    // int[] rbg = interp.interpolate(value);
    // Line line = linkRenderProperties.line;
    // line.color1 = new Color(rbg[0], rbg[1], rbg[2]);
    // }
    //
    // }
    // public static void makePumps( Session session, ColorArrayInterpolator interp, EpanetRun
    // currentSelectedRun, DateTime time,
    // HashMap<String, LinkRenderProperties> linksRenderingProperties, ResultsLinkParameters
    // currentSelectedLinkVar ) {
    // Criteria c = session.createCriteria(PumpsResultsTable.class);
    // c.add(Restrictions.eq("run", currentSelectedRun));
    // c.add(Restrictions.eq("utcTime", time));
    // List<PumpsResultsTable> jList = c.list();
    // for( PumpsResultsTable pur : jList ) {
    // String id = pur.getWork().getId();
    // LinkRenderProperties linkRenderProperties = linksRenderingProperties.get(id);
    // linkRenderProperties.resetProps();
    // float value = 0;
    // switch( currentSelectedLinkVar ) {
    // case ENERGY:
    // value = pur.getEnergy();
    // break;
    // case FLOW:
    // float flow1 = pur.getFlow1();
    // float flow2 = pur.getFlow2();
    // int[] flowRgb1 = interp.interpolate(Math.abs(flow1));
    // int[] flowRgb2 = interp.interpolate(Math.abs(flow2));
    // linkRenderProperties.hasArrows = true;
    // Line flowLine = linkRenderProperties.line;
    // flowLine.color1 = new Color(flowRgb1[0], flowRgb1[1], flowRgb1[2]);
    // flowLine.color2 = new Color(flowRgb2[0], flowRgb2[1], flowRgb2[2]);
    // flowLine.firstIsOut = flow1 >= 0;
    // flowLine.secondIsOut = flow2 >= 0;
    // continue;
    // case HEADLOSS:
    // value = pur.getHeadloss();
    // break;
    // case STATUS:
    // value = pur.getStatus();
    // break;
    // case VELOCITY:
    // float vel1 = pur.getVelocity1();
    // float vel2 = pur.getVelocity2();
    // int[] velRgb1 = interp.interpolate(Math.abs(vel1));
    // int[] velRgb2 = interp.interpolate(Math.abs(vel2));
    // Line velLine = linkRenderProperties.line;
    // velLine.color1 = new Color(velRgb1[0], velRgb1[1], velRgb1[2]);
    // velLine.color2 = new Color(velRgb2[0], velRgb2[1], velRgb2[2]);
    // continue;
    // default:
    // break;
    // }
    // int[] rbg = interp.interpolate(value);
    // Line line = linkRenderProperties.line;
    // line.color1 = new Color(rbg[0], rbg[1], rbg[2]);
    // Point point = linkRenderProperties.point;
    // point.color = new Color(rbg[0], rbg[1], rbg[2]);
    // }
    //
    // }
    // public static void makeValves( Session session, ColorArrayInterpolator interp, EpanetRun
    // currentSelectedRun, DateTime time,
    // HashMap<String, LinkRenderProperties> linksRenderingProperties, ResultsLinkParameters
    // currentSelectedLinkVar ) {
    // Criteria c = session.createCriteria(ValvesResultsTable.class);
    // c.add(Restrictions.eq("run", currentSelectedRun));
    // c.add(Restrictions.eq("utcTime", time));
    // List<ValvesResultsTable> jList = c.list();
    // for( ValvesResultsTable vr : jList ) {
    // String id = vr.getWork().getId();
    // LinkRenderProperties linkRenderProperties = linksRenderingProperties.get(id);
    // linkRenderProperties.resetProps();
    // float value = 0;
    // switch( currentSelectedLinkVar ) {
    // case ENERGY:
    // value = -9999;
    // break;
    // case FLOW:
    // float flow1 = vr.getFlow1();
    // float flow2 = vr.getFlow2();
    // int[] flowRgb1 = interp.interpolate(Math.abs(flow1));
    // int[] flowRgb2 = interp.interpolate(Math.abs(flow2));
    // linkRenderProperties.hasArrows = true;
    // Line flowLine = linkRenderProperties.line;
    // flowLine.color1 = new Color(flowRgb1[0], flowRgb1[1], flowRgb1[2]);
    // flowLine.color2 = new Color(flowRgb2[0], flowRgb2[1], flowRgb2[2]);
    // flowLine.firstIsOut = flow1 >= 0;
    // flowLine.secondIsOut = flow2 >= 0;
    // continue;
    // case HEADLOSS:
    // value = vr.getHeadloss();
    // break;
    // case STATUS:
    // value = vr.getStatus();
    // break;
    // case VELOCITY:
    // float vel1 = vr.getVelocity1();
    // float vel2 = vr.getVelocity2();
    // int[] velRgb1 = interp.interpolate(Math.abs(vel1));
    // int[] velRgb2 = interp.interpolate(Math.abs(vel2));
    // Line velLine = linkRenderProperties.line;
    // velLine.color1 = new Color(velRgb1[0], velRgb1[1], velRgb1[2]);
    // velLine.color2 = new Color(velRgb2[0], velRgb2[1], velRgb2[2]);
    // continue;
    // default:
    // break;
    // }
    // int[] rbg = interp.interpolate(value);
    // Line line = linkRenderProperties.line;
    // line.color1 = new Color(rbg[0], rbg[1], rbg[2]);
    // Point point = linkRenderProperties.point;
    // point.color = new Color(rbg[0], rbg[1], rbg[2]);
    // }
    // }
    //
    // public static void addServiceToCatalogAndMap( String outputFile, boolean addToCatalog,
    // boolean addToActiveMap,
    // IProgressMonitor progressMonitor ) {
    // try {
    // URL fileUrl = new File(outputFile).toURI().toURL();
    // if (addToCatalog) {
    // IServiceFactory sFactory = CatalogPlugin.getDefault().getServiceFactory();
    // ICatalog catalog = CatalogPlugin.getDefault().getLocalCatalog();
    // List<IService> services = sFactory.createService(fileUrl);
    // for( IService service : services ) {
    // catalog.add(service);
    // if (addToActiveMap) {
    // IMap activeMap = ApplicationGIS.getActiveMap();
    // int layerNum = activeMap.getMapLayers().size();
    // List<IResolve> members = service.members(progressMonitor);
    // for( IResolve iRes : members ) {
    // if (iRes.canResolve(IGeoResource.class)) {
    // IGeoResource geoResource = iRes.resolve(IGeoResource.class, progressMonitor);
    // ApplicationGIS.addLayersToMap(null, Collections.singletonList(geoResource), layerNum);
    // }
    // }
    // }
    // }
    // }
    // } catch (Exception e) {
    // String message = "An error occurred while adding the service to the catalog.";
    // ExceptionDetailsDialog.openError(null, message, IStatus.ERROR, EpanetPlugin.PLUGIN_ID, e);
    // e.printStackTrace();
    // }
    // }
    //
    public static void chartNode( ConnectionSource connectionSource, EpanetRun run, String currentSelectedNodeId,
            ResultsNodeParameters currentSelectedNodeVar ) throws ClassNotFoundException, SQLException {
        String title = "Results for node " + currentSelectedNodeId + " in run " + run.getId();
        String xLabel = "time";
        String yLabel = "";

        switch( currentSelectedNodeVar ) {
        case DEMAND:
            yLabel = ResultsNodeParameters.DEMAND.getKey();
            break;
        case HEAD:
            yLabel = ResultsNodeParameters.HEAD.getKey();
            break;
        case PRESSURE:
            yLabel = ResultsNodeParameters.PRESSURE.getKey();
            break;
        default:
            break;
        }

        IWork work = getWorkById(connectionSource, run, currentSelectedNodeId);
        String workName = work.getClass().getCanonicalName();
        workName = workName.replace("Table", "ResultsTable");
        Class<IResult> clazz = (Class<IResult>) Class.forName(workName);

        LinkedHashMap<DateTime, float[]> valuesMap = getValuesById(connectionSource, run, work, currentSelectedNodeVar, null,
                clazz);
        List<LinkedHashMap<DateTime, float[]>> list = new ArrayList<LinkedHashMap<DateTime, float[]>>();
        list.add(valuesMap);
        ChartHelper.chart(list, title, xLabel, yLabel);
    }

    public static void chartLink( ConnectionSource connectionSource, EpanetRun run, String currentSelectedLinkId,
            ResultsLinkParameters currentSelectedLinkVar ) throws ClassNotFoundException, SQLException {
        String title = "Results for link " + currentSelectedLinkId + " in run " + run.getId();
        String xLabel = "time";
        String yLabel = "";

        switch( currentSelectedLinkVar ) {
        case FLOW:
            yLabel = ResultsLinkParameters.FLOW.getKey();
            break;
        case HEADLOSS:
            yLabel = ResultsLinkParameters.HEADLOSS.getKey();
            break;
        case VELOCITY:
            yLabel = ResultsLinkParameters.VELOCITY.getKey();
            break;
        case ENERGY:
            yLabel = ResultsLinkParameters.ENERGY.getKey();
            break;
        case STATUS:
            yLabel = ResultsLinkParameters.STATUS.getKey();
            break;
        default:
            break;
        }

        IWork work = getWorkById(connectionSource, run, currentSelectedLinkId);
        String workName = work.getClass().getCanonicalName();
        workName = workName.replace("Table", "ResultsTable");
        Class<IResult> clazz = (Class<IResult>) Class.forName(workName);

        LinkedHashMap<DateTime, float[]> valuesMap = EpanetUtilities.getValuesById(connectionSource, run, work, null,
                currentSelectedLinkVar, clazz);
        List<LinkedHashMap<DateTime, float[]>> list = new ArrayList<LinkedHashMap<DateTime, float[]>>();
        list.add(valuesMap);
        ChartHelper.chart(list, title, xLabel, yLabel);
    }

    /**
    * Gets the {@link IWork} from the database by its id.
    *
    * @param session
    * @param run
    * @param id
    * @return
     * @throws SQLException 
    */
    public static IWork getWorkById( ConnectionSource connectionSource, EpanetRun run, String id ) throws SQLException {
        Class[] classes = {JunctionsTable.class, TanksTable.class, ReservoirsTable.class, PipesTable.class, PumpsTable.class,
                ValvesTable.class};

        for( Class clazz : classes ) {
            Dao dao = DaoManager.createDao(connectionSource, clazz);

            QueryBuilder<IWork, Long> qb = dao.queryBuilder();
            Where<IWork, Long> where = qb.where();
            where.eq(IEpanetTableConstants.RUN_ID, run);
            where.and();
            where.eq(IEpanetTableConstants.ID, id);
            PreparedQuery<IWork> preparedQuery = qb.prepare();

            List result = dao.query(preparedQuery);
            if (result != null && result.size() > 0) {
                return (IWork) result.get(0);
            }
        }
        throw new IllegalArgumentException();
    }

}
