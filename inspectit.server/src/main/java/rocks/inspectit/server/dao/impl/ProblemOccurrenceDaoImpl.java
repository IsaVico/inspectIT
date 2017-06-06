package rocks.inspectit.server.dao.impl;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;

import rocks.inspectit.server.dao.ProblemOccurrenceDao;
import rocks.inspectit.shared.cs.communication.data.diagnosis.InvocationIdentifier;
import rocks.inspectit.shared.cs.communication.data.diagnosis.ProblemOccurrence;
import rocks.inspectit.shared.cs.communication.data.diagnosis.ProblemOccurrenceData;

/**
 * @author Isabel Vico Peinado
 *
 */
@Repository
public class ProblemOccurrenceDaoImpl extends AbstractJpaDao<ProblemOccurrenceData> implements ProblemOccurrenceDao {

	// /**
	// * Cached data service to calculated the id of the problem occurrence.
	// */
	// @Autowired
	// ICachedDataService cachedDataService;

	/**
	 * Default constructor.
	 */
	public ProblemOccurrenceDaoImpl() {
		super(ProblemOccurrenceData.class);
	}

	/**
	 *
	 * {@inheritDoc}
	 *
	 * @throws IOException
	 */
	@Override
	public void saveOrUpdate(ProblemOccurrence problemOccurrence) throws IOException {
		ProblemOccurrenceData problemOccurrenceData = new ProblemOccurrenceData(problemOccurrence);
		// we save if the id is not set, otherwise, we update the current problemOccurrence that
		// corresponds.
		if (problemOccurrenceData.getId() == calculateId(problemOccurrence)) {
			super.create(problemOccurrenceData);
		} else {
			super.update(problemOccurrenceData);
		}
	}

	/**
	 * Calculate the id for the {@link ProblemOccurrenceData} based in the current
	 * {@link ProblemOccurrence}.
	 *
	 * @param problemOccurrence
	 *            The instance of the {@link ProblemOccurrence} to get the id.
	 *
	 * @return An integer containing the unique id for the problem occurrence data.
	 */
	public int calculateId(ProblemOccurrence problemOccurrence) {
		final int prime = 31;
		int result = super.hashCode();
		result = (prime * result) + getHashCode(problemOccurrence.getRequestRoot());
		result = (prime * result) + getHashCode(problemOccurrence.getGlobalContext());
		result = (prime * result) + getHashCode(problemOccurrence.getProblemContext());
		result = (prime * result) + getHashCode(problemOccurrence.getRequestRoot());
		result = (prime * result) + ((getMethodIdent(problemOccurrence.getApplicationNameIdent()) == null) ? 0 : getMethodIdent(problemOccurrence.getApplicationNameIdent()).hashCode());
		result = (prime * result)
				+ ((getMethodIdent(problemOccurrence.getBusinessTransactionNameIdent()) == null) ? 0 : getMethodIdent(problemOccurrence.getBusinessTransactionNameIdent()).hashCode());
		return result;
	}

	/**
	 * Gets the hash code for the invocation.
	 *
	 * @param invocation
	 *            Invocation to have the hash code
	 * @return Returns an integer with the hash code of the invocation.
	 */
	private int getHashCode(InvocationIdentifier invocation) {
		return ((getMethodIdent(invocation.getMethodIdent()) == null) ? 0 : getMethodIdent(invocation.getMethodIdent()).hashCode());
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
		return "" + methodIdent;
	}

	/**
	 *
	 * {@inheritDoc}
	 */
	@Override
	public List<ProblemOccurrenceData> findAll() {
		return getEntityManager().createNamedQuery(ProblemOccurrenceData.FIND_ALL, ProblemOccurrenceData.class).getResultList();

	}

	/**
	 *
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	public List<ProblemOccurrenceData> findByAgentId(long agentId) {
		Query query = entityManager.createNamedQuery(ProblemOccurrenceData.FIND_BY_AGENT_ID);
		query.setParameter("agentId", agentId);
		return query.getResultList();
	}

	/**
	 *
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	public List<ProblemOccurrenceData> findByAppName(String appName) {
		Query query = getEntityManager().createNamedQuery(ProblemOccurrenceData.FIND_BY_APP_NAME);
		query.setParameter("appName", appName);
		return query.getResultList();
	}

	/**
	 *
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	public List<ProblemOccurrenceData> findByBusinessTransaction(String businessTransaction) {
		Query query = getEntityManager().createNamedQuery(ProblemOccurrenceData.FIND_BY_BUSINESS_TRANSACTION);
		query.setParameter("businessTransaction", businessTransaction);
		return query.getResultList();
	}

	/**
	 *
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	public List<ProblemOccurrenceData> findByCauseType(String causeType) {
		Query query = getEntityManager().createNamedQuery(ProblemOccurrenceData.FIND_BY_CAUSE_TYPE);
		query.setParameter("causeType", causeType);
		return query.getResultList();
	}

	/**
	 *
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	public List<ProblemOccurrenceData> findByGlobalContext(String globalContext) {
		Query query = getEntityManager().createNamedQuery(ProblemOccurrenceData.FIND_BY_GLOBAL_CONTEXT);
		query.setParameter("globalContextMethod", globalContext);
		return query.getResultList();
	}

	/**
	 *
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	public List<ProblemOccurrenceData> findByProblemContext(String problemContext) {
		Query query = getEntityManager().createNamedQuery(ProblemOccurrenceData.FIND_BY_PROBLEM_CONTEXT);
		query.setParameter("problemContextMethod", problemContext);
		return query.getResultList();
	}

	/**
	 *
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	public List<ProblemOccurrenceData> findByRequestRoot(String requestRoot) {
		Query query = getEntityManager().createNamedQuery(ProblemOccurrenceData.FIND_BY_REQUEST_ROOT);
		query.setParameter("requestRootMethod", requestRoot);
		return query.getResultList();
	}

	/**
	 *
	 * {@inheritDoc}
	 */
	@Override
	public void delete(ProblemOccurrence problemOccurrence) throws IOException {
		ProblemOccurrenceData problemOccurrenceData = new ProblemOccurrenceData(problemOccurrence);
		super.delete(problemOccurrenceData);
	}

	/**
	 * {@inheritDoc}
	 *
	 * @throws IOException
	 */
	@Override
	public void saveOrUpdateAll(Set<ProblemOccurrence> problemOccurrences) throws IOException {
		for (ProblemOccurrence problemOccurrence : problemOccurrences) {
			saveOrUpdate(problemOccurrence);
		}
	}
}
