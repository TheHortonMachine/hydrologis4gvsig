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
package org.jgrasstools.gvsig.spatialtoolbox.core;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.gvsig.tools.swing.api.ToolsSwingLocator;
import org.gvsig.tools.swing.api.threadsafedialogs.ThreadSafeDialogsManager;
import org.jgrasstools.gears.JGrassGears;
import org.jgrasstools.gvsig.base.JGTUtilities;
import org.jgrasstools.hortonmachine.HortonMachine;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * The parameters panel.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 *
 */
public class ParametersPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    private static final String PM_VAR_NAME = "pm";

    private Class< ? > parentOmsClass;

    private String[] rasterLayers;

    private String[] vectorLayers;

    public void setVectorRasterLayers( String[] vectorLayers, String[] rasterLayers ) {
        this.vectorLayers = vectorLayers;
        this.rasterLayers = rasterLayers;
    }

    public void setModule( ModuleDescription module ) {
        clear();

        parentOmsClass = getParentClass(module);

        final List<FieldData> inputsList = module.getInputsList();
        // final List<FieldData> outputsList = module.getOutputsList();
        int allRows = inputsList.size();// + outputsList.size();
        String rowsEnc = "";
        for( int i = 0; i < allRows; i++ ) {
            if (i == 0) {
                rowsEnc = "pref ";
            } else
                rowsEnc = rowsEnc + ", 3dlu, pref";
        }

        this.setLayout(new FormLayout("left:pref, 2dlu, pref:grow", rowsEnc));

        int labelTrim = 40;

        addInputs(inputsList, labelTrim);

        // NO OUTPUTS AVAILABLE
        // for( FieldData outputField : outputsList ) {
        // String fieldLabel = null;
        // String fieldDescription = outputField.fieldDescription;
        // if (fieldDescription.length() > labelTrim) {
        // fieldLabel = fieldDescription.substring(0, labelTrim) + "...";
        // } else {
        // fieldLabel = fieldDescription;
        // }
        // String fieldTooltip = fieldDescription;
        // JLabel nameLabel = new JLabel(fieldLabel);
        // nameLabel.setToolTipText(fieldTooltip);
        // this.add(nameLabel, cc.xy(1, row));
        // JTextField f = new JTextField();
        // this.add(f, cc.xy(2, row));
        // row = row + 2;
        // }

    }

    private Class< ? > getParentClass( ModuleDescription module ) {
        Class< ? > parentOmsClass = null;
        try {
            Class< ? > moduleClass = module.getModuleClass();
            String simpleName = "Oms" + moduleClass.getSimpleName();
            Class< ? > pClass = HortonMachine.getInstance().moduleName2Class.get(simpleName);
            if (pClass == null) {
                pClass = JGrassGears.getInstance().moduleName2Class.get(simpleName);
            }
            if (pClass != null)
                parentOmsClass = pClass;
        } catch (Exception e) {
            // ignore and return null
            return null;
        }
        return parentOmsClass;
    }

    private void addInputs( final List<FieldData> inputsList, int labelTrim ) {
        CellConstraints cc = new CellConstraints();
        int row = 1;
        for( FieldData inputField : inputsList ) {
            if (inputField.fieldName.equals(PM_VAR_NAME)) {
                continue;
            }
            String fieldLabel = null;
            String fieldDescription = inputField.fieldDescription;
            if (fieldDescription.length() > labelTrim) {
                fieldLabel = fieldDescription.substring(0, labelTrim) + "...";
            } else {
                fieldLabel = fieldDescription;
            }
            String fieldTooltip = fieldDescription;
            // the label
            JLabel nameLabel = new JLabel(fieldLabel);
            nameLabel.setToolTipText(fieldTooltip);

            FileCheck fileCheck = getFileCheck(inputField);
            if (fileCheck.isOutput) {
                Font font = nameLabel.getFont();
                Font boldFont = new Font(font.getFontName(), Font.BOLD, font.getSize());
                nameLabel.setFont(boldFont);
            }
            this.add(nameLabel, cc.xy(1, row));

            // the rest of it
            int col = 3;

            if (isAtLeastOneAssignable(inputField.fieldType, String.class)) {
                if (inputField.guiHints != null && inputField.guiHints.startsWith(SpatialToolboxConstants.MULTILINE_UI_HINT)) {
                    handleTextArea(inputField, row, col, cc);
                } else if (inputField.guiHints != null && inputField.guiHints.startsWith(SpatialToolboxConstants.COMBO_UI_HINT)) {
                    handleComboField(inputField, row, col, cc);
                } else {
                    handleTextField(inputField, row, col, cc, false, fileCheck);
                }
            } else if (isAtLeastOneAssignable(inputField.fieldType, Double.class, double.class)) {
                handleTextField(inputField, row, col, cc, true, fileCheck);
            } else if (isAtLeastOneAssignable(inputField.fieldType, Float.class, float.class)) {
                handleTextField(inputField, row, col, cc, true, fileCheck);
            } else if (isAtLeastOneAssignable(inputField.fieldType, Integer.class, int.class)) {
                handleTextField(inputField, row, col, cc, true, fileCheck);
            } else if (isAtLeastOneAssignable(inputField.fieldType, Short.class, short.class)) {
                handleTextField(inputField, row, col, cc, true, fileCheck);
            } else if (isAtLeastOneAssignable(inputField.fieldType, Boolean.class, boolean.class)) {
                handleBooleanField(inputField, row, col, cc);
                // } else if (isAtLeastOneAssignable(inputField.fieldType, GridCoverage2D.class)) {
                // handleGridcoverageInputField(inputField, row, col, cc);
                // } else if (isAtLeastOneAssignable(inputField.fieldType, GridGeometry2D.class)) {
                // handleGridgeometryInputField(inputField, row, col, cc);
                // } else if (isAtLeastOneAssignable(inputField.fieldType,
                // SimpleFeatureCollection.class)) {
                // handleFeatureInputField(inputField, row, col, cc);
                // } else if (isAtLeastOneAssignable(inputField.fieldType, HashMap.class)) {
                // handleHashMapInputField(inputField, row, col, cc);
                // } else if (isAtLeastOneAssignable(inputField.fieldType, List.class)) {
                // if (inputField.guiHints != null &&
                // inputField.guiHints.equals(OmsBoxConstants.FILESPATHLIST_UI_HINT)) {
                // handleFilesPathListInputField(inputField, row, col, cc);
                // } else {
                // handleListInputField(inputField, row, col, cc);
            }

            row = row + 2;
        }
    }

    private FileCheck getFileCheck( FieldData inputField ) {
        FileCheck f = new FileCheck();
        String guiHints = inputField.guiHints;
        if (guiHints != null) {
            if (guiHints.contains(SpatialToolboxConstants.FILEIN_UI_HINT)) {
                f.isFile = true;
                f.isFolder = false;
                f.isOutput = false;
            } else if (guiHints.contains(SpatialToolboxConstants.FILEOUT_UI_HINT)) {
                f.isFile = true;
                f.isFolder = false;
                f.isOutput = true;
            } else if (guiHints.contains(SpatialToolboxConstants.FOLDERIN_UI_HINT)) {
                f.isFile = true;
                f.isFolder = true;
                f.isOutput = false;
            } else if (guiHints.contains(SpatialToolboxConstants.FOLDEROUT_UI_HINT)) {
                f.isFile = true;
                f.isFolder = true;
                f.isOutput = true;
            }
        }
        return f;
    }

    private void handleTextField( FieldData inputField, int row, int col, CellConstraints cc, boolean onlyNumbers,
            FileCheck fileCheck ) {
        if (!fileCheck.isFile) {
            JTextField textField;
            if (!onlyNumbers) {
                textField = new JTextField();
            } else {
                NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.getDefault());
                DecimalFormat decimalFormat = (DecimalFormat) numberFormat;
                decimalFormat.setGroupingUsed(false);
                textField = new JFormattedTextField(decimalFormat);
            }
            this.add(textField, cc.xy(col, row));
        } else {

            boolean isVector = false;
            boolean isRaster = false;
            if (parentOmsClass != null) {
                try {
                    Field field = parentOmsClass.getField(inputField.fieldName);
                    if (field != null) {
                        Class< ? > fieldClass = field.getType();
                        isVector = isAtLeastOneAssignable(fieldClass.getCanonicalName(), SimpleFeatureCollection.class);
                        isRaster = isAtLeastOneAssignable(fieldClass.getCanonicalName(), GridCoverage2D.class);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (isVector && (vectorLayers == null || vectorLayers.length == 0)) {
                isVector = false;
            }
            if (isRaster && (rasterLayers == null || rasterLayers.length == 0)) {
                isRaster = false;
            }

            if (isVector) {
                JComboBox<String> comboBox = new JComboBox<String>(vectorLayers);
                this.add(comboBox, cc.xy(col, row));
            } else if (isRaster) {
                JComboBox<String> comboBox = new JComboBox<String>(rasterLayers);
                this.add(comboBox, cc.xy(col, row));
            } else {
                JPanel subPanel = new JPanel();
                this.add(subPanel, cc.xy(col, row));
                subPanel.setLayout(new BorderLayout());

                final JTextField textField = new JTextField();
                subPanel.add(textField, BorderLayout.CENTER);

                JButton browseButton = new JButton("...");
                subPanel.add(browseButton, BorderLayout.EAST);
                if (!fileCheck.isFolder && !fileCheck.isOutput) {
                    // input file
                    browseButton.addActionListener(new ActionListener(){
                        public void actionPerformed( ActionEvent e ) {
                            ThreadSafeDialogsManager dialogsManager = ToolsSwingLocator.getThreadSafeDialogsManager();
                            File[] files = dialogsManager.showOpenFileDialog("Select input file", JGTUtilities.getLastFile());
                            setSelectedFile(textField, files);
                        }

                    });
                } else if (!fileCheck.isFolder && fileCheck.isOutput) {
                    // output file
                    browseButton.addActionListener(new ActionListener(){
                        public void actionPerformed( ActionEvent e ) {
                            ThreadSafeDialogsManager dialogsManager = ToolsSwingLocator.getThreadSafeDialogsManager();
                            File[] files = dialogsManager.showSaveFileDialog("Select file to save", JGTUtilities.getLastFile());
                            setSelectedFile(textField, files);
                        }
                    });
                } else if (fileCheck.isFolder && !fileCheck.isOutput) {
                    // input folder
                    browseButton.addActionListener(new ActionListener(){
                        public void actionPerformed( ActionEvent e ) {
                            ThreadSafeDialogsManager dialogsManager = ToolsSwingLocator.getThreadSafeDialogsManager();
                            File[] files = dialogsManager.showOpenDirectoryDialog("Select folder", JGTUtilities.getLastFile());
                            setSelectedFile(textField, files);
                        }
                    });
                } else if (fileCheck.isFolder && fileCheck.isOutput) {
                    // output folder
                    browseButton.addActionListener(new ActionListener(){
                        public void actionPerformed( ActionEvent e ) {
                            ThreadSafeDialogsManager dialogsManager = ToolsSwingLocator.getThreadSafeDialogsManager();
                            File[] files = dialogsManager.showOpenDirectoryDialog("Select folder", JGTUtilities.getLastFile());
                            setSelectedFile(textField, files);
                        }
                    });
                }
            }

        }
    }

    private void setSelectedFile( final JTextField textField, File[] files ) {
        if (files != null && files.length > 0) {
            final File gpapFile = files[0];
            JGTUtilities.setLastPath(gpapFile.getAbsolutePath());
            textField.setText(gpapFile.getAbsolutePath());
        }
    }

    private void handleTextArea( FieldData inputField, int row, int col, CellConstraints cc ) {
        String hint = extractSingleGuiHint(SpatialToolboxConstants.MULTILINE_UI_HINT, inputField.guiHints);
        String rowsStr = hint.replaceFirst(SpatialToolboxConstants.MULTILINE_UI_HINT, "");
        int areaRows = Integer.parseInt(rowsStr);

        JTextArea textArea = new JTextArea();
        textArea.setRows(areaRows);
        this.add(textArea, cc.xy(col, row));
    }

    private void handleBooleanField( FieldData inputField, int row, int col, CellConstraints cc ) {
        JCheckBox checkBox = new JCheckBox("");
        this.add(checkBox, cc.xy(col, row));
    }

    private void handleComboField( FieldData inputField, int row, int col, CellConstraints cc ) {
        String[] guiHintsSplit = inputField.guiHints.split(";");
        String[] imtemsSplit = new String[]{" - "};
        for( String guiHint : guiHintsSplit ) {
            if (guiHint.startsWith(SpatialToolboxConstants.COMBO_UI_HINT)) {
                String items = guiHint.replaceFirst(SpatialToolboxConstants.COMBO_UI_HINT, "").replaceFirst(":", "").trim();
                imtemsSplit = items.split(",");
                break;
            }
        }
        JComboBox<String> comboBox = new JComboBox<String>(imtemsSplit);
        this.add(comboBox, cc.xy(col, row));
    }

    /**
     * Checks if one class is assignable from at least one of the others.
     * 
     * @param main the canonical name of class to check.
     * @param classes the other classes.
     * @return true if at least one of the other classes match.
     */
    private boolean isAtLeastOneAssignable( String main, Class< ? >... classes ) {
        for( Class< ? > clazz : classes ) {
            if (clazz.getCanonicalName().equals(main)) {
                return true;
            }
        }
        return false;
    }

    private String extractSingleGuiHint( String pattern, String guiHints ) {
        String[] split = guiHints.split(",");
        for( String hint : split ) {
            hint = hint.trim();
            if (hint.contains(pattern)) {
                return hint;
            }
        }
        return null;
    }

    public void clear() {
        this.removeAll();
    }

    private static class FileCheck {
        /**
         * If <code>true</code> this is a file (might still be folder).
         */
        boolean isFile;

        /**
         * If <code>true</code>, the file is a folder.
         */
        boolean isFolder;
        /**
         * If <code>true</code>, the file is to be saved/created.
         */
        boolean isOutput;
    }
}
