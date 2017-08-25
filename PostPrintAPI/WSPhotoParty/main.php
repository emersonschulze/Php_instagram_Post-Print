<?php
	
	header("Content-type: text/html; charset=utf-8");

	ini_set("display_errors",1);
    session_start();
    $nro = explode("/",$_SERVER['HTTP_HOST'].substr($_SERVER['SCRIPT_NAME'],0,strrpos($_SERVER['SCRIPT_NAME'],"/")));
    define("HOST","http://".$_SERVER['HTTP_HOST']);
    define("PATH_APPLICATION","http://".$_SERVER['HTTP_HOST'].substr($_SERVER['SCRIPT_NAME'],0,strrpos($_SERVER['SCRIPT_NAME'],"/"))."/");//pasta raiz do projeto    
    define("NUMBER_INI",count($nro));//é o numero onde se inicia a pagina, com base no indice da url amigavel
    define("ROOT_APPLICATION",$_SERVER['DOCUMENT_ROOT'].substr($_SERVER['SCRIPT_NAME'],0,strrpos($_SERVER['SCRIPT_NAME'],"/"))."/");//pasta raiz do projeto
    
    
    #============ Dados do Banco de Dados ==========================
    define("DB_USER","postprint");
    define("DB_PASSWORD","IvuUztSo");
	define("DB_HOST","localhost");
	define("DB_NAME","postprint");
    
	#=========== EMAIL ============================
	define("EMAIL_CONTACT","tecnologia@lin3.com.br");
	define("EMAIL_PASSWORD","lin3@Tecnologia@!");
	
    
    $url = explode("/",str_replace("?","/?",$_SERVER['REQUEST_URI']));    
    $nomeCliente = $url[NUMBER_INI];
    
    function setUrl($page){
        return PATH_APPLICATION.$page;
    }
    function getDado($key){
        if(is_string($key)){
            if(isset($_REQUEST[$key])){return $_REQUEST[$key];}
        //Senão é numerico é o indice da url amigavel
        }elseif(is_numeric($key)){
            global $url;
            if(isset($url[$key+NUMBER_INI])){return $url[$key+NUMBER_INI];}
        }
        return "";
    }
    function setNULL($value){
		$value = trim($value);
		if(empty($value)){
			return "NULL";
		}else{
			return $value;
		}
	}
    function soNumber($value){
        return preg_replace('/([^0-9]*)/', '',$value);
    }
    function escapeString($value) {
		$value=trim($value);
		$value = mysql_escape_string($value);
		return utf8_decode($value);
	}
    
	
    include ROOT_APPLICATION. "classes/class.Cliente.php";
    include ROOT_APPLICATION. "classes/class.Webservice.php";
    include ROOT_APPLICATION. "classes/class.SimpleCripto.php";
	include ROOT_APPLICATION. "classes/class.GenericDataStructure.php";
    include ROOT_APPLICATION. "classes/class.Connection.php";
    include ROOT_APPLICATION. "classes/class.Email.php";
    
    $method = getDado(1);
    define("METHOD", $method);
    try{
    	Connection::openConnection();
    	$cliente = new Cliente($nomeCliente);
    	$ws = new Webservice($cliente);
    	$ws->request($method);
    	echo $ws->getResponse();
    	Connection::closeConnection();
    }catch (Exception $e){
    	echo $e->getMessage();
    	Connection::closeConnection();
    }
    
?>