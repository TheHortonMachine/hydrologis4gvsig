package org.jgrasstools.gvsig.spatialtoolbox.core.gui;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import java.awt.BorderLayout;
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


public class ParameterExampleView extends JPanel
{
   JPanel mainPanel = new JPanel();
   JLabel browseTextFieldLabel = new JLabel();
   JPanel valuesPanel = new JPanel();
   JTextField browseTextField = new JTextField();
   JButton browseButton = new JButton();
   JLabel textFieldLabel = new JLabel();
   JTextField textField = new JTextField();

   /**
    * Default constructor
    */
   public ParameterExampleView()
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

   public JPanel createmainPanel()
   {
      mainPanel.setName("mainPanel");
      FormLayout formlayout1 = new FormLayout("FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:GROW(1.0),FILL:DEFAULT:NONE","CENTER:2DLU:NONE,CENTER:DEFAULT:NONE,CENTER:2DLU:NONE,FILL:DEFAULT:NONE,CENTER:2DLU:NONE");
      CellConstraints cc = new CellConstraints();
      mainPanel.setLayout(formlayout1);

      browseTextFieldLabel.setName("browseTextFieldLabel");
      browseTextFieldLabel.setText("label for browse textfield");
      mainPanel.add(browseTextFieldLabel,cc.xy(2,4));

      mainPanel.add(createvaluesPanel(),cc.xy(4,4));
      textFieldLabel.setName("textFieldLabel");
      textFieldLabel.setText("label for textfield");
      mainPanel.add(textFieldLabel,cc.xy(2,2));

      textField.setName("textField");
      mainPanel.add(textField,cc.xy(4,2));

      addFillComponents(mainPanel,new int[]{ 1,2,3,4,5 },new int[]{ 1,2,3,4,5 });
      return mainPanel;
   }

   public JPanel createvaluesPanel()
   {
      valuesPanel.setName("valuesPanel");
      FormLayout formlayout1 = new FormLayout("FILL:DEFAULT:GROW(1.0),FILL:4DLU:NONE,FILL:DEFAULT:NONE","CENTER:DEFAULT:NONE");
      CellConstraints cc = new CellConstraints();
      valuesPanel.setLayout(formlayout1);

      browseTextField.setName("browseTextField");
      valuesPanel.add(browseTextField,cc.xy(1,1));

      browseButton.setActionCommand("...");
      browseButton.setName("browseButton");
      browseButton.setText("...");
      valuesPanel.add(browseButton,cc.xy(3,1));

      addFillComponents(valuesPanel,new int[]{ 2 },new int[0]);
      return valuesPanel;
   }

   /**
    * Initializer
    */
   protected void initializePanel()
   {
      setLayout(new BorderLayout());
      add(createmainPanel(), BorderLayout.CENTER);
   }


}
