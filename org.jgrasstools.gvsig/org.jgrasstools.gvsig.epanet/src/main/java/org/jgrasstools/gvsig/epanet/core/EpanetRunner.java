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
package org.jgrasstools.gvsig.epanet.core;

import static org.jgrasstools.gears.utils.time.UtcTimeUtilities.fromStringWithSeconds;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.List;

import org.jgrasstools.gears.libs.monitor.IJGTProgressMonitor;
import org.jgrasstools.gvsig.epanet.database.EpanetRun;
import org.jgrasstools.gvsig.epanet.database.JunctionsResultsTable;
import org.jgrasstools.gvsig.epanet.database.JunctionsTable;
import org.jgrasstools.gvsig.epanet.database.PipesResultsTable;
import org.jgrasstools.gvsig.epanet.database.PipesTable;
import org.jgrasstools.gvsig.epanet.database.PumpsResultsTable;
import org.jgrasstools.gvsig.epanet.database.PumpsTable;
import org.jgrasstools.gvsig.epanet.database.ReservoirsResultsTable;
import org.jgrasstools.gvsig.epanet.database.ReservoirsTable;
import org.jgrasstools.gvsig.epanet.database.TanksResultsTable;
import org.jgrasstools.gvsig.epanet.database.TanksTable;
import org.jgrasstools.gvsig.epanet.database.ValvesResultsTable;
import org.jgrasstools.gvsig.epanet.database.ValvesTable;
import org.jgrasstools.hortonmachine.modules.networktools.epanet.OmsEpanet;
import org.jgrasstools.hortonmachine.modules.networktools.epanet.core.types.Junction;
import org.jgrasstools.hortonmachine.modules.networktools.epanet.core.types.Pipe;
import org.jgrasstools.hortonmachine.modules.networktools.epanet.core.types.Pump;
import org.jgrasstools.hortonmachine.modules.networktools.epanet.core.types.Reservoir;
import org.jgrasstools.hortonmachine.modules.networktools.epanet.core.types.Tank;
import org.jgrasstools.hortonmachine.modules.networktools.epanet.core.types.Valve;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.support.ConnectionSource;
import com.sun.jna.Platform;

/**
 * Runner helper for the epanet model.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class EpanetRunner {
    private final String inpFilePath;
    private String dllPath;
    private static final Logger logger = LoggerFactory.getLogger(EpanetRunner.class);
    private StringBuilder warningsBuilder = null;

    public EpanetRunner( String inpFilePath ) {
        this.inpFilePath = inpFilePath;

        warningsBuilder = new StringBuilder();

        File dllFile;
        String dllName = "epanet2.dll";
        if (Platform.isWindows() && Platform.is64Bit()) {
            dllName = "epanet2_64bit.dll";
            dllFile = getResource("native" + File.separator + dllName);
        } else if (Platform.isWindows()) {
            dllName = "epanet2.dll";
            dllFile = getResource("native" + File.separator + dllName);
        } else if (Platform.isLinux() && Platform.is64Bit()) {
            dllName = "libepanet2_64bit.so";
            dllFile = getResource("native" + File.separator + dllName);
            dllFile = new File(dllFile.getParentFile(), "epanet2_64bit.so");
        } else {
            throw new RuntimeException("Os and architecture are not supported yet.");
        }

        dllPath = dllFile.getAbsolutePath();

        logger.info("USING EPANET LIB: " + dllPath);
    }

    private File getResource( String pathname ) {
        URL res = this.getClass().getClassLoader().getResource(pathname);
        return new File(res.getPath());
    }

    public String getWarnings() {
        if (warningsBuilder != null) {
            String warnings = warningsBuilder.toString();
            if (warnings.length() > 0) {
                return warnings;
            }
        }
        return null;
    }

    public void run( String tStart, double hydraulicTimestep, IJGTProgressMonitor pm, EpanetRun run,
            // table maps
            HashMap<String, JunctionsTable> jId2Table, //
            HashMap<String, PipesTable> piId2Table, //
            HashMap<String, PumpsTable> puId2Table, //
            HashMap<String, ValvesTable> vId2Table, //
            HashMap<String, TanksTable> tId2Table, //
            HashMap<String, ReservoirsTable> rId2Table, //
            ConnectionSource connectionSource //
    ) throws Exception {

        Dao<JunctionsResultsTable, Long> junctionsResultsDao = DaoManager.createDao(connectionSource,
                JunctionsResultsTable.class);
        Dao<PipesResultsTable, Long> pipesResultsDao = DaoManager.createDao(connectionSource, PipesResultsTable.class);
        Dao<PumpsResultsTable, Long> pumpsResultsDao = DaoManager.createDao(connectionSource, PumpsResultsTable.class);
        Dao<ValvesResultsTable, Long> valvesResultsDao = DaoManager.createDao(connectionSource, ValvesResultsTable.class);
        Dao<TanksResultsTable, Long> tanksResultsDao = DaoManager.createDao(connectionSource, TanksResultsTable.class);
        Dao<ReservoirsResultsTable, Long> reservoirsResultsDao = DaoManager.createDao(connectionSource,
                ReservoirsResultsTable.class);

        OmsEpanet epanet = new OmsEpanet();
        DateTime startDate = fromStringWithSeconds("1970-01-01 00:00:00");
        try {
            startDate = fromStringWithSeconds(tStart);
            epanet.tStart = tStart;
        } catch (Exception e) {
            // ignore if wrong, will start at 1970-01-01
        }
        epanet.pm = pm;
        epanet.inDll = dllPath;
        epanet.inInp = inpFilePath;

        DateTime runningDate = startDate;
        epanet.initProcess();
        while( epanet.doProcess ) {
            epanet.process();
            String tCurrent = epanet.tCurrent;

            String warnings = epanet.warnings;
            if (warnings != null) {
                warningsBuilder.append(tCurrent).append(":").append(warnings).append("\n");
            }

            DateTime dt = fromStringWithSeconds(tCurrent);
            boolean isEqualDate = dt.equals(runningDate);
            if (!isEqualDate) {
                // jump over intermediate timesteps
                continue;
            }
            runningDate = runningDate.plusMinutes((int) hydraulicTimestep);

            /*
             * insert records in the database
             */
            List<Junction> junctionsList = epanet.junctionsList;
            for( Junction junction : junctionsList ) {
                epanet.checkCancel();
                String id = junction.id;
                JunctionsResultsTable jrt = new JunctionsResultsTable();
                jrt.setWork(jId2Table.get(id));
                jrt.setRun(run);
                jrt.setUtcTime(dt);
                jrt.setDemand(junction.demand);
                jrt.setHead(junction.head);
                jrt.setPressure(junction.pressure);
                jrt.setQuality(junction.quality);
                junctionsResultsDao.create(jrt);
            }
            List<Reservoir> reservoirsList = epanet.reservoirsList;
            for( Reservoir reservoir : reservoirsList ) {
                epanet.checkCancel();
                String id = reservoir.id;
                ReservoirsResultsTable rrt = new ReservoirsResultsTable();
                rrt.setWork(rId2Table.get(id));
                rrt.setRun(run);
                rrt.setUtcTime(dt);
                rrt.setDemand(reservoir.demand);
                rrt.setHead(reservoir.head);
                rrt.setQuality(reservoir.quality);
                reservoirsResultsDao.create(rrt);
            }
            List<Tank> tankList = epanet.tanksList;
            for( Tank tank : tankList ) {
                epanet.checkCancel();
                String id = tank.id;
                TanksResultsTable trt = new TanksResultsTable();
                trt.setWork(tId2Table.get(id));
                trt.setRun(run);
                trt.setUtcTime(dt);
                trt.setDemand(tank.demand);
                trt.setHead(tank.head);
                trt.setPressure(tank.pressure);
                trt.setQuality(tank.quality);
                tanksResultsDao.create(trt);
            }
            List<Pipe> pipesList = epanet.pipesList;
            for( Pipe pipe : pipesList ) {
                epanet.checkCancel();
                String id = pipe.id;
                PipesResultsTable pirt = new PipesResultsTable();
                pirt.setWork(piId2Table.get(id));
                pirt.setRun(run);
                pirt.setUtcTime(dt);
                pirt.setFlow1(pipe.flow[0]);
                pirt.setFlow2(pipe.flow[1]);
                pirt.setHeadloss(pipe.headloss);
                pirt.setVelocity1(pipe.velocity[0]);
                pirt.setVelocity2(pipe.velocity[1]);
                pirt.setStatus(pipe.status);
                pipesResultsDao.create(pirt);
            }
            List<Pump> pumpsList = epanet.pumpsList;
            for( Pump pump : pumpsList ) {
                epanet.checkCancel();
                String id = pump.id;
                PumpsResultsTable purt = new PumpsResultsTable();
                purt.setWork(puId2Table.get(id));
                purt.setRun(run);
                purt.setUtcTime(dt);
                purt.setFlow1(pump.flow);
                purt.setFlow2(pump.flow);
                purt.setHeadloss(pump.headloss);
                purt.setVelocity1(pump.velocity);
                purt.setVelocity2(pump.velocity);
                purt.setStatus(pump.status);
                pumpsResultsDao.create(purt);
            }
            List<Valve> valvesList = epanet.valvesList;
            for( Valve valve : valvesList ) {
                epanet.checkCancel();
                String id = valve.id;
                ValvesResultsTable purt = new ValvesResultsTable();
                purt.setWork(vId2Table.get(id));
                purt.setRun(run);
                purt.setUtcTime(dt);
                purt.setFlow1(valve.flow);
                purt.setFlow2(valve.flow);
                purt.setHeadloss(valve.headloss);
                purt.setVelocity1(valve.velocity);
                purt.setVelocity2(valve.velocity);
                purt.setStatus(valve.status);
                valvesResultsDao.create(purt);
            }
        }
    }
}
