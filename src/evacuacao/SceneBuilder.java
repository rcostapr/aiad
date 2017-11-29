package evacuacao;

import java.util.ArrayList;
import java.util.List;

import jade.wrapper.StaleProxyException;
import repast.simphony.context.Context;
import repast.simphony.context.space.grid.GridFactory;
import repast.simphony.context.space.grid.GridFactoryFinder;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.parameter.Parameters;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridBuilderParameters;
import repast.simphony.space.grid.SimpleGridAdder;
import repast.simphony.space.grid.WrapAroundBorders;
import sajas.wrapper.ContainerController;

public class SceneBuilder {
	private Context<Object> context;
	private Grid<Object> grid;
	private ContainerController agentContainer;

	private int humanCount;
	private int securityCount;
	private int doorsCount;
	private int radiusVision;

	SceneBuilder(Context<Object> context) {
		this.context = context;

		try {

			GridFactory gridFactory = GridFactoryFinder.createGridFactory(null);
			this.grid = gridFactory.createGrid("grid", context,
					new GridBuilderParameters<Object>(new WrapAroundBorders(), new SimpleGridAdder<Object>(), true, 40, 25));

			Parameters params = RunEnvironment.getInstance().getParameters();

			this.humanCount = (Integer) params.getValue("human_count");
			this.securityCount = (Integer) params.getValue("security_count");
			this.doorsCount = (Integer) params.getValue("doors_count");
			this.radiusVision = (Integer) params.getValue("radius_vision");

			buildWalls(this.grid, context);
			generateExits(this.grid, context, doorsCount);
			
			context.add(this);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	@ScheduledMethod(start = 15, interval = 30)
	public void step() {
		List<Fire> fires = new ArrayList<Fire>();
		for (Object obj : grid.getObjects()) {
			if (obj instanceof Fire) {
				if (!((Fire) obj).isFireIsolate())
					fires.add((Fire) obj);
			}
		}

		for (int i = 0; i < fires.size(); i++) {
			setFire(fires.get(i));
		}

	}
	
	public ContainerController getAgentContainer() {
		return agentContainer;
	}

	public void setAgentContainer(ContainerController agentContainer) {
		this.agentContainer = agentContainer;
	}

	private void generateExits(Grid<Object> grid, Context<Object> context, int doorsCount) {
		while (doorsCount > 0) {
			double chance = RandomHelper.nextDoubleFromTo(0, 1);
			int doorExitX = 0, doorExitY = 0;
			// right wall
			if (chance <= 0.33) {
				doorExitX = grid.getDimensions().getWidth() - 1;
				doorExitY = RandomHelper.nextIntFromTo(1, grid.getDimensions().getHeight() - 2);
			}
			// top wall
			else if (chance <= 0.66) {
				doorExitX = RandomHelper.nextIntFromTo(grid.getDimensions().getWidth() - 20, grid.getDimensions().getWidth() - 2);
				doorExitY = grid.getDimensions().getHeight() - 1;
			}
			// bottom wall
			else if (chance <= 1) {
				doorExitX = RandomHelper.nextIntFromTo(grid.getDimensions().getWidth() - 20, grid.getDimensions().getWidth() - 2);
				doorExitY = 0;
			}
			Door exitDoor = new Door(grid);
			context.add(exitDoor);
			grid.moveTo(exitDoor, doorExitX, doorExitY);
			for (Object obj : grid.getObjectsAt(doorExitX, doorExitY)) {
				if (obj instanceof Wall) {
					context.remove(obj);
				}
				// if a door already exists at that location - try again
				if (obj instanceof Door) {
					context.remove(exitDoor);
					continue;
				}

			}

			doorsCount--;

		}
	}

	public void createHumans() {
		for (int i = 0; i < humanCount; i++) {
			Human newHuman = new Human(grid, context, State.inRoom, Condition.healthy, 1, radiusVision);
			context.add(newHuman);
			int startX = RandomHelper.nextIntFromTo(1, grid.getDimensions().getWidth() - 20);
			int startY = RandomHelper.nextIntFromTo(1, grid.getDimensions().getHeight() - 2);
			while (!isValidPosition(startX, startY, grid)) {
				startX = RandomHelper.nextIntFromTo(1, grid.getDimensions().getWidth() - 20);
				startY = RandomHelper.nextIntFromTo(1, grid.getDimensions().getHeight() - 2);
			}
			grid.moveTo(newHuman, startX, startY);
			try {
				agentContainer.acceptNewAgent("person" + i, newHuman).start();
			} catch (StaleProxyException e) {
				e.printStackTrace();
			}
		}
	}

	public void createSecurity() {
		for (int i = 0; i < securityCount; i++) {

			int startX = RandomHelper.nextIntFromTo(grid.getDimensions().getWidth() - 20, grid.getDimensions().getWidth() - 1);
			int startY = RandomHelper.nextIntFromTo(1, grid.getDimensions().getHeight() - 2);
			while (!isValidPosition(startX, startY, grid)) {
				startX = RandomHelper.nextIntFromTo(grid.getDimensions().getWidth() - 20, grid.getDimensions().getWidth() - 1);
				startY = RandomHelper.nextIntFromTo(1, grid.getDimensions().getHeight() - 2);
			}
			Security newSecurity = new Security(grid, context, startX, startY);
			try {
				agentContainer.acceptNewAgent("security" + i, newSecurity).start();
			} catch (StaleProxyException e) {
				e.printStackTrace();
			}
		}
	}

	private void buildWalls(Grid<Object> grid, Context<Object> context) {

		// LEFT WALL
		for (int i = 0; i < grid.getDimensions().getHeight(); i++) {
			Wall wall = new Wall(grid);
			context.add(wall);
			grid.moveTo(wall, 0, i);

		}
		// RIGHT WALL
		for (int i = 0; i < grid.getDimensions().getHeight(); i++) {
			Wall wall = new Wall(grid);
			context.add(wall);
			grid.moveTo(wall, grid.getDimensions().getWidth() - 1, i);

		}
		// TOP WALL
		for (int i = 1; i < grid.getDimensions().getWidth(); i++) {
			Wall wall = new Wall(grid);
			context.add(wall);
			grid.moveTo(wall, i, grid.getDimensions().getHeight() - 1);

		}
		// BOTTOM WALL
		for (int i = 1; i < grid.getDimensions().getWidth(); i++) {
			Wall wall = new Wall(grid);
			context.add(wall);
			grid.moveTo(wall, i, 0);
		}

		for (int i = 1; i < grid.getDimensions().getHeight() - 1; i++) {
			if (i != 3 && i != 12) {
				Wall wall = new Wall(grid);
				context.add(wall);
				grid.moveTo(wall, 10, i);
			}

		}

		for (int i = 1; i < grid.getDimensions().getHeight() - 1; i++) {
			if (i != 8 && i != 20) {
				Wall wall = new Wall(grid);
				context.add(wall);
				grid.moveTo(wall, 19, i);
			}

		}

		for (int i = 1; i < grid.getDimensions().getWidth() - 20; i++) {
			if (i != 8) {
				Wall wall = new Wall(grid);
				context.add(wall);
				grid.moveTo(wall, i, 9);
			}

		}

		for (int i = 1; i < grid.getDimensions().getWidth() - 20; i++) {
			if (i != 2 && i != 8 && i != 14) {
				Wall wall = new Wall(grid);
				context.add(wall);
				grid.moveTo(wall, i, 16);
			}

		}

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

	public void scheduleFire() {
		int startX = RandomHelper.nextIntFromTo(1, 21);
		int startY = RandomHelper.nextIntFromTo(1, grid.getDimensions().getHeight() - 2);
		while (!isValidPosition(startX, startY, grid)) {
			startX = RandomHelper.nextIntFromTo(1, 21);
			startY = RandomHelper.nextIntFromTo(1, grid.getDimensions().getHeight() - 2);
		}
		new ScheduleFire(grid, context, startX, startY);

		// System.out.println(RunEnvironment.getInstance().getCurrentSchedule().getTickCount());
		// ISchedule schedule =
		// RunEnvironment.getInstance().getCurrentSchedule();
		// ScheduleParameters scheduleParams =
		// ScheduleParameters.createOneTime(5.0);
		// System.out.println(RunEnvironment.getInstance().getCurrentSchedule().getTickCount());
		// schedule.schedule(scheduleParams, "createFire");
	}

	private void setFire(Fire fire) {
		int i = fire.getLocation().getX();
		int j = fire.getLocation().getY();

		if (validPosition(i, j + 1)) {
			new Fire(grid, context, i, j + 1);
		}
		if (validPosition(i + 1, j + 1)) {
			new Fire(grid, context, i + 1, j + 1);
		}
		if (validPosition(i + 1, j)) {
			new Fire(grid, context, i + 1, j);
		}
		if (validPosition(i, j - 1)) {
			new Fire(grid, context, i, j - 1);
		}

		if (validPosition(i + 1, j - 1)) {
			new Fire(grid, context, i + 1, j - 1);
		}

		if (validPosition(i - 1, j - 1)) {
			new Fire(grid, context, i - 1, j - 1);
		}

		if (validPosition(i - 1, j + 1)) {
			new Fire(grid, context, i - 1, j + 1);
		}

		if (validPosition(i - 1, j)) {
			new Fire(grid, context, i - 1, j);
		}

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
