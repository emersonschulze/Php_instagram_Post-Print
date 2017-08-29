package classes;

/**
 *
 * @author Emerson Schulze
 */
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

// Essa classe é a responsavel pela impressao. Ela detecta a impressora  
// instalada, recebe o texto e o imprime.  
public final class Impressao {

    // variavel estatica porque será utilizada por inumeras threads  
    private static PrintService impressora;
    private List<String> todasImpressoras;

    public Impressao() {

        detectaImpressoras();

    }

    // O metodo verifica se existe impressora conectada e a  
    // define como padrao.  
    public List<String> detectaImpressoras() {

        try {
            todasImpressoras = new ArrayList<String>();

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

    public synchronized boolean imprime(String texto) {

        // se nao existir impressora, entao avisa usuario  
        // senao imprime texto  
        if (impressora == null) {

            String msg = "Nennhuma impressora foi encontrada. Instale uma impressora padrão \r\n(Generic Text Only) e reinicie o programa.";
            System.out.println(msg);

        } else {

            try {

                DocPrintJob dpj = impressora.createPrintJob();
                InputStream stream = new ByteArrayInputStream(texto.getBytes());

                DocFlavor flavor = DocFlavor.INPUT_STREAM.AUTOSENSE;
                Doc doc = new SimpleDoc(stream, flavor, null);
                dpj.print(doc, null);

                return true;

            } catch (PrintException e) {

                System.out.println("impressão falhou, error: " + e.toString() + "");

            }

        }

        return false;

    }

}
