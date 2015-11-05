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

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComponent;

import org.gvsig.andami.IconThemeHelper;
import org.gvsig.tools.swing.api.Component;

/**
 * The spatialtoolbox view controller.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 *
 */
public class SpatialtoolboxController extends SpatialtoolboxView implements Component{
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
            }
        });
        clearFilterButton.setIcon(IconThemeHelper.getImageIcon("trash"));
        
        loadExperimentalCheckbox.addActionListener(new ActionListener(){
            public void actionPerformed( ActionEvent e ) {
                
            }
        });
        loadExperimentalCheckbox.setSelected(true);
        
    
    }

    public JComponent asJComponent() {
        return this;
    }

}
