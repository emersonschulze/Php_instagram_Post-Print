<?php
class Webservice{
    private $response = NULL;
    private $cliente = NULL;
	private $formats = array("json","xml","serialize","image");
    public function __construct(Cliente $cli){
        $this->cliente = $cli;
    }
    public function request($method){
    	$this->response = $this->cliente->request($method);
    }
    public function getResponse(){
    	if(in_array(getDado("format"), $this->formats) ){
    		$format = getDado("format");
    	}else{
    		$format = "json";
    	}
    	if($format == "xml"){
    		header("Content-type: text/xml; charset=utf-8");
    	}
    	
    	if(!is_null($this->response)){
    		$generic = new GenericDataStructure($this->response);
    		if($format == "json"){
    			return $generic->toJSON();
    		}elseif($format == "serialize"){
    			return $generic->toSerializePHP();
    		}elseif($format == "xml"){
    			return $generic->toXML();
    		}else{
    			$this->response;
    		}
    	}
    }
    
}
?>