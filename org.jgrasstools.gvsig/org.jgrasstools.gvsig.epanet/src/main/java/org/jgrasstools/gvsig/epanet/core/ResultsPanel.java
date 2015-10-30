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
import java.io.File;
import java.sql.SQLException;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import org.gvsig.app.ApplicationLocator;
import org.gvsig.app.ApplicationManager;
import org.gvsig.app.PreferencesNode;
import org.gvsig.app.project.ProjectManager;
import org.gvsig.tools.ToolsLocator;
import org.gvsig.tools.i18n.I18nManager;
import org.gvsig.tools.swing.api.Component;
import org.gvsig.tools.swing.api.ToolsSwingLocator;
import org.gvsig.tools.swing.api.threadsafedialogs.ThreadSafeDialogsManager;
import org.jgrasstools.gears.libs.modules.JGTConstants;
import org.jgrasstools.gvsig.epanet.database.EpanetRun;
import org.jgrasstools.gvsig.epanet.database.JunctionsResultsTable;
import org.jgrasstools.gvsig.epanet.database.JunctionsTable;
import org.jgrasstools.gvsig.epanet.database.PipesTable;
import org.jgrasstools.gvsig.epanet.database.PumpsTable;
import org.jgrasstools.gvsig.epanet.database.ReservoirsTable;
import org.jgrasstools.gvsig.epanet.database.TanksTable;
import org.jgrasstools.gvsig.epanet.database.ValvesTable;
import org.jgrasstools.hortonmachine.modules.networktools.epanet.core.ResultsLinkParameters;
import org.jgrasstools.hortonmachine.modules.networktools.epanet.core.ResultsNodeParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private static final Logger logger = LoggerFactory.getLogger(ResultsPanel.class);

    private ApplicationManager applicationManager;
    private PreferencesNode preferences;
    private ThreadSafeDialogsManager dialogManager;
    private I18nManager i18nManager;
    private ProjectManager projectManager;
    private File resultsFile;

    private ConnectionSource connectionSource = null;
    private Dao<JunctionsResultsTable, Long> junctionsResultDao;
    private JComboBox<String> timeCombo;

    private JComboBox<String> nodesPlotCombo;

    private JComboBox<String> linksPlotCombo;

    private EpanetRun currentSelectedRun;
    private ResultsLinkParameters currentSelectedLinkVar;

    private ResultsNodeParameters currentSelectedNodeVar;

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
        setPreferredSize(new Dimension(800, 300));

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
        Insets insets = new Insets(5, 5, 5, 5);

        GridBagConstraints c = new GridBagConstraints();
        c.weightx = 0.0;
        c.weighty = 0.5;
        c.anchor = GridBagConstraints.PAGE_START;
        c.fill = GridBagConstraints.BOTH;
        c.insets = insets;
        c.ipadx = 0;
        c.ipady = 0;

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;
        c.gridheight = 3;
        JPanel runPanel = new JPanel(new GridBagLayout());
        runPanel.setBorder(BorderFactory.createTitledBorder("Epanet Runs"));
        add(runPanel, c);

        c.gridx = 1;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;
        c.gridheight = 1;
        JPanel variablesPanel = new JPanel(new GridBagLayout());
        variablesPanel.setBorder(BorderFactory.createTitledBorder("Variables"));
        add(variablesPanel, c);

        c.gridx = 1;
        c.gridy = 1;
        c.weightx = 1;
        c.weighty = 1;
        JPanel timeLinePanel = new JPanel(new GridBagLayout());
        timeLinePanel.setBorder(BorderFactory.createTitledBorder("Timeline"));
        add(timeLinePanel, c);

        c.gridx = 1;
        c.gridy = 2;
        c.weightx = 1;
        c.weighty = 1;
        JPanel plotPanel = new JPanel(new GridBagLayout());
        plotPanel.setBorder(BorderFactory.createTitledBorder("Plots"));
        add(plotPanel, c);

        try {
            String databaseUrl = "jdbc:sqlite:" + resultsFile.getAbsolutePath();
            connectionSource = new JdbcConnectionSource(databaseUrl);

            Dao<EpanetRun, Long> epanetRunDao = DaoManager.createDao(connectionSource, EpanetRun.class);
            junctionsResultDao = DaoManager.createDao(connectionSource, JunctionsResultsTable.class);

            final Dao<JunctionsTable, Long> junctionsDao = DaoManager.createDao(connectionSource, JunctionsTable.class);
            final Dao<PipesTable, Long> pipesDao = DaoManager.createDao(connectionSource, PipesTable.class);
            final Dao<PumpsTable, Long> pumpsDao = DaoManager.createDao(connectionSource, PumpsTable.class);
            final Dao<ValvesTable, Long> valvesDao = DaoManager.createDao(connectionSource, ValvesTable.class);
            final Dao<TanksTable, Long> tanksDao = DaoManager.createDao(connectionSource, TanksTable.class);
            final Dao<ReservoirsTable, Long> reservoirsDao = DaoManager.createDao(connectionSource, ReservoirsTable.class);

            /*
             * RUNPANEL
             */

            List<EpanetRun> allRuns = epanetRunDao.queryForAll();

            EpanetRun[] runs = new EpanetRun[allRuns.size() + 1];
            runs[0] = null;
            int i = 1;
            for( EpanetRun epanetRun : allRuns ) {
                runs[i++] = epanetRun;
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
                    currentSelectedRun = (EpanetRun) runsCombo.getSelectedItem();
                    StringBuilder sb = new StringBuilder();
                    if (currentSelectedRun != null) {
                        sb.append("Title: ").append(currentSelectedRun.getTitle()).append("\n");
                        sb.append("Description: ").append(currentSelectedRun.getDescription()).append("\n");
                        sb.append("User: ").append(currentSelectedRun.getUser()).append("\n");
                        sb.append("Timestamp: ")
                                .append(currentSelectedRun.getUtcTime().toString(JGTConstants.dateTimeFormatterYYYYMMDDHHMMSS))
                                .append("\n");
                    }
                    descriptionArea.setText(sb.toString());

                    try {
                        String[] timesList = EpanetUtilities.getTimesList(junctionsResultDao, currentSelectedRun);
                        timeCombo.setModel(new DefaultComboBoxModel<String>(timesList));
                        if (timesList.length == 0) {
                            // run has no data
                            dialogManager.messageDialog(
                                    "The run has no data stored. The simulation probably stopped before being able to produce any data.",
                                    "WARNING", JOptionPane.WARNING_MESSAGE);
                        }

                        String[] nodes = EpanetUtilities.getNodes(junctionsDao, tanksDao, reservoirsDao, currentSelectedRun);
                        nodesPlotCombo.setModel(new DefaultComboBoxModel<String>(nodes));
                        String[] links = EpanetUtilities.getLinks(pipesDao, pumpsDao, valvesDao, currentSelectedRun);
                        linksPlotCombo.setModel(new DefaultComboBoxModel<String>(links));

                    } catch (SQLException e1) {
                        e1.printStackTrace();
                    }
                }
            });

            /*
             * VARIABLES PANEL
             */
            JLabel nodesLabel = new JLabel("Node variable");
            c.gridx = 0;
            c.gridy = 0;
            c.weightx = 1;
            c.weighty = 0;
            variablesPanel.add(nodesLabel, c);

            ResultsNodeParameters[] nodeVariables = ResultsNodeParameters.values();
            String[] nodeVariablesStrings = new String[nodeVariables.length];
            for( int j = 0; j < nodeVariablesStrings.length; j++ ) {
                nodeVariablesStrings[j] = nodeVariables[j].getKey();
            }
            currentSelectedNodeVar = nodeVariables[0];

            final JComboBox<String> nodesCombo = new JComboBox<String>(nodeVariablesStrings);
            c.gridx = 1;
            c.gridy = 0;
            c.weightx = 1;
            c.weighty = 0;
            variablesPanel.add(nodesCombo, c);
            nodesCombo.addActionListener(new ActionListener(){
                public void actionPerformed( ActionEvent e ) {
                    Object selectedItem = nodesCombo.getSelectedItem();
                    if (selectedItem instanceof String) {
                        String selectedNodeId = (String) selectedItem;
                        currentSelectedNodeVar = ResultsNodeParameters.forCode(selectedNodeId);
                    }
                }
            });

            JLabel linksLabel = new JLabel("Link variable");
            c.gridx = 0;
            c.gridy = 1;
            c.weightx = 1;
            c.weighty = 0;
            variablesPanel.add(linksLabel, c);

            ResultsLinkParameters[] linkVariables = ResultsLinkParameters.values();
            String[] linkVariablesStrings = new String[linkVariables.length];
            for( int j = 0; j < linkVariablesStrings.length; j++ ) {
                linkVariablesStrings[j] = linkVariables[j].getKey();
            }
            currentSelectedLinkVar = linkVariables[0];

            final JComboBox<String> linksCombo = new JComboBox<String>(linkVariablesStrings);
            c.gridx = 1;
            c.gridy = 1;
            c.weightx = 1;
            c.weighty = 0;
            variablesPanel.add(linksCombo, c);
            linksCombo.addActionListener(new ActionListener(){
                public void actionPerformed( ActionEvent e ) {
                    Object selectedItem = linksCombo.getSelectedItem();
                    if (selectedItem instanceof String) {
                        String selectedLinkId = (String) selectedItem;
                        currentSelectedLinkVar = ResultsLinkParameters.forCode(selectedLinkId);
                    }
                }
            });

            /*
             * TIMELINE PANEL
             */
            JLabel timeLabel = new JLabel("Select time");
            c.gridx = 0;
            c.gridy = 0;
            c.weightx = 1;
            c.weighty = 0;
            timeLinePanel.add(timeLabel, c);

            timeCombo = new JComboBox<String>(new String[]{null});
            c.gridx = 1;
            c.gridy = 0;
            c.weightx = 1;
            c.weighty = 0;
            timeLinePanel.add(timeCombo, c);
            timeCombo.addActionListener(new ActionListener(){
                public void actionPerformed( ActionEvent e ) {
                    String selectedTime = (String) timeCombo.getSelectedItem();
                    logger.info("Selected time: " + selectedTime);

                    // DateTime currentSelectedTime =
                    // OmsEpanet.formatter.parseDateTime(selectedTime);

                    // TODO color the layers according to results
                }
            });

            /*
             * PLOT PANEL
             */
            JLabel nodesPlotLabel = new JLabel("Plot Node");
            c.gridx = 0;
            c.gridy = 0;
            c.weightx = 1;
            c.weighty = 0;
            plotPanel.add(nodesPlotLabel, c);

            nodesPlotCombo = new JComboBox<String>(new String[]{null});
            c.gridx = 1;
            c.gridy = 0;
            c.weightx = 1;
            c.weighty = 0;
            plotPanel.add(nodesPlotCombo, c);
            nodesPlotCombo.addActionListener(new ActionListener(){
                public void actionPerformed( ActionEvent e ) {
                    String selectedNode = (String) nodesPlotCombo.getSelectedItem();
                    logger.info("Selected Node: " + selectedNode);
                    if (currentSelectedRun != null)
                        try {
                            EpanetUtilities.chartNode(connectionSource, currentSelectedRun, selectedNode, currentSelectedNodeVar);
                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }
                }
            });

            JLabel linksPlotLabel = new JLabel("Plot Link");
            c.gridx = 0;
            c.gridy = 1;
            c.weightx = 1;
            c.weighty = 0;
            plotPanel.add(linksPlotLabel, c);

            linksPlotCombo = new JComboBox<String>(new String[]{null});
            c.gridx = 1;
            c.gridy = 1;
            c.weightx = 1;
            c.weighty = 0;
            plotPanel.add(linksPlotCombo, c);
            linksPlotCombo.addActionListener(new ActionListener(){
                public void actionPerformed( ActionEvent e ) {
                    String selectedLink = (String) linksPlotCombo.getSelectedItem();
                    logger.info("Selected Link: " + selectedLink);
                    if (currentSelectedRun != null)
                        try {
                            EpanetUtilities.chartLink(connectionSource, currentSelectedRun, selectedLink, currentSelectedLinkVar);
                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }
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
