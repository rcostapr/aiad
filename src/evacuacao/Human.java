package evacuacao;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import evacuacao.onto.GoToPoint;
import evacuacao.onto.ServiceOntology;
import graph.Graph;
import repast.simphony.context.Context;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ISchedule;
import repast.simphony.engine.schedule.ScheduleParameters;
import repast.simphony.parameter.Parameters;
import repast.simphony.query.space.grid.GridCell;
import repast.simphony.query.space.grid.GridCellNgh;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;
import sajas.core.Agent;
import sajas.core.behaviours.CyclicBehaviour;
import sajas.core.behaviours.SimpleBehaviour;
import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.Ontology;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

enum Condition {
	healthy, injured, saved
}

public class Human extends Agent {
	private Grid<Object> grid;
	private Context<Object> context;;
	private Condition condition;
	private float altruist;

	private int visionRadius;

	private int exitX;
	private int exitY;

	private int securityX;
	private int securityY;

	private int exitAlive;
	private int alive;

	private int knowSecurity = 0;
	private int knowExit = 0;
	private int fireAlert = 0;
	private int helpHumansCount = 0;
	private int wasHelped = 0;
	private int saveHumansCount = 0;

	boolean handlingHelpRequest = false;
	private Human humanToCarry = null;
	private boolean gothumanToCarry = false;

	private Human humanToCarryMe = null;
	private boolean gothumanToCarryMe = false;

	protected Codec codec;
	protected Ontology serviceOntology;
	public static final String HELP_MESSAGE = "Can you help me find EXIT door?";
	public static final String FIRE_MESSAGE = "FIRE FIRE Run to exit!?!??!";
	public static final String SECURITY_MESSAGE = "TELL ME WHERE IS THE EXIT DOOR...";
	public static final String INJURED_MESSAGE = "Can you carry me out?...";

	public static final String UNKNOWN = "Unknown Message";
	public static final String HANDLE_HELP_REQUEST = "Already Handle a help request. Sorry?!?!?";
	public static final String INJURY_MESSAGE = null;

	public int getSaveHumansCount() {
		return saveHumansCount;
	}

	public void setSaveHumansCount(int saveHumansCount) {
		this.saveHumansCount = saveHumansCount;
	}

	public int getWasHelped() {
		return wasHelped;
	}

	public void setWasHelped(int wasHelped) {
		this.wasHelped = wasHelped;
	}

	public int getHelpHumansCount() {
		return helpHumansCount;
	}

	public void setHelpHumansCount(int helpHumansCount) {
		this.helpHumansCount = helpHumansCount;
	}

	public int getKnowExit() {
		return knowExit;
	}

	public void setKnowExit(int knowExit) {
		this.knowExit = knowExit;
	}

	public int getFireAlert() {
		return fireAlert;
	}

	public void setFireAlert(int fireAlert) {
		this.fireAlert = fireAlert;
	}

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

	public int getExitAlive() {
		return exitAlive;
	}

	public void setExitAlive(int exitAlive) {
		this.exitAlive = exitAlive;
	}

	public Human(Grid<Object> grid, Context<Object> context, Condition condition, float altruist, int visionRadius) {
		this.grid = grid;
		this.context = context;
		this.condition = condition;
		this.altruist = altruist;
		this.visionRadius = visionRadius;
		this.exitAlive = 0;
		this.alive = 1;
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

	private boolean checkSecurityAtLocation(int x, int y) {
		// Nota: securities = títulos mas para o caso pouco interessa.
		// Seguranças = Security Guards.

		// It only makes sense to look for security if there is a fire
		if (fireAlert == 0)
			return false;

		List<Object> securities = new ArrayList<Object>();
		for (Object obj : grid.getObjectsAt(x, y)) {
			if (obj instanceof Security) {
				securities.add(obj);
			}
		}

		if (securities.size() > 0) {
			// SEND MESSAGE TO ALL Security in that place
			ACLMessage msgSend = new ACLMessage(ACLMessage.QUERY_REF);

			// Define who's gone receive the message
			for (int i = 0; i < securities.size(); i++) {
				Security security = (Security) securities.get(i);
				msgSend.addReceiver(security.getAID());
				System.out.println(getLocalName() + " request help send to " + security.getLocalName());
			}
			// Message Content
			msgSend.setContent(SECURITY_MESSAGE);
			msgSend.setLanguage(codec.getName());
			msgSend.setOntology(serviceOntology.getName());

			// Send message
			send(msgSend);

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

	GridPoint myLocation() {
		return grid.getLocation(this);
	}

	public void moveToExplore(GridPoint pt) {
		int i = pt.getX();
		int j = pt.getY();

		ArrayList<GridPoint> possibleMoves = new ArrayList<GridPoint>();

		if (validPosition(i, j + 1)) {
			possibleMoves.add(new GridPoint(i, j + 1));
		}
		if (validPosition(i + 1, j + 1)) {
			possibleMoves.add(new GridPoint(i + 1, j + 1));
		}
		if (validPosition(i + 1, j)) {
			possibleMoves.add(new GridPoint(i + 1, j));
		}
		if (validPosition(i, j - 1)) {
			possibleMoves.add(new GridPoint(i, j - 1));
		}

		if (validPosition(i + 1, j - 1)) {
			possibleMoves.add(new GridPoint(i + 1, j - 1));
		}

		if (validPosition(i - 1, j - 1)) {
			possibleMoves.add(new GridPoint(i - 1, j - 1));
		}

		if (validPosition(i - 1, j + 1)) {
			possibleMoves.add(new GridPoint(i - 1, j + 1));
		}

		if (validPosition(i - 1, j)) {
			possibleMoves.add(new GridPoint(i - 1, j));
		}

		if (!possibleMoves.isEmpty()) {
			int move_index = RandomHelper.nextIntFromTo(0, possibleMoves.size() - 1);
			grid.moveTo(this, possibleMoves.get(move_index).getX(), possibleMoves.get(move_index).getY());
		}

	}

	public void moveToPoint(GridPoint point) {
		GridPoint nextPoint = getNextPoint(myLocation(), point);
		if (nextPoint != null) {
			grid.moveTo(this, (int) nextPoint.getX(), (int) nextPoint.getY());
		} else {
			System.out.println(
					getLocalName() + " at " + myLocation().getX() + "," + myLocation().getY() + " impossible destiny to " + point.getX() + "," + point.getY());
			moveToExplore(myLocation());
		}
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
				if (distVal < distToExit) {
					distToExit = distVal;
					indexDoor = i;
				}
			}
		}

		if (indexDoor > -1) {
			// Go To shortest Possible Direction
			GridPoint nextPoint = getNextPoint(pt, doors.get(indexDoor).getLocation());
			if (nextPoint != null) {
				System.out.println("moveTowards");
				grid.moveTo(this, (int) nextPoint.getX(), (int) nextPoint.getY());
			} else {
				System.out.println("NOT moveTowards");
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
		System.out.println(" Distance: " + g.getDist(END));
		// g.printPath(END);
		// g.printAllPaths();
		return nextPoint;
	}

	private int getDistBetween(GridPoint pt, GridPoint location) {

		ArrayList<Graph.Edge> lgraph = new ArrayList<Graph.Edge>();

		for (int i = 0; i < grid.getDimensions().getWidth(); i++)
			for (int j = 0; j < grid.getDimensions().getHeight(); j++) {
				if (validPrimitePosition(i, j)) {
					// Try to add 8 Possible edge
					// -1- (i-1,j+1)
					if (validPrimitePosition(i - 1, j + 1)) {
						Graph.Edge nEdge = new Graph.Edge("x" + Integer.toString(i) + "y" + Integer.toString(j),
								"x" + Integer.toString(i - 1) + "y" + Integer.toString(j + 1), 1, new GridPoint(i, j), new GridPoint(i - 1, j + 1));
						lgraph.add(nEdge);
					}
					// -2- (i,j+1)
					if (validPrimitePosition(i, j + 1)) {
						Graph.Edge nEdge = new Graph.Edge("x" + Integer.toString(i) + "y" + Integer.toString(j),
								"x" + Integer.toString(i) + "y" + Integer.toString(j + 1), 1, new GridPoint(i, j), new GridPoint(i, j + 1));
						lgraph.add(nEdge);
					}
					// -3- (i+1,j+1)
					if (validPrimitePosition(i + 1, j + 1)) {
						Graph.Edge nEdge = new Graph.Edge("x" + Integer.toString(i) + "y" + Integer.toString(j),
								"x" + Integer.toString(i + 1) + "y" + Integer.toString(j + 1), 1, new GridPoint(i, j), new GridPoint(i + 1, j + 1));
						lgraph.add(nEdge);
					}
					// -4- (i-1,j)
					if (validPrimitePosition(i - 1, j)) {
						Graph.Edge nEdge = new Graph.Edge("x" + Integer.toString(i) + "y" + Integer.toString(j),
								"x" + Integer.toString(i - 1) + "y" + Integer.toString(j), 1, new GridPoint(i, j), new GridPoint(i - 1, j));
						lgraph.add(nEdge);
					}
					// -5- (i+1,j)
					if (validPrimitePosition(i + 1, j)) {
						Graph.Edge nEdge = new Graph.Edge("x" + Integer.toString(i) + "y" + Integer.toString(j),
								"x" + Integer.toString(i + 1) + "y" + Integer.toString(j), 1, new GridPoint(i, j), new GridPoint(i + 1, j));
						lgraph.add(nEdge);
					}
					// -6- (i-1,j-1)
					if (validPrimitePosition(i - 1, j - 1)) {
						Graph.Edge nEdge = new Graph.Edge("x" + Integer.toString(i) + "y" + Integer.toString(j),
								"x" + Integer.toString(i - 1) + "y" + Integer.toString(j - 1), 1, new GridPoint(i, j), new GridPoint(i - 1, j - 1));
						lgraph.add(nEdge);
					}
					// -7- (i,j-1)
					if (validPrimitePosition(i, j - 1)) {
						Graph.Edge nEdge = new Graph.Edge("x" + Integer.toString(i) + "y" + Integer.toString(j),
								"x" + Integer.toString(i) + "y" + Integer.toString(j - 1), 1, new GridPoint(i, j), new GridPoint(i, j - 1));
						lgraph.add(nEdge);
					}
					// -8- (i+1,j-1)
					if (validPrimitePosition(i + 1, j - 1)) {
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
		return g.getDist(END);
	}

	private boolean validPrimitePosition(int i, int j) {
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

	public void findNearHumans(int radius) {
		ArrayList<AID> neighboursList = new ArrayList<AID>();
		GridCellNgh<Human> nghCreator = new GridCellNgh<Human>(grid, myLocation(), Human.class, radius, radius);
		List<GridCell<Human>> gridCells = nghCreator.getNeighborhood(true);
		System.out.println("--------------->");
		GridPoint pt = null;
		for (GridCell<Human> cell : gridCells) {
			if (cell.size() > 0) {
				pt = cell.getPoint();
				for (Object obj : grid.getObjectsAt(pt.getX(), pt.getY())) {
					if (obj instanceof Human) {
						neighboursList.add(((Human) obj).getAID());
						System.out.println("Send Msg ## From: " + getLocalName() + " to: " + ((Human) obj).getLocalName());
					}
				}

			}

		}
	}

	public ArrayList<AID> findNearAgents(Agent myAgent, int radius) {
		ArrayList<AID> neighboursList = new ArrayList<AID>();

		GridCellNgh<Human> neighbourhood = new GridCellNgh<Human>(grid, grid.getLocation(myAgent), Human.class, radius, radius);
		List<GridCell<Human>> nghPoints = neighbourhood.getNeighborhood(false);

		// NOTA: neighbourhood não tem em consideração as parede e valores opostos na grid
		// Ex: Grid 40x40 o ponto (0,0) está a 1 de distância do ponto (0,39)
		for (GridCell<Human> human : nghPoints) {
			if (human.size() > 0) {
				Iterable<Human> iterable = human.items();
				for (Human agent : iterable) {
					neighboursList.add(agent.getAID());
				}
			}
		}

		GridCellNgh<Security> neighbourSec = new GridCellNgh<Security>(grid, grid.getLocation(myAgent), Security.class, radius, radius);
		List<GridCell<Security>> nghPointsSec = neighbourSec.getNeighborhood(false);
		for (GridCell<Security> secure : nghPointsSec) {
			if (secure.size() > 0) {
				Iterable<Security> iterable = secure.items();
				for (Security agent : iterable) {
					neighboursList.add(agent.getAID());
				}
			}
		}

		return neighboursList;
	}

	public ArrayList<Fire> findNearFire(int radius) {
		ArrayList<Fire> neighboursFire = new ArrayList<Fire>();

		GridCellNgh<Fire> neighbourhood = new GridCellNgh<Fire>(grid, myLocation(), Fire.class, radius, radius);
		List<GridCell<Fire>> nghPoints = neighbourhood.getNeighborhood(false);

		for (GridCell<Fire> fire : nghPoints) {
			if (fire.size() > 0) {
				Iterable<Fire> iterable = fire.items();
				for (Fire myFire : iterable) {
					if (getDistBetween(myLocation(), myFire.getLocation()) <= radius)
						neighboursFire.add(myFire);
				}
			}
		}
		return neighboursFire;
	}

	public ArrayList<Door> findNearDoors(Agent myAgent, int radius) {
		ArrayList<Door> doorsList = new ArrayList<Door>();

		GridCellNgh<Door> neighbourhood = new GridCellNgh<Door>(grid, grid.getLocation(myAgent), Door.class, radius, radius);
		List<GridCell<Door>> nghPoints = neighbourhood.getNeighborhood(false);

		for (GridCell<Door> doors : nghPoints) {
			if (doors.size() > 0) {
				Iterable<Door> iterable = doors.items();
				for (Door door : iterable) {
					// Necessário garantir que está mesmo no raio de visão
					// Usar neighbourhood é mais util pois evita excesso de
					// processamento
					// na verificação de vizinhança
					if (getDistBetween(grid.getLocation(myAgent), door.getLocation()) <= radius)
						doorsList.add(door);
				}
			}
		}

		return doorsList;
	}

	public ArrayList<Security> findNearSecurity(Agent myAgent, int radius) {
		ArrayList<Security> securityList = new ArrayList<Security>();

		GridCellNgh<Security> neighbourhood = new GridCellNgh<Security>(grid, grid.getLocation(myAgent), Security.class, radius, radius);
		List<GridCell<Security>> nghPoints = neighbourhood.getNeighborhood(false);

		for (GridCell<Security> securities : nghPoints) {
			if (securities.size() > 0) {
				Iterable<Security> iterable = securities.items();
				for (Security security : iterable) {
					// Necessário garantir que está mesmo no raio de visão
					// Usar neighbourhood é mais util pois evita excesso de
					// processamento na verificação de vizinhança
					if (getDistBetween(grid.getLocation(myAgent), security.getLocation()) <= radius)
						securityList.add(security);
				}
			}
		}

		return securityList;
	}

	public ArrayList<Human> findNearHuman(Agent myAgent, int radius) {
		ArrayList<Human> humanList = new ArrayList<Human>();

		GridCellNgh<Human> neighbourhood = new GridCellNgh<Human>(grid, grid.getLocation(myAgent), Human.class, radius, radius);
		List<GridCell<Human>> nghPoints = neighbourhood.getNeighborhood(false);

		for (GridCell<Human> humans : nghPoints) {
			if (humans.size() > 0) {
				Iterable<Human> iterable = humans.items();
				for (Human human : iterable) {
					// Necessário garantir que está mesmo no raio de visão
					// Usar neighbourhood é mais util pois evita excesso de
					// processamento na verificação de vizinhança
					if (getDistBetween(grid.getLocation(myAgent), human.myLocation()) <= radius)
						humanList.add(human);
				}
			}
		}

		return humanList;
	}

	public Object findAgent(AID agentAID) {
		Iterable<Object> agents = grid.getObjects();

		for (Object security : agents) {
			if (security instanceof Security && ((Security) security).getAID().equals(agentAID)) {
				return (Security) security;
			}
		}

		for (Object human : agents) {
			if (human instanceof Human && ((Human) human).getAID().equals(agentAID)) {
				return (Human) human;
			}
		}

		return null;
	}

	@Override
	public void setup() {
		// TODO get Scene Builder Parameters
		SceneBuilder scene = (SceneBuilder) context.getObjects(SceneBuilder.class).get(0);

		// register language and ontology
		codec = new SLCodec();
		serviceOntology = ServiceOntology.getInstance();
		getContentManager().registerLanguage(codec);
		getContentManager().registerOntology(serviceOntology);

		// add behaviours

		// fire Detected
		addBehaviour(new fireDetectedBehaviour(this));
		addBehaviour(new receiveFireAlertBehaviour(this));

		// #################################
		// Movement Behaviour
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
		System.out.println("Human takeDown");
	}

	/**
	 * fireDetectedBehaviour behaviour
	 */
	class fireDetectedBehaviour extends SimpleBehaviour {
		private static final long serialVersionUID = 1L;
		// Alert of Fire
		private boolean alertsend;

		public fireDetectedBehaviour(Agent a) {
			super(a);
			alertsend = false;
		}

		public void action() {
			if (done())
				return;
			if (fireAlert == 1) {
				removeBehaviour(this);
				System.out.println("Remove fireDetectedBehaviour");
				return;
			}
			// FIRE DETECTIONS
			System.out.println("Execute fireDetectedBehaviour");
			Parameters params = RunEnvironment.getInstance().getParameters();
			ArrayList<Fire> fireList = findNearFire((Integer) params.getValue("fire_radius"));

			if (fireList.size() > 0) {

				// injured
				for (int i = 0; i < fireList.size(); i++) {
					if (getDistBetween(myLocation(), fireList.get(i).getLocation()) == 1)
						condition = Condition.injured;
				}

				fireAlert = 1;

				System.out.print(this.getAgent().getLocalName() + " Found Fire");
				for (Fire myFire : fireList)
					System.out.print(" at:" + myFire.getLocation().getX() + " " + myFire.getLocation().getY());

				System.out.println("");

				// Find people in the surrounding area
				// Considera-se que o grito de alerta de incendio se houve através das paredes
				ArrayList<AID> humanNear = findNearAgents(myAgent, visionRadius);

				// make help request
				ACLMessage msg = new ACLMessage(ACLMessage.PROPAGATE);

				// Define who's gone receive the message
				if (!humanNear.isEmpty()) {
					for (AID human : humanNear)
						msg.addReceiver(human);

					// Message Content
					msg.setContent(FIRE_MESSAGE);
					msg.setLanguage(codec.getName());
					msg.setOntology(serviceOntology.getName());

					// Send message
					send(msg);
					System.out.println(getLocalName() + " PROPAGATE fire alert message");
				}

				alertsend = true;

				// Create Help Behaviour
				addBehaviour(new HelpBehaviour(myAgent));
				System.out.println(getLocalName() + " Create Help Behaviour");
				// Nota: Somente começa a fazer pedidos de ajuda quando o fogo é
				// detetado

				// Create repeated Fire alert behaviour
				// Current Tick
				double start = RunEnvironment.getInstance().getCurrentSchedule().getTickCount();
				double startAt = start + 6;
				System.out.println(getLocalName() + " Create repeated Fire alert behaviour");
				ISchedule schedule = RunEnvironment.getInstance().getCurrentSchedule();
				ScheduleParameters scheduleParams = ScheduleParameters.createRepeating(startAt, 6.0);
				schedule.schedule(scheduleParams, this, "repeatAlert");

			}

		}

		public void repeatAlert() {
			System.out.println("Execute repeatAlert");
			if (exitAlive == 0) {
				// find people in the surrounding area
				ArrayList<AID> humanNear = findNearAgents(myAgent, visionRadius);
				if (humanNear.isEmpty()) {
					return;
				}

				// make help request
				ACLMessage msg = new ACLMessage(ACLMessage.PROPAGATE);

				// Define who's gone receive the message
				for (AID human : humanNear)
					msg.addReceiver(human);

				// Message Content
				msg.setContent(FIRE_MESSAGE);
				msg.setLanguage(codec.getName());
				msg.setOntology(serviceOntology.getName());

				// Send message
				send(msg);
				System.out.println(getLocalName() + " REPEAT fire alert message");
			}

		}

		@Override
		public boolean done() {
			return alertsend;
		}

	}

	/**
	 * receiveFireAlertBehaviour behaviour
	 */
	class receiveFireAlertBehaviour extends SimpleBehaviour {
		private static final long serialVersionUID = 1L;
		MessageTemplate template = MessageTemplate.MatchPerformative(ACLMessage.PROPAGATE);

		// Alert of Fire
		private boolean alertsend;

		public receiveFireAlertBehaviour(Agent a) {
			super(a);
			alertsend = false;
		}

		public void action() {
			if (done())
				return;
			System.out.println("Execute receiveFireAlertBehaviour");
			// Evitar que as pessoas que tenham detetado o fogo enviem o alerta
			// pois já o enviaram em fireDetectedBehaviour
			if (fireAlert == 1) {
				removeBehaviour(this);
				System.out.println("Remove receiveFireAlertBehaviour");
				return;
			}
			// #########################");
			ACLMessage msg = receive(template);
			if (msg != null) {
				if (msg.getContent().equals(FIRE_MESSAGE)) {
					fireAlert = 1;
					System.out.println(getLocalName() + " receive fire alert: " + msg.getContent());
					// find people in the surrounding area
					ArrayList<AID> humanNear = findNearAgents(myAgent, visionRadius);
					if (humanNear.isEmpty()) {
						System.out.println(getLocalName() + " Agent " + " No one near at " + myLocation().getX() + "," + myLocation().getY());
						return;
					}

					// make help request
					ACLMessage msgSend = new ACLMessage(ACLMessage.PROPAGATE);

					// Define who's gone receive the message
					for (AID human : humanNear)
						msgSend.addReceiver(human);

					// Message Content
					msgSend.setContent(FIRE_MESSAGE);
					msgSend.setLanguage(codec.getName());
					msgSend.setOntology(serviceOntology.getName());

					// Send message
					send(msgSend);
					System.out.println(getLocalName() + " Send Fire Alert message heared.");

					alertsend = true;

					// Create Help Behaviour
					addBehaviour(new HelpBehaviour(myAgent));
					// Nota: Somente começa a fazer pedidos de ajuda quando o fogo é detetado

					// Create repeated Fire alert behaviour
					// Current Tick
					double start = RunEnvironment.getInstance().getCurrentSchedule().getTickCount();
					double startAt = start + 5;
					System.out.println(RunEnvironment.getInstance().getCurrentSchedule().getTickCount());
					ISchedule schedule = RunEnvironment.getInstance().getCurrentSchedule();
					ScheduleParameters scheduleParams = ScheduleParameters.createRepeating(startAt, 5.0);
					schedule.schedule(scheduleParams, this, "repeatAlert");
				}
			}
		}

		public void repeatAlert() {
			System.out.println("Execute repeatAlert");
			if (exitAlive == 0) {
				System.out.println(getLocalName() + " Repeat Fire Alert.");
				// find people in the surrounding area
				ArrayList<AID> humanNear = findNearAgents(myAgent, visionRadius);
				if (humanNear.isEmpty()) {
					return;
				}

				// make help request
				ACLMessage msg = new ACLMessage(ACLMessage.PROPAGATE);

				// Define who's gone receive the message
				for (AID human : humanNear)
					msg.addReceiver(human);

				// Message Content
				msg.setContent(FIRE_MESSAGE);
				msg.setLanguage(codec.getName());
				msg.setOntology(serviceOntology.getName());

				// Send message
				send(msg);
				System.out.println(getLocalName() + " REPEAT fire alert message");
			}

		}

		@Override
		public boolean done() {
			return alertsend;
		}
	}

	/**
	 * Helper behaviour
	 */
	class HelpBehaviour extends CyclicBehaviour {

		private final MessageTemplate template = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.QUERY_IF),
				MessageTemplate.MatchOntology(ServiceOntology.ONTOLOGY_NAME));

		private static final long serialVersionUID = 1L;

		public HelpBehaviour(Agent a) {
			super(a);
			handlingHelpRequest = false;
		}

		public void action() {
			if (done())
				return;
			System.out.println("Execute HelpBehaviour");
			ACLMessage myMsg = receive(template);
			// Already Handle a help request
			if (handlingHelpRequest) {
				if (myMsg != null) {
					System.out.println("Received QUERY_IF message from agent " + myMsg.getSender().getName() + " can't help");
					ACLMessage reply = myMsg.createReply();
					if (HELP_MESSAGE.equals(myMsg.getContent())) {
						reply.setPerformative(ACLMessage.INFORM);
						reply.setContent(HANDLE_HELP_REQUEST);
					} else if (INJURED_MESSAGE.equals(myMsg.getContent())) {
						reply.setPerformative(ACLMessage.INFORM);
						reply.setContent(HANDLE_HELP_REQUEST);
					} else {
						reply.setPerformative(ACLMessage.NOT_UNDERSTOOD);
						reply.setContent(UNKNOWN);
					}
					send(reply);
					return;

				}

			} else {// Not help anyone. Help if altruist.
				if (myMsg != null) {
					if (myMsg.getPerformative() == ACLMessage.QUERY_IF) {
						// Receive a QUERY_IF
						handleHelpRequest(myMsg);
					}

				}
			}

			// if not know exit and know fire alert request help for directions
			if (knowExit == 0 && fireAlert == 1) {
				ArrayList<Human> listNearHuman = findNearHuman(getAgent(), visionRadius);
				if (!listNearHuman.isEmpty()) {

					// SEND MESSAGE TO ALL Human in that place
					ACLMessage msgSend = new ACLMessage(ACLMessage.QUERY_IF);

					System.out.print(getLocalName() + " " + myLocation().getX() + " " + myLocation().getY() + " visionRadius " + visionRadius + " Found Human");

					// Define who's gone receive the message
					for (int i = 0; i < listNearHuman.size(); i++) {
						System.out.print(" at position:" + listNearHuman.get(i).myLocation().getX() + "," + listNearHuman.get(i).myLocation().getY());
						Human human = (Human) listNearHuman.get(i);
						msgSend.addReceiver(human.getAID());
					}
					System.out.println(" send help request.");
					// Message Content
					msgSend.setContent(HELP_MESSAGE);
					msgSend.setLanguage(codec.getName());
					msgSend.setOntology(serviceOntology.getName());

					// Send message
					send(msgSend);
				}
			}

		}

		private void handleHelpRequest(ACLMessage request) {
			if (altruist == 0 || getAlive() == 0)
				return;

			final MessageTemplate template = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.QUERY_IF),
					MessageTemplate.MatchOntology(ServiceOntology.ONTOLOGY_NAME));

			ACLMessage msg = receive(template);
			// TODO implement Altruism and injured
			if (msg != null) {
				if (knowExit == 1 && altruist > 0) {
					ACLMessage reply = msg.createReply();
					String content = msg.getContent();
					if ((content != null) && (content.indexOf("EXIT") != -1)) {
						// if was send a request for exit info
						reply.setPerformative(ACLMessage.INFORM);
						GridPoint point = new GridPoint(exitX, exitY);
						GoToPoint goToPoint = new GoToPoint(point.getX(), point.getY());

						try {
							// send reply
							reply.setContentObject(goToPoint);
							send(reply);
							helpHumansCount = 1;
						} catch (IOException e) {
							e.printStackTrace();
						}
						System.out.println(getLocalName() + " send help reply " + msg.getSender().getLocalName());
					}

					// TODO implement Save Human
					if (altruist > 1) {
						if ((content != null) && (content.equals(INJURED_MESSAGE))) {
							// if was send a request for save rescue
							reply.setPerformative(ACLMessage.INFORM);
							try {
								// send reply
								reply.setContentObject(this);
								send(reply);
								saveHumansCount = 1;
								handlingHelpRequest = true;
							} catch (IOException e) {
								e.printStackTrace();
							}
							// Store the human to save
							humanToCarry = ((Human) findAgent(msg.getSender()));
							GridPoint humanPoint = humanToCarry.myLocation();
							System.out.println(getLocalName() + " send save reply " + msg.getSender().getLocalName() + " at: " + humanPoint.getX() + ","
									+ humanPoint.getY() + " will help");
						}
					}

				} else {
					System.out.println(getLocalName() + " can't help " + msg.getSender().getLocalName());
				}
			}

		}
	}

	/**
	 * MoveHandler behaviour
	 */
	class moveHandler extends SimpleBehaviour {

		private static final long serialVersionUID = 1L;

		public moveHandler(Agent a) {
			super(a);
		}

		public void action() {
			if (done()) {
				System.out.println("Human " + getLocalName() + " done");
				return;
			}

			handleHelpInform();

			if (knowExit == 1 && condition == Condition.healthy) {
				moveToPoint(new GridPoint(exitX, exitY));
				return;
			}
			if (knowSecurity == 1 && myLocation().getX() < 20) {
				moveToPoint((new GridPoint(securityX, securityY)));
				return;
			}
			if (knowSecurity == 1 && condition == Condition.healthy) {
				moveToPoint((new GridPoint(securityX, securityY)));
				return;
			}

			GridPoint exitRomsPointTop = new GridPoint(19, 20);
			GridPoint exitRomsPointBottom = new GridPoint(19, 8);
			GridPoint exitRomsPoint = null;
			if (fireAlert == 1) {
				if (getDistBetween(myLocation(), exitRomsPointTop) < getDistBetween(myLocation(), exitRomsPointBottom)) {
					// Choose top Exit more close
					if (validPath(exitRomsPointTop)) {
						exitRomsPoint = new GridPoint(20, 20);
						System.out.println(getLocalName() + " goto exitRomsPointTop.");
					} else {
						// There is no path to the shortest exit lets try the other one
						if (validPath(exitRomsPointBottom)) {
							exitRomsPoint = new GridPoint(20, 8);
							System.out.println(getLocalName() + " goto exitRomsPointBottom.");
						} else {
							System.out.println(getLocalName() + " is trapped in the fire.");
						}
					}

				} else {
					// Choose Bottom Exit more close
					if (validPath(exitRomsPointBottom)) {
						exitRomsPoint = new GridPoint(20, 8);
						System.out.println(getLocalName() + " goto exitRomsPointBottom.");
					} else {
						// There is no path to the shortest exit lets try the other one
						if (validPath(exitRomsPointTop)) {
							exitRomsPoint = new GridPoint(20, 20);
							System.out.println(getLocalName() + " goto exitRomsPointTop.");
						} else {
							System.out.println(getLocalName() + " is trapped in the fire.");
						}
					}
				}
			}

			if (myLocation().getX() < 20 && fireAlert == 1 && exitRomsPoint != null) {
				moveToPoint(exitRomsPoint);
			} else {
				moveToExplore(myLocation());
				if (condition == Condition.injured) {
					removeBehaviour(this);
					addBehaviour(new injuredHandler(myAgent));
				}
			}

			// Somente toma comportamento diferente após a deteção de Incêndio
			if (fireAlert == 1) {
				// Se for visivel a saída no raio de visão
				// Registar o local de saida e ir para a saída
				// Responder aos pedidos de ajuda
				// Adicionar um parametro que permita testar a simulação sem esta opção

				ArrayList<Door> listNearDoors = findNearDoors(getAgent(), visionRadius);

				if (listNearDoors.size() > 0) {
					// Se avistar mais do que uma escolher a que estiver mais perto
					knowExit = 1;
					int distToDoor = 99999;
					System.out
							.print(getLocalName() + " " + myLocation().getX() + " " + myLocation().getY() + " visionRadius " + visionRadius + " Found Door :");
					for (int i = 0; i < listNearDoors.size(); i++) {
						System.out.print(" at position:" + listNearDoors.get(i).getLocation().getX() + "," + listNearDoors.get(i).getLocation().getY());
						int distDoor = getDistBetween(myLocation(), listNearDoors.get(i).getLocation());
						if (distDoor < distToDoor) {
							distToDoor = distDoor;
							exitX = listNearDoors.get(i).getLocation().getX();
							exitY = listNearDoors.get(i).getLocation().getY();
						}
					}
					System.out.println("");

					// moveTowards(listNearDoors.get(i).getLocation());
					// moveToExit(listNearDoors.get(i).getLocation());
					// getNextPoint(myLocation(),listNearDoors.get(i).getLocation());
				}

				// Se for visivel um segurança
				// Perguntar onde é a saída
				// Registar a saída
				// Responder aos pedidos de ajuda

				ArrayList<Security> listNearSecurity = findNearSecurity(getAgent(), visionRadius);

				if (listNearSecurity.size() > 0) {
					knowSecurity = 1;
					// Se avistar mais do que um segurança escolher o que estiver mais perto
					int distToSecurity = 99999;
					System.out.print(
							getLocalName() + " " + myLocation().getX() + " " + myLocation().getY() + " visionRadius " + visionRadius + " Found Security");
					for (int i = 0; i < listNearSecurity.size(); i++) {
						System.out.print(" at position:" + listNearSecurity.get(i).getLocation().getX() + "," + listNearSecurity.get(i).getLocation().getY());
						int distSecurity = getDistBetween(myLocation(), listNearSecurity.get(i).getLocation());
						if (distSecurity < distToSecurity) {
							distToSecurity = distSecurity;
							securityX = listNearSecurity.get(i).getLocation().getX();
							securityY = listNearSecurity.get(i).getLocation().getY();
						}
					}
					System.out.println("");
				}
			} // END if (fireAlert == 1)
		}

		private void handleHelpInform() {

			final MessageTemplate template = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM),
					MessageTemplate.MatchOntology(ServiceOntology.ONTOLOGY_NAME));

			ACLMessage msg = null;
			while ((msg = receive(template)) != null) {
				GoToPoint goToPoint;
				try {
					if ((goToPoint = (GoToPoint) msg.getContentObject()) == null)
						humanToCarryMe = (Human) msg.getContentObject();
					if (goToPoint != null) {

						// Se for necessário saber a localização do segurança
						// Security security = findAgent(msg.getSender());

						System.out.println(getLocalName() + " receive Information go to EXIT at: " + goToPoint.getX() + "," + goToPoint.getY() + " from  "
								+ msg.getSender().getLocalName());
						knowExit = 1;
						exitX = goToPoint.getX();
						exitY = goToPoint.getY();
						if (msg.getSender().getLocalName().indexOf("person") != -1) {
							wasHelped = 1;
						}
					} else if (humanToCarryMe != null) {
						// TODO receive save help from other human
						System.out.println(getLocalName() + " receive save info at: " + humanToCarryMe.myLocation().getX() + ","
								+ humanToCarryMe.myLocation().getY() + " from  " + msg.getSender().getLocalName());
						gothumanToCarryMe = true;

					} else {
						System.out.println(getLocalName() + " receive msg " + msg.getContent() + " from " + msg.getSender().getLocalName());
					}
				} catch (UnreadableException e) {
					e.printStackTrace();
				}

			}

		}

		@Override
		public boolean done() {
			if (fireAlert == 1) {
				if (checkDoorAtLocation(myLocation().getX(), myLocation().getY())) {
					System.out.println(getLocalName() + " Found Door -> " + myLocation().getX() + " : " + myLocation().getY());
					exitAlive = 1;
					takeDown();
					return true;
				}

				if (!checkSecurityAtLocation(myLocation().getX(), myLocation().getY()) && knowSecurity == 1 && myLocation().getX() == securityX
						&& myLocation().getY() == securityY) {
					// Security change position lets try to find him again
					knowSecurity = 0;
				}
				if (checkFireAtLocation(myLocation().getX(), myLocation().getY())) {
					alive = 0;
					System.out.println(getLocalName() + " Die........");
					takeDown();
					return true;
				}
			}
			return false;
		}

	}

	public boolean validPath(GridPoint point) {
		GridPoint nextPoint = getNextPoint(myLocation(), point);
		if (nextPoint != null) {
			return true;
		}
		return false;
	}

	class injuredHandler extends SimpleBehaviour {

		private static final long serialVersionUID = 1L;

		public injuredHandler(Agent a) {
			super(a);
		}

		public void action() {
			if (done()) {
				System.out.println("Human " + getLocalName() + " done");
				return;
			}
			// TODO injured behaviour

			if (condition == Condition.injured && humanToCarryMe.myLocation().getX() == myLocation().getX()
					&& humanToCarryMe.myLocation().getY() == myLocation().getY()) {
				// Help arrived follow the human helper
				condition = Condition.saved;
			}

			if (condition == Condition.saved) {
				moveToPoint(humanToCarryMe.myLocation());
			}
			if (wasHelped == 0) {
				// I'm injuerd need help. Send help request.
				ArrayList<Human> listNearHuman = findNearHuman(getAgent(), visionRadius);
				if (!listNearHuman.isEmpty()) {

					// SEND MESSAGE TO ALL Human in that place
					ACLMessage msgSend = new ACLMessage(ACLMessage.QUERY_IF);

					System.out.print(getLocalName() + " " + myLocation().getX() + " " + myLocation().getY() + " visionRadius " + visionRadius + " Found Human");

					// Define who's gone receive the message
					for (int i = 0; i < listNearHuman.size(); i++) {
						System.out.print(" at position:" + listNearHuman.get(i).myLocation().getX() + "," + listNearHuman.get(i).myLocation().getY());
						Human human = (Human) listNearHuman.get(i);
						msgSend.addReceiver(human.getAID());
					}
					System.out.println(" send Save ME request.");
					// Message Content
					msgSend.setContent(INJURY_MESSAGE);
					msgSend.setLanguage(codec.getName());
					msgSend.setOntology(serviceOntology.getName());

					// Send message
					send(msgSend);
				}
			}

		}

		@Override
		public boolean done() {
			return false;
		}
	}

}
