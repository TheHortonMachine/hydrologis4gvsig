///*
// * Stage - Spatial Toolbox And Geoscript Environment 
// * (C) HydroloGIS - www.hydrologis.com 
// *
// * All rights reserved. This program and the accompanying materials
// * are made available under the terms of the Eclipse Public License v1.0
// * (http://www.eclipse.org/legal/epl-v10.html).
// */
//package org.jgrasstools.gvsig.spatialtoolbox.widgets;
//
//import java.awt.Component;
//import java.awt.GridBagConstraints;
//import java.awt.GridBagLayout;
//
//import javax.swing.JCheckBox;
//import javax.swing.JComponent;
//
//import org.jgrasstools.gvsig.spatialtoolbox.FieldData;
//
///**
// * Class representing a gui for boolean choice.
// * 
// * @author Andrea Antonello (www.hydrologis.com)
// */
//public class GuiBooleanField extends ModuleGuiElement {
//
//    private String constraints;
//    private final FieldData data;
//    private JCheckBox checkButton;
//
//    public GuiBooleanField( FieldData data, String constraints ) {
//        this.data = data;
//        this.constraints = constraints;
//
//    }
//
//    @Override
//    public JComponent makeGui( JComponent parent ) {
//
//        parent.setLayout(new GridBagLayout());
//
//        GridBagConstraints c = new GridBagConstraints();
//        c.gridx = 0;
//        c.gridy = 0;
//        c.weightx = 1;
//
//        checkButton = new JCheckBox();
//        parent.add(checkButton, c);
//        checkButton.setText("");
//
//        boolean checked = Boolean.parseBoolean(data.fieldValue);
//        checkButton.setSelection(checked);
//        checkButton.addSelectionListener(new SelectionAdapter(){
//            public void widgetSelected( SelectionEvent e ) {
//                if (checkButton.getSelection()) {
//                    data.fieldValue = String.valueOf(true);
//                } else {
//                    data.fieldValue = String.valueOf(false);
//                }
//            }
//        });
//
//        return checkButton;
//    }
//
//    public FieldData getFieldData() {
//        return data;
//    }
//
//    public boolean hasData() {
//        return true;
//    }
//
//    @Override
//    public String validateContent() {
//        return null;
//    }
//
//}
