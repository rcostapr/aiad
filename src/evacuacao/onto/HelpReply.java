package evacuacao.onto;

import jade.content.Predicate;
import jade.core.AID;

public class HelpReply implements Predicate {
	
	private static final long serialVersionUID = 1L;
	
	private int visionRadius;
	private AID proposerAID;

	public HelpReply() {
	}
	
	public HelpReply(int visionRadius) {
		this.visionRadius = visionRadius;
	}
	
	public int getVisionRadius() {
		return visionRadius;
	}
	
	public void setVisionRadius(int visionRadius) {
		this.visionRadius = visionRadius;
	}

	public AID getProposerAID() {
		return proposerAID;
	}
	
	public void setProposerAID(AID proposerAID) {
		this.proposerAID = proposerAID;
	}
}
