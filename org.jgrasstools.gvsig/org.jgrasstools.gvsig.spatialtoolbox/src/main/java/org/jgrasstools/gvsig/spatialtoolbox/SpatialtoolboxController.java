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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Enumeration;
import java.util.EventListener;
import java.util.List;
import java.util.TreeMap;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.event.EventListenerList;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import org.gvsig.andami.IconThemeHelper;
import org.gvsig.tools.swing.api.Component;
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

    public SpatialtoolboxController() {
        setPreferredSize(new Dimension(800, 500));
        init();
    }

    private void init() {
        processingRegionButton.addActionListener(new ActionListener(){
            public void actionPerformed( ActionEvent e ) {
            }
        });
        processingRegionButton.setIcon(IconThemeHelper.getImageIcon("processingregion"));

        startButton.addActionListener(new ActionListener(){
            public void actionPerformed( ActionEvent e ) {
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
                    // if (tree.getModel().getRoot().equals(nodo)) {
                    // setIcon(root);
                    // } else if (nodo.getChildCount() > 0) {
                    // setIcon(parent);
                    // } else {
                    // setIcon(leaf);
                    // }
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

                    // if (value instanceof ViewerFolder) {
                    // ViewerFolder folder = (ViewerFolder) value;
                    // setOpenIcon(categoryIcon);
                    // setClosedIcon(categoryIcon);
                    // if (folder.getSubFolders().size() > 0) {
                    // setLeafIcon(categoryIcon);
                    // } else {
                    // if (isExperimental(value)) {
                    // setLeafIcon(moduleExpIcon);
                    // } else {
                    // setLeafIcon(moduleIcon);
                    // }
                    // }
                    // } else if (value instanceof ViewerModule) {
                    // if (isExperimental(value)) {
                    // setOpenIcon(moduleExpIcon);
                    // setClosedIcon(moduleExpIcon);
                    // } else {
                    // setOpenIcon(moduleIcon);
                    // setClosedIcon(moduleIcon);
                    // }
                    // } else if (value instanceof Modules) {
                    // setOpenIcon(categoryIcon);
                    // setClosedIcon(categoryIcon);
                    // }
                    //
                    // java.awt.Component c = super.getTreeCellRendererComponent(tree, value, arg2,
                    // arg3, arg4, arg5, arg6);
                    // return c;
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

            layoutTree(false);
        } catch (Exception e1) {
            logger.error("Error", e1);
        }
    }

    private void layoutTree( boolean expandNodes ) {
        TreeMap<String, List<ModuleDescription>> availableModules = SpatialToolboxModulesManager.getInstance()
                .browseModules(false);
        // for( String folder : availableModules.keySet() ) {
        // logger.info("Found modules category: " + folder);
        // }
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
            // ArrayList<Field> fields = ((Variable) parent).getFields();
            // Field f = (Field) fields.get(index);
            // Object parentValue = ((Variable) parent).getValue();
            // try {
            // return new Variable(f.getType(), f.getName(), f.get(parentValue));
            // } catch (IllegalAccessException e) {
            // return null;
            // }
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

}
