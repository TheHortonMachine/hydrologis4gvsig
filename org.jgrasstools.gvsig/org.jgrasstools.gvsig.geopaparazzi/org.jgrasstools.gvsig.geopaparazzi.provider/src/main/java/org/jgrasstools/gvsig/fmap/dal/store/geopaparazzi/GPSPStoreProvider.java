package org.jgrasstools.gvsig.fmap.dal.store.geopaparazzi;

import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.cresques.cts.IProjection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.gvsig.fmap.crs.CRSFactory;
import org.gvsig.fmap.dal.DALLocator;
import org.gvsig.fmap.dal.DataManager;
import org.gvsig.fmap.dal.DataServerExplorer;
import org.gvsig.fmap.dal.DataStore;
import org.gvsig.fmap.dal.DataStoreNotification;
import org.gvsig.fmap.dal.DataTypes;
import org.gvsig.fmap.dal.FileHelper;
import org.gvsig.fmap.dal.exception.DataException;
import org.gvsig.fmap.dal.exception.InitializeException;
import org.gvsig.fmap.dal.exception.LoadException;
import org.gvsig.fmap.dal.exception.OpenException;
import org.gvsig.fmap.dal.exception.ReadException;
import org.gvsig.fmap.dal.exception.ValidateDataParametersException;
import org.gvsig.fmap.dal.exception.WriteException;
import org.gvsig.fmap.dal.feature.EditableFeatureAttributeDescriptor;
import org.gvsig.fmap.dal.feature.EditableFeatureType;
import org.gvsig.fmap.dal.feature.Feature;
import org.gvsig.fmap.dal.feature.FeatureSet;
import org.gvsig.fmap.dal.feature.FeatureType;
import org.gvsig.fmap.dal.feature.exception.PerformEditingException;
import org.gvsig.fmap.dal.feature.spi.FeatureProvider;
import org.gvsig.fmap.dal.feature.spi.FeatureStoreProviderServices;
import org.gvsig.fmap.dal.feature.spi.memory.AbstractMemoryStoreProvider;
import org.gvsig.fmap.dal.resource.ResourceAction;
import org.gvsig.fmap.dal.resource.exception.AccessResourceException;
import org.gvsig.fmap.dal.resource.exception.ResourceNotifyCloseException;
import org.gvsig.fmap.dal.resource.exception.ResourceNotifyOpenException;
import org.gvsig.fmap.dal.resource.file.FileResource;
import org.gvsig.fmap.dal.resource.spi.ResourceConsumer;
import org.gvsig.fmap.dal.resource.spi.ResourceProvider;
import org.gvsig.fmap.dal.serverexplorer.filesystem.FilesystemServerExplorer;
import org.gvsig.fmap.dal.serverexplorer.filesystem.FilesystemServerExplorerParameters;
import org.gvsig.fmap.dal.spi.DataStoreProviderServices;
import org.gvsig.fmap.geom.Geometry;
import org.gvsig.fmap.geom.Geometry.SUBTYPES;
import org.gvsig.fmap.geom.Geometry.TYPES;
import org.gvsig.fmap.geom.GeometryLocator;
import org.gvsig.fmap.geom.GeometryManager;
import org.gvsig.fmap.geom.exception.CreateEnvelopeException;
import org.gvsig.fmap.geom.exception.CreateGeometryException;
import org.gvsig.fmap.geom.operation.GeometryOperationContext;
import org.gvsig.fmap.geom.primitive.Arc;
import org.gvsig.fmap.geom.primitive.Circle;
import org.gvsig.fmap.geom.primitive.Ellipse;
import org.gvsig.fmap.geom.primitive.Envelope;
import org.gvsig.fmap.geom.primitive.Line;
import org.gvsig.fmap.geom.primitive.OrientablePrimitive;
import org.gvsig.fmap.geom.type.GeometryType;
import org.gvsig.tools.ToolsLocator;
import org.gvsig.tools.dispose.DisposableIterator;
import org.gvsig.tools.dynobject.exception.DynFieldNotFoundException;
import org.gvsig.tools.dynobject.exception.DynMethodException;
import org.gvsig.tools.exception.NotYetImplemented;
import org.gvsig.tools.logger.FilteredLogger;
import org.gvsig.tools.persistence.PersistentState;
import org.gvsig.tools.persistence.exception.PersistenceException;
import org.gvsig.tools.task.SimpleTaskStatus;
import org.gvsig.tools.task.TaskStatusManager;

public class GPSPStoreProvider extends AbstractMemoryStoreProvider implements ResourceConsumer {
    private static final Logger logger = LoggerFactory.getLogger(GPSPStoreProvider.class);

    public static final String NAME = "GPAP";
    public static final String DESCRIPTION = "Geopaparazzi database";

    public static final String METADATA_DEFINITION_NAME = NAME;

    public static final String NAME_FIELD_ID = "ID";
    public static final String NAME_FIELD_GEOMETRY = "Geometry";
    public static final String NAME_FIELD_ENTITY = "Entity";
    public static final String NAME_FIELD_LAYER = "Layer";
    public static final String NAME_FIELD_COLOR = "Color";
    public static final String NAME_FIELD_ELEVATION = "Elevation";
    public static final String NAME_FIELD_THICKNESS = "Thickness";
    public static final String NAME_FIELD_TEXT = "Text";
    public static final String NAME_FIELD_HEIGHTTEXT = "HeightText";
    public static final String NAME_FIELD_ROTATIONTEXT = "Rotation";

    private static final int ID_FIELD_ID = 0;
    private static final int ID_FIELD_GEOMETRY = 1;
    private static final int ID_FIELD_ENTITY = 2;
    private static final int ID_FIELD_LAYER = 3;
    private static final int ID_FIELD_COLOR = 4;
    private static final int ID_FIELD_ELEVATION = 5;
    private static final int ID_FIELD_THICKNESS = 6;
    private static final int ID_FIELD_TEXT = 7;
    private static final int ID_FIELD_HEIGHTTEXT = 8;
    private static final int ID_FIELD_ROTATIONTEXT = 9;

    private IProjection projection;
    private ResourceProvider resource;
    private LegendBuilder legendBuilder;

    private long counterNewsOIDs = 0;
    private Envelope envelope;
    private Writer writer;
    protected GeometryManager geomManager = GeometryLocator.getGeometryManager();

    private SimpleTaskStatus taskStatus;

    public GPSPStoreProvider( GPAPStoreParameters parameters, DataStoreProviderServices storeServices )
            throws InitializeException {
        super(parameters, storeServices, FileHelper.newMetadataContainer(METADATA_DEFINITION_NAME));

        TaskStatusManager manager = ToolsLocator.getTaskStatusManager();
        this.taskStatus = manager.creteDefaultSimpleTaskStatus(NAME);

        counterNewsOIDs = 0;

        File file = getGPAPParameters().getFile();
        resource = this.createResource(FileResource.NAME, new Object[]{file.getAbsolutePath()});

        resource.addConsumer(this);

        this.projection = CRSFactory.getCRS("EPSG:4326");

        try {
            legendBuilder = (LegendBuilder) this.invokeDynMethod(LegendBuilder.DYNMETHOD_BUILDER_NAME, null);
        } catch (DynMethodException e) {
            legendBuilder = null;
        } catch (Exception e) {
            throw new InitializeException(e);
        }

        this.initializeFeatureTypes();

    }

    private GPAPStoreParameters getGPAPParameters() {
        return (GPAPStoreParameters) this.getParameters();
    }

    public String getProviderName() {
        return NAME;
    }

    public boolean allowWrite() {
        return true;
    }

    public Object getLegend() throws OpenException {
        this.open();
        if (legendBuilder == null) {
            return null;
        }
        return legendBuilder.getLegend();
    }

    public Object getLabeling() throws OpenException {
        this.open();
        if (legendBuilder == null) {
            return null;
        }
        return legendBuilder.getLabeling();
    }

    private class DXFData {
        public List data = null;
        public FeatureType defaultFType = null;
        public List fTypes = null;
        public Envelope envelope = null;
        public IProjection projection;
        public LegendBuilder legendBuilder;
        public Envelope getEnvelopeCopy() throws CreateEnvelopeException {
            if (envelope == null) {
                return null;
            }
            Envelope newEnvelope;
            if (envelope.getDimension() == 2) {
                newEnvelope = geomManager.createEnvelope(SUBTYPES.GEOM2D);
            } else {
                newEnvelope = geomManager.createEnvelope(SUBTYPES.GEOM3D);

            }
            newEnvelope.setLowerCorner(envelope.getLowerCorner());
            newEnvelope.setUpperCorner(envelope.getUpperCorner());
            return newEnvelope;
        }
    }

    public void open() throws OpenException {
        if (this.data != null) {
            return;
        }
        openEver();
    }

    private void openEver() throws OpenException {
        try {
            this.taskStatus.add();
            getResource().execute(new ResourceAction(){
                public Object run() throws Exception {
                    DXFData dxfData = null;
                    resource.setData(new HashMap());
                    FeatureStoreProviderServices store = getStoreServices();
                    dxfData = new DXFData();
                    dxfData.data = new ArrayList();
                    data = dxfData.data;
                    counterNewsOIDs = 0;
                    Reader reader = new Reader().initialice(getMemoryProvider(), (File) resource.get(), projection,
                            legendBuilder);
                    reader.begin(store);
                    dxfData.defaultFType = reader.getDefaultType().getNotEditableCopy();
                    ArrayList types = new ArrayList();
                    Iterator it = reader.getTypes().iterator();
                    EditableFeatureType fType;
                    while( it.hasNext() ) {
                        fType = (EditableFeatureType) it.next();
                        if (fType.getId().equals(dxfData.defaultFType.getId())) {
                            types.add(dxfData.defaultFType);
                        } else {
                            types.add(fType.getNotEditableCopy());
                        }
                    }
                    dxfData.fTypes = types;

                    resource.notifyOpen();
                    store.setFeatureTypes(dxfData.fTypes, dxfData.defaultFType);
                    reader.load();

                    dxfData.envelope = reader.getEnvelope();

                    dxfData.legendBuilder = legendBuilder;

                    dxfData.projection = projection;

                    reader.end();
                    resource.notifyClose();
                    ((Map) resource.getData()).put(projection.getAbrev(), dxfData); // OJO la
                                                                                    // reproyeccion

                    data = dxfData.data;
                    store.setFeatureTypes(dxfData.fTypes, dxfData.defaultFType);
                    legendBuilder = dxfData.legendBuilder;
                    envelope = dxfData.getEnvelopeCopy();
                    // setDynValue("CRS", projection.getAbrev());
                    counterNewsOIDs = data.size();
                    return null;
                }
            });
            this.taskStatus.terminate();
        } catch (Exception e) {
            data = null;
            this.taskStatus.abort();
            try {
                throw new OpenException(resource.getName(), e);
            } catch (AccessResourceException e1) {
                throw new OpenException(getProviderName(), e);
            }
        } finally {
            this.taskStatus.remove();
        }
    }

    public DataServerExplorer getExplorer() throws ReadException {
        DataManager manager = DALLocator.getDataManager();
        FilesystemServerExplorerParameters params;
        try {
            params = (FilesystemServerExplorerParameters) manager.createServerExplorerParameters(FilesystemServerExplorer.NAME);
            params.setRoot(this.getGPAPParameters().getFile().getParent());
            return manager.openServerExplorer(FilesystemServerExplorer.NAME, params);
        } catch (DataException e) {
            throw new ReadException(this.getProviderName(), e);
        } catch (ValidateDataParametersException e) {
            throw new ReadException(this.getProviderName(), e);
        }

    }

    public void performChanges( Iterator deleteds, Iterator inserteds, Iterator updateds, Iterator originalFeatureTypesUpdated )
            throws PerformEditingException {

        try {
            this.taskStatus.add();
            taskStatus.message("_preparing");
            getResource().execute(new ResourceAction(){
                public Object run() throws Exception {
                    FeatureSet features = null;
                    DisposableIterator it = null;
                    try {
                        File file = (File) resource.get();
                        String fileName = file.getAbsolutePath();
                        writer = new Writer().initialice(file, projection);
                        features = getStoreServices().getFeatureStore().getFeatureSet();
                        List newdata = new ArrayList();
                        writer.begin();
                        it = features.fastIterator();
                        taskStatus.setRangeOfValues(0, 0);
                        long counter = 0;
                        while( it.hasNext() ) {
                            taskStatus.setCurValue(counter++);
                            FeatureProvider feature = getStoreServices()
                                    .getFeatureProviderFromFeature((org.gvsig.fmap.dal.feature.Feature) it.next());
                            writer.add(feature);
                            if (feature.getOID() == null) {
                                logger.warn("feature without OID");
                                feature.setOID(createNewOID());
                            }
                            newdata.add(feature);
                        }
                        data = newdata;
                        writer.end();
                        if (writer.getEnvelope() != null) {
                            envelope = writer.getEnvelope().getGeometry().getEnvelope();
                        }
                        resource.notifyChanges();
                        // counterNewsOIDs = 0;
                    } finally {
                        if (it != null) {
                            it.dispose();
                        }
                        if (features != null) {
                            features.dispose();
                        }
                    }
                    return null;
                }
            });
            this.taskStatus.terminate();
        } catch (Exception e) {
            this.taskStatus.abort();
            throw new PerformEditingException(getResource().toString(), e);
        } finally {
            this.taskStatus.remove();
        }
    }

    public static void initializeFeatureType( EditableFeatureType featureType, IProjection projection, int geometrySubtype ) {
        featureType.setHasOID(true);

        // ID_FIELD_ID = 0;
        featureType.add(NAME_FIELD_ID, DataTypes.INT).setDefaultValue(Integer.valueOf(0)).getIndex();

        EditableFeatureAttributeDescriptor attr = featureType.add(NAME_FIELD_GEOMETRY, DataTypes.GEOMETRY);
        attr.setSRS(projection);
        attr.setGeometryType(Geometry.TYPES.GEOMETRY);
        // If is a 3D file, set the geometry subtype
        attr.setGeometrySubType(geometrySubtype);

        // ID_FIELD_GEOMETRY = 1; //attr.getIndex();

        featureType.setDefaultGeometryAttributeName(NAME_FIELD_GEOMETRY);

        // FIXME: Cual es el size y el valor por defecto para Entity ?
        // ID_FIELD_ENTITY = 2;
        featureType.add(NAME_FIELD_ENTITY, DataTypes.STRING, 100).setDefaultValue("").getIndex();

        // FIXME: Cual es el size de Layer ?
        // ID_FIELD_LAYER = 3;
        featureType.add(NAME_FIELD_LAYER, DataTypes.STRING, 100).setDefaultValue("default").getIndex();

        // ID_FIELD_COLOR = 4;
        featureType.add(NAME_FIELD_COLOR, DataTypes.INT).setDefaultValue(Integer.valueOf(0)).getIndex();

        // ID_FIELD_ELEVATION = 5;
        featureType.add(NAME_FIELD_ELEVATION, DataTypes.DOUBLE).setDefaultValue(Double.valueOf(0)).getIndex();

        // ID_FIELD_THICKNESS = 6;
        featureType.add(NAME_FIELD_THICKNESS, DataTypes.DOUBLE).setDefaultValue(Double.valueOf(0)).getIndex();

        // FIXME: Cual es el size de Text ?
        // ID_FIELD_TEXT = 7;
        featureType.add(NAME_FIELD_TEXT, DataTypes.STRING, 100).setDefaultValue("").getIndex();

        // ID_FIELD_HEIGHTTEXT = 8;
        featureType.add(NAME_FIELD_HEIGHTTEXT, DataTypes.DOUBLE).setDefaultValue(Double.valueOf(10)).getIndex();

        // ID_FIELD_ROTATIONTEXT = 9;
        featureType.add(NAME_FIELD_ROTATIONTEXT, DataTypes.DOUBLE).setDefaultValue(Double.valueOf(0)).getIndex();

        // FIXME: Parece que el DXF puede tener mas atributos opcionales.
        // Habria que ver de pillarlos ?

    }

    public class Reader {
        private File file;
        private String fileName;
        private IProjection projection;
        private List types;
        private LegendBuilder leyendBuilder;
        private AbstractMemoryStoreProvider store;
        private Envelope envelope;

        // Next two variables are used to read the DXF file
        private DxfFeatureMaker featureMaker;
        private DxfHeaderManager headerManager;

        public Reader initialice( AbstractMemoryStoreProvider store, File file, IProjection projection,
                LegendBuilder leyendBuilder ) {
            this.store = store;
            this.file = file;
            this.fileName = file.getAbsolutePath();
            this.projection = projection;
            this.leyendBuilder = leyendBuilder;
            if (leyendBuilder != null) {
                leyendBuilder.initialize(store);
            }
            return this;
        }

        public Envelope getEnvelope() {
            return this.envelope;
        }

        public void begin( FeatureStoreProviderServices store ) throws LoadException {
            taskStatus.message("_preloading_data");
            
            
            
            featureMaker = new DxfFeatureMaker(projection);
            headerManager = new DxfHeaderManager();
            DxfFile dxfFeatureFile = new DxfFile(projection, file.getAbsolutePath(), featureMaker, headerManager);

            try {
                dxfFeatureFile.load();
            } catch (Exception e1) {
                throw new LoadException(e1, fileName);
            }

            taskStatus.message("_preparing_featureType");
            int geometrySubtype;
            if (featureMaker.isDxf3DFile() && headerManager.isWritedDxf3D()) {
                geometrySubtype = Geometry.SUBTYPES.GEOM3D;
            } else {
                geometrySubtype = Geometry.SUBTYPES.GEOM2D;
            }
            EditableFeatureType featureType = store.createFeatureType(getName());
            initializeFeatureType(featureType, this.projection, geometrySubtype);

            types = new ArrayList();
            types.add(featureType);

            if (leyendBuilder != null) {
                taskStatus.message("_preparing_leyend");
                leyendBuilder.begin();
            }

        }

        public void end() {
            if (leyendBuilder != null) {
                leyendBuilder.end();
            }
        }

        public List getTypes() {
            return types;
        }

        public EditableFeatureType getDefaultType() {
            return (EditableFeatureType) types.get(0);
        }

        private Double toDouble( String value ) {
            if (value == null) {
                return Double.valueOf(0);
            }
            return Double.valueOf(value);
        }

        private FeatureProvider createFeature( Feature fea, FeatureType ft, int id ) throws DataException {

            FeatureProvider feature = store.createFeatureProvider(ft);

            feature.setOID(new Long(id));
            feature.set(ID_FIELD_ID, Integer.valueOf(id));
            feature.set(ID_FIELD_ENTITY, fea.getProp("dxfEntity"));
            feature.set(ID_FIELD_LAYER, fea.getProp("layer"));
            feature.set(ID_FIELD_COLOR, Integer.valueOf(fea.getProp("color")));
            feature.set(ID_FIELD_TEXT, fea.getProp("text"));
            feature.set(ID_FIELD_HEIGHTTEXT, toDouble(fea.getProp("textHeight")));
            feature.set(ID_FIELD_ROTATIONTEXT, toDouble(fea.getProp("textRotation")));
            feature.set(ID_FIELD_ELEVATION, toDouble(fea.getProp("elevation")));
            feature.set(ID_FIELD_THICKNESS, toDouble(fea.getProp("thickness")));
            feature.set(ID_FIELD_GEOMETRY, null);
            // FIXME: Abria que pillar el resto de atributos del DXF.

            // FIXME: Habia una incongruencia en el codigo ya que al
            // campo
            // ID_FIELD_GEOMETRY igual le asignaba una geometria que un
            // valor de cadena como 'Point3D', 'Polyline2D' o
            // 'Polyline3D'
            // Faltaria un atributo ID_FIELD_FSHAPE ?
            //
            return feature;
        }

        private Geometry processPoints( GeometryManager gManager, Feature dxffeature ) throws CreateGeometryException {
            if (dxffeature.getGeometry() instanceof org.gvsig.dxf.px.gml.Point3D) {
                org.gvsig.dxf.px.gml.Point3D point = (org.gvsig.dxf.px.gml.Point3D) dxffeature.getGeometry();
                Point3D pto = point.getPoint3D(0);
                org.gvsig.fmap.geom.primitive.Point geom = gManager.createPoint(pto.getX(), pto.getY(), SUBTYPES.GEOM3D);
                geom.setCoordinateAt(Geometry.DIMENSIONS.Z, pto.getZ());

                if (point.isTextPoint()) {
                    /// TODO labeling
                }
                return geom;

            }
            if (dxffeature.getGeometry() instanceof Point) {
                Point point = (Point) dxffeature.getGeometry();
                Point2D pto = point.get(0);

                org.gvsig.fmap.geom.primitive.Point geom = gManager.createPoint(pto.getX(), pto.getY(), SUBTYPES.GEOM2D);

                if (point.isTextPoint()) {
                    /// TODO labeling
                }
                return geom;
            }
            return null;
        }

        private Geometry processLines( GeometryManager gManager, Feature dxffeature ) throws CreateGeometryException {
            if (dxffeature.getGeometry() instanceof LineString3D) {
                Line line = gManager.createLine(SUBTYPES.GEOM3D);
                for( int j = 0; j < dxffeature.getGeometry().pointNr(); j++ ) {
                    Point3D point = ((LineString3D) dxffeature.getGeometry()).getPoint3D(j);
                    line.addVertex(point.getX(), point.getY(), point.getZ());
                }
                return line;

            }
            if (dxffeature.getGeometry() instanceof LineString) {
                Line line = gManager.createLine(SUBTYPES.GEOM2D);
                for( int j = 0; j < dxffeature.getGeometry().pointNr(); j++ ) {
                    Point2D point = dxffeature.getGeometry().get(j);
                    line.addVertex(point.getX(), point.getY());
                }
                return line;
            }
            return null;
        }

        private Geometry processPolygons( GeometryManager gManager, Feature dxffeature ) throws CreateGeometryException {
            if (dxffeature.getGeometry() instanceof Polygon3D) {
                org.gvsig.fmap.geom.primitive.Polygon polygon = gManager.createPolygon(SUBTYPES.GEOM3D);
                for( int j = 0; j < dxffeature.getGeometry().pointNr(); j++ ) {
                    Point3D point = ((LineString3D) dxffeature.getGeometry()).getPoint3D(j);
                    polygon.addVertex(point.getX(), point.getY(), point.getZ());
                }
                Point3D point = ((LineString3D) dxffeature.getGeometry()).getPoint3D(0);
                polygon.addVertex(point.getX(), point.getY(), point.getZ());
                return polygon;

            }
            if (dxffeature.getGeometry() instanceof Polygon) {
                org.gvsig.fmap.geom.primitive.Polygon polygon = gManager.createPolygon(SUBTYPES.GEOM2D);
                for( int j = 0; j < dxffeature.getGeometry().pointNr(); j++ ) {
                    Point2D point = dxffeature.getGeometry().get(j);
                    polygon.addVertex(point.getX(), point.getY());
                }
                Point2D point = dxffeature.getGeometry().get(0);
                polygon.addVertex(point.getX(), point.getY());
                return polygon;
            }
            return null;
        }

        private void addGeometryToFeature( Geometry geometry, FeatureProvider feature ) {
            if (geometry != null) {
                feature.set(ID_FIELD_GEOMETRY, geometry);
                feature.setDefaultGeometry(geometry);
                if (this.envelope == null) {
                    this.envelope = geometry.getEnvelope();
                } else {
                    this.envelope.add(geometry.getEnvelope());
                }
            }
        }

        private void addfeatureToLegend( FeatureProvider feature ) {
            if (leyendBuilder != null) {
                try {
                    leyendBuilder.process(feature);
                } catch (Exception e) {
                    logger.warn(MessageFormat.format("load: legendBuilder process fails in the feature {1}", feature));
                }
            }
        }

        public void load() throws DataException {

            this.envelope = null;

            IObjList.vector features = (IObjList.vector) featureMaker.getObjects();
            String acadVersion = headerManager.getAcadVersion();

            logger.info("load: acadVersion = '" + acadVersion + "'");

            GeometryManager gManager = GeometryLocator.getGeometryManager();

            if (!featureMaker.isDxf3DFile() && !headerManager.isWritedDxf3D()) {
                taskStatus.message("_fixing_3dgeometries");
                Feature[] features2D = new Feature[features.size()];
                taskStatus.setRangeOfValues(0, features.size());
                for( int i = 0; i < features.size(); i++ ) {
                    taskStatus.setCurValue(i);
                    Feature fea = (Feature) features.get(i);
                    if (fea.getGeometry() instanceof org.gvsig.dxf.px.gml.Point3D) {
                        Point point = (Point) fea.getGeometry();
                        Point point2 = new Point();
                        for( int j = 0; j < point.pointNr(); j++ ) {
                            point2.add(point.get(j));
                        }
                        point2.setTextPoint(point.isTextPoint());
                        fea.setGeometry(point2);
                        features2D[i] = fea;

                    } else if (fea.getGeometry() instanceof LineString3D) {
                        LineString lineString = (LineString) fea.getGeometry();
                        LineString lineString2 = new LineString();
                        for( int j = 0; j < lineString.pointNr(); j++ ) {
                            lineString2.add(lineString.get(j));
                        }
                        fea.setGeometry(lineString2);
                        features2D[i] = fea;
                    } else if (fea.getGeometry() instanceof Polygon3D) {
                        Polygon polygon = (Polygon) fea.getGeometry();
                        Polygon polygon2 = new Polygon();
                        for( int j = 0; j < polygon.pointNr(); j++ ) {
                            polygon2.add(polygon.get(j));
                        }
                        fea.setGeometry(polygon2);
                        features2D[i] = fea;
                    }
                }
                features.clear();
                for( int i = 0; i < features2D.length; i++ ) {
                    features.add(features2D[i]);
                }
            }

            FilteredLogger logger = new FilteredLogger(GPSPStoreProvider.logger, "DXFLoafing", 20);

            FeatureType ft = store.getStoreServices().getDefaultFeatureType();
            taskStatus.message("_loading");
            // Nos recorremos las geometrias tres veces para cargarlas en orden:
            // - poligonos
            // - lineas
            // - puntos
            // Y garantizar que siempre se pinten los puntos sobre lineas y
            // poligonos y las lineas sobre los polignos.
            int n = 0;
            taskStatus.setRangeOfValues(0, features.size() * 3);
            for( int i = 0; i < features.size(); i++ ) {
                taskStatus.setCurValue(n++);
                try {
                    Feature dxffeature = (Feature) features.get(i);
                    FeatureProvider feature = createFeature(dxffeature, ft, i);
                    Geometry geometry = processPolygons(gManager, dxffeature);
                    if (geometry != null) {
                        addGeometryToFeature(geometry, feature);
                        store.addFeatureProvider(feature);
                        addfeatureToLegend(feature);
                    }
                } catch (Exception e) {
                    logger.warn("Can't proccess feature '" + i + ", of file '" + fileName + "'.", e);
                }
            }
            for( int i = 0; i < features.size(); i++ ) {
                taskStatus.setCurValue(n++);
                try {
                    Feature dxffeature = (Feature) features.get(i);
                    FeatureProvider feature = createFeature(dxffeature, ft, i);
                    Geometry geometry = processLines(gManager, dxffeature);
                    if (geometry != null) {
                        addGeometryToFeature(geometry, feature);
                        store.addFeatureProvider(feature);
                        addfeatureToLegend(feature);
                    }
                } catch (Exception e) {
                    logger.warn("Can't proccess feature '" + i + ", of file '" + fileName + "'.", e);
                }
            }
            for( int i = 0; i < features.size(); i++ ) {
                taskStatus.setCurValue(n++);
                try {
                    Feature dxffeature = (Feature) features.get(i);
                    FeatureProvider feature = createFeature(dxffeature, ft, i);
                    Geometry geometry = processPoints(gManager, dxffeature);
                    if (geometry != null) {
                        addGeometryToFeature(geometry, feature);
                        store.addFeatureProvider(feature);
                        addfeatureToLegend(feature);
                    }
                } catch (Exception e) {
                    logger.warn("Can't proccess feature '" + i + ", of file '" + fileName + "'.", e);
                }
            }
        }

    }

    public class Writer {
        private Double DEFAULT_ELEVATION = new Double(0);

        private DxfFile.EntityFactory entityMaker;

        private IProjection proj = null;

        private int handle = 40; // Revisar porqu� es 40.

        private int k = 0;

        private boolean dxf3DFile = false;
        private String fileName;
        private Envelope envelope;

        public Writer initialice( File file, IProjection projection ) {
            this.proj = projection;
            this.fileName = file.getAbsolutePath();
            entityMaker = new DxfEntityMaker(proj);
            return this;
        }

        public void begin() {
            envelope = null;
            entityMaker = new DxfEntityMaker(proj);
        }

        public void end() throws WriteException {
            try {
                DxfFile dxfFile = new DxfFile(null, fileName, entityMaker);
                dxfFile.setCadFlag(true);
                if (dxf3DFile) {
                    dxfFile.setDxf3DFlag(true);
                }
                dxfFile.save(fileName);
                dxfFile.close();
            } catch (Exception e) {
                throw new WriteException(fileName, e);
            }
        }

        public void add( FeatureProvider feature ) throws WriteException {
            try {
                Geometry geom = feature.getDefaultGeometry();
                if (geom == null) {
                    // FIXME: tirar al log y avisar al usuario
                    return;
                }
                GeometryType type = geom.getGeometryType();
                boolean geometrySupported = true;

                if ((TYPES.POINT == type.getType()) && (SUBTYPES.GEOM3D == type.getSubType())) {
                    dxf3DFile = true;
                    k = createPoint3D(handle, k, feature);

                } else if ((TYPES.POINT == type.getType()) && (SUBTYPES.GEOM2D == type.getSubType())) {
                    k = createPoint2D(handle, k, feature);

                } else if ((TYPES.CURVE == type.getType()) && (SUBTYPES.GEOM3D == type.getSubType())) {
                    dxf3DFile = true;
                    k = createPolyline3D(handle, k, feature);

                } else if ((TYPES.ARC == type.getType()) && (SUBTYPES.GEOM2D == type.getSubType())) {
                    k = createArc2D(handle, k, feature);

                } else if ((TYPES.CURVE == type.getType()) && (SUBTYPES.GEOM2D == type.getSubType())) {
                    k = createLwPolyline2D(handle, k, feature, false);

                } else if ((TYPES.SURFACE == type.getType()) && (SUBTYPES.GEOM3D == type.getSubType())) {
                    dxf3DFile = true;
                    k = createPolyline3D(handle, k, feature);

                } else if ((TYPES.CIRCLE == type.getType()) && (SUBTYPES.GEOM2D == type.getSubType())) {
                    k = createCircle2D(handle, k, feature);

                } else if ((TYPES.ELLIPSE == type.getType()) && (SUBTYPES.GEOM2D == type.getSubType())) {
                    k = createEllipse2D(handle, k, feature);

                } else if ((TYPES.SURFACE == type.getType()) && (SUBTYPES.GEOM2D == type.getSubType())) {
                    k = createLwPolyline2D(handle, k, feature, true);

                } else {
                    geometrySupported = false;
                    logger.warn(
                            MessageFormat.format("Geometry '{1}' not yet supported", new Object[]{geom.getClass().getName()}));
                    k++;
                }
                if (geometrySupported) {
                    if (this.envelope != null) {
                        this.envelope.add(feature.getDefaultEnvelope());
                    } else {
                        this.envelope = feature.getDefaultEnvelope().getGeometry().getEnvelope();
                    }
                }
            } catch (Exception e) {
                throw new WriteException(fileName, e);
            }

        }

        private boolean hasText( FeatureProvider feature ) {
            if (feature.isNull(ID_FIELD_TEXT)) {
                return false;
            }
            if (feature.get(ID_FIELD_TEXT).equals("")) {
                return false;
            }
            return true;
        }

        private DxfGroupVector updateProperties( FeatureProvider feature, int k ) {
            DxfGroupVector polv = new DxfGroupVector();

            String layer = (String) feature.get(ID_FIELD_LAYER);
            Integer color = (Integer) feature.get(ID_FIELD_COLOR);
            Double thickness = (Double) feature.get(ID_FIELD_THICKNESS);

            DxfGroup geometryLayer = new DxfGroup(8, layer);

            DxfGroup handleGroup = new DxfGroup();
            handleGroup.setCode(5);
            handleGroup.setData(new Integer(handle + k).toString());

            DxfGroup handleColor = new DxfGroup();
            handleColor.setCode(62);
            handleColor.setData(color);

            DxfGroup handleThickness = new DxfGroup();
            handleThickness.setCode(39);
            handleThickness.setData(thickness);

            polv.add(geometryLayer);
            polv.add(handleGroup);
            polv.add(handleColor);
            return polv;
        }

        private int createPoint2D( int handle, int k, FeatureProvider feature ) throws Exception {

            if (hasText(feature)) {
                return createText2D(handle, k, feature);
            }
            org.gvsig.fmap.geom.primitive.Point point = geomManager.createPoint(0, 0, SUBTYPES.GEOM2D);
            double[] pointCoords = new double[6];
            PathIterator pointIt = (feature.getDefaultGeometry()).getPathIterator(null);
            while( !pointIt.isDone() ) {
                pointIt.currentSegment(pointCoords);
                point = geomManager.createPoint(pointCoords[0], pointCoords[1], SUBTYPES.GEOM2D);
                pointIt.next();
            }
            Point2D pto = new Point2D.Double(point.getX(), point.getY());

            DxfGroup px = new DxfGroup();
            DxfGroup py = new DxfGroup();
            DxfGroup pz = new DxfGroup();
            px.setCode(10);
            px.setData(new Double(pto.getX()));
            py.setCode(20);
            py.setData(new Double(pto.getY()));
            pz.setCode(30);
            // FIXME: POINT del DXF tiene cota. Le asigno cero arbitrariamente.
            pz.setData(new Double(0.0));
            DxfGroupVector pv = updateProperties(feature, k);
            pv.add(px);
            pv.add(py);
            pv.add(pz);
            entityMaker.createPoint(pv);
            k++;
            return k;
        }

        private int createText2D( int handle, int k, FeatureProvider feature ) throws Exception {

            String text = feature.get(ID_FIELD_TEXT).toString();
            Double heightText = (Double) feature.get(ID_FIELD_HEIGHTTEXT);
            Double rotationText = (Double) feature.get(ID_FIELD_ROTATIONTEXT);

            DxfGroup handleText = new DxfGroup();
            handleText.setCode(1);
            handleText.setData(text);

            DxfGroup handleHeightText = new DxfGroup();
            handleHeightText.setCode(40);
            handleHeightText.setData(heightText);

            DxfGroup handleRotationText = new DxfGroup();
            handleRotationText.setCode(50);
            handleRotationText.setData(rotationText);

            org.gvsig.fmap.geom.primitive.Point point = geomManager.createPoint(0, 0, SUBTYPES.GEOM2D);
            double[] pointCoords = new double[6];
            PathIterator pointIt = (feature.getDefaultGeometry()).getPathIterator(null);
            while( !pointIt.isDone() ) {
                pointIt.currentSegment(pointCoords);
                point = geomManager.createPoint(pointCoords[0], pointCoords[1], SUBTYPES.GEOM2D);
                pointIt.next();
            }
            Point2D pto = new Point2D.Double(point.getX(), point.getY());
            DxfGroup handleGroup = new DxfGroup();
            handleGroup.setCode(5);
            handleGroup.setData(new Integer(handle + k).toString());
            DxfGroup px = new DxfGroup();
            DxfGroup py = new DxfGroup();
            DxfGroup pz = new DxfGroup();
            px.setCode(10);
            px.setData(new Double(pto.getX()));
            py.setCode(20);
            py.setData(new Double(pto.getY()));
            pz.setCode(30);
            // FIXME: POINT del DXF tiene cota. Le asigno cero arbitrariamente.
            pz.setData(new Double(0.0));
            DxfGroupVector pv = updateProperties(feature, k);
            pv.add(handleText);
            pv.add(handleHeightText);
            pv.add(handleRotationText);
            pv.add(handleGroup);
            pv.add(px);
            pv.add(py);
            pv.add(pz);
            entityMaker.createText(pv);
            k++;
            return k;
        }

        private int createPoint3D( int handle, int k, FeatureProvider feature ) throws Exception {
            if (hasText(feature)) {
                return createText3D(handle, k, feature);
            }
            org.gvsig.fmap.geom.primitive.Point point = (org.gvsig.fmap.geom.primitive.Point) geomManager.create(TYPES.POINT,
                    SUBTYPES.GEOM3D);
            double[] pointCoords = new double[6];
            PathIterator pointIt = (feature.getDefaultGeometry()).getPathIterator(null);
            while( !pointIt.isDone() ) {
                pointIt.currentSegment(pointCoords);
                point = (org.gvsig.fmap.geom.primitive.Point) geomManager.create(TYPES.POINT, SUBTYPES.GEOM3D);
                point.setCoordinateAt(0, pointCoords[0]);
                point.setCoordinateAt(1, pointCoords[1]);
                point.setCoordinateAt(2, pointCoords[2]);
                pointIt.next();
            }
            org.gvsig.fmap.geom.primitive.Point pto = (org.gvsig.fmap.geom.primitive.Point) geomManager.create(TYPES.POINT,
                    SUBTYPES.GEOM3D);
            pto.setCoordinateAt(0, point.getCoordinateAt(0));
            pto.setCoordinateAt(1, point.getCoordinateAt(1));
            pto.setCoordinateAt(2, point.getCoordinateAt(2));
            DxfGroup px = new DxfGroup();
            DxfGroup py = new DxfGroup();
            DxfGroup pz = new DxfGroup();
            px.setCode(10);
            px.setData(new Double(pto.getX()));
            py.setCode(20);
            py.setData(new Double(pto.getY()));
            pz.setCode(30);
            pz.setData(new Double(pto.getCoordinateAt(2)));
            double velev = ((org.gvsig.fmap.geom.primitive.Point) feature.getDefaultGeometry()).getCoordinateAt(2);
            Double elevation = DEFAULT_ELEVATION;
            elevation = new Double(velev);
            DxfGroup handleElevation = new DxfGroup();
            handleElevation.setCode(38);
            handleElevation.setData(elevation);

            DxfGroupVector pv = updateProperties(feature, k);
            pv.add(handleElevation);
            pv.add(px);
            pv.add(py);
            pv.add(pz);
            entityMaker.createPoint(pv);
            k++;
            return k;
        }

        private int createText3D( int handle, int k, FeatureProvider feature ) throws Exception {

            double velev = ((org.gvsig.fmap.geom.primitive.Point) feature.getDefaultGeometry()).getCoordinateAt(0);

            Double elevation = new Double(velev);
            String text = feature.get(ID_FIELD_TEXT).toString();
            Double heightText = (Double) feature.get(ID_FIELD_HEIGHTTEXT);
            Double rotationText = (Double) feature.get(ID_FIELD_ROTATIONTEXT);

            DxfGroup handleText = new DxfGroup();
            handleText.setCode(1);
            handleText.setData(text);

            DxfGroup handleHeightText = new DxfGroup();
            handleHeightText.setCode(40);
            handleHeightText.setData(heightText);

            DxfGroup handleRotationText = new DxfGroup();
            handleRotationText.setCode(50);
            handleRotationText.setData(rotationText);

            DxfGroup handleElevation = new DxfGroup();
            handleElevation.setCode(38);
            handleElevation.setData(elevation);

            org.gvsig.fmap.geom.primitive.Point point = (org.gvsig.fmap.geom.primitive.Point) (feature.getDefaultGeometry())
                    .getInternalShape();

            DxfGroup handleGroup = new DxfGroup();
            handleGroup.setCode(5);
            handleGroup.setData(new Integer(handle + k).toString());
            DxfGroup px = new DxfGroup();
            DxfGroup py = new DxfGroup();
            DxfGroup pz = new DxfGroup();
            px.setCode(10);
            px.setData(new Double(point.getX()));
            py.setCode(20);
            py.setData(new Double(point.getY()));
            pz.setCode(30);
            pz.setData(new Double(point.getCoordinateAt(2)));
            DxfGroupVector pv = updateProperties(feature, k);
            pv.add(handleElevation);
            pv.add(handleText);
            pv.add(handleHeightText);
            pv.add(handleRotationText);
            pv.add(handleGroup);
            pv.add(px);
            pv.add(py);
            pv.add(pz);
            entityMaker.createText(pv);
            k++;
            return k;
        }

        private int createLwPolyline2D( int handle, int k, FeatureProvider feature, boolean isPolygon ) throws Exception {
            boolean first = true;
            DxfGroupVector polv = updateProperties(feature, k);
            Vector vpoints = new Vector();

            DxfGroup polylineFlag = new DxfGroup();
            polylineFlag.setCode(70);
            if (isPolygon) {
                polylineFlag.setData(new Integer(1)); // cerrada
            } else {
                polylineFlag.setData(new Integer(0)); // abierta
            }

            PathIterator theIterator = (feature.getDefaultGeometry()).getPathIterator(null, geomManager.getFlatness()); // polyLine.
            // getPathIterator
            // (null,
            // flatness);

            double[] theData = new double[6];
            while( !theIterator.isDone() ) {
                int theType = theIterator.currentSegment(theData);
                switch( theType ) {
                case PathIterator.SEG_MOVETO:
                    if (!first) {
                        for( int j = 0; j < vpoints.size(); j++ ) {
                            DxfGroup xvertex = new DxfGroup();
                            xvertex.setCode(10);
                            xvertex.setData(new Double(((org.gvsig.fmap.geom.primitive.Point) vpoints.get(j)).getX()));
                            DxfGroup yvertex = new DxfGroup();
                            yvertex.setCode(20);
                            yvertex.setData(new Double(((org.gvsig.fmap.geom.primitive.Point) vpoints.get(j)).getY()));
                            polv.add(xvertex);
                            polv.add(yvertex);
                        }

                        entityMaker.createLwPolyline(polv);
                        k++;
                        polv = updateProperties(feature, k);

                    }
                    first = false;
                    polv.add(polylineFlag);
                    vpoints.clear();
                    vpoints.add(geomManager.createPoint(theData[0], theData[1], SUBTYPES.GEOM2D));
                    break;
                case PathIterator.SEG_LINETO:
                    vpoints.add(geomManager.createPoint(theData[0], theData[1], SUBTYPES.GEOM2D));
                    break;
                case PathIterator.SEG_QUADTO:
                    break;
                case PathIterator.SEG_CUBICTO:
                    break;
                case PathIterator.SEG_CLOSE:
                    polylineFlag.setData(new Integer(1)); // cerrada
                    break;

                }
                theIterator.next();
            }

            for( int j = 0; j < vpoints.size(); j++ ) {
                DxfGroup xvertex = new DxfGroup();
                xvertex.setCode(10);
                xvertex.setData(new Double(((org.gvsig.fmap.geom.primitive.Point) vpoints.get(j)).getX()));
                DxfGroup yvertex = new DxfGroup();
                yvertex.setCode(20);
                yvertex.setData(new Double(((org.gvsig.fmap.geom.primitive.Point) vpoints.get(j)).getY()));
                polv.add(xvertex);
                polv.add(yvertex);
            }

            entityMaker.createLwPolyline(polv);
            k++;
            return k;
        }

        private int createPolyline3D( int handle, int k, FeatureProvider feature ) throws Exception {
            DxfGroupVector polv = updateProperties(feature, k);
            Vector vpoints = new Vector();
            PathIterator theIterator = (feature.getDefaultGeometry()).getPathIterator(null, geomManager.getFlatness()); // polyLine.
            // getPathIterator
            // (null,
            // flatness);
            double[] theData = new double[6];
            OrientablePrimitive curve = (OrientablePrimitive) feature.getDefaultGeometry();
            double[] velev = new double[curve.getNumVertices()];
            for( int i = 0; i < curve.getNumVertices(); i++ ) {
                velev[i] = curve.getCoordinateAt(i, 2);
            }

            while( !theIterator.isDone() ) {
                int theType = theIterator.currentSegment(theData);
                switch( theType ) {
                case PathIterator.SEG_MOVETO:
                    vpoints.add(geomManager.createPoint(theData[0], theData[1], SUBTYPES.GEOM2D));
                    break;
                case PathIterator.SEG_LINETO:
                    vpoints.add(geomManager.createPoint(theData[0], theData[1], SUBTYPES.GEOM2D));
                    break;
                }
                theIterator.next();
            }
            if (constantElevation(velev)) {
                DxfGroup polylineFlag = new DxfGroup();
                polylineFlag.setCode(70);
                polylineFlag.setData(new Integer(0));
                polv.add(polylineFlag);
                DxfGroup elevation = new DxfGroup();
                elevation.setCode(38);
                elevation.setData(new Double(velev[0]));
                polv.add(elevation);
                for( int j = 0; j < vpoints.size(); j++ ) {
                    DxfGroup xvertex = new DxfGroup();
                    xvertex.setCode(10);
                    xvertex.setData(new Double(((org.gvsig.fmap.geom.primitive.Point) vpoints.get(j)).getX()));
                    DxfGroup yvertex = new DxfGroup();
                    yvertex.setCode(20);
                    yvertex.setData(new Double(((org.gvsig.fmap.geom.primitive.Point) vpoints.get(j)).getY()));
                    polv.add(xvertex);
                    polv.add(yvertex);
                }
                entityMaker.createLwPolyline(polv);
                k++;
            } else {
                DxfGroup polylineFlag = new DxfGroup();
                polylineFlag.setCode(70);
                polylineFlag.setData(new Integer(8));
                polv.add(polylineFlag);
                DxfGroup xgroup = new DxfGroup();
                xgroup.setCode(10);
                xgroup.setData(new Double(0.0));
                polv.add(xgroup);
                DxfGroup ygroup = new DxfGroup();
                ygroup.setCode(20);
                ygroup.setData(new Double(0.0));
                polv.add(ygroup);
                DxfGroup elevation = new DxfGroup();
                elevation.setCode(30);
                elevation.setData(new Double(0.0));
                polv.add(elevation);
                DxfGroup subclassMarker = new DxfGroup(100, "AcDb3dPolyline");
                polv.add(subclassMarker);
                entityMaker.createPolyline(polv);
                k++;
                for( int j = 0; j < vpoints.size(); j++ ) {
                    DxfGroupVector verv = new DxfGroupVector();
                    DxfGroup entityType = new DxfGroup(0, "VERTEX");
                    verv.add(entityType);
                    DxfGroup generalSubclassMarker = new DxfGroup(100, "AcDbEntity");
                    verv.add(generalSubclassMarker);
                    DxfGroup layerName = new DxfGroup(8, "default");
                    verv.add(layerName);
                    DxfGroup vertexSubclassMarker = new DxfGroup(100, "AcDbVertex");
                    verv.add(vertexSubclassMarker);
                    DxfGroup xvertex = new DxfGroup();
                    xvertex.setCode(10);
                    xvertex.setData(new Double(((org.gvsig.fmap.geom.primitive.Point) vpoints.get(j)).getX()));
                    DxfGroup yvertex = new DxfGroup();
                    yvertex.setCode(20);
                    yvertex.setData(new Double(((org.gvsig.fmap.geom.primitive.Point) vpoints.get(j)).getY()));
                    DxfGroup zvertex = new DxfGroup();
                    zvertex.setCode(30);
                    zvertex.setData(new Double(velev[j]));
                    verv.add(xvertex);
                    verv.add(yvertex);
                    verv.add(zvertex);
                    entityMaker.addVertex(verv);
                    k++;
                }
                DxfGroupVector seqv = new DxfGroupVector();
                DxfGroup entityType = new DxfGroup(0, "SEQEND");
                seqv.add(entityType);
                DxfGroup generalSubclassMarker = new DxfGroup(100, "AcDbEntity");
                seqv.add(generalSubclassMarker);
                DxfGroup layerName = new DxfGroup(8, "default");
                seqv.add(layerName);
                DxfGroup handleSeqGroup = new DxfGroup();
                handleSeqGroup.setCode(5);
                handleSeqGroup.setData(new Integer(handle + k).toString());
                seqv.add(handleSeqGroup);
                entityMaker.endSeq();
                k++;
            }
            return k;
        }

        private boolean constantElevation( double[] velev ) {
            boolean constant = true;
            for( int i = 0; i < velev.length; i++ ) {
                for( int j = 0; j < velev.length; j++ ) {
                    if (j > i) {
                        if (velev[i] != velev[j]) {
                            constant = false;
                            break;
                        }
                    }
                }
                break;
            }
            return constant;
        }

        private int createCircle2D( int handle, int k, FeatureProvider feature ) throws Exception {
            DxfGroupVector polv = updateProperties(feature, k);
            DxfGroup circleFlag = new DxfGroup();
            circleFlag.setCode(100);
            polv.add(circleFlag);

            DxfGroup xvertex = new DxfGroup();
            xvertex.setCode(10);
            Circle circle = (Circle) (feature.getDefaultGeometry()).getInternalShape();
            xvertex.setData(new Double(circle.getCenter().getX()));
            DxfGroup yvertex = new DxfGroup();
            yvertex.setCode(20);
            yvertex.setData(new Double(circle.getCenter().getY()));
            DxfGroup zvertex = new DxfGroup();
            zvertex.setCode(30);
            // TODO: COORDENADA Z. REVISAR ESTO PARA ENTIDADES 3D
            zvertex.setData(new Double(0));

            DxfGroup radius = new DxfGroup();
            radius.setCode(40);
            radius.setData(new Double(circle.getRadious()));

            polv.add(xvertex);
            polv.add(yvertex);
            polv.add(zvertex);
            polv.add(radius);

            entityMaker.createCircle(polv);
            k++;
            return k;
        }

        private int createArc2D( int handle, int k, FeatureProvider feature ) throws Exception {
            return 1;
        }

        private int createEllipse2D( int handle, int k, FeatureProvider feature ) throws Exception {
            return 1;
        }

        public Envelope getEnvelope() {
            return this.envelope;
        }

    }

    public boolean closeResourceRequested( ResourceProvider resource ) {
        return true;
    }

    public int getOIDType() {
        return DataTypes.LONG;
    }

    public boolean supportsAppendMode() {
        return false;
    }

    public void append( FeatureProvider featureProvider ) {
        try {
            writer.add(featureProvider);
        } catch (WriteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void beginAppend() {
        try {
            writer = new Writer().initialice((File) resource.get(), projection);
            writer.begin();
        } catch (AccessResourceException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void endAppend() {
        try {
            resource.notifyOpen();
            writer.end();
            resource.notifyClose();
            counterNewsOIDs = 0;
        } catch (ResourceNotifyOpenException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ResourceNotifyCloseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (WriteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void saveToState( PersistentState state ) throws PersistenceException {
        // TODO Auto-generated method stub
        throw new NotYetImplemented();
    }

    public void loadFromState( PersistentState state ) throws PersistenceException {
        // TODO Auto-generated method stub
        throw new NotYetImplemented();
    }

    public Object createNewOID() {
        return new Long(counterNewsOIDs++);
    }

    protected void initializeFeatureTypes() throws InitializeException {
        try {
            this.open();
        } catch (OpenException e) {
            throw new InitializeException(this.getProviderName(), e);
        }
    }

    public Envelope getEnvelope() throws DataException {
        this.open();
        return this.envelope;
    }

    public Object getDynValue( String name ) throws DynFieldNotFoundException {
        if (DataStore.METADATA_ENVELOPE.equalsIgnoreCase(name)) {
            try {
                return this.getEnvelope();
            } catch (DataException e) {
                return null;
            }
        }
        return super.getDynValue(name);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.gvsig.fmap.dal.resource.spi.ResourceConsumer#resourceChanged(org.
     * gvsig.fmap.dal.resource.spi.ResourceProvider)
     */
    public void resourceChanged( ResourceProvider resource ) {
        this.getStoreServices().notifyChange(DataStoreNotification.RESOURCE_CHANGED, resource);
    }

    public Object getSourceId() {
        return this.getGPAPParameters().getFile();
    }

    public String getName() {
        String name = this.getGPAPParameters().getFile().getName();
        int n = name.lastIndexOf(".");
        if (n < 1) {
            return name;
        }
        return name.substring(0, n);
    }

    public String getFullName() {
        return this.getGPAPParameters().getFile().getAbsolutePath();
    }

    public ResourceProvider getResource() {
        return resource;
    }

}