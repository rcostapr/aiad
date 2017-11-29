package evacuacao.onto;

import jade.content.Predicate;

public class GoToPoint implements Predicate {
	
	private static final long serialVersionUID = 1L;
	
	private int x;
	private int y;

	public GoToPoint(int x, int y) {
		this.x=x;
		this.y=y;
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}
}
