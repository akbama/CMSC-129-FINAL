import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.KeyStroke;

public class Bama_Marzo_Yparraguirre_FinalProj extends JFrame {
    // ... other class members

    public Bama_Marzo_Yparraguirre_FinalProj() {
        // ... initialize your JFrame
        
        // Create a KeyStroke for the shortcut (Ctrl + C in this example)
        KeyStroke compileShortcut = KeyStroke.getKeyStroke(KeyEvent.VK_C, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx());

        // Create an AbstractAction for the compileCode method
        AbstractAction compileAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                compileCode();
            }
        };

        // Map the KeyStroke to the compileAction in the JFrame's input map
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(compileShortcut, "compileAction");
        getRootPane().getActionMap().put("compileAction", compileAction);
    }

    private void compileCode() {
        // Implementation of your compileCode method
        // ...
    }

    // ... other class methods
}
