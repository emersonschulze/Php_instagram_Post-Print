/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package classes;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintException;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.SimpleDoc;

/**
 *
 * @author cardial
 */
public class Instagram {
    
    public static ArrayList<Photo> fotosInstagram;
    
    public Instagram(){
       
    }
    
    public static boolean importPhotosInstagram(String tag, String outputDirectory){
        try {  
          
            outputDirectory = outputDirectory+tag+"/";
            
            File dirEnviadas = new File(outputDirectory);
            if(dirEnviadas.exists() == false){
                dirEnviadas.mkdir();
            }
            
            // 1MB de buffer  
            final int BUFFER_SIZE = 1024 * 1024;  
            
            Webservice ws = new Webservice();
            ArrayList<Photo> result =  ws.getListPhotos(tag);
            String[][] images = new String[result.size()][3];
            for(int j = 0; j < result.size(); j++){
                images[j][0] = result.get(j).id;
                images[j][1] = result.get(j).url;
                images[j][2] = result.get(j).nome_usuario;
            }
            
            for(int i = 0; i < images.length; i++){
                
                String nomeArquivo = outputDirectory+images[i][2]+"-"+images[i][0]+"_baixando.jpg";  
                File fExist = new File(nomeArquivo);
                File fExist2 = new File(outputDirectory+images[i][2]+"-"+images[i][0]+".jpg");
                
                if(fExist2.exists() == false && fExist.exists() == false){//Se não existe na pasta, então Baixa
                
                    try{
                        URL url = new URL(images[i][1]);
                        BufferedInputStream stream = new BufferedInputStream(url.openStream(), BUFFER_SIZE);  
                        BufferedOutputStream fos = new BufferedOutputStream(new FileOutputStream(nomeArquivo));  
                        byte buf[] = new byte[BUFFER_SIZE];
                        int numBytesRead;  
                        do{  
                            numBytesRead = stream.read(buf);  
                            if (numBytesRead > 0) {  
                                fos.write(buf, 0, numBytesRead);  
                            }  
                        } while (numBytesRead > 0);  
                        fos.flush();  
                        fos.close();  
                        stream.close();  
                        buf = null;

                        fExist.renameTo(new File(fExist.getPath().replace("_baixando.jpg", ".jpg")));
                    }catch(FileNotFoundException exf){
                        System.out.println("importPhotos: "+exf.getMessage());
                    }
                    
                }
            }
            return true;
        } catch (MalformedURLException e1) {  
            // TODO Auto-generated catch block  
            e1.printStackTrace();  
        } catch (Exception e) {  
            // TODO Auto-generated catch block  
            e.printStackTrace();  
        }  
        return false;
    }
    
    public void printImagesInDirectory(String pathDirectoryImages){
        PrintService service = PrintServiceLookup.lookupDefaultPrintService();
        DocPrintJob job = service.createPrintJob();
        DocFlavor flavor = DocFlavor.SERVICE_FORMATTED.PRINTABLE;
        try {
            File dir = new File(pathDirectoryImages);
            if(dir.isDirectory()){
                File arquivos[] = dir.listFiles();
                for(int i = 0; i < arquivos.length;i++){
                    if(arquivos[i].getName().indexOf(".jpg") > 0 || arquivos[i].getName().indexOf(".JPG") > 0){
                        SimpleDoc doc = new SimpleDoc(new PrintObject(arquivos[i]), flavor, null);
                        job.print(doc, null);
                    }
                }
            }
        } catch (PrintException ex) {
            Logger.getLogger(Instagram.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}