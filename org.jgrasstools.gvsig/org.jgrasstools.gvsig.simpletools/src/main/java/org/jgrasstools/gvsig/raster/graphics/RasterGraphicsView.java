package org.jgrasstools.gvsig.raster.graphics;

import com.jeta.open.i18n.I18NUtils;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import java.awt.BorderLayout;
import java.awt.ComponentOrientation;
import java.awt.Container;
import java.awt.Dimension;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;


public class RasterGraphicsView extends JPanel
{
   JLabel _layerLabel = new JLabel();
   JComboBox _rasterLayerCombo = new JComboBox();
   JCheckBox _showSteepestDirectionCheck = new JCheckBox();
   JLabel _numFormatLabel = new JLabel();
   JButton _refreshButton = new JButton();
   JButton _clearButton = new JButton();
   JTextField _numFormatField = new JTextField();
   JCheckBox _showCellsCheck = new JCheckBox();
   JLabel _textModeLabel = new JLabel();
   JRadioButton _mode1ValuesButton = new JRadioButton();
   ButtonGroup _buttongroup1 = new ButtonGroup();
   JRadioButton _mode2ColRowButton = new JRadioButton();
   JRadioButton _mode3ValuesColRowButton = new JRadioButton();
   JRadioButton _mode4NoTextButton = new JRadioButton();

   /**
    * Default constructor
    */
   public RasterGraphicsView()
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
      FormLayout formlayout1 = new FormLayout("FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:8DLU:GROW(1.0),FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:NONE","CENTER:DEFAULT:NONE,CENTER:DEFAULT:NONE,CENTER:DEFAULT:NONE,CENTER:DEFAULT:NONE,CENTER:DEFAULT:NONE,CENTER:DEFAULT:NONE,CENTER:DEFAULT:NONE,CENTER:DEFAULT:NONE,CENTER:2DLU:NONE,CENTER:DEFAULT:NONE,CENTER:2DLU:NONE,CENTER:DEFAULT:NONE,CENTER:2DLU:NONE,CENTER:DEFAULT:NONE,CENTER:DEFAULT:NONE,CENTER:DEFAULT:NONE,CENTER:DEFAULT:NONE,CENTER:DEFAULT:NONE,CENTER:DEFAULT:NONE,CENTER:DEFAULT:NONE,CENTER:DEFAULT:NONE,CENTER:DEFAULT:NONE,CENTER:DEFAULT:NONE,CENTER:DEFAULT:NONE,CENTER:DEFAULT:NONE,CENTER:DEFAULT:NONE");
      CellConstraints cc = new CellConstraints();
      jpanel1.setLayout(formlayout1);

      _layerLabel.setName("layerLabel");
      _layerLabel.setText("Layer");
      jpanel1.add(_layerLabel,cc.xy(2,2));

      _rasterLayerCombo.setName("rasterLayerCombo");
      jpanel1.add(_rasterLayerCombo,cc.xywh(4,2,16,1));

      _showSteepestDirectionCheck.setActionCommand("Show numbers");
      _showSteepestDirectionCheck.setName("showSteepestDirectionCheck");
      _showSteepestDirectionCheck.setText("Show steepest direction");
      jpanel1.add(_showSteepestDirectionCheck,cc.xywh(4,21,16,1));

      _numFormatLabel.setName("numFormatLabel");
      _numFormatLabel.setText("Number format");
      jpanel1.add(_numFormatLabel,cc.xywh(4,17,10,1));

      jpanel1.add(createPanel1(),cc.xywh(2,25,18,1));
      _numFormatField.setName("numFormatField");
      jpanel1.add(_numFormatField,cc.xywh(15,17,5,1));

      _showCellsCheck.setActionCommand("Show numbers");
      _showCellsCheck.setName("showCellsCheck");
      _showCellsCheck.setText("Show cells");
      jpanel1.add(_showCellsCheck,cc.xywh(4,4,16,1));

      _textModeLabel.setName("textModeLabel");
      _textModeLabel.setText("Labelling mode");
      jpanel1.add(_textModeLabel,cc.xywh(4,6,16,1));

      _mode1ValuesButton.setActionCommand("raster values");
      _mode1ValuesButton.setName("mode1ValuesButton");
      _mode1ValuesButton.setText("raster values");
      _buttongroup1.add(_mode1ValuesButton);
      jpanel1.add(_mode1ValuesButton,cc.xywh(5,8,12,1));

      _mode2ColRowButton.setActionCommand("cols and rows");
      _mode2ColRowButton.setName("mode2ColRowButton");
      _mode2ColRowButton.setText("cols and rows");
      _buttongroup1.add(_mode2ColRowButton);
      jpanel1.add(_mode2ColRowButton,cc.xywh(5,10,12,1));

      _mode3ValuesColRowButton.setActionCommand("raster values + cols and rows");
      _mode3ValuesColRowButton.setName("mode3ValuesColRowButton");
      _mode3ValuesColRowButton.setText("raster values + cols and rows");
      _buttongroup1.add(_mode3ValuesColRowButton);
      jpanel1.add(_mode3ValuesColRowButton,cc.xywh(5,12,12,1));

      _mode4NoTextButton.setActionCommand("raster values + cols and rows");
      _mode4NoTextButton.setName("mode4NoTextButton");
      _mode4NoTextButton.setText("no text");
      _buttongroup1.add(_mode4NoTextButton);
      jpanel1.add(_mode4NoTextButton,cc.xywh(5,14,12,1));

      addFillComponents(jpanel1,new int[]{ 1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20 },new int[]{ 1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26 });
      return jpanel1;
   }

   public JPanel createPanel1()
   {
      JPanel jpanel1 = new JPanel();
      FormLayout formlayout1 = new FormLayout("FILL:DEFAULT:GROW(1.0),FILL:DEFAULT:GROW(1.0),FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:GROW(1.0),FILL:DEFAULT:GROW(1.0)","CENTER:DEFAULT:NONE");
      CellConstraints cc = new CellConstraints();
      jpanel1.setLayout(formlayout1);

      _refreshButton.setActionCommand("Refresh");
      _refreshButton.setName("refreshButton");
      _refreshButton.setText("Refresh");
      jpanel1.add(_refreshButton,cc.xywh(2,1,3,1));

      _clearButton.setActionCommand("Refresh");
      _clearButton.setName("clearButton");
      _clearButton.setText("Clear");
      jpanel1.add(_clearButton,cc.xy(6,1));

      addFillComponents(jpanel1,new int[]{ 1,3,4,5,7 },new int[]{ 1 });
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
