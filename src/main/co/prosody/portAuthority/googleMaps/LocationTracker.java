/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package co.prosody.portAuthority.googleMaps;

import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import co.prosody.portAuthority.InvalidInputException;
import co.prosody.portAuthority.core.OutputHelper;
import co.prosody.portAuthority.util.Location;

/**
 *
 * @author Adithya
 */
public class LocationTracker {

	public static Logger log = LoggerFactory.getLogger(LocationTracker.class);

    /**
     * Case 1: Request returns list of Coordinates
     * Proceed to Step 2
     * 
     * Case 2: Unable to understand source location
     * Ask user to try again.

     * 
     * @param json returned by striking the Google maps API
     * @param limit set limit to the number of places returned by the API
     * @return
     * @throws JSONException 
     */
	protected static List<Location> getLatLngDetails(JSONObject json, int limit) throws JSONException, InvalidInputException {
    	List<Location> output = new ArrayList<>();
    	
        JSONArray results = json.getJSONArray("results");
        log.debug("JSON Results Size={}",results.length());
        if (results.length() == 0) {
            throw new InvalidInputException("No results from JSON","I did not understand the source location, " + OutputHelper.LOCATION_PROMPT);
        }
        int numResultsToReturn=Math.min(limit, results.length());
        
        
        JSONObject result;
       	JSONObject location;

        for (int i = 0; i < numResultsToReturn; i++) {
        	result = results.getJSONObject(i);
        	
        	location = result.getJSONObject("geometry").getJSONObject("location");
        	Location c = new Location(
        			result.getString("name"),
        			location.getDouble("lat"),
        			location.getDouble("lng"),
        			result.getString("formatted_address"),
        			makeList(result.getJSONArray("types")));

        	output.add(c);
        }
        return output;
    }
    
	protected static List<String> makeList(JSONArray array) throws JSONException{
    	List<String>  output = new ArrayList<String>();
    	for (int i=0;i<array.length();i++){
    		output.add(array.getString(i));
    	}
    	return output;
    }
    
}
