package simulator.view;

import java.awt.Color;
import java.awt.Component;
import java.awt.Frame;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

/** Utility helpers for the Swing views. */
public class ViewUtils {

    // Return the frame to which 'c' belongs
    public static Frame getWindow(Component c) {
        Frame w = null;
        if (c != null) {
            if (c instanceof Frame)
                w = (Frame) c;
            else
                w = (Frame) SwingUtilities.getWindowAncestor(c);
        }
        return w;
    }

    // Show an error dialog relative to the window of 'c'
    public static void showErrorMsg(Component c, String msg) {
        JOptionPane.showMessageDialog(getWindow(c), msg, "ERROR", JOptionPane.ERROR_MESSAGE);
    }

    // Ask confirmation and quit the application
    public static void quit(Component c) {
        int n = JOptionPane.showOptionDialog(getWindow(c), "Are sure you want to quit?", "Quit",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);
        if (n == 0) {
            System.exit(0);
        }
    }

    // Convert hash code of 's' to a Color
    public static Color get_color(Object s) {
        return new Color(s.hashCode());
    }

    // Convenience overload
    public static void showErrorMsg(String msg) {
        showErrorMsg(null, msg);
    }
}
