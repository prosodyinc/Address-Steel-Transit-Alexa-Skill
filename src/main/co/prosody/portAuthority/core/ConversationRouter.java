package co.prosody.portAuthority.core;

import java.io.IOException;
import java.util.List;

import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import co.prosody.portAuthority.InvalidInputException;
import co.prosody.portAuthority.storage.PaInputData;
import co.prosody.portAuthority.util.Stop;


public class ConversationRouter {
	private static Logger log = LoggerFactory.getLogger(DataHelper.class);

	
	/**
	 * First calculates the nearest stop given the provided location, and then checks to see if it is within a mile of the location.
	 * If it is within a mile, set the skill context ask no further questions.
	 * @param data
	 * @param skillContext
	 * @throws InvalidInputException
	 */
	public static void validateStop(PaInputData data, SkillContext skillContext) throws InvalidInputException{
		if (data.getStopID() == null) {
			Stop stop = DataHelper.getNearestStop(data, skillContext);
			data.setStop(stop);
			if (stop.isToFar()) {
				data.setStopID(null);
				skillContext.setSayLocationInResponse(true);
				skillContext.setFeedbackText(OutputHelper.getStopTooFarResponse(data, skillContext).getSpeechOutput());
				skillContext.setLastQuestion(OutputHelper.LOCATION_PROMPT);
			} else {
				skillContext.setSayLocationInResponse(true);
				skillContext.setAdditionalQuestions(false); // we have
															// everything we
															// need!
			}
		} else {
			skillContext.setAdditionalQuestions(false); // we have everything we
														// need!
		}
	}
	
	/**
	 * This method will first check if any given slot has not been provided a value by the user.
	 * If a raw slot value is present, the method will then attempt to validate each of the slot parameter, starting with the routeID.
	 * If any of the slot parameters do not pass validation, the user will be re-prompted for a correct value.
	 * @param data
	 * @param skillContext
	 * @return
	 * @throws InvalidInputException
	 */
	public static boolean checkForMissingInput(PaInputData data, SkillContext skillContext) throws InvalidInputException {
		if (data.getRouteID() == null){ //if no route was spoken by the user, prompt the user for a route.
			skillContext.setLastQuestion(OutputHelper.ROUTE_PROMPT);
			return true;
		} else if (data.getRouteName() == null){ //otherwise, attempt to validate the raw route value.
			try{
				DataHelper.addRouteToConversation(data, skillContext);
			} catch (InvalidInputException e){
				skillContext.setLastQuestion(OutputHelper.ROUTE_PROMPT);
				throw e;
			}
		}
		
		if (data.getRawDirection() == null){ //if no direction was spoken by the user, prompt the user for a direction.
			skillContext.setLastQuestion(OutputHelper.DIRECTION_PROMPT);
			return true;
		} else if (data.getDirection() == null){ //otherwise, attempt to validate the raw direction value.
			try{
				DataHelper.addDirectionToConversation(data, skillContext);
			} catch (InvalidInputException e){
				skillContext.setLastQuestion(OutputHelper.DIRECTION_PROMPT);
				throw e;
			}
		}
		
		if (data.getLocationName() == null){ //if no location was spoken by the user, prompt the user for a location.
			skillContext.setLastQuestion(OutputHelper.LOCATION_PROMPT);
			return true;
		} else if (data.getLocationAddress() == null){ //otherwise, attempt to validate the raw location value.
			try{
				DataHelper.addLocationToConversation(data, skillContext);
			} catch (InvalidInputException e){
				skillContext.setLastQuestion(OutputHelper.LOCATION_PROMPT);
				throw e;
			}
		}
		
		return false;
	}
	
}