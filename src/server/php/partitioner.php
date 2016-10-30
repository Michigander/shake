<?
/* * partitioner.php
 *
 * Reformats a specified table using RANGE COLUMN partitioning.
 *
 * Ranges of values for each partition are specified 
 *    by providing dividers of the form,
 *       PARTITION p_i VALUES LESS THAN ( col_val_1, ... ,col_val_n )
 */

/* 
PARSE ARGUMENTS */ 
$table = $argv[1]; // table to partition

echo $table . "\n";

/* 
CONNECT TO DATABASE */
$servername="localhost";    // host

$username="mac";            // user

$password="voix_1552185";   // pass

$database="contactdb"; // name

// make connection 
$db = new mysqli( $servername , $username , $password, $database );

if( $db->connect_error ){
  
  // error 
  die("[P] - Connection failed: " . $db->connect_error);

}

// check that table exists 
if( $db->query("SHOW TABLES LIKE 'events'")->num_rows == 0){

  // no table
  $db->close();
  die("[P] - Table DNE \n");
  
}  

/* FORM SQL STATEMENT */
/* 
Split into four partitions by time, latitude, longitude values 

Williams College location = 42.7120 , -73.2037 
                          = 42.7120 N, 73.2037 W

First partition: 
    All entries which occur past the time of partitioning, 
    and outside Williams College campus to the South and West 

Second partition: 
    All entries which  occur past the time of partitioning, 
    and within Williams College campus 

Third partition:
    All entries which occur past the time of partitioning, 
    and outside Williams College to the North and East 

Fourth partition: 
    All entries which occurred before time of partitioning,
    at any location  */

$current_time = time(); // current unix time 

$willy_lat_max = 43000; // latitude range 
$willy_lat_min = 42000;

$willy_lon_max = -73000;// longitude range
$willy_lon_min = -74000;

$sql = "ALTER TABLE $table PARTITION BY RANGE COLUMNS(latitude,longitude) (
PARTITION p0 VALUES LESS THAN ($willy_lat_min, $willy_lon_min),
PARTITION p1 VALUES LESS THAN ($willy_lat_max, $willy_lon_max),
PARTITION p2 VALUES LESS THAN (MAXVALUE,MAXVALUE)
)";

// execute 
if( $db->query($sql) === TRUE ) {

  // success 
  echo "[P] - table successfully partitioned\n";

} else {

  // error
  printf("[P] - Partitioning failed: %s\n", $db->error);

}

// close 
$db->close();
?>