package evacuacao;

import jade.core.Profile;
import jade.core.ProfileImpl;
import repast.simphony.context.Context;
import repast.simphony.context.space.graph.NetworkBuilder;
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
		if (USE_RESULTS_COLLECTOR) {
			myScene.setAgentContainer(agentContainer);
			myScene.createHumans();
			myScene.createSecurity();

		}
		System.out.println("launchAgents END");
		System.out.println("Schedule Fire");
		myScene.scheduleFire();
	}

	@Override
	public Context<?> build(Context<Object> context) {
		clearScreen();
		System.out.println("context start");

		myScene = new SceneBuilder(context);

		// Create agent interaction network
		NetworkBuilder<Object> netBuilder = new NetworkBuilder<Object>("Evacuation network", context, true);
		netBuilder.buildNetwork();

		System.out.println("context end");
		return super.build(context);
	}

	public static void clearScreen() {
		for (int i = 0; i < 200; i++)
			System.out.println("");
	}

}
