package org.jgrasstools.gvsig.base.utils.console;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.io.PrintStream;

import javax.swing.JComponent;
import javax.swing.text.BadLocationException;

import org.gvsig.andami.IconThemeHelper;
import org.jgrasstools.gears.libs.modules.JGTConstants;
import org.jgrasstools.gears.libs.monitor.IJGTProgressMonitor;
import org.jgrasstools.gvsig.base.JGTUtilities;
import org.joda.time.DateTime;

public class LogConsoleController extends LogConsoleView {

    private IJGTProgressMonitor pm;
    private String processName;

    public LogConsoleController( final IJGTProgressMonitor pm ) {
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
                JGTUtilities.copyToClipboard(logArea.getText());
            }
        });
        copyButton.setIcon(IconThemeHelper.getImageIcon("copy_edit"));

        stopButton.addActionListener(new ActionListener(){
            public void actionPerformed( ActionEvent e ) {
                if (pm != null)
                    pm.setCanceled(true);
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
                + new DateTime().toString(JGTConstants.dateTimeFormatterYYYYMMDDHHMMSS) + "\n\n");
    }

    public void finishProcess() {
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
