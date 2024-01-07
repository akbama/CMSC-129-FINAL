/*

Programmers:
Alyanna Krista Bama       - 2020-09745
Julienne Elyze Marzo      - 2020-10718
Djewel Yparraguirre       - 2020-11513

PE 4 - Syntax Analysis

This program implements a simple syntax
analyzer with static semantic analysis
for a custom programming language.

Programming Language: JAVA
*/

//IMPORTS PACKAGES, CLASSES, INTERFACES
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.BadLocationException;
import javax.swing.text.Highlighter;
import javax.swing.text.JTextComponent;


// This class is responsible for the active line highlighter in the code editor
class LinePainter implements Highlighter.HighlightPainter, CaretListener, MouseListener, MouseMotionListener {
    private JTextComponent component;
    private Color color;
    private Rectangle lastView;
    
    public LinePainter(JTextComponent component) {
        this(component, new Color(0, 255, 255, 64));
    }

    public LinePainter(JTextComponent component, Color color) {
        this.component = component;
        setColor(color);

        component.addCaretListener(this);
        component.addMouseListener(this);
        component.addMouseMotionListener(this);

        try {
            component.getHighlighter().addHighlight(0, 0, this);
        } catch (BadLocationException ble) {
            System.out.println(ble);
        }
    }

    public void setColor(Color color) {
        this.color = color;
    }

    @Override
    public void paint(Graphics g, int p0, int p1, Shape bounds, JTextComponent c) {
        try {
            Rectangle r = c.modelToView(c.getCaretPosition());
            g.setColor(color);
            g.fillRect(0, r.y, c.getWidth(), r.height);

            if (lastView == null)
                lastView = r;
        } catch (BadLocationException ble) {
            System.out.println(ble);
        }
    }

    public void resetHighlight() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
                    int offset = component.getCaretPosition();
                    Rectangle currentView = component.modelToView(offset);

                    if (lastView != null && lastView.y != currentView.y) {
                        component.repaint(0, lastView.y, component.getWidth(), lastView.height);
                        lastView = currentView;
                    }
                } catch (BadLocationException ble) {
                }
            }
        });
    }

    @Override
    public void caretUpdate(CaretEvent e) {
        resetHighlight();
    }

    @Override
    public void mousePressed(MouseEvent e) {
        resetHighlight();
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        resetHighlight();
    }

    @Override
    public void mouseMoved(MouseEvent e) {
    }
}

// JFrame class for implementing UI
class Bama_Marzo_Yparraguirre_FinalProj extends JFrame {

    // Decalarations for UI components
    private JTextPane codeEditor;
    private JTable tableVar;
    private DefaultTableModel tableModel;
    private JTextArea console, lineNumbers, tokenizedCodeArea;
    private JLabel codeEditorLbl, consoleLbl, tableVarLbl;
    private JScrollPane codeEditorScrollPane, consoleScrollPane, tableVarScrollPane;
    private JPanel codeEditorPanel, consolePanel, tableVarPanel, contentPanel;
    private JSplitPane splitPanel;
    private LinePainter linePainter; // Add LinePainter instance

    // Menu and menu items components
    private JMenuBar mb;
    private JMenu file, compileCode, showTokenizedCode, executeCode;
    private JMenuItem save, saveAs, newFile, openFile;
    private Color lineNumbersColor;
    private File currentFile;

    public Bama_Marzo_Yparraguirre_FinalProj() {

        setTitle("Syntax Analyzer");
        setSize(800, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // Menu and Menu Items
        mb = new JMenuBar();
        file = new JMenu("File");
        compileCode = new JMenu("Compile Code (Ctrl + R)");
        showTokenizedCode = new JMenu("Show Tokenized Code (Ctrl + T)");
        executeCode = new JMenu("Execute Code (Ctrl + E)");
        save = new JMenuItem("Save");
        saveAs = new JMenuItem("Save As");
        newFile = new JMenuItem("New File");
        openFile = new JMenuItem("Open File");
        file.add(save);
        file.add(saveAs);
        file.addSeparator();
        file.add(newFile);
        file.add(openFile);
        mb.add(file);
        mb.add(compileCode);
        mb.add(showTokenizedCode);
        mb.add(executeCode); // this line of code doesn't have any used in the current version of the program
        executeCode.setEnabled(false);

        // Code Editor and Tokenized Code Area
        codeEditor = new JTextPane();
        tokenizedCodeArea = new JTextArea();
        codeEditorLbl = new JLabel("Code Editor");
        lineNumbers = new JTextArea(" 1 ");
        lineNumbers.setBorder(null);
        lineNumbers.setEditable(false);
        lineNumbersColor = Color.decode("#E0E0E0");
        lineNumbers.setBackground(lineNumbersColor);

        codeEditorPanel = new JPanel(new BorderLayout());
        codeEditorPanel.add(codeEditorLbl, BorderLayout.NORTH);
        codeEditorPanel.add(lineNumbers, BorderLayout.WEST);
        codeEditorPanel.add(codeEditor, BorderLayout.CENTER);

        codeEditorScrollPane = new JScrollPane(codeEditorPanel);
        
        // Console
        console = new JTextArea();
        console.setEditable(false);
        consoleLbl = new JLabel("Console");
        consoleScrollPane = new JScrollPane(console);

        consolePanel = new JPanel(new BorderLayout());
        consolePanel.add(consoleLbl, BorderLayout.NORTH);
        consolePanel.add(consoleScrollPane, BorderLayout.CENTER);
        
        // Table of variables
        tableModel = new DefaultTableModel(0, 2){
            @Override
            public boolean isCellEditable(int row, int column) {
                //FALSE TO MAKE TABLE UNEDITABLE
                return false;
            }
        };

        tableVar = new JTable(tableModel);
        tableVarLbl = new JLabel("Table of Variables");
        tableModel.setColumnIdentifiers(new String[]{"Type", "Variable Name"});
        tableVar.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableVar.setSelectionBackground(tableVar.getBackground());
        tableVar.getTableHeader().setReorderingAllowed(false);
        tableVar.getTableHeader().setResizingAllowed(false);
        tableVarScrollPane = new JScrollPane(tableVar);
        tableVarScrollPane.setPreferredSize(new Dimension(200, 200));
        tableVarPanel = new JPanel(new BorderLayout());
        tableVarPanel.add(tableVarLbl, BorderLayout.NORTH);
        tableVarPanel.add(tableVarScrollPane, BorderLayout.CENTER);

        // Split panel between the the main panel and console panel
        splitPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT, codeEditorScrollPane, consolePanel);
        splitPanel.setResizeWeight(0.8);

        // Content Panel to add to the JFrame
        contentPanel = new JPanel(new BorderLayout());
        contentPanel.add(splitPanel, BorderLayout.CENTER);
        contentPanel.add(tableVarPanel, BorderLayout.EAST);
        
        setJMenuBar(mb);
        add(contentPanel, BorderLayout.CENTER);
        setLocationRelativeTo(null);
        // Initialize the LinePainter with the codeEditor
        linePainter = new LinePainter(codeEditor);

        codeEditor.getDocument().addDocumentListener(new DocumentListener() {
        @Override
        public void insertUpdate(DocumentEvent e) {
            executeCode.setEnabled(false);
            updateLineNumbers();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            executeCode.setEnabled(false);
            updateLineNumbers();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            executeCode.setEnabled(false);
            updateLineNumbers();
        }
    });

    // Action listener for when the Open File menu item is clicked
    openFile.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            executeCode.setEnabled(false);
            openFileMethod();
        }
    });

    // Action listener for when the New File menu item is clicked
    newFile.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            executeCode.setEnabled(false);
            newFileMethod();
            saveAsMethod();
        }
    });

    // Action listener for when the Save As menu item is clicked
    saveAs.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            saveAsMethod();
        }
    });

    // Action listener for when the Save menu item is clicked
    save.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            saveMethod();
        }
    });

    // Action listener for when the Compile Code menu item is clicked
    compileCode.addMenuListener(new MenuListener() {
        @Override
        public void menuSelected(MenuEvent e) {
            if (currentFile == null) {
                int option = JOptionPane.showConfirmDialog(
                        Bama_Marzo_Yparraguirre_FinalProj.this,
                        "Save changes before compiling?",
                        "Save",
                        JOptionPane.YES_NO_CANCEL_OPTION);
    
                if (option == JOptionPane.YES_OPTION) {
                    saveMethod();
                } else if (option == JOptionPane.CANCEL_OPTION || option == JOptionPane.CLOSED_OPTION) {
                    return;  // Cancel compilation if user cancels the save dialog
                } else {
                    // If the user selects "No" or closes the dialog, proceed without saving
                }
            }
    
            compileCode();
        }
    
        @Override
        public void menuDeselected(MenuEvent e) {
            // This method is called when the menu is deselected
        }
    
        @Override
        public void menuCanceled(MenuEvent e) {
            // This method is called when the menu is canceled
        }
    });

    // Action listener for when the Show Tokenized Code menu item is clicked
    showTokenizedCode.addMenuListener(new MenuListener() {
            @Override
            public void menuSelected(MenuEvent e) {
                showTokenizedCode();
            }

            @Override
            public void menuDeselected(MenuEvent e) {
                // This method is called when the menu is deselected
            }

            @Override
            public void menuCanceled(MenuEvent e) {
                // This method is called when the menu is canceled
            }
        });

        executeCode.addMenuListener(new MenuListener() {
            @Override
            public void menuSelected(MenuEvent e) {
                executeProgram();
            }

            @Override
            public void menuDeselected(MenuEvent e) {
                // This method is called when the menu is deselected
            }

            @Override
            public void menuCanceled(MenuEvent e) {
                // This method is called when the menu is canceled
            }
        });

        addShortcutKey(compileCode, KeyEvent.VK_R, InputEvent.CTRL_DOWN_MASK);
        addShortcutKey(showTokenizedCode, KeyEvent.VK_T, InputEvent.CTRL_DOWN_MASK);
        addShortcutKey(executeCode, KeyEvent.VK_E, InputEvent.CTRL_DOWN_MASK);
    }
    private void addShortcutKey(JMenu menu, int keyCode, int modifiers) {
        KeyStroke keyStroke = KeyStroke.getKeyStroke(keyCode, modifiers);

        // Create an AbstractAction for the corresponding menu item action
        AbstractAction action = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Perform the corresponding action based on the menu item
                if (menu == compileCode) {
                    compileCode();
                } else if (menu == showTokenizedCode) {
                    showTokenizedCode();
                } else if (menu == executeCode) {
                    if (executeCode.isEnabled())
                        executeProgram();
                }
            }
        };

        // Map the KeyStroke to the AbstractAction in the JFrame's input map
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(keyStroke, menu.getText());
        getRootPane().getActionMap().put(menu.getText(), action);
    }
    // Method for opening file with a file chooser window
    private void openFileMethod(){
        
        JFileChooser fc = new JFileChooser();
        
        // Create a file filter to only allow .iol files
        FileFilter iolFilter = new FileFilter() {
            public boolean accept(File file) {
                return file.isDirectory() || file.getName().endsWith(".iol");
            }

            public String getDescription() {
                return "IOL Files (*.iol)";
            }
        };
        
        fc.setFileFilter(iolFilter);

        int returnVal = fc.showOpenDialog(null);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            if (file.exists()) {
                try {
                    if (file.getName().endsWith(".iol")) {
                        codeEditor.setText("");
                        tokenizedCodeArea.setText("");
                        tableModel.setRowCount(0);
                        console.setText("");
                        // Read the content of the selected file
                        BufferedReader reader = new BufferedReader(new FileReader(file));
                        StringBuilder content = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            content.append(line).append("\n");
                        }
                        reader.close();

                        // Set the file content to the codeEditor JTextArea
                        codeEditor.setText(content.toString());

                        currentFile = file;

                        codeEditorLbl.setText(file.getName());
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            } else {
                JOptionPane.showMessageDialog(null, "File does not exist.", "File Not Found", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // Method for creating a new file and resetting the corresponding UI components
    private void newFileMethod(){
        int choice = JOptionPane.showConfirmDialog(
            null,
            "Do you want to create a new file? Any unsaved changes will be lost.",
            "Confirm New File",
            JOptionPane.YES_NO_OPTION
        );

        if (choice == JOptionPane.YES_OPTION) {
            // Clear the content of the code editor
            codeEditor.setText("");
            tokenizedCodeArea.setText("");
            tableModel.setRowCount(0);
            console.setText("");
        }
    }

    // Method for saving a file as another file
    private void saveAsMethod(){
        JFileChooser fc = new JFileChooser(System.getProperty("user.home"));
        
        // Add a file filter for .iol files
        fc.setFileFilter(new FileNameExtensionFilter("IOL Files (*.iol)", "iol"));

        int returnVal = fc.showSaveDialog(null);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            
            // Ensure that the file has a .iol extension
            if (!file.getName().endsWith(".iol")) {
                file = new File(file.getAbsolutePath() + ".iol");
            }

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                // Write the content of the code editor to the file
                writer.write(codeEditor.getText());
                codeEditorLbl.setText(file.getName());
                currentFile = file;
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    // Method for saving the current changes to the file
    private void saveMethod() {
        if (currentFile == null) {
            // If no file is currently loaded, prompt the user to choose a location to save
            JFileChooser fc = new JFileChooser();

            // Add a file filter for .iol files
            fc.setFileFilter(new FileNameExtensionFilter("IOL Files (*.iol)", "iol"));

            int returnVal = fc.showSaveDialog(null);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                currentFile = fc.getSelectedFile();
            }
            codeEditorLbl.setText(currentFile.getName());
        }

        if (currentFile != null) {
            // Ensure that the file has a .iol extension
            if (!currentFile.getName().endsWith(".iol")) {
                currentFile = new File(currentFile.getAbsolutePath() + ".iol");
            }

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(currentFile))) {
                // Write the content of the codeEditor JTextArea to the current file
                writer.write(codeEditor.getText());
                codeEditorLbl.setText(currentFile.getName());
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    // Method for updating the number line in the left side of the code editor
    private void updateLineNumbers() {
        int lineCount = codeEditor.getDocument().getDefaultRootElement().getElementCount();
        StringBuilder numbers = new StringBuilder();

        for (int i = 1; i <= lineCount; i++) {
            numbers.append(" ").append(i).append(" ").append("\n");
        }

        lineNumbers.setText(numbers.toString());
        highlightActiveLine();
    }

    private void highlightActiveLine() {
        // Reset the highlight using LinePainter
        linePainter.resetHighlight();
    }
    
    // Class for handling the lexical analysis
    public class Lexeme {
        private String token; // Token type (e.g., KEYWORD, INT_LIT, IDENT)
        private String lexeme; // Lexeme content
        private int line;
        public Lexeme(String token, String lexeme, int line) {
            this.token = token;
            this.lexeme = lexeme;
            this.line = line;
        }
        public String getToken() {
            return token;
        }
        public String getLexeme() {
            return lexeme;
        }
        public int getLine() {
            return line;
        }
    }
    

    public List<Lexeme> lexemes = new ArrayList<>();
    public List<Lexeme> variables = new ArrayList<>();

    private Set<String> addedLexemes = new HashSet<>();
    private Lexeme word;
    
    // Mehtod to check if a given word is a keyword
    public boolean isKeyword(String word) {
        String[] keywords = {"DEFINE", "INTO", "IS", "BEG", "PRINT","INT","STR","NEWLN","LOI","IOL","ADD","SUB","MULT","DIV","MOD"};
    
        for (String keyword : keywords) {
            if (word.equals(keyword)) {
                return true;
            }
        }
        return false;
    }

    public boolean isCommand(String word) {
        String[] keywords = {"DEFINE", "INTO", "BEG", "PRINT", "NEWLN", "IOL", "LOI"};
    
        for (String keyword : keywords) {
            if (word.equals(keyword)) {
                return true;
            }
        }
        return false;
    }
    public boolean isOppr(String word) {
        String[] oppr = {"ADD","SUB","MULT","DIV","MOD"};
    
        for (String keyword : oppr) {
            if (word.equals(keyword)) {
                return true;
            }
        }
        return false;
    }

    // Method to check if a given word is an integer literal
    public boolean isIntLIT(String word) {
        try {
            // Attempt to parse the string as an integer
            Long.parseLong(word);
            // If parsing succeeds, it's an integer
            return true;
        } catch (NumberFormatException e) {
            // If parsing fails, it's not an integer
            return false;
        }
    }
    
    // Method to check if a given word is a valid variable name
    public boolean isVariableName(String word) {
        return word.matches("[a-zA-Z][a-zA-Z0-9]*");
    }

    boolean lexicalSuccess = true;
    boolean syntaxAndSemanticSuccess = true;
    //Method for compiling code and performing lexical analysis
    private void compileCode() {
        successfulComp = true;
        lexicalSuccess = true;
        syntaxAndSemanticSuccess = true;
        console.setText("");//Clears console Text for errors
        saveMethod();
        tableModel.setRowCount(0);
        int count = 0;//To track what line that contains ERR_LEX
        addedLexemes.clear();
        lexemes.clear(); // Clears the contents of the lexemes list
        variables.clear();
        String codeEditorContent = codeEditor.getText();
        StringBuilder tokenizedForm = new StringBuilder(); // Initialize as a StringBuilder
        // Split the content into lines
        String[] perLine = codeEditorContent.split("\n");
        // Process each line
        console.append("\nExecute lexical analysis: \n");
        for (String line : perLine) {
            // Skip empty lines
            count++;
        
            try {
                if (line.trim().isEmpty()) {
                    continue;
                }
        
                String[] perLexeme = line.trim().split("[\\s\\t]+");
        
                // Split the line into tokens using one or more spaces
                for (String lexeme : perLexeme) {
                    if (isKeyword(lexeme)) { // token,KEYWORD
                        word = new Lexeme(lexeme, "KEYWORD", count);
                        lexemes.add(word);
                    } else if (isIntLIT(lexeme)) {
                        word = new Lexeme("INT_LIT", lexeme, count);
                        lexemes.add(word);
                    } else if (isVariableName(lexeme)) {
                        word = new Lexeme("IDENT", lexeme, count);
                        lexemes.add(word);
                    } else {
                        lexicalSuccess = false;
                        successfulComp = false;
                        word = new Lexeme("ERR_LEX", lexeme, count);
                        tokenizedForm.append("ERR_LEX");
                        lexemes.add(word);
                        console.append("Error lexeme at line " + count + " Lexeme: [ " + lexeme + " ]\n");
                    }
                }
            } catch (Exception e) {
                // Handle the exception (e.g., log it, throw a different exception, etc.)
                System.err.println("Exception during lexeme processing: " + e.getMessage());
                // Provide a default action or take appropriate steps in case of an exception
            }
        }
        
        if (lexicalSuccess) console.append("\n" + "No Lexical Errors Found\n");
        showTokenizedCode();
        printToTableOfVariables();
        syntaxAndSemanticAnalysis();
        
        if (syntaxAndSemanticSuccess) {
            console.append("\n" + "No Syntax and Semantic Errors Found\n");   
        }

        if (lexicalSuccess && syntaxAndSemanticSuccess) {
            console.append("\n" + currentFile.getName() + " compiled with no errors found");
            executeCode.setEnabled(true);
        }
    }
    
    // Method to display tokenized code in the TokenizedCodeArea
    private void showTokenizedCode() {
        saveMethod();
    
        JDialog tkDialog = new JDialog();
        tkDialog.setTitle("Tokenized Code");
        tkDialog.setLayout(new BorderLayout());
    
        JTextArea tokenizedCodeArea = new JTextArea();
        tokenizedCodeArea.setEditable(false);
    
        JScrollPane tkScrollPane = new JScrollPane(tokenizedCodeArea);
    
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> tkDialog.dispose());
    
        tkDialog.add(tkScrollPane, BorderLayout.CENTER);
        tkDialog.add(closeButton, BorderLayout.SOUTH);
    
        tkDialog.setSize(400, 300);
        tkDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        tkDialog.setLocationRelativeTo(null);
        tkDialog.setModal(true);
    
        // Tokenize the code before setting the text area content
        String codeEditorContent = codeEditor.getText();
        StringBuilder tokenizedForm = new StringBuilder();
    
        // Split the content into lines
        String[] perLine = codeEditorContent.split("\n");
        for (String line : perLine) {
            if (line.trim().isEmpty()) {
                continue;
            }
            String[] perLexeme = line.trim().split("\\s+");
            for (String lexeme : perLexeme) {
                if (isKeyword(lexeme)) {
                    tokenizedForm.append(lexeme);
                } else if (isIntLIT(lexeme)) {
                    tokenizedForm.append("INT_LIT");
                } else if (isVariableName(lexeme)) {
                    tokenizedForm.append("IDENT");
                } else {
                    tokenizedForm.append("ERR_LEX");
                }
                tokenizedForm.append(" ");
            }
            tokenizedForm.append("\n"); // Add a newline character after each line
        }
    
        // Set the generated tokenized code to the tokenizedCodeArea before making the dialog visible
        tokenizedCodeArea.setText(tokenizedForm.toString());
        saveTokenizedCodeToTknFile(tokenizedForm);
    
        // Make the dialog visible after setting the text
        tkDialog.setVisible(true);
    }
    
    

    // Display output in the table of variables
    public void printToTableOfVariables() {
        tableModel.setRowCount(0);
        // Iterate through the lexemes using an index
        for (int i = 0; i < lexemes.size(); i++) {
            Lexeme currentLexeme = lexemes.get(i);
            String currentToken = currentLexeme.getToken();

            // Check if the current lexeme is "INT" and there is a next lexeme
            if ("INT".equals(currentToken) && i + 1 < lexemes.size()) {
                Lexeme nextLexeme = lexemes.get(i + 1);
                String nextToken = nextLexeme.getToken();

                // Check if the next lexeme is "IDENT"
                if ("IDENT".equals(nextToken)) {
                    String[] row = {currentToken, nextLexeme.getLexeme()};
                    tableModel.addRow(row);
                    word = new Lexeme(currentToken, nextLexeme.getLexeme(), 0);
                    variables.add(word);
                }
            }
            else if ("STR".equals(currentToken) && i + 1 < lexemes.size()) {
                Lexeme nextLexeme = lexemes.get(i + 1);
                String nextToken = nextLexeme.getToken();

                // Check if the next lexeme is "IDENT"
                if ("IDENT".equals(nextToken)) {
                    String[] row = {currentToken, nextLexeme.getLexeme()};
                    tableModel.addRow(row);
                    word = new Lexeme(currentToken, nextLexeme.getLexeme(), 0);
                    variables.add(word);
                }
            }
        }
    }
    public static String inputFilePath;
    // Method to save tokenized code to a .tkn file
    private void saveTokenizedCodeToTknFile(StringBuilder tokenizedForm) {
        inputFilePath = currentFile.getAbsolutePath();
        File tknFile = new File(inputFilePath.replace(".iol", ".tkn"));
    
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(tknFile))) {
            // Write the content of the tokenizedCodeArea to the .tkn file
            writer.write(tokenizedForm.toString()); // Convert StringBuilder to String
            console.append("\nTokenized code has been saved to " + tknFile.getName() + "\n");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
    // Class representing integer data with variable name and value
    public class intData {
        private String variableName; // Token type (e.g., KEYWORD, INT_LIT, IDENT)
        private long value; // Lexeme content
    
         // Constructor for IntData class
        public intData(String variableName, long value) {
            this.variableName = variableName;
            this.value = value;
        }
    
        // Getter method for variable name
        public String getVarName() {
            return variableName;
        }
    
        // Getter method for variable value
        public long getVarValue() {
            return value;
        }

        public void setVarValue(long newValue) {
            this.value = newValue;
        }
    }

    // Class representing string data with variable name and value
    public class strData {
        private String variableName; // Token type (e.g., KEYWORD, INT_LIT, IDENT)
        private String value; // Lexeme content
    
         // Constructor for StrData class
        public strData(String variableName, String value) {
            this.variableName = variableName;
            this.value = value;
        }
    
        // Getter method for variable name
        public String getVarName() {
            return variableName;
        }
    
        // Getter method for variable value
        public String getVarValue() {
            return value;
        }

        public void setVarValue(String newValue) {
            this.value = newValue;
        }
    }

    public List<intData> intVar = new ArrayList<>();
    public List<strData> strVar = new ArrayList<>();
    private intData intV;
    private strData strV;

    public static String currentToken;
    public static String currentLexeme;
    public static int currentLine;
    public static int curExpr1;
    public Queue<String> currentStatements = new LinkedList<>();

    boolean errorExist = false;
    boolean errorInEXPR = false;
    boolean successfulComp = true;

    // Method for syntax analysis of the code
    private void syntaxAndSemanticAnalysis() {
        errorExist = false;
        errorInEXPR = false;
        successfulComp = false;
        emptyStack();
        intVar.clear();
        strVar.clear();
        console.append("\nExecute syntax analysis and analysis of static semantics: \n");
        error = "";
        for (int i = 0; i < lexemes.size(); i++) {
            errorInEXPR = false;
            Lexeme lexeme = lexemes.get(i);
            Lexeme type, Is;
            currentToken = lexeme.getToken();
            currentLexeme = lexeme.getLexeme();
            currentLine = lexeme.getLine();
            String errorType = "";

            Lexeme eval = lexemes.get(0), eval2 = lexemes.get(lexemes.size()-1);
            
            String firstToken = eval.getToken();
            String lastToken = eval2.getToken();
            if (!firstToken.equals("IOL") || !lastToken.equals("LOI")) {
                errorExist = true;
                error = "MUST START WITH IOL AND END WITH LOI";
                break;
            }
            else if (currentToken.equals("INT") || currentToken.equals("STR")) {
                currentStatements.offer(currentToken);
                if (currentToken.equals("INT")) {
                    i++;
                    type = lexemes.get(i);
                    String currentVariable = type.getToken();
                    currentStatements.offer(type.getLexeme());
                    if (currentVariable.equals("IDENT")) {
                        Is = lexemes.get(i+1);
                        currentStatements.offer(Is.getToken());
                        if (Is.getToken().equals("IS")) {
                            Lexeme varValue = lexemes.get(i+2);
                            currentStatements.offer(varValue.getLexeme());
                            if (isValidInteger(varValue.getLexeme())) {
                                intV = new intData (type.getLexeme(), Long.parseLong(varValue.getLexeme()));
                                intVar.add(intV);
                                i+=2;
                            }
                            else { //error statement
                                if (varValue.getLexeme().equals("ERR_LEX")) errorType = "NOT A VALID VARIABLE";
                                else errorType = "NOT A VALID INTEGER VALUE [" + varValue.getLexeme() + "]";
                                printError(currentLine, errorType);
                                i+=2;
                            }
                        }
                        else { //int var declaration
                            intV = new intData (type.getLexeme(), 0);
                            intVar.add(intV);
                        }
			            int a = varDataCount(type.getLexeme());
                            if (a!=1) printError(currentLine, "VARIABLE REDECLARATION");
                        }
                    else {
                        errorType = "NOT A VALID VARIABLE";
                        printError(currentLine, errorType);
                    }
                }
                else if (currentToken.equals("STR")) {
                    i++;
                    type = lexemes.get(i);
                    String currentVariable = type.getToken();
                    currentStatements.offer(type.getLexeme());
                    if (currentVariable.equals("IDENT")) {
                        strV = new strData (type.getLexeme(), "");
                        strVar.add(strV);
			        int a = varDataCount(type.getLexeme());
                        if (a!=1) printError(currentLine, "VARIABLE REDECLARATION");
                    }
                    else {
                        if (currentVariable.equals("ERR_LEX")) errorType = "NOT A VALID VARIABLE";
                        else errorType = "2INVALID FORMAT";
                        printError(currentLine, errorType);
                    }
                }
            }

            else if (currentToken.equals("INTO")) {
                into = true;
                currentStatements.offer(currentToken);
                i++;
                curExpr1 = i;
                Lexeme intoVar = lexemes.get(i);
                currentStatements.offer(intoVar.getLexeme());
                if (intoVar.getToken().equals("IDENT") && searchData(intoVar.getLexeme())) {
                    i++;
                    Is = lexemes.get(i);
                    if (Is.getToken().equals("IS")) {
                        i++;
                        currentStatements.offer(Is.getToken());
                        i = checkExpr0(i) - 1;
                        //System.out.println("Pos: " + i);
                    }
                }
                else {
                    if (intoVar.getToken().equals("ERR_LEX")) errorType = "NOT A VALID VARIABLE [" + intoVar.getLexeme() + "]";
                    else if (intoVar.getLexeme().equals("KEYWORD")) {i -= 1; errorType = "3INVALID FORMAT";}
                    else errorType = "UNDECLARED VARIABLE [" + intoVar.getLexeme() +"]";
                    printError(currentLine, errorType);
                }
            }

            else if (currentToken.equals("PRINT")) {
                into = false;
                currentStatements.offer(currentToken);
                i++;
                i = checkExpr0(i)-1;
            }
            else if (currentToken.equals("BEG")) {
                currentStatements.offer(currentToken);
                i++;
                Lexeme begVar = lexemes.get(i);
                currentStatements.offer(begVar.getToken());
                if (begVar.getToken().equals("IDENT") && searchData(begVar.getLexeme())) {
                }
                else {
                    errorExist = true;
                    if (begVar.getToken().equals("ERR_LEX")) errorType = "NOT A VALID VARIABLE";
                    else if (begVar.getLexeme().equals("KEYWORD")) {i -= 1; errorType = "5INVALID FORMAT";}
                    else errorType = "UNDECLARED VARIABLE [" + begVar.getLexeme() +"]";
                    printError(currentLine, errorType);
                }
            }
            else if (isOppr(currentToken)) {
                i = checkExpr0(i) - 1;
                //System.out.println("PosExpr: " + i);
            }
            else if (currentToken.equals("NEWLN")) continue;
            else if (currentToken.equals("IOL") || currentToken.equals("LOI")) continue;
            else {
                i = checkExpr0(i)-1;
                emptyStack();
                if (isKeyword(currentToken)) {
                    currentLexeme = currentToken;
                }
            }
            emptyStack();
        }
        if (errorExist) errorsPartII();
    }

    public void emptyStack() {
        while (!currentStatements.isEmpty()) {
            currentStatements.poll();
        }
    }
    String error = "";
    public void printError (int line, String errorType) {
        errorExist = true;
        syntaxAndSemanticSuccess = false;
        error += "ERROR: " + errorType + " in [";
        while (!currentStatements.isEmpty()) {
            String a = currentStatements.poll();
            error += a;
            if (currentStatements.peek() != null) error += " ";
        }
        error += "] on line " + line + " \n";
        errorType = ""; //clear error type
    }

    void errorsPartII() {
        syntaxAndSemanticSuccess = false;
        successfulComp = false;
        console.append("\nErrors Found: \n" + error);
    }

    public int pos;
    boolean activeOppr = false;
    boolean into = false;

    // Method to check expressions at level 0

    public int checkExpr0 (int count0) {
        String errorType = "";
        activeOppr = false;
        pos = count0;
        Lexeme expr = lexemes.get(pos);
        System.out.println("DIRI: " + expr.getToken());
        if (isIntLIT(expr.getLexeme())) {
            currentStatements.offer(expr.getLexeme()); 
            return pos+1;}
        else if (expr.getToken().equals("IDENT")) {
            String a = lexemes.get(curExpr1).getLexeme();
            String b = lexemes.get(pos).getLexeme();
            if (!searchData(expr.getLexeme())) {
                currentStatements.offer(expr.getLexeme()); 
                errorType = "UNDECLARED VARIABLE" + " [" + expr.getLexeme() + "]";
                printError(currentLine, errorType);
            }
            else if (into && !((searchIntData(a) && searchIntData(b)) || (searchStrData(a) && searchStrData(b))))  {
                currentStatements.offer(expr.getLexeme()); 
                System.out.println(":)");
                errorType = "TYPE INCOMPABILITY";
                printError(currentLine, errorType);
            }
            //System.out.println("POS:" + pos + ">>>>>" + expr.getLexeme());
            currentStatements.offer(expr.getLexeme()); 
            return pos+1;
        }
        else if (isOppr(expr.getToken())) {
            activeOppr = true;
            numExpr();
        }
        else if (expr.getToken().equals("ERR_LEX")){
            currentStatements.offer(expr.getLexeme()); 
            errorType = "INVALID FORMAT";
            printError(currentLine, errorType);
            return pos+1;
        }
        return pos;
    }

    // Method for processing numeric expressions
    public void numExpr () {
        Lexeme expr1 = lexemes.get(pos);
        if (isIntLIT(expr1.getLexeme())) {currentStatements.offer(expr1.getLexeme()); pos++; }
        else if (isOppr(expr1.getToken())) {
            currentStatements.offer(expr1.getToken());
            pos++;
            expr();
            expr();
        }
    }

    // Method for processing general expressions
    public void expr () {
        String errorType = "";
        boolean err = false;
        Lexeme expr1 = lexemes.get(pos);
        if (isIntLIT(expr1.getLexeme())) {
            currentStatements.offer(expr1.getLexeme());
            pos++;
        }
        else if (expr1.getToken().equals("IDENT")) {
            currentStatements.offer(expr1.getLexeme());
            
            if (!searchIntData(expr1.getLexeme())) {
                err = true;
                errorType = "NOT AN INT LITERAL [" + expr1.getLexeme() +"]";
            }
            if (!searchData(expr1.getLexeme())) {
                err = true;
                errorType = "UNDECLARED VARIABLE [" + expr1.getLexeme() +"]";
            }
            pos++;
        }
        else if (isOppr(expr1.getToken())) {
            numExpr();
        }
        else {
            currentStatements.offer(expr1.getToken());
            printError(currentLine, "1INVALID FORMAT");
            pos++;
        }
        if (err) {
            printError(currentLine, errorType);
        }
    }

    // Method to search for variables in data structures
    public boolean searchData (String var) {
        for (intData data : intVar) {
            if (var.equals(data.getVarName())) return true;
        }
        for (strData data : strVar) {
            if (var.equals(data.getVarName())) return true;
        }
        return false;
    }
    
    public int varDataCount (String var) {
        int dataCount = 0;
        for (intData data : intVar) {
            if (var.equals(data.getVarName())) dataCount ++;
        }
        for (strData data : strVar) {
            if (var.equals(data.getVarName())) dataCount ++;
        }
        return dataCount;
    }

    public boolean searchIntData (String var) {
        for (intData data : intVar) {
            if (var.equals(data.getVarName())) return true;
        }
        return false;
    }

    public boolean searchStrData (String var) {
        for (strData data : strVar) {
            if (var.equals(data.getVarName())) return true;
        }
        return false;
    }

    // Helper method to check if a string represents a valid integer
    private boolean isValidInteger(String str) {
        try {
            Long.parseLong(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }


    public void executeProgram() {
        console.append("\n\nIOL Execution\n\n");
        for (int i = 0; i < lexemes.size(); i++) {
            typeError = false;
            String var = "";
            String tok = "";
            //COMMANDS : BEG, INTO, NEWLN, PRINT, 
            Lexeme lexeme = lexemes.get(i);
            currentToken = lexeme.getToken();
            currentLexeme = lexeme.getLexeme();
            if (currentToken.equals("BEG")){
                i++;
                var = lexemes.get(i).getLexeme();
                String userInput = JOptionPane.showInputDialog("Value for " + var + " : ");
                variableSearchAndReplace(var, userInput);
                if (typeError) {
                    console.append("\nType Mismatch\n");
                    console.append("\nProgram will now be terminated...\n");
                    return;
                }
                console.append("Input for " + var + ": " + userInput + "\n");
            }
            else if (currentToken.equals("INTO")) {
                i++;
                var = lexemes.get(i).getLexeme(); //get var name
                i+=2; //skipping IS
                Object[] result = exprEval(i, lexemes.get(i).getLexeme());
                i = (int)result[0]-1;
                System.out.println("Current: " + i);
                variableSearchAndReplace(var, resultVar((String)result[1]));
            }
            else if(currentToken.equals("PRINT")) {
                i++;
                var = lexemes.get(i).getLexeme(); //get var name
                if(var.equals("KEYWORD")) {
                    tok = lexemes.get(i).getToken();
                    if(tok.equals("ADD" )|| tok.equals("SUB") || tok.equals("MULT") || tok.equals("DIV") || tok.equals("MOD")){
                  System.out.println("print");
                        Object[] result = exprEval(i, lexemes.get(i).getLexeme());
                        console.append(resultVar((String)result[1]));
                    }
                    }else{
                console.append(resultVar(var));
            }
            }
            else if (currentToken.equals("NEWLN")) {
                console.append("\n");
            }
            else if (currentToken.equals("LOI")) {
                console.append("\n\nProgram terminated successfully...");
            }
        }
        printIntAndStrData();
    }

    public static int posCount;
    public String exprRes = "";
    public long firstExpr = 0;
    public long secondExpr = 0;

    public Object[] exprEval(int count, String var) {
        firstExpr = 0; 
        secondExpr = 0;
        posCount = count;
        Lexeme expr = lexemes.get(posCount);
        if (isIntLIT(expr.getLexeme()) || expr.getToken().equals("IDENT")) {
            exprRes = expr.getLexeme();
            //System.out.println("Pos: " + posCount);
            //System.out.println("Expr: " + exprRes);
            return new Object[]{posCount++, exprRes};
        }
        else if (isOppr(expr.getToken())) {
            //System.out.println("Pos: " + posCount);
            //System.out.println("Expr: " + expr.getToken());
           numExprEval();
        }
        return new Object[]{posCount, exprRes};
    }
    
    public long numExprEval () {
        String opp = "";
        long results = 0;
        Lexeme expr1 = lexemes.get(posCount);
        System.out.println(expr1);
        if (isIntLIT(expr1.getLexeme())) {
            System.out.println("Ye");
            posCount++; 
            return Long.parseLong(expr1.getLexeme());
        }
        else if (isOppr(expr1.getToken())) {
            System.out.println("Ya");
            opp = expr1.getToken();
            posCount++;
            firstExpr = exprEval1();
            secondExpr = exprEval1();

            results = eqSolve(opp, firstExpr, secondExpr);
            System.out.println("Ngano ka? " + results);
            exprRes = String.valueOf(results);
            System.out.println("Ngano ka? " + results);
            return results;
        }
        System.out.println("Ngano ka? " + results);
        return results;
    }

    // Method for processing general expressions
    public long exprEval1() {
        Lexeme expr1 = lexemes.get(posCount);
        if (isIntLIT(expr1.getLexeme())) { // NAGCHANGE KO DIRI
            System.out.println("Expr: " + expr1.getLexeme());
            posCount++;
            return Long.parseLong(expr1.getLexeme());
        } else if(expr1.getToken().equals("IDENT")){
            posCount++;
            //System.out.println("huy" + resultVar(expr1.getLexeme()));
            return Long.parseLong(resultVar(expr1.getLexeme()));
        }
        else if (isOppr(expr1.getToken())) {
            return numExprEval2();
        }
        return 0;
    }

    public long numExprEval2 () {
        String opp = "";
        long a, b;
        Lexeme expr1 = lexemes.get(posCount);
        if (isIntLIT(expr1.getLexeme())) {
            //System.out.println("Ye1");
            posCount++; 
            return Long.parseLong(expr1.getLexeme());
        }
        else if (isOppr(expr1.getToken())) {
            //System.out.println("Ya1");
            opp = expr1.getToken();
            posCount++;
            a = exprEval1();
            b = exprEval1();
            return eqSolve(opp, a, b);
        }
        return 0;
    }

    public long eqSolve(String opp, long a, long b) {
        long results = 0;
    
        try {
            switch (opp) {
                case "ADD":
                    results = a + b;
                    break;
                case "SUB":
                    results = a - b;
                    break;
                case "MULT":
                    results = a * b;
                    break;
                case "DIV":
                    // Check for division by zero
                    if (b != 0) {
                        results = a / b;
                    } else {
                        console.append("Error: Division by zero");
                    }
                    break;
                case "MOD":
                    // Check for division by zero
                    if (b != 0) {
                        results = a % b;
                    } else {
                        console.append("Error: Division by zero");
                    }
                    break;
            }
        } catch (ArithmeticException e) {
            console.append("Arithmetic Exception: " + e.getMessage());
        }
        System.out.println("Results: >>>>>>> " + results);
        return results;
    }
    

    boolean typeError = false;
    public void variableSearchAndReplace(String var, String userInput){
        for (intData data : intVar) {
            if (!isValidInteger(userInput)) break;
            if (var.equals(data.getVarName())) {
                data.setVarValue(Long.parseLong(userInput));
                return;
            }
        }
        for (strData data : strVar) {
            if (var.equals(data.getVarName())) {
                data.setVarValue(userInput);
                return;
            }
        }
        typeError = true;
    }

    public String resultVar (String var) {
        for (intData data : intVar) {
            if (var.equals(data.getVarName())) { 
                return String.valueOf(data.getVarValue());
            }
        }
        for (strData data : strVar) {
            if (var.equals(data.getVarName())) { 
                return data.getVarValue();
            }
        }
        return var;
    }

    public void printIntAndStrData(){
        System.out.println("INT DATA: ");
        for (intData data : intVar) {
            System.out.println(data.getVarName() + " => " + data.getVarValue());
            }
        System.out.print("\n");
        System.out.println("STR DATA: ");
        for (strData data : strVar) {
            System.out.println(data.getVarName() + " => " + data.getVarValue());
        }
    }
     // Main method to launch the compiler GUI
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Bama_Marzo_Yparraguirre_FinalProj compilerGUI = new Bama_Marzo_Yparraguirre_FinalProj();
            compilerGUI.setVisible(true);
        });
    }
}