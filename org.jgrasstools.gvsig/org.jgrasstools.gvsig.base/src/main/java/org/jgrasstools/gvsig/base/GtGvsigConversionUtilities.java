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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.referencing.CRS;
import org.gvsig.crs.Crs;
import org.gvsig.fmap.dal.DALLocator;
import org.gvsig.fmap.dal.DataManager;
import org.gvsig.fmap.dal.DataStore;
import org.gvsig.fmap.dal.DataTypes;
import org.gvsig.fmap.dal.coverage.store.parameter.RasterDataParameters;
import org.gvsig.fmap.dal.feature.EditableFeature;
import org.gvsig.fmap.dal.feature.EditableFeatureAttributeDescriptor;
import org.gvsig.fmap.dal.feature.EditableFeatureType;
import org.gvsig.fmap.dal.feature.Feature;
import org.gvsig.fmap.dal.feature.FeatureAttributeDescriptor;
import org.gvsig.fmap.dal.feature.FeatureSet;
import org.gvsig.fmap.dal.feature.FeatureStore;
import org.gvsig.fmap.dal.feature.FeatureType;
import org.gvsig.fmap.geom.Geometry;
import org.gvsig.fmap.geom.Geometry.SUBTYPES;
import org.gvsig.fmap.geom.Geometry.TYPES;
import org.gvsig.fmap.geom.GeometryLocator;
import org.gvsig.fmap.geom.GeometryManager;
import org.gvsig.fmap.geom.type.GeometryType;
import org.gvsig.fmap.mapcontext.layers.vectorial.FLyrVect;
import org.gvsig.raster.fmap.layers.FLyrRaster;
import org.gvsig.tools.dispose.DisposableIterator;
import org.jgrasstools.gears.utils.CrsUtilities;
import org.jgrasstools.gears.utils.geometry.GeometryUtilities;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.WKTReader;

/**
 * Conversion class to aid when converting between geotools and gvsig.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class GtGvsigConversionUtilities {

    /**
     * Convert a gvSIG FeatureStore to geotools FeatureCollection.
     * 
     * @param store the store to convert.
     * @return the converted featurecollection.
     * @throws Exception
     */
    public static SimpleFeatureCollection toGtFeatureCollection( FeatureStore store ) throws Exception {
        FeatureType featureType = store.getDefaultFeatureType();

        // WKBReader wkbReader = new WKBReader();
        WKTReader wktReader = new WKTReader();

        FeatureAttributeDescriptor[] attributeDescriptors = featureType.getAttributeDescriptors();

        CoordinateReferenceSystem crs = getGtCrsFromFeatureStore(store);

        SimpleFeatureTypeBuilder gtFeatureTypeBuilder = new SimpleFeatureTypeBuilder();
        gtFeatureTypeBuilder.setName(store.getName());
        gtFeatureTypeBuilder.setCRS(crs);

        int attributesCount = 0;
        int geomIndex = -1;

        for( FeatureAttributeDescriptor attributeDescriptor : attributeDescriptors ) {
            String name = attributeDescriptor.getName();

            Class< ? > clazz = attributeDescriptor.getDataType().getDefaultClass();
            // Class< ? > clazz = attributeDescriptor.getClassOfValue();

            GeometryType geomType = null;
            try {
                geomType = attributeDescriptor.getGeomType();
            } catch (Exception e) {
                // ignore
            }
            if (geomType != null) {
                name = "the_geom";
                geomIndex = attributesCount;
                int type = geomType.getType();
                switch( type ) {
                case Geometry.TYPES.POINT:
                    clazz = Point.class;
                    break;
                case Geometry.TYPES.MULTIPOINT:
                    clazz = MultiPoint.class;
                    break;
                case Geometry.TYPES.LINE:
                    clazz = LineString.class;
                    break;
                case Geometry.TYPES.MULTICURVE:
                    clazz = MultiLineString.class;
                    break;
                case Geometry.TYPES.POLYGON:
                    clazz = Polygon.class;
                    break;
                case Geometry.TYPES.MULTISURFACE:
                    clazz = MultiPolygon.class;
                    break;
                default:
                    throw new IllegalArgumentException("Can't recognize geomatry type: " + type);
                }
            }
            gtFeatureTypeBuilder.add(name, clazz);
            attributesCount++;
        }

        DefaultFeatureCollection gtFeatureCollection = new DefaultFeatureCollection();

        SimpleFeatureType gtFeatureType = gtFeatureTypeBuilder.buildFeatureType();
        SimpleFeatureBuilder gtFeatureBuilder = new SimpleFeatureBuilder(gtFeatureType);

        FeatureSet featureSet = store.getFeatureSet();
        DisposableIterator featureIterator = featureSet.fastIterator();
        while( featureIterator.hasNext() ) {
            Feature feature = (Feature) featureIterator.next();

            List<Object> gtAttributesList = new ArrayList<Object>();

            for( int i = 0; i < attributesCount; i++ ) {
                Object object = feature.get(i);
                if (i != geomIndex) {
                    gtAttributesList.add(object);
                } else {
                    // convert geometry
                    // byte[] wkb = feature.getDefaultGeometry().convertToWKB();
                    String wkt = feature.getDefaultGeometry().convertToWKT();
                    // com.vividsolutions.jts.geom.Geometry geometry = wkbReader.read(wkb);
                    com.vividsolutions.jts.geom.Geometry geometry2 = wktReader.read(wkt);
                    gtAttributesList.add(geometry2);
                }
            }

            gtFeatureBuilder.addAll(gtAttributesList);
            SimpleFeature gtFeature = gtFeatureBuilder.buildFeature(null);
            gtFeatureCollection.add(gtFeature);
        }

        return gtFeatureCollection;
    }

    public static FeatureStore toGvsigMemoryFeatureStore( String name, SimpleFeatureCollection gtFeatureCollection )
            throws Exception {
        DataManager dataManager = DALLocator.getDataManager();
        GeometryManager geomManager = GeometryLocator.getGeometryManager();
        FeatureStore store = dataManager.createMemoryStore(null);
        store.edit();

        EditableFeatureType editableFeatureType = store.getDefaultFeatureType().getEditable();

        SimpleFeatureType gtFeatureType = gtFeatureCollection.getSchema();
        CoordinateReferenceSystem crs = gtFeatureType.getCoordinateReferenceSystem();
        String epsgCode = "EPSG:4326";
        // TODO make this from WKT in gvSIG 2.3
        try {
            epsgCode = CrsUtilities.getCodeFromCrs(crs);
        } catch (Exception e) {
            // for now ignore and fallback on 4326
        }

        TreeMap<String, Integer> namesMap = new TreeMap<String, Integer>();

        List<AttributeDescriptor> attributeDescriptors = gtFeatureType.getAttributeDescriptors();
        int attributesCount = attributeDescriptors.size();
        for( AttributeDescriptor attributeDescriptor : attributeDescriptors ) {
            if (attributeDescriptor instanceof GeometryDescriptor) {
                GeometryDescriptor gtGeometryDescriptor = (GeometryDescriptor) attributeDescriptor;
                String geomName = gtGeometryDescriptor.getLocalName();

                EditableFeatureAttributeDescriptor geometryDescriptor = editableFeatureType.add(geomName, DataTypes.GEOMETRY);
                org.jgrasstools.gears.utils.geometry.GeometryType geometryType = GeometryUtilities
                        .getGeometryType(gtGeometryDescriptor.getType());
                switch( geometryType ) {
                case POINT:
                    geometryDescriptor.setGeometryType(geomManager.getGeometryType(TYPES.POINT, SUBTYPES.GEOM2D));
                    break;
                case MULTIPOINT:
                    geometryDescriptor.setGeometryType(geomManager.getGeometryType(TYPES.MULTIPOINT, SUBTYPES.GEOM2D));
                    break;
                case LINE:
                    geometryDescriptor.setGeometryType(geomManager.getGeometryType(TYPES.LINE, SUBTYPES.GEOM2D));
                    break;
                case MULTILINE:
                    geometryDescriptor.setGeometryType(geomManager.getGeometryType(TYPES.MULTICURVE, SUBTYPES.GEOM2D));
                    break;
                case POLYGON:
                    geometryDescriptor.setGeometryType(geomManager.getGeometryType(TYPES.POLYGON, SUBTYPES.GEOM2D));
                    break;
                case MULTIPOLYGON:
                    geometryDescriptor.setGeometryType(geomManager.getGeometryType(TYPES.MULTISURFACE, SUBTYPES.GEOM2D));
                    break;
                default:
                    geometryDescriptor.setGeometryType(geomManager.getGeometryType(TYPES.GEOMETRY, SUBTYPES.GEOM2D));
                    break;
                }
                // geometryDescriptor.setSRS(new Crs(epsgCode));
                editableFeatureType.setDefaultGeometryAttributeName(geomName);
            } else {
                String attrName = attributeDescriptor.getLocalName();
                Integer nCount = namesMap.get(attrName);
                if (nCount == null) {
                    nCount = 1;
                    namesMap.put(attrName, 1);
                } else {
                    nCount++;
                    namesMap.put(attrName, nCount);
                    if (nCount < 10) {
                        attrName = attrName.substring(0, attrName.length() - 1) + nCount;
                    } else {
                        attrName = attrName.substring(0, attrName.length() - 2) + nCount;
                    }
                }

                AttributeType type = attributeDescriptor.getType();
                Class< ? > binding = type.getBinding();
                int dType = DataTypes.STRING;
                if (binding.isAssignableFrom(Integer.class)) {
                    dType = DataTypes.INT;
                } else if (binding.isAssignableFrom(Double.class)) {
                    dType = DataTypes.DOUBLE;
                } else if (binding.isAssignableFrom(Float.class)) {
                    dType = DataTypes.FLOAT;
                } else if (binding.isAssignableFrom(Long.class)) {
                    dType = DataTypes.LONG;
                } else if (binding.isAssignableFrom(Date.class)) {
                    dType = DataTypes.DATE;
                } else if (binding.isAssignableFrom(String.class)) {
                    dType = DataTypes.STRING;
                } else {
                    dType = DataTypes.OBJECT;
                }
                editableFeatureType.add(attrName, dType);
            }

        }

        store.update(editableFeatureType);

        // EditableFeatureAttributeDescriptor featureIdDescriptor =
        // editableFeatureType.add(GraphicLayer.FEATURE_ATTR_FEATUREID,
        // DataTypes.LONG);
        // featureIdDescriptor.setIsPrimaryKey(true);
        // editableFeatureType.setHasOID(true);

        // add features
        SimpleFeatureIterator gtFeatureIterator = gtFeatureCollection.features();
        while( gtFeatureIterator.hasNext() ) {
            SimpleFeature gtFeature = gtFeatureIterator.next();
            EditableFeature gvsigFeature = store.createNewFeature().getEditable();
            for( int i = 0; i < attributesCount; i++ ) {
                Object attribute = gtFeature.getAttribute(i);
                if (attribute instanceof com.vividsolutions.jts.geom.Geometry) {
                    com.vividsolutions.jts.geom.Geometry geom = (com.vividsolutions.jts.geom.Geometry) attribute;
                    Geometry geometry = geomManager.createFrom(geom.toText(), epsgCode);
                    gvsigFeature.set(i, geometry);
                } else {
                    gvsigFeature.set(i, attribute);
                }
            }
            store.insert(gvsigFeature);
        }

        store.finishEditing();
        //
        return store;
    }

    public static CoordinateReferenceSystem gvsigCrs2gtCrs( Crs crsObj ) throws FactoryException {
        CoordinateReferenceSystem crs = CRS.parseWKT(crsObj.getWKT());
        return crs;
    }

    public static CoordinateReferenceSystem getGtCrsFromFeatureStore( FeatureStore store ) throws FactoryException {
        Crs crsObj = (Crs) store.getDynValue(DataStore.METADATA_CRS);
        CoordinateReferenceSystem crs = gvsigCrs2gtCrs(crsObj);
        return crs;
    }

    public static CoordinateReferenceSystem getGtCrsFromVectorFileLayer( FLyrVect vectorLayer ) throws FactoryException {
        Crs crsObject = (Crs) vectorLayer.getFeatureStore().getDynValue(DataStore.METADATA_CRS);
        CoordinateReferenceSystem crs = gvsigCrs2gtCrs(crsObject);
        return crs;
    }

    public static CoordinateReferenceSystem getGtCrsFromRasterFileLayer( FLyrRaster rasterLayer ) throws FactoryException {
        RasterDataParameters rdParams = ((RasterDataParameters) rasterLayer.getDataStore().getParameters());
        Crs crsObject = (Crs) rdParams.getSRS();
        CoordinateReferenceSystem crs = gvsigCrs2gtCrs(crsObject);
        return crs;
    }

}
