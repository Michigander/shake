/* 
Relay - 

Receive notification from watch.

Assemble data. 

Send to Server as pairing request. 

Return result to watch.

Continue. 

*/

var pairing = null;

/*

Listen for launch.

*/
Pebble.addEventListener('ready', function(e) {
  
  // USER = load from localStorage  
  // run login here
    console.log('PebbleKit JS ready!');
  
  // report launch
    Pebble.sendAppMessage({'JSREADY':1});
  
});

/*

Listen for message from watch

*/
Pebble.addEventListener('appmessage', function(e) {

  // Get message's dictionary  
    var dict = e.payload;
    console.log('Got message: ' + JSON.stringify(dict));

  /* Want to get pairing -
   *  need relevant data. */

  /* GPS - from phone service */
  // Upon success, 
    function locSuccess(pos){
    
    // Get position,
	var lat = pos.coords.latitude;  // latitude
	var lon = pos.coords.longitude; // longitude
	console.log('lat= ' + lat + 'lon= '+ lon);   
    
    // Get time,
	var date = new Date();
	var t = date.getTime();
    
    // RUN TEST
	runTest();
      
    }
  
  // Upon failure,
    function locError(err){

	console.log('location error ('+err.code+'):'+err.message);
  
    }
  
  // Data options,
    var options = {

	enableHighAccuracy: true,
    
	maximumAge: 10000,
    
	timeout: 10000

    };
  
  // Request GPS, 
    navigator.geolocation.getCurrentPosition(locSuccess,locError,options);
  
});

function sendRequest(url , callback . data){

  
}

/* II  . */
function runTest(){
  
  /* --- II  . CONTACT SYSNET0 ( TO SERVER ) --- */ 
  
  // Http variables,
    var test_data = 'USER='+'test_user'+'&LAT='+'test_lat'+'&LON='+'test_lon'+'&TIME='+'test_time'; 
    var method = 'GET';
  
    var url = 'sysnet0.cs.williams.edu/matchmaker.php';
  
    var request = new XMLHttpRequest();
  
  // Build and send request 
    request.open(method, url, true);
  
  // request.setRequestHeader("Content-type",);
    request.setRequestHeader("Content-length", test_data.length);
    request.setRequestHeader("Connection","close");
   
  // Callback for successful request
    request.onload = function(){

    // Read result
	try{
        
	    var json = JSON.parse(this.responseText);
	    pairing = json.main.PAIRING;
    
	}catch(err){
      
	    pairing = 'ERROR';
	    console.log('Error in parsing JSON');
      
	}
	console.log('Response: '+this.responseText);  
	console.log('Code: ' + this.status);  
        
    };

  // Send request
    request.open('GET', "matchmaker.php");
    request.send();
  
  /* --- III . Send Result to Sensor ( TO WATCH ) --- */
  
  // Assemble message 
    var msg = {
  
	'PAIRING': pairing
  
    };

  // Send to watch
    Pebble.sendAppMessage(msg, function(e) { 
  
	console.log('Message sent successfully: ' + JSON.stringify(msg));
  
    }, function(e) {
  
	console.log('Message failed: ' + JSON.stringify(e));
  
    });
}
    

