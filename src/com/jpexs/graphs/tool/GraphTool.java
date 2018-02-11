/*
 * Copyright (C) 2018 JPEXS, All rights reserved.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.
 */
package com.jpexs.graphs.tool;

import com.jpexs.graphs.graphviz.dot.parser.DotParseException;
import com.jpexs.graphs.graphviz.dot.parser.DotParser;
import com.jpexs.graphs.graphviz.graph.Graph;
import com.jpexs.graphs.graphviz.graph.operations.StepHandler;
import com.jpexs.graphs.graphviz.graph.operations.StringOperation;
import com.jpexs.graphs.graphviz.graph.operations.TestOperation;
import com.jpexs.graphs.graphviz.graph.operations.codestructure.BasicDecomposedGraphOperation;
import com.jpexs.graphs.graphviz.graph.operations.codestructure.CodeStructureModifyOperation;
import com.jpexs.graphs.graphviz.graph.operations.codestructure.StructuredGraphFacade;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;

/**
 *
 * @author JPEXS
 */
public class GraphTool {

    static JPanel imagePanel;
    static JScrollPane imageScrollPane;
    static BufferedImage img;
    static JFrame frame;
    static JSplitPane splitPane;

    static StringOperation op = null;
    private static String currentScriptName = "in";
    private static final String EXTENSION = ".gv";
    private static final String FILES_PATH = "graphs";
    private static final String DOT_PATH = "c:\\Program Files (x86)\\Graphviz2.38\\bin\\dot.exe";

    private static SwingWorker worker;
    private static JComboBox<String> scriptCombo;

    private static JEditorPane textArea;
    private static final String NOVY = "<new>";

    private static final String NOVY_TEXT = "digraph {\r\nstart;\r\nstart->end;\r\nend;\r\n}";

    private static final BufferedImage EMPTY_IMAGE = new BufferedImage(1, 1, BufferedImage.TYPE_4BYTE_ABGR);

    private static String makeFileName(String name) {
        return FILES_PATH + "/" + name + EXTENSION;
    }

    private static String SETTINGS_PROP_FILE = "settings.properties";

    private synchronized static void setGraphImage(BufferedImage newImage) {
        img = newImage;
        imagePanel.repaint();
        imagePanel.updateUI();
    }

    private static void sortScriptCombo() {
        String selectedItem = (String) scriptCombo.getSelectedItem();
        scriptCombo.removeItem(NOVY);
        List<String> items = new ArrayList<>();
        for (int i = 0; i < scriptCombo.getItemCount(); i++) {
            items.add(scriptCombo.getItemAt(i));
        }
        Collections.sort(items);
        items.add(NOVY);
        scriptCombo.setModel(new DefaultComboBoxModel<>(items.toArray(new String[items.size()])));
        scriptCombo.setSelectedItem(selectedItem);

    }

    static StepHandler handlerDoStep = new StepHandler() {
        @Override
        public void step(String currentGraph) {
            try {
                setGraphImage(textToImage(currentGraph));
            } catch (IOException ex) {
                setGraphImage(EMPTY_IMAGE);
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException ex) {
                Logger.getLogger(GraphTool.class.getName()).log(Level.SEVERE, null, ex);
                //ignore
            }
        }
    };

    private static String regenerateText(String text) {
        StructuredGraphFacade f = new StructuredGraphFacade();
        return f.recompose(text);
    }

    private static BufferedImage textToImage(String text) throws IOException {
        File outGv = new File("out.gv");
        PrintWriter pw = new PrintWriter(outGv);
        pw.println(text);
        pw.close();

        runCommand("\"" + DOT_PATH + "\" -Tpng -Nfontname=times-bold -Nfontsize=12 -o out.png out.gv");
        outGv.delete();

        BufferedImage br = ImageIO.read(new File("out.png"));
        return br;
    }

    private static void setOperation(BasicDecomposedGraphOperation op) {
        GraphTool.op = op;
    }

    private static void saveSettings() {
        Properties propOut = new Properties();
        propOut.setProperty("currentScriptName", currentScriptName);
        propOut.setProperty("window.width", "" + frame.getWidth());
        propOut.setProperty("window.height", "" + frame.getHeight());
        propOut.setProperty("window.location.x", "" + frame.getLocation().x);
        propOut.setProperty("window.location.y", "" + frame.getLocation().y);
        propOut.setProperty("window.splitter.location", "" + splitPane.getDividerLocation());
        try (OutputStream output = new FileOutputStream(SETTINGS_PROP_FILE)) {
            propOut.store(output, null);
        } catch (IOException ex) {
            System.err.println("Error saving current config");
        }
    }

    private static void loadSettings() {
        Properties propIn = new Properties();
        if (new File(SETTINGS_PROP_FILE).exists()) {
            try (InputStream input = new FileInputStream(SETTINGS_PROP_FILE)) {
                propIn.load(input);
            } catch (IOException ex) {
                Logger.getLogger(GraphTool.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        int windowWidth = Integer.parseInt(propIn.getProperty("window.width", "" + frame.getWidth()));
        int windowHeight = Integer.parseInt(propIn.getProperty("window.height", "" + frame.getHeight()));
        frame.setSize(windowWidth, windowHeight);

        int windowLocationX = Integer.parseInt(propIn.getProperty("window.location.x", "" + frame.getLocation().x));
        int windowLocationY = Integer.parseInt(propIn.getProperty("window.location.y", "" + frame.getLocation().y));
        frame.setLocation(windowLocationX, windowLocationY);

        int windowSplitterLocation = Integer.parseInt(propIn.getProperty("window.splitter.location", "" + splitPane.getDividerLocation()));
        splitPane.setDividerLocation(windowSplitterLocation);

        currentScriptName = propIn.getProperty("currentScriptName", "");
    }

    private static void saveCurrent() {
        if (!currentScriptName.isEmpty()) {
            try {
                PrintWriter pw = new PrintWriter(new File(makeFileName(currentScriptName)));
                pw.print(textArea.getText());
                pw.close();
            } catch (Exception ex) {
                System.err.println("Error saving script");
            }
        }
        saveSettings();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {

        try {
            // Set System L&F
            UIManager.setLookAndFeel(
                    UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // handle exception
        }
        frame = new JFrame("Graph structure detection");
        imagePanel = new JPanel() {
            @Override
            public void paint(Graphics g) {
                if (img == null) {
                    return;
                }
                g.drawImage(img, 0, 0, null);
                Dimension dim = new Dimension(img.getWidth(), img.getHeight());
                imagePanel.setMinimumSize(dim);
                imagePanel.setPreferredSize(dim);
                imagePanel.setSize(dim);
                //f.pack();
            }
        };
        initGui();
        loadSettings();

        setGraphImage(EMPTY_IMAGE);
        String fileName = makeFileName(currentScriptName);
        String text = (new File(fileName)).exists() ? new String(Files.readAllBytes(Paths.get(fileName)), StandardCharsets.UTF_8) : NOVY_TEXT;
        textArea.setText(text);
        setGraphImage(textToImage(text));
        scriptCombo.setSelectedItem(currentScriptName);
    }

    private static void initGui() {
        int WIN_HEIGHT = 800;

        frame.getContentPane().setLayout(new BorderLayout());

        MouseAdapter ma = new MouseAdapter() {

            private Point origin;

            @Override
            public void mousePressed(MouseEvent e) {
                origin = new Point(e.getPoint());
            }

            @Override
            public void mouseReleased(MouseEvent e) {
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (origin != null) {
                    JViewport viewPort = (JViewport) SwingUtilities.getAncestorOfClass(JViewport.class, imagePanel);
                    if (viewPort != null) {
                        int deltaX = origin.x - e.getX();
                        int deltaY = origin.y - e.getY();

                        Rectangle view = viewPort.getViewRect();
                        view.x += deltaX;
                        view.y += deltaY;

                        imagePanel.scrollRectToVisible(view);
                    }
                }
            }

        };

        imagePanel.addMouseListener(ma);
        imagePanel.addMouseMotionListener(ma);

        imageScrollPane = new JScrollPane(imagePanel);
        imageScrollPane.setPreferredSize(new Dimension(500, WIN_HEIGHT));
        textArea = new JEditorPane("text/plain", "");
        textArea.setContentType("text/plain");
        textArea.setFont(new Font("Courier New", Font.PLAIN, 12));
        Dimension textAreaSize = new Dimension(600, WIN_HEIGHT);
        textArea.setMinimumSize(textAreaSize);
        textArea.setPreferredSize(textAreaSize);

        JButton runInteractiveButton = new JButton("Run interactive");
        runInteractiveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                final String runText = textArea.getText();
                if (worker != null) {
                    worker.cancel(true);
                }
                worker = new SwingWorker<Void, Void>() {
                    @Override
                    protected Void doInBackground() throws Exception {
                        try {
                            setOperation(new CodeStructureModifyOperation());
                            String newText = op.execute(runText, handlerDoStep);
                            try {
                                setGraphImage(textToImage(newText));
                            } catch (IOException ex) {
                                setGraphImage(EMPTY_IMAGE);
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                        return null;
                    }
                };
                worker.execute();

            }
        });

        JButton runDetectionButton = new JButton("Run detection");
        runDetectionButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                final String runText = textArea.getText();
                if (worker != null) {
                    worker.cancel(true);
                }
                worker = new SwingWorker<Void, Void>() {
                    @Override
                    protected Void doInBackground() throws Exception {
                        try {
                            setOperation(new CodeStructureModifyOperation());
                            String newText = op.execute(runText, null);
                            try {
                                setGraphImage(textToImage(newText));
                            } catch (IOException ex) {
                                setGraphImage(EMPTY_IMAGE);
                            }
                            imagePanel.repaint();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                        return null;
                    }
                };
                worker.execute();

            }
        });

        JButton showButton = new JButton("SHOW");
        showButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    setGraphImage(textToImage(textArea.getText()));
                } catch (IOException ex) {
                    setGraphImage(EMPTY_IMAGE);
                }
                try {
                    DotParser dotParser = new DotParser();
                    Graph gr = dotParser.parse(new StringReader(textArea.getText()));
                } catch (DotParseException ex) {
                    Logger.getLogger(GraphTool.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(GraphTool.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });

        JButton testButton = new JButton("TEST");
        testButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                final String runText = textArea.getText();
                if (worker != null) {
                    worker.cancel(true);
                }
                worker = new SwingWorker<Void, Void>() {
                    @Override
                    protected Void doInBackground() throws Exception {
                        try {
                            setOperation(new TestOperation());
                            String newText = op.execute(runText, handlerDoStep);
                            try {
                                setGraphImage(textToImage(newText));
                            } catch (IOException ex) {
                                setGraphImage(EMPTY_IMAGE);
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                        return null;
                    }
                };
                worker.execute();
            }
        });

        imagePanel.setAlignmentX(0.5f);

        JPanel imagePanelBkg = new JPanel(new GridBagLayout());
        imagePanelBkg.setBackground(Color.white);
        imagePanelBkg.add(imagePanel, new GridBagConstraints());

        JPanel codePanel = new JPanel(new BorderLayout());
        codePanel.add(new JScrollPane(textArea), BorderLayout.CENTER);

        JPanel buttonsPanel = new JPanel(new FlowLayout());
        buttonsPanel.add(showButton);
        buttonsPanel.add(runDetectionButton);
        buttonsPanel.add(runInteractiveButton);
        buttonsPanel.add(testButton);
        codePanel.add(buttonsPanel, BorderLayout.SOUTH);

        JPanel selectScriptPanel = new JPanel(new FlowLayout());

        String files[] = new File(FILES_PATH).list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(EXTENSION);
            }
        });
        String files2[] = new String[files.length + 1];
        for (int i = 0; i < files.length; i++) {
            files2[i] = files[i].substring(0, files[i].length() - EXTENSION.length());
        }
        files2[files2.length - 1] = NOVY;
        files = files2;
        scriptCombo = new JComboBox<>(files);
        selectScriptPanel.add(scriptCombo);
        JButton renameButton = new JButton("rename");
        JButton deleteButton = new JButton("delete");

        selectScriptPanel.add(renameButton);
        selectScriptPanel.add(deleteButton);
        scriptCombo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveCurrent();

                String newName = (String) scriptCombo.getSelectedItem();
                if (newName.equals(NOVY)) {
                    newName = JOptionPane.showInputDialog("Enter new name: ");
                    if (newName == null) {
                        return;
                    }
                    scriptCombo.addItem(newName);
                    scriptCombo.setSelectedItem(newName);
                    currentScriptName = newName;
                    textArea.setText(NOVY_TEXT);
                    sortScriptCombo();
                    try {
                        setGraphImage(textToImage(textArea.getText()));
                    } catch (IOException ex) {
                        setGraphImage(EMPTY_IMAGE);
                    }
                } else {
                    if (newName.equals(currentScriptName)) {
                        return;
                    }
                    currentScriptName = newName;

                    String fileName = makeFileName(newName);
                    String text = NOVY_TEXT;
                    if (new File(fileName).exists()) {
                        try {
                            text = new String(Files.readAllBytes(Paths.get(fileName)), StandardCharsets.UTF_8);
                        } catch (IOException ex) {
                            Logger.getLogger(GraphTool.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    textArea.setText(text);
                    try {
                        setGraphImage(textToImage(text));
                    } catch (IOException ex) {
                        setGraphImage(EMPTY_IMAGE);
                    }
                    currentScriptName = newName;
                }
            }
        });

        renameButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String newName = JOptionPane.showInputDialog("Enter new name: ", currentScriptName);
                if (newName == null || newName.isEmpty()) {
                    return;
                }
                if (new File(makeFileName(newName)).exists()) {
                    JOptionPane.showMessageDialog(frame, "File " + newName + " already exists", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                String oldFileName = currentScriptName;

                PrintWriter pw;
                try {
                    pw = new PrintWriter(new File(makeFileName(newName)));
                    pw.print(textArea.getText());
                    pw.close();
                } catch (IOException ex) {
                    Logger.getLogger(GraphTool.class.getName()).log(Level.SEVERE, null, ex);
                }

                scriptCombo.addItem(newName);
                scriptCombo.setSelectedItem(newName);
                currentScriptName = newName;
                sortScriptCombo();
                scriptCombo.removeItem(oldFileName);
                new File(makeFileName(oldFileName)).delete();
            }
        });

        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (JOptionPane.showConfirmDialog(frame, "Really delete " + currentScriptName + "?", "Delete", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                    String oldFileName = currentScriptName;
                    scriptCombo.removeItem(oldFileName);
                    currentScriptName = (String) scriptCombo.getSelectedItem();
                    String fname = makeFileName(oldFileName);
                    if (!new File(fname).delete()) {
                        System.err.println("cannot delete " + fname);
                    }

                }
            }
        });

        codePanel.add(selectScriptPanel, BorderLayout.NORTH);

        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(imagePanelBkg), codePanel);
        frame.getContentPane().add(splitPane, BorderLayout.CENTER);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                saveCurrent();
                System.exit(0);
            }

        });
        frame.setSize(new Dimension(1024, 768));
        frame.setVisible(true);
        splitPane.setDividerLocation(0.3);
    }

    private static void runCommand(String command) {
        try {
            Process process = Runtime.getRuntime().exec(command);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String s;
            while ((s = reader.readLine()) != null) {

            }
        } catch (IOException e) {
            //ignore
        }
    }

    /*
    - Přidej startovní bod do TODO listu
- Vezmi [BOD] z TODO listu, odstraň ho z TODO listu
 If jsou všechny body odkazující do [BOD] zpracované
  {
     kontrola strukturovanosti()     
     přidej body následující [BOD] do TODO listu
     nastav hranám z [BOD] do následníků vypočtený decisionList
     označ [BOD] jako zpracovaný
  }

Kontrola strukturovanosti:
Je decisionList jen 1? ukonči kontrolu
Vyjmout decisionListy, které mám z minula zapamatované
  jako nestrukturované
Jsou nějaké  decisionListy shodné a
je jejich počet stejný, jako počet větví
posledního decision?  
=> odstraň je ze seznamu, přidej do seznamu 1 decisionList,
který je kratší o 1 koncové písmeno

Pro zbylé decisionListy:
 Pro decistionList [DL1], existuje stejný decisionList
 s přidaným 1 písmenem - [DL2]?
ANO => odstraň [DL1]
(opakuj pro zbylé decisionListy)

Poslední písmena zbylých decisionListů=> decision, kde se
odděluje nestrukturovaný kód.

Cestou zpátky odtud do posledního písmena lze identifikovat,
které z vycházejících bodů toho písmena to je.

Zapamatovat si zbylé decisionListy.
U zbylých decisionListů najít společný prefix, odebrat poslední
písmeno a to dát jako decisionList následníkům.

Společný prefix není => následníci mají prázdný decisionList
     */
}
