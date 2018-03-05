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

import javax.imageio.spi.IIORegistry;

import org.gvsig.andami.plugins.Extension;
import org.hortonmachine.hmachine.HortonMachine;

import it.geosolutions.imageio.stream.input.spi.URLImageInputStreamSpi;

/**
 * Extension to add {@link HortonMachine} support
 *
 * @author Antonello Andrea (www.hydrologis.com)
 */
public abstract class HMExtension extends Extension {
    static {
        IIORegistry.getDefaultInstance().registerApplicationClasspathSpis();
        IIORegistry.getDefaultInstance().registerServiceProvider(new URLImageInputStreamSpi());
    }
}