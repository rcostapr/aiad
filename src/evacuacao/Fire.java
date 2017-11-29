package evacuacao;

import repast.simphony.context.Context;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;

public class Fire{
	private Grid<Object> grid;
	Context<Object> context;
	
	public Fire(Grid<Object> grid, Context<Object> context,int startX,int startY) {
		this.grid = grid;
		this.context = context;
		context.add(this);
		grid.moveTo(this, startX, startY);
	}
	
	public GridPoint getLocation() {
		return grid.getLocation(this);
	}
	
	public boolean isFireIsolate(){
		
		int i = getLocation().getX();
		int j = getLocation().getY();
		
		if (validPosition(i, j + 1)) {
			return false;
		}
		if (validPosition(i + 1, j + 1)) {
			return false;
		}
		if (validPosition(i + 1, j)) {
			return false;
		}
		if (validPosition(i, j - 1)) {
			return false;
		}

		if (validPosition(i + 1, j - 1)) {
			return false;
		}

		if (validPosition(i - 1, j - 1)) {
			return false;
		}

		if (validPosition(i - 1, j + 1)) {
			return false;
		}

		if (validPosition(i - 1, j)) {
			return false;
		}
		
		return true;
	}
	
	private boolean validPosition(int i, int j) {
		if (i < 0 || j < 0)
			return false;
		if (i >= grid.getDimensions().getWidth())
			return false;
		if (j >= grid.getDimensions().getHeight())
			return false;
		for (Object obj : grid.getObjectsAt(i, j)) {
			if (obj instanceof Wall || obj instanceof Fire) {
				return false;
			}
		}
		return true;
	}

}