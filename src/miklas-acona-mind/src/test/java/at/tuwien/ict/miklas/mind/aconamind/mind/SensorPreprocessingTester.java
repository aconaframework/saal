package at.tuwien.ict.miklas.mind.aconamind.mind;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import at.tuwien.ict.acona.cell.config.CellConfig;
import at.tuwien.ict.acona.cell.config.CellFunctionConfig;
import at.tuwien.ict.acona.cell.core.CellGatewayImpl;
import at.tuwien.ict.acona.cell.datastructures.Chunk;
import at.tuwien.ict.acona.cell.datastructures.ChunkBuilder;
import at.tuwien.ict.acona.cell.datastructures.DatapointBuilder;
import at.tuwien.ict.acona.launcher.SystemControllerImpl;
import at.tuwien.ict.miklas.aconamind.datatransferobjects.sensorDataDTO;

public class SensorPreprocessingTester {
	private static Logger log = LoggerFactory.getLogger(SensorPreprocessingTester.class);
	// private final JadeContainerUtil util = new JadeContainerUtil();
	private SystemControllerImpl launcher = SystemControllerImpl.getLauncher();
	// private Gateway comm = commUtil.getJadeGateway();

	@Before
	public void setUp() throws Exception {
		try {
			// Create container
			log.debug("Create or get main container");
			this.launcher.createMainContainer("localhost", 1099, "MainContainer");

			log.debug("Create subcontainer");
			this.launcher.createSubContainer("localhost", 1099, "Subcontainer");

			// log.debug("Create gui");
			// this.commUtil.createDebugUserInterface();

			// Create gateway
			// commUtil.initJadeGateway();

		} catch (Exception e) {
			log.error("Cannot initialize test environment", e);
		}
	}

	@After
	public void tearDown() throws Exception {
		synchronized (this) {
			try {
				this.wait(200);
			} catch (InterruptedException e) {

			}
		}

		this.launcher.stopSystem();

		synchronized (this) {
			try {
				this.wait(200);
			} catch (InterruptedException e) {

			}
		}
	}

	/**
	 * Idea: Generate a game grid and place the computer player in the middle of it. Add some obstacles to the game grid and check if 
	 * the distances to that obstacles are calculated right.
	 */
	@Test
	public void sensorPreprocessingTester() {
		try {
			String agentName=  "testAgent";
			String agentFunctionName = "SensorPreprocessing";
			
			CellGatewayImpl agent = this.launcher.createAgent(CellConfig.newConfig(agentName)
					.addCellfunction(CellFunctionConfig.newConfig(agentFunctionName, SensorPreprocessing.class)));

			synchronized (this) {
				try {
					this.wait(100);
				} catch (Exception e) {
					
				}
			}
			
			// GENERATE EXTERNAL PERCEPTION REQUEST
			Chunk inputs= ChunkBuilder.newChunk("Inputs", "INPUT");
			inputs.setValue("Timestamp", 4);
			inputs.setValue("Healthchange", 1);
			inputs.setValue("Health", 99);
			
			//Get perception
			ArrayList<Obstacle> obstacles = new ArrayList<>();
			obstacles.add(new Obstacle(new Point(0, -5), "OBSTACLE1"));
			obstacles.add(new Obstacle(new Point(0, -4), "OBSTACLE2"));
			obstacles.add(new Obstacle(new Point(0, 2), "OBSTACLE3"));
			obstacles.add(new Obstacle(new Point(0, 4), "OBSTACLE4"));
			obstacles.add(new Obstacle(new Point(6, 0), "OBSTACLE5"));
			obstacles.add(new Obstacle(new Point(7, 0), "OBSTACLE6"));
			obstacles.add(new Obstacle(new Point(-5, 0), "OBSTACLE7"));
			obstacles.add(new Obstacle(new Point(-3, 0), "OBSTACLE8"));
			generateExternalPerception(inputs, obstacles, 7);
					
			
			agent.getCommunicator().write(Arrays.asList(DatapointBuilder.newDatapoint(SensorPreprocessing.PERCEPTIONADDRESS).setValue(inputs.toJsonObject())));
			
			synchronized (this) {
				try {
					this.wait(300);
				} catch (Exception e) {
					
				}
			}
			
			log.debug("Read Sensor preprocessing data={}", agent.getCommunicator().read(SensorPreprocessing.SENSORDATAADDRESS));
			sensorDataDTO result = new Gson().fromJson(agent.getCommunicator().read(SensorPreprocessing.SENSORDATAADDRESS).getValue(), sensorDataDTO.class);

			log.debug("Distance down: correct value={}, actual value={}", 2, result.distanceDown);
			log.debug("Distance down obstacle: correct value={}, actual value={}", "OBSTACLE3", result.distanceDownObjectName);
			log.debug("Distance up: correct value={}, actual value={}", 4, result.distanceUp);
			log.debug("Distance up obstacle: correct value={}, actual value={}", "OBSTACLE2", result.distanceUpObjectName);
			log.debug("Distance right: correct value={}, actual value={}", 6, result.distanceRight);
			log.debug("Distance right obstacle: correct value={}, actual value={}", "OBSTACLE5", result.distanceRightObjectName);
			log.debug("Distance left: correct value={}, actual value={}", 3, result.distanceLeft);
			log.debug("Distance left obstacle: correct value={}, actual value={}", "OBSTACLE8", result.distanceLeftObjectName);

			assertEquals(result.distanceDown, 2);
			assertEquals(result.distanceDownObjectName, "OBSTACLE3");
			assertEquals(result.distanceUp, 4);
			assertEquals(result.distanceUpObjectName, "OBSTACLE2");
			assertEquals(result.distanceLeft, 3);
			assertEquals(result.distanceLeftObjectName, "OBSTACLE8");
			assertEquals(result.distanceRight, 6);
			assertEquals(result.distanceRightObjectName, "OBSTACLE5");
			log.info("Test passed");
		} catch (Exception e) {
			log.error("Error testing system", e);
			fail("Error");
		}

	}
	
	private void generateExternalPerception(Chunk inputs, ArrayList<Obstacle> obstacles, int gameSize) throws Exception
	{
		int counter = 0;
		//assume AI is placed in the middle of the gamefield
		
		if(Math.floorMod(gameSize, 2) == 0)
			gameSize = gameSize + 1;
		
		for (int x = -gameSize; x < gameSize; x++) 
			for (int y = -gameSize; y < gameSize; y++) {
				Chunk percept=ChunkBuilder.newChunk("FLOOR_GREY" + counter, "PERCEPT")
						.setValue("BodyType", "FLOOR")
						.setValue("Name", "FLOOR_GREY")
						.setValue("Id", "FLOOR_GREY" + counter)
						.setValue("x", x)
						.setValue("y", y);
				inputs.addAssociatedContent("hasPercept", percept);
				counter++;
		}
		
		for(Obstacle p : obstacles)
		{
			Chunk percept=ChunkBuilder.newChunk(p.name + counter, "PERCEPT")
					.setValue("BodyType", "COLLECTOBJECTBODY")
					.setValue("Name", p.name)
					.setValue("Id", p.name + counter)
					.setValue("x", (int) p.point.getX())
					.setValue("y", (int) p.point.getY());
			inputs.addAssociatedContent("hasPercept", percept);
			counter++;
		}
	}
	
	protected class Obstacle
	{
		public Point point;
		public String name;
		
		public Obstacle(int x, int y, String name)
		{
			this.point = new Point(x, y);
			this.name = name;
		}
		
		public Obstacle(Point point, String name)
		{
			this.point = point;
			this.name = name;
		}
	}
}
