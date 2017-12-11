<?php

session_start();

// Script By Kshitij Soni  
//Youtube channel : https://www.youtube.com/channel/UCOWT2JvRnSMVSboxvccZBFQ
//Youtube video (login with instagram) : https://www.youtube.com/watch?v=Hi-wVYiJ5fk
	
// if user is logged in, destroy all  sessions
if( isset($_SESSION['user_info']) or isset($_SESSION['login']) ){
	unset( $_SESSION['user_info'] ); // destroy
	unset( $_SESSION['login'] ); // destroy
	header("location: index.php"); // redirect user to index page
}

else{ // if user is not logged in
	header("location: index.php"); // redirect user to index page
}

?>