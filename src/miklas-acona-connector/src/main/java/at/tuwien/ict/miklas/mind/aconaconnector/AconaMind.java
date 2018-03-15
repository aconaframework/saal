package at.tuwien.ict.miklas.mind.aconaconnector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.tuwien.ict.acona.cell.cellfunction.specialfunctions.CFStateGenerator;
import at.tuwien.ict.acona.cell.config.CellConfig;
import at.tuwien.ict.acona.cell.config.CellFunctionConfig;
import at.tuwien.ict.acona.cell.core.CellGateway;
import at.tuwien.ict.acona.cell.core.CellGatewayImpl;
import at.tuwien.ict.acona.cell.core.CellImpl;
import at.tuwien.ict.acona.launcher.SystemControllerImpl;
import entity.mind.BodyPerceptionInterface;
import entity.mind.ExternalMindBodyInterface;
import entity.mind.ExternalMindControlInterface;
import evaluator.EvaluatorMindInterface;



public class AconaMind implements ExternalMindControlInterface {

	private static final Logger log = LoggerFactory.getLogger(AconaMind.class); 
	private AconaMindGateway gateway;
	
	// private final JadeContainerUtil util = new JadeContainerUtil();
	//private SystemControllerImpl aconaLauncher;
	
	private ExternalMindBodyInterface gameMethods;
	
	private int count=0;
	
	public AconaMind(ExternalMindBodyInterface game) throws Exception {
		try {
			//Get the game methods into the connector
			gameMethods = game;
			
			//Start the Acona system, start JADE	
			SystemControllerImpl aconaLauncher = SystemControllerImpl.getLauncher();
			aconaLauncher.startDefaultSystem();
			
			//Create an agent that implements this interface
			//Create a config
			// Create service agent
			
			final String agentName = "MiklasAgentGateway";
			final String miklasFunctionName = "miklas";
			
			//Check if the with the mind already exists.
			CellGateway cellService = aconaLauncher.getAgent(agentName);
			if (cellService==null) {	//If not, create the agent and add the functions
				cellService = aconaLauncher.createAgent(CellConfig.newConfig(agentName, CellImpl.class)
						.addCellfunction(CellFunctionConfig.newConfig(miklasFunctionName, MiklasAconaFunction.class))
						.addCellfunction(CellFunctionConfig.newConfig("StateMonitor", CFStateGenerator.class)));
					cellService.getCommunicator().setDefaultTimeout(1000000);
			} else {	//If yes, then just add the game function
				cellService.getCell().addCellFunction(CellFunctionConfig.newConfig(miklasFunctionName, MiklasAconaFunction.class));
				if (cellService.getCell().getFunctionHandler().getCellFunction("StateMonitor")!=null) {
					cellService.getCell().addCellFunction(CellFunctionConfig.newConfig("StateMonitor", CFStateGenerator.class));
				}
			}
			
			
			synchronized (this) {
				try {
					this.wait(200);
				} catch (InterruptedException e) {

				}
			}
			
			gateway = (AconaMindGateway)cellService.getCell().getFunctionHandler().getCellFunction(miklasFunctionName);

			log.info("Acona-Miklas gateway initialized");
			
		} catch (Exception e) {
			log.error("Cannot start system");
			throw new Exception(e.getMessage());
		}
	}
	

	@Override
	public void startCycle() {
		try {
			
			BodyPerceptionInterface bodyPerception = gameMethods.getBodyPerception();
			double currentHealth = (double)bodyPerception.getCurrentHealth();
			double maxHealth = (double)bodyPerception.getMaxHealth();
			double absoluteHealthChange = (double)bodyPerception.getPainOrPleasure();
			double healthlevel = currentHealth/maxHealth;
			double healthchange = absoluteHealthChange/maxHealth;
			
			EvaluatorMindInterface score = this.getGameMethods().getEvalution();

			this.gateway.setSensorInputs(this.gameMethods.getExternalPerception(), healthlevel, healthchange, score);
			
			String action = gateway.getAction();
			this.gameMethods.setAction(action);
			log.info("Got action={}", action);
			
			count++;
		} catch (Exception e) {
			log.error("Cannot execute cycle", e);
		}
		
	}

	@Override
	public void killMind() {
		//this.aconaLauncher.stopSystem();
		log.info("Killed mind");
		
	}


	private ExternalMindBodyInterface getGameMethods() {
		return gameMethods;
	}
}
