package at.tuwien.ict.miklas.mind.aconaconnector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.tuwien.ict.acona.cell.cellfunction.CellFunctionThreadImpl;
import at.tuwien.ict.acona.cell.cellfunction.SyncMode;
import at.tuwien.ict.acona.cell.config.DatapointConfig;
import at.tuwien.ict.acona.cell.datastructures.Chunk;
import at.tuwien.ict.acona.cell.datastructures.ChunkBuilder;
import at.tuwien.ict.acona.cell.datastructures.Datapoint;
import at.tuwien.ict.acona.cell.datastructures.DatapointBuilder;
import at.tuwien.ict.acona.cell.datastructures.JsonRpcRequest;
import at.tuwien.ict.acona.cell.datastructures.JsonRpcResponse;
import entity.mind.ExternalPerceptionInterface;
import evaluator.EvaluatorMindInterface;

public class MiklasAconaFunction extends CellFunctionThreadImpl implements AconaMindGateway {

	public final static String INPUTBUFFER = "inputbuffer";
	public final static String OUTPUTBUFFER = "inputbuffer";
	public final static String PERCEPTIONADDRESS = INPUTBUFFER + ".perception";
	public final static String SCOREADDRESS = INPUTBUFFER + ".score";
	public final static String ACIONADDRESS = OUTPUTBUFFER + ".action";
	
	private static Logger log = LoggerFactory.getLogger(MiklasAconaFunction.class);
	
	private double health = 0;
	private double healthchange = 0;
	private ArrayList<ExternalPerceptionInterface> perception = new ArrayList<ExternalPerceptionInterface>();
	private EvaluatorMindInterface score;
	private String action = "NONE";
	
	int count = 0;
	
	@Override
	public JsonRpcResponse performOperation(JsonRpcRequest param, String caller) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void cellFunctionThreadInit() throws Exception {
		//this.setExecuteRate(500);
		//this.setExecuteOnce(false);
		//this.setStart();
		
		this.addManagedDatapoint(DatapointConfig.newConfig(ACIONADDRESS, ACIONADDRESS, SyncMode.SUBSCRIBEONLY));
		
		log.info("Initialized Miklas gateway function={} in agent={}", this.getFunctionName(), this.getCell().getName());
	}

	@Override
	protected void executeFunction() throws Exception {
		//log.info("Start the mind of the system");
		
		//Random Action
		//if (Math.random()<0.05) {
//		log.debug("Random actions");
//		String[] allactions = {"MOVE_FORWARD", "TURN_LEFT", "TURN_RIGHT", "EAT", "ATTACK"};
//		action = allactions[(int) (allactions.length*Math.random())];
		//}
		
		count++;
	}

	@Override
	protected void executeCustomPreProcessing() throws Exception {
		log.info("Gather Miklas simulation data");
		
		Chunk inputs= ChunkBuilder.newChunk("Inputs", "INPUT");
		inputs.setValue("Timestamp", count);
		inputs.setValue("Healthchange", healthchange);
		inputs.setValue("Health", health);
		
		//Get perception
		for (ExternalPerceptionInterface p : perception) {
			Chunk percept=ChunkBuilder.newChunk(p.getObjectIdentifier(), "PERCEPT")
					.setValue("BodyType", p.getObjectBodyType())
					.setValue("Name", p.getObjectName())
					.setValue("Id", p.getObjectIdentifier())
					.setValue("x", p.getXRelativeCoordinate())
					.setValue("y", p.getYRelativeCoordinate());
			
			inputs.addAssociatedContent("hasPercept", percept);
		}
		
		Chunk score = ChunkBuilder.newChunk("Score", "INPUT")
				.setValue("Score", this.score.getScore())
				.setValue("PositiveActions", this.score.getPositiveActions())
				.setValue("NegativeActions", this.score.getNegativeActions())
				.setValue("NeutralActions", this.score.getNeutralActions());
				
		
		this.getCommunicator().write(Arrays.asList(DatapointBuilder.newDatapoint(PERCEPTIONADDRESS).setValue(inputs.toJsonObject()), 
				DatapointBuilder.newDatapoint(SCOREADDRESS).setValue(score.toJsonObject())));
		log.debug("Written new perception values ={} from Miklas into the input buffer={} of Acona", inputs, PERCEPTIONADDRESS);
		log.debug("Written new score values ={} from Miklas into the input buffer={} of Acona", score, SCOREADDRESS);
		
	}
	
	@Override
	protected void executeCustomPostProcessing() throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void updateDatapointsByIdOnThread(Map<String, Datapoint> data) {
		if (data.containsKey(ACIONADDRESS)) {
			synchronized (this.action) {
				this.action = data.get(ACIONADDRESS).getValueAsString();
			}
		}
		
	}

	@Override
	protected void shutDownExecutor() throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getAction() {
		return this.action;
	}

	@Override
	public void setSensorInputs(ArrayList<ExternalPerceptionInterface> perception, double healthlevel, double healthChange, EvaluatorMindInterface score) {
		this.healthchange = healthChange;
		this.perception = perception;
		this.health = healthlevel;
		this.score = score;
		this.setStart();
		log.debug("Written sensor values");
	}

}
