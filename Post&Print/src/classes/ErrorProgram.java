/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package classes;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author Renato
 */
public class ErrorProgram {
    
    public ErrorProgram(){
        
    }
    
    public static void addOnFile(String msg){
        
        File dir = new File("log/");
        if(dir.exists() == false){
            dir.mkdir();
        }
        
        Date dt = new Date();
        SimpleDateFormat formatador = new SimpleDateFormat("yyyy_MM_dd");  
        // cria a string  
        String novoFormato = formatador.format(dt);  
        String path = "log/error_"+novoFormato+".txt";
        
        SimpleDateFormat formatador2 = new SimpleDateFormat("HH:mm:ss");  
        // cria a string  
        String hora = formatador2.format(dt);  
        
        File arquivo = new File(path);
        try{ 
            FileWriter fw = new FileWriter(arquivo, true);
            fw.write(hora+" "+msg+"\r\n");
            fw.flush();
        }catch(IOException ex){
          ex.printStackTrace();
        }
    }
    
}
