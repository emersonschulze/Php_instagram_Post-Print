/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package classes;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintException;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.SimpleDoc;
import javax.swing.JOptionPane;

/**
 *
 * @author emerson
 */
public class Impressora {
    
   private static PrintService impressora;
   private List<String> todasImpressoras;
   
  public List<String> detectaImpressoras() {

        try {
            todasImpressoras = new ArrayList<>();

            DocFlavor df = DocFlavor.SERVICE_FORMATTED.PRINTABLE;
            PrintService[] ps = PrintServiceLookup.lookupPrintServices(df, null);
            for (PrintService p : ps) {

                System.out.println("Impressora encontrada: " + p.getName());
                todasImpressoras.add(p.getName());

                if (p.getName().contains("Text") || p.getName().contains("Generic")) {

                    System.out.println("Impressora Selecionada: " + p.getName());
                    impressora = p;
                    return todasImpressoras;
                }

            }

        } catch (Exception e) {
            System.out.println("Problemas ao selecionar a impressora, error: " + e.toString() + "");

        }
        return todasImpressoras;
    }
  
  
    public void selecionaImpressoras(String impressoraSelecionada) {  //Passa por parâmetro o nome salvo da impressora
        try {  
            DocFlavor df = DocFlavor.SERVICE_FORMATTED.PRINTABLE;  
            PrintService[] ps = PrintServiceLookup.lookupPrintServices(df, null);  
            for (PrintService p : ps) {  
                if(p.getName()!=null && p.getName().contains(impressoraSelecionada)){  
                    impressora = p;  
                }     
            }  
        } catch (Exception e) {  
           
        }  
    }
    
    public  boolean imprime(String texto) {  
        if (impressora == null) {  
            JOptionPane.showMessageDialog(null, "Nenhuma impressora foi encontrada. Instale uma impressora padrão \r\n e reinicie o programa."); 
        } else {  
            try {  
                DocPrintJob dpj = impressora.createPrintJob();  
                InputStream stream = new ByteArrayInputStream((texto + "\n").getBytes());  
                DocFlavor flavor = DocFlavor.INPUT_STREAM.AUTOSENSE;  
                Doc doc = new SimpleDoc(stream, flavor, null);  
                dpj.print(doc, null);  
                return true;  
            } catch (PrintException e) {  
              
            }  
        }  
        return false;  
    }
}
