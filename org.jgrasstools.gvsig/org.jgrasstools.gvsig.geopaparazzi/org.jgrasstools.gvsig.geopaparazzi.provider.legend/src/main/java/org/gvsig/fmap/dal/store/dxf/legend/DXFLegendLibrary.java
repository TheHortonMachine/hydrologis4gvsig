/* gvSIG. Geographic Information System of the Valencian Government
 *
 * Copyright (C) 2007-2008 Infrastructures and Transports Department
 * of the Valencian Government (CIT)
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 *
 */

/*
* AUTHORS (In addition to CIT):
* 2008 IVER T.I. S.A.   {{Task}}
*/

/**
 *
 */
package org.gvsig.fmap.dal.store.dxf.legend;

import org.gvsig.fmap.dal.DALLibrary;
import org.gvsig.metadata.MetadataLibrary;
import org.gvsig.metadata.MetadataLocator;
import org.gvsig.tools.dynobject.DynClass;
import org.gvsig.tools.library.AbstractLibrary;
import org.gvsig.tools.library.LibraryException;
import org.jgrasstools.gvsig.fmap.dal.store.geopaparazzi.GPAPLibrary;
import org.jgrasstools.gvsig.fmap.dal.store.geopaparazzi.GPSPStoreProvider;

/**
 * @author jmvivo
 *
 */
public class DXFLegendLibrary extends AbstractLibrary {

    @Override
    public void doRegistration() {
        registerAsServiceOf(DALLibrary.class);
        require(GPAPLibrary.class);
        require(MetadataLibrary.class);
        require(DALLibrary.class);
    }

	@Override
	protected void doInitialize() throws LibraryException {
	}

	@Override
	protected void doPostInitialize() throws LibraryException {
		DynClass metadataDefinition = (DynClass) MetadataLocator.getMetadataManager()
			.getDefinition(GPSPStoreProvider.METADATA_DEFINITION_NAME);
		DXFGetLegendBuilder.register(metadataDefinition);
		DXFGetLegend.register(metadataDefinition);
		DXFGetLabeling.register(metadataDefinition);
	}
}