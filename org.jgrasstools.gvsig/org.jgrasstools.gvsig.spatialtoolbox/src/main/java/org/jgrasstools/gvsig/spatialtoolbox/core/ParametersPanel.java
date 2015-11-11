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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * The parameters panel.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 *
 */
public class ParametersPanel extends JPanel {

    private static final String PM_VAR_NAME = "pm";

    public ParametersPanel() {
        // TODO init
    }

    public void setModule( ModuleDescription module ) {
        clear();

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

        int labelTrim = 25;

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
            JLabel nameLabel = new JLabel(fieldLabel);
            nameLabel.setToolTipText(fieldTooltip);
            this.add(nameLabel, cc.xy(1, row));
            
            
            JTextField f = new JTextField();
            this.add(f, cc.xy(3, row));
            row = row + 2;
        }

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

        SwingUtilities.invokeLater(new Runnable(){
            public void run() {
                validate();
                repaint();
            }
        });
    }

    public void clear() {
        this.removeAll();
    }

}
