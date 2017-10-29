package evacuacao;

import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;

public class Fire {
	private Grid<Object> grid;
	public Fire(Grid<Object> grid) {
		this.grid = grid;		
	}
	
	public GridPoint getLocation() {
		return grid.getLocation(this);
	}
	
	// create fire spread
	public void step(){
		
	}
}
