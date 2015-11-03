package org.jgrasstools.gvsig.geopaparazzi;

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
import org.jgrasstools.gvsig.base.JGTUtilities;

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
                File[] files = dialogManager.showOpenDirectoryDialog("Select output folder", JGTUtilities.getLastFile());
                if (files != null && files.length > 0) {
                    String absolutePath = files[0].getAbsolutePath();
                    JGTUtilities.setLastPath(absolutePath);
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
