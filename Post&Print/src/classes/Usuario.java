package classes;

import javax.swing.JOptionPane;
import org.json.JSONArray;

public class Usuario {

    

    private String id_intagram;
    private String username;
    private String imagem_perfil;
    private String insta_token;
    private String id_suaID;
    private final Webservice ws;
    
    public Usuario() {
       ws = new Webservice();
    }
    
   public String insert() {
        try {
            if (ws.insertUser()) {
                return "OK";
            } else {
                return ws.msgError;
            }
        } catch (Exception e) {
             JOptionPane.showMessageDialog(null, "Erro ao inserir o evento! "+ e.getMessage());
        }
        return null;
    }
   
   public JSONArray listUsers() {
        JSONArray array = ws.getListUser();
        return array;
    }
   
    public String[][] listUsuario() {
        return ws.getCarregaUser();
    }
    
    public String getId_intagram() {
        return id_intagram;
    }

    public void setId_intagram(String id_intagram) {
        this.id_intagram = id_intagram;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getImagem_perfil() {
        return imagem_perfil;
    }

    public void setImagem_perfil(String imagem_perfil) {
        this.imagem_perfil = imagem_perfil;
    }

    public String getInsta_token() {
        return insta_token;
    }

    public void setInsta_token(String insta_token) {
        this.insta_token = insta_token;
    }

    public String getId_suaID() {
        return id_suaID;
    }

    public void setId_suaID(String id_suaID) {
        this.id_suaID = id_suaID;
    }
   
}