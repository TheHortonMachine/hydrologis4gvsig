package org.jgrasstools.gvsig.pointinfo;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Point2D;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JPanel;

import org.cresques.cts.ICoordTrans;
import org.cresques.cts.IProjection;
import org.gvsig.andami.IconThemeHelper;
import org.gvsig.andami.ui.mdiManager.IWindow;
import org.gvsig.app.ApplicationLocator;
import org.gvsig.app.ApplicationManager;
import org.gvsig.app.gui.panels.CRSSelectPanelFactory;
import org.gvsig.app.gui.panels.crs.ISelectCrsPanel;
import org.gvsig.app.project.ProjectManager;
import org.gvsig.app.project.documents.Document;
import org.gvsig.app.project.documents.view.ViewDocument;
import org.gvsig.app.project.documents.view.gui.IView;
import org.gvsig.fmap.geom.primitive.Point;
import org.gvsig.fmap.mapcontrol.MapControl;
import org.gvsig.fmap.mapcontrol.MapControlCreationListener;
import org.gvsig.fmap.mapcontrol.MapControlLocator;
import org.gvsig.fmap.mapcontrol.MapControlManager;
import org.gvsig.fmap.mapcontrol.tools.BehaviorException;
import org.gvsig.fmap.mapcontrol.tools.Behavior.Behavior;
import org.gvsig.fmap.mapcontrol.tools.Behavior.MoveBehavior;
import org.gvsig.fmap.mapcontrol.tools.Behavior.PointBehavior;
import org.gvsig.fmap.mapcontrol.tools.Behavior.PolygonBehavior;
import org.gvsig.fmap.mapcontrol.tools.Behavior.PolylineBehavior;
import org.gvsig.fmap.mapcontrol.tools.Behavior.RectangleBehavior;
import org.gvsig.fmap.mapcontrol.tools.Events.EnvelopeEvent;
import org.gvsig.fmap.mapcontrol.tools.Events.MeasureEvent;
import org.gvsig.fmap.mapcontrol.tools.Events.MoveEvent;
import org.gvsig.fmap.mapcontrol.tools.Events.PointEvent;
import org.gvsig.fmap.mapcontrol.tools.Listeners.AbstractPointListener;
import org.gvsig.fmap.mapcontrol.tools.Listeners.AbstractToolListener;
import org.gvsig.fmap.mapcontrol.tools.Listeners.PanListener;
import org.gvsig.fmap.mapcontrol.tools.Listeners.PolylineListener;
import org.gvsig.fmap.mapcontrol.tools.Listeners.RectangleListener;
import org.gvsig.tools.swing.api.Component;
import org.gvsig.tools.swing.api.ToolsSwingLocator;
import org.gvsig.tools.swing.api.windowmanager.WindowManager.MODE;
import org.jgrasstools.gvsig.base.JGTUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PointInfoController extends PointInfoView implements MapControlCreationListener, Component, MouseListener {
    private static final String POINTER_TOOLS = "pointer_tools";
    private static final Logger logger = LoggerFactory.getLogger(PointInfoController.class);
    private MapControl mapControl;
    private ApplicationManager applicationManager;
    private ProjectManager projectManager;
    private IProjection selectedProjection;

    public PointInfoController() {
        applicationManager = ApplicationLocator.getManager();

        MapControlManager mapControlManager = MapControlLocator.getMapControlManager();
        mapControlManager.addMapControlCreationListener(this);

        setPreferredSize(new Dimension(500, 200));
        init();
    }

    private void init() {
        ImageIcon copyIcon = IconThemeHelper.getImageIcon("copy");
        copyLatButton.addActionListener(new ActionListener(){
            public void actionPerformed( ActionEvent e ) {
                JGTUtilities.copyToClipboard(latTextField.getText());
            }
        });
        copyLatButton.setIcon(copyIcon);

        copyLonButton.addActionListener(new ActionListener(){
            public void actionPerformed( ActionEvent e ) {
                JGTUtilities.copyToClipboard(lonTextField.getText());
            }
        });
        copyLonButton.setIcon(copyIcon);

        copyYButton.addActionListener(new ActionListener(){
            public void actionPerformed( ActionEvent e ) {
                JGTUtilities.copyToClipboard(yField.getText());
            }
        });
        copyYButton.setIcon(copyIcon);

        copyXButton.addActionListener(new ActionListener(){
            public void actionPerformed( ActionEvent e ) {
                JGTUtilities.copyToClipboard(xField.getText());
            }
        });
        copyXButton.setIcon(copyIcon);

        crsButton.addActionListener(new ActionListener(){
            public void actionPerformed( ActionEvent e ) {
                ISelectCrsPanel csSelect = CRSSelectPanelFactory.getUIFactory().getSelectCrsPanel(null, true);
                ToolsSwingLocator.getWindowManager().showWindow((JComponent) csSelect,
                        "Please insert the CRS EPSG code for the required projection.", MODE.DIALOG);
                if (csSelect.isOkPressed() && csSelect.getProjection() != null) {
                    selectedProjection = csSelect.getProjection();
                    epsgField.setText(selectedProjection.getAbrev());
                } else {
                    selectedProjection = null;
                    epsgField.setText("");
                    xField.setText("");
                    yField.setText("");
                }
            }
        });

        IWindow activeWindow = applicationManager.getActiveWindow();
        if (activeWindow == null) {
            return;
        }

        addComponentListener(new ComponentListener(){

            public void componentShown( ComponentEvent e ) {
            }

            public void componentResized( ComponentEvent e ) {
            }

            public void componentMoved( ComponentEvent e ) {
            }

            public void componentHidden( ComponentEvent e ) {
                freeResources();
            }
        });

        projectManager = applicationManager.getProjectManager();

        Document activeDocument = projectManager.getCurrentProject().getActiveDocument();
        ViewDocument docView = (ViewDocument) activeDocument;
        IWindow mainWindow = docView.getMainWindow();
        IView view = (IView) mainWindow;
        mapControl = view.getMapControl();
        addBehavior();

    }

    private void addBehavior() {
        // PointInfoListener infoListener = new PointInfoListener();
        // RectangleInfoListener rectListener = new RectangleInfoListener();
        // PanInfoListener panInfoListener = new PanInfoListener();
        // PolylineInfoListener polylineInfoListener = new PolylineInfoListener();

        mapControl.addMouseListener(this);
        // mapControl.addBehavior("pointSelection", new PointBehavior(infoListener));
        // mapControl.addBehavior("info", new PointBehavior(infoListener));
        // mapControl.addBehavior("zoomOut", new PointBehavior(infoListener));
        // mapControl.addBehavior("zoomIn",
        // new Behavior[]{new RectangleBehavior(rectListener), new PointBehavior(infoListener,
        // Behavior.BUTTON_RIGHT)});
        // mapControl.addBehavior("rectSelection", new RectangleBehavior(rectListener));
        //
        // mapControl.addBehavior("pan", new MoveBehavior(panInfoListener, Behavior.BUTTON_LEFT));
        // mapControl.addBehavior("medicion", new PolylineBehavior(polylineInfoListener));
        // mapControl.addBehavior("area", new PolygonBehavior(polylineInfoListener));
        // mapControl.addBehavior("polSelection", new PolygonBehavior(polylineInfoListener));
    }

    private void setPoint( double lon, double lat ) {
        lonTextField.setText("" + lon);
        latTextField.setText("" + lat);

        if (selectedProjection != null) {

            final IProjection mapProjection = mapControl.getProjection();
            final ICoordTrans ct = mapProjection.getCT(selectedProjection);
            final Point2D orig = new Point2D.Double(lon, lat);
            Point2D transformed;
            if (ct == null) {
                transformed = orig;
            } else {
                transformed = ct.convert(orig, null);
            }
            xField.setText("" + transformed.getX());
            yField.setText("" + transformed.getY());
        }
    }

    private class PointInfoListener extends AbstractPointListener {
        public void point( PointEvent event ) throws BehaviorException {
            Point mapPoint = event.getMapPoint();
            final double lon = mapPoint.getX();
            final double lat = mapPoint.getY();
            setPoint(lon, lat);
        }
    }

    private class PanInfoListener extends AbstractToolListener implements PanListener {
        public void move( MoveEvent event ) throws BehaviorException {
            Point2D mapPoint = event.getEvent().getPoint();
            final double lon = mapPoint.getX();
            final double lat = mapPoint.getY();
            setPoint(lon, lat);
        }
    }

    private class RectangleInfoListener extends AbstractToolListener implements RectangleListener {
        public void rectangle( EnvelopeEvent event ) throws BehaviorException {
            Point upperCorner = event.getWorldCoordRect().getUpperCorner();
            final double lon = upperCorner.getX();
            final double lat = upperCorner.getY();
            setPoint(lon, lat);
        }
    }

    private class PolylineInfoListener extends AbstractToolListener implements PolylineListener {
        public void points( MeasureEvent event ) throws BehaviorException {
            doPoint(event);
        }

        public void pointFixed( MeasureEvent event ) throws BehaviorException {
            doPoint(event);
        }

        private void doPoint( MeasureEvent event ) {
            Point2D currentPoint = event.getGP().getCurrentPoint();
            final double lon = currentPoint.getX();
            final double lat = currentPoint.getY();
            setPoint(lon, lat);
        }

        public void polylineFinished( MeasureEvent event ) throws BehaviorException {
            doPoint(event);
        }
    }

    public MapControl mapControlCreated( MapControl mapControl ) {
        freeResources();
        this.mapControl = mapControl;
        addBehavior();

        return mapControl;
    }

    private void freeResources() {
        if (mapControl != null)
            mapControl.removeMouseListener(this);
    }

    public JComponent asJComponent() {
        return this;
    }

    public void mouseReleased( MouseEvent e ) {
    }

    public void mousePressed( MouseEvent e ) {
    }

    public void mouseExited( MouseEvent e ) {
    }

    public void mouseEntered( MouseEvent e ) {
    }

    public void mouseClicked( MouseEvent e ) {
        int x = e.getX();
        int y = e.getY();
        Point mapPoint = mapControl.getViewPort().convertToMapPoint(new Point2D.Double(x, y));
        setPoint(mapPoint.getX(), mapPoint.getY());
    }

}
