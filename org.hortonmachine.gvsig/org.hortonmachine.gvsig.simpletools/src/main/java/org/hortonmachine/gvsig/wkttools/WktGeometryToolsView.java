package org.hortonmachine.gvsig.wkttools;

import com.jeta.open.i18n.I18NUtils;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import java.awt.BorderLayout;
import java.awt.ComponentOrientation;
import java.awt.Container;
import java.awt.Dimension;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;


public class WktGeometryToolsView extends JPanel
{
   JLabel _getFromLayerLabel = new JLabel();
   JTextArea _getWktFromLayerArea = new JTextArea();
   JButton _copyWktButton = new JButton();
   JButton _getWktFromLayerButton = new JButton();
   JLabel _putToLayerLabel = new JLabel();
   JButton _putWktToLayerButton = new JButton();
   JTextArea _putWktToLayerArea = new JTextArea();
   JLabel _zoomBufferLabel = new JLabel();
   JTextField _zoomBufferField = new JTextField();
   JCheckBox _zoomToCheckbox = new JCheckBox();
   JLabel _selectCrsLabel = new JLabel();
   JTextField _crsTextField = new JTextField();
   JButton _selectCrsButton = new JButton();

   /**
    * Default constructor
    */
   public WktGeometryToolsView()
   {
      initializePanel();
   }

   /**
    * Adds fill components to empty cells in the first row and first column of the grid.
    * This ensures that the grid spacing will be the same as shown in the designer.
    * @param cols an array of column indices in the first row where fill components should be added.
    * @param rows an array of row indices in the first column where fill components should be added.
    */
   void addFillComponents( Container panel, int[] cols, int[] rows )
   {
      Dimension filler = new Dimension(10,10);

      boolean filled_cell_11 = false;
      CellConstraints cc = new CellConstraints();
      if ( cols.length > 0 && rows.length > 0 )
      {
         if ( cols[0] == 1 && rows[0] == 1 )
         {
            /** add a rigid area  */
            panel.add( Box.createRigidArea( filler ), cc.xy(1,1) );
            filled_cell_11 = true;
         }
      }

      for( int index = 0; index < cols.length; index++ )
      {
         if ( cols[index] == 1 && filled_cell_11 )
         {
            continue;
         }
         panel.add( Box.createRigidArea( filler ), cc.xy(cols[index],1) );
      }

      for( int index = 0; index < rows.length; index++ )
      {
         if ( rows[index] == 1 && filled_cell_11 )
         {
            continue;
         }
         panel.add( Box.createRigidArea( filler ), cc.xy(1,rows[index]) );
      }

   }

   /**
    * Helper method to load an image file from the CLASSPATH
    * @param imageName the package and name of the file to load relative to the CLASSPATH
    * @return an ImageIcon instance with the specified image file
    * @throws IllegalArgumentException if the image resource cannot be loaded.
    */
   public ImageIcon loadImage( String imageName )
   {
      try
      {
         ClassLoader classloader = getClass().getClassLoader();
         java.net.URL url = classloader.getResource( imageName );
         if ( url != null )
         {
            ImageIcon icon = new ImageIcon( url );
            return icon;
         }
      }
      catch( Exception e )
      {
         e.printStackTrace();
      }
      throw new IllegalArgumentException( "Unable to load image: " + imageName );
   }

   /**
    * Method for recalculating the component orientation for 
    * right-to-left Locales.
    * @param orientation the component orientation to be applied
    */
   public void applyComponentOrientation( ComponentOrientation orientation )
   {
      // Not yet implemented...
      // I18NUtils.applyComponentOrientation(this, orientation);
      super.applyComponentOrientation(orientation);
   }

   public JPanel createPanel()
   {
      JPanel jpanel1 = new JPanel();
      FormLayout formlayout1 = new FormLayout("FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:GROW(1.0),FILL:DEFAULT:NONE,FILL:4DLU:NONE,LEFT:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:NONE","CENTER:DEFAULT:NONE,CENTER:DEFAULT:NONE,CENTER:2DLU:NONE,CENTER:DEFAULT:NONE,CENTER:DEFAULT:NONE,CENTER:DEFAULT:NONE,CENTER:DEFAULT:NONE,CENTER:DEFAULT:NONE,CENTER:DEFAULT:NONE,CENTER:2DLU:NONE,CENTER:DEFAULT:NONE,CENTER:2DLU:NONE,CENTER:DEFAULT:NONE,CENTER:DEFAULT:NONE");
      CellConstraints cc = new CellConstraints();
      jpanel1.setLayout(formlayout1);

      _getFromLayerLabel.setName("getFromLayerLabel");
      _getFromLayerLabel.setText("Get WKT from layer");
      jpanel1.add(_getFromLayerLabel,cc.xy(2,2));

      _getWktFromLayerArea.setName("getWktFromLayerArea");
      _getWktFromLayerArea.setRows(8);
      JScrollPane jscrollpane1 = new JScrollPane();
      jscrollpane1.setViewportView(_getWktFromLayerArea);
      jscrollpane1.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
      jscrollpane1.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
      jpanel1.add(jscrollpane1,cc.xywh(2,4,5,1));

      _copyWktButton.setName("copyWktButton");
      jpanel1.add(_copyWktButton,cc.xy(8,4));

      _getWktFromLayerButton.setActionCommand("get WKT");
      _getWktFromLayerButton.setName("getWktFromLayerButton");
      _getWktFromLayerButton.setText("get WKT");
      jpanel1.add(_getWktFromLayerButton,cc.xy(6,2));

      _putToLayerLabel.setName("putToLayerLabel");
      _putToLayerLabel.setText("Put WKT into layer");
      jpanel1.add(_putToLayerLabel,cc.xy(2,9));

      _putWktToLayerButton.setActionCommand("put WKT");
      _putWktToLayerButton.setName("putWktToLayerButton");
      _putWktToLayerButton.setText("put WKT");
      jpanel1.add(_putWktToLayerButton,cc.xy(6,9));

      _putWktToLayerArea.setName("putWktToLayerArea");
      _putWktToLayerArea.setRows(8);
      JScrollPane jscrollpane2 = new JScrollPane();
      jscrollpane2.setViewportView(_putWktToLayerArea);
      jscrollpane2.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
      jscrollpane2.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
      jpanel1.add(jscrollpane2,cc.xywh(2,11,5,1));

      _zoomBufferLabel.setName("zoomBufferLabel");
      _zoomBufferLabel.setText("zoom buffer");
      jpanel1.add(_zoomBufferLabel,cc.xy(4,13));

      _zoomBufferField.setName("zoomBufferField");
      jpanel1.add(_zoomBufferField,new CellConstraints(6,13,1,1,CellConstraints.FILL,CellConstraints.DEFAULT));

      _zoomToCheckbox.setActionCommand("zoom to inserted geometries");
      _zoomToCheckbox.setName("zoomToCheckbox");
      _zoomToCheckbox.setText("zoom to inserted geometries");
      jpanel1.add(_zoomToCheckbox,cc.xy(2,13));

      jpanel1.add(createPanel1(),cc.xywh(2,6,5,1));
      addFillComponents(jpanel1,new int[]{ 1,2,3,4,5,6,7,8,9 },new int[]{ 1,2,3,4,5,6,7,8,9,10,11,12,13,14 });
      return jpanel1;
   }

   public JPanel createPanel1()
   {
      JPanel jpanel1 = new JPanel();
      FormLayout formlayout1 = new FormLayout("FILL:DEFAULT:NONE,FILL:4DLU:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:4DLU:NONE,FILL:DEFAULT:NONE","CENTER:DEFAULT:NONE");
      CellConstraints cc = new CellConstraints();
      jpanel1.setLayout(formlayout1);

      _selectCrsLabel.setName("selectCrsLabel");
      _selectCrsLabel.setText("Select optional CRS for copy");
      jpanel1.add(_selectCrsLabel,cc.xy(1,1));

      _crsTextField.setName("crsTextField");
      jpanel1.add(_crsTextField,cc.xywh(3,1,14,1));

      _selectCrsButton.setActionCommand(" ... ");
      _selectCrsButton.setName("selectCrsButton");
      _selectCrsButton.setText(" ... ");
      jpanel1.add(_selectCrsButton,cc.xy(18,1));

      addFillComponents(jpanel1,new int[]{ 2,4,5,6,7,8,9,10,11,12,13,14,15,16,17 },new int[0]);
      return jpanel1;
   }

   /**
    * Initializer
    */
   protected void initializePanel()
   {
      setLayout(new BorderLayout());
      add(createPanel(), BorderLayout.CENTER);
   }


}
