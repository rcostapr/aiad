package evacuacao;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import evacuacao.onto.GoToPoint;
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

public class Security extends Agent {
	private Grid<Object> grid;
	private Context<Object> context;

	protected Codec codec;
	protected Ontology serviceOntology;
	protected ACLMessage myCfp;
	private int exitAlive;
	private int alive;

	public int getAlive() {
		return alive;
	}

	public void setAlive(int alive) {
		this.alive = alive;
	}

	public int getDead() {
		if (alive == 0)
			return 1;
		else
			return 0;
	}

	public Security(Grid<Object> grid, Context<Object> context, int startX, int startY) {
		this.grid = grid;
		this.context = context;
		context.add(this);
		grid.moveTo(this, startX, startY);
		this.exitAlive = 0;
		this.alive = 1;
	}

	public int getExitAlive() {
		return exitAlive;
	}

	public void setExitAlive(int exitAlive) {
		this.exitAlive = exitAlive;
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
				if (validPath(doors.get(i).getLocation())) {
					double distVal = Math.hypot(myLocation().getX() - doors.get(i).getLocation().getX(),
							myLocation().getY() - doors.get(i).getLocation().getY());
					if (distVal < distToExit) {
						distToExit = distVal;
						indexDoor = i;
					}
				}
			}

			if (indexDoor > -1) {
				// Go To shortest Possible Direction
				moveToPoint(doors.get(indexDoor).getLocation());
			}
		}
	}

	private GridPoint getNextPoint(GridPoint pt, GridPoint location) {

		ArrayList<Graph.Edge> lgraph = new ArrayList<Graph.Edge>();

		for (int i = 0; i < grid.getDimensions().getWidth(); i++)
			for (int j = 0; j < grid.getDimensions().getHeight(); j++) {
				if (validPosition(i, j)) {
					// Try to add 8 Possible edge
					// -1- (i-1,j+1)
					if (validPosition(i - 1, j + 1)) {
						Graph.Edge nEdge = new Graph.Edge("x" + Integer.toString(i) + "y" + Integer.toString(j),
								"x" + Integer.toString(i - 1) + "y" + Integer.toString(j + 1), 1, new GridPoint(i, j), new GridPoint(i - 1, j + 1));
						lgraph.add(nEdge);
					}
					// -2- (i,j+1)
					if (validPosition(i, j + 1)) {
						Graph.Edge nEdge = new Graph.Edge("x" + Integer.toString(i) + "y" + Integer.toString(j),
								"x" + Integer.toString(i) + "y" + Integer.toString(j + 1), 1, new GridPoint(i, j), new GridPoint(i, j + 1));
						lgraph.add(nEdge);
					}
					// -3- (i+1,j+1)
					if (validPosition(i + 1, j + 1)) {
						Graph.Edge nEdge = new Graph.Edge("x" + Integer.toString(i) + "y" + Integer.toString(j),
								"x" + Integer.toString(i + 1) + "y" + Integer.toString(j + 1), 1, new GridPoint(i, j), new GridPoint(i + 1, j + 1));
						lgraph.add(nEdge);
					}
					// -4- (i-1,j)
					if (validPosition(i - 1, j)) {
						Graph.Edge nEdge = new Graph.Edge("x" + Integer.toString(i) + "y" + Integer.toString(j),
								"x" + Integer.toString(i - 1) + "y" + Integer.toString(j), 1, new GridPoint(i, j), new GridPoint(i - 1, j));
						lgraph.add(nEdge);
					}
					// -5- (i+1,j)
					if (validPosition(i + 1, j)) {
						Graph.Edge nEdge = new Graph.Edge("x" + Integer.toString(i) + "y" + Integer.toString(j),
								"x" + Integer.toString(i + 1) + "y" + Integer.toString(j), 1, new GridPoint(i, j), new GridPoint(i + 1, j));
						lgraph.add(nEdge);
					}
					// -6- (i-1,j-1)
					if (validPosition(i - 1, j - 1)) {
						Graph.Edge nEdge = new Graph.Edge("x" + Integer.toString(i) + "y" + Integer.toString(j),
								"x" + Integer.toString(i - 1) + "y" + Integer.toString(j - 1), 1, new GridPoint(i, j), new GridPoint(i - 1, j - 1));
						lgraph.add(nEdge);
					}
					// -7- (i,j-1)
					if (validPosition(i, j - 1)) {
						Graph.Edge nEdge = new Graph.Edge("x" + Integer.toString(i) + "y" + Integer.toString(j),
								"x" + Integer.toString(i) + "y" + Integer.toString(j - 1), 1, new GridPoint(i, j), new GridPoint(i, j - 1));
						lgraph.add(nEdge);
					}
					// -8- (i+1,j-1)
					if (validPosition(i + 1, j - 1)) {
						Graph.Edge nEdge = new Graph.Edge("x" + Integer.toString(i) + "y" + Integer.toString(j),
								"x" + Integer.toString(i + 1) + "y" + Integer.toString(j - 1), 1, new GridPoint(i, j), new GridPoint(i + 1, j - 1));
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

	public GridPoint getNearExit() {
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
				if (distVal < distToExit) {
					distToExit = distVal;
					indexDoor = i;
				}
			}
		}

		if (indexDoor > -1) {
			// Go To shortest Possible Direction
			return doors.get(indexDoor).getLocation();

		}
		return null;
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
		List<Security> people = new ArrayList<Security>();
		for (Object obj : grid.getObjects()) {
			if (obj instanceof Security) {
				// Ainda não saiu e está vivo
				if (((Security) obj).getExitAlive() == 0 && ((Security) obj).getAlive() == 1)
					people.add((Security) obj);
			}
		}

		List<Human> humans = new ArrayList<Human>();
		for (Object obj : grid.getObjects()) {
			if (obj instanceof Human) {
				// Humano ainda não saiu e está vivo Segurança espera
				if (((Human) obj).getExitAlive() == 0 && ((Human) obj).getAlive() == 1)
					humans.add((Human) obj);
			}
		}

		if ((people.size() + humans.size()) == 0) {
			System.out.println("#########   Animation END  #########");
			RunEnvironment.getInstance().endRun();
		}

	}

	/**
	 * MoveHandler behaviour
	 */
	class moveHandler extends SimpleBehaviour {
		private MessageTemplate template = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.QUERY_REF),
				MessageTemplate.MatchOntology(ServiceOntology.ONTOLOGY_NAME));
		private static final long serialVersionUID = 1L;

		public moveHandler(Agent a) {
			super(a);
		}

		public void action() {

			ACLMessage msg = receive(template);

			if (done()) {
				System.out.println("Security done");
				return;
			}

			GridCellNgh<Human> nghCreator = new GridCellNgh<Human>(grid, myLocation(), Human.class, 1, 1);
			List<GridCell<Human>> gridCells = nghCreator.getNeighborhood(true);
			SimUtilities.shuffle(gridCells, RandomHelper.getUniform());

			List<Human> humans = new ArrayList<Human>();
			for (Object obj : grid.getObjects()) {
				if (obj instanceof Human) {
					// Humano ainda não saiu e está vivo Segurança espera
					if (((Human) obj).getExitAlive() == 0 && ((Human) obj).getAlive() == 1)
						humans.add((Human) obj);
				}
			}
			// System.out.println("Humans qtd: " + humans.size());

			if (humans.size() == 0 || findNearFire(2))
				moveTowards(myLocation());

			if (msg != null) {
				if (msg.getPerformative() == ACLMessage.QUERY_REF) {
					ACLMessage reply = msg.createReply();
					String content = msg.getContent();
					if ((content != null) && (content.indexOf("EXIT") != -1)) {
						reply.setPerformative(ACLMessage.INFORM);
						GridPoint point = getNearExit();
						GoToPoint goToPoint = new GoToPoint(point.getX(), point.getY());

						try {
							// send reply
							reply.setContentObject(goToPoint);
							send(reply);
						} catch (IOException e) {
							e.printStackTrace();
						}
						System.out.println(getLocalName() + " send reply " + msg.getSender().getLocalName());
					}
				}
			}

		}

		@Override
		public boolean done() {
			if (checkDoorAtLocation(myLocation().getX(), myLocation().getY())) {
				System.out.println("Security Found Door -> " + myLocation().getX() + " : " + myLocation().getY());
				exitAlive = 1;
				System.out.println("Security out alive");
				takeDown();
				return true;
			}
			if (checkFireAtLocation(myLocation().getX(), myLocation().getY())) {
				alive = 0;
				System.out.println(getLocalName() + " Die........");
				takeDown();
				return true;
			}
			return false;
		}
	}

	private boolean checkDoorAtLocation(int x, int y) {
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

	private boolean checkFireAtLocation(int x, int y) {
		List<Object> fires = new ArrayList<Object>();
		for (Object obj : grid.getObjectsAt(x, y)) {
			if (obj instanceof Fire) {
				fires.add(obj);
			}
		}

		if (fires.size() > 0) {
			return true;
		}
		return false;
	}

	public boolean findNearFire(int radius) {

		GridCellNgh<Fire> neighbourhood = new GridCellNgh<Fire>(grid, myLocation(), Fire.class, radius, radius);
		List<GridCell<Fire>> nghPoints = neighbourhood.getNeighborhood(false);

		for (GridCell<Fire> fire : nghPoints) {
			if (fire.size() > 0) {
				return true;
			}
		}
		return false;
	}

	public GridPoint getLocation() {
		return grid.getLocation(this);
	}

	public void moveToPoint(GridPoint point) {
		GridPoint nextPoint = getNextPoint(myLocation(), point);
		if (nextPoint != null) {
			grid.moveTo(this, (int) nextPoint.getX(), (int) nextPoint.getY());
		} else {
			System.out.println(
					getLocalName() + " at " + myLocation().getX() + "," + myLocation().getY() + " impossible destiny to " + point.getX() + "," + point.getY());
		}
	}

	public boolean validPath(GridPoint point) {
		GridPoint nextPoint = getNextPoint(myLocation(), point);
		if (nextPoint != null) {
			return true;
		}
		return false;
	}

}
