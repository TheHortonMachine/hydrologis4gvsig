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
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.EventListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
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
import org.gvsig.tools.dynobject.DynObject;
import org.gvsig.tools.swing.api.Component;
import org.gvsig.tools.swing.api.ToolsSwingLocator;
import org.gvsig.tools.swing.api.threadsafedialogs.ThreadSafeDialogsManager;
import org.gvsig.tools.swing.api.windowmanager.WindowManager;
import org.gvsig.tools.swing.api.windowmanager.WindowManager.MODE;
import org.jgrasstools.gears.io.geopaparazzi.geopap4.TimeUtilities;
import org.jgrasstools.gears.io.vectorreader.OmsVectorReader;
import org.jgrasstools.gears.libs.monitor.IJGTProgressMonitor;
import org.jgrasstools.gears.utils.files.FileUtilities;
import org.jgrasstools.gvsig.base.DataUtilities;
import org.jgrasstools.gvsig.base.GtGvsigConversionUtilities;
import org.jgrasstools.gvsig.base.JGTUtilities;
import org.jgrasstools.gvsig.base.LayerUtilities;
import org.jgrasstools.gvsig.base.ProjectUtilities;
import org.jgrasstools.gvsig.base.utils.console.ProcessLogConsoleController;
import org.jgrasstools.gvsig.spatialtoolbox.core.JGrasstoolsModulesManager;
import org.jgrasstools.gvsig.spatialtoolbox.core.ModuleDescription;
import org.jgrasstools.gvsig.spatialtoolbox.core.ParametersPanel;
import org.jgrasstools.gvsig.spatialtoolbox.core.SpatialToolboxConstants;
import org.jgrasstools.gvsig.spatialtoolbox.core.ViewerFolder;
import org.jgrasstools.gvsig.spatialtoolbox.core.ViewerModule;
import org.jgrasstools.gvsig.spatialtoolbox.core.exec.StageScriptExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private ThreadSafeDialogsManager dialogManager = ToolsSwingLocator.getThreadSafeDialogsManager();
    private DynObject preferences;
    private HashMap<String, String> prefsMap = new HashMap<>();

    private final String SPATIAL_TOOLBOX_PREFERENCES_KEY = "SPATIAL_TOOLBOX_PREFERENCES";
    private final String HEAP_KEY = "heap";
    private final String DEBUG_KEY = "debug";

    public SpatialtoolboxController() {
        setPreferredSize(new Dimension(900, 600));

        preferences = ProjectUtilities.getPluginPreferences(SpatialtoolboxExtension.class);
        Object prefsMapTmp = preferences.getDynValue(SPATIAL_TOOLBOX_PREFERENCES_KEY);
        if (prefsMapTmp != null) {
            prefsMap = (HashMap) prefsMapTmp;
        }

        init();
    }

    @SuppressWarnings({"unchecked", "serial"})
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

        // TODO enable when used
        processingRegionButton.setVisible(false);

        startButton.setToolTipText("Start the current module.");
        startButton.addActionListener(new ActionListener(){
            public void actionPerformed( ActionEvent e ) {

                WindowManager windowManager = ToolsSwingLocator.getWindowManager();
                final ProcessLogConsoleController logConsole = new ProcessLogConsoleController();
                windowManager.showWindow(logConsole.asJComponent(), "Console Log", MODE.WINDOW);

                try {
                    runModuleInNewJVM(logConsole);
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        });
        startButton.setIcon(IconThemeHelper.getImageIcon("start"));

        runScriptButton.setToolTipText("Run a script from file.");
        runScriptButton.addActionListener(new ActionListener(){
            public void actionPerformed( ActionEvent e ) {
                File[] loadFiles = dialogManager.showOpenFileDialog("Load script", JGTUtilities.getLastFile());
                if (loadFiles != null && loadFiles.length > 0) {
                    try {
                        JGTUtilities.setLastPath(loadFiles[0].getAbsolutePath());
                        String readFile = FileUtilities.readFile(loadFiles[0]);

                        WindowManager windowManager = ToolsSwingLocator.getWindowManager();
                        final ProcessLogConsoleController logConsole = new ProcessLogConsoleController();
                        windowManager.showWindow(logConsole.asJComponent(), "Console Log", MODE.WINDOW);

                        StageScriptExecutor exec = new StageScriptExecutor();
                        exec.addProcessListener(logConsole);

                        String logLevel = debugCheckbox.isSelected()
                                ? SpatialToolboxConstants.LOGLEVEL_GUI_ON
                                : SpatialToolboxConstants.LOGLEVEL_GUI_OFF;
                        String ramLevel = heapCombo.getSelectedItem().toString();

                        String sessionId = "File: " + loadFiles[0].getName() + " - "
                                + TimeUtilities.INSTANCE.TIMESTAMPFORMATTER_LOCAL.format(new Date());
                        Process process = exec.exec(sessionId, readFile, logLevel, ramLevel, null);
                        logConsole.beginProcess(process, sessionId);

                    } catch (Exception e1) {
                        e1.printStackTrace();
                        dialogManager.messageDialog("ERROR", "an error occurred while running the script: " + e1.getMessage(),
                                JOptionPane.ERROR_MESSAGE);
                    }
                }

            }
        });
        runScriptButton.setIcon(IconThemeHelper.getImageIcon("run_script"));

        generateScriptButton.setToolTipText("Save the current module as a script to file.");
        generateScriptButton.addActionListener(new ActionListener(){
            public void actionPerformed( ActionEvent e ) {
                ModuleDescription module = pPanel.getModule();
                HashMap<String, Object> fieldName2ValueHolderMap = pPanel.getFieldName2ValueHolderMap();
                List<String> outputFieldNames = pPanel.getOutputFieldNames();
                final HashMap<String, String> outputStringsMap = new HashMap<>();
                Class< ? > moduleClass = module.getModuleClass();
                StringBuilder scriptBuilder = getScript(fieldName2ValueHolderMap, outputFieldNames, outputStringsMap,
                        moduleClass);

                File[] saveFiles = dialogManager.showSaveFileDialog("Save script", JGTUtilities.getLastFile());
                if (saveFiles != null && saveFiles.length > 0) {
                    try {
                        JGTUtilities.setLastPath(saveFiles[0].getAbsolutePath());
                        FileUtilities.writeFile(scriptBuilder.toString(), saveFiles[0]);
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
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

        loadExperimentalCheckbox.setSelected(true);
        loadExperimentalCheckbox.addActionListener(new ActionListener(){
            public void actionPerformed( ActionEvent e ) {
                layoutTree(true);
            }
        });

        boolean doDebug = false;
        String debugStr = prefsMap.get(DEBUG_KEY);
        if (debugStr != null && debugStr.trim().length() > 0) {
            doDebug = Boolean.parseBoolean(debugStr);
        }
        debugCheckbox.setSelected(doDebug);
        heapCombo.setModel(new DefaultComboBoxModel<>(SpatialToolboxConstants.HEAPLEVELS));
        String heapStr = prefsMap.get(HEAP_KEY);
        heapCombo.setSelectedItem(heapStr);

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

        if (mapControl == null) {
            mapControl = ProjectUtilities.getCurrentMapcontrol();
            if (mapControl != null) {
                mapControl.addMouseListener(pPanel);
            }
        } else {
            mapControl.removeMouseListener(pPanel);
            mapControl = ProjectUtilities.getCurrentMapcontrol();
            if (mapControl != null) {
                mapControl.addMouseListener(pPanel);
            }
        }
    }

    private void freeResources() {
        String ramLevel = heapCombo.getSelectedItem().toString();
        prefsMap.put(DEBUG_KEY, debugCheckbox.isSelected() + "");
        prefsMap.put(HEAP_KEY, ramLevel);
        preferences.setDynValue(SPATIAL_TOOLBOX_PREFERENCES_KEY, prefsMap);

        if (mapControl != null)
            mapControl.removeMouseListener(pPanel);
        if (pPanel != null)
            pPanel.freeResources();
    }

    private void runModuleInNewJVM( ProcessLogConsoleController logConsole ) throws Exception {
        ModuleDescription module = pPanel.getModule();
        HashMap<String, Object> fieldName2ValueHolderMap = pPanel.getFieldName2ValueHolderMap();
        List<String> outputFieldNames = pPanel.getOutputFieldNames();
        final HashMap<String, String> outputStringsMap = new HashMap<>();
        Class< ? > moduleClass = module.getModuleClass();

        StringBuilder scriptBuilder = getScript(fieldName2ValueHolderMap, outputFieldNames, outputStringsMap, moduleClass);

        StageScriptExecutor exec = new StageScriptExecutor();
        exec.addProcessListener(logConsole);

        Runnable finishRunnable = new Runnable(){
            public void run() {
                // finished, try to load results
                for( Entry<String, String> outputStringFieldEntry : outputStringsMap.entrySet() ) {
                    try {
                        String value = outputStringFieldEntry.getValue();
                        File file = new File(value);
                        if (file.exists()) {
                            if (DataUtilities.isSupportedVectorExtension(value)) {
                                // FIXME remove once CRS is supported in GVSIG
                                ReferencedEnvelope readEnvelope = OmsVectorReader.readEnvelope(file.getAbsolutePath());
                                String epsgCode = GtGvsigConversionUtilities
                                        .gtCrs2Epsg(readEnvelope.getCoordinateReferenceSystem());
                                FeatureStore featureStore = DataUtilities.readShapefileDatastore(file, epsgCode);
                                String nameWithoutExtention = FileUtilities.getNameWithoutExtention(file);
                                LayerUtilities.loadFeatureStore2Layer(featureStore, nameWithoutExtention);
                            } else if (DataUtilities.isSupportedRasterExtension(value)) {
                                String nameWithoutExtention = FileUtilities.getNameWithoutExtention(file);
                                LayerUtilities.loadRasterFile2Layer(file, nameWithoutExtention);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        logConsole.addFinishRunnable(finishRunnable);

        String logLevel = debugCheckbox.isSelected()
                ? SpatialToolboxConstants.LOGLEVEL_GUI_ON
                : SpatialToolboxConstants.LOGLEVEL_GUI_OFF;
        String ramLevel = heapCombo.getSelectedItem().toString();

        String sessionId = moduleClass.getSimpleName() + " " + TimeUtilities.INSTANCE.TIMESTAMPFORMATTER_LOCAL.format(new Date());
        Process process = exec.exec(sessionId, scriptBuilder.toString(), logLevel, ramLevel, null);
        logConsole.beginProcess(process, sessionId);
    }

    private StringBuilder getScript( HashMap<String, Object> fieldName2ValueHolderMap, List<String> outputFieldNames,
            final HashMap<String, String> outputStringsMap, Class< ? > moduleClass ) {
        String canonicalName = moduleClass.getCanonicalName();
        String simpleName = moduleClass.getSimpleName();
        String objectName = "_" + simpleName.toLowerCase();

        StringBuilder scriptBuilder = new StringBuilder();
        // TODO check if this is ok
        // scriptBuilder.append("import " + StageScriptExecutor.ORG_JGRASSTOOLS_MODULES +
        // ".*;\n\n");
        // scriptBuilder.append("import " + canonicalName + ";\n\n");

        scriptBuilder.append(canonicalName).append(" ").append(objectName).append(" = new ").append(canonicalName)
                .append("();\n");

        for( Entry<String, Object> entry : fieldName2ValueHolderMap.entrySet() ) {
            try {
                String fieldName = entry.getKey();
                String value = stringFromObject(entry.getValue());

                if (value.trim().length() == 0) {
                    continue;
                }

                scriptBuilder.append(objectName).append(".").append(fieldName).append(" = ");

                Field field = moduleClass.getField(fieldName);
                field.setAccessible(true);
                Class< ? > type = field.getType();
                if (type.isAssignableFrom(String.class)) {

                    scriptBuilder.append("\"").append(value).append("\"");
                    if (outputFieldNames.contains(fieldName)) {
                        outputStringsMap.put(fieldName, value);
                    }
                } else if (type.isAssignableFrom(double.class)) {
                    scriptBuilder.append(value);
                } else if (type.isAssignableFrom(Double.class)) {
                    scriptBuilder.append(value);
                } else if (type.isAssignableFrom(int.class)) {
                    scriptBuilder.append(value);
                } else if (type.isAssignableFrom(Integer.class)) {
                    scriptBuilder.append(value);
                } else if (type.isAssignableFrom(long.class)) {
                    scriptBuilder.append(value);
                } else if (type.isAssignableFrom(Long.class)) {
                    scriptBuilder.append(value);
                } else if (type.isAssignableFrom(float.class)) {
                    scriptBuilder.append(value);
                } else if (type.isAssignableFrom(Float.class)) {
                    scriptBuilder.append(value);
                } else if (type.isAssignableFrom(short.class)) {
                    scriptBuilder.append(value);
                } else if (type.isAssignableFrom(Short.class)) {
                    scriptBuilder.append(value);
                } else if (type.isAssignableFrom(boolean.class)) {
                    scriptBuilder.append(value);
                } else if (type.isAssignableFrom(Boolean.class)) {
                    scriptBuilder.append(value);
                } else {
                    logger.error("NOT SUPPORTED TYPE: " + type);
                }
                scriptBuilder.append(";\n");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        scriptBuilder.append(objectName).append(".process();\n");
        // dumpSimpleOutputs(module, scriptBuilder);

        return scriptBuilder;
    }

    // private void dumpSimpleOutputs( ModuleDescription module, StringBuilder scriptSb ) {
    // scriptSb.append("println \"\"\n");
    // scriptSb.append("println \"\"\n");
    //
    // // make print whatever is simple output
    // String mainVarName = variableNamesMap.get(mainModuleDescription);
    // List<FieldData> outputsList = mainModuleDescription.getOutputsList();
    // for( FieldData fieldData : outputsList ) {
    // if (fieldData.isSimpleType()) {
    // String varPlusField = mainVarName + "." + fieldData.fieldName;
    // // String ifString = "if( " + varPlusField + " != null )\n";
    // // scriptSb.append(ifString);
    // scriptSb.append("println \"");
    // String fieldDescription = fieldData.fieldDescription.trim();
    // if (fieldDescription.endsWith(".")) {
    // fieldDescription = fieldDescription.substring(0, fieldDescription.length() - 1);
    // }
    // scriptSb.append(fieldDescription);
    // scriptSb.append(" = \" + ");
    // scriptSb.append(varPlusField);
    // scriptSb.append("\n");
    // }
    // }
    //
    // // in case make print double[] and double[][] outputs
    // scriptSb.append("println \"\"\n\n");
    // for( FieldData fieldData : outputsList ) {
    // String varPlusField = mainVarName + "." + fieldData.fieldName;
    // if (fieldData.isSimpleArrayType()) {
    // if (fieldData.fieldType.equals(double[][].class.getCanonicalName())
    // || fieldData.fieldType.equals(float[][].class.getCanonicalName())
    // || fieldData.fieldType.equals(int[][].class.getCanonicalName())) {
    //
    // String ifString = "if( " + varPlusField + " != null ) {\n";
    // scriptSb.append(ifString);
    // String typeStr = null;
    // if (fieldData.fieldType.equals(double[][].class.getCanonicalName())) {
    // typeStr = "double[][]";
    // } else if (fieldData.fieldType.equals(float[][].class.getCanonicalName())) {
    // typeStr = "float[][]";
    // } else if (fieldData.fieldType.equals(int[][].class.getCanonicalName())) {
    // typeStr = "int[][]";
    // }
    //
    // scriptSb.append("println \"");
    // scriptSb.append(fieldData.fieldDescription);
    // scriptSb.append("\"\n");
    // scriptSb.append("println \"-----------------------------------\"\n");
    // scriptSb.append(typeStr);
    // scriptSb.append(" matrix = ");
    // scriptSb.append(varPlusField);
    // scriptSb.append("\n");
    //
    // scriptSb.append("for( int i = 0; i < matrix.length; i++ ) {\n");
    // scriptSb.append("for( int j = 0; j < matrix[0].length; j++ ) {\n");
    // scriptSb.append("print matrix[i][j] + \" \";\n");
    // scriptSb.append("}\n");
    // scriptSb.append("println \" \";\n");
    // scriptSb.append("}\n");
    // scriptSb.append("}\n");
    // scriptSb.append("\n");
    // } else if (fieldData.fieldType.equals(double[].class.getCanonicalName())
    // || fieldData.fieldType.equals(float[].class.getCanonicalName())
    // || fieldData.fieldType.equals(int[].class.getCanonicalName())) {
    //
    // String ifString = "if( " + varPlusField + " != null ) {\n";
    // scriptSb.append(ifString);
    //
    // String typeStr = null;
    // if (fieldData.fieldType.equals(double[].class.getCanonicalName())) {
    // typeStr = "double[]";
    // } else if (fieldData.fieldType.equals(float[].class.getCanonicalName())) {
    // typeStr = "float[]";
    // } else if (fieldData.fieldType.equals(int[].class.getCanonicalName())) {
    // typeStr = "int[]";
    // }
    // scriptSb.append("println \"");
    // scriptSb.append(fieldData.fieldDescription);
    // scriptSb.append("\"\n");
    // scriptSb.append("println \"-----------------------------------\"\n");
    // scriptSb.append(typeStr);
    // scriptSb.append(" array = ");
    // scriptSb.append(mainVarName);
    // scriptSb.append(".");
    // scriptSb.append(fieldData.fieldName);
    // scriptSb.append("\n");
    //
    // scriptSb.append("for( int i = 0; i < array.length; i++ ) {\n");
    // scriptSb.append("println array[i] + \" \";\n");
    // scriptSb.append("}\n");
    // scriptSb.append("}\n");
    // scriptSb.append("\n");
    // }
    // scriptSb.append("println \" \"\n\n");
    // }
    // }
    // }

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
