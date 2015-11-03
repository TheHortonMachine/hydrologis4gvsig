package org.jgrasstools.gvsig.geopaparazzi;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import java.awt.BorderLayout;
import java.awt.ComponentOrientation;
import java.awt.Container;
import java.awt.Dimension;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;


public class GenerateTilesParametersPanelView extends JPanel
{
   JComboBox minZoomCombo = new JComboBox();
   JLabel maxZoomLabel = new JLabel();
   JComboBox maxZoomCombo = new JComboBox();
   JLabel outputFolderLabel = new JLabel();
   JLabel nameLabel = new JLabel();
   JTextField nameField = new JTextField();
   JLabel imageTypeLabel = new JLabel();
   JComboBox imageTypeCombo = new JComboBox();
   JButton browseFolderButton = new JButton();
   JTextField outputFolderField = new JTextField();
   JButton okButton = new JButton();
   JButton cancelButton = new JButton();

   /**
    * Default constructor
    */
   public GenerateTilesParametersPanelView()
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
      FormLayout formlayout1 = new FormLayout("FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:GROW(1.0),FILL:DEFAULT:NONE","CENTER:DEFAULT:NONE,CENTER:DEFAULT:NONE,CENTER:DEFAULT:NONE,CENTER:DEFAULT:NONE,CENTER:DEFAULT:NONE,CENTER:DEFAULT:NONE,CENTER:DEFAULT:NONE,CENTER:DEFAULT:NONE,CENTER:DEFAULT:NONE,CENTER:DEFAULT:NONE,CENTER:DEFAULT:NONE,CENTER:DEFAULT:NONE");
      CellConstraints cc = new CellConstraints();
      jpanel1.setLayout(formlayout1);

      JLabel jlabel1 = new JLabel();
      jlabel1.setText("Minimum zoom level");
      jpanel1.add(jlabel1,cc.xy(2,2));

      minZoomCombo.setName("minZoomCombo");
      jpanel1.add(minZoomCombo,cc.xy(4,2));

      maxZoomLabel.setName("maxZoomLabel");
      maxZoomLabel.setText("Maximum zoom level");
      jpanel1.add(maxZoomLabel,cc.xy(2,4));

      maxZoomCombo.setName("maxZoomCombo");
      jpanel1.add(maxZoomCombo,cc.xy(4,4));

      outputFolderLabel.setName("outputFolderLabel");
      outputFolderLabel.setText("Output folder");
      jpanel1.add(outputFolderLabel,cc.xy(2,10));

      nameLabel.setName("nameLabel");
      nameLabel.setText("Dataset name");
      jpanel1.add(nameLabel,cc.xy(2,8));

      nameField.setName("nameField");
      jpanel1.add(nameField,cc.xy(4,8));

      imageTypeLabel.setName("imageTypeLabel");
      imageTypeLabel.setText("Image type used");
      jpanel1.add(imageTypeLabel,cc.xy(2,6));

      imageTypeCombo.setName("imageTypeCombo");
      jpanel1.add(imageTypeCombo,cc.xy(4,6));

      jpanel1.add(createPanel1(),cc.xy(4,10));
      jpanel1.add(createPanel2(),new CellConstraints(2,12,3,1,CellConstraints.RIGHT,CellConstraints.CENTER));
      addFillComponents(jpanel1,new int[]{ 1,2,3,4,5 },new int[]{ 1,2,3,4,5,6,7,8,9,10,11,12 });
      return jpanel1;
   }

   public JPanel createPanel1()
   {
      JPanel jpanel1 = new JPanel();
      FormLayout formlayout1 = new FormLayout("FILL:DEFAULT:GROW(1.0),FILL:4DLU:NONE,FILL:DEFAULT:NONE","CENTER:DEFAULT:NONE");
      CellConstraints cc = new CellConstraints();
      jpanel1.setLayout(formlayout1);

      browseFolderButton.setActionCommand("...");
      browseFolderButton.setName("browseFolderButton");
      browseFolderButton.setText("...");
      jpanel1.add(browseFolderButton,cc.xy(3,1));

      outputFolderField.setName("outputFolderField");
      jpanel1.add(outputFolderField,cc.xy(1,1));

      addFillComponents(jpanel1,new int[]{ 2 },new int[0]);
      return jpanel1;
   }

   public JPanel createPanel2()
   {
      JPanel jpanel1 = new JPanel();
      FormLayout formlayout1 = new FormLayout("FILL:DEFAULT:NONE,FILL:4DLU:NONE,FILL:DEFAULT:NONE","CENTER:DEFAULT:NONE");
      CellConstraints cc = new CellConstraints();
      jpanel1.setLayout(formlayout1);

      okButton.setActionCommand("Ok");
      okButton.setName("okButton");
      okButton.setText("Ok");
      jpanel1.add(okButton,cc.xy(1,1));

      cancelButton.setActionCommand("Cancel");
      cancelButton.setName("cancelButton");
      cancelButton.setText("Cancel");
      jpanel1.add(cancelButton,cc.xy(3,1));

      addFillComponents(jpanel1,new int[]{ 2 },new int[0]);
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
