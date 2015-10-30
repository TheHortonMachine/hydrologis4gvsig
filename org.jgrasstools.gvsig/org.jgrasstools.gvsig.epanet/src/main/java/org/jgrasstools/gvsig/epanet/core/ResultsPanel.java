/*
 * This file is part of JGrasstools (http://www.jgrasstools.org)
 * (C) HydroloGIS - www.hydrologis.com 
 * 
 * JGrasstools is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.jgrasstools.gvsig.epanet.core;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.sql.SQLException;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import org.gvsig.app.ApplicationLocator;
import org.gvsig.app.ApplicationManager;
import org.gvsig.app.PreferencesNode;
import org.gvsig.app.project.ProjectManager;
import org.gvsig.tools.ToolsLocator;
import org.gvsig.tools.i18n.I18nManager;
import org.gvsig.tools.swing.api.Component;
import org.gvsig.tools.swing.api.ToolsSwingLocator;
import org.gvsig.tools.swing.api.threadsafedialogs.ThreadSafeDialogsManager;
import org.jgrasstools.gears.io.geopaparazzi.geopap4.TimeUtilities;
import org.jgrasstools.gears.libs.modules.JGTConstants;
import org.jgrasstools.gvsig.epanet.database.EpanetRun;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;

/**
 * Epanet results panel.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class ResultsPanel extends JPanel implements Component {
    private static final long serialVersionUID = 1L;
    private ApplicationManager applicationManager;
    private PreferencesNode preferences;
    private ThreadSafeDialogsManager dialogManager;
    private I18nManager i18nManager;
    private ProjectManager projectManager;
    private File resultsFile;

    private ConnectionSource connectionSource = null;

    public ResultsPanel( File resultsFile ) {
        this.resultsFile = resultsFile;
        applicationManager = ApplicationLocator.getManager();
        preferences = applicationManager.getPreferences();
        dialogManager = ToolsSwingLocator.getThreadSafeDialogsManager();
        i18nManager = ToolsLocator.getI18nManager();
        projectManager = applicationManager.getProjectManager();

        // setName("Epanet Results Browser");
        // Utilities.centerComponentOnScreen(this);

        init();
        setPreferredSize(new Dimension(800, 700));

    }

    public void freeResources() {
        if (connectionSource != null)
            try {
                connectionSource.close();
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
    }

    private void init() {
        setLayout(new GridBagLayout());
        int col = 0;
        int width = 1;
        int height = 1;
        Insets insets = new Insets(5, 5, 5, 5);

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = col;
        c.gridy = 0;
        c.gridwidth = width;
        c.gridheight = height;
        c.weightx = 0.0;
        c.weighty = 1.0;
        c.anchor = GridBagConstraints.PAGE_START;
        c.fill = GridBagConstraints.BOTH;
        c.insets = insets;
        c.ipadx = 0;
        c.ipady = 0;

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        JPanel runPanel = new JPanel(new GridBagLayout());
        runPanel.setBorder(BorderFactory.createTitledBorder("Epanet Runs"));
        add(runPanel, c);

        c.gridx = 1;
        c.gridy = 0;
        c.weightx = 2;
        JPanel variablesPanel = new JPanel(new GridBagLayout());
        variablesPanel.setBorder(BorderFactory.createTitledBorder("Variables"));
        add(variablesPanel, c);

        c.gridx = 0;
        c.gridy = 1;
        c.weightx = 1;
        JPanel timeLinePanel = new JPanel(new GridBagLayout());
        timeLinePanel.setBorder(BorderFactory.createTitledBorder("Timeline"));
        add(timeLinePanel, c);

        c.gridx = 1;
        c.gridy = 1;
        c.weightx = 1;
        JPanel plotPanel = new JPanel(new GridBagLayout());
        plotPanel.setBorder(BorderFactory.createTitledBorder("Plots"));
        add(plotPanel, c);

        try {
            String databaseUrl = "jdbc:sqlite:" + resultsFile.getAbsolutePath();
            connectionSource = new JdbcConnectionSource(databaseUrl);

            Dao<EpanetRun, Long> epanetRunDao = DaoManager.createDao(connectionSource, EpanetRun.class);

            // Dao<JunctionsTable, Long> junctionsDao = DaoManager.createDao(connectionSource,
            // JunctionsTable.class);
            // Dao<PipesTable, Long> pipesDao = DaoManager.createDao(connectionSource,
            // PipesTable.class);
            // Dao<PumpsTable, Long> pumpsDao = DaoManager.createDao(connectionSource,
            // PumpsTable.class);
            // Dao<ValvesTable, Long> valvesDao = DaoManager.createDao(connectionSource,
            // ValvesTable.class);
            // Dao<TanksTable, Long> tanksDao = DaoManager.createDao(connectionSource,
            // TanksTable.class);
            // Dao<ReservoirsTable, Long> reservoirsDao = DaoManager.createDao(connectionSource,
            // ReservoirsTable.class);

            List<EpanetRun> allRuns = epanetRunDao.queryForAll();
            
            EpanetRun[] runs = new EpanetRun[allRuns.size()+1];
            runs[0] = null;
            int i = 1;
            for( EpanetRun epanetRun : allRuns ) {
                runs[i] = epanetRun;
            }

            final JComboBox<EpanetRun> runsCombo = new JComboBox<EpanetRun>(runs);
            c.gridx = 0;
            c.gridy = 0;
            c.weightx = 1;
            c.weighty = 0;
            runPanel.add(runsCombo, c);

            final JTextArea descriptionArea = new JTextArea();
            c.gridx = 0;
            c.gridy = 1;
            c.weightx = 1;
            c.weighty = 1;
            runPanel.add(descriptionArea, c);

            runsCombo.addActionListener(new ActionListener(){
                public void actionPerformed( ActionEvent e ) {
                    EpanetRun selectedRun = (EpanetRun) runsCombo.getSelectedItem();

                    StringBuilder sb = new StringBuilder();
                    if (selectedRun != null) {
                        sb.append("Title: ").append(selectedRun.getTitle()).append("\n");
                        sb.append("Description: ").append(selectedRun.getDescription()).append("\n");
                        sb.append("User: ").append(selectedRun.getUser()).append("\n");
                        sb.append("Timestamp: ")
                                .append(selectedRun.getUtcTime().toString(JGTConstants.dateTimeFormatterYYYYMMDDHHMMSS))
                                .append("\n");
                    }
                    descriptionArea.setText(sb.toString());
                }
            });

            // dialogManager.messageDialog(warnings, "WARNING", JOptionPane.WARNING_MESSAGE);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public JComponent asJComponent() {
        return this;
    }

}
