package org.hortonmachine.gvsig.pointinfo;

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
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;


public class PointInfoView extends JPanel
{
   JLabel lonLabel = new JLabel();
   JLabel latLabel = new JLabel();
   JTextField lonTextField = new JTextField();
   JTextField latTextField = new JTextField();
   JLabel crsLabel = new JLabel();
   JButton crsButton = new JButton();
   JTextField epsgField = new JTextField();
   JLabel xLabel = new JLabel();
   JLabel yLabel = new JLabel();
   JTextField xField = new JTextField();
   JTextField yField = new JTextField();
   JButton copyLonButton = new JButton();
   JButton copyXButton = new JButton();
   JButton copyLatButton = new JButton();
   JButton copyYButton = new JButton();

   /**
    * Default constructor
    */
   public PointInfoView()
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
      FormLayout formlayout1 = new FormLayout("FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:4DLU:NONE,FILL:DEFAULT:GROW(1.0),FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:NONE","CENTER:DEFAULT:NONE,CENTER:DEFAULT:NONE,CENTER:2DLU:NONE,CENTER:DEFAULT:NONE,CENTER:DEFAULT:NONE,CENTER:DEFAULT:NONE,CENTER:DEFAULT:NONE,CENTER:DEFAULT:NONE,CENTER:2DLU:NONE,CENTER:DEFAULT:NONE,CENTER:DEFAULT:NONE");
      CellConstraints cc = new CellConstraints();
      jpanel1.setLayout(formlayout1);

      lonLabel.setName("lonLabel");
      lonLabel.setText("Easting");
      jpanel1.add(lonLabel,cc.xy(2,2));

      latLabel.setName("latLabel");
      latLabel.setText("Northing");
      jpanel1.add(latLabel,cc.xy(2,4));

      lonTextField.setBackground(new Color(236,233,216));
      lonTextField.setEditable(false);
      lonTextField.setName("lonTextField");
      jpanel1.add(lonTextField,cc.xy(4,2));

      latTextField.setBackground(new Color(236,233,216));
      latTextField.setEditable(false);
      latTextField.setName("latTextField");
      jpanel1.add(latTextField,cc.xy(4,4));

      jpanel1.add(createPanel1(),cc.xywh(2,6,3,1));
      xLabel.setName("xLabel");
      xLabel.setText("Reprojected Easting");
      jpanel1.add(xLabel,cc.xy(2,8));

      yLabel.setName("yLabel");
      yLabel.setText("Reprojected Northing");
      jpanel1.add(yLabel,cc.xy(2,10));

      xField.setBackground(new Color(236,233,216));
      xField.setEditable(false);
      xField.setName("xField");
      jpanel1.add(xField,cc.xy(4,8));

      yField.setBackground(new Color(236,233,216));
      yField.setEditable(false);
      yField.setName("yField");
      jpanel1.add(yField,cc.xy(4,10));

      copyLonButton.setName("copyLonButton");
      jpanel1.add(copyLonButton,cc.xy(6,2));

      copyXButton.setName("copyXButton");
      jpanel1.add(copyXButton,cc.xy(6,8));

      copyLatButton.setName("copyLatButton");
      jpanel1.add(copyLatButton,cc.xy(6,4));

      copyYButton.setName("copyYButton");
      jpanel1.add(copyYButton,cc.xy(6,10));

      addFillComponents(jpanel1,new int[]{ 1,2,3,4,5,6,7 },new int[]{ 1,2,3,4,5,6,7,8,9,10,11 });
      return jpanel1;
   }

   public JPanel createPanel1()
   {
      JPanel jpanel1 = new JPanel();
      FormLayout formlayout1 = new FormLayout("FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:GROW(1.0)","CENTER:DEFAULT:NONE");
      CellConstraints cc = new CellConstraints();
      jpanel1.setLayout(formlayout1);

      crsLabel.setName("crsLabel");
      crsLabel.setText("Select Coordinate reference System");
      jpanel1.add(crsLabel,cc.xy(1,1));

      crsButton.setActionCommand("...");
      crsButton.setName("crsButton");
      crsButton.setText("...");
      jpanel1.add(crsButton,cc.xy(3,1));

      epsgField.setBackground(new Color(236,233,216));
      epsgField.setEditable(false);
      epsgField.setName("epsgField");
      jpanel1.add(epsgField,cc.xy(5,1));

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
