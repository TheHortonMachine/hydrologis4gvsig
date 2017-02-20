package org.jgrasstools.gvsig.featurebrowser;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComponent;

import org.gvsig.fmap.dal.exception.DataException;
import org.gvsig.fmap.dal.feature.Feature;
import org.gvsig.fmap.dal.feature.FeatureSelection;
import org.gvsig.fmap.geom.Geometry;
import org.gvsig.fmap.geom.primitive.Envelope;
import org.gvsig.fmap.mapcontext.MapContext;
import org.gvsig.fmap.mapcontext.layers.vectorial.FLyrVect;
import org.gvsig.tools.swing.api.Component;
import org.gvsig.tools.swing.api.ToolsSwingLocator;
import org.gvsig.tools.swing.api.threadsafedialogs.ThreadSafeDialogsManager;
import org.jgrasstools.gvsig.base.FeatureUtilities;
import org.jgrasstools.gvsig.base.JGTUtilities;
import org.jgrasstools.gvsig.base.LayerUtilities;

public class FeatureBrowserController extends FeatureBrowserView implements Component {

    private HashMap<String, FLyrVect> name2LayersMap = new HashMap<String, FLyrVect>();
    private MapContext currentMapcontext;
    private ThreadSafeDialogsManager dialogManager = ToolsSwingLocator.getThreadSafeDialogsManager();

    private String selectedLayerName;

    private int featureCount = 0;
    private int featureSize = 0;
    private List<Feature> features;
    private String[] attributesNames;
    private FLyrVect selectedLayer;

    public FeatureBrowserController() {
        setPreferredSize(new Dimension(600, 300));
        init();
    }

    private void init() {
        
        // TODO implement buffer part
        _bufferLabel.setVisible(false);
        _bufferText.setVisible(false);
        // end TODO
        
        setCombos();

        _layerCombo.addActionListener(new ActionListener(){


            public void actionPerformed( ActionEvent e ) {
                selectedLayerName = (String) _layerCombo.getSelectedItem();

                selectedLayer = name2LayersMap.get(selectedLayerName);
                if (selectedLayer != null) {
                    try {
                        attributesNames = LayerUtilities.getAttributesNames(selectedLayer, false);

                        features = FeatureUtilities.getFeatures(selectedLayer.getFeatureStore(), null, null);
                        featureCount = 1;
                        featureSize = features.size();

                        updateForFeature();

                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                } else {
                    _infoTextArea.setText("");
                }
            }
        });
        
        
        _nextButton.addActionListener(new ActionListener(){
            
            @Override
            public void actionPerformed( ActionEvent e ) {
                featureCount++;
                if (featureCount > featureSize) {
                    featureCount = 1;
                }
                updateForFeature();
            }
        });
        _previousButton.addActionListener(new ActionListener(){
            
            @Override
            public void actionPerformed( ActionEvent e ) {
                featureCount--;
                if (featureCount < 1) {
                    featureCount = featureSize;
                }
                updateForFeature();
            }
        });
    }

    private void updateForFeature() {
        _featureCountLabel.setText("Feature N." + featureCount + " of " + featureSize);

        Feature feature = features.get(featureCount - 1);

        StringBuilder sb = new StringBuilder();
        for( String attrName : attributesNames ) {
            sb.append(attrName).append("=").append(feature.get(attrName).toString()).append("\n");
        }
        _infoTextArea.setText(sb.toString());
        
        Geometry defaultGeometry = feature.getDefaultGeometry();
        Envelope envelope = defaultGeometry.getEnvelope();
        
//        String bufferStr = _bufferText.getText();
//        double buffer = 0;
//        try {
//            buffer = Double.parseDouble(bufferStr);
//        } catch (NumberFormatException e) {
//            // ignore
//        }
        JGTUtilities.zoomTo(envelope);
        
        try {
            FeatureSelection featureSelection = selectedLayer.getFeatureStore().getFeatureSelection();
            featureSelection.deselectAll();
            featureSelection.select(feature);
        } catch (DataException e) {
            e.printStackTrace();
        }
    }

    private void setCombos() {
        try {
            List<FLyrVect> vectorLayers = LayerUtilities.getVectorLayers(currentMapcontext);
            
            if (vectorLayers.size() == name2LayersMap.size()) {
                return;
            }
            
            
            name2LayersMap = new HashMap<String, FLyrVect>();
            String[] names = new String[vectorLayers.size()];
            int count = 0;
            for( FLyrVect fLyrVect : vectorLayers ) {
                String name = fLyrVect.getName();
                name2LayersMap.put(name, fLyrVect);
                names[count++] = name;
            }

            Object selectedLayer = _layerCombo.getSelectedItem();
            _layerCombo.setModel(new DefaultComboBoxModel<String>(names));
            if (selectedLayer != null) {
                _layerCombo.setSelectedItem(selectedLayer);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public JComponent asJComponent() {
        return this;
    }

    public void isVisibleTriggered() {
        // MapContext newMapcontext = ProjectUtilities.getCurrentMapcontext();
        // if (newMapcontext == currentMapcontext) {
        // return;
        // }
        setCombos();
    }

}
