/*
 * This file is part of HortonMachine (http://www.hortonmachine.org)
 * (C) HydroloGIS - www.hydrologis.com 
 * 
 * The HortonMachine is free software: you can redistribute it and/or modify
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
package org.hortonmachine.gvsig.geopaparazzi;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.gvsig.tools.swing.api.Component;
import org.gvsig.tools.swing.api.ToolsSwingLocator;
import org.gvsig.tools.swing.api.threadsafedialogs.ThreadSafeDialogsManager;
import org.hortonmachine.gvsig.base.HMUtilities;

/**
 * Tyles generation gui
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 *
 */
public class GenerateTilesParametersPanelController extends GenerateTilesParametersPanelView implements Component {

    String[] zoomLevels = new String[]{"1", //
            "2", //
            "3", //
            "4", //
            "5", //
            "6", //
            "7", //
            "8", //
            "9", //
            "10", //
            "11", //
            "12", //
            "13", //
            "14", //
            "15", //
            "16", //
            "17", //
            "18", //
            "19", //
            "20", //
            "21", //
            "22"//
    };

    private ThreadSafeDialogsManager dialogManager;

    boolean okToRun = false;
    int minZoom;
    int maxZoom;
    String imageType;
    String dbName;
    String outputFolder;

    public GenerateTilesParametersPanelController() {
        dialogManager = ToolsSwingLocator.getThreadSafeDialogsManager();
        
        setPreferredSize(new Dimension(400, 200));
        init();
    }

    private void init() {
        minZoomCombo.setModel(new DefaultComboBoxModel<String>(zoomLevels));
        minZoomCombo.setSelectedIndex(12);

        maxZoomCombo.setModel(new DefaultComboBoxModel<String>(zoomLevels));
        maxZoomCombo.setSelectedIndex(18);

        imageTypeCombo.setModel(new DefaultComboBoxModel<String>(new String[]{"png", "jpg"}));
        imageTypeCombo.setSelectedIndex(0);

        browseFolderButton.addActionListener(new ActionListener(){
            public void actionPerformed( ActionEvent e ) {
                File[] files = dialogManager.showOpenDirectoryDialog("Select output folder", HMUtilities.getLastFile());
                if (files != null && files.length > 0) {
                    String absolutePath = files[0].getAbsolutePath();
                    HMUtilities.setLastPath(absolutePath);
                    outputFolderField.setText(absolutePath);
                }
            }
        });

        okButton.addActionListener(new ActionListener(){
            public void actionPerformed( ActionEvent e ) {
                okToRun = gatherValues();
                if (okToRun)
                    setVisible(false);
            }

        });
        cancelButton.addActionListener(new ActionListener(){
            public void actionPerformed( ActionEvent e ) {
                okToRun = false;
                setVisible(false);
            }
        });
    }

    private boolean gatherValues() {

        dbName = nameField.getText();
        if (dbName.trim().length() == 0) {
            dialogManager.messageDialog("The dataset name is mandatory.", "ERROR", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        outputFolder = outputFolderField.getText();
        if (outputFolder.trim().length() == 0 || !new File(outputFolder).exists()) {
            dialogManager.messageDialog("The output folder needs to be an existing folder.", "ERROR", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        minZoom = Integer.parseInt(minZoomCombo.getSelectedItem().toString());
        maxZoom = Integer.parseInt(maxZoomCombo.getSelectedItem().toString());
        imageType = imageTypeCombo.getSelectedItem().toString();

        return true;
    }

    public JComponent asJComponent() {
        return this;
    }
}
