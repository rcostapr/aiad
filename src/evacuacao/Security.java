package evacuacao;

import java.util.ArrayList;
import java.util.List;

import evacuacao.onto.ServiceOntology;
import graph.Graph;
import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.Ontology;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import repast.simphony.context.Context;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.query.space.grid.GridCell;
import repast.simphony.query.space.grid.GridCellNgh;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;
import repast.simphony.util.SimUtilities;
import sajas.core.Agent;
import sajas.core.behaviours.SimpleBehaviour;

public class Security extends Agent{
	private Grid<Object> grid;
	private boolean moved;
	private Context<Object> context;
	
	protected Codec codec;
	protected Ontology serviceOntology;	
	protected ACLMessage myCfp;	

	public Security(Grid<Object> grid, Context<Object> context, int startX, int startY) {
		this.grid = grid;
		this.context = context;
		context.add(this);
		grid.moveTo(this, startX, startY);
	}


	private GridPoint myLocation() {
		return grid.getLocation(this);
	}
	
	public void moveTowards(GridPoint pt) {
		double distToExit = 999999;
		int indexDoor = -1;

		// Move To Shortest Exit
		List<Door> doors = new ArrayList<Door>();
		for (Object obj : grid.getObjects()) {
			if (obj instanceof Door) {
				doors.add((Door) obj);
			}
		}
		if (doors.size() > 0) {
			for (int i = 0; i < doors.size(); i++) {
				double distVal = Math.hypot(myLocation().getX() - doors.get(i).getLocation().getX(), myLocation().getY() - doors.get(i).getLocation().getY());
				//System.out.println(myLocation().getX() + " " + myLocation().getY() + " " + doors.get(i).getLocation().getX() + " "
				//		+ doors.get(i).getLocation().getX() + " " + distVal);
				if (distVal < distToExit) {
					distToExit = distVal;
					indexDoor = i;
				}
			}
		}

		if (indexDoor > -1) {
			// Go To shortest Possible Direction

			GridPoint nextPoint = getNextPoint(pt, doors.get(indexDoor).getLocation());
			if(nextPoint != null){
				grid.moveTo(this, (int) nextPoint.getX(), (int) nextPoint.getY());
			}

		}
		setMoved(true);
	}

	private GridPoint getNextPoint(GridPoint pt, GridPoint location) {

		ArrayList<Graph.Edge> lgraph = new ArrayList<Graph.Edge>();

		for (int i = 0; i < grid.getDimensions().getWidth(); i++)
			for (int j = 0; j < grid.getDimensions().getHeight(); j++) {
				if (validPosition(i, j)) {
					// Try to add 8 Possible edge
					// -1- (i-1,j+1)
					if (validPosition(i - 1, j + 1)) {
						Graph.Edge nEdge = new Graph.Edge(
								"x" + Integer.toString(i) + "y" + Integer.toString(j),
								"x" + Integer.toString(i - 1) + "y" + Integer.toString(j + 1)
								, 1
								, new GridPoint(i, j)
								, new GridPoint(i - 1, j + 1));
						lgraph.add(nEdge);
					}
					// -2- (i,j+1)
					if (validPosition(i, j + 1)) {
						Graph.Edge nEdge = new Graph.Edge(
								"x" + Integer.toString(i) + "y" + Integer.toString(j),
								"x" + Integer.toString(i) + "y" + Integer.toString(j + 1),
								1, 
								new GridPoint(i, j), 
								new GridPoint(i, j + 1));
						lgraph.add(nEdge);
					}
					// -3- (i+1,j+1)
					if (validPosition(i + 1, j + 1)) {
						Graph.Edge nEdge = new Graph.Edge(
								"x" + Integer.toString(i) + "y" + Integer.toString(j),
								"x" + Integer.toString(i + 1) + "y" + Integer.toString(j + 1),
								1, 
								new GridPoint(i, j), 
								new GridPoint(i + 1, j + 1));
						lgraph.add(nEdge);
					}
					// -4- (i-1,j)
					if (validPosition(i - 1, j)) {
						Graph.Edge nEdge = new Graph.Edge(
								"x" + Integer.toString(i) + "y" + Integer.toString(j),
								"x" + Integer.toString(i - 1) + "y" + Integer.toString(j),
								1, 
								new GridPoint(i, j), 
								new GridPoint(i - 1, j));
						lgraph.add(nEdge);
					}
					// -5- (i+1,j)
					if (validPosition(i + 1, j)) {
						Graph.Edge nEdge = new Graph.Edge(
								"x" + Integer.toString(i) + "y" + Integer.toString(j),
								"x" + Integer.toString(i + 1) + "y" + Integer.toString(j), 
								1, 
								new GridPoint(i, j), 
								new GridPoint(i + 1, j));
						lgraph.add(nEdge);
					}
					// -6- (i-1,j-1)
					if (validPosition(i - 1, j - 1)) {
						Graph.Edge nEdge = new Graph.Edge(
								"x" + Integer.toString(i) + "y" + Integer.toString(j),
								"x" + Integer.toString(i - 1) + "y" + Integer.toString(j - 1),
								1, 
								new GridPoint(i, j), 
								new GridPoint(i - 1, j - 1));
						lgraph.add(nEdge);
					}
					// -7- (i,j-1)
					if (validPosition(i, j - 1)) {
						Graph.Edge nEdge = new Graph.Edge(
								"x" + Integer.toString(i) + "y" + Integer.toString(j),
								"x" + Integer.toString(i) + "y" + Integer.toString(j - 1),
								1, 
								new GridPoint(i, j), 
								new GridPoint(i, j - 1));
						lgraph.add(nEdge);
					}
					// -8- (i+1,j-1)
					if (validPosition(i + 1, j - 1)) {
						Graph.Edge nEdge = new Graph.Edge(
								"x" + Integer.toString(i) + "y" + Integer.toString(j),
								"x" + Integer.toString(i + 1) + "y" + Integer.toString(j - 1), 
								1, 
								new GridPoint(i, j), 
								new GridPoint(i + 1, j - 1));
						lgraph.add(nEdge);
					}
				}
			}

		Graph.Edge[] GRAPH = new Graph.Edge[lgraph.size()];
		GRAPH = lgraph.toArray(GRAPH);
		
		final String START = "x" + Integer.toString(pt.getX()) + "y" + Integer.toString(pt.getY());
		final String END = "x" + Integer.toString(location.getX()) + "y" + Integer.toString(location.getY());

		Graph g = new Graph(GRAPH);
		g.dijkstra(START);
		GridPoint nextPoint = g.getNextPoint(START, END);
		//g.printPath(END);
		//g.printAllPaths();
		return nextPoint;
	}

	private boolean validPosition(int i, int j) {
		if (i < 0 || j < 0)
			return false;
		if (i >= grid.getDimensions().getWidth())
			return false;
		if (j >= grid.getDimensions().getHeight())
			return false;
		for (Object obj : grid.getObjectsAt(i, j)) {
			if (obj instanceof Wall) {
				return false;
			}
		}
		return true;
	}

	public boolean isMoved() {
		return moved;
	}

	public void setMoved(boolean moved) {
		this.moved = moved;
	}
	
	@Override
	public void setup() {
		// register language and ontology
		codec = new SLCodec();
		serviceOntology = ServiceOntology.getInstance();
		getContentManager().registerLanguage(codec);
		getContentManager().registerOntology(serviceOntology);	

		// add behaviours
		addBehaviour(new moveHandler(this));
	}

	@Override
	protected void takeDown() {
		System.out.println("Security out alive");
		context.remove(this);
		List<Security> people = new ArrayList<Security>();
		for (Object obj : grid.getObjects()) {
			if (obj instanceof Security) {
				people.add((Security) obj);
			}
		}
		if (people.size() == 0)
			RunEnvironment.getInstance().endRun();
		// notify results collector
		
	}
	
	/**
	 * MoveHandler behaviour
	 */
	class moveHandler extends SimpleBehaviour {
		private MessageTemplate template = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.PROPAGATE), MessageTemplate.MatchOntology(ServiceOntology.ONTOLOGY_NAME));
		private static final long serialVersionUID = 1L;

		public moveHandler(Agent a) {
			super(a);
		}

		public void action() {
			
			if(done()){
				System.out.println("Security done");
				return;
			}
			
			GridCellNgh<Human> nghCreator = new GridCellNgh<Human>(grid, myLocation(), Human.class, 1, 1);
			List<GridCell<Human>> gridCells = nghCreator.getNeighborhood(true);
			SimUtilities.shuffle(gridCells, RandomHelper.getUniform());
			
			
			List<Human> humans = new ArrayList<Human>();
			for (Object obj : grid.getObjects()) {
				if (obj instanceof Human) {
					humans.add((Human) obj);
				}
			}
			System.out.println("Humans qtd: " + humans.size());
			if(humans.size()==0)
				moveTowards(myLocation());
			
			ACLMessage msg = receive(template);
			if(msg!= null) {
				System.out.println(msg);
			}
		}

		@Override
		public boolean done() {	
			if (checkDoorAtLocation(myLocation().getX(),myLocation().getY())) {
				System.out.println("Security Found Door -> " + myLocation().getX() + " : " + myLocation().getY());
				takeDown();
				return true;
			}
			return false;
		}
	}
	
	private boolean checkDoorAtLocation(int x, int y){
		List<Object> doors = new ArrayList<Object>();
		for (Object obj : grid.getObjectsAt(x, y)) {
			if (obj instanceof Door) {
				doors.add(obj);
			}
		}

		if (doors.size() > 0) {
			return true;
		}
		return false;
	}		

}
