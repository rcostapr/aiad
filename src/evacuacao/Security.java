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
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import repast.simphony.context.Context;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ISchedule;
import repast.simphony.engine.schedule.ScheduleParameters;
import repast.simphony.query.space.grid.GridCell;
import repast.simphony.query.space.grid.GridCellNgh;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;
import sajas.core.Agent;
import sajas.core.behaviours.CyclicBehaviour;
import sajas.core.behaviours.SimpleBehaviour;

public class Security extends Agent {

	public static final String FIRE_MESSAGE = "FIRE Run to EXIT !!!";

	private Grid<Object> grid;
	// private Context<Object> context;

	protected Codec codec;
	protected Ontology serviceOntology;
	private int exitAlive;
	private int alive;
	private int fireAlert = 0;

	private GridPoint humanPoint;

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
		// this.context = context;
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

	public GridPoint myLocation() {
		return grid.getLocation(this);
	}

	public void moveTowards(GridPoint pt) {
		double distToExit = 999999;
		int indexDoor = -1;

		// Move To Shortest Exit
		List<Door> doors = new ArrayList<Door>();
		for (Object obj : grid.getObjects()) {
			if (obj instanceof Door) {
				if (validPath(((Door) obj).getLocation()))
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
			if (obj instanceof Wall || obj instanceof Fire) {
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
		// System.out.println("######### Animation START SEC #########");
		// register language and ontology
		codec = new SLCodec();
		serviceOntology = ServiceOntology.getInstance();
		getContentManager().registerLanguage(codec);
		getContentManager().registerOntology(serviceOntology);

		// add behaviours
		addBehaviour(new moveHandler(this));
		addBehaviour(new receiveFireAlertBehaviour(this));
		addBehaviour(new helpBehaviour(this));
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
		private static final long serialVersionUID = 1L;

		public moveHandler(Agent a) {
			super(a);
		}

		public void action() {

			if (done()) {
				System.out.println("Security done");
				return;
			}

			List<Human> humans = getHumans();
			// System.out.println("Humans qtd: " + humans.size());

			// Nobody left behind go to exit
			if (humans.size() == 0 || findNearFire(3)) {
				moveTowards(myLocation());
				return;
			}
			// #############################

		}

		@Override
		public boolean done() {
			if (checkDoorAtLocation(myLocation().getX(), myLocation().getY())) {
				System.out.println(getLocalName() + " Found Door -> " + myLocation().getX() + " : " + myLocation().getY());
				exitAlive = 1;
				System.out.println(getLocalName() + " out alive");
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
				if (fireAlert == 0) {
					fireAlert = 1;
					// Create repeated Fire alert behaviour
					// Current Tick
					double start = RunEnvironment.getInstance().getCurrentSchedule().getTickCount();
					double startAt = start + 5;
					// System.out.println(RunEnvironment.getInstance().getCurrentSchedule().getTickCount());
					ISchedule schedule = RunEnvironment.getInstance().getCurrentSchedule();
					ScheduleParameters scheduleParams = ScheduleParameters.createRepeating(startAt, 5.0);
					schedule.schedule(scheduleParams, this, "repeatAlert");
					// Improve security help
					// Current Tick
					System.out.println(RunEnvironment.getInstance().getCurrentSchedule().getTickCount());
					ISchedule nschedule = RunEnvironment.getInstance().getCurrentSchedule();
					ScheduleParameters nscheduleParams = ScheduleParameters.createRepeating(startAt, 6.0);
					nschedule.schedule(nscheduleParams, this, "GoHelpHuman");
				}

				return true;
			}
		}

		return false;
	}

	public void repeatAlert() {
		System.out.println("Security Execute repeatAlert");
		if (exitAlive == 0) {
			// System.out.println(getLocalName() + " Repeat Fire Alert.");
			// find people in the surrounding area
			ArrayList<AID> humanNear = findNearAgents(this, 6);
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

	public void GoHelpHuman() {

		List<Human> humans = getHumans();

		if (humanPoint != null && (getLocation().getX() == humanPoint.getX() && getLocation().getY() == humanPoint.getY())) {
			humanPoint = null;
		}
		if (humans.size() < 5 && humanPoint == null && fireAlert == 1) {
			int move_index = RandomHelper.nextIntFromTo(0, humans.size() - 1);
			humanPoint = humans.get(move_index).myLocation();
		}
		if (humanPoint != null)
			moveToPoint(humanPoint);
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

	public ArrayList<AID> findNearAgents(Agent myAgent, int radius) {
		ArrayList<AID> neighboursList = new ArrayList<AID>();

		GridCellNgh<Human> neighbourhood = new GridCellNgh<Human>(grid, grid.getLocation(myAgent), Human.class, radius, radius);
		List<GridCell<Human>> nghPoints = neighbourhood.getNeighborhood(false);

		// NOTA: neighbourhood não tem em consideração as parede
		// e valores opostos na grid
		// Ex: Grid 40x40
		// o ponto (0,0) está a 1 de distância do ponto (0,39)
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

	private List<Human> getHumans() {
		List<Human> humans = new ArrayList<Human>();
		for (Object obj : grid.getObjects()) {
			if (obj instanceof Human) {
				// Humano ainda não saiu e está vivo Segurança espera
				if (((Human) obj).getAlive() == 1)
					if (((Human) obj).getKnowExit() == 0)
						humans.add((Human) obj);
			}
		}
		return humans;
	}
	// #######################################
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
			// System.out.println("Execute receiveFireAlertBehaviour");
			// Evitar que as pessoas que tenham detetado o fogo enviem o alerta
			// pois já o enviaram em fireDetectedBehaviour
			if (fireAlert == 1) {
				System.out.println("Security Remove receiveFireAlertBehaviour");
				removeBehaviour(this);
				return;
			}

			ACLMessage msg = receive(template);
			if (msg != null) {
				if (msg.getContent().indexOf("FIRE") != -1) {
					fireAlert = 1;
					System.out.println(getLocalName() + " receive fire alert: " + msg.getContent());
					// find people in the surrounding area
					ArrayList<AID> humanNear = findNearAgents(myAgent, 6);
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
					// System.out.println(getLocalName() + " Send Fire Alert message heared.");

					alertsend = true;

					// Create repeated Fire alert behaviour
					// Current Tick
					double start = RunEnvironment.getInstance().getCurrentSchedule().getTickCount();
					double startAt = start + 5;
					// System.out.println(RunEnvironment.getInstance().getCurrentSchedule().getTickCount());
					ISchedule schedule = RunEnvironment.getInstance().getCurrentSchedule();
					ScheduleParameters scheduleParams = ScheduleParameters.createRepeating(startAt, 5.0);
					schedule.schedule(scheduleParams, this, "repeatAlert");

					// Improve security help
					// Current Tick
					System.out.println(RunEnvironment.getInstance().getCurrentSchedule().getTickCount());
					ISchedule nschedule = RunEnvironment.getInstance().getCurrentSchedule();
					ScheduleParameters nscheduleParams = ScheduleParameters.createRepeating(startAt, 6.0);
					nschedule.schedule(nscheduleParams, this, "goHelpHuman");
				}
			}
		}

		public void repeatAlert() {
			// System.out.println("Execute repeatAlert");
			if (exitAlive == 0) {
				// System.out.println(getLocalName() + " Repeat Fire Alert.");
				// find people in the surrounding area
				ArrayList<AID> humanNear = findNearAgents(myAgent, 6);
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

		public void goHelpHuman() {
			List<Human> humans = getHumans();

			if (humans.size() == 0)
				return;
			System.out.println(getLocalName() + " go help Human");
			if (humanPoint != null && (getLocation().getX() == humanPoint.getX() && getLocation().getY() == humanPoint.getY())) {
				humanPoint = null;
			}
			if (humans.size() < 5 && humanPoint == null && fireAlert == 1) {
				int move_index = RandomHelper.nextIntFromTo(0, humans.size() - 1);
				humanPoint = humans.get(move_index).myLocation();
			}
			if (humanPoint != null)
				moveToPoint(humanPoint);
		}

		@Override
		public boolean done() {
			return alertsend;
		}
	}
	// #######################################
	/**
	 * Helper behaviour
	 * Try to get help for the emergency situation
	 * Try to know where exit is
	 */
	class helpBehaviour extends CyclicBehaviour {
		private static final long serialVersionUID = 1L;
		private MessageTemplate template = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.QUERY_REF),
				MessageTemplate.MatchOntology(ServiceOntology.ONTOLOGY_NAME));
		
		public helpBehaviour(Agent a) {
			super(a);
		}

		@Override
		public void action() {
			ACLMessage msg = null;
			// Allow security answer to more than one human at same time
			while ((msg = receive(template)) != null) {
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
	// #######################################

}
