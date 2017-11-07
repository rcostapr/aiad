package evacuacao;

import jade.core.AID;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.StaleProxyException;
import repast.simphony.context.Context;
import repast.simphony.context.space.graph.NetworkBuilder;
import repast.simphony.context.space.grid.GridFactory;
import repast.simphony.context.space.grid.GridFactoryFinder;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.parameter.Parameters;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridBuilderParameters;
import repast.simphony.space.grid.SimpleGridAdder;
import repast.simphony.space.grid.WrapAroundBorders;
import sajas.core.Runtime;
import sajas.sim.repasts.RepastSLauncher;
import sajas.wrapper.ContainerController;

public class JEvacuationBuilder extends RepastSLauncher {

	public static final boolean USE_RESULTS_COLLECTOR = true;

	public static final boolean SEPARATE_CONTAINERS = false;

	private ContainerController mainContainer;
	private ContainerController agentContainer;
	private SceneBuilder myScene;

	@Override
	public String getName() {
		return "Evacuation Project -- SAJaS RepastS Simulation";
	}

	@Override
	protected void launchJADE() {

		Runtime rt = Runtime.instance();
		Profile p1 = new ProfileImpl();
		mainContainer = rt.createMainContainer(p1);

		if (SEPARATE_CONTAINERS) {
			Profile p2 = new ProfileImpl();
			agentContainer = rt.createAgentContainer(p2);
		} else {
			agentContainer = mainContainer;
		}

		launchAgents();
	}

	private void launchAgents() {
		System.out.println("launchAgents");
		AID resultsCollectorAID = null;
		if (USE_RESULTS_COLLECTOR) {
			// create results collector
			/*
			 * ResultsCollector resultsCollector = new ResultsCollector(N_HUMANS
			 * + N_SECURITY); mainContainer.acceptNewAgent("ResultsCollector",
			 * resultsCollector).start(); resultsCollectorAID =
			 * resultsCollector.getAID();
			 */
			myScene.setAgentContainer(agentContainer);
			myScene.createHumans();
			myScene.createSecurity();
			
		}
		System.out.println("launchAgents END");
	}

	@Override
	public Context<?> build(Context<Object> context) {
		System.out.println("context start");
		
		myScene = new SceneBuilder(context);

		//createHumans(grid, context, humanCount, radiusVision);

		//createSecurity(grid, context, securityCount);

		// Create agent interaction network
		NetworkBuilder<Object> netBuilder = new NetworkBuilder<Object>("Evacuation network", context, true);
		netBuilder.buildNetwork();

		System.out.println("context end");
		return super.build(context);
	}

}
