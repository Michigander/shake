<?
/* matchmaker.php 
   
   Fulfill a request to pair from a user. 
   
   Partner is closest via distance function,
   {
     abs( user_location - other_location ) < 10 
     abs( user_time - other_time ) < 10 
   } 

*/

/* 
   MySQL login */

$servername="localhost";    // host 

$username="mac";            // user 

$password="voix_1552185";   // pass 

$database="contactdb";      // name

/* 
   Connect */
$mysql = new mysqli($servername,$username,$password,$database);

if( $mysql->connect_error ){
  
  //error 
  die("[M] || Error while trying to connect: " . $mysql->connect_error );

}

/* 
   Get request data */
 
$client_id = intval($_GET["USER"]);

$client_lon = intval($_GET["LON"]);

$client_lat = intval($_GET["LAT"]);

$client_time = intval($_GET["TIME"]);

/* 
  Insert */

// check that table exists
if( $mysql->query("SHOW TABLES LIKE 'events'")->num_rows == 0){

  // no table
  $mysql->close();
  die("[MM] || Table DNE \n");

}

// make statement  
$sql_in = " INSERT INTO events VALUES ($client_id,$client_time,$client_lat,$client_lon)";

// execute statement 
if( $mysql->query($sql_in) === TRUE ) {

  // success
  echo "[MM] || Data entered successfully";

}else{

  // error
  die( "[MM] || Error executing sql_in: " . $mysql->error );

}

/* 
  SELECT */

// make statement 
$sql_get = "SELECT *
            FROM events
             WHERE (id != $client_id)
             AND (ABS(time - $client_time) < 10)
              AND (ABS(latitude - $client_lat) < 100)
               AND (ABS(longitude - $client_lon) < 100)";

// execute statement
if( ($result = $mysql->query($sql_get)) === TRUE ) {

  // success
  echo "[MM] || Locale found successfully";

}else{
  
  // error 
  die( "[MM] || Error executing sql_get: " . $mysql->error );

}

/* 
   PAIR */

$partner = array("id"=>0,"time"=>0,"latitude"=>0,"longitude"=>0);
$score = 0;

// iterate through result group
while( $other = $result->fetch_assoc()){
  
  // tally 
  $other_score = 0;

  // check each value 
  for($other as $type => $stat ){

    // increment tally if better than current partner
    if(abs($_GET[$type] - $stat) < abs($_GET[$type] - $partner[$type])){
      
      $score++;

    }

  }
  
  // replace 
  if($other_score >= $score){

    $partner = $other;

  }

}

/*
  Log */

// $log_file = $client_id . "_" . $client_time . ".txt"; 

// $log_content = implode(" " , $_GET);

// file_put_contents( $log_file , $log_content );

/* 
   Send */
header('content-type: application/json');

$response = array('PAIRING' => $partner['id']);

echo json_encode( $response ); 

?>