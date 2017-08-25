<?php
class SimpleCripto{
	
	private $senha = "Senha";
	
	public function getSenha(){
		return $this->senha;
	}
	
	public function setSenha($val){
		$this->senha = $val;
	}
	
	private function randomizar($iv_len){
	    $iv = '';
	    while ($iv_len-- > 0) {
	        $iv .= chr(mt_rand() & 0xff);
	    }
	    return $iv;
	}

	public  function encriptar($texto, $iv_len = 16){
	    $texto .= "\x13";
	    $n = strlen($texto);
	    if ($n % 16) $texto .= str_repeat("\0", 16 - ($n % 16));
	    $i = 0;
	    $enc_Texto = $this->randomizar($iv_len);
	    $iv = substr($this->senha ^ $enc_Texto, 0, 512);
	    while ($i < $n) {
	        $bloco = substr($texto, $i, 16) ^ pack('H*', md5($iv));
	        $enc_Texto .= $bloco;
	        $iv = substr($bloco . $iv, 0, 512) ^ $this->senha;
	        $i += 16;
	    }
	    return base64_encode($enc_Texto);
	}

	public function desencriptar($enc_Texto, $iv_len = 16){
	    $enc_Texto = base64_decode($enc_Texto);
	    $n = strlen($enc_Texto);
	    $i = $iv_len;
	    $texto = '';
	    $iv = substr($this->senha ^ substr($enc_Texto, 0, $iv_len), 0, 512);
	    while ($i < $n) {
	        $bloco = substr($enc_Texto, $i, 16);
	        $texto .= $bloco ^ pack('H*', md5($iv));
	        $iv = substr($bloco . $iv, 0, 512) ^ $this->senha;
	        $i += 16;
	    }
	    return preg_replace('/\\x13\\x00*$/', '', $texto);
	}
	
}

/*****************************************
$texto = 'Texto a ser criptografado';
$senha = 'Senha';
echo "O texto é: [${texto}]<br />\n";
echo "A senha é: [${senha}]<br />\n";

$Enc_Texto = Encriptar($texto, $senha);
echo "Texto encriptado: [${Enc_Texto}]<br />\n";

$texto2 = Desencriptar($Enc_Texto, $senha);
echo "O texto desencriptado: [${texto2}]<br />\n";

*/

?>