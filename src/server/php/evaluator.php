<?

/* evaluator.php -
   run tests to evaluate the performance of the system.
   currently implemented to output the .csv files,

        file_name   | partition type        |# zones
        ptest_0.csv | no partitioning       |1
	ptest_1.csv | partition-by-location |2 
	ptest_2.csv | partition-by-location |3

 */

/* CONNECT TO DATABASE */

// Profile
$host = 'localhost';
$user = 'mac';
$pass = 'voix_1552185';
$name = 'testdb';

// Connect 
$db = new mysqli( $host , $user , $pass , $name );
if($db->connect_error){
  // error 
  die("connection failure" . $db->connect_error);
}

// Create tables

$tables = array( 'ptest0' , 'ptest1' ); // names 

for( $i = 0 ; $i < count($tables) ; $i++ ){

  // create sql
  $creation_sql = "CREATE TABLE $tables[$i] (user INT, time INT, latitude INT, longitude INT)";

  // execute sql
  if( $db->query($creation_sql) === TRUE ){
    
    // success 
    printf( "table %d created", $i ); 

  }else{

    // error 
    printf( "[E] || table %d ERROR\n", $i);
    die("[E] || error message" . $db->error);

  }

}

// Partition ptest1 
$partition_sql = "ALTER TABLE ptest1 PARTITION BY RANGE COLUMNS (latitude, longitude)(
PARTITION BY VALUE LESS THAN (42000,-74000),
PARTITION BY VALUE LESS THAN (43000,-73000),
PARTITION BY VALUE LESS THAN (MAXVALUE,MAXVALUE)
)";

if( $db->query($partition_sql) === TRUE ){

  //success 
  echo "[E] || successfully partitioned ptest1";

}else {

  // failure 
  die("[E] || error partitioning ptest1 : " . $db->error); 

}

/* SIMULATIONS */

/* 
   A test is a simulation of roughly 2 minutes of system activity.

   Each simulation contains as many pairing requests 
    as the value of the variable $TRAFFIC. 

   A pair has the form,
       (a , a_time , a_lat , a_lon )
       (b , b_time, b_lat, b_lon)

   Possible values of time, 
       ($SEED_TIME - 60) < time < ($SEED_TIME + 60)

   Possible values of location,
       41000 < latitude < 44000 
      =75000 < longitude < -72000 

   This defines a grid around Williamstown.

   We skew the data to be 100% actual matches.

   To simulate this scenario we,
     I. Generate time, longitude, latitude offset 
      FOR THE PAIR 
     
     II.Generate time, longitude, latitude offset
      FOR THE INDIVIDUAL 

   In this way we have a pairing. 

   Adding "noise" requests will be important to further evaluation.  

*/

/* 1. LOW TRAFFIC */ 

$TRAFFIC = 5; //# pairs 

$SEED_TIME = time(); //unix time

$data = array();
//$table1_data = array();
//$table2_data = array();
  
/* CREATE SIMULATED DATA */
for($pair_num = 0 ; $pair_num < $TRAFFIC ; $pair_num++){
  
  /* Create a Pair */
  
  // usernames
  $a_user = $pair_num;
  $b_user = $pair_num * 10000;

  // Time

  // offset of pair
  $pair_t_offset = rand(-60 , 60);
  
  // offset of individuals
  $a_t_offset = $pair_t_offset + rand(-5,5);
  $b_t_offset = $pair_t_offset + rand(-5,5);

  // time of individuals
  $a_time = $SEED_TIME + $a_t_offset;
  $b_time = $SEED_TIME + $b_t_offset;

  // Location 
  $pair_lat = rand(41000,44000);
  $pair_lon = rand(-75000, -72000);
  
  /* In williamstown, offsets in latitude 
     and longitude are roughly the width of route 2 */
  $a_lat_offset = rand(-100,100);  
  $b_lat_offset = rand(-100,100);

  $a_lon_offset = rand(-100,100);
  $b_lon_offset = rand(-100,100);
  
  // lat and lon of individuals
  $a_lat = pair_lat + a_lat_offset;
  $a_lon = pair_lon + a_lon_offset;

  $b_lat = pair_lat + b_lat_offset;
  $b_lon = pair_lon + b_lon_offset;

  // record 
  $a =  array("a_user" => $a_user , "a_time" => $a_time , "a_lat" => $a_lat$ , "a_lon" => $a_lon);
  $b =  array("b_user" => $b_user , "b_time" => $b_time , "b_lat" => $b_lat$ , "b_lon" => $b_lon);

  array_push($data , $a , $b);
  //  array_push(table2_data , $a , $b);
  
}

/* RUN SIMULATION */

// define compare function for sorting
function compare_time( $t1 , $t2 ){
  if( $t1 < $t2 ){
    
    return -1;

  }else if( $t1 > $t2 ){

    return 1;

  }
  
  return 0;
}

// order-by-time 
usort($data , 'compare_time');

// simulate non partitioned system 
while(count($data) > 0){

  // search for open thread 
  for($i = 0 ; $i < count($thread_pool) ; i++){

    if( !($thread_pool[$i]->$active) ){
      
      $result = (int)$thread_pool[$i]->listen();
      
      $new_thread = new Threader($command, );

    }
  }
}

for( $i = 0 ; $i < count($data) ; $i++ ){
  
  $request = $data[$i];
  
  $command = "~/Research/Contact/live -f matchmaker.php";

  $args = "-test " . $request

}


/* we populate the tables based on a TRAFFIC value


?>