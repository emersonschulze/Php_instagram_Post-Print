<?php

function datebr_to_date( $v ){
	return preg_replace( '/([0-9]{2})\/([0-9]{2})\/([0-9]{4})/', '$3-$2-$1', $v );
}

class Event{
	private $token = "Token-do-PhotoPartyLin3-";
	private $cripto;
	
	private $data = array();
	
	public  function __construct(){
		$this->cripto = new SimpleCripto();
		$this->cripto->setSenha("Ph07O-P@rty");	
		$this->token .= date("Ymdh");
	}
	
	private function auth(){
		if(isset($_POST['name_machine']) && isset($_POST['login'])){
			$sql = "select id_user, serial, active from user where name_machine = '".escapeString($_POST['name_machine'])."' and login = '".escapeString($_POST['login'])."'";
			$result = Connection::query($sql);
			if($result->num_rows > 0){
				$res = $result->fetch_assoc();
				if($res['active'] == "S"){
					$key = $this->cripto->desencriptar(str_replace(" ", "+",$res['serial']));
					if(strtoupper($key) == strtoupper($_POST['login'].$_POST['name_machine'])){
						$this->data['id_user'] = $res['id_user'];
						return true;
					}
				}
			}
			return false;
		}
	}
	
	public function validLogin(){
		if($this->auth()){
			return array("code" => "1", "result" => "OK");
		}else{
			return array("code" => "0", "error" => "Certificado não autorizado");
		}
	}
	
	public function assignCertificate(){
		if(isset($_POST['name_machine'])  && isset($_POST['login'])){
			$sql = "select id_user, serial, active from user where login = '".escapeString($_POST['login'])."'";
			$result = Connection::query($sql);
			if($result->num_rows > 	0){
				$res = $result->fetch_assoc();
				if($res['active'] == "S"){
					$key = $this->cripto->desencriptar(str_replace(" ", "+",$res['serial']));
					if(strtoupper($key) == strtoupper($_POST['login'].$_POST['name_machine'])){
						
						$os_machine = "";
						if(isset($_POST['os_machine'])){
							$os_machine = $_POST['os_machine'];
						}
						
						$sql = "update user set os_machine = '".escapeString($os_machine)."', name_machine = '".escapeString($_POST['name_machine'])."' where id_user = ".$res['id_user'];
						$result = Connection::query($sql);
						if(Connection::getAffecteds() > 0){
							return array("code" => "1", "result" => "OK");
						}
					}
				}
			}
		}
		return array("code" => "0", "error" => "Certificado não autorizado");
	}
	
	public function generateSerial(){
		if(isset($_POST['login']) && isset($_POST['password']) && $_POST['name_machine']){
			try {
				$sql = "select id_user, serial, active, login from user where login = '".escapeString($_POST['login'])."' and password = '".escapeString($_POST['password'])."'";
				$busca = Connection::query($sql);
				if($busca->num_rows > 0){
					$res = $busca->fetch_assoc();
					if($res['serial'] == "" && $res['active'] == "N"){
						$serial = $_POST['login'].$_POST['name_machine'];
						$serial = $this->cripto->encriptar($serial);
						$sql = "update user set serial = '".$serial."',name_machine = '".$_POST['name_machine']."' where id_user = ".$res['id_user'];
						$result = Connection::query($sql);
						if(Connection::getAffecteds() > 0){
							/*$link = "http://postprint.lin3.com.br/WSPhotoParty/Event/activeLicense?key=".$serial;
							$mail = new Email();
							$mail->setUserName(EMAIL_CONTACT);
							$mail->setPassword(EMAIL_PASSWORD);
							if($mail->sendEmail($res['login'], utf8_decode("PhotoParty: Ativar licença"), utf8_decode("Acesso o link abaixo para ativar sua licença de uso.<br>").$link, "PhotoParty")){*/
								return array("code" => "1", "result" => "OK", "site" => "http://postprint.com.br" );	
							/*}else{
								return array("code" => "0", "error" => "Serial gerado, mas não foi possivel enviar o e-mail, acesse a conta online");
							}*/
							
						}else{
							return array("code" => "0", "error" => "Erro ao gerar serial");
						}
					}else{
						return array("code" => "0", "error" => "Serial já gerado para este usuário");
					}
				}		
			} catch (Exception $e) {
				return array("code" => "0", "error" => "Erro inesperado: ".$e->getMessage());
			}
		}else{
			return array("code" => "0", "error" => "Dados não informados");
		}
	}
	
	public function updateEvent(){
		if($this->auth()){
			try{
				if(isset($_POST['id_event']) && isset($_POST['name']) && isset($_POST['dt_event']) && isset($_POST['hashtag']) && isset($_POST['have_screen']) && isset($_POST['have_print'])){
					$sql = "select id_event from event where id_event = ".soNumber($_POST['id_event']);
					$busca = Connection::query($sql);
					if($busca->num_rows > 0){
						$sql = "select id_event from event where name = '".escapeString($_POST['name'])."' and id_event <> ".soNumber($_POST['id_event']);
						$busca = Connection::query($sql);
						if($busca->num_rows > 0){
							return array("code" => "0", "error" => "Nome de evento já está sendo utilizado por outro evento");
						}
						$sql = "select id_event from event where hashtag = '".escapeString($_POST['hashtag'])."' and id_event <> ".soNumber($_POST['id_event']);
						$busca = Connection::query($sql);
						if($busca->num_rows > 0){
							return array("code" => "0", "error" => "Hashtag já cadastrada para outro evento");
						}
						$sql = "update event set name = '".escapeString($_POST['name'])."', 
						dt_event = '".escapeString(datebr_to_date($_POST['dt_event']))."',
						hashtag = '".escapeString($_POST['hashtag'])."',
						logo_event = '".escapeString($_POST['logo_event'])."',  
						automatic = '".escapeString($_POST['automatic'])."',
						have_screen = '".escapeString($_POST['have_screen'])."',
						have_print = '".escapeString($_POST['have_print'])."',
						id_print_template = ".setNULL(soNumber($_POST['id_print_template'])).", 
						qtde_fotos = ".(int)soNumber($_POST['qtde_fotos'])." 
						where id_event = ".soNumber($_POST['id_event']);
						$result = Connection::query($sql);
						if(Connection::getAffecteds() > 0){
							return array("code" => "1", "result" => "OK");
						}else{
							return array("code" => "0", "error" => "Nenhum Dado foi alterado por você");
						}
					}else{
						return array("code" => "0", "error" => "Evento já existe");
					}
				}
			}catch (Exception $e){
				return array("code" => "0", "error" => "Erro inesperado: ".$e->getMessage());
			}
		}else{	
			return array("code" => "0", "error" => "Não autenticado");
		}
	}
	
	public function removeEvent(){
		if(isset($_POST['id_event'])){
			$sql = "select id_event from event where id_event = ".soNumber($_POST['id_event']);
			$busca = Connection::query($sql);
			if($busca->num_rows > 0){
				$sql = "delete from event where id_event = ".soNumber($_POST['id_event']);
				$result = Connection::query($sql);
				if(Connection::getAffecteds() > 0){
					return array("code" => "1", "result" => "OK");
				}else{
					return array("code" => "0", "error" => "Nenhum Dado foi removido");
				}
			}else{
				return array("code" => "0", "error" => "Evento não encontrato");
			}
		}
	}
	
	public function insertEvent(){
		if($this->auth()){
			try{
				if(isset($_POST['name']) && isset($_POST['dt_event']) && isset($_POST['hashtag'])  && isset($_POST['automatic']) && isset($_POST['have_screen']) && isset($_POST['have_print'])){
					$sql = "select id_event from event where name = '".escapeString($_POST['name'])."'";
					$busca = Connection::query($sql);
					if($busca->num_rows > 0){
						return array("code" => "0", "error" => "Evento já existe");
					}else{
						$sql = "select id_event from event where hashtag = '".escapeString($_POST['hashtag'])."'";
						$busca = Connection::query($sql);
						if($busca->num_rows > 0){
							return array("code" => "0", "error" => "Hashtag já cadastrada para outro evento");
						}
						$sql = "insert into event(name, dt_event,hashtag,automatic,have_screen,have_print,id_print_template,logo_event,active,qtde_fotos)values";
						$sql .= "('".escapeString($_POST['name'])."','".escapeString(datebr_to_date($_POST['dt_event']))."','".escapeString($_POST['hashtag'])."','".escapeString($_POST['automatic'])."','".escapeString($_POST['have_screen'])."', '".escapeString($_POST['have_print'])."', ".setNULL(soNumber($_POST['id_print_template'])).",'".escapeString($_POST['logo_event'])."','N', ".(int)soNumber($_POST['qtde_fotos']).")";
						$result = Connection::query($sql);
						if(Connection::getAffecteds() > 0){
							return array("code" => "1", "result" => "OK");
						}else{
							return array("code" => "0", "error" => "Erro ao incluir evento");
						}
					}
				}
			}catch (Exception $e){
				return array("code" => "0", "error" => "Erro inesperado: ".$e->getMessage());
			}
		}else{	
			return array("code" => "0", "error" => "Não autenticado");
		}
	}
	
	public function getConfigTemplate(){

		//Default
		$array = array( "papel_width" => "598",
				"papel_height" => "803",
				"foto_width" => "538",
				"foto_height" => "538",
				"foto_left" => "30",
				"foto_top" => "100",
				"usuario_top" => "50",
				"usuario_left" => "30",
				"usuario_font" => "Times New Roman",
				"usuario_font_size" => "30",
				"usuario_font_style" => "NORMAL",
				"usuario_align" => "NORMAL",
				"empresa_image" => "",
				"empresa_width" => "100",
				"empresa_height" => "40",
				"empresa_left" => "468",
				"empresa_top" => "40",
				"evento_width" => "",
				"evento_height" => "70",
				"evento_left" => "",
				"evento_top" => "716",
				"evento_font" => "Times New Roman",
				"evento_font_size" => "30",
				"evento_font_style" => "NORMAL",
				"evento_align" => "CENTER");
		
		if(isset($_GET['id_template'])){
			$id_template = soNumber($_GET['id_template']);
			if($id_template != ""){
				$sql = "select specs, layout_image from print_template where id_print_template = ".$id_template;
				$busca = Connection::query($sql);
				if($busca->num_rows == 1){
					$res = $busca->fetch_assoc();
					$json = json_decode($res['specs']);
					$array = array( "papel_width" => $json->papel_width,
									"papel_height" => $json->papel_height,
									"foto_width" => $json->foto_width,
									"foto_height" => $json->foto_height,
									"foto_left" => $json->foto_left,
									"foto_top" => $json->foto_top,
									"usuario_top" => $json->usuario_top,
									"usuario_left" => $json->usuario_left,
									"usuario_font" => $json->usuario_font,
									"usuario_font_size" => $json->usuario_font_size,
									"usuario_font_style" => $json->usuario_font_style,
									"usuario_align" => $json->usuario_align,
									"empresa_image" => $res['layout_image'],
									"empresa_width" => $json->empresa_width,
									"empresa_height" => $json->empresa_height,
									"empresa_left" => $json->empresa_left,
									"empresa_top" => $json->empresa_top,
									"evento_width" => $json->evento_width,
									"evento_height" => $json->evento_height,
									"evento_left" => $json->evento_left,
									"evento_top" => $json->evento_top,
									"evento_font" => $json->evento_font,
									"evento_font_size" => $json->evento_font_size,
									"evento_font_style" => $json->evento_font_style,
									"evento_align" => $json->evento_align);
				}
			}
		}		
		return $array;
		
	}
	
	public function login(){
		try{
			if(isset($_POST['login']) && isset($_POST['password'])){
				$sql = "select id_user, login, serial, active, os_machine, name_machine from user where login = '".escapeString($_POST['login'])."' and password = '".escapeString($_POST['password'])."'";
				$result = Connection::query($sql);
				if($result->num_rows == 1){
					return array("code" => "1", "result" => "OK", "data" => $result->fetch_assoc());
				}
			}else{
				return array("code" => "0", "error" => "Parametros não encontrados");
			}
		}catch (Exception $e){
			return array("code" => "0", "error" => "Erro inesperado: ".$e->getMessage());
		}
	}
	
	public function listEvent(){
		if($this->auth()){
			try{
				$sql = "select id_event, name, DATE_FORMAT(dt_event, '%d/%m/%Y') as dt_event, have_screen, have_print, automatic, hashtag, id_print_template, logo_event, active, qtde_fotos from event order by id_event desc";
				$busca = Connection::query($sql);
				$array = array("code" => "1", "result" => array());
				if($busca->num_rows > 0){
					while($res = $busca->fetch_assoc()){
						$array['result'][] = $res;
					}
				}
				return $array;
			}catch (Exception $e){
				return array("code" => "0", "error" => "Erro inesperado: ".$e->getMessage());
			}
		}else{	
			return array("code" => "0", "error" => "Não autenticado");
		}
	}
	
	public function getEvent(){
		if($this->auth() && isset($_POST['id_event'])){
			try{
				$sql = "select id_event, name, DATE_FORMAT(dt_event, '%d/%m/%Y') as dt_event, have_screen, have_print, automatic, hashtag, id_print_template, logo_event, active, qtde_fotos from event where id_event = ".soNumber($_POST['id_event'])." order by active, dt_event desc";
				$busca = Connection::query($sql);
				$array = array("code" => "1", "result" => array());
				if($busca->num_rows > 0){
					while($res = $busca->fetch_assoc()){
						$array['result'][] = $res;
					}
				}
				return $array;
			}catch (Exception $e){
				return array("code" => "0", "error" => "Erro inesperado: ".$e->getMessage());
			}
		}else{	
			return array("code" => "0", "error" => "Não autenticado");
		}
	}
	
	public function listTemplates(){
		try{
			$sql = "select id_print_template,name,specs,layout_image from print_template";
			$busca = Connection::query($sql);
			$array = array("code" => "0");
			if($busca->num_rows > 0){
				$array = array("code" => "1", "result" => array());
				while($res = $busca->fetch_assoc()){
					$array['result'][] = $res;
				}
			}
			return $array;
		}catch (Exception $e){
			return array("code" => "0", "error" => "Erro inesperado: ".$e->getMessage());
		}
	}
	
	
	public function notFound(){
		return "method-not-found";
	}
	
	public function getToken(){
		return  urlencode($this->cripto->encriptar($this->token));
	}
	public function exigeToken(){
		if($this->cripto->desencriptar(getDado("token") ) != $this->token){
			echo "token-invalid";
			exit;
		}
	}

	public function activeLicense(){
		$html = "<h2>Não foi possível activar sua conta, entre em contato com o suporte</h2>";
		if(isset($_GET['key'])){
			$sql = "select serial, id_user from user where serial = '".escapeString(str_replace(" ","+",$_GET['key']))."'";
			//$html .= $sql;
			$busca = Connection::query($sql);
			if($busca->num_rows == 1){
				$res = $busca->fetch_assoc();
				$sql = "update user set active = 'S' where id_user = ".$res['id_user'];
				$result = Connection::query($sql);
				if(Connection::getAffecteds() > 0){
					$html = "<h2>Conta ativada com sucesso</h2>";
				}
			}
		}
		echo $html;
		exit;
	}
	
}
?>