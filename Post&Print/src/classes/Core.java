/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package classes;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import org.json.JSONObject;

/**
 *
 * @author cardial
 */
public class Core {
    
    public String login;
    public String site_login;
    public JSONObject infoUser;
    
    public boolean existCertificate(){
        if(new File("certificate.dat").exists()){
            return true;
        }
        return false;
    } 
    
    public boolean isValidCertificate(){
        try {
            ObjectInputStream object = new ObjectInputStream(new FileInputStream(new File("certificate.dat")));
            Certificate cert = (Certificate)object.readObject();
            object.close();
            
            String user_home = this.cripto(System.getProperty("user.home"));
            String user_name = this.cripto(System.getProperty("user.name"));
            String os_name = this.cripto(System.getProperty("os.name"));
            String os_version = this.cripto(System.getProperty("os.version"));
            this.login = PWSec.decrypt(cert.login);
            if(cert.user_home.equals(user_home) && cert.user_name.equals(user_name) && cert.os_version.equals(os_version)){
                String cert_login = PWSec.decrypt(cert.login);
                if(this.validEmail(cert_login)){
                    String params = "login="+cert_login+"&name_machine="+System.getProperty("user.name")+"&os_machine="+System.getProperty("os.name");
                    Webservice ws = new Webservice();
                    String response = ws.request(ws.urlWSEvent.replace("[method]", "validLogin") , params,"POST");
                    if(!response.equals("")){
                        JSONObject json = new JSONObject(response);
                        if(json.get("code").toString().equals("1")){
                            this.login = cert_login;
                            return true;
                        }
                    }
                }
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Core.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Core.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(Core.class.getName()).log(Level.SEVERE, null, ex);
        } catch (BadPaddingException ex) {
            Logger.getLogger(Core.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchPaddingException ex) {
            Logger.getLogger(Core.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalBlockSizeException ex) {
            Logger.getLogger(Core.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvalidKeyException ex) {
            Logger.getLogger(Core.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(Core.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvalidAlgorithmParameterException ex) {
            Logger.getLogger(Core.class.getName()).log(Level.SEVERE, null, ex);
        } catch(Exception ex){
            Logger.getLogger(Core.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }
    
    public boolean generateSerial(String login, String password, String user_machine){
        String params = "login="+login+"&password="+password+"&name_machine="+user_machine;
        Webservice ws = new Webservice();
        String response = ws.request(ws.urlWSEvent.replace("[method]", "generateSerial") , params,"POST");
        if(!response.equals("")){
            JSONObject json = new JSONObject(response);
            if(json.get("code").toString().equals("1")){
                this.login = login;
                this.site_login = json.get("site").toString();
                return true;
            }
        }
        return false;
    }
    
    public boolean authenticate(String login, String password){
        String params = "login="+login+"&password="+password;
        Webservice ws = new Webservice();
        String response = ws.request(ws.urlWSEvent.replace("[method]", "login") , params,"POST");
        if(!response.equals("")){
            JSONObject json = new JSONObject(response);
            if(json.get("code").toString().equals("1")){
                this.login = login;
                this.infoUser = json.getJSONObject("data");
                return true;
            }
        }
        return false;
    }
    
    public boolean assignCertificate(){
        String params = "login="+this.login+"&name_machine="+System.getProperty("user.name")+"&os_machine="+System.getProperty("os.name");
        Webservice ws = new Webservice();
        String response = ws.request(ws.urlWSEvent.replace("[method]", "assignCertificate") , params,"POST");
        if(!response.equals("")){
            JSONObject json = new JSONObject(response);
            if(json.get("code").toString().equals("1")){
                return true;
            }
        }
        return false;
    }
    
    public boolean createCertificate(String login,String password){
        try {
            if(this.authenticate(login, password)){
                if(this.generateSerial(login, password,System.getProperty("user.name"))){
                    Certificate cert = new Certificate();
                    cert.login = PWSec.encrypt(login);
                    cert.os_name = this.cripto(System.getProperty("os.name"));
                    cert.os_version = this.cripto(System.getProperty("os.version"));
                    cert.user_name = this.cripto(System.getProperty("user.name"));
                    cert.user_home = this.cripto(System.getProperty("user.home"));
                    ObjectOutputStream output = new ObjectOutputStream(new FileOutputStream(new File("certificate.dat")));
                    output.writeObject(cert);
                    output.close();
                    return true;
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(Core.class.getName()).log(Level.SEVERE, null, ex);
        } catch (BadPaddingException ex) {
            Logger.getLogger(Core.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchPaddingException ex) {
            Logger.getLogger(Core.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalBlockSizeException ex) {
            Logger.getLogger(Core.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvalidKeyException ex) {
            Logger.getLogger(Core.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(Core.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvalidAlgorithmParameterException ex) {
            Logger.getLogger(Core.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return false;
    }
    
    private String cripto(String original){
        
        MessageDigest algorithm;
        try {
            algorithm = MessageDigest.getInstance("SHA-256");
            byte messageDigest[] = algorithm.digest(original.getBytes("UTF-8"));

            StringBuilder hexString = new StringBuilder();
            for (byte b : messageDigest) {
              hexString.append(String.format("%02X", 0xFF & b));
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(Core.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(Core.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    private boolean validEmail(String email){
        String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"+"[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
        Pattern pattern = Pattern.compile(EMAIL_PATTERN);
        Matcher matcher = pattern.matcher(email);
	return matcher.matches();
    }
    
}
