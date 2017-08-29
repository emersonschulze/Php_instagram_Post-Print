package classes;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.Printable;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;

public class PrintObject implements Printable {

    public File arquivo;

    public PrintObject(File f) {
        this.arquivo = f;
    }

    @Override
    public int print(Graphics g, PageFormat f, int pageIndex) {
        Graphics2D g2 = (Graphics2D) g;  // Allow use of Java 2 graphics on  
        // the print pages :  
        if (pageIndex == 0) {
            Paper p = new Paper();
            p.setSize(5.48, 8.6);
            f.setPaper(p);
            try {
                if (arquivo != null) {
                    ImageIcon printImage = new javax.swing.ImageIcon(arquivo.getAbsoluteFile().toString());
                    System.out.println(arquivo.getAbsoluteFile().toString());
                    double pageWidth = f.getImageableWidth();
                    double pageHeight = f.getImageableHeight();
                    double imageWidth = printImage.getIconWidth();
                    double imageHeight = printImage.getIconHeight();
                    double scaleX = pageWidth / imageWidth;
                    double scaleY = pageHeight / imageHeight;
                    double scaleFactor = Math.min(scaleX, scaleY);
                    g2.scale(scaleFactor, scaleFactor);
                    g.drawImage(printImage.getImage(), 0, 0, null);
                }
            } catch (Exception ex) {
                Logger.getLogger(PrintObject.class.getName()).log(Level.SEVERE, null, ex);
            }
            return PAGE_EXISTS;
        } else {
            return NO_SUCH_PAGE;
        }
    }
}
