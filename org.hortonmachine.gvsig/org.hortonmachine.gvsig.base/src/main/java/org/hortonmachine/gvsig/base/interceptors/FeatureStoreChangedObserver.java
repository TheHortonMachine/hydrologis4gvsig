package org.hortonmachine.gvsig.base.interceptors;

import org.gvsig.fmap.dal.feature.Feature;
import org.gvsig.fmap.dal.feature.FeatureStore;
import org.gvsig.fmap.dal.feature.FeatureStoreNotification;
import org.gvsig.tools.observer.Observable;
import org.gvsig.tools.observer.Observer;

/**
 * {@link FeatureStore} changes observer.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public abstract class FeatureStoreChangedObserver implements Observer {
    @Override
    public void update( final Observable observable, final Object notification ) {
        if (notification instanceof FeatureStoreNotification) {
            FeatureStoreNotification event = (FeatureStoreNotification) notification;
            if (event.getSource() instanceof FeatureStore) {
                FeatureStore featureStore = (FeatureStore) event.getSource();
                String eventType = event.getType();
                if (eventType == FeatureStoreNotification.AFTER_INSERT) {
                    onFeatureAdded(event.getFeature(), featureStore);
                } else if (eventType == FeatureStoreNotification.BEFORE_INSERT) {
                    onBeforeFeatureAdded(event.getFeature(), featureStore);
                } else if (eventType == FeatureStoreNotification.AFTER_DELETE) {
                    onFeatureDeleted(event.getFeature(), featureStore);
                } else if (eventType == FeatureStoreNotification.SELECTION_CHANGE) {
                    onSelectionChanged(event.getFeature(), featureStore);
                } else if (eventType == FeatureStoreNotification.BEFORE_UPDATE) {
                    onBeforeUpdate(event.getFeature(), featureStore);
                }
            }
        }
    }

    public void onBeforeUpdate( Feature feature, FeatureStore featureStore ) {

    }

    public void onBeforeFeatureAdded( Feature feature, FeatureStore featureStore ) {

    }

    public void onSelectionChanged( Feature feature, FeatureStore featureStore ) {

    }

    public void onFeatureDeleted( Feature feature, FeatureStore featureStore ) {

    }

    public void onFeatureAdded( Feature feature, FeatureStore featureStore ) {

    }
}
