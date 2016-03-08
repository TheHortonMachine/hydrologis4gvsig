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

import org.gvsig.fmap.dal.coverage.RasterLocator;
import org.gvsig.fmap.dal.coverage.RasterManager;
import org.gvsig.fmap.dal.coverage.dataset.Buffer;
import org.gvsig.fmap.dal.coverage.datastruct.DataStructFactory;
import org.gvsig.fmap.dal.coverage.datastruct.NoData;
import org.gvsig.fmap.dal.coverage.store.RasterDataStore;
import org.gvsig.fmap.dal.coverage.store.RasterQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Raster helper methods.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class RasterUtilities {
    private static final Logger logger = LoggerFactory.getLogger(RasterUtilities.class);

    private static RasterManager rasterManager = RasterLocator.getManager();

    /**
     * Get the raster limits as [min, max].
     * 
     * @param dataStore
     * @param novalue
     * @return
     * @throws Exception
     */
    public static double[] getDoubleRasterLimits( RasterDataStore dataStore, Double novalue ) throws Exception {
        DataStructFactory dataStructFactory = rasterManager.getDataStructFactory();

        RasterQuery query = rasterManager.createQuery();
        query.setSupersamplingOption(false);
        query.setDrawableBands(new int[]{0});
        query.setReadOnly(true);
        if (novalue != null) {
            // NoData defaultNoData = dataStructFactory.createDefaultNoData(1, Buffer.TYPE_DOUBLE);
            // defaultNoData.setValue(novalue);
            //
            NoData noData = dataStructFactory.createNoData(novalue, novalue, dataStore.getName());
            query.setNoDataToFill(noData);
        }
        // query.setAreaOfInterest(clipRect);
        query.storeLastBuffer(true);
        // query.setAreaOfInterest(canvasExtent, canvas.getWidth(),
        Buffer buffer = dataStore.query(query);
        double[] limits = buffer.getLimits();
        logger.info("Limits = " + Arrays.toString(limits));
        return limits;
    }

    // int bandCount = buffer.getBandCount();
    // final double[] value = new double[bandCount];
    // int w = buffer.getWidth();
    // int h = buffer.getHeight();
    // for( int c = 0; c < w; c++ ) {
    // for( int r = 0; r < h; r++ ) {
    // buffer.get
    // double value = elevIter.getSampleDouble(c, r, 0);
    // if (isNovalue(value)) {
    // continue;
    // }
    // max = Math.max(max, value);
    // min = Math.min(min, value);
    // }
    // }

}
