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
package org.hortonmachine.gvsig.base;

import javax.media.jai.JAI;
import javax.media.jai.OperationRegistry;

import org.gvsig.andami.IconThemeHelper;
import org.gvsig.andami.plugins.Extension;
import org.hortonmachine.hmachine.HortonMachine;

import com.sun.media.jai.imageioimpl.ImageReadWriteSpi;

/**
 * Extension to add {@link HortonMachine} support
 *
 * @author Antonello Andrea (www.hydrologis.com)
 */
public class HortonMachineExtension extends Extension {

    public void execute( String actionCommand ) {
    }
    

    public void initialize() {
        IconThemeHelper.registerIcon("action", "copy_edit", this);
        IconThemeHelper.registerIcon("action", "progress_stop", this);
        IconThemeHelper.registerIcon("action", "trash", this);
        
        initJAI();
    }
    
    protected void initJAI() {
        // See [URL]http://docs.oracle.com/cd/E17802_01/products/products/java-media/jai/forDevelopers/jai-apidocs/javax/media/jai/OperationRegistry.html[/URL]
        OperationRegistry registry = JAI.getDefaultInstance().getOperationRegistry();
        
        if( registry == null) {
            new RuntimeException().printStackTrace();
        } else {
            try {
                new ImageReadWriteSpi().updateRegistry(registry);
            } catch(IllegalArgumentException e) {
                // Probably indicates it was already registered.
            }
        }
    }

    public boolean isEnabled() {
        return false;
    }

    public boolean isVisible() {
        return false;
    }

}