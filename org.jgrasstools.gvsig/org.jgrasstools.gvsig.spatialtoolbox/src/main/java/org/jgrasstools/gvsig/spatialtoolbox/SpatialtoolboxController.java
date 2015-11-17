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
package org.jgrasstools.gvsig.spatialtoolbox;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.EventListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.border.EmptyBorder;
import javax.swing.event.EventListenerList;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import org.cresques.cts.IProjection;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.gvsig.andami.IconThemeHelper;
import org.gvsig.fmap.dal.feature.FeatureStore;
import org.gvsig.fmap.mapcontext.MapContext;
import org.gvsig.fmap.mapcontext.layers.vectorial.FLyrVect;
import org.gvsig.fmap.mapcontrol.MapControl;
import org.gvsig.raster.fmap.layers.FLyrRaster;
import org.gvsig.tools.swing.api.Component;
import org.gvsig.tools.swing.api.ToolsSwingLocator;
import org.gvsig.tools.swing.api.windowmanager.WindowManager;
import org.gvsig.tools.swing.api.windowmanager.WindowManager.MODE;
import org.jgrasstools.gears.io.vectorreader.OmsVectorReader;
import org.jgrasstools.gears.libs.monitor.IJGTProgressMonitor;
import org.jgrasstools.gears.libs.monitor.LogProgressMonitor;
import org.jgrasstools.gears.utils.files.FileUtilities;
import org.jgrasstools.gvsig.base.DataUtilities;
import org.jgrasstools.gvsig.base.GtGvsigConversionUtilities;
import org.jgrasstools.gvsig.base.JGTUtilities;
import org.jgrasstools.gvsig.base.LayerUtilities;
import org.jgrasstools.gvsig.base.ProjectUtilities;
import org.jgrasstools.gvsig.base.utils.console.LogConsoleController;
import org.jgrasstools.gvsig.spatialtoolbox.core.JGrasstoolsModulesManager;
import org.jgrasstools.gvsig.spatialtoolbox.core.ModuleDescription;
import org.jgrasstools.gvsig.spatialtoolbox.core.ParametersPanel;
import org.jgrasstools.gvsig.spatialtoolbox.core.ViewerFolder;
import org.jgrasstools.gvsig.spatialtoolbox.core.ViewerModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import oms3.annotations.Execute;
import oms3.annotations.Finalize;
import oms3.annotations.Initialize;

/**
 * The spatialtoolbox view controller.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 *
 */
public class SpatialtoolboxController extends SpatialtoolboxView implements Component {
    private static final Logger logger = LoggerFactory.getLogger(SpatialtoolboxController.class);
    private ParametersPanel pPanel;
    private MapControl mapControl;
    private HashMap<String, FLyrVect> vectorLayerMap;
    private HashMap<String, FLyrRaster> rasterLayerMap;

    public SpatialtoolboxController() {
        setPreferredSize(new Dimension(900, 600));
        init();
    }

    private void init() {
        parametersPanel.setLayout(new BorderLayout());

        addComponentListener(new ComponentListener(){

            public void componentShown( ComponentEvent e ) {
            }

            public void componentResized( ComponentEvent e ) {
            }

            public void componentMoved( ComponentEvent e ) {
            }

            public void componentHidden( ComponentEvent e ) {
                freeResources();
            }
        });

        pPanel = new ParametersPanel();
        mapControl = ProjectUtilities.getCurrentMapcontrol();
        if (mapControl != null) {
            mapControl.addMouseListener(pPanel);
        }
        pPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        JScrollPane scrollpane = new JScrollPane(pPanel);
        scrollpane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollpane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        parametersPanel.add(scrollpane, BorderLayout.CENTER);

        processingRegionButton.addActionListener(new ActionListener(){
            public void actionPerformed( ActionEvent e ) {
            }
        });
        processingRegionButton.setIcon(IconThemeHelper.getImageIcon("processingregion"));

        startButton.addActionListener(new ActionListener(){
            public void actionPerformed( ActionEvent e ) {
                final IJGTProgressMonitor pm = new LogProgressMonitor();
                WindowManager windowManager = ToolsSwingLocator.getWindowManager();
                final LogConsoleController logConsole = new LogConsoleController(pm);
                windowManager.showWindow(logConsole.asJComponent(), "Console Log", MODE.WINDOW);

                new Thread(new Runnable(){
                    public void run() {
                        try {
                            logConsole.beginProcess("RunEpanetExtension");
                            runModule(pm);
                            logConsole.finishProcess();
                            logConsole.stopLogging();
                            // SwingUtilities.invokeLater(new Runnable(){
                            // public void run() {
                            // logConsole.setVisible(false);
                            // }
                            // });
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        });
        startButton.setIcon(IconThemeHelper.getImageIcon("start"));

        runScriptButton.addActionListener(new ActionListener(){
            public void actionPerformed( ActionEvent e ) {
            }
        });
        runScriptButton.setIcon(IconThemeHelper.getImageIcon("run_script"));

        generateScriptButton.addActionListener(new ActionListener(){
            public void actionPerformed( ActionEvent e ) {
            }
        });
        generateScriptButton.setIcon(IconThemeHelper.getImageIcon("generate_script"));

        clearFilterButton.addActionListener(new ActionListener(){
            public void actionPerformed( ActionEvent e ) {
                filterField.setText("");
                layoutTree(false);
            }
        });
        clearFilterButton.setIcon(IconThemeHelper.getImageIcon("trash"));

        loadExperimentalCheckbox.addActionListener(new ActionListener(){
            public void actionPerformed( ActionEvent e ) {

            }
        });
        loadExperimentalCheckbox.setSelected(true);
        loadExperimentalCheckbox.addActionListener(new ActionListener(){
            public void actionPerformed( ActionEvent e ) {
                layoutTree(true);
            }
        });

        filterField.addKeyListener(new KeyAdapter(){
            @Override
            public void keyReleased( KeyEvent e ) {
                layoutTree(true);
            }
        });

        final ImageIcon categoryIcon = IconThemeHelper.getImageIcon("category");
        final ImageIcon moduleIcon = IconThemeHelper.getImageIcon("module");
        final ImageIcon moduleExpIcon = IconThemeHelper.getImageIcon("module_exp");

        try {
            modulesTree.setCellRenderer(new DefaultTreeCellRenderer(){
                @Override
                public java.awt.Component getTreeCellRendererComponent( JTree tree, Object value, boolean selected,
                        boolean expanded, boolean leaf, int row, boolean hasFocus ) {

                    super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
                    if (value instanceof Modules) {
                        setIcon(categoryIcon);
                    } else if (value instanceof ViewerFolder) {
                        setIcon(categoryIcon);
                    } else if (value instanceof ViewerModule) {
                        if (isExperimental(value)) {
                            setIcon(moduleExpIcon);
                        } else {
                            setIcon(moduleIcon);
                        }
                    }
                    return this;
                }

                private boolean isExperimental( Object node ) {
                    if (node instanceof ViewerModule) {
                        ViewerModule module = (ViewerModule) node;
                        ModuleDescription md = module.getModuleDescription();
                        if (md.getStatus() == ModuleDescription.Status.experimental) {
                            return true;
                        }
                    }
                    return false;
                }
            });

            modulesTree.addTreeSelectionListener(new TreeSelectionListener(){
                public void valueChanged( TreeSelectionEvent evt ) {
                    TreePath[] paths = evt.getPaths();

                    for( int i = 0; i < paths.length; i++ ) {
                        Object lastPathComponent = paths[i].getLastPathComponent();
                        if (lastPathComponent instanceof ViewerModule) {
                            ViewerModule module = (ViewerModule) lastPathComponent;
                            ModuleDescription moduleDescription = module.getModuleDescription();
                            pPanel.setModule(moduleDescription);

                            // SwingUtilities.invokeLater(new Runnable(){
                            // public void run() {
                            // parametersPanel.invalidate();
                            parametersPanel.validate();
                            parametersPanel.repaint();
                            // }
                            // });
                            break;
                        }
                        if (lastPathComponent instanceof ViewerFolder) {
                            pPanel.setModule(null);
                            parametersPanel.validate();
                            parametersPanel.repaint();
                            break;
                        }
                    }
                }
            });

            layoutTree(false);
        } catch (Exception e1) {
            logger.error("Error", e1);
        }
    }

    private void layoutTree( boolean expandNodes ) {
        TreeMap<String, List<ModuleDescription>> availableModules = JGrasstoolsModulesManager.getInstance().getModulesMap();

        final List<ViewerFolder> viewerFolders = ViewerFolder.hashmap2ViewerFolders(availableModules, filterField.getText(),
                loadExperimentalCheckbox.isSelected());
        Modules modules = new Modules();
        modules.viewerFolders = viewerFolders;
        ObjectTreeModel model = new ObjectTreeModel();
        model.setRoot(modules);
        modulesTree.setModel(model);

        if (expandNodes)
            expandAllNodes(modulesTree, 0, modulesTree.getRowCount());
    }

    private void expandAllNodes( JTree tree, int startingIndex, int rowCount ) {
        for( int i = startingIndex; i < rowCount; ++i ) {
            tree.expandRow(i);
        }

        if (tree.getRowCount() != rowCount) {
            expandAllNodes(tree, rowCount, tree.getRowCount());
        }
    }

    class Modules {
        List<ViewerFolder> viewerFolders;
        @Override
        public String toString() {
            if (viewerFolders != null && !viewerFolders.isEmpty()) {
                return "Modules";
            } else {
                return "No modules found";
            }
        }
    }

    class ObjectTreeModel implements TreeModel {

        private Modules root;
        private EventListenerList listenerList = new EventListenerList();
        /**
        * Constructs an empty tree.
        */
        public ObjectTreeModel() {
            root = null;
        }

        /**
        * Sets the root to a given variable.
        * @param v the variable that is being described by this tree
        */
        public void setRoot( Modules v ) {
            Modules oldRoot = v;
            root = v;
            fireTreeStructureChanged(oldRoot);
        }

        public Object getRoot() {
            return root;
        }

        public int getChildCount( Object parent ) {
            if (parent instanceof Modules) {
                Modules modules = (Modules) parent;
                return modules.viewerFolders.size();
            } else if (parent instanceof ViewerFolder) {
                ViewerFolder folder = (ViewerFolder) parent;
                return folder.getModules().size() + folder.getSubFolders().size();
            } else if (parent instanceof List) {
                List list = (List) parent;
                return list.size();
            }
            return 0;
        }

        public Object getChild( Object parent, int index ) {
            if (parent instanceof Modules) {
                Modules modules = (Modules) parent;
                return modules.viewerFolders.get(index);
            } else if (parent instanceof ViewerFolder) {
                ViewerFolder folder = (ViewerFolder) parent;
                int modulesSize = folder.getModules().size();
                if (index < modulesSize) {
                    return folder.getModules().get(index);
                } else {
                    index = index - modulesSize;
                    return folder.getSubFolders().get(index);
                }
            } else if (parent instanceof List) {
                List list = (List) parent;
                Object item = list.get(index);
                return item;
            }
            return null;
        }

        public int getIndexOfChild( Object parent, Object child ) {
            int n = getChildCount(parent);
            for( int i = 0; i < n; i++ )
                if (getChild(parent, i).equals(child))
                    return i;
            return -1;
        }

        public boolean isLeaf( Object node ) {
            return getChildCount(node) == 0;
        }

        public void valueForPathChanged( TreePath path, Object newValue ) {
        }

        public void addTreeModelListener( TreeModelListener l ) {
            listenerList.add(TreeModelListener.class, l);
        }

        public void removeTreeModelListener( TreeModelListener l ) {
            listenerList.remove(TreeModelListener.class, l);
        }

        protected void fireTreeStructureChanged( Object oldRoot ) {
            TreeModelEvent event = new TreeModelEvent(this, new Object[]{oldRoot});
            EventListener[] listeners = listenerList.getListeners(TreeModelListener.class);
            for( int i = 0; i < listeners.length; i++ )
                ((TreeModelListener) listeners[i]).treeStructureChanged(event);
        }

    }

    public JComponent asJComponent() {
        return this;
    }

    public void isVisibleTriggered() {
        vectorLayerMap = new HashMap<String, FLyrVect>();
        rasterLayerMap = new HashMap<String, FLyrRaster>();

        MapContext currentMapcontext = ProjectUtilities.getCurrentMapcontext();
        List<FLyrVect> vectorLayers = LayerUtilities.getVectorLayers(currentMapcontext);
        String[] vectorNames = new String[vectorLayers.size()];
        for( int i = 0; i < vectorNames.length; i++ ) {
            FLyrVect fLyrVect = vectorLayers.get(i);
            vectorNames[i] = fLyrVect.getName();
            vectorLayerMap.put(vectorNames[i], fLyrVect);
        }
        List<FLyrRaster> rasterLayers = LayerUtilities.getRasterLayers(currentMapcontext);
        String[] rasterNames = new String[rasterLayers.size()];
        for( int i = 0; i < rasterNames.length; i++ ) {
            FLyrRaster fLyrRaster = rasterLayers.get(i);
            rasterNames[i] = fLyrRaster.getName();
            rasterLayerMap.put(rasterNames[i], fLyrRaster);
        }
        pPanel.setVectorRasterLayers(vectorNames, rasterNames);

        if (mapControl != null) {
            mapControl.removeMouseListener(pPanel);
            mapControl = ProjectUtilities.getCurrentMapcontrol();
            if (mapControl != null) {
                mapControl.addMouseListener(pPanel);
            }
        }
    }

    private void freeResources() {
        if (mapControl != null)
            mapControl.removeMouseListener(pPanel);
        if (pPanel != null)
            pPanel.freeResources();
    }

    private void runModule( IJGTProgressMonitor pm ) throws Exception {
        ModuleDescription module = pPanel.getModule();
        HashMap<String, Object> fieldName2ValueHolderMap = pPanel.getFieldName2ValueHolderMap();

        Class< ? > moduleClass = module.getModuleClass();
        Object newInstance = moduleClass.newInstance();
        for( Entry<String, Object> entry : fieldName2ValueHolderMap.entrySet() ) {
            try {
                String value = stringFromObject(entry.getValue());
                String fieldName = entry.getKey();
                Field field = moduleClass.getField(fieldName);
                field.setAccessible(true);
                Class< ? > type = field.getType();
                if (type.isAssignableFrom(String.class)) {
                    field.set(newInstance, value);
                } else if (type.isAssignableFrom(double.class)) {
                    field.set(newInstance, Double.parseDouble(value));
                } else if (type.isAssignableFrom(Double.class)) {
                    field.set(newInstance, new Double(value));
                } else if (type.isAssignableFrom(int.class)) {
                    field.set(newInstance, (int) Double.parseDouble(value));
                } else if (type.isAssignableFrom(Integer.class)) {
                    field.set(newInstance, new Integer((int) Double.parseDouble(value)));
                } else if (type.isAssignableFrom(long.class)) {
                    field.set(newInstance, (long) Double.parseDouble(value));
                } else if (type.isAssignableFrom(Long.class)) {
                    field.set(newInstance, new Long((long) Double.parseDouble(value)));
                } else if (type.isAssignableFrom(float.class)) {
                    field.set(newInstance, (float) Double.parseDouble(value));
                } else if (type.isAssignableFrom(Float.class)) {
                    field.set(newInstance, new Float((float) Double.parseDouble(value)));
                } else if (type.isAssignableFrom(short.class)) {
                    field.set(newInstance, (short) Double.parseDouble(value));
                } else if (type.isAssignableFrom(Short.class)) {
                    field.set(newInstance, new Short((short) Double.parseDouble(value)));
                } else if (type.isAssignableFrom(boolean.class)) {
                    field.set(newInstance, (boolean) Boolean.parseBoolean(value));
                } else if (type.isAssignableFrom(Boolean.class)) {
                    field.set(newInstance, new Boolean(value));
                } else {
                    logger.error("NOT SUPPORTED TYPE: " + type);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // set progress monitor
        Field pmField = moduleClass.getField("pm");
        pmField.setAccessible(true);
        pmField.set(newInstance, pm);

        // run the methods annotated
        Method initMethod = getMethodAnnotatedWith(moduleClass, Initialize.class);
        if (initMethod != null) {
            initMethod.invoke(newInstance);
        }
        Method execMethod = getMethodAnnotatedWith(moduleClass, Execute.class);
        execMethod.invoke(newInstance);
        Method finishMethod = getMethodAnnotatedWith(moduleClass, Finalize.class);
        if (finishMethod != null) {
            finishMethod.invoke(newInstance);
        }

        // finished, try to load results
        List<String> outputFieldNames = pPanel.getOutputFieldNames();
        for( String outputField : outputFieldNames ) {
            try {
                Field field = moduleClass.getField(outputField);
                if (field.getType().isAssignableFrom(String.class)) {
                    String value = field.get(newInstance).toString();
                    File file = new File(value);
                    if (file.exists()) {
                        if (DataUtilities.isSupportedVectorExtension(value)) {
                            // FIXME remove once CRS is supported in GVSIG
                            ReferencedEnvelope readEnvelope = OmsVectorReader.readEnvelope(file.getAbsolutePath());
                            IProjection proj = GtGvsigConversionUtilities
                                    .gtCrs2gvsigCrs(readEnvelope.getCoordinateReferenceSystem());
                            FeatureStore featureStore = DataUtilities.readShapefileDatastore(file, proj.getAbrev());
                            String nameWithoutExtention = FileUtilities.getNameWithoutExtention(file);
                            LayerUtilities.loadFeatureStore2Layer(featureStore, nameWithoutExtention);
                        } else if (DataUtilities.isSupportedRasterExtension(value)) {
                            String nameWithoutExtention = FileUtilities.getNameWithoutExtention(file);
                            LayerUtilities.loadRasterFile2Layer(file, nameWithoutExtention);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    public static Method getMethodAnnotatedWith( final Class< ? > klass, Class< ? extends Annotation> annotation ) {
        Method[] allMethods = klass.getDeclaredMethods();
        for( final Method method : allMethods ) {
            if (method.isAnnotationPresent(annotation)) {
                return method;
            }
        }
        return null;
    }

    private String stringFromObject( Object value ) throws Exception {
        if (value instanceof JTextField) {
            JTextField tf = (JTextField) value;
            return tf.getText();
        } else if (value instanceof JTextArea) {
            JTextArea tf = (JTextArea) value;
            return tf.getText();
        } else if (value instanceof JCheckBox) {
            JCheckBox tf = (JCheckBox) value;
            return tf.isSelected() ? "true" : "false";
        } else if (value instanceof JComboBox) {
            JComboBox tf = (JComboBox) value;
            String comboItem = tf.getSelectedItem().toString();
            // check if it is a layer first
            FLyrVect fLyrVect = vectorLayerMap.get(comboItem);
            if (fLyrVect != null) {
                File file = LayerUtilities.getFileFromVectorFileLayer(fLyrVect);
                if (file != null && file.exists()) {
                    return file.getAbsolutePath();
                }
            } else {
                FLyrRaster fLyrRaster = rasterLayerMap.get(comboItem);
                if (fLyrRaster != null) {
                    File file = LayerUtilities.getFileFromRasterFileLayer(fLyrRaster);
                    if (file != null && file.exists()) {
                        return file.getAbsolutePath();
                    }
                }
            }
            return comboItem;
        }
        return null;
    }
}
