package at.tuwien.ict.miklas.mind.aconamind.mind;

import static org.junit.Assert.*;

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

public class AconaTestMindTester {
	
	private static Logger log = LoggerFactory.getLogger(AconaTestMindTester.class);
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
			String agentFunctionName = "AconaTestMind";
			
			CellGatewayImpl agent = this.launcher.createAgent(CellConfig.newConfig(agentName)
					.addCellfunction(CellFunctionConfig.newConfig(agentFunctionName, AconaTestMind.class)));
			
			synchronized (this) {
				try {
					this.wait(20000);
				} catch (Exception e) {
					
				}
			}
			
			// SET SENSOR RANGE TO 4
			SensorPreprocessing.SensorRange = 4;

			int count = 0;
			sensorDataDTO sensorDataDTO = new sensorDataDTO();
			
			sensorDataDTO.init(count++);
			sensorDataDTO.distanceUp = 1;
			sensorDataDTO.distanceRight = 2;
			sensorDataDTO.distanceDown = 3;
			sensorDataDTO.distanceLeft = 4;
			sensorDataDTO.normalizeFeatureScaling(SensorPreprocessing.SensorRange + 1, 0);
			
			agent.getCommunicator().write(Arrays.asList(DatapointBuilder.newDatapoint(SensorPreprocessing.SENSORDATAADDRESS).setValue(sensorDataDTO.toJsonObject())));
			log.debug("Written sensor data={}", sensorDataDTO);
			
			for(int i = 0; i < 100; i++)
			{
				synchronized (this) {
					try {
						if(i > 0 && Math.floorMod(i, 4) == 0)
							this.wait();
						else
							this.wait(500);
					} catch (Exception e) {
						
					}
					
					log.debug("Read Sensor preprocessing data={}", agent.getCommunicator().read(AconaTestMind.ACIONADDRESS));
					String action = agent.getCommunicator().read(AconaTestMind.ACIONADDRESS).getValueAsString();
					
					sensorDataDTO.performAction(count++);
					
					agent.getCommunicator().write(Arrays.asList(DatapointBuilder.newDatapoint(SensorPreprocessing.SENSORDATAADDRESS).setValue(sensorDataDTO.toJsonObject())));
					log.debug("Written sensor data={}", sensorDataDTO);
					
					//if(i == 5)
					//	continue;
				}
			}
			
			synchronized (this) {
				try {
					this.wait(100000);
				} catch (Exception e) {
					
				}
			}
			
		} catch (Exception e) {
			log.error("Error testing system", e);
			fail("Error");
		}
	}
}
