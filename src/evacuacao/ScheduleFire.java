package evacuacao;

import repast.simphony.context.Context;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;

public class ScheduleFire {
	private Grid<Object> grid;
	Context<Object> context;
	private int startX;
	private int startY;
	private static final int tickValue = 5;
	
	public ScheduleFire(Grid<Object> grid, Context<Object> context,int startX,int startY) {
		this.grid = grid;
		this.context = context;
		this.startX=startX;
		this.startY=startY;
		context.add(this);
	}
	
	@ScheduledMethod (start = tickValue)
	public void step () {
		new Fire(grid,context,startX,startY);
		System.out.println("FIRE START!!!! at : " + this.startX + " , " + this.startY);
	}
	
	public GridPoint getLocation() {
		return grid.getLocation(this);
	}

}