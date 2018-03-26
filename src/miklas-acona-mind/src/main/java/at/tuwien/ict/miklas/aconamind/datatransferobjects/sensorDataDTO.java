package at.tuwien.ict.miklas.aconamind.datatransferobjects;

import javax.json.Json;

import com.google.gson.JsonObject;

import at.tuwien.ict.acona.cell.datastructures.Chunk;
import at.tuwien.ict.acona.cell.datastructures.ChunkBuilder;

public class sensorDataDTO extends chunkTimestampHeaderDTO {
	
	public int distanceUp = Integer.MAX_VALUE; 
	public String distanceUpObjectName = "";
	public int distanceDown = Integer.MAX_VALUE;
	public String distanceDownObjectName = "";
	public int distanceRight = Integer.MAX_VALUE;
	public String distanceRightObjectName = "";
	public int distanceLeft = Integer.MAX_VALUE;
	public String distanceLeftObjectName = "";
	
	public double distanceUpNormalized = 1.0;
	public double distanceDownNormalized = 1.0;
	public double distanceRightNormalized = 1.0;
	public double distanceLeftNormalized = 1.0;
	
	
	public void init()
	{
		distanceUp = Integer.MAX_VALUE;
		distanceDown = Integer.MAX_VALUE;
		distanceRight = Integer.MAX_VALUE;
		distanceLeft = Integer.MAX_VALUE;
		distanceUpNormalized = 1.0;
		distanceDownNormalized = 1.0;
		distanceRightNormalized = 1.0;
		distanceLeftNormalized = 1.0;
		distanceUpObjectName = "";
		distanceDownObjectName = "";
		distanceLeftObjectName = "";
		distanceRightObjectName = "";
	}
	
	public void init(int timestamp) {
		init();
		this.Timestamp = timestamp;
	}
	
	public void normalizeFeatureScaling(double max, double min)
	{
		//sensor data can become greater than sensor range -> no object in range of view
		if(distanceUp > max)
			distanceUpNormalized = 1.0;
		else
			distanceUpNormalized = (distanceUp - min) / (max - min);
		
		if(distanceRight > max)
			distanceRightNormalized = 1.0;
		else
			distanceRightNormalized = (distanceRight - min) / (max - min);
		
		if(distanceDown > max)
			distanceDownNormalized = 1.0;
		else
			distanceDownNormalized = (distanceDown - min) / (max - min);
		if(distanceLeft > max)
			distanceLeftNormalized = 1.0;
		else
			distanceLeftNormalized = (distanceLeft - min) / (max - min);
	}
	
	@Override
	public String toString() {
		return String.format("{Timestamp=%d, SensorData={DistanceUp=%d, DistanceUpObjectName=%s},"
				+ "{DistanceDown=%d, DistanceLeftObjectName=%s},"
				+ "{DistanceRight=%d, DistanceRightObjectName=%s},"
				+ "{DistanceLeft=%d, DistanceLeftObjectName=%s}}", 
				Timestamp, distanceUp, distanceUpObjectName, distanceDown, distanceDownObjectName,
				distanceRight, distanceRightObjectName, distanceLeft, distanceLeftObjectName);			
	}
	
	public JsonObject toJsonObject() throws Exception {
		Chunk sensorData = ChunkBuilder.newChunk("SensorData", "SENSORDATA");
		sensorData.setValue("Timestamp", this.Timestamp);
		sensorData.setValue("distanceUp", this.distanceUp);
		sensorData.setValue("distanceUpNormalized", this.distanceUpNormalized);
		sensorData.setValue("distanceUpObjectName", this.distanceUpObjectName);
		sensorData.setValue("distanceDown", this.distanceDown);
		sensorData.setValue("distanceDownNormalized", this.distanceDownNormalized);
		sensorData.setValue("distanceDownObjectName", this.distanceDownObjectName);
		sensorData.setValue("distanceRight", this.distanceRight);
		sensorData.setValue("distanceRightNormalized", this.distanceRightNormalized);
		sensorData.setValue("distanceRightObjectName", this.distanceRightObjectName);
		sensorData.setValue("distanceLeft", this.distanceLeft);
		sensorData.setValue("distanceLeftNormalized", this.distanceLeftNormalized);
		sensorData.setValue("distanceLeftObjectName", this.distanceLeftObjectName);
		return sensorData.toJsonObject();
	}
	
	public void performAction(int timestamp)
	{
		this.Timestamp = timestamp;
		
		//if rotateRight then
		int helperUp = this.distanceUp;
		int helperDown = this.distanceDown;
		int helperRight = this.distanceRight;
		int helperLeft = this.distanceLeft;
		double helperUpNorm = this.distanceUpNormalized;
		double helperDownNorm = this.distanceDownNormalized;
		double helperRightNorm = this.distanceRightNormalized;
		double helperLeftNorm = this.distanceLeftNormalized;
		
		this.distanceUp = helperRight;
		this.distanceRight = helperDown;
		this.distanceDown = helperLeft;
		this.distanceLeft = helperUp;
		this.distanceUpNormalized = helperRightNorm;
		this.distanceRightNormalized = helperDownNorm;
		this.distanceDownNormalized = helperLeftNorm;
		this.distanceLeftNormalized = helperUpNorm;
		
	}
}
