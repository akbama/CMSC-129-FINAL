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
import java.util.Stack;
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
class Bama_Marzo_Yparraguirre_PE04 extends JFrame {

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

    public Bama_Marzo_Yparraguirre_PE04() {

        setTitle("Syntax Analyzer");
        setSize(800, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // Menu and Menu Items
        mb = new JMenuBar();
        file = new JMenu("File");
        compileCode = new JMenu("Compile Code");
        showTokenizedCode = new JMenu("Show Tokenized Code");
        executeCode = new JMenu("Execute Code");
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
        //mb.add(executeCode); // this line of code doesn't have any used in the current version of the program

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
            updateLineNumbers();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            updateLineNumbers();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            updateLineNumbers();
        }
    });

    // Action listener for when the Open File menu item is clicked
    openFile.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            openFileMethod();
        }
    });

    // Action listener for when the New File menu item is clicked
    newFile.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
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
            if (currentFile == null)  {
                saveAsMethod();
            }
            if (currentFile != null) {
                compileCode();
            }
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
            Integer.parseInt(word);
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
            if (line.trim().isEmpty()) {
                continue;
            }
            String[] perLexeme = line.split("[\\s\\t]+");
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
                    word = new Lexeme("ERR_LEX", "ERR_LEX", count);
                    tokenizedForm.append("ERR_LEX");
                    lexemes.add(word);
                    console.append("Error lexeme at line " + count + " Lexeme: [ " + lexeme + " ]\n");
                }
            }
        }
        
        if (lexicalSuccess) console.append("\n" + "No Lexical Errors Found\n");
        showTokenizedCode();
        printToTableOfVariables();
        syntaxAndSemanticAnalysis();
        if (syntaxAndSemanticSuccess) console.append("\n" + "No Syntax and Semantic Errors Found\n");

        if (lexicalSuccess && syntaxAndSemanticSuccess) {
            console.append("\n" + currentFile.getName() + " compiled with no errors found");
        }
    }
    
    // Method to display tokenized code in the TokenizedCodeArea
    private void showTokenizedCode() {
        saveMethod();
        JFrame tkFrame = new JFrame("Tokenized Code");
        JTextArea tokenizedCodeArea = new JTextArea();
        JScrollPane tkScrollPane = new JScrollPane(tokenizedCodeArea);

        tkFrame.setSize(400, 300);
        tkFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        tkFrame.setLocationRelativeTo(null);
        tkFrame.setVisible(true);

        tokenizedCodeArea.setEditable(false);

        tkFrame.add(tkScrollPane);

        String codeEditorContent = codeEditor.getText();
        StringBuilder tokenizedForm = new StringBuilder(); // Initialize as a StringBuilder
        // Split the content into lines
        String[] perLine = codeEditorContent.split("\n");
        for (String line : perLine) {
            if (line.trim().isEmpty()) {
                continue;
            }
            String[] perLexeme = line.split("\\s+");
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
        tokenizedCodeArea.append(tokenizedForm.toString());
        saveTokenizedCodeToTknFile(tokenizedForm);
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
        private int value; // Lexeme content
    
         // Constructor for IntData class
        public intData(String variableName, int value) {
            this.variableName = variableName;
            this.value = value;
        }
    
        // Getter method for variable name
        public String getVarName() {
            return variableName;
        }
    
        // Getter method for variable value
        public int getVarValue() {
            return value;
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
                error = "INVALID CODE";
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
                                intV = new intData (type.getLexeme(), Integer.parseInt(varValue.getLexeme()));
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
                    }
                    else {
                        if (currentVariable.equals("ERR_LEX")) errorType = "NOT A VALID VARIABLE";
                        else errorType = "INVALID FORMAT";
                        printError(currentLine, errorType);
                    }
                }
            }

            else if (currentToken.equals("INTO")) {
                into = true;
                errorInEXPR = false;
                currentStatements.offer(currentToken);
                i++;
                curExpr1 = i;
                Lexeme intoVar = lexemes.get(i);
                currentStatements.offer(intoVar.getLexeme());
                if (intoVar.getToken().equals("IDENT") && searchData(intoVar.getLexeme())) {
                    i++;
                    Is = lexemes.get(i);
                    if (Is.getToken().equals("IS")) {
                        currentStatements.offer(Is.getToken());
                        i++;
                        i = checkExpr0(i) - 1;
                        String current = "";
                        Lexeme error = lexemes.get(i+1);
                        current = error.getLexeme();
                        while (!isCommand(error.getToken())){
                            i++;
                            error = lexemes.get(i);
                            errorInEXPR = true;
                            current = error.getLexeme();
                            if (error.getLexeme().equals("KEYWORD")) current = error.getToken();
                            if (isCommand(error.getToken())) {
                                i -=1;
                                break;
                            }
                            currentStatements.offer(current);
                        }
                        if (errorInEXPR) {
                            errorExist = true;
                            errorType = "INVALID FORMAT";
                            printError(currentLine, errorType);
                        }
                    }
                    else {
                        currentStatements.offer(Is.getLexeme());
                        errorType = "INVALID FORMAT";
                        Lexeme error = lexemes.get(i+1);
                        String current = error.getLexeme();
                        while (!isCommand(error.getToken())){
                            i++;
                            error = lexemes.get(i);
                            errorInEXPR = true;
                            current = error.getLexeme();
                            if (error.getLexeme().equals("KEYWORD")) current = error.getToken();
                            if (isCommand(error.getToken())) {
                                i -=1;
                                break;
                            }
                            currentStatements.offer(current);
                        }
                        printError(currentLine, errorType);
                    }
                }
                else {
                    if (intoVar.getToken().equals("ERR_LEX")) errorType = "NOT A VALID VARIABLE";
                    else if (intoVar.getLexeme().equals("KEYWORD")) {i -= 1; errorType = "INVALID FORMAT";}
                    else errorType = "UNDECLARED VARIABLE [" + intoVar.getLexeme() +"]";
                    printError(currentLine, errorType);
                }
            }

            else if (currentToken.equals("PRINT")) {
                into = false;
                currentStatements.offer(currentToken);
                i++;
                i = checkExpr0(i)-1;
                String current = "";
                Lexeme error = lexemes.get(i+1);
                current = error.getLexeme();
                while (!isCommand(error.getToken())){
                    i++;
                    error = lexemes.get(i);
                    errorInEXPR = true;
                    current = error.getLexeme();
                    if (error.getLexeme().equals("KEYWORD")) current = error.getToken();
                    if (isCommand(error.getToken())) {
                        i -=1;
                        break;
                    }
                    currentStatements.offer(current);
                }
                if (errorInEXPR) {
                    errorType = "INVALID FORMAT";
                    errorExist = true;
                    printError(currentLine, errorType);
                }
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
                    else if (begVar.getLexeme().equals("KEYWORD")) {i -= 1; errorType = "INVALID FORMAT";}
                    else errorType = "UNDECLARED VARIABLE [" + begVar.getLexeme() +"]";
                    printError(currentLine, errorType);
                }
            }
            else if (currentToken.equals("NEWLN")) continue;
            else if (currentToken.equals("IOL") || currentToken.equals("LOI")) continue;
            else {
                if (isKeyword(currentToken)) {
                    currentLexeme = currentToken;
                }
                else {
                    currentStatements.offer(currentLexeme);
                    errorType = "INVALID FORMAT";
                    printError(currentLine, errorType);
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
        successfulComp = false;
        console.append("\nErrors Found: \n" + error);
    }

    public int count;
    boolean activeOppr = false;
    boolean into = false;
    // Method to check expressions at level 0
    public int checkExpr0 (int count0) {
        String errorType = "";
        activeOppr = false;
        count = count0;
        Lexeme expr = lexemes.get(count);
        if (isIntLIT(expr.getLexeme())) {
            currentStatements.offer(expr.getLexeme()); 
            return count+1;}
        else if (expr.getToken().equals("IDENT")) {
            String a = lexemes.get(curExpr1).getLexeme();
            String b = lexemes.get(count).getLexeme();
            if (into && !((searchIntData(a) && searchIntData(b)) || (searchStrData(a) && searchStrData(b))))  {
                currentStatements.offer(expr.getLexeme()); 
                errorType = "TYPE INCOMPABILITY";
                printError(currentLine, errorType);
            }
            else if (!searchData(expr.getLexeme())) {
                currentStatements.offer(expr.getLexeme()); 
                errorType = "UNDECLARED VARIABLE";
                printError(currentLine, errorType);
            }
            currentStatements.offer(expr.getLexeme()); 
            return count+1;
        }
        else if (isOppr(expr.getToken())) {
            activeOppr = true;
            numExpr();
        }
        return count;
    }

    // Method for processing numeric expressions
    public void numExpr () {
        Lexeme expr1 = lexemes.get(count);
        if (isIntLIT(expr1.getLexeme())) {currentStatements.offer(expr1.getLexeme()); count++; }
        else if (isOppr(expr1.getToken())) {
            currentStatements.offer(expr1.getToken());
            count++;
            expr();
            expr();
        }
    }

    // Method for processing general expressions
    public void expr () {
        boolean err = false;
        Lexeme expr1 = lexemes.get(count);
        if (isIntLIT(expr1.getLexeme())) {
            currentStatements.offer(expr1.getLexeme());
            count++;
        }
        else if (expr1.getToken().equals("IDENT")) {
            currentStatements.offer(expr1.getLexeme());
            if (!searchData(expr1.getLexeme())) {
                err = true;
            }
            else {
                if (!searchIntData(expr1.getLexeme()) && activeOppr) {
                err = true;
            }
            if (err) {
                printError(currentLine, "INVALID FORMAT");
            }
            }
            count++;
        }
        else if (isOppr(expr1.getToken())) {
            numExpr();
        }
        else {
            printError(currentLine, "INVALID FORMAT");
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
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

     // Main method to launch the compiler GUI
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Bama_Marzo_Yparraguirre_PE04 compilerGUI = new Bama_Marzo_Yparraguirre_PE04();
            compilerGUI.setVisible(true);
        });
    }
}