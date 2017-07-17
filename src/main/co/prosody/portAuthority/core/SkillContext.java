package co.prosody.portAuthority.core;

import java.util.LinkedHashMap;

/**
 * Contains session scoped settings.
 */
public class SkillContext {
    private boolean needsMoreHelp;
    private boolean allRoutes;
    private boolean sayLocationInResponse;
    private boolean needsBusStop;
    private boolean additionalQuestions;
    private String lastQuestion;
    private String feedbackText;
    public static SkillContext create(Object o){
    	SkillContext context = null;
    	if (o instanceof LinkedHashMap){
    		LinkedHashMap<String, Object> map = (LinkedHashMap<String, Object>)o;
    		context = SkillContext.newInstance();
    		context.setNeedsMoreHelp((boolean)(map.get("needsMoreHelp")));
    		context.setAllRoutes((boolean)(map.get("allRoutes")));
    		context.setSayLocationInResponse((boolean)(map.get("sayLocationInResponse")));
    		context.setNeedsBusStop((boolean)(map.get("needsBusStop")));
    		context.setAdditionalQuestions((boolean)(map.get("additionalQuestions")));
    		context.setLastQuestion((String)map.get("lastQuestion"));
    		context.setFeedbackText((String)map.get("feedbackText"));
    	} else if (o instanceof SkillContext){
    		context = (SkillContext)o;
    	} else {
    		throw new ClassCastException("Cannot create a SkillContext object with " + o.getClass().toString());
    	}
    	return context;
    }
    
    public static SkillContext newInstance(){
    	SkillContext context = new SkillContext();
    	context.setAllRoutes(false);
    	context.setNeedsBusStop(true);
    	context.setSayLocationInResponse(true);
    	context.setNeedsMoreHelp(true);
    	context.setAdditionalQuestions(true);
    	context.setLastQuestion(OutputHelper.ROUTE_PROMPT);
    	context.setFeedbackText("");
    	return context;
    }
    
    public boolean getNeedsMoreHelp() {
        return needsMoreHelp;
    }

    public void setNeedsMoreHelp(boolean needsMoreHelp) {
        this.needsMoreHelp = needsMoreHelp;
    }

	public boolean isAllRoutes() {
		return allRoutes;
	}

	public void setAllRoutes(boolean showAllRoutes) {
		this.allRoutes = showAllRoutes;
	}

	public boolean getSayLocationInResponse() {
		return sayLocationInResponse;
	}

	public void setSayLocationInResponse(boolean sayLocationInResponse) {
		this.sayLocationInResponse = sayLocationInResponse;
	}

	public boolean getNeedsBusStop() {
		return needsBusStop;
	}

	public void setNeedsBusStop(boolean needsBusStop) {
		this.needsBusStop = needsBusStop;
	}
	
	public boolean getAdditionalQuestions(){
		return additionalQuestions;
	}
	
	public void setAdditionalQuestions(boolean additionalQuestions){
		this.additionalQuestions = additionalQuestions;
	}
	
	public String getLastQuestion(){
		return lastQuestion;
	}
	public void setLastQuestion(String lastQuestion){
		this.lastQuestion = lastQuestion;
	}
	
	public String getFeedbackText(){
		return feedbackText;
	}
	
	public void setFeedbackText(String feedbackText){
		this.feedbackText = feedbackText;
	}
	
	public void addFeedbackText(String additionalText){
		this.feedbackText += additionalText;
	}
}
