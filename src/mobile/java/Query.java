package com.williams_research.pebble_shake;

import android.os.StrictMode;
import android.util.Base64;
import android.util.JsonReader;
import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import javax.net.ssl.HttpsURLConnection;

/**
 * Created by gmfinnie33 on 11/10/15.
 *
 * A match query. We query an online database for a matching session.
 *  Returns:
 *  - Whether a matching session exists
 *  - The contact information of a matching trace's actor
 *  -
 */
public class Query {

    /*******************************************\
     *
     * Keys and IDs **/

    // [db] api key
    private final String db_API_KEY = "OWF0WGdxTDRRYTZQWEhGakJBdjY5TjExTEgzbldIcjV6UmgzVHVoY1lpMlgzN2Y3MFBHS251OHNGUVNrVGdRVA==";

    // [db] index of corresponding table in database
    private final int MATCHTABLE_KEY = 2;
    private final int USERTABLE_KEY = 1;
    private final int EVENTTABLE_KEY = 3;

    // [db] ID
    private int USER_ID = 0;

    // [db] URL
    private URL db_matches_url;
    private URL db_users_url;
    private URL db_events_url;

    private final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64) Chrome/30.0.1599.66";

    // [db] Field IDs in MATCH_TABLE
    // [ the wonderful RAGIC informed me that I would need these ...
    //   ... I have not yet found why]
    private final String db_USER_FIELD = "1000006";
    private final String db_MATCH_FIELD = "1000007";
    private final String db_TIME_FIELD = "1000011";

    /*******************************************\
     *
     * Data of Event **/

    // [p] gesture data
    private int[][] q_gesture;

    // [p] signature gesture is matched with
    private int[][] q_signature;

    // [db] phone's entry in MATCH_TABLE
    private String[] db_match_info;

    /*******************************************\
     *
     *  **/

    //
    private int TIME;
    private Event EVENT;
    private PseudoServer PS;
    /*******************************************\
     *
     * A Query is built with the trace (Accelerometer & Time data) of a local pebble session.
     *  We "Query" the database to find matching trace from another user.
     *  We return the Contact Information of that match. This data then needs to be
     *  formed into a phone contact via makeContact();
     *
     *
    protected Query( int[][] dat, int[][] sig ){

        // [!to_do!] now - dummy server
        TIME = 0;

        // set data
        q_gesture = dat;
        q_signature = sig;

        try{

            db_matches_url = new URL("https://api.ragic.com/shake/sheets/"+MATCHTABLE_KEY+"/"+USER_ID+"?v=3");
            db_users_url = new URL("https://api.ragic.com/shake/sheets/"+USERTABLE_KEY+"?v=3");
            db_events_url = new URL("https://api.ragic.com/shake/sheets/"+EVENTTABLE_KEY+"?v=3");

        }catch(Exception q_ex_0){

        }

    }
*/
    protected Query( Event ev, PseudoServer ps ){

        // set time of query
        EVENT = ev;
        TIME = ev.getTime();
        PS = ps;

        // set URLs
        try{

            db_matches_url = new URL("https://api.ragic.com/shake/sheets/"+MATCHTABLE_KEY+"/"+USER_ID+"?v=3");
            db_users_url = new URL("https://api.ragic.com/shake/sheets/"+USERTABLE_KEY+"?v=3");
            db_events_url = new URL("https://api.ragic.com/shake/sheets/"+EVENTTABLE_KEY+"/0?v=3");

        }catch(Exception q_ex_0){

        }

        // Allow network I/O on main thread [Intend to change later]
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

    }

    /***
     *
     * Function to post an event to DATA_TABLE
     *
     */
    protected void post() throws Exception{

        //Get bytes of authorization key
        byte[] key_bytes = db_API_KEY.getBytes();
        String key_encoding = Base64.encodeToString(key_bytes, Base64.DEFAULT);

        //Connect to given url
        Log.i("Query.post()", "CONNECTING");
        HttpsURLConnection postConn = (HttpsURLConnection) db_events_url.openConnection();
        postConn.setRequestProperty("Authorization", "Basic " + key_encoding);
        postConn.setRequestMethod("POST");


        // form post data
        String data = db_TIME_FIELD + "="+TIME;

        // send post data
        try{

            DataOutputStream wr = new DataOutputStream(postConn.getOutputStream());
            wr.writeBytes(data);

        }catch( Exception query_post_ex0){

            query_post_ex0.printStackTrace();

        }

        // get response
        try{

            // [perhaps do this with gson]
            BufferedReader br = new BufferedReader(new InputStreamReader(postConn.getInputStream()));
            StringBuilder jsonStr = new StringBuilder();
            String inputLine;

            while ((inputLine = br.readLine()) != null) {
                jsonStr.append(inputLine);
            }

            // log response
            Log.i("Query.post() :", "[RESPONSE] :" + jsonStr.toString());

        }catch( Exception query_post_ex1){

            query_post_ex1.printStackTrace();

        }

    }

    /*******************************************************\
     *
     * Function to send a request to database. This updates the database MATCH_TABLE
     *
     */
    protected boolean hasMatch() {
        // Determine match ID from table
        int matchID = getMatchID();

        // Return if match was found
        if( matchID != -1 ) {

            // Load the contact information from USER_TABLE
            db_match_info = getUserInfo(matchID);

            // success
            return true;
        }

        // No match
        return false;
    }


    /*******************************************************\
     *
     * Function to get the match ID in MATCH_TABLE
     *
     */
    protected int getMatchID(){

        // Send query
         try {

             HttpsURLConnection conn = getAuthorizedConn( db_matches_url);
             Log.i("response code", "" + conn.getResponseCode());

            // Buffer the result into a string
            BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));

             StringBuilder sb = new StringBuilder();

             String line;

             while ((line = rd.readLine()) != null) {

                        sb.append(line);

             }

             rd.close();

             conn.disconnect();

             Log.i("getMatchID() : ", sb.toString());


        }catch(Exception gmi_ex_1) {
            gmi_ex_1.printStackTrace();
             Log.i("QUERY","getMatchID() : id_ex_0");

        }

        // error
        Log.i("QUERY", "getMatchID() : no match ID found");

        return -1;

    }

    /*******************************************************\
     *
     * Function to get the contact information of a match in USER_TABLE
     *
     */
    protected String[] getUserInfo(int ID) {

        //Contact Information : [name, phone_number]
        String[] contact_info = new String[2];

        try {

            //Build URL:
            // user indices start at 0, table indices at 1.
            db_users_url = new URL("api.ragic.com/shake/sheets/"+USERTABLE_KEY+"/"+(ID+1));

            // authorize
            authorize(db_users_url);

            // get secure, authorized connection
            HttpURLConnection userConn = (HttpURLConnection) db_users_url.openConnection();

            // set properties of connection
            userConn.setRequestMethod("GET");
            userConn.setRequestProperty("User-Agent", USER_AGENT);
            userConn.setRequestProperty("charset", "UTF-8");

            // parse the JSON object
            JsonReader userFeed = new JsonReader(new InputStreamReader(userConn.getInputStream(),"UTF-8"));
            userFeed.beginObject();

            while(userFeed.hasNext()){

                String field_name = userFeed.nextName();

                if(field_name.equals("NAME")){

                    // add name
                    contact_info[0] = userFeed.nextString();

                }else if(field_name.equals("PHONE")){

                    // add number
                    contact_info[1] = userFeed.nextString();

                }else{

                    // skip
                    userFeed.skipValue();
                }

            }

            // end JSON parse
            userFeed.endObject();
            userFeed.close();
            userConn.disconnect();

        }catch(Exception gui_ex_0){

            Log.i("QUERY", "getUserInfo : gui_ex_0 ");

        }

        return contact_info;
    }

    /*******************************************************\
    *
    * Function to get the contact information of a match in USER_TABLE
    *
    */
    protected String[] getContactInfo(){

        return db_match_info;

    }

    /*******************************************************\
     * Function to authorize connection with online database
     *
     */
    private void authorize(URL port){

        // encode the api key
        byte[] key_bytes = db_API_KEY.getBytes();
        String encoding = Base64.encodeToString(key_bytes, Base64.NO_WRAP);

        // set up connection
        try {

            HttpsURLConnection authConn = (HttpsURLConnection) port.openConnection();

            authConn.setRequestProperty("Authorization", "Basic " + encoding);

            authConn.connect();

        }catch (Exception auth_ex){
            Log.i("QUERY","a1_ex");
        }

    }

    private HttpsURLConnection getAuthorizedConn(URL port){

        // encode the api key
        byte[] key_bytes = db_API_KEY.getBytes();


        String encoding = Base64.encodeToString(key_bytes, Base64.DEFAULT);

        // set up connection
        try {

            HttpsURLConnection authConn = (HttpsURLConnection) port.openConnection();

            authConn.setRequestProperty("Authorization", "Basic " + encoding);

            return authConn;

        }catch (Exception auth_ex){

            //error
            Log.i("QUERY","a2_ex");

        }

        // error
        return null;
    }


}
