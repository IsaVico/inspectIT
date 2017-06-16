/**
 *
 */
package rocks.inspectit.server.processor.impl;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;

import org.springframework.beans.factory.annotation.Autowired;

import rocks.inspectit.server.dao.ProblemOccurrenceDao;
import rocks.inspectit.server.diagnosis.results.DiagnosisResults;
import rocks.inspectit.server.diagnosis.service.IDiagnosisResultNotificationService;
import rocks.inspectit.server.diagnosis.service.IDiagnosisService;
import rocks.inspectit.server.diagnosis.service.rules.CachedDataMapper;
import rocks.inspectit.server.processor.AbstractCmrDataProcessor;
import rocks.inspectit.shared.all.communication.DefaultData;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;
import rocks.inspectit.shared.cs.communication.data.diagnosis.ProblemOccurrence;
import rocks.inspectit.shared.cs.communication.data.diagnosis.TimerDataProblemOccurence;

/**
 * This processor starts the {@link #diagnosisService} and stores the results in
 * {@link #diagnosisResults}.
 *
 * @author Claudio Waldvogel, Christian Voegele
 *
 */
public class DiagnosisCmrProcessor extends AbstractCmrDataProcessor implements IDiagnosisResultNotificationService {

	private int countProblems = 0;

	private int countToDiagnose = 0;

	/**
	 * Diagnosis service interface.
	 */
	@Autowired(required = false)
	private IDiagnosisService diagnosisService;

	/**
	 * Diagnosis service interface.
	 */
	@Autowired(required = false)
	private DiagnosisResults diagnosisResults;

	/**
	 * Baseline value.
	 */
	private final double baseline;

	/**
	 * {@link ProblemOccurrenceDao} to store the diagnosis results in the database.
	 */
	@Autowired
	ProblemOccurrenceDao problemOccurrenceDao;

	/**
	 * Basic constructor.
	 *
	 * @param baseline
	 *            The default baseline value defined in the configuration.
	 */
	public DiagnosisCmrProcessor(final double baseline) {
		this.baseline = baseline;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void processData(DefaultData defaultData, EntityManager entityManager) {
		countToDiagnose++;
		diagnosisService.diagnose((InvocationSequenceData) defaultData, baseline);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean canBeProcessed(DefaultData defaultData) {
		return (defaultData instanceof InvocationSequenceData) && (((InvocationSequenceData) defaultData).getDuration() > baseline);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onNewDiagnosisResult(ProblemOccurrence problemOccurrence) {
		diagnosisResults.getDiagnosisResults().add(problemOccurrence);
		countProblems++;
		printProblemOccurence(problemOccurrence);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onNewDiagnosisResult(Collection<ProblemOccurrence> problemOccurrences) {
		diagnosisResults.getDiagnosisResults().addAll(problemOccurrences);
		countProblems = countProblems + problemOccurrences.size();
		for (ProblemOccurrence problemOccurrence : problemOccurrences) {
			printProblemOccurence(problemOccurrence);
		}
	}

	/* TODO: Remove */
	private void printProblemOccurence(ProblemOccurrence problemOccurrence) {
		System.out.println(" ------------------");
		System.out.println("Anzahl Probleme " + countProblems + " ToDiagnose " + countToDiagnose);
		System.out.println("+ " + CachedDataMapper.getInstance().getBusinessTransactionName(problemOccurrence.getBusinessTransactionNameIdent(), problemOccurrence.getApplicationNameIdent()));
		System.out.println("+ " + CachedDataMapper.getInstance().getApplicationName(problemOccurrence.getApplicationNameIdent()));
		System.out.println("+ " + problemOccurrence.getRequestRoot().getInvocationId());
		System.out.println("+ " + CachedDataMapper.getInstance().getFQMethodeName(problemOccurrence.getGlobalContext().getMethodIdent()));
		System.out.println("+ " + problemOccurrence.getGlobalContext().getTimerDataProblemOccurence().getExclusiveTime());
		System.out.println("+ " + CachedDataMapper.getInstance().getFQMethodeName(problemOccurrence.getProblemContext().getMethodIdent()));
		System.out.println("+ " + problemOccurrence.getProblemContext().getTimerDataProblemOccurence().getExclusiveTime());
		System.out.println("+ " + CachedDataMapper.getInstance().getFQMethodeName(problemOccurrence.getRootCause().getMethodIdent()));
		System.out.println("+ " + problemOccurrence.getRootCause().getTimerDataProblemOccurence().getExclusiveTime());

		List<TimerDataProblemOccurence> rootCauseList = problemOccurrence.getRootCause().getTimerDataOfMethodInvocations();
		double sum = 0;
		for (int i = 0; i < rootCauseList.size(); i++) {
			sum += rootCauseList.get(i).getExclusiveTime();
		}

		System.out.println(" Average " + ((sum / rootCauseList.size()) + " Size " + rootCauseList.size()) + " Sum " + sum);
		System.out.println("+ " + problemOccurrence.getCauseStructure().getCauseType());
	}

	/**
	 * Store the diagnosis results in the database.
	 *
	 * @throws IOException
	 *             Throws an IOException in case the data cannot be stored.
	 */
	private void storeDiagnosisResults() throws IOException {
		Set<ProblemOccurrence> problemOccurrences = diagnosisResults.getDiagnosisResults();
		if (!problemOccurrences.isEmpty()) {
			problemOccurrenceDao.saveOrUpdateAll(problemOccurrences);
		}
	}
}