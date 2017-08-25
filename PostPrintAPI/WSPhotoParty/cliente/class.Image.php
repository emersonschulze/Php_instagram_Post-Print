<?php

include ROOT_APPLICATION. "classes/class.Thumb.php";

class Image{

	private $width_paper = "642";//Em Pixels
	private $height_paper = "975";//Em Pixels
	private $width_photo = "612";//Em Pixels
	private $height_photo = "612";//Em Pixels
	
	
	public function teste(){
		header ("Content-type: image/jpeg"); 
		
		$image = "http://distilleryimage0.ak.instagram.com/72e9204a3c1311e3bc2222000a1cbcce_7.jpg";
		
		$background = imagecreate($this->width_paper, $this->height_paper);
		$pkt = imagecreatefromjpeg($image); 
		//imagecolortransparent($pkt,imagecolorat($pkt,0,0));
		$insert_x = imagesx($pkt);
  		$insert_y = imagesy($pkt); 

  		imagecopymerge($background,$pkt,50,150,0,0,$insert_x,$insert_y,100);
		
		
		imagejpeg($background,"",100); 
		exit;
	}
	
	public function addInfoTemplate(){
		
		$thumb = new Thumb();
		
		$image = "http://distilleryimage0.ak.instagram.com/72e9204a3c1311e3bc2222000a1cbcce_7.jpg";
		$thumb->Createcanvas($this->width_paper, $this->height_paper, IMAGETYPE_PNG, "#FFFFFF", true);
		
		$thumb->Thumbheight = $this->height_photo;
		$thumb->Thumbwidth = $this->width_photo;
		
		$thumb->Framewidth = 10;
		$thumb->Framecolor = '#00000';
		
		$thumb->Copyrighttext = 'MYWEBMYMAIL.COM';
		$thumb->Copyrightposition = '50% 80%';
		$thumb->Copyrightfontsize = 30;
		$thumb->Copyrighttextcolor = '#FFFFFF';
		
		$thumb->Createthumb($image);
		
	}

}