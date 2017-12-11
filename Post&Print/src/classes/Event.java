package classes;

import javax.swing.JOptionPane;
import org.json.JSONArray;
import org.json.JSONObject;

public class Event {
    private String id_event;
    private String name;
    private String dt_event;
    private String hashtag;
    private String automatic;
    private String have_screen;
    private String have_print;
    private String logo_event;
    private String id_print_template;
    private String active;
    private String qtde_fotos;
    private final Webservice ws;
    
    public Event() {
        ws = new Webservice();
    }
        
    public String toParams() {
        String params = "name=" + this.getName() + "&dt_event=" + this.getDt_event() + "&hashtag=" + this.getHashtag() + "&automatic=" + this.getAutomatic() + "&have_screen=" + this.getHave_screen() + "&have_print=" + this.getHave_print() + "&id_print_template=" + this.getId_print_template() + "&logo_event=" + this.getLogo_event() + "&active=" + this.getActive() + "&qtde_fotos=" + this.getQtde_fotos();
        if (this.getId_event() != null) {
            params = "id_event=" + this.getId_event() + "&" + params;
        }
        return params;
    }

    public JSONArray listTemplate() {
        JSONArray array = ws.getListTemplate();
        return array;
    }

    public String insert() {
        try {
            if (ws.insertEvent(this)) {
                return "OK";
            } else {
                return ws.msgError;
            }
        } catch (Exception e) {
             JOptionPane.showMessageDialog(null, "Erro ao inserir o evento! "+ e.getMessage());
        }
        return null;
    }
    
    public String remove() {
        try {
            if (ws.removeEvent(this)) {
                return "OK";
            } else {
                return ws.msgError;
            }
        } catch (Exception e) {
             JOptionPane.showMessageDialog(null, "Erro ao remover o evento! "+ e.getMessage());
        }
        return null;
    }

    public String update() {
        try {
            if (ws.updateEvent(this)) {
                return "OK";
            } else {
                return ws.msgError;
            }
        } catch (Exception e) {
             JOptionPane.showMessageDialog(null, "Erro ao atualizar o evento! "+ e.getMessage());
        }
        return null;
    }

    public String[][] listEvents() {
        return ws.getListEventos();
    }
    
      public String[][] listUsers() {
        return ws.getCarregaUser();
    }
      
     public JSONArray listaUsuario() {
        JSONArray array = ws.getListUser();
        return array;
    }

    public boolean loadEvent(String id_event) {
        JSONObject obj = ws.getEvento(id_event);
        if (obj != null) {
            this.setId_event(id_event);
            this.setName(obj.getString("name"));
            this.setDt_event(obj.getString("dt_event"));
            this.setHashtag(obj.getString("hashtag"));
            this.setAutomatic(obj.getString("automatic"));
            this.setHave_print(obj.getString("have_print"));
            this.setHave_screen(obj.getString("have_screen"));
            this.setId_print_template(obj.getString("id_print_template"));
            this.setLogo_event(obj.getString("logo_event"));
            this.setDt_event(obj.getString("dt_event"));
            this.setActive(obj.getString("active"));
            this.setQtde_fotos(obj.getString("qtde_fotos"));
            return true;
        }
        return false;
    }

    public String getId_event() {
        return id_event;
    }

    public void setId_event(String id_event) {
        this.id_event = id_event;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the dt_event
     */
    public String getDt_event() {
        return dt_event;
    }

    /**
     * @param dt_event the dt_event to set
     */
    public void setDt_event(String dt_event) {
        this.dt_event = dt_event;
    }

    /**
     * @return the hashtag
     */
    public String getHashtag() {
        return hashtag;
    }

    /**
     * @param hashtag the hashtag to set
     */
    public void setHashtag(String hashtag) {
        this.hashtag = hashtag;
    }

    /**
     * @return the automatic
     */
    public String getAutomatic() {
        return automatic;
    }

    /**
     * @param automatic the automatic to set
     */
    public void setAutomatic(String automatic) {
        this.automatic = automatic;
    }

    /**
     * @return the have_screen
     */
    public String getHave_screen() {
        return have_screen;
    }

    /**
     * @param have_screen the have_screen to set
     */
    public void setHave_screen(String have_screen) {
        this.have_screen = have_screen;
    }

    /**
     * @return the have_print
     */
    public String getHave_print() {
        return have_print;
    }

    /**
     * @param have_print the have_print to set
     */
    public void setHave_print(String have_print) {
        this.have_print = have_print;
    }

    /**
     * @return the id_print_template
     */
    public String getId_print_template() {
        return id_print_template;
    }

    /**
     * @param id_print_template the id_print_template to set
     */
    public void setId_print_template(String id_print_template) {
        this.id_print_template = id_print_template;
    }

    /**
     * @return the active
     */
    public String getActive() {
        return active;
    }

    /**
     * @param active the active to set
     */
    public void setActive(String active) {
        this.active = active;
    }

    /**
     * @return the logo_event
     */
    public String getLogo_event() {
        return logo_event;
    }

    /**
     * @param logo_event the logo_event to set
     */
    public void setLogo_event(String logo_event) {
        this.logo_event = logo_event;
    }

    /**
     * @return the qtde_fotos
     */
    public String getQtde_fotos() {
        return qtde_fotos;
    }

    /**
     * @param qtde_fotos the qtde_fotos to set
     */
    public void setQtde_fotos(String qtde_fotos) {
        this.qtde_fotos = qtde_fotos;
    }

}
