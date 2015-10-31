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

package org.jgrasstools.gvsig.fmap.dal.store.geopaparazzi;

import java.util.ArrayList;
import java.util.List;

import org.gvsig.fmap.dal.DALFileLibrary;
import org.gvsig.fmap.dal.DALFileLocator;
import org.gvsig.fmap.dal.DALLibrary;
import org.gvsig.fmap.dal.DALLocator;
import org.gvsig.fmap.dal.FileHelper;
import org.gvsig.fmap.dal.spi.DataManagerProviderServices;
import org.gvsig.metadata.exceptions.MetadataException;
import org.gvsig.tools.library.AbstractLibrary;
import org.gvsig.tools.library.LibraryException;

public class GPAPLibrary extends AbstractLibrary {

    @Override
    public void doRegistration() {
        registerAsServiceOf(DALLibrary.class);
        require(DALFileLibrary.class);
    }

	@Override
	protected void doInitialize() throws LibraryException {
	}

	@Override
	protected void doPostInitialize() throws LibraryException {
		List<Throwable> exs = new ArrayList<Throwable>();

		FileHelper.registerParametersDefinition(
				GPAPStoreParameters.PARAMETERS_DEFINITION_NAME,
				GPAPStoreParameters.class, "GPAPParameters.xml");
		try {
			FileHelper.registerMetadataDefinition(
					GPSPStoreProvider.METADATA_DEFINITION_NAME,
					GPSPStoreProvider.class, "GPAPMetadata.xml");
		} catch (MetadataException e) {
			exs.add(e);
		}

		DataManagerProviderServices dataman = (DataManagerProviderServices) DALLocator
				.getDataManager();

		try {
			if (!dataman.getStoreProviders().contains(GPSPStoreProvider.NAME)) {
				dataman.registerStoreProviderFactory(new GPAPStoreProviderFactory(GPSPStoreProvider.NAME, GPSPStoreProvider.DESCRIPTION));

			}
		} catch (RuntimeException e) {
			exs.add(e);
		}

		try {
			DALFileLocator.getFilesystemServerExplorerManager()
					.registerProvider(GPSPStoreProvider.NAME,
							GPSPStoreProvider.DESCRIPTION,
							GPAPFilesystemServerProvider.class);
		} catch (RuntimeException e) {
			exs.add(e);
		}

		if (exs.size() > 0) {
			throw new LibraryException(this.getClass(), exs);
		}
	}
}