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

/**
 * The parameters panel.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 *
 */
public class ParametersPanel extends JPanel {

    private JPanel inputsPanel;
    private JPanel outputsPanel;

    public ParametersPanel() {
        // TODO init
        this.setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.anchor = GridBagConstraints.PAGE_START;
        
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0.5;
        c.weighty = 0.5;
        inputsPanel = new JPanel(new GridBagLayout());
        inputsPanel.setBorder(BorderFactory.createTitledBorder("Inputs"));
        this.add(inputsPanel, c);

        c.gridx = 0;
        c.gridy = 1;
        c.weightx = 0.5;
        c.weighty = 0.5;
        outputsPanel = new JPanel(new GridBagLayout());
        outputsPanel.setBorder(BorderFactory.createTitledBorder("Outputs"));
        this.add(outputsPanel, c);
    }

    public void setModule( ModuleDescription module ) {
        clear();
        List<FieldData> inputsList = module.getInputsList();
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.anchor = GridBagConstraints.PAGE_START;
        int row = 0;
        for( FieldData inputField : inputsList ) {
            c.gridx = 0;
            c.gridy = row;
            c.weightx = 0.5;

            JLabel nameLabel = new JLabel(inputField.fieldDescription);
            inputsPanel.add(nameLabel, c);

            c.gridx = 1;
            c.gridy = row;
            c.weightx = 0.5;

            JTextField f = new JTextField();
            inputsPanel.add(f, c);

            row++;
        }

        List<FieldData> outputsList = module.getOutputsList();
        row = 0;
        for( FieldData outputField : outputsList ) {
            c.gridx = 0;
            c.gridy = row;
            c.weightx = 0.5;

            JLabel nameLabel = new JLabel(outputField.fieldDescription);
            outputsPanel.add(nameLabel, c);

            c.gridx = 1;
            c.gridy = row;
            c.weightx = 0.5;

            JTextField f = new JTextField();
            outputsPanel.add(f, c);

            row++;
        }

    }

    public void clear() {
        inputsPanel.removeAll();
        outputsPanel.removeAll();
    }

}
