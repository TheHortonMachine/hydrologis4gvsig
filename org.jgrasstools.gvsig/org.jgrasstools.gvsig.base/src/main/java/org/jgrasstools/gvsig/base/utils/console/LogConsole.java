package org.jgrasstools.gvsig.base.utils.console;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.io.PrintStream;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.BadLocationException;

import org.gvsig.fmap.dal.NewDataStoreParameters;
import org.gvsig.tools.swing.api.Component;
import org.jgrasstools.gears.libs.modules.JGTConstants;
import org.jgrasstools.gears.libs.monitor.IJGTProgressMonitor;
import org.joda.time.DateTime;

public class LogConsole extends JPanel implements Component {
    /**
     * The text area which is used for displaying logging information.
     */
    private JTextArea textArea;

    private JButton buttonClear = new JButton("Clear");
    private JButton buttonKill = new JButton("Interrupt");

    private IJGTProgressMonitor pm;

    private String processName;

    public LogConsole( final IJGTProgressMonitor pm ) {
        this.pm = pm;
        textArea = new JTextArea(50, 20);
        textArea.setEditable(false);
        PrintStream printStream = new PrintStream(new CustomOutputStream(textArea));

        // re-assigns standard output stream and error output stream
        System.setOut(printStream);
        System.setErr(printStream);

        setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.insets = new Insets(10, 10, 10, 10);
        constraints.anchor = GridBagConstraints.WEST;

        add(buttonClear, constraints);

        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.gridwidth = 2;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 1.0;
        constraints.weighty = 1.0;

        add(new JScrollPane(textArea), constraints);

        // adds event handler for button Clear
        buttonClear.addActionListener(new ActionListener(){
            public void actionPerformed( ActionEvent evt ) {
                // clears the text area
                try {
                    textArea.getDocument().remove(0, textArea.getDocument().getLength());
                } catch (BadLocationException ex) {
                    ex.printStackTrace();
                }
            }
        });
        buttonKill.addActionListener(new ActionListener(){
            public void actionPerformed( ActionEvent evt ) {
                if (pm != null)
                    pm.setCanceled(true);
            }
        });

        setPreferredSize(new Dimension(480, 320));

        addComponentListener(new ComponentListener(){

            public void componentShown( ComponentEvent e ) {
            }

            public void componentResized( ComponentEvent e ) {
            }

            public void componentMoved( ComponentEvent e ) {
            }

            public void componentHidden( ComponentEvent e ) {
                stopLogging();
            }

        });

    }

    public void startProcess( String name ) {
        processName = name;
        System.out.println("Process " + name + " started at: "
                + new DateTime().toString(JGTConstants.dateTimeFormatterYYYYMMDDHHMMSS) + "\n\n");
    }

    public void stopProcess() {
        System.out.println("\n\nProcess " + processName + " stopped at: "
                + new DateTime().toString(JGTConstants.dateTimeFormatterYYYYMMDDHHMMSS));
    }

    public void stopLogging() {
        System.setOut(System.out);
        System.setErr(System.err);
    }

    public JComponent asJComponent() {
        return this;
    }

    public IJGTProgressMonitor getProgressMonitor() {
        return pm;
    }
}