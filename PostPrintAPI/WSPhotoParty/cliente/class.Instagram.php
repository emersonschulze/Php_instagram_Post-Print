<?php session_start();
ob_start();
$ler = $_COOKIE['CookiePostPrint'];
function datebr_to_date( $v ){
	return preg_replace( '/([0-9]{2})\/([0-9]{2})\/([0-9]{4})/', '$3-$2-$1', $v );
}


class Instagram{
   	private $data = array();

    public function insertUser(){
    	try{	
    		if(isset($ler)){
				$sql = "select id_instagram from instagram where id_suaID = '".escapeString($_SESSION['user_info']['data']['id'])."'";
    			$busca = Connection::query($sql);
    			if($busca->num_rows > 0){
    			return array("code" => "0", "error" => "Usuário cadastrado, atualizar");
    			}else{
    				$sql = "insert into instagram(username,imagem_perfil,insta_token, id_suaID)values";
    				$sql .= "('".escapeString($_SESSION['user_info']['data']['username'])."','".escapeString($_SESSION['user_info']['data']['profile_picture'])."','".escapeString($_SESSION['access_token'])."', '".escapeString($_SESSION['user_info']['data']['id'])."')";
    				$result = Connection::query($sql);
    				if(Connection::getAffecteds() > 0){
    					return array("code" => "1", "result" => "OK");
    				}else{
    					return array("code" => "0", "error" => "Erro ao incluir usuário");
    				}
    			}
    		}else{
    		 	return array("code" => "0", "error" => "Erro ao incluir usuário");
    		}
    	}catch (Exception $e){
			return array("code" => "0", "error" => "Algum campo obrigatório não foi informado: ".$e->getMessage());
		}
	}
    
	public function updateUser(){
	    try{
    	    if(isset($_SESSION['user_info'])){
    			$sql = "select id_instagram from instagram where id_suaID  = '".escapeString($_SESSION['user_info']['data']['id'])."'";
    			$busca = Connection::query($sql);
    			if($busca->num_rows > 0){
    				$sql = "update instagram set username = '".escapeString($_SESSION['user_info']['data']['username'])."', imagem_perfil = '".escapeString($_SESSION['user_info']['data']['profile_picture'])."', insta_token = '".escapeString($_SESSION['access_token'])."' where id_suaID  = '".escapeString($_SESSION['user_info']['data']['id'])."'";
    				$result = Connection::query($sql);
    				if(Connection::getAffecteds() > 0){
    					return array("code" => "1", "result" => "OK");
    				}else{
    					return array("code" => "0", "error" => "Nenhum Dado foi alterado por você");
    				}
    			}else{
    				return array("code" => "0", "error" => "Nenhum Usuário encontrado");
    			}
    		}
	    }catch (Exception $e){
			return array("code" => "0", "error" => "Algum campo obrigatório não foi informado: ".$e->getMessage());
		}
	    
	}
	
	public function carregaUser(){
	   try{
	        if(isset($_SESSION['user_info'])){
    			$sql = "select username, imagem_perfil, insta_token from instagram where username like '%emerson%'";
    			$busca = Connection::query($sql);
    				$array = array("code" => "1", "result" => array());
    				if($busca->num_rows > 0){
    					while($res = $busca->fetch_assoc()){
    						$array['result'][] = $res;
    					}
    				}
    				return $array;
	        }
			}catch (Exception $e){
				return array("code" => "0", "error" => "Erro inesperado: ".$e->getMessage());
	
		}
	}  
}
?>