package org.hortonmachine.gvsig.base.utils.console;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.io.PrintStream;

import javax.swing.JComponent;
import javax.swing.text.BadLocationException;

import org.gvsig.andami.IconThemeHelper;
import org.hortonmachine.gears.libs.modules.HMConstants;
import org.hortonmachine.gears.libs.monitor.IHMProgressMonitor;
import org.hortonmachine.gui.console.CustomOutputStream;
import org.hortonmachine.gvsig.base.HMUtilities;
import org.joda.time.DateTime;

public class LogConsoleController extends LogConsoleView {

    private IHMProgressMonitor pm;
    private String processName;

    public LogConsoleController( final IHMProgressMonitor pm ) {
        this.pm = pm;
        init();
    }

    private void init() {
        clearButton.addActionListener(new ActionListener(){
            public void actionPerformed( ActionEvent e ) {
                // clears the text area
                try {
                    logArea.getDocument().remove(0, logArea.getDocument().getLength());
                } catch (BadLocationException ex) {
                    ex.printStackTrace();
                }
            }
        });
        clearButton.setIcon(IconThemeHelper.getImageIcon("trash"));
        
        copyButton.addActionListener(new ActionListener(){
            public void actionPerformed( ActionEvent e ) {
                HMUtilities.copyToClipboard(logArea.getText());
            }
        });
        copyButton.setIcon(IconThemeHelper.getImageIcon("copy_edit"));

        stopButton.addActionListener(new ActionListener(){
            public void actionPerformed( ActionEvent e ) {
                if (pm != null)
                    pm.setCanceled(true);
                stopButton.setEnabled(false);
            }
        });
        stopButton.setIcon(IconThemeHelper.getImageIcon("progress_stop"));

        PrintStream printStream = new PrintStream(new CustomOutputStream(logArea));

        // re-assigns standard output stream and error output stream
        System.setOut(printStream);
        System.setErr(printStream);

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

    public void beginProcess( String name ) {
        processName = name;
        System.out.println("Process " + name + " started at: "
                + new DateTime().toString(HMConstants.dateTimeFormatterYYYYMMDDHHMMSS) + "\n\n");
    }

    public void finishProcess() {
        System.out.println("\n\nProcess " + processName + " stopped at: "
                + new DateTime().toString(HMConstants.dateTimeFormatterYYYYMMDDHHMMSS));
        stopButton.setEnabled(false);
    }

    public void stopLogging() {
        System.setOut(System.out);
        System.setErr(System.err);
    }

    public JComponent asJComponent() {
        return this;
    }

    public IHMProgressMonitor getProgressMonitor() {
        return pm;
    }
}
