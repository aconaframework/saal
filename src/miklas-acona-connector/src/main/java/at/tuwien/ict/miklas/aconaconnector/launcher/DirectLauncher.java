package at.tuwien.ict.miklas.aconaconnector.launcher;

import java.awt.Dimension;
import java.awt.Toolkit;

import javax.imageio.spi.RegisterableService;
import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.tuwien.ict.acona.cell.cellfunction.specialfunctions.CFStateGenerator;
import at.tuwien.ict.acona.cell.config.CellConfig;
import at.tuwien.ict.acona.cell.config.CellFunctionConfig;
import at.tuwien.ict.acona.cell.core.CellGateway;
import at.tuwien.ict.acona.cell.core.CellGatewayImpl;
import at.tuwien.ict.acona.cell.core.CellImpl;
import at.tuwien.ict.acona.launcher.SystemControllerImpl;
import at.tuwien.ict.miklas.mind.aconaconnector.MiklasAconaFunction;
import at.tuwien.ict.miklas.mind.aconamind.mind.AconaTestMind;
import at.tuwien.ict.miklas.mind.aconamind.mind.SensorPreprocessing;
import at.tuwien.ict.miklas.mind.aconamind.restserver.JerseyRestServer;
import at.tuwien.ict.miklas.mind.aconamind.restserver.RestService;
import userinterface.MusicManagerImpl;
import userinterface.MusicManager;
import userinterface.SoundManager;
import userinterface.SoundManagerImpl;
import userinterface.Visualization;
import config.ConfigLoader;
import evaluator.EvaluatorManager;
import evaluator.EvaluatorManagerMindInterface;
import gameengine.GameEngineImpl;

public class DirectLauncher {
	
	private static final Logger	log	= LoggerFactory.getLogger(DirectLauncher.class);
	
	private static DirectLauncher instance = null;
	
	private SystemControllerImpl aconaLauncher = SystemControllerImpl.getLauncher();
	
	public DirectLauncher() {
		try {
			log.info("Start Miklas 1.0");
			
			//Load parameters from config file
			ConfigLoader conf = null;
			try {
				conf = ConfigLoader.getConfig();
				conf.init();
			} catch (Exception e) {
				log.error("Could not load config. Exit program", e);
				System.exit(-1);
			}
			
		    //=== Add external ACONA functions ===//
		    this.addRestInterface();		//Just for demonstration of the function
		    this.addAconaMind();	//Add the external acona mind
		    this.addSensorPreprocessing();
			
			//Load config
			//WorldConfig world = new WorldConfig();	
			
			//Init visualization
			final Visualization vis = new Visualization();
			
		    final GameEngineImpl gameEngine = new GameEngineImpl();
		    
			//Load score manager
			EvaluatorManagerMindInterface scoreManager = new EvaluatorManager(vis);
		    
			//Load sound manager
			SoundManager soundManager = new SoundManagerImpl(gameEngine.getGameGrid());
			
			
			//Load music manager and play music
			MusicManager musicManager = new MusicManagerImpl();
			String relativeMusicPath = conf.getMusicConfig().getRelativMusicPath();
			if (relativeMusicPath.equals("")==false) {
				musicManager.playMusic(relativeMusicPath);
			}
			
			//Set sound manager in the gameengine
			gameEngine.setSoundManager(soundManager);
			gameEngine.setScoreManager(scoreManager);
			
			//As sson as everything is assigned, init the game engine
			gameEngine.init();
		    
		    //Start Visualization
		    SwingUtilities.invokeLater(new Runnable() {
		    	public void run() {
		    		//Visualization oVis = new Visualization();
		    		vis.setGameEngine(gameEngine);
		    		vis.init();
		    		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		    		vis.setLocation(dim.width/2-vis.getSize().width/2, dim.height/2-vis.getSize().height/2);
		    		vis.setVisible(true);
		    	}
		    });
		    
		} catch (Exception e) {
			log.error("Cannot run Miklas", e);
			System.exit(-1);
		}
		
	}
	
	public static void main(String[] args) {
		try {
			instance = new DirectLauncher();
		} catch (Exception e) {
			log.error("Cannot start Miklas", e);
		}
	}
	
	private void addRestInterface() throws Exception {
		try {
			log.info("Start Rest server");
			//Start the Acona system, start JADE
			aconaLauncher = SystemControllerImpl.getLauncher();
			aconaLauncher.startDefaultSystem();
			
			//Create an agent that implements this interface
			//Create a config
			// Create service agent
			final String agentName = "RestInterface2";
			final String miklasFunctionName = "RestServer";
			
			CellGatewayImpl cellService = aconaLauncher.createAgent(CellConfig.newConfig(agentName, CellImpl.class)
				.addCellfunction(CellFunctionConfig.newConfig(miklasFunctionName, JerseyRestServer.class)
						.setProperty(RestService.PARAMAGENTSYSTEMSTATEADDRESS, "MiklasAgentGateway" + ":" + CFStateGenerator.SYSTEMSTATEADDRESS)
						.setProperty(RestService.PARAMINPUTADDRESS, "MiklasAgentGateway" + ":" + SensorPreprocessing.PERCEPTIONADDRESS)));	//Add the rest service
			cellService.getCommunicator().setDefaultTimeout(10000);
			
			synchronized (this) {
				try {
					this.wait(2000);
				} catch (InterruptedException e) {

				}
			}
		} catch (Exception e) {
			log.error("Cannot start the additional functions of the ACONA agent", e);
			throw new Exception(e.getMessage());
		}
	}
	
	private void addAconaMind() throws Exception {
		log.info("Add the ACONA mind function to the agent");
		
		CellFunctionConfig functionConfig = CellFunctionConfig.newConfig("AconaMind", AconaTestMind.class);
		CellGateway cell = aconaLauncher.getAgent("MiklasAgentGateway");
		if (cell==null) {
			log.info("Cell={} does not exist. Create cell", "MiklasAgentGateway");
			cell = aconaLauncher.createAgent(CellConfig.newConfig("MiklasAgentGateway", CellImpl.class));
			synchronized (this) {
				try {
					this.wait(200);
				} catch (Exception e) {
					
				}
			}
		}
		
		log.debug("Got cell={}", cell.getCell().getName());
		try {
			cell.getCell().addCellFunction(functionConfig);
		} catch (Exception e) {
			log.error("Cannot add function={}", functionConfig, e);
			throw new Exception (e.getMessage());
		}
	}
	
	private void addSensorPreprocessing() throws Exception {
		log.info("Add the Sensor Preprocessing function to the agent");
		
		CellFunctionConfig functionConfig = CellFunctionConfig.newConfig("SensorPreprocessing", SensorPreprocessing.class);
		CellGateway cell = aconaLauncher.getAgent("MiklasAgentGateway");
		if (cell==null) {
			log.info("Cell={} does not exist. Create cell", "MiklasAgentGateway");
			cell = aconaLauncher.createAgent(CellConfig.newConfig("MiklasAgentGateway", CellImpl.class));
			synchronized (this) {
				try {
					this.wait(200);
				} catch (Exception e) {
					
				}
			}
		}
		
		log.debug("Got cell={}", cell.getCell().getName());
		try {
			cell.getCell().addCellFunction(functionConfig);
		} catch (Exception e) {
			log.error("Cannot add function={}", functionConfig, e);
			throw new Exception (e.getMessage());
		}
	}
}
