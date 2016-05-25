// This program works in Windows running Java 8.  Tested in Windows 10 and 7.

import java.awt.*;
import java.awt.event.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.*;
import javax.swing.JOptionPane; 

public class AutoSierra {

    private static final String ENCODING = "UTF-8";
    private static final String[] ITEM_TYPES = {"books", "magazines"};
    private static final String BARCODE_PREFIX = "31223";
    private static final int BARCODE_LENGTH = 13;
    private static final int BOOK_DELAY = 3500;
    private static final int MAGAZINE_DELAY = 15000;
    
    
    public static void main(String[] args) throws AWTException,IOException {

        // Get filename
        String filename = JOptionPane.showInputDialog(null, "Enter input filename with extension \n(Ex: file.txt):", "file.txt");

        // Get item type
        String type = (String)JOptionPane.showInputDialog(null, "Select item type",
        "Select the item type to be processed.  This adjusts the delay time between keystrokes generated by this program.", JOptionPane.QUESTION_MESSAGE, null,
        ITEM_TYPES, ITEM_TYPES[0]);

        // Debug:
        // String encoding = JOptionPane.showInputDialog(null, "Enter name of encoding for input \n(Ex: UTF-16):", "UTF-16");
        // String process = JOptionPane.showInputDialog(null, "Enter name of process with extension to launch \n(Ex: sierra.exe):", "sierra.exe");
        
        // Read text file from filename and convert to String array
        String[] text = readTextFile(filename);
        
        // Verify file contents; if any barcodes aren't 13 digits, don't start with the correct prefix, or contain a non-digit character, abort.
        for (int i = 0; i < text.length; i++) {
            if (text[i].length() != BARCODE_LENGTH) {
                JOptionPane.showMessageDialog(null, "The following barcode is the wrong length; fix it and try again:\n" + text[i],
                "Alert", JOptionPane.WARNING_MESSAGE);
                System.exit(0);
            }
            if (!text[i].startsWith(BARCODE_PREFIX)) {
                JOptionPane.showMessageDialog(null, "The following barcode starts with a prefix other than " + BARCODE_PREFIX + 
                "; fix it and try again:\n" + text[i],
                "Alert", JOptionPane.WARNING_MESSAGE);
                System.exit(0);
            }
            if (!text[i].matches("[0-9]+")) {
                JOptionPane.showMessageDialog(null, "The following barcode contains a non-digit character; fix it and try again:\n" + text[i],
                "Alert", JOptionPane.WARNING_MESSAGE);
                System.exit(0);
                
            }
        }
        
        // Display beginning message and offer to abort
        int result = JOptionPane.showConfirmDialog(null, "Beginning batch processing; switch the active window to Sierra in the next 15 seconds, \n" +
                                                        "then do not use the mouse or keyboard until the completion dialog box appears.",
        "Alert", JOptionPane.OK_CANCEL_OPTION);
        
        if (result != 0) {
            System.exit(0);
        }
        
        // Begin timing the batch
        long startTime = System.nanoTime();
        
        // Launch Sierra as active window.  Problem: can't launch Sierra as easily as notepad.
        Runtime.getRuntime().exec("notepad.exe");

        // Give user time to begin Sierra as active process
        try {
            Thread.sleep(15000);   // 1000 milliseconds is one second.
        } catch(InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        
        // Make a robot object
        Robot robot = new Robot();

        // Direct program to Sierra's "Search / Holds" screen
        robot.keyPress(KeyEvent.VK_F3);
        robot.keyRelease(KeyEvent.VK_F3);
        robot.delay(5000);
        
        // Select the barcode search choice from Sierra's drop-down menu in "Search / Holds"
        robot.keyPress(KeyEvent.VK_B);
        robot.keyRelease(KeyEvent.VK_B);
        robot.delay(500);
        
        // Enter text into window
        for(int i = 0; i < text.length; i++) {
            
            // Copy each barcode into Sierra using Windows clipboard
            StringSelection stringSelection = new StringSelection(text[i]);
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(stringSelection, stringSelection);
            robot.keyPress(KeyEvent.VK_CONTROL);
            robot.keyPress(KeyEvent.VK_V);
            robot.keyRelease(KeyEvent.VK_V);
            robot.keyRelease(KeyEvent.VK_CONTROL);
            robot.keyPress(KeyEvent.VK_ENTER);
            robot.keyRelease(KeyEvent.VK_ENTER);

            // Adjust robot delay according to item type
            if (type == "books") {
                robot.delay(BOOK_DELAY);
            }
            else {        // type == "magazines"
                robot.delay(MAGAZINE_DELAY);
            }
            
            // Execute Sierra Macro at CTRL+F12
            robot.keyPress(KeyEvent.VK_CONTROL);
            robot.keyPress(KeyEvent.VK_F12);
            robot.keyRelease(KeyEvent.VK_F12);
            robot.keyRelease(KeyEvent.VK_CONTROL);

            // Adjust robot delay according to item type
            if (type == "books") {
                robot.delay(BOOK_DELAY - 1000);
            }
            else {        // type == "magazines"
                robot.delay(MAGAZINE_DELAY - 1000);
            }
        }
        
        // Display end message:
        JOptionPane.showMessageDialog(null, "Processing complete!\n" + 
            "The elapsed time for this batch process was " + ((double)(System.nanoTime() - startTime)) / 1000000000 + " seconds.");
    }
    
    private static String[] readTextFile(String filename) {
        String input = "";
        try(BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filename), ENCODING))) {
            String line;
            while((line = br.readLine()) != null) {
                input += line + " ";
            }
        }
	catch(IOException ex) {
		System.out.println (ex.toString());
	}
        return input.split(" ");
    }
}
