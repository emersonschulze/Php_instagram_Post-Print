<?php 
session_start();

$ler =  unserialize($_COOKIE['CookiePostPrint']);

     if( isset($_SESSION['user_info'])){ // check is user is logged in
        $title = "Logged in as ".$_SESSION['user_info']['data']['full_name']; // page title
        //$title = 0;
        }
        
        else{
        $title = "Logado no Instagram"; // page title
        }
        
        ?>
        <!DOCTYPE html>
        <html>
        <head>
        <meta charset="utf-8">
        <title><?php echo $title; ?></title>
        </head>
        <body>
        
        <?php
        
        if( isset($_SESSION['user_info']) ){ // if user is logged in
        	$user_info = $_SESSION['user_info']; // get user info array
        	$full_name = $_SESSION['user_info']['data']['full_name']; // get full name
        	$username = $_SESSION['user_info']['data']['username']; // get username
        	$bio = $_SESSION['user_info']['data']['bio']; // get bio
        	$ID = $_SESSION['user_info']['data']['id']; // get bio
        	$website = $_SESSION['user_info']['data']['website']; // get bio
        	$media_count = $_SESSION['user_info']['data']['counts']['media']; // get media count
        	$followers_count = $_SESSION['user_info']['data']['counts']['followed_by']; // get followers
        	$following_count = $_SESSION['user_info']['data']['counts']['follows']; // get following
        	$profile_picture = $_SESSION['user_info']['data']['profile_picture']; // get profile picture
        	$Cook = print_r($ler);
        	?>
        		<h2>COOK <?php echo $Cook; ?>!</h2>
        	<h2>Bem vindo <?php echo $full_name; ?>!</h2>
        	<p>Seu usuário: <?php echo $username; ?></p>
        	<p>Sua Biografia: <?php echo $bio; ?></p>
        	<p>Seu website: <a href="<?php echo $website; ?>"><?php echo $website; ?></a></p>
        	<p>Total de Media: <?php echo $media_count; ?></p>
        	<p>Total de seguidores: <?php echo $followers_count; ?></p>
        	<p>Total seguindo: <?php echo $following_count; ?></p>
        	<p>Sua ID: <?php echo $ID; ?></p>
        	<p><img src="<?php echo $profile_picture; ?>"></p>
        	<p><a href="logout.php">Logout?</a></p>
        	<?php
        
        $array = array("id" => $ID ,
        	           "usuario" => $username,
        	           "foto" => $profile_picture);
        	
        	setcookie("CookiePostPrint", serialize($$array), time()+3600);
        return $array;
        }else{ // if user is not logged in
        	echo '<a href="login.php">Logar no Instagram</a>';
    	}
?>



</body>
</html>