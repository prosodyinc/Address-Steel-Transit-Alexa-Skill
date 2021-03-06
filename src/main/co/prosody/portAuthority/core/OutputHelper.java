package co.prosody.portAuthority.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.org.apache.regexp.internal.recompile;

import co.prosody.portAuthority.GetNextBusSpeechlet;
import co.prosody.portAuthority.ResponseObject;
import co.prosody.portAuthority.api.Message;
import co.prosody.portAuthority.googleMaps.GoogleMaps;
import co.prosody.portAuthority.storage.PaInputData;
import co.prosody.portAuthority.util.Navigation;
import co.prosody.portAuthority.util.Result;


public class OutputHelper {
	
	// CONFIGURE ME!
	public static final String AUDIO_WELCOME = "<audio src=\"https://s3.amazonaws.com/maya-audio/ppa_welcome.mp3\" />";
	public static final String AUDIO_FAILURE = "<audio src=\"https://s3.amazonaws.com/maya-audio/ppa_failure.mp3\" />";
	public static final String AUDIO_SUCCESS = "<audio src=\"https://s3.amazonaws.com/maya-audio/ppa_success.mp3\" />";
    public static final String S3_BUCKET = System.getenv("S3_BUCKET"); //S3 Bucket name
    public static final String IMG_FOLDER = "image"; //S3 Folder name
    
    
	public final static Logger LOGGER = LoggerFactory.getLogger("OutputHelper");
	
	public static String SPEECH_WELCOME = "Welcome to "+GetNextBusSpeechlet.INVOCATION_NAME;
	
	//TODO: add markers into conversation
	public static final String CHANGE_MARKER="By the way, ";
	public static final String SUCCESS_MARKER="okay, ";
	public static final String FAILED_MARKER="oh, ";
	
	public static final String ROUTE_PROMPT = " Which bus line would you like arrival information for?";
	public static final String HELP_ROUTE= "The Bus Line is usually a number, like sixty-seven, or a number and a letter, "
			+ "like the seventy-one B , If you don't know what bus line you want, say, cancel, and go look it up on Google Maps";
	
	public static final String LOCATION_PROMPT = "Where are you now?";
	public static final String HELP_LOCATION= "You can say a street address where you are, or a landmark near your bus stop , "
			+ GetNextBusSpeechlet.INVOCATION_NAME+ " will figure out the closest stop to the location you give.";
	
	
	public static final String DIRECTION_PROMPT = "In which direction are you <w role=\"ivona:NN\">traveling</w>?";
	public static final String HELP_DIRECTION= "For busses headed <emphasis>towards</emphasis> "
			+ "<phoneme alphabet=\"x-sampa\" ph=\"dAn tAn\">downtown</phoneme> ,"
			+ "you can say, <phoneme alphabet=\"x-sampa\" ph=\"InbaUnd\">Inbound</phoneme> ,"
			+ "or, for busses headed <emphasis>away</emphasis> from the city, say, Outbound";
	
	public static final String HELP_INTENT = "Use a complete sentence, like ,  I am currently outside Gateway Three";
	
	/**
	 * Location Name, StopName
	 */
	public static final String LOCATION_SPEECH="The nearest stop to %s is %s.\n";
	/**
	 * StopName
	 */
	public static final String BUSSTOP_SPEECH=" At %s, ";
	
	
	////RESULTS////
	/**
	 * Speech fragment if there are no prediction results for an "All Routes" request
	 * Format with Direction, BusStopName
	 */
	public static final String NO_ALL_ROUTES_SPEECH=" No %s busses are expected at %s in the next 30 minutes. ";
	
	/**
	 * Speech fragment if there are no prediction results for an "All Routes" request
	 * Format with Direction, RouteID, and BusStopName
	 */
	public static final String NO_SINGLE_ROUTE_SPEECH=" No %s, %s is expected at %s in the next 30 minutes. ";
	
	/**
	 * Speech fragment for first prediction result
	 * Format with RouteID, Prediction Time
	 */
	public static final String FIRST_RESULT_SPEECH=" The %s is arriving ";
	
	
	/**
	 * Speech fragment with instructions to hear all routes.
	 */
	public static final String HELP_ALL_ROUTES_SPEECH=CHANGE_MARKER+"to hear predictions for all routes that stop there, say, Alexa, ask "+GetNextBusSpeechlet.INVOCATION_NAME+" for All Routes.";

	/**
	 * Speech fragment with generic instructions .
	 */
	public static final String HELP_SPEECH=GetNextBusSpeechlet.INVOCATION_NAME+" will tell you when the next bus is coming if you provide it a bus line, direction, and location near your bus stop.";
	/**
	 * Speech fragment for stopping or canceling.
	 */
	public static final String STOP_SPEECH="Oh? OK";
	
	public static final String TOO_FAR_SPEECH= "%s is over a mile away from the %s I found. Please say the location again and be more specific. ";
	
	public static ResponseObject getWelcomeResponse(){
		String output=AUDIO_WELCOME+" "+SPEECH_WELCOME + ROUTE_PROMPT;
		return new ResponseObject("", output);
	}
	
	public static ResponseObject getHelpResponse(){
		String output=AUDIO_WELCOME+" "+HELP_SPEECH + " " + ROUTE_PROMPT;
		ResponseObject response = new ResponseObject("", output);
		return response;
	}
	
	public static ResponseObject getStopResponse(){
		String output=STOP_SPEECH;
		ResponseObject response = new ResponseObject("", output);
		return response;
	}
	
	//No Speech Output
	/**
	 * Returns a ResponseObject response that indicates there are no buses (or no particular bus) arriving at the given stop within the next 30 minutes.
	 * @param inputData The data object of the conversation
	 * @param skillContext The context of the conversation
	 * @return The response indicating no buses are due to arrive
	 */
	public static ResponseObject getNoResponse(PaInputData inputData, SkillContext skillContext) {
		String textOutput="";
		if (skillContext.getSayLocationInResponse()){
			textOutput=String.format(LOCATION_SPEECH, inputData.getLocationName(), inputData.getStopName()); 
		}

		if (skillContext.isAllRoutes()){
			textOutput+=String.format(NO_ALL_ROUTES_SPEECH, inputData.getDirection().toLowerCase(), inputData.getStopName());
		} else {
			textOutput+=String.format(NO_SINGLE_ROUTE_SPEECH, inputData.getDirection().toLowerCase(), inputData.getRouteID() , inputData.getStopName());
		}

		if ((skillContext.getNeedsMoreHelp())&&(!skillContext.isAllRoutes())){
			textOutput+=HELP_ALL_ROUTES_SPEECH;
			
		}
		String speechOutput=textOutput.replaceAll("(\r\n|\n\r|\n)","<break time=\".25s\" />");
		return new ResponseObject(textOutput, speechOutput);

	}

	/**
	 * Lists off each bus arriving at a given stop.
	 * Builds a response given a list of bus routes and times arriving at a particular stop. 
	 * Formats the response so that the times for each bus route arriving at this stop are listed off.
	 * @param inputData The data object of the conversation
	 * @param results The route number and estimated time of each bus arriving at this stop in the next 30 minutes.
	 * @param skillContext The context of the conversation
	 * @return SSML speechOutput and a regular text output, returned in array 
	 */
	public static ResponseObject generateResponse(PaInputData inputData, ArrayList<Result> results, SkillContext skillContext) {
		String textOutput = "";
		
		if (skillContext.getSayLocationInResponse()){
			if (!skillContext.isAllRoutes()){
				textOutput+=skillContext.getFeedbackText() + ".\n ";
			}
			textOutput+=String.format(LOCATION_SPEECH, inputData.getLocationName(), inputData.getStopName());
		} else {
			textOutput+=String.format(BUSSTOP_SPEECH, inputData.getStopName()) ;
		}
		
		//TODO: Collect Route responses together, but Return the first bus first. 
		Collections.sort(results);
		
		
		//getResponseObjectForResults
		String routeID;
		String prevRouteID=null;
		boolean isNewRoute;
		
		for (int i = 0; i < results.size(); i++) {
			// P1
			routeID=results.get(i).getRoute();
			
			//prevRouteID is null the first time through
			if (prevRouteID ==null){
				isNewRoute=true;
			} else {
				isNewRoute=(!routeID.equals(prevRouteID));
			}

			// if it is the first prediction for this route
			//"The P1 is arriving ... "
			if (isNewRoute) { 
				textOutput+=(String.format(FIRST_RESULT_SPEECH,routeID));
			
			// if it is the last prediction for this route	
			} else if ((i >= results.size() - 1) || (!results.get(i + 1).getRoute().equals(routeID))){
				textOutput+=", and ";
			
			// if this prediction is somewhere in the middle	
			} else {
				textOutput+=", ";
			}
			
			textOutput+=results.get(i).getPredictionString();

			// if it is the last prediction for this route	
			if ((i >= results.size() - 1) || (!results.get(i + 1).getRoute().equals(routeID))){
				textOutput+=". \n";	
			}
			prevRouteID=routeID;
		}
		
		String speechOutput=textOutput.replaceAll("(\r\n|\n\r|\n)","<break time=\".25s\" />");
		
		return new ResponseObject(textOutput, speechOutput);
		//TODO: maybe the skill context should get these values, instead of having them returned...
	}
        
	/**
	 * Uploads a picture of the closest path from the location provided by the user to the nearest bus stop.
	 * Uploaded to an S3 bucket.
	 * @param locationLat The latitude of the user's location
	 * @param locationLon The longitude of the user's location
	 * @param stopLat The latitude of the nearest stop
	 * @param stopLon The longitude of the nearest stop
	 * @return A navigation object containing the image URL and directions from the user's location to the nearest stop
	 * @throws IOException
	 * @throws JSONException
	 * @throws Exception
	 */
    public static Navigation buildNavigation(String locationLat, String locationLon, double stopLat, double stopLon) throws IOException, JSONException, Exception{	
    	Navigation navigation = new Navigation();
        
    	String directions = GoogleMaps.generateDirections(locationLat, locationLon, stopLat, stopLon);
    	
    	String image = GoogleMaps.generateImageURL(locationLat, locationLon, stopLat, stopLon);
        image = image.substring(0, image.length() -1); //Remove the last '|'
        
        //Set image Name
        String imageName = locationLat+locationLon+stopLat+stopLon;
        imageName = imageName.replaceAll("\\.", "");
        
        //Upload image on S3
        ImageUploader.uploadImage(image, imageName, IMG_FOLDER, S3_BUCKET);
        LOGGER.info("UPLOAD IMAGE SUCCESSFUL WITH NAME: "+imageName);
        
        //Set instructions and S3 image link to navigation object
        navigation.setInstructions(directions);
        navigation.setImage("https://s3.amazonaws.com/"+S3_BUCKET+"/"+IMG_FOLDER+"/"+ imageName+".png");
        LOGGER.info("SET IMAGE SUCCESSFUL");
        //LOGGER.info("IMAGE URL={}",image);
        return navigation;
    }

    /**
     * Extracts relevant routeID and ETA information from the Messages returned by the TrueTimeAPI.
     * If there are no Messages, or there is an error message, return null.
     * @param messages The Messages returned by the TrueTime API
     * @param skillContext The conversation context
     * @return A List of Results
     */
    public static ArrayList<Result> getResults(List<Message> messages){
    	ArrayList<Result> results = new ArrayList<Result>();
		if (messages.size() == 0 || messages.get(0).getMessageType().equals(Message.ERROR)){
    		return results; //return an empty ArrayList
    	}

		for (int i = 0; i < messages.size(); i++) {
			results.add(new Result(messages.get(i).getRouteID(), messages.get(i).getEstimate()));
		}
		return results;

		
    }
    
    
    /**
     * Returns a response that indicates there has been an issue connecting to one of the APIs
     * @param failureLabel The API that couldn't establish connection
     * @return The failure reponse
     */
	public static ResponseObject getAPIFailureResponse(String failureLabel) {
		String message = ("There has been a problem connecting to " + failureLabel + ". I'll let the developers know.");
		ResponseObject response = new ResponseObject(message, message);
		return response;
	}
	
	public static ResponseObject getStopTooFarResponse(PaInputData data, SkillContext skillContext) {
		String message=String.format(TOO_FAR_SPEECH, data.getStopName(), data.getLocationName());
		return new ResponseObject(message, message);
	}
	
	public static void main(String[] args){
		PaInputData inputData = PaInputData.newInstance("MYID");
		inputData.setLocationName("Highmark Building"); 
		inputData.setStopName("Fifth Ave at Wood St");
		
		SkillContext sc=new SkillContext();
		sc.setAllRoutes(true);
		sc.setSayLocationInResponse(true);
		
		ArrayList<Result> results = new ArrayList<Result>();
		results.add(new Result("61A", 2));
		results.add(new Result("71B", 7));
		results.add(new Result("61A", 21));
		results.add(new Result("71A", 11));
		results.add(new Result("71B", 1));
		results.add(new Result("61A", 9));
		
		ResponseObject ro= OutputHelper.generateResponse(inputData, results, sc);
		System.out.println(ro.getTextOutput());
		System.out.println(ro.getSpeechOutput());
	}

}
