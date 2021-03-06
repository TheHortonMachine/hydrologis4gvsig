package org.hortonmachine.gvsig.geopaparazzi;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.ComponentOrientation;
import java.awt.Container;
import java.awt.Dimension;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;


public class GeopaparazziPanelView extends JPanel
{
   JList geopaparazziLayersList = new JList();
   JLabel projectDescriptionLabel = new JLabel();
   JTable descriptionTable = new JTable();
   JLabel availableLayersLabel = new JLabel();
   JButton browseButton = new JButton();
   JTextField geopaparazziDatabasePathField = new JTextField();
   JLabel geopaparazziLabel = new JLabel();
   JCheckBox exportshapesCheckBox = new JCheckBox();

   /**
    * Default constructor
    */
   public GeopaparazziPanelView()
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
      FormLayout formlayout1 = new FormLayout("FILL:DEFAULT:NONE,FILL:DEFAULT:GROW(1.0),FILL:DEFAULT:NONE","CENTER:DEFAULT:NONE,CENTER:DEFAULT:NONE,CENTER:DEFAULT:NONE,CENTER:DEFAULT:NONE,CENTER:80DLU:NONE,CENTER:DEFAULT:NONE,CENTER:DEFAULT:NONE,FILL:DEFAULT:GROW(0.5),CENTER:2DLU:NONE,CENTER:DEFAULT:NONE,CENTER:2DLU:NONE,CENTER:DEFAULT:NONE");
      CellConstraints cc = new CellConstraints();
      jpanel1.setLayout(formlayout1);

      geopaparazziLayersList.setName("geopaparazziLayersList");
      JScrollPane jscrollpane1 = new JScrollPane();
      jscrollpane1.setViewportView(geopaparazziLayersList);
      jscrollpane1.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
      jscrollpane1.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
      jpanel1.add(jscrollpane1,cc.xy(2,8));

      projectDescriptionLabel.setName("projectDescriptionLabel");
      projectDescriptionLabel.setText("Project Description");
      jpanel1.add(projectDescriptionLabel,cc.xy(2,4));

      descriptionTable.setName("descriptionTable");
      JScrollPane jscrollpane2 = new JScrollPane();
      jscrollpane2.setViewportView(descriptionTable);
      jscrollpane2.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
      jscrollpane2.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
      jpanel1.add(jscrollpane2,cc.xy(2,5));

      availableLayersLabel.setName("availableLayersLabel");
      availableLayersLabel.setText("Available Layers");
      jpanel1.add(availableLayersLabel,cc.xy(2,7));

      jpanel1.add(createPanel1(),cc.xy(2,2));
      exportshapesCheckBox.setActionCommand("export layers to shapefile (see tooltip)");
      exportshapesCheckBox.setName("exportshapesCheckBox");
      exportshapesCheckBox.setText("export layers to shapefile (see tooltip)");
      exportshapesCheckBox.setToolTipText("This is necessary to save the maps in the project and query the layers properly. A folder will be created beside the database file.");
      jpanel1.add(exportshapesCheckBox,cc.xy(2,10));

      addFillComponents(jpanel1,new int[]{ 1,2,3 },new int[]{ 1,2,3,4,5,6,7,8,9,10,11,12 });
      return jpanel1;
   }

   public JPanel createPanel1()
   {
      JPanel jpanel1 = new JPanel();
      FormLayout formlayout1 = new FormLayout("FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:GROW(1.0),FILL:DEFAULT:NONE,FILL:DEFAULT:NONE","CENTER:DEFAULT:NONE");
      CellConstraints cc = new CellConstraints();
      jpanel1.setLayout(formlayout1);

      browseButton.setActionCommand("JButton");
      browseButton.setName("browseButton");
      browseButton.setText("...");
      jpanel1.add(browseButton,cc.xy(5,1));

      geopaparazziDatabasePathField.setBackground(new Color(236,233,216));
      geopaparazziDatabasePathField.setEditable(false);
      geopaparazziDatabasePathField.setName("geopaparazziDatabasePathField");
      jpanel1.add(geopaparazziDatabasePathField,cc.xy(3,1));

      geopaparazziLabel.setName("geopaparazziLabel");
      geopaparazziLabel.setText("Geopaparazzi database");
      jpanel1.add(geopaparazziLabel,cc.xy(1,1));

      addFillComponents(jpanel1,new int[]{ 2,4 },new int[0]);
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
