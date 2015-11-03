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

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.gvsig.tools.swing.api.Component;
import org.gvsig.tools.swing.api.ToolsSwingLocator;
import org.gvsig.tools.swing.api.threadsafedialogs.ThreadSafeDialogsManager;
import org.jgrasstools.gvsig.base.JGTUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Epanet results panel.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class GenerateTilesParametersPanel extends JPanel implements Component {
    private static final long serialVersionUID = 1L;

    private static final Logger logger = LoggerFactory.getLogger(GenerateTilesParametersPanel.class);

    private ThreadSafeDialogsManager dialogManager;

    private JComboBox<String> minZoomCombo;

    private JComboBox<String> maxZoomCombo;

    private JTextField nameField;

    private JTextField folderField;

    private JComboBox<String> imageTypeCombo;

    public GenerateTilesParametersPanel() {
        dialogManager = ToolsSwingLocator.getThreadSafeDialogsManager();

        init();
        setPreferredSize(new Dimension(800, 300));
    }

    private void init() {
        setLayout(new GridBagLayout());
        Insets insets = new Insets(5, 5, 5, 5);

        GridBagConstraints c = new GridBagConstraints();
        c.weightx = 0.0;
        c.weighty = 0.5;
        c.anchor = GridBagConstraints.PAGE_START;
        c.fill = GridBagConstraints.BOTH;
        c.insets = insets;
        c.ipadx = 0;
        c.ipady = 0;

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
        try {

            int row = 0;
            c.weighty = 0;

            int leftWeight = 2;
            int rightWeight = 2;

            JLabel minZoomLabel = new JLabel("Minimum zoom level");
            c.gridx = 0;
            c.gridy = row;
            c.weightx = leftWeight;
            add(minZoomLabel, c);

            minZoomCombo = new JComboBox<String>(zoomLevels);
            minZoomCombo.setSelectedIndex(12);
            c.gridx = 1;
            c.gridy = row;
            c.weightx = rightWeight;
            add(minZoomCombo, c);

            row++;

            JLabel maxZoomLabel = new JLabel("Maximum zoom level");
            c.gridx = 0;
            c.gridy = row;
            c.weightx = leftWeight;
            add(maxZoomLabel, c);

            maxZoomCombo = new JComboBox<String>(zoomLevels);
            maxZoomCombo.setSelectedIndex(18);
            c.gridx = 1;
            c.gridy = row;
            c.weightx = rightWeight;
            add(maxZoomCombo, c);

            row++;

            JLabel nameLabel = new JLabel("Dataset name");
            c.gridx = 0;
            c.gridy = row;
            c.weightx = leftWeight;
            add(nameLabel, c);

            nameField = new JTextField("new_dataset");
            c.gridx = 1;
            c.gridy = row;
            c.weightx = rightWeight;
            add(nameField, c);

            row++;

            JLabel outputFolderLabel = new JLabel("Output folder");
            c.gridx = 0;
            c.gridy = row;
            c.weightx = 1;
            add(outputFolderLabel, c);

            folderField = new JTextField();
            c.gridx = 1;
            c.gridy = row;
            c.weightx = 2;
            add(folderField, c);

            final JButton browseFolderButton = new JButton("...");
            c.gridx = 2;
            c.gridy = row;
            c.weightx = 1;
            add(browseFolderButton, c);
            browseFolderButton.addActionListener(new ActionListener(){
                public void actionPerformed( ActionEvent e ) {
                    File[] files = dialogManager.showOpenDirectoryDialog("Select output folder", JGTUtilities.getLastFile());
                    if (files != null && files.length > 0) {
                        String absolutePath = files[0].getAbsolutePath();
                        JGTUtilities.setLastPath(absolutePath);
                        browseFolderButton.setText(absolutePath);
                    }
                }
            });

            row++;

            JLabel imageTypeLabel = new JLabel("Image type used");
            c.gridx = 0;
            c.gridy = row;
            c.weightx = leftWeight;
            add(imageTypeLabel, c);

            imageTypeCombo = new JComboBox<String>(new String[]{"png", "jpg"});
            imageTypeCombo.setSelectedIndex(0);
            c.gridx = 1;
            c.gridy = row;
            c.weightx = rightWeight;
            add(imageTypeCombo, c);

            row++;

            JButton okButton = new JButton("Ok");
            c.gridx = 1;
            c.gridy = row;
            c.weightx = 1;
            add(okButton, c);
            okButton.addActionListener(new ActionListener(){
                public void actionPerformed( ActionEvent e ) {
                    close();
                }
            });

            JButton cancelButton = new JButton("Cancel");
            c.gridx = 2;
            c.gridy = row;
            c.weightx = 1;
            add(cancelButton, c);
            cancelButton.addActionListener(new ActionListener(){
                public void actionPerformed( ActionEvent e ) {
                    close();
                }
            });

            // dialogManager.messageDialog(warnings, "WARNING", JOptionPane.WARNING_MESSAGE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void close() {
        setVisible(false);
    }

    public int getMinZoom() {
        String minZoomStr = minZoomCombo.getSelectedItem().toString();
        int minZoom = Integer.parseInt(minZoomStr);
        return minZoom;
    }

    public int getMaxZoom() {
        String maxZoomStr = maxZoomCombo.getSelectedItem().toString();
        int maxZoom = Integer.parseInt(maxZoomStr);
        return maxZoom;
    }

    public String getImageType() {
        return imageTypeCombo.getSelectedItem().toString();
    }

    public String getDbName() {
        String name = nameField.getText();
        return name;
    }

    public String getDbFolder() {
        String folderPath = folderField.getText();
        return folderPath;
    }

    public JComponent asJComponent() {
        return this;
    }

}
