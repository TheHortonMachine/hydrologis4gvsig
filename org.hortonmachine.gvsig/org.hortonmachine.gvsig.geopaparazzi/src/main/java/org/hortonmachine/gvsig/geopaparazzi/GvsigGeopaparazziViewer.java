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
package org.hortonmachine.gvsig.geopaparazzi;

import java.util.List;

import javax.swing.Action;
import javax.swing.UIManager;

import org.hortonmachine.gears.io.geopaparazzi.geopap4.DaoGpsLog.GpsLog;
import org.hortonmachine.gears.io.geopaparazzi.geopap4.Image;
import org.hortonmachine.gears.io.geopaparazzi.geopap4.Note;
import org.hortonmachine.geopaparazzi.GeopaparazziViewer;
import org.hortonmachine.geopaparazzi.simpleserver.ProjectInfo;
import org.hortonmachine.gui.utils.GuiBridgeHandler;
import org.hortonmachine.gui.utils.ImageCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The geopaparazzi view controller.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 *
 */
public class GvsigGeopaparazziViewer extends GeopaparazziViewer {
    private static final Logger logger = LoggerFactory.getLogger(GvsigGeopaparazziViewer.class);
    private static final long serialVersionUID = 1L;

    public GvsigGeopaparazziViewer( GuiBridgeHandler guiBridge ) {
        super(guiBridge);
        
        UIManager.put("Tree.openIcon", ImageCache.getInstance().getImage(ImageCache.TREE_OPEN));
        UIManager.put("Tree.closedIcon", ImageCache.getInstance().getImage(ImageCache.TREE_CLOSED));
    }

    @Override
    protected List<Action> makeGpsLogActions( GpsLog selectedLog ) {
        return super.makeGpsLogActions(selectedLog);
    }

    @Override
    protected List<Action> makeProjectAction( ProjectInfo project ) {
        logger.debug("make project action");
        List<Action> projectActions = super.makeProjectAction(project);
        logger.debug("using project actions from supre: " + projectActions.size());
        return projectActions;
    }

    @Override
    protected List<Action> makeImageAction( Image selectedImage ) {
        List<Action> actions = super.makeImageAction(selectedImage);
        return actions;

    }

    @Override
    protected List<Action> makeNotesActions( Note selectedNote ) {
        // TODO Auto-generated method stub
        return super.makeNotesActions(selectedNote);
    }

    public void isVisibleTriggered() {

    }

}
