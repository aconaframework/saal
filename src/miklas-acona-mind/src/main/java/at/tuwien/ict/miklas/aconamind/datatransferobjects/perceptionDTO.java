package at.tuwien.ict.miklas.aconamind.datatransferobjects;

import java.util.ArrayList;

public class perceptionDTO extends chunkTimestampHeaderDTO { 
	public int Healthchange;
	public int Health;
	public ArrayList<hasPerceptDTO> hasPercept;
	
        
	public class hasPerceptDTO extends chunkHeaderDTO {
		public String BodyType;
		public String Name;
		public String Id;
		public int x;
		public int y;
    }
}
