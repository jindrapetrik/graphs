/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package graphs;

import com.sun.prism.paint.Gradient;
import guru.nidi.graphviz.model.Graph;
import guru.nidi.graphviz.model.Serializer;
import static guru.nidi.graphviz.attribute.Records.*;
import static guru.nidi.graphviz.model.Compass.*;
import guru.nidi.graphviz.model.Link;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.model.MutableNode;
import guru.nidi.graphviz.model.MutableNodePoint;
import guru.nidi.graphviz.parse.Parser;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.TextArea;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;
import javax.swing.UIManager;

/**
 *
 * @author Jindra
 */
public class Graphs {

    static JPanel imagePanel;
    static BufferedImage img;

    static Operation op = null;
    private static String currentFileName = "in";
    private static final String EXTENSION = ".gv";
    private static final String FILES_PATH = "graphs";

    private static SwingWorker worker;

    private static JEditorPane textArea;

    private static String makeFileName(String name) {
        return FILES_PATH + "/" + name + EXTENSION;
    }

    static StepHandler handlerDoStep = new StepHandler() {
        @Override
        public void step(String currentGraph) {
            try {
                img = textToImage(currentGraph);
            } catch (IOException ex) {
                //ignore
            }
            imagePanel.repaint();
            try {
                Thread.sleep(500);
            } catch (InterruptedException ex) {
                Logger.getLogger(Graphs.class.getName()).log(Level.SEVERE, null, ex);
                //ignore
            }
        }
    };

    private static String regenerateText(String text) {
        GraphVizFacade f = new GraphVizFacade();
        return f.regenerateGraphString(text);
    }

    private static BufferedImage textToImage(String text) throws IOException {
        File outGv = new File("out.gv");
        PrintWriter pw = new PrintWriter(outGv);
        pw.println(regenerateText(text));
        pw.close();

        runCommand("\"c:\\Program Files (x86)\\Graphviz2.38\\bin\\dot.exe\" -Tpng -Nfontname=times-bold -Nfontsize=12 -o out.png out.gv");
        outGv.delete();

        BufferedImage br = ImageIO.read(new File("out.png"));
        return br;
    }

    private static void setOperation(AbstractOperation op) {
        Graphs.op = op;
    }

    private static void loadCurrent() {
        File f = new File("current.txt");
        if (f.exists()) {
            BufferedReader br;
            try {
                br = new BufferedReader(new FileReader(f));
                currentFileName = br.readLine();
                br.close();
            } catch (IOException ex) {
                Logger.getLogger(Graphs.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private static void saveCurrent() {
        try {
            PrintWriter pw = new PrintWriter(new File(makeFileName(currentFileName)));
            pw.print(textArea.getText());
            pw.close();
        } catch (Exception ex) {

        }

        try {
            PrintWriter pw = new PrintWriter(new File("current.txt"));
            pw.print(currentFileName);
            pw.close();
        } catch (Exception ex) {

        }
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
        loadCurrent();
        JFrame frame = new JFrame("Graph");
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

        String text = new String(Files.readAllBytes(Paths.get(makeFileName(currentFileName))), StandardCharsets.UTF_8);
        img = textToImage(text);

        int WIN_HEIGHT = 800;

        frame.getContentPane().setLayout(new BorderLayout());
        JScrollPane imageScrollPane = new JScrollPane(imagePanel);
        imageScrollPane.setPreferredSize(new Dimension(500, WIN_HEIGHT));
        textArea = new JEditorPane("text/plain", text);
        textArea.setContentType("text/plain");
        textArea.setFont(new Font("Courier New", Font.PLAIN, 12));
        Dimension textAreaSize = new Dimension(600, WIN_HEIGHT);
        textArea.setMinimumSize(textAreaSize);
        textArea.setPreferredSize(textAreaSize);

        JButton runButton = new JButton("RUN");
        runButton.addActionListener(new ActionListener() {
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
                            setOperation(new DetectCodeStructureOperation(runText));
                            op.setStepHandler(handlerDoStep);
                            String newText = op.execute();
                            try {
                                img = textToImage(newText);
                            } catch (IOException ex) {
                                //ignore
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

        JButton quickButton = new JButton("Quick RUN");
        quickButton.addActionListener(new ActionListener() {
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
                            setOperation(new DetectCodeStructureOperation(runText));
                            String newText = op.execute();
                            try {
                                img = textToImage(newText);
                            } catch (IOException ex) {
                                //ignore
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
                    img = textToImage(textArea.getText());
                } catch (IOException ex) {
                    img = new BufferedImage(1, 1, BufferedImage.TYPE_4BYTE_ABGR);
                }
                imagePanel.repaint();
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
                            setOperation(new TestOperation(runText));
                            String newText = op.execute();
                            try {
                                img = textToImage(newText);
                            } catch (IOException ex) {
                                //ignore
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

        imagePanel.setAlignmentX(0.5f);

        JPanel imagePanelBkg = new JPanel(new GridBagLayout());
        imagePanelBkg.setBackground(Color.white);
        imagePanelBkg.add(imagePanel, new GridBagConstraints());

        JPanel codePanel = new JPanel(new BorderLayout());
        codePanel.add(new JScrollPane(textArea), BorderLayout.CENTER);

        JPanel buttonsPanel = new JPanel(new FlowLayout());
        buttonsPanel.add(showButton);
        buttonsPanel.add(runButton);
        buttonsPanel.add(quickButton);
        buttonsPanel.add(testButton);
        codePanel.add(buttonsPanel, BorderLayout.SOUTH);

        JPanel selectScriptPanel = new JPanel(new FlowLayout());
        final String NOVY = "<new>";
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
        JComboBox<String> scriptCombo = new JComboBox<>(files);
        selectScriptPanel.add(scriptCombo);
        JButton renameButton = new JButton("rename");

        selectScriptPanel.add(renameButton);
        scriptCombo.setSelectedItem(currentFileName);
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
                    scriptCombo.removeItem(NOVY);
                    scriptCombo.addItem(newName);
                    scriptCombo.addItem(NOVY);
                    scriptCombo.setSelectedItem(newName);
                    currentFileName = newName;
                    img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
                } else {
                    if (newName.equals(currentFileName)) {
                        return;
                    }
                    currentFileName = newName;

                    String text;
                    try {
                        text = new String(Files.readAllBytes(Paths.get(makeFileName(newName))), StandardCharsets.UTF_8);
                        textArea.setText(text);
                        img = textToImage(text);
                        currentFileName = newName;
                    } catch (IOException ex) {
                        Logger.getLogger(Graphs.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                imagePanel.repaint();
            }
        });

        renameButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String newName = JOptionPane.showInputDialog("Enter new name: ", currentFileName);
                if (newName == null || newName.isEmpty()) {
                    return;
                }
                new File(makeFileName(currentFileName)).renameTo(new File(makeFileName(newName)));
                scriptCombo.removeItem(NOVY);
                scriptCombo.removeItem(currentFileName);
                scriptCombo.addItem(newName);
                scriptCombo.setSelectedItem(newName);
                scriptCombo.addItem(NOVY);
                currentFileName = newName;
            }
        });
        codePanel.add(selectScriptPanel, BorderLayout.NORTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(imagePanelBkg), codePanel);
        frame.getContentPane().add(split, BorderLayout.CENTER);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                saveCurrent();
                System.exit(0);
            }

        });
        frame.setSize(new Dimension(1024, 768));
        frame.setVisible(true);
        split.setDividerLocation(0.3);

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
