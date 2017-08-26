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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Webservice {
    private static final int TAM_MAX_BUFFER = 10240; // 10Kbytes
    
    private final String urlTags = "https://api.instagram.com/v1/tags/[myTag]?access_token=270817430.6c61e68.80a6f0177a744248a581147fe03e08ce";
    private final String urlPhoto = "https://api.instagram.com/v1/media/[IDPhoto]?access_token=270817430.6c61e68.80a6f0177a744248a581147fe03e08ce";
    private final String user = "https://api.instagram.com/oauth/authorize/?client_id=6c61e681117e45baa6ea51667ce36d32&redirect_uri=https://postprint.com.br/&response_type=code";
    public String urlWSEvent = "https://postprint.com.br/WSPhotoParty/Event/[method]?format=json";
    private HttpURLConnection urlConnection;
    
    public String msgError = "";
    
    public Webservice() {
        
    }
    
    public Photo getInfoPhoto(String idPhoto){
        
        Photo photo = new Photo();
        String response = this.request(urlPhoto.replace("[IDPhoto]", idPhoto) , "","GET");
    	if(!response.trim().equals("")){
            try{
                
                JSONArray jsonArray = new JSONArray("["+response+"]");
                JSONObject json = jsonArray.getJSONObject(0).getJSONObject("data");
                photo.id = (String) json.get("id");
                photo.url = (String)json.getJSONObject("images").getJSONObject("standard_resolution").get("url").toString();
                photo.nome_usuario = "@"+(String)json.getJSONObject("user").get("username").toString();
                photo.foto_usuario = (String)json.getJSONObject("user").get("profile_picture").toString();
                try{
                    photo.description = (String)json.getJSONObject("caption").get("text").toString();
                }catch(Exception ex){
                    System.out.println("Caracter especial no caption");
                }
            }catch(Exception e){
                System.out.println("Erro WS: "+e.getMessage()+ " -- " + response);
            }
    	}else{
            System.out.println("Não tem responsta : "+urlPhoto.replace("[IDPhoto]", idPhoto));
        }
    	
        return photo;
    }
    
    public ArrayList<Photo> getListPhotos(String tag){
    	ArrayList<Photo> lista = new ArrayList<Photo>();
    	String response = this.request(urlTags.replace("[myTag]", tag) , "","GET");
    	if(!response.trim().equals("")){
            try{
                //System.out.println("PASSO 1 "+response);
                //response = org.apache.commons.lang.StringEscapeUtils.unescapeJava(response);
                
                JSONArray jsonArray = new JSONArray("["+response+"]");
                JSONArray data = jsonArray.getJSONObject(0).getJSONArray("data");
                for(int i = 0; i < data.length(); i++){

                    JSONObject json = data.getJSONObject(i);

                    Photo photo = new Photo();
                    photo.id = (String) json.get("id");
                    photo.url = (String)json.getJSONObject("images").getJSONObject("standard_resolution").get("url").toString();
                    photo.nome_usuario = "@"+(String)json.getJSONObject("user").get("username").toString();
                    photo.foto_usuario = (String)json.getJSONObject("user").get("profile_picture").toString();
                    try{
                        photo.description = (String)json.getJSONObject("caption").get("text").toString();
                    }catch(Exception ex){
                        System.out.println("Caracter especial no caption");
                    }
                    lista.add(photo);
                }
            }catch(Exception e){
                System.out.println("Erro WS: "+e.getMessage());
            }
    	}else{
            System.out.println("Não tem responsta");
        }
    	
    	return lista;
    }
    
     public boolean existHashTag(String tag){
    	String response = this.request(urlTags.replace("[myTag]", tag) , "","GET");
    	if(!response.trim().equals("")){
            try{
                JSONArray jsonArray = new JSONArray("["+response+"]");
                Object media = jsonArray.optJSONObject(0).optJSONObject("data").get("media_count");
                if(!media.equals(0)){
                    return true;
                }
            }catch(JSONException e){
                System.out.println("Erro WS: "+e.getMessage());
            }
    	}else{
            System.out.println("Não tem responsta");
        }
    	
    	return false;
    }
    
    public JSONArray getListTemplate(){
        String response = this.request(urlWSEvent.replace("[method]", "listTemplates") ,"","GET");
    	if(!response.trim().equals("")){
            try{
                JSONArray jsonArray = new JSONArray("["+response+"]");
                if(jsonArray.length() > 0){
                    if(jsonArray.getJSONObject(0).getString("code").equals("1")){
                        return jsonArray;
                    }
                }
            }catch(Exception e){
                System.out.println("Erro WS: "+e.getMessage());
            }
    	}else{
            System.out.println("Não tem resposta");
        }
    	
    	return null;
    }
    
    
    public JSONObject getConfigTemplate(String id_template){
    	
    	String response = this.request(urlWSEvent.replace("[method]", "getConfigTemplate") , "id_template="+id_template,"GET");
    	if(!response.trim().equals("")){
    		try{
                    JSONArray jsonArray = new JSONArray("["+response+"]");
                    JSONObject json = jsonArray.optJSONObject(0);
                    return json;
    		}catch(Exception e){
                    System.out.println("Erro WS: "+e.getMessage());
    		}
    	}else{
            System.out.println("Não tem resposta");
        }
    	
    	return null;
    }
    
    private String sn(String value){
        if(value.equals("S")){
            return "Sim";
        }
        return "Não";
    }
    
    public JSONObject getEvento(String id_event){
        Core core = new Core();
        if(core.isValidCertificate()){
            String params = "login="+core.login+"&name_machine="+System.getProperty("user.name")+"&os_machine="+System.getProperty("os.name");
            String response = this.request(urlWSEvent.replace("[method]", "getEvent") , "id_event="+id_event+"&"+params,"POST");
            if(!response.trim().equals("")){
                try{
                    JSONArray jsonArray = new JSONArray("["+response+"]");
                    JSONArray data = jsonArray.getJSONObject(0).getJSONArray("result");
                    JSONObject json = data.getJSONObject(0);
                    return json;
                }catch(Exception e){
                    System.out.println("Erro WS: "+e.getMessage());
                }
            }else{
                System.out.println("Não tem responsta");
            }
        }
        return null;
    }
    
    public String[][] getListEventos(){
        Core core = new Core();
        if(core.isValidCertificate()){
            String params = "login="+core.login+"&name_machine="+System.getProperty("user.name")+"&os_machine="+System.getProperty("os.name");
            String response = this.request(urlWSEvent.replace("[method]", "listEvent") , params,"POST");
            if(!response.trim().equals("")){
                try{
                    JSONArray jsonArray = new JSONArray("["+response+"]");
                    JSONArray data = jsonArray.getJSONObject(0).getJSONArray("result");
                    String[][] lista = new String[data.length()][8];
                    for(int i = 0; i < data.length(); i++){

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
                }catch(Exception e){
                    System.out.println("Erro WS: "+e.getMessage());
                }
            }else{
                System.out.println("Não tem responsta");
            }
        }
        return null;
    }
    
    public boolean insertEvent(Event event){
        try{
            
            Core core = new Core();
            if(core.isValidCertificate()){
                
                String params = event.toParams()+"&login="+core.login+"&name_machine="+System.getProperty("user.name")+"&os_machine="+System.getProperty("os.name");
                
                //System.out.println("PARAMS: "+event.toParams()+params+" -- url: "+urlWSEvent.replace("[method]", "insertEvent"));
                
                String response = this.request(urlWSEvent.replace("[method]", "insertEvent") , params,"POST");
                
                if(!response.trim().equals("")){
                    
                    JSONArray jsonArray = new JSONArray("["+response+"]");
                    JSONObject json = jsonArray.optJSONObject(0);
                    if(json.get("code").toString().equals("1")){
                        return true;
                    }else if(json.get("code").toString().equals("0")){
                        msgError = json.get("error").toString();
                    }
                }   
            }
            
        }catch(Exception e){
            System.out.println("Erro WS: "+e.getMessage());
        }
        
        return false;
    }
    
    public boolean updateEvent(Event event){
        try{
            
            Core core = new Core();
            if(core.isValidCertificate()){
                String params = event.toParams()+"&login="+core.login+"&name_machine="+System.getProperty("user.name")+"&os_machine="+System.getProperty("os.name");
                String response = this.request(urlWSEvent.replace("[method]", "updateEvent") , params,"POST");
                if(!response.trim().equals("")){
                    System.out.println(response);
                    JSONArray jsonArray = new JSONArray("["+response+"]");
                    JSONObject json = jsonArray.optJSONObject(0);
                    if(json.get("code").toString().equals("1")){
                        return true;
                    }else if(json.get("code").toString().equals("0")){
                        msgError = json.get("error").toString();
                    }
                }   
            }
            
        }catch(Exception e){
            System.out.println("Erro WS: "+e.getMessage());
        }
        
        return false;
    }
    
    public String request(String urlRequest,String params, String method){
        String resultado = "";
        try {
          urlConnection = null;
          URL url;
          if(method.equals("GET")){
              if(params.isEmpty() == false){
                  urlRequest = urlRequest +"&"+params;
              }
          }
          
          url= new URL(urlRequest);
          urlConnection = (HttpURLConnection) url.openConnection();
          urlConnection.setDoOutput(true);
          urlConnection.setRequestMethod(method);
          urlConnection.setUseCaches(false);
          urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded"); 
          
          urlConnection.setConnectTimeout(5000);
          urlConnection.setReadTimeout(10000);
          //urlConnection.setRequestProperty("Content-Type","text/json; charset=utf-8");
          
          if(method.equals("POST")){
                
                DataOutputStream wr  = new DataOutputStream(urlConnection.getOutputStream());
                wr.writeBytes(params);
                wr.flush();
                wr.close();
          }
          //OutputStream out = urlConnection.getOutputStream();
          //out.write(params.getBytes());
          
          if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
              InputStream in = new BufferedInputStream(
                      urlConnection.getInputStream());    
              
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(in,
                                "UTF-8"), TAM_MAX_BUFFER);
                
                StringBuilder builder = new StringBuilder();
                
                for (String line = null ; (line = reader.readLine())!= null;) {
                    builder.append(line).append("\n");
                }
                
                resultado = builder.toString();
                
                reader.close();
                in.close();
                
                // Retira a string <?xml version="1.0" encoding="utf-8" ?> 
                // <string xmlns="http://tempuri.org/"> e a tag </GetEstadosResult> 
                // para obter o resultado em Json, já que o webservice está
                // retornando uma string
          }
          else{
              System.out.println("ResponseCode: " + urlConnection.getResponseMessage());
          }

        }
        catch(IOException e){
            System.out.println(e.toString());
            ErrorProgram.addOnFile(e.getMessage());
        }
        finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
       }
        
        return resultado;
    }
}
