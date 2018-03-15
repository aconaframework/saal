package at.tuwien.ict.miklas.mind.aconamind.restserver;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import at.tuwien.ict.acona.cell.cellfunction.specialfunctions.CFStateGenerator;

@Path("miklasservice")
public class RestService {

	//public static final String PARAMREQUESTPREFIXADDRESS = "requestprefixaddress";
	//public static final String PARAMUSERRESULTADDRESS = "userresultaddress";
	/**
	 * Name of the function that collects the state of the system. It is used to respond to a request to read the working memory state
	 */
	//public static final String PARAMSTATESERVICENAME = "stateservicename";
	//public final static String PARAMCOGSYSTRIGGER = "cogsystrigger";
	public static final String PARAMAGENTSYSTEMSTATEADDRESS = "systemstateaddress";
	public static final String PARAMINPUTADDRESS = "inputaddress";

	private String systemstateAddress = "";
	private String inputAddress = "";

	// private String optimizationRequestAddress = "";

	private final JerseyRestServer function;

	private final static Logger log = LoggerFactory.getLogger(RestService.class);

	public RestService() throws Exception {
		this.function = FuckingSingletonHack.getFunction();

		this.inputAddress = this.function.getFunctionConfig().getProperty(PARAMINPUTADDRESS, "inputs.miklas");
		this.systemstateAddress = this.function.getFunctionConfig().getProperty(PARAMAGENTSYSTEMSTATEADDRESS, CFStateGenerator.SYSTEMSTATEADDRESS);

		log.info("Initialized Jersey service with inputaddress={}, state collector function name={}", this.inputAddress, this.systemstateAddress);
	}

	/**
	 * Read the current system state, what is currently running
	 * 
	 * @return
	 * @throws Exception
	 */
	@GET
	@Path("readstate")
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.APPLICATION_JSON)
	public Response readSystemState(@DefaultValue("") @QueryParam("agent") String agentName) throws Exception {
		Response result = null;

		try {
			// System state
			String checkAddress = this.systemstateAddress;
			if (agentName.isEmpty() == false) {
				checkAddress = agentName + ":" + CFStateGenerator.SYSTEMSTATEADDRESS;
			}

			log.debug("Read address={}", checkAddress);
			
			JsonObject systemState = this.function.getCommunicatorFromFunction().read(checkAddress).getValue().getAsJsonObject();
			result = Response.ok().entity(systemState.toString()).build();
		} catch (Exception e) {
			log.error("Cannot read address: " + this.systemstateAddress, e);
			result = Response.status(Response.Status.BAD_REQUEST).build();
			throw new Exception(e.getMessage());
		}

		return result;
	}

	@GET
	@Path("readworkingmemory")
	@Produces(MediaType.APPLICATION_JSON)
	public Response readMemory() throws Exception {
		Response result = null;

		try {
			//JsonRpcRequest req = new JsonRpcRequest("readall", 0);
			// Execute RPC request to read the state of the cogsys memories
			//JsonRpcResponse memory = this.function.getCommunicatorFromFunction().execute(this.stateCollectorName, req);
			//JsonElement element = memory.getResult();
			JsonElement element = this.function.getCommunicatorFromFunction().read(this.inputAddress).getValue();

			result = Response.ok().entity(element.toString()).build();
		} catch (Exception e) {
			log.error("Cannot read address: " + this.inputAddress, e);
			result = Response.status(Response.Status.BAD_REQUEST).build();
			throw new Exception(e.getMessage());
		}

		return result;
	}
}
