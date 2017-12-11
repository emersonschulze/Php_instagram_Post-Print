<?php
class Cliente{
	private $class = "";
	public function __construct($nomeCliente){
    	$this->loadCliente($nomeCliente);
	}
	
	
	
	private function loadCliente($nomeCliente){
		if(file_exists("cliente/class.".$nomeCliente.".php")){
			$this->class = $nomeCliente;
		}else{
			$this->class = "DefaultClient";
		}
	}
	public function request($method){
	  	if(file_exists("cliente/class.".$this->class.".php")){
	  		include("cliente/class.".$this->class.".php");
	  	}else{
	  		return NULL;
	  	}
	  	$reflect = new ReflectionClass($this->class);
		$obj = ( $reflect->isInstantiable() ) ? $reflect->newInstance() : false ;
		if ( $obj ){
			$reflect->hasMethod( $method ) || $method = 'notFound';
			if ( $reflect->hasMethod( $method) ){
				return  $this->action( $obj, $method );
			}
		}else{
			return "METHOD-NOT-FOUND";
		}
  	}
	private function action( $obj, $method ){
			return call_user_func( array( $obj, $method ) );
	}
}
?>