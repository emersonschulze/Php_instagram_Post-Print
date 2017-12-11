package classes;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import javax.swing.JOptionPane;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Webservice {

    private static final int TAM_MAX_BUFFER = 10240; // 10Kbytes
    private final String urlTagsRecent = "https://api.instagram.com/v1/tags/[myTag]/media/recent?access_token=";
    private final String urlTags = "https://api.instagram.com/v1/tags/[myTag]?access_token=";
    private final String urlPhoto = "https://api.instagram.com/v1/media/[IDPhoto]?access_token=";
    public String urlWSEvent = "https://postprint.com.br/WSPhotoParty/Event/[method]?format=json";
    public final String urlLogado  = "https://postprint.com.br/WSPhotoParty/Instagram/[method]?format=json";
    private HttpURLConnection urlConnection;
    public String msgError = "";

    public Webservice() {
      
    }

    private String getToken(){
        JSONArray listaUsuario = this.getListUser();
        JSONArray data = listaUsuario.getJSONObject(0).getJSONArray("result");
        JSONObject json = data.getJSONObject(0);
        return json.getString("insta_token"); 
    }
    
    public Photo getInfoPhoto(String idPhoto) {
        Photo photo = new Photo();
        String response = this.request(urlPhoto.replace("[IDPhoto]", idPhoto) + getToken() , "", "GET");
        if (!response.trim().equals("")) {
            try {

                JSONArray jsonArray = new JSONArray("[" + response + "]");
                JSONObject json = jsonArray.getJSONObject(0).getJSONObject("data");
                photo.id = (String) json.get("id");
                photo.url = (String) json.getJSONObject("images").getJSONObject("standard_resolution").get("url").toString();
                photo.nome_usuario = "@" + (String) json.getJSONObject("user").get("username").toString();
                photo.foto_usuario = (String) json.getJSONObject("user").get("profile_picture").toString();
                try {
                    photo.description = (String) json.getJSONObject("caption").get("text").toString();
                } catch (JSONException ex) {
                    JOptionPane.showMessageDialog(null, "Caracter especial no caption");
                }
            } catch (JSONException e) {
               JOptionPane.showMessageDialog(null, "Erro WS: " + e.getMessage() + " -- " + response);
            }
        } else {
            JOptionPane.showMessageDialog(null, "Não tem responsta : " + urlPhoto.replace("[IDPhoto]", idPhoto));
        }

        return photo;
    }

    public ArrayList<Photo> getListPhotos(String tag) {
        ArrayList<Photo> lista = new ArrayList<>();
        String response = this.request(urlTagsRecent.replace("[myTag]", tag)+  getToken(), "", "GET");
        if (!response.trim().equals("")) {
            try {
                JSONArray jsonArray = new JSONArray("[" + response + "]");
                JSONArray data = jsonArray.getJSONObject(0).getJSONArray("data");
                
                for (int i = 0; i < data.length(); i++) {
                    JSONObject json = data.getJSONObject(i);
                    Photo photo = new Photo();
                    photo.id = (String) json.get("id");
                    photo.url = (String) json.getJSONObject("images").getJSONObject("standard_resolution").get("url").toString();
                    photo.nome_usuario = "@" + (String) json.getJSONObject("user").get("username").toString();
                    photo.foto_usuario = (String) json.getJSONObject("user").get("profile_picture").toString();
                    try {
                        photo.description = (String) json.getJSONObject("caption").get("text").toString();
                    } catch (JSONException ex) {
                          JOptionPane.showMessageDialog(null,"Caracter especial no caption");
                    }
                    lista.add(photo);
                }
            } catch (JSONException e) {
                JOptionPane.showMessageDialog(null, "Erro WS: " + e.getMessage());
            }
        } else {
            JOptionPane.showMessageDialog(null, "Não tem responsta");
        }
        return lista;
    }

    public boolean existHashTag(String tag) {
        String response = this.request(urlTags.replace("[myTag]", tag) +  getToken(), "", "GET");
        if (!response.trim().equals("")) {
            try {
                JSONArray jsonArray = new JSONArray("[" + response + "]");
                Object media = jsonArray.optJSONObject(0).optJSONObject("data").get("media_count");
                if (!media.equals(0)) {

                    return true;
                }
            } catch (JSONException e) {
               JOptionPane.showMessageDialog(null, "Erro ao validar hashtag WS: " + e.getMessage());
            }
        } else {
            JOptionPane.showMessageDialog(null, "Não tem responsta");
        }
        return false;
    }

    public JSONArray getListTemplate() {
        String response = this.request(urlWSEvent.replace("[method]", "listTemplates"), "", "GET");
        if (!response.trim().equals("")) {
            try {
                JSONArray jsonArray = new JSONArray("[" + response + "]");
                if (jsonArray.length() > 0) {
                    if (jsonArray.getJSONObject(0).getString("code").equals("1")) {
                        return jsonArray;
                    }
                }
            } catch (JSONException e) {
                JOptionPane.showMessageDialog(null, "Erro WS: " + e.getMessage());
            }
        } else {
            JOptionPane.showMessageDialog(null, "Não tem resposta");
        }
        return null;
    }

    public JSONObject getConfigTemplate(String id_template) {

        String response = this.request(urlWSEvent.replace("[method]", "getConfigTemplate"), "id_template=" + id_template, "GET");
        if (!response.trim().equals("")) {
            try {
                JSONArray jsonArray = new JSONArray("[" + response + "]");
                JSONObject json = jsonArray.optJSONObject(0);
                return json;
            } catch (JSONException e) {
                JOptionPane.showMessageDialog(null, "Erro ao buscar templates WS: " + e.getMessage());
            }
        } else {
            JOptionPane.showMessageDialog(null, "Não tem resposta");
        }

        return null;
    }

    private String sn(String value) {
        if (value.equals("S")) {
            return "Sim";
        }
        return "Não";
    }
    
    public JSONObject getEvento(String id_event) {
        String params = "login=" + "contato@postprint.com.br" + "&name_machine=" + System.getProperty("user.name") + "&os_machine=" + System.getProperty("os.name");
        String response = this.request(urlWSEvent.replace("[method]", "getEvent"), "id_event=" + id_event + "&" + params, "POST");
        if (!response.trim().equals("")) {
            try {
                JSONArray jsonArray = new JSONArray("[" + response + "]");
                JSONArray data = jsonArray.getJSONObject(0).getJSONArray("result");
                JSONObject json = data.getJSONObject(0);
                return json;
            } catch (JSONException e) {
               JOptionPane.showMessageDialog(null, "Erro WS: " + e.getMessage());
            }
        } else {
            JOptionPane.showMessageDialog(null, "Não tem responsta");
        }
        return null;
    }
    
      public JSONArray getListUser() {
        String response = this.request(urlWSEvent.replace("[method]", "carregaUser"), "", "GET");
        if (!response.trim().equals("")) {
            try {
                JSONArray jsonArray = new JSONArray("[" + response + "]");
                if (jsonArray.length() > 0) {
                    if (jsonArray.getJSONObject(0).getString("code").equals("1")) {
                        return jsonArray;
                    }
                }
            } catch (JSONException e) {
                JOptionPane.showMessageDialog(null, "Erro WS: " + e.getMessage());
            }
        } else {
            JOptionPane.showMessageDialog(null, "Não tem resposta");
        }
        return null;
    }

    
    public String[][] getCarregaUser() {
        String response = this.request(urlWSEvent.replace("[method]", "carregaUser"), "", "GET");
        if (!response.trim().equals("")) {
            try {
                JSONArray jsonArray = new JSONArray("[" + response + "]");
                JSONArray data = jsonArray.getJSONObject(0).getJSONArray("result");
                String[][] lista = new String[data.length()][8];
                for (int i = 0; i < data.length(); i++) {
                    JSONObject json = data.getJSONObject(i);
                    lista[i][0] = json.getString("username");
                    lista[i][1] = json.getString("imagem_perfil");
                    lista[i][2] = json.getString("insta_token");
                }
                return lista;
            } catch (JSONException e) {
                JOptionPane.showMessageDialog(null, "Erro ao listar o usuário! WS: " + e.getMessage());
            }
        } else {
            JOptionPane.showMessageDialog(null, "Não tem responsta do servidor");
        }
        return null;
    }
     
   
    public String[][] getListEventos() {
        String params = "login=" + "contato@postprint.com.br" + "&name_machine=" + System.getProperty("user.name") + "&os_machine=" + System.getProperty("os.name");
        String response = this.request(urlWSEvent.replace("[method]", "listEvent"), params, "POST");
        if (!response.trim().equals("")) {
            try {
                JSONArray jsonArray = new JSONArray("[" + response + "]");
                JSONArray data = jsonArray.getJSONObject(0).getJSONArray("result");
                String[][] lista = new String[data.length()][8];
                for (int i = 0; i < data.length(); i++) {
                    JSONObject json = data.getJSONObject(i);
                    lista[i][0] = json.getString("id_event");
                    lista[i][1] = json.getString("name");
                    lista[i][2] = json.getString("hashtag");
                    lista[i][3] = json.getString("dt_event");
                    lista[i][4] = sn(json.getString("have_print"));
                    lista[i][5] = sn(json.getString("have_screen"));
                    lista[i][6] = sn(json.getString("automatic"));
                    lista[i][7] = json.getString("qtde_fotos");

                }
                return lista;
            } catch (JSONException e) {
                JOptionPane.showMessageDialog(null, "Erro ao listar os eventos! WS: " + e.getMessage());
            }
        } else {
            JOptionPane.showMessageDialog(null, "Não tem responsta do servidor");
        }
        return null;
    }

    public boolean insertEvent(Event event) {
        try {
            String params = event.toParams() + "&login=" + "contato@postprint.com.br" + "&name_machine=" + System.getProperty("user.name") + "&os_machine=" + System.getProperty("os.name");
            String response = this.request(urlWSEvent.replace("[method]", "insertEvent"), params, "POST");
            if (!response.trim().equals("")) {
                JSONArray jsonArray = new JSONArray("[" + response + "]");
                JSONObject json = jsonArray.optJSONObject(0);
                if (json.get("code").toString().equals("1")) {
                    return true;
                } else if (json.get("code").toString().equals("0")) {
                    msgError = json.get("error").toString();
                }
            }
        } catch (JSONException e) {
           JOptionPane.showMessageDialog(null, "Erro ao inserir o evento! ws:"+ e.getMessage());
        }
        return false;
    }
    
    public boolean insertUser() {
        try {
            String params = "&login=" + "contato@postprint.com.br" + "&name_machine=" + System.getProperty("user.name") + "&os_machine=" + System.getProperty("os.name");
            String response = this.request(urlLogado.replace("[method]", "insertUser")+  getToken(), params, "POST");
            if (!response.trim().equals("")) {
                JSONArray jsonArray = new JSONArray("[" + response + "]");
                JSONObject json = jsonArray.optJSONObject(0);
                if (json.get("code").toString().equals("1")) {
                    return true;
                } else if (json.get("code").toString().equals("0")) {
                    msgError = json.get("error").toString();
                }
            }
        } catch (JSONException e) {
           JOptionPane.showMessageDialog(null, "Erro ao inserir o evento! ws:"+ e.getMessage());
        }
        return false;
    }


    public boolean updateEvent(Event event) {
        try {
            String params = event.toParams() + "&login=" + "contato@postprint.com.br" + "&name_machine=" + System.getProperty("user.name") + "&os_machine=" + System.getProperty("os.name");
            String response = this.request(urlWSEvent.replace("[method]", "updateEvent"), params, "POST");
            if (!response.trim().equals("")) {
                JSONArray jsonArray = new JSONArray("[" + response + "]");
                JSONObject json = jsonArray.optJSONObject(0);
                if (json.get("code").toString().equals("1")) {
                    return true;
                } else if (json.get("code").toString().equals("0")) {
                    msgError = json.get("error").toString();
                }
            }
        } catch (JSONException e) {
            JOptionPane.showMessageDialog(null, "Erro ao atualizar o evento! ws:"+ e.getMessage());
        }
        return false;
    }
    
     public boolean removeEvent(Event event) {
        try {
            String params = event.toParams() + "&login=" + "contato@postprint.com.br" + "&name_machine=" + System.getProperty("user.name") + "&os_machine=" + System.getProperty("os.name");
            String response = this.request(urlWSEvent.replace("[method]", "removeEvent"), params, "POST");
            if (!response.trim().equals("")) {
                JSONArray jsonArray = new JSONArray("[" + response + "]");
                JSONObject json = jsonArray.optJSONObject(0);
                if (json.get("code").toString().equals("1")) {
                    return true;
                } else if (json.get("code").toString().equals("0")) {
                    msgError = json.get("error").toString();
                }
            }
        } catch (JSONException e) {
            JOptionPane.showMessageDialog(null, "Erro ao remover o evento! ws:"+ e.getMessage());
        }
        return false;
    }

    public String request(String urlRequest, String params, String method) {
        String resultado = "";
        try {
            urlConnection = null;
            URL url;
            if (method.equals("GET")) {
                if (params.isEmpty() == false) {
                    urlRequest = urlRequest + "&" + params;
                }
            }

            url = new URL(urlRequest);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setDoOutput(true);
            urlConnection.setRequestMethod(method);
            urlConnection.setUseCaches(false);
            urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            urlConnection.setConnectTimeout(5000);
            urlConnection.setReadTimeout(10000);
            if (method.equals("POST")) {

                try (DataOutputStream wr = new DataOutputStream(urlConnection.getOutputStream())) {
                    wr.writeBytes(params);
                    wr.flush();
                }
            }

            if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                try (InputStream in = new BufferedInputStream(
                        urlConnection.getInputStream()); 
                        BufferedReader reader = new BufferedReader(
                                new InputStreamReader(in,
                                        "UTF-8"), TAM_MAX_BUFFER)) {
                    
                    StringBuilder builder = new StringBuilder();
                    
                    for (String line; (line = reader.readLine()) != null;) {
                        builder.append(line).append("\n");
                    }
                    resultado = builder.toString();
                }
            } else {
                JOptionPane.showMessageDialog(null, "ResponseCode: " + urlConnection.getResponseMessage());
            }

        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, e.toString());
            ErrorProgram.addOnFile(e.getMessage());
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }

        return resultado;
    }
}
