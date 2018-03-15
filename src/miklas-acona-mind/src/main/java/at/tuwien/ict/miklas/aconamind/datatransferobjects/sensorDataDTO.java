package at.tuwien.ict.miklas.aconamind.datatransferobjects;

public class sensorDataDTO extends chunkTimestampHeaderDTO {
	public int distanceUp = Integer.MAX_VALUE; 
	public String distanceUpObjectName = "";
	public int distanceDown = Integer.MAX_VALUE;
	public String distanceDownObjectName = "";
	public int distanceRight = Integer.MAX_VALUE;
	public String distanceRightObjectName = "";
	public int distanceLeft = Integer.MAX_VALUE;
	public String distanceLeftObjectName = "";
	
	public void init()
	{
		this.hasName = "";
		this.hasType = "";
		this.Timestamp = 0;
		distanceUp = Integer.MAX_VALUE;
		distanceDown = Integer.MAX_VALUE;
		distanceRight = Integer.MAX_VALUE;
		distanceLeft = Integer.MAX_VALUE;
		distanceUpObjectName = "";
		distanceDownObjectName = "";
		distanceLeftObjectName = "";
		distanceRightObjectName = "";
	}
	
	public void init(int timestamp) {
		init();
		this.Timestamp = timestamp;
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
}
