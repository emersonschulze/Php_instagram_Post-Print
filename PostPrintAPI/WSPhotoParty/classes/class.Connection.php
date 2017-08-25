<?php
class Connection {		

	private static $ins_id		= NULL;
	private static $affected_r	= NULL;
	public static $qtde_pagina = 10;
	public static $page = 1;
	public static $htmlPaginacao = "";
	public static $conn = NULL;
	
	public static function openConnection() {
		if(self::$conn == NULL){
			self::$conn = mysqli_connect(DB_HOST,DB_USER,DB_PASSWORD,DB_NAME);
		}
	} 
	public static function getInsertId() {
		return self::$ins_id;
	}
	public static function getAffecteds() {
		return self::$affected_r;
	}
	public static function setQtdePaginacao($val){	
		$val = soNumber($val);
		self::$qtde_pagina =$val;
	}
	public static function query($sql,$paginacao = false) {
		if($paginacao){
			$sql_pag = "select count(*) as total ".substr($sql,strrpos($sql," from "));
			$busca = self::query($sql_pag);
			if($busca->num_rows > 0){
				$res = $busca->fetch_assoc();
				$total = $res['total'];
				if(isset($_GET['page'])){self::$page = $_GET['page'];}
				$ini = self::$page * self::$qtde_pagina;
				$tot_pages = ceil($total/self::$qtde_pagina);
				$ini = $ini - self::$qtde_pagina;
				$sql .= " limit ".$ini.",".self::$qtde_pagina;
				$url = substr($_SERVER['REQUEST_URI'],0,strrpos($_SERVER['REQUEST_URI'],"/?page"));

				if(empty($url)){$url = $_SERVER['REQUEST_URI'];}
				$url = "http://".$_SERVER['HTTP_HOST'].$url;
				
				if(isset($_GET['page'])){
					$url = str_replace("&page=".$_GET['page'],"",$url);
				}
				
				if(strrpos($url,"/?") > 0 || strrpos($url,"?") > 0){
					$url = $url."&";
				}else{
					$url = $url."/?";
				}
				
				$url = str_replace("ajax/","", $url);
				
				if($tot_pages > 1){
					self::$htmlPaginacao = '<table border="0" cellpadding="0" cellspacing="0" id="paging-table"><tr><td><a href="'.$url.'page=1" class="page-far-left"></a>';
					if(self::$page > 1){self::$htmlPaginacao .= "<a href='".$url."page=".(self::$page-1)."' class='page-left'></a>";}
					self::$htmlPaginacao .= '<div id="page-info">Página <strong>'.self::$page.'</strong> / '.$tot_pages.'</div>';
					if(self::$page < $tot_pages ){self::$htmlPaginacao .= "<a href='".$url."page=".(self::$page+1)."' class='page-right'></a>";}
					self::$htmlPaginacao .= '<a href="'.$url.'page='.$tot_pages.'" class="page-far-right"></a>';
					self::$htmlPaginacao .= '
														&nbsp;&nbsp;<select id="paginacao" class="fancy" name="paginacao" style="width:20%" onChange="javascript:document.location.href=this.value" style="width:100px;">
															<option value="">Páginas</option>';
															for($i = 1; $i <= $tot_pages;$i++){
																self::$htmlPaginacao .= '<option value="'.$url.'page='.$i.'">'.$i.'</option>';	
															}
					self::$htmlPaginacao .= '</select>
													</td>
												</tr>
											</table>';	
				}
			}
		}
		
		$result = mysqli_query(self::$conn,$sql);
		
		if (mysqli_error(self::$conn)) {
			try {   
				throw new Exception("MySQL error {".mysqli_error(self::$conn)."} <br> Query: [$sql]", mysqli_errno(self::$conn));   	
			}catch(Exception $e ) {
				echo $e->getMessage();
				//echo "Ocorreu um erro inesperado";
				exit;
			}
		}
		self::$ins_id = mysqli_insert_id(self::$conn);
		self::$affected_r = mysqli_affected_rows(self::$conn);
		return $result;
	}
	public static function criaPaginacao(){
		return self::$htmlPaginacao;
	}
	
	public static function closeConnection() {
		if(self::$conn != NULL){
			mysqli_close(self::$conn);
			self::$conn = NULL;
		}
	} 
	
} // class Connection

?>