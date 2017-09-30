package classes;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintException;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.SimpleDoc;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.Copies;
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
           JOptionPane.showMessageDialog(null, "Problemas ao selecionar a impressora, error: " + e.toString() + "");
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
           JOptionPane.showMessageDialog(null, "Seleção de impressora com problemas"  +  e);
        }  
    }  
    
  public boolean imprime(String texto) {  
        if (impressora == null) {  
            JOptionPane.showMessageDialog(null, "Nenhuma impressora foi encontrada. Instale uma impressora padrão \r\n e reinicie o programa."); 
        } else {  
                try{
                  PrintRequestAttributeSet pras = new HashPrintRequestAttributeSet();
                  pras.add(new Copies(1));
                  final DocPrintJob jobb = impressora.createPrintJob();
               
                    try (FileInputStream fin = new FileInputStream(texto)) {
                        Doc doc = new SimpleDoc(fin, DocFlavor.INPUT_STREAM.JPEG, null);
                        
                        jobb.print(doc, pras);
                    }
            return true;  
    
           } catch (IOException | PrintException e) {  
               JOptionPane.showMessageDialog(null, "Algo de errado na impressão"  +  e);
           }
         }
       return false;
    }
    
}
   
