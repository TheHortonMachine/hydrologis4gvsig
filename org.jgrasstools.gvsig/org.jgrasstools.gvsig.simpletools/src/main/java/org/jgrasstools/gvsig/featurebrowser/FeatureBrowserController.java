package org.jgrasstools.gvsig.featurebrowser;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComponent;

import org.cresques.cts.ICoordTrans;
import org.cresques.cts.IProjection;
import org.gvsig.fmap.dal.feature.Feature;
import org.gvsig.fmap.dal.feature.FeatureSelection;
import org.gvsig.fmap.geom.Geometry;
import org.gvsig.fmap.geom.primitive.Envelope;
import org.gvsig.fmap.mapcontext.MapContext;
import org.gvsig.fmap.mapcontext.layers.vectorial.FLyrVect;
import org.gvsig.tools.swing.api.Component;
import org.jgrasstools.gvsig.base.FeatureUtilities;
import org.jgrasstools.gvsig.base.JGTUtilities;
import org.jgrasstools.gvsig.base.LayerUtilities;
import org.jgrasstools.gvsig.base.ProjectUtilities;

public class FeatureBrowserController extends FeatureBrowserView implements Component {

    private static final long serialVersionUID = 1L;
    private static final String EMPTY_LAYER = " - ";
    private HashMap<String, FLyrVect> name2LayersMap = new HashMap<String, FLyrVect>();
    private MapContext currentMapcontext;

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
                if (selectedLayerName.equals(EMPTY_LAYER)) {
                    return;
                }

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
        double length = envelope.getLength(0);
        length = Math.max(length, envelope.getLength(1));
        double buffer = length * 0.9;
        try {
            Geometry bufferGeom = defaultGeometry.buffer(buffer);
            envelope = bufferGeom.getEnvelope();

            IProjection dataCrs = feature.getDefaultSRS();

            // reproject to map's crs
            IProjection mapCrs = currentMapcontext.getViewPort().getProjection();
            ICoordTrans coordTrans = dataCrs.getCT(mapCrs);
            Geometry envelopeGeometry = envelope.getGeometry();

            envelopeGeometry.reProject(coordTrans);
            Envelope reprojectedEnvelope = envelopeGeometry.getEnvelope();

            // String bufferStr = _bufferText.getText();
            // double buffer = 0;
            // try {
            // buffer = Double.parseDouble(bufferStr);
            // } catch (NumberFormatException e) {
            // // ignore
            // }
            JGTUtilities.zoomTo(reprojectedEnvelope);

            FeatureSelection featureSelection = selectedLayer.getFeatureStore().getFeatureSelection();
            featureSelection.deselectAll();
            featureSelection.select(feature);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    private void setCombos() {
        try {
            currentMapcontext = ProjectUtilities.getCurrentMapcontext();
            List<FLyrVect> vectorLayers = LayerUtilities.getVectorLayers(currentMapcontext);

            if (vectorLayers.size() == name2LayersMap.size()) {
                return;
            }

            name2LayersMap = new HashMap<String, FLyrVect>();
            String[] names = new String[vectorLayers.size() + 1];
            names[0] = EMPTY_LAYER;
            int count = 1;
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
