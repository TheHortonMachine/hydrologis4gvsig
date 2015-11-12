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
package org.jgrasstools.gvsig.geopaparazzi;

import java.io.File;

import javax.swing.JOptionPane;

import org.gvsig.andami.IconThemeHelper;
import org.gvsig.andami.plugins.Extension;
import org.gvsig.andami.ui.mdiManager.IWindow;
import org.gvsig.app.ApplicationLocator;
import org.gvsig.app.ApplicationManager;
import org.gvsig.app.project.ProjectManager;
import org.gvsig.app.project.documents.Document;
import org.gvsig.app.project.documents.view.ViewDocument;
import org.gvsig.app.project.documents.view.gui.IView;
import org.gvsig.fmap.dal.feature.Feature;
import org.gvsig.fmap.dal.feature.FeatureAttributeDescriptor;
import org.gvsig.fmap.dal.feature.FeatureQuery;
import org.gvsig.fmap.dal.feature.FeatureSet;
import org.gvsig.fmap.dal.feature.FeatureStore;
import org.gvsig.fmap.dal.feature.FeatureType;
import org.gvsig.fmap.geom.primitive.Envelope;
import org.gvsig.fmap.mapcontext.MapContext;
import org.gvsig.fmap.mapcontext.layers.FLayer;
import org.gvsig.fmap.mapcontext.layers.FLayers;
import org.gvsig.fmap.mapcontext.layers.vectorial.FLyrVect;
import org.gvsig.fmap.mapcontrol.MapControl;
import org.gvsig.fmap.mapcontrol.tools.BehaviorException;
import org.gvsig.fmap.mapcontrol.tools.Behavior.RectangleBehavior;
import org.gvsig.fmap.mapcontrol.tools.Events.EnvelopeEvent;
import org.gvsig.fmap.mapcontrol.tools.Listeners.AbstractToolListener;
import org.gvsig.fmap.mapcontrol.tools.Listeners.RectangleListener;
import org.gvsig.tools.dispose.DisposableIterator;
import org.gvsig.tools.swing.api.ToolsSwingLocator;
import org.gvsig.tools.swing.api.threadsafedialogs.ThreadSafeDialogsManager;
import org.jgrasstools.gvsig.base.JGTUtilities;
import org.jgrasstools.gvsig.base.LayerUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 *
 */
public class GeopaparazziMediaOpenerExtension extends Extension {

    private static final Logger logger = LoggerFactory.getLogger(GeopaparazziMediaOpenerExtension.class);
    private static final String MEDIA_TEXT_FIELD = "text";
    private static final String TOOL_NAME = "Geopaparazzi.mediaopentool";
    private static final String ACTION_MEDIAINFO_TOOL = "gpap-media-open-tool";

    private ApplicationManager applicationManager;

    private ProjectManager projectManager;

    private ThreadSafeDialogsManager dialogManager;

    private FeatureStore dataStore;

    private File mediaFolder;

    public void initialize() {
        // PluginsManager manager = PluginsLocator.getManager();
        IconThemeHelper.registerIcon("action", "picture_link", this);

        applicationManager = ApplicationLocator.getManager();

        projectManager = applicationManager.getProjectManager();
        dialogManager = ToolsSwingLocator.getThreadSafeDialogsManager();
    }

    public void postInitialize() {
    }

    /**
     * Execute the actions associated to this extension.
     */
    public void execute( String actionCommand ) {
        if (ACTION_MEDIAINFO_TOOL.equalsIgnoreCase(actionCommand)) {
            try {
                Document activeDocument = projectManager.getCurrentProject().getActiveDocument();
                if (activeDocument == null) {
                    return;
                }
                IWindow activeWindow = applicationManager.getActiveWindow();
                IView view = null;
                if (activeWindow instanceof IView) {
                    view = (IView) activeWindow;
                } else {
                    return;
                }

                MapControl mapControl = view.getMapControl();
                ViewDocument viewDoc = (ViewDocument) activeDocument;
                MapContext mapContext = viewDoc.getMapContext();
                FLayers layers = mapContext.getLayers();

                String mediaLayerPath = null;
                int layersCount = layers.getLayersCount();
                for( int i = 0; i < layersCount; i++ ) {
                    FLayer layer = layers.getLayer(i);
                    if (layer == null)
                        continue;
                    if (layer instanceof FLyrVect) {
                        FLyrVect vectorLayer = (FLyrVect) layer;
                        if (vectorLayer.getName().equals(GeopaparazziLayerWizard.MEDIA_NOTES)) {
                            File vectorFile = LayerUtilities.getFileFromVectorFileLayer(vectorLayer);
                            mediaLayerPath = vectorFile.getAbsolutePath();
                            dataStore = (FeatureStore) vectorLayer.getDataStore();
                            break;
                        }
                    }
                }

                if (mediaLayerPath == null || !new File(mediaLayerPath).exists()) {
                    dialogManager.messageDialog("Could not find the Geopaparazzi media layer.", "WARNING",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }

                File mediaShapefile = new File(mediaLayerPath);
                mediaFolder = mediaShapefile.getParentFile();

                FeatureType defaultFeatureType = dataStore.getDefaultFeatureType();
                FeatureAttributeDescriptor attributeDescriptor = defaultFeatureType.getAttributeDescriptor(MEDIA_TEXT_FIELD);
                if (attributeDescriptor == null) {
                    dialogManager.messageDialog("The media layer doesn't have the mandatory " + MEDIA_TEXT_FIELD + " field.",
                            "WARNING", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                GeopapMediaListener listener = new GeopapMediaListener();
                mapControl.addBehavior(TOOL_NAME, new RectangleBehavior(listener));
                mapControl.setTool(TOOL_NAME);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private class GeopapMediaListener extends AbstractToolListener implements RectangleListener {

        public void rectangle( EnvelopeEvent event ) throws BehaviorException {
            Envelope rect = event.getWorldCoordRect();
            FeatureSet set = null;
            DisposableIterator it = null;

            try {
                String attrGeomName = dataStore.getDefaultFeatureType().getDefaultGeometryAttributeName();
                FeatureQuery query = dataStore.createFeatureQuery();
                query.setFilter(new EnvelopeIntersectionEvaluator(attrGeomName, rect));
                set = dataStore.getFeatureSet(query);
                if (set.isEmpty()) {
                    return;
                }
                it = set.fastIterator();
                while( it.hasNext() ) {
                    Feature f = (Feature) it.next();
                    Object mediaObj = f.get(MEDIA_TEXT_FIELD);
                    if (mediaObj instanceof String) {
                        String mediaString = (String) mediaObj;
                        if (mediaString.startsWith("media")) {
                            String[] imagesSplit = mediaString.split(";");
                            for( String imageRel : imagesSplit ) {
                                File imageFile = new File(mediaFolder, imageRel);
                                if (imageFile.exists()) {
                                    JGTUtilities.openFile(imageFile);
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (it != null) {
                    it.dispose();
                }
                if (set != null) {
                    set.dispose();
                }
            }
        }
    }

    /**
     * Check if tools of this extension are enabled.
     */
    public boolean isEnabled() {
        return true;
    }

    /**
     * Check if tools of this extension are visible.
     */
    public boolean isVisible() {
        return true;
    }

}
