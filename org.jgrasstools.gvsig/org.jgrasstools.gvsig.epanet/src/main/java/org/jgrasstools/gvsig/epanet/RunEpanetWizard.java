package org.jgrasstools.gvsig.epanet;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.HashMap;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.jgrasstools.hortonmachine.modules.networktools.epanet.core.TimeParameterCodes;

import jwizardcomponent.JWizardComponents;
import jwizardcomponent.JWizardPanel;
import jwizardcomponent.Utilities;
import jwizardcomponent.frame.JWizardFrame;

/**
 * Epanet run wizard.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class RunEpanetWizard extends JWizardFrame {

    public static final int PANEL_GeneralParametersWizardPage = 0;
    public static final int PANEL_TimeParametersWizardPage = 1;
    public static final int PANEL_OptionsParametersWizardPage = 2;
    public static final int PANEL_ExtraFilesWizardPage = 3;

    private JTextField P1_titleText;
    private JTextField P1_descriptionText;
    private JTextField P1_userText;
    // private PreferencesNode preferences;

    private HashMap<TimeParameterCodes, JTextField> timeKey2ValueMap = new HashMap<TimeParameterCodes, JTextField>();

    public RunEpanetWizard() {
        super();

        // ApplicationManager applicationManager = ApplicationLocator.getManager();
        // preferences = applicationManager.getPreferences();
        init();
    }

    private void init() {
        this.setTitle("Epanet Run Wizard");
        // this.setIconImage("Epanet Run Wizard");

        JWizardPanel panel = new GeneralParametersWizardPage(getWizardComponents());
        getWizardComponents().addWizardPanel(PANEL_GeneralParametersWizardPage, panel);

        panel = new TimeParametersWizardPanel(getWizardComponents());
        getWizardComponents().addWizardPanel(PANEL_TimeParametersWizardPage, panel);

        // panel = new OptionWizardPanel(getWizardComponents(), "A");
        // getWizardComponents().addWizardPanel(PANEL_OptionsParametersWizardPage, panel);
        //
        // panel = new LastWizardPanel(getWizardComponents());
        // getWizardComponents().addWizardPanel(PANEL_ExtraFilesWizardPage, panel);

        setSize(500, 400);
        Utilities.centerComponentOnScreen(this);
    }

    public static void main( String[] args ) {
        RunEpanetWizard wizard = new RunEpanetWizard();
        wizard.setVisible(true);
    }

    private class GeneralParametersWizardPage extends JWizardPanel {
        private static final long serialVersionUID = 1L;

        public GeneralParametersWizardPage( JWizardComponents wizardComponents ) {
            super(wizardComponents, "Define general parameters");
            setPanelTitle("Define general parameters");
            // setDescription("Insert general parameters to use to describe the simulation.");
            this.setLayout(new GridBagLayout());

            int row = 0;
            int col = 0;
            int width = 1;
            int height = 1;
            int times = 3;
            JLabel titleLabel = new JLabel("Title");
            Insets insets = new Insets(5, 5, 5, 5);
            add(titleLabel, new GridBagConstraints(col, row, width, height, 0.0, 0.0, GridBagConstraints.NORTHWEST,
                    GridBagConstraints.BOTH, insets, 0, 0));

            P1_titleText = new JTextField("New Epanet Run");
            add(P1_titleText, new GridBagConstraints(col + 1, row++, width * times, height, 2.0, 0.0,
                    GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, insets, 0, 0));
            DocumentListener textListener = new DocumentListener(){
                public void changedUpdate( DocumentEvent e ) {
                    update();
                }
                public void removeUpdate( DocumentEvent e ) {
                    update();
                }
                public void insertUpdate( DocumentEvent e ) {
                    update();
                }
            };
            P1_titleText.getDocument().addDocumentListener(textListener);

            JLabel descriptionLabel = new JLabel("Description");
            add(descriptionLabel, new GridBagConstraints(col, row, width, height, 0.0, 0.0, GridBagConstraints.NORTHWEST,
                    GridBagConstraints.BOTH, insets, 0, 0));
            P1_descriptionText = new JTextField("New Epanet Run Description");
            add(P1_descriptionText, new GridBagConstraints(col + 1, row++, width * times, height, 2.0, 0.0,
                    GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, insets, 0, 0));
            P1_descriptionText.getDocument().addDocumentListener(textListener);

            JLabel userLabel = new JLabel("User");
            add(userLabel, new GridBagConstraints(col, row, width, height, 0.0, 0.0, GridBagConstraints.NORTHWEST,
                    GridBagConstraints.BOTH, insets, 0, 0));
            String userStr = System.getProperty("user.name");
            P1_userText = new JTextField(userStr);
            add(P1_userText, new GridBagConstraints(col + 1, row++, width * times, height, 2.0, 0.0, GridBagConstraints.NORTHWEST,
                    GridBagConstraints.BOTH, insets, 0, 0));
            P1_userText.getDocument().addDocumentListener(textListener);

            update();
        }

        public void update() {
            setNextButtonEnabled(allTextsFilled());
            setFinishButtonEnabled(false);
            setBackButtonEnabled(false);
        }

        private boolean allTextsFilled() {
            return P1_titleText.getText().trim().length() > 0 && P1_descriptionText.getText().trim().length() > 0
                    && P1_userText.getText().trim().length() > 0;
        }
    }

    private class TimeParametersWizardPanel extends JWizardPanel {
        private static final long serialVersionUID = 1L;

        public TimeParametersWizardPanel( JWizardComponents wizardComponents ) {
            super(wizardComponents, "Define time parameters");
            setPanelTitle("Define time parameters");
            // setDescription("Insert the time parameters to use during the simulation.");
            this.setLayout(new GridBagLayout());

            DocumentListener textListener = new DocumentListener(){
                public void changedUpdate( DocumentEvent e ) {
                    update();
                }
                public void removeUpdate( DocumentEvent e ) {
                    update();
                }
                public void insertUpdate( DocumentEvent e ) {
                    update();
                }
            };

            TimeParameterCodes[] values = TimeParameterCodes.values();
            int row = 0;
            int col = 0;
            int width = 1;
            int height = 1;
            int times = 3;
            Insets insets = new Insets(5, 5, 5, 5);
            for( TimeParameterCodes timeParameterCode : values ) {
                String label = timeParameterCode.getKey();
                String tooltip = timeParameterCode.getDescription();
                String value = timeParameterCode.getDefaultValue();

                // value = preferences.get(label, value);

                JLabel codeLabel = new JLabel(label);
                codeLabel.setToolTipText(tooltip);
                add(codeLabel, new GridBagConstraints(col, row, width, height, 0.0, 0.0, GridBagConstraints.NORTHWEST,
                        GridBagConstraints.BOTH, insets, 0, 0));

                JTextField textField = new JTextField(value);
                add(textField, new GridBagConstraints(col + 1, row++, width * times, height, 2.0, 0.0,
                        GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, insets, 0, 0));
                textField.getDocument().addDocumentListener(textListener);

                timeKey2ValueMap.put(timeParameterCode, textField);
            }

            update();
        }

        public void update() {
            setNextButtonEnabled(allTextsFilled());
            setFinishButtonEnabled(false);
            setBackButtonEnabled(true);
        }

        private boolean allTextsFilled() {
            for( JTextField textField : timeKey2ValueMap.values() ) {
                if (textField.getText().trim().length() == 0) {
                    return false;
                }
            }
            return true;
        }
    }

    // class OptionWizardPanel extends LabelWizardPanel {
    // public OptionWizardPanel( JWizardComponents wizardComponents, String option ) {
    // super(wizardComponents, "Option " + option + " was choosed");
    // setPanelTitle("Option " + option + " panel");
    // }
    // public void next() {
    // switchPanel(RunEpanetWizard.PANEL_ExtraFilesWizardPage);
    // }
    // public void back() {
    // switchPanel(RunEpanetWizard.PANEL_TimeParametersWizardPage);
    // }
    // }
    //
    // class LastWizardPanel extends LabelWizardPanel {
    // public LastWizardPanel( JWizardComponents wizardComponents ) {
    // super(wizardComponents, "Last panel, you can finish now");
    // setPanelTitle("Last simple static panel");
    // }
    // }
}