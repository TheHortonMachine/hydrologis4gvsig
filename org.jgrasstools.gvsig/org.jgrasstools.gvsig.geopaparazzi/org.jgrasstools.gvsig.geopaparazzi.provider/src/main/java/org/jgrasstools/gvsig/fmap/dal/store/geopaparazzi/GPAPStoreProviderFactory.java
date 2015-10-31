package org.jgrasstools.gvsig.fmap.dal.store.geopaparazzi;

import org.gvsig.fmap.dal.DataParameters;
import org.gvsig.fmap.dal.DataStoreProvider;
import org.gvsig.fmap.dal.exception.InitializeException;
import org.gvsig.fmap.dal.feature.FeatureStoreProviderFactory;
import org.gvsig.fmap.dal.feature.spi.AbstractFeatureStoreProviderFactory;
import org.gvsig.fmap.dal.spi.DataStoreProviderServices;
import org.gvsig.tools.dynobject.DynObject;

public class GPAPStoreProviderFactory extends AbstractFeatureStoreProviderFactory implements FeatureStoreProviderFactory {

    protected GPAPStoreProviderFactory( String name, String description ) {
        super(name, description);
    }

    public DataStoreProvider createProvider( DataParameters parameters, DataStoreProviderServices providerServices )
            throws InitializeException {
        return new GPSPStoreProvider((GPAPStoreParameters) parameters, providerServices);
    }

    public DynObject createParameters() {
        return new GPAPStoreParameters();
    }

    public int allowCreate() {
        return YES;
    }

    public int allowWrite() {
        return YES;
    }

    public int allowRead() {
        return YES;
    }

    public int hasRasterSupport() {
        return NO;
    }

    public int hasTabularSupport() {
        return YES;
    }

    public int hasVectorialSupport() {
        return YES;
    }

    public int allowMultipleGeometryTypes() {
        return NO;
    }

    public int allowEditableFeatureType() {
        return NO;
    }

}
