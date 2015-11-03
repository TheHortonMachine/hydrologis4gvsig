package org.jgrasstools.gvsig.geopaparazzi;

import org.gvsig.fmap.dal.exception.DataException;
import org.gvsig.fmap.dal.feature.AbstractFeatureStoreTransform;
import org.gvsig.fmap.dal.feature.EditableFeature;
import org.gvsig.fmap.dal.feature.Feature;
import org.gvsig.fmap.dal.feature.FeatureType;

public class GeopaparazziFeatureStoreTransform extends AbstractFeatureStoreTransform {

    public FeatureType getSourceFeatureTypeFrom( FeatureType targetFeatureType ) {
        // TODO Auto-generated method stub
        return null;
    }

    public void applyTransform( Feature source, EditableFeature target ) throws DataException {
        // TODO Auto-generated method stub
        
    }

    public boolean isTransformsOriginalValues() {
        // TODO Auto-generated method stub
        return false;
    }

}
