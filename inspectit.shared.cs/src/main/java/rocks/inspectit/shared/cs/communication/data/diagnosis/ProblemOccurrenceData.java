package rocks.inspectit.shared.cs.communication.data.diagnosis;

import java.io.IOException;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

import org.codehaus.jackson.map.ObjectMapper;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Isabel Vico Peinado
 *
 */
@Entity
@NamedQueries({ @NamedQuery(name = ProblemOccurrenceData.FIND_ALL, query = "SELECT p FROM ProblemOccurrenceData p"),
	@NamedQuery(name = ProblemOccurrenceData.FIND_BY_AGENT_ID, query = "SELECT p FROM ProblemOccurrenceData p WHERE p.agentId = :agentId"),
	@NamedQuery(name = ProblemOccurrenceData.FIND_BY_APP_NAME, query = "SELECT p FROM ProblemOccurrenceData p WHERE p.appName = :appName"),
	@NamedQuery(name = ProblemOccurrenceData.FIND_BY_BUSINESS_TRANSACTION, query = "SELECT p FROM ProblemOccurrenceData p WHERE p.businessTransaction = :businessTransaction"),
	@NamedQuery(name = ProblemOccurrenceData.FIND_BY_CAUSE_TYPE, query = "SELECT p FROM ProblemOccurrenceData p WHERE p.causeType = :causeType"),
	@NamedQuery(name = ProblemOccurrenceData.FIND_BY_GLOBAL_CONTEXT, query = "SELECT p FROM ProblemOccurrenceData p WHERE p.globalContextMethod = :globalContextMethod"),
	@NamedQuery(name = ProblemOccurrenceData.FIND_BY_PROBLEM_CONTEXT, query = "SELECT p FROM ProblemOccurrenceData p WHERE p.problemContextMethod = :problemContextMethod"),
	@NamedQuery(name = ProblemOccurrenceData.FIND_BY_REQUEST_ROOT, query = "SELECT p FROM ProblemOccurrenceData p WHERE p.requestRootMethod = :requestRootMethod") })
public class ProblemOccurrenceData {

	// /**
	// * Cached data service to calculated the id of the problem occurrence.
	// */
	// @Autowired
	// @Transient
	// ICachedDataService cachedDataService;

	/**
	 * The id of this instance (if persisted, otherwise <code>null</code>).
	 */
	@Id
	int id;

	/**
	 * Name of the request root.
	 */
	@JsonProperty(value = "requestRootMethod")
	String requestRootMethod;

	/**
	 * Name of the global context.
	 */
	@JsonProperty(value = "globalContextMethod")
	String globalContextMethod;

	/**
	 * Name of the problem context.
	 */
	@JsonProperty(value = "problemContextMethod")
	String problemContextMethod;

	/**
	 * Name of the application.
	 */
	@JsonProperty(value = "appName")
	String appName;

	/**
	 * Name of the businessTransaction.
	 */
	@JsonProperty(value = "businessTransaction")
	String businessTransaction;

	/**
	 * Name of the application.
	 */
	@JsonProperty(value = "causeType")
	String causeType;

	/**
	 * Id of the agent.
	 */
	@JsonProperty(value = "agentId")
	long agentId;

	/**
	 * All the information of the problem occurrence stored in JSON format.
	 */
	@JsonProperty(value = "problemOccurrenceJson")
	@Column(length = 10000)
	String problemOccurrenceJson;

	/**
	 * Constant for findAll query.
	 */
	public static final String FIND_ALL = "ProblemOccurenceData.findAll";

	/**
	 * Constant for findByProblemContext query.
	 */
	public static final String FIND_BY_PROBLEM_CONTEXT = "ProblemOccurenceData.findByProblemContext";

	/**
	 * Constant for findByGlobalContext query.
	 */
	public static final String FIND_BY_GLOBAL_CONTEXT = "ProblemOccurenceData.findByGlobalContext";

	/**
	 * Constant for findByRequestRoot query.
	 */
	public static final String FIND_BY_REQUEST_ROOT = "ProblemOccurenceData.findByRequestRoot";

	/**
	 * Constant for findByCauseType query.
	 */
	public static final String FIND_BY_CAUSE_TYPE = "ProblemOccurenceData.findByCauseType";

	/**
	 * Constant for findByAppName query.
	 */
	public static final String FIND_BY_APP_NAME = "ProblemOccurenceData.findByAppName";

	/**
	 * Constant for findByBusinessTransaction query.
	 */
	public static final String FIND_BY_BUSINESS_TRANSACTION = "ProblemOccurenceData.findByBusinessTransaction";

	/**
	 * Constant for findByAgentId query.
	 */
	public static final String FIND_BY_AGENT_ID = "ProblemOccurenceData.findByAgentId";

	/**
	 * Constructor of the class by default.
	 */
	public ProblemOccurrenceData() {
	}

	/**
	 * Constructor of the class.
	 *
	 * @param problemOccurrence
	 *            The {@link ProblemOccurrence} which will be stored later in the database
	 * @throws IOException
	 *             An {@link IOException} will be thrown if something was wrong when serializing or
	 *             deserializing the data
	 */
	public ProblemOccurrenceData(ProblemOccurrence problemOccurrence) throws IOException {
		problemOccurrenceJson = fromProblemOccurrenceToJson(problemOccurrence);
		problemContextMethod = getMethodIdent(problemOccurrence.getProblemContext().getMethodIdent());
		globalContextMethod = getMethodIdent(problemOccurrence.getGlobalContext().getMethodIdent());
		requestRootMethod = getMethodIdent(problemOccurrence.getRequestRoot().getMethodIdent());
		causeType = problemOccurrence.getCauseStructure().getCauseType().toString();
		appName = getMethodIdent(problemOccurrence.getApplicationNameIdent());
		businessTransaction = getMethodIdent(problemOccurrence.getBusinessTransactionNameIdent());
		id = calculateId();
	}

	/**
	 * Calculate the id for the {@link ProblemOccurrenceData} based in the current
	 * {@link ProblemOccurrence}.
	 *
	 * @return A int containing the unique id for the problem occurrence data.
	 */
	private int calculateId() {
		final int prime = 31;
		int result = super.hashCode();
		result = (prime * result) + ((this.requestRootMethod == null) ? 0 : this.requestRootMethod.hashCode());
		result = (prime * result) + ((this.globalContextMethod == null) ? 0 : this.globalContextMethod.hashCode());
		result = (prime * result) + ((this.problemContextMethod == null) ? 0 : this.problemContextMethod.hashCode());
		result = (prime * result) + ((this.requestRootMethod == null) ? 0 : this.requestRootMethod.hashCode());
		result = (prime * result) + ((this.appName == null) ? 0 : this.appName.hashCode());
		result = (prime * result) + ((this.businessTransaction == null) ? 0 : this.businessTransaction.hashCode());
		return result;
	}

	/**
	 * Gets the method identifier (String) for the long identifier.
	 *
	 * @param methodIdent
	 *            Long identifier of the method.
	 *
	 * @return Returns the String identifier of the method.
	 */
	private String getMethodIdent(long methodIdent) {
		// return cachedDataService.getMethodIdentForId(methodIdent).toString();
		return methodIdent + "";
	}

	/**
	 * Getter for the id.
	 *
	 * @return The id of the entity.
	 */
	public int getId() {
		return id;
	}

	/**
	 * Getter for the problemOccurrenceJson.
	 *
	 * @return Return the {@link String} that contains all the {@link ProblemOccurrence}
	 */
	public String getProblemOccurrenceJson() {
		return problemOccurrenceJson;
	}

	/**
	 * Getter for the {@link ProblemOccurrence}.
	 *
	 * @return The {@link ProblemOccurrence} from the JSON.
	 * @throws IOException
	 *             An {@link IOException} will be thrown if something was wrong when serializing or
	 *             deserializing the data
	 */
	public ProblemOccurrence getProblemOccurrence() throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		ProblemOccurrence problemOccurrence = null;
		try {
			problemOccurrence = mapper.readValue(problemOccurrenceJson, ProblemOccurrence.class);
		} catch (IOException e) {
			throw new IOException("Something was wrong when deserializing ProblemOccurrence", e);
		}
		return problemOccurrence;
	}

	/**
	 * Serialize a {@link ProblemOccurrence} object into a JSON string.
	 *
	 * @param problemOccurrence
	 *            {@link ProblemOccurrence} object to serialize to JSON.
	 * @return The {@link String} that contains the information of the {@link ProblemOccurrence}
	 * @throws IOException
	 *             An {@link IOException} will be thrown if something was wrong when serializing or
	 *             deserializing the data
	 */
	public String fromProblemOccurrenceToJson(ProblemOccurrence problemOccurrence) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		String jsonProblemOccurrence = null;
		try {
			jsonProblemOccurrence = mapper.writeValueAsString(problemOccurrence);
		} catch (IOException e) {
			throw new IOException("Something was wrong when serializing ProblemOccurrence", e);
		}
		return jsonProblemOccurrence;
	}
}
