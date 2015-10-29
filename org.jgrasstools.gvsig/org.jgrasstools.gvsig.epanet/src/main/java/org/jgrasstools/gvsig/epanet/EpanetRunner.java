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
package org.jgrasstools.gvsig.epanet;

import static org.jgrasstools.gears.utils.time.UtcTimeUtilities.fromStringWithSeconds;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.hibernate.classic.Session;
import org.jgrasstools.hortonmachine.modules.networktools.epanet.Epanet;
import org.jgrasstools.hortonmachine.modules.networktools.epanet.core.types.Junction;
import org.jgrasstools.hortonmachine.modules.networktools.epanet.core.types.Pipe;
import org.jgrasstools.hortonmachine.modules.networktools.epanet.core.types.Pump;
import org.jgrasstools.hortonmachine.modules.networktools.epanet.core.types.Reservoir;
import org.jgrasstools.hortonmachine.modules.networktools.epanet.core.types.Tank;
import org.jgrasstools.hortonmachine.modules.networktools.epanet.core.types.Valve;
import org.joda.time.DateTime;

import eu.hydrologis.jgrass.epanet.EpanetPlugin;
import eu.hydrologis.jgrass.epanet.actions.EclipseProgressMonitorAdapter;
import eu.hydrologis.jgrass.epanet.annotatedclasses.EpanetRun;
import eu.hydrologis.jgrass.epanet.annotatedclasses.junctions.JunctionsResultsTable;
import eu.hydrologis.jgrass.epanet.annotatedclasses.junctions.JunctionsTable;
import eu.hydrologis.jgrass.epanet.annotatedclasses.pipes.PipesResultsTable;
import eu.hydrologis.jgrass.epanet.annotatedclasses.pipes.PipesTable;
import eu.hydrologis.jgrass.epanet.annotatedclasses.pumps.PumpsResultsTable;
import eu.hydrologis.jgrass.epanet.annotatedclasses.pumps.PumpsTable;
import eu.hydrologis.jgrass.epanet.annotatedclasses.reservoirs.ReservoirsResultsTable;
import eu.hydrologis.jgrass.epanet.annotatedclasses.reservoirs.ReservoirsTable;
import eu.hydrologis.jgrass.epanet.annotatedclasses.tanks.TanksResultsTable;
import eu.hydrologis.jgrass.epanet.annotatedclasses.tanks.TanksTable;
import eu.hydrologis.jgrass.epanet.annotatedclasses.valves.ValvesResultsTable;
import eu.hydrologis.jgrass.epanet.annotatedclasses.valves.ValvesTable;

/**
 * Runner helper for the epanet model.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class EpanetRunner {
    private final String inpFilePath;
    private String dllPath;

    private StringBuilder warningsBuilder = null;

    public EpanetRunner( String inpFilePath ) {
        this.inpFilePath = inpFilePath;

        warningsBuilder = new StringBuilder();

        String dllName = "epanet2.dll";
        String os = Platform.getOS();
        String arch = Platform.getOSArch();
        if (os.equals(Platform.OS_WIN32) && arch.equals(Platform.ARCH_X86_64)) {
            dllName = "epanet2_64bit.dll";
        } else if (os.equals(Platform.OS_WIN32) && arch.equals(Platform.ARCH_X86)) {
            dllName = "epanet2.dll";
        } else if (os.equals(Platform.OS_LINUX) && arch.equals(Platform.ARCH_X86_64)) {
            dllName = "epanet2_64bit.so";
        } else {
            throw new RuntimeException("Os and architecture are not supported yet.");
        }

        try {
            URL dllUrl = Platform.getBundle(EpanetPlugin.PLUGIN_ID).getResource("nativelibs/" + dllName);
            dllPath = FileLocator.toFileURL(dllUrl).getPath();
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("USING EPANET LIB: " + dllPath);
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

    public void run( String tStart, double hydraulicTimestep, EclipseProgressMonitorAdapter pm, Session session, EpanetRun run,
            HashMap<String, JunctionsTable> jId2Table, HashMap<String, PipesTable> piId2Table,
            HashMap<String, PumpsTable> puId2Table, HashMap<String, ValvesTable> vId2Table,
            HashMap<String, TanksTable> tId2Table, HashMap<String, ReservoirsTable> rId2Table ) throws Exception {

        Epanet epanet = new Epanet();
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
                String id = junction.id;
                JunctionsResultsTable jrt = new JunctionsResultsTable();
                jrt.setWork(jId2Table.get(id));
                jrt.setRun(run);
                jrt.setUtcTime(dt);
                jrt.setDemand(junction.demand);
                jrt.setHead(junction.head);
                jrt.setPressure(junction.pressure);
                jrt.setQuality(junction.quality);
                session.save(jrt);
            }
            List<Reservoir> reservoirsList = epanet.reservoirsList;
            for( Reservoir reservoir : reservoirsList ) {
                String id = reservoir.id;
                ReservoirsResultsTable rrt = new ReservoirsResultsTable();
                rrt.setWork(rId2Table.get(id));
                rrt.setRun(run);
                rrt.setUtcTime(dt);
                rrt.setDemand(reservoir.demand);
                rrt.setHead(reservoir.head);
                rrt.setQuality(reservoir.quality);
                session.save(rrt);
            }
            List<Tank> tankList = epanet.tanksList;
            for( Tank tank : tankList ) {
                String id = tank.id;
                TanksResultsTable trt = new TanksResultsTable();
                trt.setWork(tId2Table.get(id));
                trt.setRun(run);
                trt.setUtcTime(dt);
                trt.setDemand(tank.demand);
                trt.setHead(tank.head);
                trt.setPressure(tank.pressure);
                trt.setQuality(tank.quality);
                session.save(trt);
            }
            List<Pipe> pipesList = epanet.pipesList;
            for( Pipe pipe : pipesList ) {
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
                session.save(pirt);
            }
            List<Pump> pumpsList = epanet.pumpsList;
            for( Pump pump : pumpsList ) {
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
                session.save(purt);
            }
            List<Valve> valvesList = epanet.valvesList;
            for( Valve valve : valvesList ) {
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
                session.save(purt);
            }
        }
    }
}
