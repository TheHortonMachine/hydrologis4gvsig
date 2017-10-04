/*
 * This file is part of HortonMachine (http://www.hortonmachine.org)
 * (C) HydroloGIS - www.hydrologis.com 
 * 
 * The HortonMachine is free software: you can redistribute it and/or modify
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
package org.hortonmachine.gvsig.epanet.core.style;


/**
 * A class for doing linear interpolations on color triplets.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class ColorArrayInterpolator {

    private int[][] yList;
    private double[] xList;

    public ColorArrayInterpolator( double[] xList, int[][] yList ) {
        this.xList = xList;
        this.yList = yList;
    }

    public int[] interpolate( double x ) {

        double first = xList[0];
        double last = xList[xList.length - 1];

        // check out of range
        if (x < first) {
            return yList[0];
        }
        if (x > last) {
            return yList[yList.length - 1];
        }

        for( int i = 0; i < xList.length; i++ ) {
            double x2 = xList[i];
            if (x2 == x) {
                return yList[i];
            } else if (x2 > x) {
                double x1 = xList[i - 1];
                int[] y1 = yList[i - 1];
                int[] y2 = yList[i];

                int[] y = new int[3];
                y[0] = (int) ((double) (y2[0] - y1[0]) * (x - x1) / (x2 - x1) + (double) y1[0]);
                y[1] = (int) ((double) (y2[1] - y1[1]) * (x - x1) / (x2 - x1) + (double) y1[1]);
                y[2] = (int) ((double) (y2[2] - y1[2]) * (x - x1) / (x2 - x1) + (double) y1[2]);
                return y;
            }
        }
        return new int[]{0, 0, 0};
    }
}
