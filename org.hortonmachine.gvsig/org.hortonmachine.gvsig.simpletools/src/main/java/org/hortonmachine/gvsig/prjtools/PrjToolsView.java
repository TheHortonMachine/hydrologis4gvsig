package org.hortonmachine.gvsig.prjtools;

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
import javax.swing.JScrollPane;
import javax.swing.JTextArea;


public class PrjToolsView extends JPanel
{
   JLabel layerToPrjLabel = new JLabel();
   JComboBox layerToPrjCombo = new JComboBox();
   JLabel selectPrjLabel = new JLabel();
   JLabel customPrjLabel = new JLabel();
   JTextArea customPrjArea = new JTextArea();
   JButton applyPrjButton = new JButton();
   JButton choosePrjButton = new JButton();

   /**
    * Default constructor
    */
   public PrjToolsView()
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
      FormLayout formlayout1 = new FormLayout("FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:4DLU:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:4DLU:NONE,FILL:DEFAULT:GROW(0.3),FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:GROW(1.0),FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:NONE","CENTER:DEFAULT:NONE,CENTER:DEFAULT:NONE,CENTER:DEFAULT:NONE,CENTER:DEFAULT:NONE,CENTER:DEFAULT:NONE,FILL:DEFAULT:GROW(1.0),CENTER:DEFAULT:NONE");
      CellConstraints cc = new CellConstraints();
      jpanel1.setLayout(formlayout1);

      layerToPrjLabel.setName("layerToPrjLabel");
      layerToPrjLabel.setText("Layer to add prj file to");
      jpanel1.add(layerToPrjLabel,cc.xy(2,2));

      layerToPrjCombo.setName("layerToPrjCombo");
      jpanel1.add(layerToPrjCombo,cc.xywh(4,2,10,1));

      selectPrjLabel.setName("selectPrjLabel");
      selectPrjLabel.setText("Select CRS");
      jpanel1.add(selectPrjLabel,cc.xy(2,4));

      customPrjLabel.setName("customPrjLabel");
      customPrjLabel.setText("Customize");
      jpanel1.add(customPrjLabel,new CellConstraints(2,6,1,1,CellConstraints.DEFAULT,CellConstraints.CENTER));

      customPrjArea.setName("customPrjArea");
      customPrjArea.setRows(8);
      JScrollPane jscrollpane1 = new JScrollPane();
      jscrollpane1.setViewportView(customPrjArea);
      jscrollpane1.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
      jscrollpane1.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
      jpanel1.add(jscrollpane1,cc.xywh(4,6,8,1));

      applyPrjButton.setActionCommand("Apply");
      applyPrjButton.setName("applyPrjButton");
      applyPrjButton.setText("Apply");
      jpanel1.add(applyPrjButton,new CellConstraints(13,6,1,1,CellConstraints.DEFAULT,CellConstraints.CENTER));

      choosePrjButton.setActionCommand("Apply");
      choosePrjButton.setName("choosePrjButton");
      choosePrjButton.setText("...");
      jpanel1.add(choosePrjButton,cc.xy(4,4));

      addFillComponents(jpanel1,new int[]{ 1,2,3,4,5,6,7,8,9,10,11,12,13,14 },new int[]{ 1,2,3,4,5,6,7 });
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
