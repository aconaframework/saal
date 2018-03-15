package at.tuwien.ict.miklas.mind.aconamind.mind;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import at.tuwien.ict.acona.cell.cellfunction.CellFunctionThreadImpl;
import at.tuwien.ict.acona.cell.cellfunction.SyncMode;
import at.tuwien.ict.acona.cell.config.DatapointConfig;
import at.tuwien.ict.acona.cell.datastructures.Datapoint;
import at.tuwien.ict.acona.cell.datastructures.DatapointBuilder;
import at.tuwien.ict.acona.cell.datastructures.JsonRpcRequest;
import at.tuwien.ict.acona.cell.datastructures.JsonRpcResponse;

public class AconaTestMind extends CellFunctionThreadImpl {
	
	private static Logger log = LoggerFactory.getLogger(AconaTestMind.class);
	
	public final static String INPUTBUFFER = "inputbuffer";
	public final static String OUTPUTBUFFER = "inputbuffer";
	//public final static String PERCEPTIONADDRESS = INPUTBUFFER + ".perception";
	public final static String SENSORDATAADDRESS = INPUTBUFFER + ".sensordata";
	public final static String SCOREADDRESS = INPUTBUFFER + ".score";
	public final static String ACIONADDRESS = OUTPUTBUFFER + ".action";
	
	private String action = "NONE";

	@Override
	protected void cellFunctionThreadInit() throws Exception {
		
		this.addManagedDatapoint(DatapointConfig.newConfig(SENSORDATAADDRESS, SENSORDATAADDRESS, SyncMode.SUBSCRIBEONLY));
		
	}
	
	@Override
	public JsonRpcResponse performOperation(JsonRpcRequest param, String caller) {
		// TODO Auto-generated method stub
		return null;
	}

	//static int count = 0;
	@Override
	protected void executeFunction() throws Exception {
		//log.debug("Create random actions");
		String[] allactions = {"MOVE_FORWARD", "TURN_LEFT", "TURN_RIGHT", "EAT", "ATTACK"};
		action = allactions[(int) (allactions.length*Math.random())];
		/*if(count > 5)
			action = "TURN_RIGHT";
		count++;*/
		//log.debug("Selected action={}", action);
		
	}

	@Override
	protected void executeCustomPreProcessing() throws Exception {
		
	}
	
	@Override
	protected void executeCustomPostProcessing() throws Exception {
		this.getCommunicator().write(DatapointBuilder.newDatapoint(ACIONADDRESS).setValue(action));
		log.debug("Written action={}", action);
		
	}

	@Override
	protected void updateDatapointsByIdOnThread(Map<String, Datapoint> data) {
		if (data.containsKey(SENSORDATAADDRESS)==true) {
			log.debug("Inputs received={}", data);
			this.setStart();
		}
	}

	@Override
	protected void shutDownExecutor() throws Exception {
		// TODO Auto-generated method stub
		
	}
}
