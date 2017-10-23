package evacuacao;

import repast.simphony.context.Context;
import repast.simphony.context.space.grid.GridFactory;
import repast.simphony.context.space.grid.GridFactoryFinder;
import repast.simphony.dataLoader.ContextBuilder;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.parameter.Parameters;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridBuilderParameters;
import repast.simphony.space.grid.SimpleGridAdder;
import repast.simphony.space.grid.WrapAroundBorders;

public class JEvacuationBuilder implements ContextBuilder<Object > {

	@Override
	public Context<?> build(Context<Object> context) {
		GridFactory gridFactory = GridFactoryFinder.createGridFactory(null);
		Grid<Object> grid = gridFactory.createGrid("grid", context, 
				new GridBuilderParameters<Object>(new WrapAroundBorders(),
						new SimpleGridAdder<Object>(),
						true, 25, 25));
		
		Parameters params = RunEnvironment.getInstance().getParameters();
		
		builWalls(grid, context);
		
		int humanCount = (Integer)params.getValue("human_count");
		
		int doorExitX = grid.getDimensions().getWidth()-1;
		int doorExitY = RandomHelper.nextIntFromTo(1,grid.getDimensions().getHeight()-2);
		Door exitDoor = new Door(grid);
		context.add(exitDoor);
		grid.moveTo(exitDoor, doorExitX, doorExitY);
		
		for (Object obj : grid.getObjectsAt(doorExitX, doorExitY)) {
			if (obj instanceof Wall) {
				context.remove(obj);
			}
		}
		
		for(int i = 0; i < humanCount; i++) {
			Human newHuman = new Human(grid);
			context.add(newHuman);
			int startX = RandomHelper.nextIntFromTo(1,grid.getDimensions().getWidth()-2);
			int startY = RandomHelper.nextIntFromTo(1,grid.getDimensions().getHeight()-2);
			while(!isValidPosition(startX,startY, grid)){
				startX = RandomHelper.nextIntFromTo(1,grid.getDimensions().getWidth()-2);
				startY = RandomHelper.nextIntFromTo(1,grid.getDimensions().getHeight()-2);
			}
			grid.moveTo(newHuman, startX, startY);
		}
		
		return context;
	}

	private boolean isValidPosition(int startX, int startY, Grid<Object> grid) {
		if (startX < 0 || startY < 0)
			return false;
		if (startX >= grid.getDimensions().getWidth())
			return false;
		if (startY >= grid.getDimensions().getHeight())
			return false;
		for (Object obj : grid.getObjectsAt(startX, startY)) {
			if (obj instanceof Wall) {
				return false;
			}
		}
		return true;
	}

	private void builWalls(Grid<Object> grid, Context<Object> context) {
		
		//LEFT WALL
		for(int i=0; i<grid.getDimensions().getHeight(); i++){
			Wall wall = new Wall(grid);
			context.add(wall);
			grid.moveTo(wall, 0, i);
			
		}
		//RIGHT WALL
		for(int i=0; i<grid.getDimensions().getHeight(); i++){
			Wall wall = new Wall(grid);		
			context.add(wall);
			grid.moveTo(wall, grid.getDimensions().getWidth()-1, i);
			
		}
		//TOP WALL
		for(int i=1; i<grid.getDimensions().getWidth(); i++){
			Wall wall = new Wall(grid);			
			context.add(wall);
			grid.moveTo(wall, i, grid.getDimensions().getHeight()-1);
			
		}
		//BOTTOM WALL
		for(int i=1; i<grid.getDimensions().getWidth(); i++){
			Wall wall = new Wall(grid);
			context.add(wall);
			grid.moveTo(wall, i, 0);
		}
		
		
		for(int i=1; i<grid.getDimensions().getHeight()-1; i++){
			if(i!=7 && i!=20){
				Wall wall = new Wall(grid);
				context.add(wall);
				grid.moveTo(wall, 5, i);
			}
		}
		
		for(int i=1; i<grid.getDimensions().getHeight()-1; i++){
			if(i!=3 && i!=12){
			Wall wall = new Wall(grid);
			context.add(wall);
			grid.moveTo(wall, 10, i);
			}
			
		}
		
		for(int i=1; i<grid.getDimensions().getHeight()-1; i++){
			if(i!=8 && i!=20){
			Wall wall = new Wall(grid);
			context.add(wall);
			grid.moveTo(wall, 18, i);
			}
			
		}
		
		for(int i=1; i<grid.getDimensions().getHeight()-1; i++){
			if(i!=3 && i!=12){
			Wall wall = new Wall(grid);
			context.add(wall);
			grid.moveTo(wall, grid.getDimensions().getWidth()-3, i);
			}
			
		}
		
		for(int i=1; i<grid.getDimensions().getWidth()-3; i++){
			if(i!=2 && i!=8 && i!=14 && i!=20){
			Wall wall = new Wall(grid);
			context.add(wall);
			grid.moveTo(wall, i, 5);
			}
			
		}
		
		for(int i=1; i<grid.getDimensions().getWidth()-3; i++){
			if(i!=8 && i!=20){
			Wall wall = new Wall(grid);
			context.add(wall);
			grid.moveTo(wall, i, 10);
			}
			
		}
		
		for(int i=1; i<grid.getDimensions().getWidth()-3; i++){
			if(i!=2 && i!=8 && i!=14 && i!=20){
			Wall wall = new Wall(grid);
			context.add(wall);
			grid.moveTo(wall, i, 17);
			}
			
		}
		
		
	}

}
