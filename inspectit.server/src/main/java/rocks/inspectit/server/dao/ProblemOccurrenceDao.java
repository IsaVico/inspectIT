package rocks.inspectit.server.dao;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import rocks.inspectit.shared.cs.communication.data.diagnosis.ProblemOccurrence;
import rocks.inspectit.shared.cs.communication.data.diagnosis.ProblemOccurrenceData;

/**
 * This DAO is used to handle all {@link ProblemOccurrenceData} objects.
 *
 * @author Isabel Vico Peinado
 *
 */
public interface ProblemOccurrenceDao {
	/**
	 * Load a specific {@link ProblemOccurrenceData} from the underlying storage by passing the id.
	 *
	 * @param id
	 *            The id of the object.
	 * @return The found {@link ProblemOccurrenceData} object.
	 */
	ProblemOccurrenceData load(Long id);

	/**
	 * Save or update the specific {@link ProblemOccurrenceData} to the database.
	 *
	 * @param problemOccurrence
	 *            {@link ProblemOccurrence} to save.
	 * @throws IOException
	 *             {@link IOException} that will be thrown if something was wrong when saving or
	 *             updating.
	 */
	void saveOrUpdate(ProblemOccurrence problemOccurrence) throws IOException;

	/**
	 * Save or update all the {@link ProblemOccurrence} to the database.
	 *
	 * @param problemOccurrences
	 *            {@link ProblemOccurrence} to save or update.
	 * @throws IOException
	 *             {@link IOException} that will be thrown if something was wrong when saving or
	 *             updating.
	 */
	void saveOrUpdateAll(Set<ProblemOccurrence> problemOccurrences) throws IOException;

	/**
	 * Deletes this specific {@link ProblemOccurrenceData} object.
	 *
	 * @param problemOccurrence
	 *            The {@link ProblemOccurrenceData} object to delete.
	 * @throws IOException
	 *             {@link IOException} that will be thrown if something was wrong when saving or
	 *             updating.
	 */
	void delete(ProblemOccurrence problemOccurrence) throws IOException;

	/**
	 * Deletes all {@link ProblemOccurrenceData} objects which are stored in the passed list.
	 *
	 * @param problemOccurrencesData
	 *            The list containing the {@link ProblemOccurrenceData} objects to delete.
	 */
	void deleteAll(List<ProblemOccurrenceData> problemOccurrencesData);

	/**
	 * Returns all {@link ProblemOccurrenceData} objects which are saved in the underlying storage.
	 *
	 * @return Returns all stored {@link ProblemOccurrenceData} objects.
	 */
	List<ProblemOccurrenceData> findAll();

	/**
	 * Returns all {@link ProblemOccurrenceData} objects which match with the agentId.
	 *
	 * @param agentId
	 *            The id of the agent
	 * @return Returns the retrieved stored {@link ProblemOccurrenceData} objects.
	 */
	List<ProblemOccurrenceData> findByAgentId(long agentId);

	/**
	 * Returns all {@link ProblemOccurrenceData} objects which match with the app name.
	 *
	 * @param appName
	 *            The name of the application
	 * @return Returns the retrieved stored {@link ProblemOccurrenceData} objects.
	 */

	List<ProblemOccurrenceData> findByAppName(String appName);

	/**
	 * Returns all {@link ProblemOccurrenceData} objects which match with the business transaction.
	 **
	 * @param businessTransaction
	 *            The name of the businessTransaction
	 * @return Returns the retrieved stored {@link ProblemOccurrenceData} objects.
	 */
	List<ProblemOccurrenceData> findByBusinessTransaction(String businessTransaction);

	/**
	 * Returns all {@link ProblemOccurrenceData} objects which match with the cause type.
	 *
	 * @param causeType
	 *            The cause type
	 * @return Returns the retrieved stored {@link ProblemOccurrenceData} objects.
	 */
	List<ProblemOccurrenceData> findByCauseType(String causeType);

	/**
	 * Returns all {@link ProblemOccurrenceData} objects which match with the global context.
	 **
	 * @param globalContext
	 *            The global context name
	 * @return Returns the retrieved stored {@link ProblemOccurrenceData} objects.
	 */
	List<ProblemOccurrenceData> findByGlobalContext(String globalContext);

	/**
	 * Returns all {@link ProblemOccurrenceData} objects which match with the problem context.
	 **
	 * @param problemContext
	 *            The problem context name
	 * @return Returns the retrieved stored {@link ProblemOccurrenceData} objects.
	 */
	List<ProblemOccurrenceData> findByProblemContext(String problemContext);

	/**
	 * Returns all {@link ProblemOccurrenceData} objects which match with the request root.
	 *
	 ** @param requestRoot
	 *            The request root name
	 * @return Returns the retrieved stored {@link ProblemOccurrenceData} objects.
	 */
	List<ProblemOccurrenceData> findByRequestRoot(String requestRoot);

}
