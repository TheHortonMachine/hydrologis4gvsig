/*
 * This file is part of JGrasstools (http://www.jgrasstools.org)
 * (C) HydroloGIS - www.hydrologis.com 
 * 
 * JGrasstools is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.jgrasstools.gvsig.base;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;

import org.jgrasstools.gears.utils.colors.EColorTables;

public class DefaultGvsigTables {
    private static HashMap<String, String> map = new HashMap<String, String>();

    public DefaultGvsigTables() {
        String rainbow = "255 255 0\n" + //
                "0 255 0\n" + //
                "0 255 255\n" + //
                "0 0 255\n" + //
                "255 0 255\n" + //
                "255 0 0\n";

        map.put(EColorTables.rainbow.name(), rainbow);

        String extRainbow = "255 255 0\n" + //
                "128 255 0\n" + //
                "0 255 0\n" + //
                "0 255 128\n" + //
                "0 255 255\n" + //
                "0 128 255\n" + //
                "0 0 255\n" + //
                "128 0 255\n" + //
                "255 0 255\n" + //
                "255 0 128\n" + //
                "255 0 0\n";

        map.put(EColorTables.extrainbow.name(), extRainbow);

        String aspect = "255 255 255\n" + //
                "0 0 0\n" + //
                "255 255 255";
        map.put(EColorTables.aspect.name(), aspect);

        String bath = "-30000    0   0   0\n" + //
                "-20000    0   0   0\n" + //
                "-10000    0   0   59 \n" + //
                "-9000     0   0   130\n" + //
                "-8000     0   0   202\n" + //
                "-7000     0   18  255\n" + //
                "-6000     0   90  255\n" + //
                "-5000     0   157 255\n" + //
                "-4000     0   227 255\n" + //
                "-3000     43  255 255\n" + //
                "-2000     115 255 255\n" + //
                "-1000     184 255 255\n" + //
                "0         0   128 0  \n" + //
                "500       133 5   0  \n" + //
                "1000      255 128 0  \n" + //
                "2000      255 255 0  \n" + //
                "3000      255 255 127\n" + //
                "4000      255 255 255\n" + //
                "10000     255 255 255";
        map.put(EColorTables.bathymetric.name(), bath);

        String elev = "0 191 191 \n" + //
                "0 255 0 \n" + //
                "255 255 0 \n" + //
                "255 127 0\n" + //
                "191 127 63 \n" + //
                "20 21 20\n";
        map.put(EColorTables.elev.name(), elev);

        String flow = "1:E 255 255 0\n" + //
                "2:NE 0 255 0\n" + //
                "3:N 0 255 255\n" + //
                "4:NW 255 0 255\n" + //
                "5:W 0 0 255\n" + //
                "6:SW 160 32 240\n" + //
                "7:S 255 165 0\n" + //
                "8:SE 30 144 255\n" + //
                "10:outlet 255 0 0\n";
        map.put(EColorTables.flow.name(), flow);

        String loga = "-1.0 255 255 255\n" + //
                "0.0 255 255 0\n" + //
                "1 0 255 0\n" + //
                "10 0 255 255\n" + //
                "100 0 0 255\n" + //
                "1000 255 0 255\n" + //
                "10000 255 0 0\n" + //
                "100000 110 0 0\n" + //
                "1000000 0 0 0\n";
        map.put(EColorTables.logarithmic.name(), loga);

        String tca = "1 255 255 255\n" + //
                "10 0 255 0\n" + //
                "100 0 255 255\n" + //
                "1000 0 0 255\n" + //
                "10000 255 0 255\n" + //
                "100000 255 0 0\n" + //
                "1000000 110 0 0\n" + //
                "10000000 0 0 0\n";
        map.put(EColorTables.tca.name(), tca);

        String sea = "-30000.0 255 255 255\n" + //
                "-8000.0 0 0 255\n" + //
                "-2500.0 30 144 255\n" + //
                "-2000 162 208 252\n" + //
                "-1500 250 117 117\n" + //
                "0.0 255 0 0\n";
        map.put(EColorTables.sea.name(), sea);

        String radiation = "198 198 224\n" + //
                "0 0 115\n" + //
                "0 100 210\n" + //
                "90 183 219\n" + //
                "0 255 255\n" + //
                "40 254 100\n" + //
                "80 131 35\n" + //
                "160 190 0\n" + //
                "255 255 100\n" + //
                "255 180 0\n" + //
                "255 0 0\n";

        map.put(EColorTables.radiation.name(), radiation);

        String net = "2 255 0 0";
        map.put(EColorTables.net.name(), net);

        String greyscale = "0 0 0 \n" + //
                "255 255 255\n";
        map.put(EColorTables.greyscale.name(), greyscale);

        String greyscaleInverse = "255 255 255\n" + //
                "0 0 0\n";
        map.put(EColorTables.greyscaleinverse.name(), greyscaleInverse);

        // 1 : unconditionally unstable
        // 2 : unconditionally stable
        // 3 : stable
        // 4 : unstable
        // 8888 : pixel characterized by rock (if soil thickness < 0.01)
        String shalstab = "1.0:unconditionally_unstable 255 0 0\n" + //
                "2.0:unconditionally_stable 0 255 0\n" + //
                "3.0:stable 255 255 0\n" + //
                "4.0:unstable 0 0 255\n" + //
                "8888.0:rock 77 77 77\n";
        map.put(EColorTables.shalstab.name(), shalstab);

        String slope = " -5.0 255 0 0\n" + //
                " -2.0 255 0 128\n" + //
                " -1.0 255 0 255\n" + //
                " -0.7 128 0 255\n" + //
                " -0.5 0 0 255  \n" + //
                " -0.3 0 128 255\n" + //
                " -0.1 0 255 255\n" + //
                "-0.07 0 255 128\n" + //
                "-0.03 0 255 0\n" + //
                "-0.01 128 255 0\n" + //
                " 0 255 255 0\n" + //
        // invert
        "0 255 255 0\n" + //
                "0.01 128 255 0\n" + //
                "0.03 0 255 0\n" + //
                "0.07 0 255 128\n" + //
                "0.1 0 255 255\n" + //
                "0.3 0 128 255\n" + //
                "0.5 0 0 255\n" + //
                "0.7 128 0 255\n" + //
                "1.0 255 0 255\n" + //
                "2.0 255 0 128\n" + //
                "5.0 255 0 0";

        map.put(EColorTables.slope.name(), slope);

        String geomorphon = //
        "1000.0:flat 127 127 127\n" + //
                "1001.0:peak 108 0 0\n" + //
                "1002.0:ridge 255 0 0\n" + //
                "1003.0:shoulder 255 165 0\n" + //
                "1004.0:spur 255 219 61\n" + //
                "1005.0:slope 255 255 0\n" + //
                "1006.0:hollow 143 203 44\n" + //
                "1007.0:footslope 50 189 160\n" + //
                "1008.0:valley 0 0 255\n" + //
                "1009.0:pit 0 0 0\n";
        map.put(EColorTables.geomorphon.name(), geomorphon);
    }

    /**
     * Method to add a table at runtime to the tables available.
     *
     * @param name the name of the table.
     * @param palette the colors and values.
     */
    public static void addRuntimeTable( String name, String palette ) {
        map.put(name, palette);
    }

    public static String[] getTableNames() {
        Set<String> keySet = map.keySet();
        String[] array = keySet.toArray(new String[0]);
        Arrays.sort(array);
        return array;
    }

    public String getTableString( String name ) {
        return map.get(name);
    }
}
