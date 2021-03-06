package robert.gui;

import robert.model.AbstractStructure;
import robert.model.StructureType;

import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MainFrame extends JFrame {

    private static final String[] BCS = new String[]{"Periodic", "Non-periodic"};
    private static boolean BC = true; // periodic
    private static int selectedStructureIndex = 0;

    private DrawingPanel drawingPanel;
    private JTextField textFieldX, textFieldY;
    private static JLabel infoLabel;
    private Thread lifeThread = null;
    private boolean isThreadRunning = false;
    private JButton startButton;
    private JProgressBar progressBar;
    private JComboBox bcBox, structureBox;
    private JTextArea textArea;

    public MainFrame() {
        super("Game of Life");
        this.setLayout(new BorderLayout());
        drawingPanel = new DrawingPanel();
        this.add(drawingPanel, BorderLayout.CENTER);

        JPanel southPanel = new JPanel(new FlowLayout());
        southPanel.setBackground(Color.GRAY);
        startButton = new JButton("Start");
        startButton.addActionListener(new StartButtonListener());
        JButton addButton = new JButton("Add new life");
        addButton.addActionListener(new AddButtonListener());
        JButton killButton = new JButton("Kill cell");
        killButton.addActionListener(new KillCellAction());

        JButton clearButton = new JButton("Clear");
        clearButton.addActionListener(e -> {
            drawingPanel.clearCells();
        });

        progressBar = new JProgressBar(SwingConstants.VERTICAL, 0, (drawingPanel.SIZE * drawingPanel.SIZE));
        progressBar.setValue(0);
        progressBar.setStringPainted(true);
        progressBar.setForeground(Color.GREEN.darker());
        progressBar.setBackground(Color.MAGENTA.darker().darker());

        JPanel eastPanel = new JPanel(new GridLayout(1, 1));
        eastPanel.add(progressBar);
        //eastPanel.add(new JLabel("Progress"));


        southPanel.add(startButton);
        southPanel.add(clearButton);
        southPanel.add(killButton);
        southPanel.add(addButton);
        textFieldX = new JTextField(2);
        southPanel.add(new JLabel("x: "));
        southPanel.add(textFieldX);
        textFieldY = new JTextField(2);
        southPanel.add(new JLabel("y: "));
        southPanel.add(textFieldY);


        JPanel westPanel = new JPanel(new BorderLayout());
        JPanel innerNorthPanel = new JPanel(new FlowLayout());
        westPanel.add(innerNorthPanel, BorderLayout.NORTH);


        bcBox = new JComboBox(BCS);
        bcBox.addActionListener(e -> {
            if (bcBox.getSelectedIndex() == 0) BC = true;
            else BC = false;
            System.out.println("BC: " + BC);
        });
        structureBox = new JComboBox(StructureType.values());
        structureBox.addActionListener(e -> {
            this.selectedStructureIndex = structureBox.getSelectedIndex();
        });
        innerNorthPanel.add(new JLabel("Structures: "));
        innerNorthPanel.add(structureBox);

        this.add(southPanel, BorderLayout.SOUTH);
        infoLabel = new JLabel("Nothing happend yet.");
        JPanel northPanel = new JPanel(new FlowLayout());
        northPanel.setBackground(Color.GRAY);
        northPanel.add(new JLabel("BCs: "));
        northPanel.add(bcBox);
        northPanel.add(new JLabel(" Last action: "));
        northPanel.add(infoLabel);

        textArea = new JTextArea("Event log\n", DrawingPanel.SIZE, 5);
        textArea.setEditable(false);
        DefaultCaret caret = (DefaultCaret) textArea.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

        westPanel.add(new JScrollPane(textArea), BorderLayout.CENTER);
        this.add(northPanel, BorderLayout.NORTH);
        this.add(eastPanel, BorderLayout.EAST);
        this.add(westPanel, BorderLayout.WEST);
    }

    private class AddButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (textFieldY.getText().equals("") || textFieldX.getText().equals("")) return;
            int x = Integer.parseInt(textFieldX.getText());
            int y = Integer.parseInt(textFieldY.getText());
            if (drawingPanel.addNewLife(x, y)) {
                drawingPanel.getFieldsAt(x, y).setBackground(Color.GREEN);
            } else {
                infoLabel.setText("Given coordinates are not correct!");
            }
        }
    }

    private class KillCellAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (textFieldY.getText().equals("") || textFieldX.getText().equals("")) return;
            int x = Integer.parseInt(textFieldX.getText());
            int y = Integer.parseInt(textFieldY.getText());
            drawingPanel.killLife(x, y);
        }
    }

    private class StartButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (lifeThread == null || !lifeThread.isAlive()) {
                lifeThread = new Thread(new LifeRunnable());
                lifeThread.start();
                startButton.setText("Stop");
                System.out.println("Thread started.");
            } else {
                isThreadRunning = false;
                startButton.setEnabled(false);
                startButton.setText("Wait...");
            }
            //startButton.repaint();
        }
    }

    public static void updateInfo(String text) {
        infoLabel.setText(text);
    }

    public static int getSelectedStructureIndex() {
        return selectedStructureIndex;
    }

    private class LifeRunnable implements Runnable {
        public LifeRunnable() {
            isThreadRunning = true;
        }

        long counter = 0;
        @Override
        public void run() {
            textArea.setText("");
            while (isThreadRunning) {
                for (AbstractStructure s : drawingPanel.getStructures()) {
                    s.move();
                }
                progressBar.setValue(0);
                CellPane cellPane;
                CellPane[][] cellPanes = drawingPanel.getCells();
                for (int j, i = 0; i < DrawingPanel.SIZE; i++) {
                    for (j = 0; j < DrawingPanel.SIZE; j++) {
                        cellPane = cellPanes[i][j];

                        if (BC) cellPane.checkNeighbourhood();
                        else cellPane.checkNonPeriodic();

                        progressBar.setValue(progressBar.getValue() + 1);
                        try {
                            Thread.sleep(1); // sleep for slower screen update
                        } catch (InterruptedException e) {
                            System.out.println("Error - thread sleep try.");
                        }
                    }
                    //System.out.println("\tAlive myCells:");
                    //System.out.println(getAliveCellsCords());
                }


                this.updateCells(++counter);

                try {
                    Thread.sleep(1_000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                //isThreadRunning = false;
            }

            System.out.println("Thread stopped.");
            System.out.println("\n*******************************************");
            System.out.println("\n\tAlive myCells:");
            System.out.println(getAliveCellsCords());
            startButton.setText("Start");
            infoLabel.setText("End of the game.");
            startButton.setEnabled(true);
        }

        private void updateCells(long counter) {

            for (CellPane cell : CellPane.getToUpdateList()) {
                cell.setToUpdate(false);
                if (cell.isAlive()) {
                    drawingPanel.killLife(cell.getCordX(), cell.getCordY());
                } else {
                    drawingPanel.addNewLife(cell.getCordX(), cell.getCordY());
                }
            }
            CellPane.getToUpdateList().clear();
            infoLabel.setText("Board updated.");
            textArea.append("* Cycle #" + counter + "\n");
        }
    }

    private String getAliveCellsCords() {
        StringBuilder sb = new StringBuilder();
        int total = 0;
        CellPane[][] cells = DrawingPanel.getPanel().getCells();
        for (int j, i = 0; i < DrawingPanel.SIZE; i++)
            for (j = 0; j < DrawingPanel.SIZE; j++) {
                if (cells[i][j].isAlive()) {
                    ++total;
                    sb.append(cells[i][j].getCords());
                    sb.append("\n");
                }
            }
        sb.append("\tTotal: " + total + " of " + (DrawingPanel.SIZE * DrawingPanel.SIZE));
        if (total == 0) {
            isThreadRunning = false;
            //textArea.append("All myCells are dead. Game stopped.");
        }
        return sb.toString();
    }

    public static boolean isBC() {
        return BC;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                JFrame mainFrame = new MainFrame();
                mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                //mainFrame.setLocationRelativeTo(null);
                mainFrame.setLocationByPlatform(true);
                mainFrame.setResizable(false);
                mainFrame.pack();
                mainFrame.setVisible(true);
            }
        });

    }


}
