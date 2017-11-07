package evacuacao.onto;

import jade.content.Predicate;

public class HelpRequest implements Predicate {
	
	private static final long serialVersionUID = 1L;
	private int visionRadius;

	private String message = "Can you help me find exit?";
	
	public HelpRequest() {
	}
	
	public HelpRequest(int visionRadius) {
		this.visionRadius = visionRadius;
	}

	/**
	 * @return the knowledge
	 */
	public int getVisionRadius() {
		return visionRadius;
	}

	/**
	 * @param knowkledge the knowledge to set
	 */
	public void setVisionRadius(int visionRadius) {
		this.visionRadius = visionRadius;
	}
	
	/**
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * @param message the message to set
	 */
	public void setMessage(String message) {
		this.message = message;
	}
}