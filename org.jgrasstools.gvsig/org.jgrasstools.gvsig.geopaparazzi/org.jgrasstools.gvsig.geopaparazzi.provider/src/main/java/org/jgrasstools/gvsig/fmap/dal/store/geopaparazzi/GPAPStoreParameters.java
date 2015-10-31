package org.jgrasstools.gvsig.fmap.dal.store.geopaparazzi;

import java.io.File;

import org.cresques.cts.IProjection;
import org.gvsig.fmap.dal.DALLocator;
import org.gvsig.fmap.dal.DataManager;
import org.gvsig.fmap.dal.DataStoreParameters;
import org.gvsig.fmap.dal.FileHelper;
import org.gvsig.fmap.dal.feature.EditableFeatureType;
import org.gvsig.fmap.dal.feature.FeatureType;
import org.gvsig.fmap.dal.feature.NewFeatureStoreParameters;
import org.gvsig.fmap.dal.serverexplorer.filesystem.FilesystemStoreParameters;
import org.gvsig.fmap.dal.spi.AbstractDataParameters;
import org.gvsig.fmap.dal.spi.DataStoreProviderServices;
import org.gvsig.fmap.geom.Geometry;
import org.gvsig.tools.dynobject.DelegatedDynObject;

public class GPAPStoreParameters extends AbstractDataParameters
        implements
            DataStoreParameters,
            FilesystemStoreParameters,
            NewFeatureStoreParameters {

    public static final String PARAMETERS_DEFINITION_NAME = "GPAPStoreParameters";

    private static final String FILE_PARAMTER_NAME = "File";

    private DelegatedDynObject parameters;

    public GPAPStoreParameters() {
        this(PARAMETERS_DEFINITION_NAME);
    }

    protected GPAPStoreParameters( String parametersDefinitionName ) {
        this(parametersDefinitionName, GPSPStoreProvider.NAME);
    }

    public GPAPStoreParameters( String parametersDefinitionName, String name ) {
        super();
        this.parameters = (DelegatedDynObject) FileHelper.newParameters(parametersDefinitionName);
        this.setDynValue(DataStoreProviderServices.PROVIDER_PARAMTER_NAME, name);
    }

    public String getDataStoreName() {
        return (String) this.getDynValue(DataStoreProviderServices.PROVIDER_PARAMTER_NAME);
    }

    public String getDescription() {
        return this.getDynClass().getDescription();
    }

    protected DelegatedDynObject getDelegatedDynObject() {
        return parameters;
    }

    public String getFileName() {
        if (this.getFile() == null) {
            return null;
        }
        return this.getFile().getPath();
    }

    public boolean isValid() {
        if (getFileName() == null) {
            return false;
        }
        return true;
    }

    public File getFile() {
        return (File) this.getDynValue(FILE_PARAMTER_NAME);
    }

    public void setFile( File file ) {
        this.setDynValue(FILE_PARAMTER_NAME, file);
    }

    public void setFile( String file ) {
        this.setDynValue(FILE_PARAMTER_NAME, file);
    }

    public EditableFeatureType getDefaultFeatureType() {
        // FIXME: Esto no se si funciona.
        DataManager manager = DALLocator.getDataManager();
        EditableFeatureType featureType = manager.createFeatureType();
        GPSPStoreProvider.initializeFeatureType(featureType, null, Geometry.SUBTYPES.GEOM3D);
        return featureType;
    }

    public void setDefaultFeatureType( FeatureType defaultFeatureType ) {
        throw new UnsupportedOperationException();
    }
}