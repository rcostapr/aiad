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

}