package org.jgrasstools.gvsig.fmap.dal.store.geopaparazzi;

import java.io.File;
import java.io.IOException;

import org.gvsig.fmap.dal.DALLocator;
import org.gvsig.fmap.dal.DataManager;
import org.gvsig.fmap.dal.DataServerExplorer;
import org.gvsig.fmap.dal.DataStoreParameters;
import org.gvsig.fmap.dal.NewDataStoreParameters;
import org.gvsig.fmap.dal.exception.CreateException;
import org.gvsig.fmap.dal.exception.DataException;
import org.gvsig.fmap.dal.exception.FileNotFoundException;
import org.gvsig.fmap.dal.exception.InitializeException;
import org.gvsig.fmap.dal.exception.RemoveException;
import org.gvsig.fmap.dal.feature.NewFeatureStoreParameters;
import org.gvsig.fmap.dal.resource.ResourceAction;
import org.gvsig.fmap.dal.resource.file.FileResource;
import org.gvsig.fmap.dal.resource.spi.ResourceConsumer;
import org.gvsig.fmap.dal.resource.spi.ResourceProvider;
import org.gvsig.fmap.dal.serverexplorer.filesystem.AbsolutePathRequiredException;
import org.gvsig.fmap.dal.serverexplorer.filesystem.impl.AbstractFilesystemServerExplorerProvider;
import org.gvsig.fmap.dal.serverexplorer.filesystem.spi.FilesystemServerExplorerProvider;
import org.gvsig.fmap.dal.serverexplorer.filesystem.spi.FilesystemServerExplorerProviderServices;

public class GPAPFilesystemServerProvider extends AbstractFilesystemServerExplorerProvider
        implements
            FilesystemServerExplorerProvider,
            ResourceConsumer {

    private FilesystemServerExplorerProviderServices serverExplorer;

    public String getDataStoreProviderName() {
        return GPSPStoreProvider.NAME;
    }

    public int getMode() {
        return DataServerExplorer.MODE_FEATURE | DataServerExplorer.MODE_GEOMETRY;
    }

    public boolean accept( File pathname ) {
        return (pathname.getName().toLowerCase().endsWith(".gpap"));
    }

    public String getDescription() {
        return GPSPStoreProvider.DESCRIPTION;
    }

    public DataStoreParameters getParameters( File file ) throws DataException {
        DataManager manager = DALLocator.getDataManager();
        GPAPStoreParameters params = (GPAPStoreParameters) manager.createStoreParameters(this.getDataStoreProviderName());
        params.setFile(file.getPath());
        return params;
    }

    public boolean canCreate() {
        return true;
    }

    public boolean canCreate( NewDataStoreParameters parameters ) {
        if (!(parameters instanceof GPAPStoreParameters)) {
            throw new IllegalArgumentException(); // FIXME ???
        }
        GPAPStoreParameters gpapParams = (GPAPStoreParameters) parameters;
        // TODO comporbar si el ftype es correcto (para este formato es fijo)
        File file = new File(gpapParams.getFileName());

        // TODO comprobamos extension del fichero ??
        if (file.exists()) {
            return file.canWrite();
        } else {
            return file.getParentFile().canWrite();
        }
    }

    public void create( NewDataStoreParameters parameters, boolean overwrite ) throws CreateException {

        GPAPStoreParameters params = (GPAPStoreParameters) parameters;

        File file = params.getFile();
        if (!file.isAbsolute()) {
            throw new AbsolutePathRequiredException(file.getPath());
        }

        if (file.exists()) {
            if (overwrite) {
                if (!file.delete()) {
                    throw new CreateException(this.getDataStoreProviderName(), new IOException("cannot delete file"));
                }
            } else {
                throw new CreateException(this.getDataStoreProviderName(), new IOException("file already exist"));
            }
        }

        // projection = CRSFactory.getCRS(params.getSRSID());

        final FileResource resource;
        try {
            resource = (FileResource) this.serverExplorer.getServerExplorerProviderServices().createResource(FileResource.NAME,
                    new Object[]{file.getAbsolutePath()});
        } catch (InitializeException e1) {
            throw new CreateException(params.getFileName(), e1);
        }
        resource.addConsumer(this);

        try {
            resource.execute(new ResourceAction(){
                public Object run() throws Exception {
                    Builder builder = new Builder().initialice(resource.getFileName());
                    resource.notifyOpen();
                    builder.begin();
                    builder.create();
                    builder.end();
                    resource.notifyClose();

                    resource.setData(null); // FIXME: Seguro que hay que ponerlo
                                            // a null ??
                    resource.notifyChanges();
                    return null;
                }
            });
        } catch (Exception e) {
            throw new CreateException(params.getFileName(), e);
        } finally {
            resource.removeConsumer(this);
        }
    }

    public class Builder {
        private String fileName;

        public Builder initialice( String fileName ) {
            this.fileName = fileName;
            return this;
        }

        public void begin() {

        }

        public void end() {

        }

        public void create() throws IOException {
            /*
             * create a new gpap database? TODO
             */
        }
    }

    public NewDataStoreParameters getCreateParameters() throws DataException {
        return (NewFeatureStoreParameters) DALLocator.getDataManager().createStoreParameters(this.getDataStoreProviderName());
    }

    public void initialize( FilesystemServerExplorerProviderServices serverExplorer ) {
        this.serverExplorer = serverExplorer;
    }

    public void remove( DataStoreParameters parameters ) throws RemoveException {
        File file = new File(((GPAPStoreParameters) parameters).getFileName());
        if (!file.exists()) {
            throw new RemoveException(this.getDataStoreProviderName(), new FileNotFoundException(file));
        }
        if (!file.delete()) {
            // FIXME throws ???
        }

    }

    public boolean closeResourceRequested( ResourceProvider resource ) {
        // while it is using a resource anyone can't close it
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.gvsig.fmap.dal.resource.spi.ResourceConsumer#resourceChanged(org.
     * gvsig.fmap.dal.resource.spi.ResourceProvider)
     */
    public void resourceChanged( ResourceProvider resource ) {
        // Do nothing

    }

}
