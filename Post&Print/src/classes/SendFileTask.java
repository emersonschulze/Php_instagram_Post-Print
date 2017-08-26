/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package classes;


import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.microedition.io.Connection;
import javax.microedition.io.Connector;
import javax.obex.ClientSession;
import javax.obex.HeaderSet;
import javax.obex.Operation;

public class SendFileTask implements Runnable {
 
    private String btConnectionURL;
    private byte[] file;
    private String filename;
    private MyDefaultTableModel table;
    private int row;
    
    public boolean CONNECTED = true;
    public OutputStream outputStream = null;
    public Connection connection = null;
    public ClientSession cs = null;
    public Operation putOperation = null;
    
    public boolean TIMEOUT = true;
    
    public SendFileTask(String url, byte[] file, String filename, MyDefaultTableModel myTable, int row) {
        this.btConnectionURL = url;
        this.file = file;
        this.filename = filename;
        this.table = myTable;
        this.row = row;
    }
 
    public void stopTask(){
        try {
            this.finalize();
        } catch (Throwable ex) {
            Logger.getLogger(SendFileTask.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void run() {
        
        try {
            connection = Connector.open( btConnectionURL,Connector.READ_WRITE,true);
            // connection obtained
        }catch(Exception e){
            table.setValueAt("ERRO", row, 2);
            ErrorProgram.addOnFile(e.getMessage()+ " [FileTask] - Erro ao tentar conexão com a impressora, linha: "+row);

            System.out.println("SendFileTask, Erro ao tentar conectar - Row: "+row);
            //JOptionPane.showMessageDialog(null,"Não conseguiu Conectar"+e.getMessage());
            connection = null;
            CONNECTED = false;
            TIMEOUT = false;
            return;
        }
        try{
            TIMEOUT = false;
            CONNECTED = true;
            // now, let’s create a session
            // and a headerset objects
            cs = (ClientSession) connection;
            
            HeaderSet hs = cs.createHeaderSet();
 
            // now let’s send the connect header
            cs.connect(hs);
            
            hs.setHeader(HeaderSet.NAME, filename);
            // content-type could be
            // checked from a filename
            hs.setHeader(HeaderSet.TYPE, "image/jpeg");
            hs.setHeader(
                HeaderSet.LENGTH,
                new Long(file.length));
 
            putOperation = cs.put(hs);
 
            outputStream = putOperation.openOutputStream();
            
            outputStream.write(file);
            outputStream.close();
            putOperation.close();
            cs.disconnect(null);
            connection.close();
            
            // file push complete
            //System.out.println("Response Code: "+putOperation.getResponseCode()+ " - Length: "+ Long.toString(putOperation.getLength()));//160 = sem papel
            
        } catch (Exception e){
            System.out.println("SendFileTask Error: "+e.getMessage()+" - ROW: "+row);
            TIMEOUT = true;
            //table.setValueAt("ERRO", row, 2);
            //JOptionPane.showMessageDialog(null,"Erro Impressora em uso. "+e.getMessage());
            try {
                outputStream.close();
                //putOperation.close();
                //cs.disconnect(null);
                //connection.close();
            } catch (IOException ex){
                Logger.getLogger(SendFileTask.class.getName()).log(Level.SEVERE, null, ex);
               // JOptionPane.showMessageDialog(null,"Erro dentro do FileTask. "+ex.getMessage());
            }
        }
    }
}