<?php

function getdarausingcurl($method, $url, $header, $data, $json){
    if( $method == 1 ){
        $method_type = 1; // 1 = POST
    }else{
        $method_type = 0; // 0 = GET
    }
 
    $curl = curl_init();
    curl_setopt($curl, CURLOPT_URL, $url);
    curl_setopt($curl, CURLOPT_RETURNTRANSFER, 1);
    curl_setopt($curl, CURLOPT_SSL_VERIFYPEER, 0);
    curl_setopt($curl, CURLOPT_HEADER, 0);

    if( $header !== 0 ){
        curl_setopt($curl, CURLOPT_HTTPHEADER, $header);
    }

    curl_setopt($curl, CURLOPT_POST, $method_type);
 
    if( $data !== 0 ){
        curl_setopt($curl, CURLOPT_POSTFIELDS, $data);
    }
 
    $response = curl_exec($curl);

    if( $json == 0 ){
    	$json = $response;
    }else{
    	$json = json_decode($response, true);
    }

    curl_close($curl);
 
    return $json;
}

?>
<?php

session_start();
ob_start();


if( isset($_SESSION['user_info']) or !isset($_SESSION['login']) ){ // if user is logged in

header("location: index.php"); // redirect user to index page
return false;
	
}



include 'config.php'; // include app data

$code = $_GET['code'];

/* Get User Access Token */

$method = 1; // method = 1, because we want POST method

$url = "https://api.instagram.com/oauth/access_token";

$header = 0; // header = 0, because we do not have header

$data = array(
			"client_id" => $client_id,
			"client_secret" => $client_secret,
			"redirect_uri" => $redirect_uri,
			"grant_type" => "authorization_code",
			"code" => $code
		);

$json = 1; // json = 1, because we want JSON response

$get_access_token = getdarausingcurl($method, $url, $header, $data, $json);

$access_token = $get_access_token['access_token'];

$get = file_get_contents("https://api.instagram.com/v1/users/self/?access_token=$access_token");

$json = json_decode($get, true);

$_SESSION['user_info'] = $json; // save user info in session

header("location: index.php"); // redirect user to index page


?>