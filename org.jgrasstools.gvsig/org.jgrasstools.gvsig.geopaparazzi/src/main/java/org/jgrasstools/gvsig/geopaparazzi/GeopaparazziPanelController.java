package org.jgrasstools.gvsig.geopaparazzi;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.List;

import org.gvsig.fmap.dal.DALLocator;
import org.gvsig.fmap.dal.DataManager;
import org.gvsig.fmap.dal.DataServerExplorer;
import org.gvsig.fmap.dal.DataServerExplorerParameters;
import org.gvsig.fmap.dal.DataStoreParameters;
import org.gvsig.fmap.dal.exception.DataException;
import org.gvsig.fmap.dal.exception.ValidateDataParametersException;
import org.gvsig.tools.swing.api.ToolsSwingLocator;
import org.gvsig.tools.swing.api.threadsafedialogs.ThreadSafeDialogsManager;
import org.jgrasstools.gvsig.base.JGTUtilities;

public class GeopaparazziPanelController extends GeopaparazziPanelView {

    public GeopaparazziPanelController() {
        init();
    }

    private void init() {
        this.browseButton.addActionListener(new ActionListener(){

            public void actionPerformed( ActionEvent e ) {
                browse();
            }
        });
        // TODO Auto-generated method stub

    }

    public void browse() {
        ThreadSafeDialogsManager dialogsManager = ToolsSwingLocator.getThreadSafeDialogsManager();
        File[] files = dialogsManager.showOpenFileDialog("Select Geopaparazzi File", JGTUtilities.getLastFile());
        if (files != null && files.length > 0) {
            File gpapFile = files[0];
            this.geopaparazziDatabasePathField.setText(gpapFile.getAbsolutePath());
            
            
            try {
                openDatabaseFile(gpapFile);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
    public DataStoreParameters[] getParameters() {
        // TODO Auto-generated method stub
        return null;
    }
    
    private void openDatabaseFile( File gpapFile ) throws ValidateDataParametersException, DataException  {
        DataManager dataManager = DALLocator.getDataManager();
        
        DataServerExplorerParameters parameters = dataManager.createServerExplorerParameters("SqliteExplorer");
        parameters.setDynValue("dbname", gpapFile.getAbsolutePath());
        DataServerExplorer serverExplorer = dataManager.openServerExplorer("SqliteExplorer",parameters);
        
        List list = serverExplorer.list();
        
        
    }

}
