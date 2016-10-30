package com.williams_research.pebble_shake;

import android.util.Base64;
import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;


/*************************************************
 *
 * Created by gmfinnie33 on 11/24/15.
 *
 * ___ Summary ______
 *
 * This class mimics the interaction of a server with the database.
 * [To be used while no server / database combination is available]
 * [ in the hope that this code can be transferred to the server when active]
 *
 * ___ Operations ____
 *
 * 1. Pair( USER_ID x ) - analyze the table of all traces for a partner for Phone of USER_IDs trace
 *
 *
 */

public class PseudoServer {

    //Authorization Key (ragic)
    private final String db_API_KEY = "OWF0WGdxTDRRYTZQWEhGakJBdjY5TjExTEgzbldIcjV6UmgzVHVoY1lpMlgzN2Y3MFBHS251OHNGUVNrVGdRVA==";

    // [db] index of corresponding table in database
    private final int MATCHTABLE_KEY = 2;
    private final int USERTABLE_KEY = 1;
    private final int EVENTTABLE_KEY = 3;

    private final int db_USER_FIELD = 1000006;
    private final int db_MATCH_FIELD = 1000007;
    private final int db_TIME_FIELD = 1000011;

    //Table URLs
    private URL db_events_url;
    private URL db_matches_url;

    //Valid match distance threshold
    private final int D_THRESHOLD = 10000;

    /*********************
     *
     * A PseudoServer - Constructed with
     *
     */
    public PseudoServer(){
        try {

            db_events_url = new URL("https://api.ragic.com/shake/sheets/" + EVENTTABLE_KEY + "?v=3");

        }catch( Exception pseudo_ex_0){

            pseudo_ex_0.printStackTrace();

        }
    }

    /*******************
     *
     * Function called when a match request is made (a TRACE entry appears in EVENT_TABLE)
     *
     * @param ID : The identity of the phone we wish to match.
     *
     * @return : Whether a match was found.
     *
     */
    protected boolean pair( int ID, int event_time ){

        try {

            /* [1] Open authorized connection with data table */
            HttpsURLConnection dataConn = getConnection(db_events_url, db_API_KEY, "GET");

            /* [2] Get data table */

            // [Gson objects and functions] -
            // Convert the input stream to its root JSON element
            JsonParser jp = new JsonParser();
            JsonElement root = jp.parse(new InputStreamReader((InputStream) dataConn.getContent()));

            // Get root as JSON array
            JsonArray rootArray;
            if( root.isJsonArray() ) {

                rootArray = root.getAsJsonArray();

            }else{

                Log.i("SERVER : " , "db_events_url has ROOT != JSONARRAY");
                Log.i("SERVER : " , root.toString());
                return false;
            }

            /* [3] Operate on the data table */

            // Characteristics of data table
            int numEntries = rootArray.size();

            // Characteristics of an entry
            int user = -1;
            int time = -1;
            int proximity = -1;
            Log.i("PSEUDO.pair() : ", "Searching through" + rootArray.size() +" entries in data table");

            // Characteristics of a matching entry
            int bestUser = -1;
            int bestProximity = -1;

            // Search through each entry in table
            for(int i=0 ; i < numEntries ; i++) {

                // get entry as object
                JsonObject entry = rootArray.get(i).getAsJsonObject();

                // get user
                user = entry.get("USER_ID").getAsInt();

                // get time
                time = entry.get("TIME").getAsInt();

                // get proximity
                proximity = getDistance(event_time, time);


                if( (proximity < bestProximity) && (proximity < D_THRESHOLD ) && (user != ID) ){
                    // set current as best
                    bestUser = user;
                }

            }

            // Set the match of ID to be bestUSER
            setMatch(ID, bestUser);

            // pair success if we have found a matching user
            return (bestUser != -1);


        }catch (Exception pseudo_ex ){

            //error
            pseudo_ex.printStackTrace();
            Log.i("QUERY", "a2_ex");


        }
        return false;

    }
    protected HttpsURLConnection getConnection( URL url , String key, String type) throws Exception{

        //Get bytes of authorization key
        byte[] key_bytes = key.getBytes();
        String key_encoding = Base64.encodeToString(key_bytes, Base64.DEFAULT);

        //Connect to given url
        HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
        conn.setRequestMethod(type);
        conn.setRequestProperty("Authorization", "Basic " + key_encoding);


        //Log
        Log.i("Pseudo.connect", "HTTPS Connection Response: " + conn.getResponseCode());
        Log.i( "Pseudo.connect", "HTTPS url: " + url);
        Log.i( "Pseudo.connect", "HTTPS key: " + key);
        Log.i( "Pseudo.connect", "HTTPS key: " + type);

        return conn;
    }

    /*******************
     *
     * Function to compute the distance between two events.
     * [ intention is generality. implemented now as time-proximity only.
     *
     * @param a_time : time of event 1 (user)
     * @param b_time : time of event 2 (potential match)
     *
     * @return : integer distance between two times
     *
     */
    private int getDistance( int a_time, int b_time ){

        return Math.abs(a_time - b_time);

    }

    /******
     *
     * Function to change MATCH_TABLE(a) = b
     *
     * @param a_ID : user a
     * @param b_ID : user b
     *
     */
    private void setMatch(int a_ID, int b_ID) throws Exception{

        // Encode the api key for authorization
        byte[] key_bytes = db_API_KEY.getBytes();
        String encoding = Base64.encodeToString(key_bytes, Base64.NO_WRAP);

        // Make URL
        db_matches_url = new URL("https://api.ragic.com/shake/sheets/" + MATCHTABLE_KEY + "/" + a_ID + "?v=3");

        // Connect to URL
        HttpsURLConnection postConn = (HttpsURLConnection) db_matches_url.openConnection();
        postConn.setRequestMethod("POST");
        postConn.setRequestProperty("Authorization", "Basic " + encoding);

        // form post data
        String data = db_MATCH_FIELD + "="+b_ID;

        // send post data
        try{

                DataOutputStream wr = new DataOutputStream(postConn.getOutputStream());
                wr.writeBytes(data);

        }catch( Exception pseudo_setmatch_ex0){

                pseudo_setmatch_ex0.printStackTrace();

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
                Log.i("Pseudo.setMatch() :", "[RESPONSE] :" + jsonStr.toString());

        }catch( Exception pseudo_setmatch_ex1){

                pseudo_setmatch_ex1.printStackTrace();

        }


    }


    private void addUser(){

    }


}
