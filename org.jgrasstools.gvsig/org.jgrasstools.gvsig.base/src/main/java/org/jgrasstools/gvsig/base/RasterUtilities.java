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

import java.awt.Dimension;
import java.io.File;

import org.cresques.cts.IProjection;
import org.gvsig.fmap.dal.DALLocator;
import org.gvsig.fmap.dal.DataManager;
import org.gvsig.fmap.dal.DataServerExplorer;
import org.gvsig.fmap.dal.DataServerExplorerParameters;
import org.gvsig.fmap.dal.coverage.BufferFactory;
import org.gvsig.fmap.dal.coverage.RasterLocator;
import org.gvsig.fmap.dal.coverage.RasterManager;
import org.gvsig.fmap.dal.coverage.dataset.Buffer;
import org.gvsig.fmap.dal.coverage.dataset.BufferParam;
import org.gvsig.fmap.dal.coverage.datastruct.BufferHistogram;
import org.gvsig.fmap.dal.coverage.datastruct.DataStructFactory;
import org.gvsig.fmap.dal.coverage.datastruct.Extent;
import org.gvsig.fmap.dal.coverage.datastruct.NoData;
import org.gvsig.fmap.dal.coverage.store.RasterDataStore;
import org.gvsig.fmap.dal.coverage.store.RasterQuery;
import org.gvsig.fmap.dal.coverage.store.parameter.NewRasterStoreParameters;
import org.gvsig.fmap.dal.coverage.store.parameter.RasterFileStoreParameters;
import org.gvsig.fmap.dal.coverage.store.props.ColorInterpretation;
import org.gvsig.fmap.dal.coverage.store.props.HistogramComputer;
import org.gvsig.fmap.dal.coverage.store.props.Statistics;
import org.gvsig.raster.cache.buffer.BufferInterpolation;
import org.gvsig.tools.dataTypes.DataTypes;
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
     * Get raster {@link Statistics}.
     * 
     * @param dataStore the store to query.
     * @param novalue the value to consider as novalue. If <code>null</code>, it is ignored.
     * @param forceRecalculation if <code>true</code>, the stats are recalculated. Else they
     *              could be read from the rmf file.
     * @return the statistics.
     * @throws Exception
     */
    public static Statistics getRasterStatistics( RasterDataStore dataStore, Double novalue, boolean forceRecalculation )
            throws Exception {
        DataStructFactory dataStructFactory = rasterManager.getDataStructFactory();

        if (novalue != null) {
            NoData noData = dataStructFactory.createNoData(novalue, novalue, dataStore.getName());
            dataStore.setNoDataValue(noData);
        }
        Statistics statistics = dataStore.getStatistics();
        if (forceRecalculation) {
            statistics.forceToRecalc(); // if not run, it could ignore the setNoDataValue
        }
        statistics.calculate(1.0);
        return statistics;
    }

    /**
     * Get raster {@link BufferHistogram}.
     * 
     * @param dataStore the store to query.
     * @param novalue the value to consider as novalue. If <code>null</code>, it is ignored.
     * @param forceRecalculation if <code>true</code>, the stats are recalculated. Else they
     *              could be read from the rmf file.
     * @return the histogram.
     * @throws Exception
     */
    public static BufferHistogram getRasterHistogram( RasterDataStore dataStore, Double novalue, boolean forceRecalculation )
            throws Exception {
        DataStructFactory dataStructFactory = rasterManager.getDataStructFactory();

        if (novalue != null) {
            NoData noData = dataStructFactory.createNoData(novalue, novalue, dataStore.getName());
            dataStore.setNoDataValue(noData);
        }
        HistogramComputer histogramComputer = dataStore.getHistogramComputer();
        if (forceRecalculation) {
            histogramComputer.refreshHistogram(); // if not run, it could ignore the setNoDataValue
        }
        BufferHistogram histogram = histogramComputer.getBufferHistogram();
        return histogram;
    }

    /**
     * Open a raster file source and get a RasterDataStore.
     * 
     * TODO to be checked, copied from outdated docs
     * 
     * @param source the raster file.
     * @return the datastore.
     * @throws Exception
     */
    public static RasterDataStore openRasterSource( File source ) throws Exception {
        DataManager manager = DALLocator.getDataManager();
        RasterFileStoreParameters params = (RasterFileStoreParameters) manager.createStoreParameters("Gdal Store");
        params.setFile(source);
        // params.setSRS(getProjection());
        RasterDataStore dataStore = (RasterDataStore) manager.createStore(params);
        return dataStore;
    }

    /**
     * Read raster data from a datastore.
     * 
     * TODO to be checked, copied from outdated docs
     * 
     * @param rasterDataStore the datastore to read from.
     * @param extent an optional world envelope to read.
     * @param samplingDimension optional resampling window for the read data.
     * @return the read buffer.
     * @throws Exception
     */
    public static Buffer readSingleBandRasterData( RasterDataStore rasterDataStore, Extent extent, Dimension samplingDimension )
            throws Exception {
        RasterQuery query = RasterLocator.getManager().createQuery();
        query.setDrawableBands(new int[]{0});
        query.setReadOnly(true);
        boolean supersamplingLoadingBuffer = false;
        if (extent != null) {
            if (samplingDimension != null) {
                supersamplingLoadingBuffer = true;
                query.setAreaOfInterest(extent, samplingDimension.width, samplingDimension.height);
            } else {
                query.setAreaOfInterest(extent);
            }
        }
        query.setSupersamplingOption(supersamplingLoadingBuffer);
        query.setAreaOfInterest();
        Buffer buffer = rasterDataStore.query(query);
        return buffer;
    }

    /**
     * Create a writable raster buffer.
     * 
     * TODO to be checked, copied from outdated docs
     * 
     * @param width the width of the raster.
     * @param height the height of the raster.
     * @param dataType the datatype as on of: {@link DataTypes}.
     * @param makeMemoryBuffer if <code>true</code>, the buffer is kept in memory.
     * @return the writable buffer.
     * @throws Exception
     */
    public static Buffer createSingleBandWriteBuffer( int width, int height, int dataType, boolean makeMemoryBuffer )
            throws Exception {
        int bandNr = 1; // because single band
        boolean malloc = true;
        BufferFactory bufferFactory = rasterManager.getBufferFactory();
        BufferParam params;
        if (makeMemoryBuffer) {
            params = bufferFactory.createMemoryBufferParams(width, height, bandNr, dataType, malloc);
        } else {
            params = bufferFactory.createBufferParams(width, height, bandNr, dataType, malloc);
        }
        Buffer buffer = bufferFactory.createBuffer(params);
        return buffer;
    }

    /**
     * Create a writable raster buffer of double values.
     * 
     * TODO to be checked, copied from outdated docs
     * 
     * @param width the width of the raster.
     * @param height the height of the raster.
     * @param makeMemoryBuffer if <code>true</code>, the buffer is kept in memory.
     * @return the writable buffer.
     * @throws Exception
     */
    public static Buffer createSingleBandWriteDoubleBuffer( int width, int height, boolean makeMemoryBuffer ) throws Exception {
        int dataType = DataTypes.DOUBLE;
        return createSingleBandWriteBuffer(width, height, dataType, makeMemoryBuffer);
    }

    /**
     * Write a raster buffer to file.
     * 
     * @param outFile the file to write to.
     * @param buffer the data buffer.
     * @param projection the projection to set.
     * @throws Exception
     */
    public static void writeRasterToFile( File outFile, Buffer buffer, IProjection projection ) throws Exception {
        DataManager manager = DALLocator.getDataManager();
        DataServerExplorerParameters eparams = manager.createServerExplorerParameters("FilesystemExplorer");
        eparams.setDynValue("initialpath", outFile.getParentFile().getAbsolutePath());
        DataServerExplorer serverExplorer = manager.openServerExplorer(eparams.getExplorerName(), eparams);

        NewRasterStoreParameters sparams = (NewRasterStoreParameters) serverExplorer.getAddParameters("Gdal Store");
        sparams.setDestination(outFile.getName());
        sparams.setBuffer(buffer);
        sparams.setColorInterpretation(new String[]{ColorInterpretation.GRAY_BAND});
        sparams.setProjection(projection);
        sparams.setBand(0); // 0 means write all bands

        serverExplorer.add("Gdal Store", sparams, true);
    }

    /**
     * Get a double value from a raster at a given row/col position.
     * 
     * @param buffer the raster data.
     * @param row the row to query.
     * @param col the col to query.
     * @param band the band to query.
     * @return the read value.
     */
    public static double getDoubleValue( Buffer buffer, int row, int col, int band ) {
        return buffer.getElemDouble(row, col, band);
    }

    /**
     * Get a double row from a raster.
     * 
     * @param buffer the raster data.
     * @param row the row to query.
     * @param band the band to query.
     * @return the array of data of the row.
     */
    public static double[] getDoubleRow( Buffer buffer, int row, int band ) {
        return buffer.getLineFromBandDouble(row, band);
    }

    /**
     * Write a double value to a raster buffer. 
     * 
     * @param buffer the raster data.
     * @param row the row to set the value.
     * @param col the col to set the value.
     * @param band the band to use.
     * @param value the value to set.
     */
    public static void setDoubleValue( Buffer buffer, int row, int col, int band, double value ) {
        buffer.setElem(row, col, band, value);
    }

    /**
     * Write a double value to a raster buffer. 
     * 
     * @param buffer the raster data.
     * @param row the row to insert.
     * @param band the band to use.
     * @param rowValues the array of values to set the row to.
     */
    public static void setDoubleRowValues( Buffer buffer, int row, int band, double[] rowValues ) {
        buffer.setLineInBandDouble(rowValues, row, band);
    }

    /**
     * Set a whole band to a constant value.
     * 
     * @param buffer the raster data.
     * @param band the band to use.
     * @param value the value to set.
     */
    public static void setConstantDoubleValues( Buffer buffer, int band, double value ) {
        buffer.assign(band, value);
    }

    /**
     * Resample a region to new rows/cols by a given interpolation mode.
     * 
     * @param buffer the raster data.
     * @param newWidth the new cols.
     * @param newHeight the new rows.
     * @param interpolationMode the interpolation mode. If -1, nearest neighbor is used.
     * @return the resampled buffer.
     * @throws Exception
     */
    public static Buffer resample( Buffer buffer, int newWidth, int newHeight, int interpolationMode ) throws Exception {
        if (interpolationMode < 0) {
            interpolationMode = BufferInterpolation.INTERPOLATION_NearestNeighbour;
        }
        Buffer out = buffer.getAdjustedWindow(newWidth, newHeight, interpolationMode);
        return out;
    }

    // public static void main( String[] args ) throws Exception {
    // openRasterSource(null);
    // }

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
