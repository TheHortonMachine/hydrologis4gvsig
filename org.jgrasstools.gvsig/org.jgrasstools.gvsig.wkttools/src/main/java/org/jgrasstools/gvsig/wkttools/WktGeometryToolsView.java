package org.jgrasstools.gvsig.wkttools;

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
   JLabel getFromLayerLabel = new JLabel();
   JTextArea getWktFromLayerArea = new JTextArea();
   JButton copyWktButton = new JButton();
   JButton getWktFromLayerButton = new JButton();
   JLabel putToLayerLabel = new JLabel();
   JButton putWktToLayerButton = new JButton();
   JTextArea putWktToLayerArea = new JTextArea();
   JLabel zoomBufferLabel = new JLabel();
   JTextField zoomBufferField = new JTextField();
   JCheckBox zoomToCheckbox = new JCheckBox();

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
      FormLayout formlayout1 = new FormLayout("FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:GROW(1.0),FILL:DEFAULT:NONE,FILL:4DLU:NONE,LEFT:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:NONE","CENTER:DEFAULT:NONE,CENTER:DEFAULT:NONE,CENTER:2DLU:NONE,CENTER:DEFAULT:NONE,CENTER:DEFAULT:NONE,CENTER:DEFAULT:NONE,CENTER:DEFAULT:NONE,CENTER:2DLU:NONE,CENTER:DEFAULT:NONE,CENTER:2DLU:NONE,CENTER:DEFAULT:NONE,CENTER:DEFAULT:NONE");
      CellConstraints cc = new CellConstraints();
      jpanel1.setLayout(formlayout1);

      getFromLayerLabel.setName("getFromLayerLabel");
      getFromLayerLabel.setText("Get WKT from layer");
      jpanel1.add(getFromLayerLabel,cc.xy(2,2));

      getWktFromLayerArea.setName("getWktFromLayerArea");
      getWktFromLayerArea.setRows(8);
      JScrollPane jscrollpane1 = new JScrollPane();
      jscrollpane1.setViewportView(getWktFromLayerArea);
      jscrollpane1.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
      jscrollpane1.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
      jpanel1.add(jscrollpane1,cc.xywh(2,4,5,1));

      copyWktButton.setName("copyWktButton");
      jpanel1.add(copyWktButton,cc.xy(8,4));

      getWktFromLayerButton.setActionCommand("get WKT");
      getWktFromLayerButton.setName("getWktFromLayerButton");
      getWktFromLayerButton.setText("get WKT");
      jpanel1.add(getWktFromLayerButton,cc.xy(6,2));

      putToLayerLabel.setName("putToLayerLabel");
      putToLayerLabel.setText("Put WKT into layer");
      jpanel1.add(putToLayerLabel,cc.xy(2,7));

      putWktToLayerButton.setActionCommand("put WKT");
      putWktToLayerButton.setName("putWktToLayerButton");
      putWktToLayerButton.setText("put WKT");
      jpanel1.add(putWktToLayerButton,cc.xy(6,7));

      putWktToLayerArea.setName("putWktToLayerArea");
      putWktToLayerArea.setRows(8);
      JScrollPane jscrollpane2 = new JScrollPane();
      jscrollpane2.setViewportView(putWktToLayerArea);
      jscrollpane2.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
      jscrollpane2.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
      jpanel1.add(jscrollpane2,cc.xywh(2,9,5,1));

      zoomBufferLabel.setName("zoomBufferLabel");
      zoomBufferLabel.setText("zoom buffer");
      jpanel1.add(zoomBufferLabel,cc.xy(4,11));

      zoomBufferField.setName("zoomBufferField");
      jpanel1.add(zoomBufferField,new CellConstraints(6,11,1,1,CellConstraints.FILL,CellConstraints.DEFAULT));

      zoomToCheckbox.setActionCommand("zoom to inserted geometries");
      zoomToCheckbox.setName("zoomToCheckbox");
      zoomToCheckbox.setText("zoom to inserted geometries");
      jpanel1.add(zoomToCheckbox,cc.xy(2,11));

      addFillComponents(jpanel1,new int[]{ 1,2,3,4,5,6,7,8,9 },new int[]{ 1,2,3,4,5,6,7,8,9,10,11,12 });
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
