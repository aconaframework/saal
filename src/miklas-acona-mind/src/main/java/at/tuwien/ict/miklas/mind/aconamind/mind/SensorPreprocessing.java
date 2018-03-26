package at.tuwien.ict.miklas.mind.aconamind.mind;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import at.tuwien.ict.acona.cell.cellfunction.CellFunctionThreadImpl;
import at.tuwien.ict.acona.cell.cellfunction.SyncMode;
import at.tuwien.ict.acona.cell.config.DatapointConfig;
import at.tuwien.ict.acona.cell.datastructures.Chunk;
import at.tuwien.ict.acona.cell.datastructures.ChunkBuilder;
import at.tuwien.ict.acona.cell.datastructures.Datapoint;
import at.tuwien.ict.acona.cell.datastructures.DatapointBuilder;
import at.tuwien.ict.acona.cell.datastructures.JsonRpcRequest;
import at.tuwien.ict.acona.cell.datastructures.JsonRpcResponse;
import at.tuwien.ict.miklas.aconamind.datatransferobjects.*;
import at.tuwien.ict.miklas.aconamind.datatransferobjects.perceptionDTO.hasPerceptDTO;

public class SensorPreprocessing extends CellFunctionThreadImpl {

	private static Logger log = LoggerFactory.getLogger(SensorPreprocessing.class);
	private static Gson gson = new Gson();
	private int count = 0;
	private sensorDataDTO sensorDataDTO = new sensorDataDTO();
	
	// Set the range of view for the sensor. 
	public final static int SENSORRANGEDEFAULT = 5;
	public static int SensorRange = SENSORRANGEDEFAULT;  
	
	public final static String INPUTBUFFER = "inputbuffer";
	public final static String OUTPUTBUFFER = "inputbuffer";
	public final static String PERCEPTIONADDRESS = INPUTBUFFER + ".perception";
	public final static String SENSORDATAADDRESS = OUTPUTBUFFER + ".sensordata";
	
	@Override
	protected void cellFunctionThreadInit() throws Exception {
		this.addManagedDatapoint(DatapointConfig.newConfig(PERCEPTIONADDRESS, PERCEPTIONADDRESS, SyncMode.SUBSCRIBEONLY));
	}

	@Override
	protected void executeFunction() throws Exception {

		//long startTime = System.nanoTime();
		
		getSensorDataVer0();
		//getSensorDataVer1();
		
		//long stopTime = System.nanoTime();
	    //long elapsedTime = stopTime - startTime;
	    //log.debug("Execution Time={}", elapsedTime);
	}
	
	private void getSensorDataVer0() throws Exception {
		// Execution Time=14659624ns
		perceptionDTO per = gson.fromJson(this.getCommunicator().read(PERCEPTIONADDRESS).getValue(), perceptionDTO.class);
		sensorDataDTO.init(count++);
		
		for (Iterator<hasPerceptDTO> i = per.hasPercept.iterator(); i.hasNext();) {
			hasPerceptDTO item = i.next();
			if(!(item.BodyType.toLowerCase().equals("floor")) && (item.x == 0 || item.y == 0)) {				
				
				if((item.y == 0 && item.x < 0 && Math.abs(item.x) <= SensorRange) && (Math.abs(item.x) < sensorDataDTO.distanceLeft)) {
					sensorDataDTO.distanceLeft = Math.abs(item.x);
					sensorDataDTO.distanceLeftNormalized = (double) sensorDataDTO.distanceLeft / (double) SensorRange;
					sensorDataDTO.distanceLeftObjectName = item.Name;
				}
				else if((item.y == 0 && item.x > 0 && item.x <= SensorRange) && (item.x < sensorDataDTO.distanceRight)) {
					sensorDataDTO.distanceRight = item.x;
					sensorDataDTO.distanceRightNormalized = (double) sensorDataDTO.distanceRight / (double) SensorRange;
					sensorDataDTO.distanceRightObjectName = item.Name;
				}
				else if((item.x == 0 && item.y < 0 && Math.abs(item.y) <= SensorRange) && (Math.abs(item.y) < sensorDataDTO.distanceUp)) {
					sensorDataDTO.distanceUp = Math.abs(item.y);
					sensorDataDTO.distanceUpNormalized = (double) sensorDataDTO.distanceUp / (double) SensorRange;
					sensorDataDTO.distanceUpObjectName = item.Name;
				}
				else if((item.x == 0 && item.y > 0 && item.y <= SensorRange) && (item.y < sensorDataDTO.distanceDown)) {
					sensorDataDTO.distanceDown = item.y;
					sensorDataDTO.distanceDownNormalized = (double) sensorDataDTO.distanceDown / (double) SensorRange;
					sensorDataDTO.distanceDownObjectName = item.Name;
				}
			}
		}
	}
	
	/*private void getSensorDataVer1() throws Exception {
		// Execution Time=49307940ns
		String line = this.getCommunicator().read(PERCEPTIONADDRESS).toJsonString();
		String pattern = "\\{\"hasName\":\"\\w*\",\"hasType\":\"\\w*\",\"BodyType\":\"[^FLOOR]\\w*\",\"Name\":\"\\w*\",\"Id\":\"\\w*\",\"x\":-?0,\"y\":-?\\d+}|\\{\"hasName\":\"\\w*\",\"hasType\":\"\\w*\",\"BodyType\":\"[^FLOOR]\\w*\",\"Name\":\"\\w*\",\"Id\":\"\\w*\",\"x\":-?\\d+,\"y\":-?0}";
		//perceptionDTO per = gson.fromJson(this.getCommunicator().read(PERCEPTIONADDRESS).getValue(), perceptionDTO.class);
		sensorDataDTO.init(count++);
		
		Matcher m = Pattern.compile(pattern).matcher(line);
		
		while(m.find())
		{
			String test = line.substring(m.start(), m.end());
			hasPerceptDTO item = gson.fromJson(line.substring(m.start(), m.end()), hasPerceptDTO.class);
			if((item.x < 0 && item.y == 0) && (Math.abs(item.x) < sensorDataDTO.distanceLeft)) {
				sensorDataDTO.distanceLeft = Math.abs(item.x);
				sensorDataDTO.distanceLeftObjectName = item.Name;
			}
			else if((item.x > 0 && item.y == 0) && (item.x < sensorDataDTO.distanceRight)) {
				sensorDataDTO.distanceRight = item.x;
				sensorDataDTO.distanceRightObjectName = item.Name;
			}
			else if((item.x == 0 && item.y < 0) && (Math.abs(item.y) < sensorDataDTO.distanceDown)) {
				sensorDataDTO.distanceDown = Math.abs(item.y);
				sensorDataDTO.distanceUpObjectName = item.Name;
			}
			else if((item.x == 0 && item.y > 0) && (item.y < sensorDataDTO.distanceUp)) {
				sensorDataDTO.distanceUp = item.y;
				sensorDataDTO.distanceDownObjectName = item.Name;
			}
		}
	}*/

	@Override
	protected void executeCustomPostProcessing() throws Exception {
				
		this.getCommunicator().write(Arrays.asList(DatapointBuilder.newDatapoint(SENSORDATAADDRESS).setValue(sensorDataDTO.toJsonObject())));
		log.debug("Written sensor data={}", sensorDataDTO);
	}

	@Override
	protected void executeCustomPreProcessing() throws Exception {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public JsonRpcResponse performOperation(JsonRpcRequest param, String caller) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void updateDatapointsByIdOnThread(Map<String, Datapoint> data) {
		//Subscribe the input of the simulator, calculate the new sensor values and write to another 
		//datapoint, which is subscribed by the real acona mind.
		if (data.containsKey(PERCEPTIONADDRESS)==true) {
			log.debug("Inputs received={}", data);
			this.setStart();
		}
	}

	@Override
	protected void shutDownExecutor() throws Exception {
		// TODO Auto-generated method stub
		
	}
}
