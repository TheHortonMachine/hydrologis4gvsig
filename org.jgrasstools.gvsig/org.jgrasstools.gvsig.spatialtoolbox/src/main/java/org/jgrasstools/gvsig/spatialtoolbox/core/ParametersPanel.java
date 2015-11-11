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

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * The parameters panel.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 *
 */
public class ParametersPanel extends JPanel {

    public ParametersPanel() {
        // TODO init
    }

    public void setModule( ModuleDescription module ) {
        clear();

        List<FieldData> inputsList = module.getInputsList();
        List<FieldData> outputsList = module.getOutputsList();
        int allRows = inputsList.size() + outputsList.size();
        String rowsEnc = "";
        for( int i = 0; i < allRows; i++ ) {
            rowsEnc = rowsEnc + ", 3dlu, pref";
        }
        rowsEnc = rowsEnc.substring(1);

        this.setLayout(new FormLayout("right:pref, 2dlu, pref:grow", rowsEnc));

        CellConstraints cc = new CellConstraints();
        int row = 1;
        for( FieldData inputField : inputsList ) {
            JLabel nameLabel = new JLabel(inputField.fieldDescription);
            this.add(nameLabel, cc.xy(1, row));
            JTextField f = new JTextField();
            this.add(f, cc.xy(2, row));
            row++;
        }

        // row = 0;
        for( FieldData outputField : outputsList ) {
            JLabel nameLabel = new JLabel(outputField.fieldDescription);
            this.add(nameLabel, cc.xy(1, row));
            JTextField f = new JTextField();
            this.add(f, cc.xy(2, row));
            row++;
        }
        
        this.invalidate();

    }

    public void clear() {
        this.removeAll();
    }

}
