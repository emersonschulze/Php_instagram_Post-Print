/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package classes;

import com.intel.bluetooth.RemoteDeviceHelper;
import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.bluetooth.DeviceClass;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.DiscoveryListener;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.ServiceRecord;
import javax.bluetooth.UUID;
import javax.microedition.io.Connection;
import javax.microedition.io.Connector;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author Renato
 */
public class PrinterPolaroid  implements DiscoveryListener{
    
    private static Logger LOG = Logger.getLogger(PrinterPolaroid.class.getName());
    private static final UUID[] UUIDS = new UUID[] {new UUID(0x1105), new UUID(0x1106), new UUID(0x0008)};//private static final UUID[] UUIDS = new UUID[] {new UUID(0x0003), new UUID(0x1101)};
    private Map<Integer, RemoteDevice> searchForServices = new HashMap<Integer, RemoteDevice>();
    private Collection<ServiceRecord> servicesDiscovered = new HashSet<ServiceRecord>();
    private CountDownLatch waitForDevices;
    
    public String NAME = "";//Nome da Impressora
    public String PIN = "6000";//Senha do Bluetooth
    public String BLUETOOTH_ADDRESS = "";//Endereço Bluetooth
    public boolean PRINTING = false;//Está imprimindo
    public boolean CONNECTED = false;//Está conectada
    public boolean PRINTER_RECEIVE = false;
    
    public int ROW_CURRENT = -1;
    public int ROW_FOTO_ANTERIOR = -1;
    public int QTDE_PAPEL = 10;
    public boolean ACABOU_PAPEL = false;
            
    public int TIME_USE = 0;
    public SendFileTask task;
    
    private int TIME_PRINT = 50000;//Tempo de impressão 50 segundos
    
    public PrinterPolaroid(String namePrinter){
        NAME = namePrinter;
    }
    
    public boolean isPrinting(){
        return PRINTING;
    }
    public  boolean isConnected(){
        return CONNECTED;
    }
    public  boolean connect(RemoteDevice device){
        try{
            if(PRINTING == false){
                BLUETOOTH_ADDRESS = device.getBluetoothAddress();
                CONNECTED = true;
                LOG.info("Printer Connected ");
                synchronized(this) {
                    this.notify();
                }
                return true;
            }
        }catch(Exception e){
            BLUETOOTH_ADDRESS = "";
            CONNECTED = false;
            System.out.println(e.getMessage());
        }
        
        return false;
    }
    
    public boolean connectionIsOK(){
        Connection connection = null;
        try {
            String url = "btgoep://" + BLUETOOTH_ADDRESS + ":1";
            connection = Connector.open( url,Connector.READ_WRITE,true);
            connection.close();
            return true;
            // connection obtained
        }catch(Exception e){
            System.out.println("Conector is OK = FALSE");
            //JOptionPane.showMessageDialog(null,"Não conseguiu Conectar"+e.getMessage());
            connection = null;
        }
        
        return false;
    }
    
    public  boolean printImage(String fileName,MyDefaultTableModel table, int row){
        
        FileInputStream stream = null;
        try{
            if(isConnected()){
                PRINTING = true;
                PRINTER_RECEIVE = false;
                this.task = null;
                this.ROW_CURRENT = row;
                // Build URL for the bluetooth device, note the port 1
                String url = "btgoep://" + BLUETOOTH_ADDRESS + ":1";

                // Get file as bytes
                stream = new FileInputStream(fileName);
                File f = new File(fileName);
                int size = (int) f.length();
                byte[] file = new byte[size];
                stream.read(file);
                
                // Filename
                String filename = f.getName();

                // Trigger the task in a different thread
                // so it won’t block the UI
                try{
                    table.setValueAt("IMPRIMINDO", row, 2);
                    System.out.println("Mudou status para IMPRIMINDO - Linha "+row+" -- "+table.getValueAt(row, 1).toString());
              
                    TIME_USE = 0;
                    this.task = new SendFileTask(url, file, filename,table,row);
                    //Thread thread = new Thread(task);
                    task.run();
                    
                    PRINTER_RECEIVE = true;
                    
                    System.out.println("Mandou imprimir: "+url+"  --- IMAGE: "+fileName);
                    
                    if(task.TIMEOUT == false){
                        ACABOU_PAPEL = false;
                        Thread.sleep(TIME_PRINT);
                        System.out.println("Impressão concluida("+BLUETOOTH_ADDRESS+") -- IMAGE:"+fileName);
                        TIME_USE = 0;
                        //if(table.getValueAt(row, 2).toString().equals("IMPRIMINDO")){
                            table.setValueAt("IMPRESSA", row, 2);
                            PRINTING = false;
                            this.ROW_FOTO_ANTERIOR = row;
                            PRINTER_RECEIVE = false;
                            InputStream in = null;
                            OutputStream out = null;
                            try {
                                File newFile = new File(fileName.replace("FotosFila", "FotosImpressas"));
                                in = new FileInputStream(f);
                                out = new FileOutputStream(newFile);
                                byte[] moveBuff = new byte[1024];
                                int butesRead;
                                while ((butesRead = in.read(moveBuff)) > 0) {
                                    out.write(moveBuff, 0, butesRead);
                                }
                                in.close();
                                out.close();

                                stream.close();
                                f.setWritable(true, true);
                                f.delete();
                            }catch(Exception e){
                                ErrorProgram.addOnFile(e.getMessage()+ " [printImage:1]");
                                JOptionPane.showMessageDialog(null,"ERRO ao Mover o Arquivo. "+e.getMessage());
                                System.out.println(e.getMessage());
                                in.close();
                                out.close();
                                stream.close();
                            }
                        //}
                    
                    }
                    
                    PRINTER_RECEIVE = false;
                    
                } catch (InterruptedException ex) {
                    Logger.getLogger(PrinterPolaroid.class.getName()).log(Level.SEVERE, null, ex);
                    table.setValueAt("ERRO", row, 2);
                    System.out.println("Erro: "+ex.getMessage());
                    ErrorProgram.addOnFile(ex.getMessage()+ " [printImage:2]");
                    JOptionPane.showMessageDialog(null,"ERRO 2. "+ex.getMessage());
                    try {
                        if(stream != null){
                            stream.close();
                        }
                    } catch (IOException ex1) {
                        Logger.getLogger(PrinterPolaroid.class.getName()).log(Level.SEVERE, null, ex1);
                    }
                }catch(Exception e){
                    System.out.println(e.getMessage());
                    //JOptionPane.showMessageDialog(null,"Verifique se a luz de alerta da impressora está piscando, pare o processo, o papel pode ter acabado! "+e.getMessage());
                    table.setValueAt("ERRO", row, 2);
                    ErrorProgram.addOnFile(e.getMessage()+ " [printImage:3]");
                }
                //table.setRowColour(row, Color.GREEN);
                PRINTER_RECEIVE = false;
                return true;
            }else{
                table.setValueAt("ERRO", row, 2);
                JOptionPane.showMessageDialog(null,"ERRO 3. Não Conectado");
                System.out.println("Nao conectado");
                ErrorProgram.addOnFile("Não Conectado");
            }
        }catch(IOException e){
            table.setValueAt("ERRO", row, 2);
            JOptionPane.showMessageDialog(null,"ERRO 4. "+e.getMessage());
            System.out.println("Erro: "+e.getMessage());
            ErrorProgram.addOnFile(e.getMessage()+ " [printImage:4]");

            try {
                if(stream != null){
                    stream.close();
                }
            } catch (IOException ex1) {
                Logger.getLogger(PrinterPolaroid.class.getName()).log(Level.SEVERE, null, ex1);
            }
        }
        PRINTER_RECEIVE = false;
        return false;
    }

    @Override
    public void deviceDiscovered(RemoteDevice rd, DeviceClass dc) {
        try {
            String name = rd.getFriendlyName(false);
            boolean isMine = NAME.equals(name);
            LOG.info("Discovered: " + name + "(" + rd.getBluetoothAddress() + ")" + (isMine ? "" : " - ignoring"));
            if (!isMine)
                return;
            if (!rd.isAuthenticated()) {
                boolean paired = RemoteDeviceHelper.authenticate(rd, PIN);
                LOG.info("Pair with " + name + (paired ? " succesfull" : " failed"));
                if(paired){
                    connect(rd);
                }else{
                    LOG.info("PIN: " + PIN);
                }
            }else{
                
            }
            
        } catch (IOException ex) {
            Logger.getLogger(PrinterPolaroid.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void inquiryCompleted(int respCode) {
        /*synchronized(this) {
            this.notify();
        }*/
        switch (respCode) {
            case DiscoveryListener.INQUIRY_COMPLETED :
                LOG.fine("INQUIRY_COMPLETED");
                break;
            case DiscoveryListener.INQUIRY_TERMINATED :
                LOG.fine("INQUIRY_TERMINATED");
                break;
            case DiscoveryListener.INQUIRY_ERROR :
                LOG.fine("INQUIRY_ERROR");
                break;
            default :
                LOG.fine("Unknown Response Code - " + respCode);
                break;
        }
    }

    public void serviceSearchCompleted(int transID, int respCode) {
        String rd = searchForServices.get(transID).getBluetoothAddress();
        //searchForServices.remove(transID);
        switch (respCode) {
                case DiscoveryListener.SERVICE_SEARCH_COMPLETED:
                        LOG.fine(rd + ": The service search completed normally");
                break;
                case DiscoveryListener.SERVICE_SEARCH_TERMINATED:
                        LOG.fine(rd + ": The service search request was cancelled by a call to DiscoveryAgent.cancelServiceSearch(int)");
                break;
                case DiscoveryListener.SERVICE_SEARCH_ERROR:
                        LOG.warning(rd + ": An error occurred while processing the request");
                break;
                case DiscoveryListener.SERVICE_SEARCH_NO_RECORDS:
                        LOG.info(rd + ": No records were found during the service search");
                break;
                case DiscoveryListener.SERVICE_SEARCH_DEVICE_NOT_REACHABLE:
                        LOG.warning(rd + ": The device specified in the search request could not be reached or the local device could not establish a connection to the remote device");
                break;
            default:
                LOG.warning(rd + ": Unknown Response Code - " + respCode);
                break;
        }
        if (waitForDevices != null)
            waitForDevices.countDown();
    }

    public void servicesDiscovered(int transID, ServiceRecord[] srs) {
        LOG.info("Services discovered in transaction " + transID + " : " + srs.length);
        for (ServiceRecord sr : srs) {
            LOG.info(sr.getConnectionURL(ServiceRecord.NOAUTHENTICATE_NOENCRYPT, false));
            servicesDiscovered.add(sr);
        }
    }
    
}
