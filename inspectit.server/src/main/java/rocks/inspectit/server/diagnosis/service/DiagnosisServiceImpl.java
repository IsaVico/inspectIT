package rocks.inspectit.server.diagnosis.service;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.apache.commons.math3.util.Pair;
import org.slf4j.Logger;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import rocks.inspectit.server.diagnosis.engine.DiagnosisEngine;
import rocks.inspectit.server.diagnosis.engine.DiagnosisEngineConfiguration;
import rocks.inspectit.server.diagnosis.engine.IDiagnosisEngine;
import rocks.inspectit.server.diagnosis.engine.rule.annotation.Rule;
import rocks.inspectit.server.diagnosis.engine.session.ISessionCallback;
import rocks.inspectit.server.diagnosis.engine.session.SessionVariables;
import rocks.inspectit.server.diagnosis.service.results.ProblemOccurrence;
import rocks.inspectit.server.diagnosis.service.rules.RuleConstants;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;
import rocks.inspectit.shared.all.spring.logger.Log;

/**
 * @author Claudio Waldvogel
 *
 */
public class DiagnosisServiceImpl implements IDiagnosisService, Runnable {
	/** The logger of this class. */
	@Log
	Logger log;

	private static final long TIMEOUT = 50;

	// @Autowired
	// private IDiagnosisResultNotificationService diagnosisResultService;

	private IDiagnosisEngine<InvocationSequenceData> engine;

	private final int capacity = 100;

	private final BlockingQueue<DiagnosisInput> queue = new LinkedBlockingQueue<>(capacity);

	private final ExecutorService executor = Executors.newSingleThreadExecutor();

	private final List<String> rulesPackages;

	public DiagnosisServiceImpl(List<String> rulesPackages) {
		this.rulesPackages = rulesPackages;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean diagnose(InvocationSequenceData invocation, double baseline) {
		try {
			return queue.offer(new DiagnosisInput(invocation, baseline), TIMEOUT, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			return false;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int diagnose(Collection<Pair<InvocationSequenceData, Double>> invocationBaselinePairs) {
		int count = 0;
		for (Pair<InvocationSequenceData, Double> invocationBaselinePair : invocationBaselinePairs) {
			boolean successfullySubmitted = diagnose(invocationBaselinePair.getFirst(), invocationBaselinePair.getSecond());
			if (!successfullySubmitted) {
				break;
			}
			count++;
		}
		return count;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void run() {
		try {
			while (true) {
				DiagnosisInput diagnosisInput = queue.take();
				SessionVariables sessionVariables = new SessionVariables();
				sessionVariables.put(RuleConstants.VAR_BASELINE, diagnosisInput.getBaseline());
				engine.analyze(diagnosisInput.getInvocation(), sessionVariables);
			}
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	@PostConstruct
	public void init() throws ClassNotFoundException {

		ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);

		scanner.addIncludeFilter(new AnnotationTypeFilter(Rule.class));
		Set<Class<?>> ruleClasses = new HashSet<>();
		for (String packageName : rulesPackages) {
			for (BeanDefinition bd : scanner.findCandidateComponents(packageName)) {
				Class<?> clazz = Class.forName(bd.getBeanClassName());
				ruleClasses.add(clazz);
			}
		}

		DiagnosisEngineConfiguration<InvocationSequenceData, List<ProblemOccurrence>> configuration = new DiagnosisEngineConfiguration<InvocationSequenceData, List<ProblemOccurrence>>();

		configuration.setNumSessionWorkers(2);
		configuration.setRuleClasses(ruleClasses);
		configuration.setResultCollector(new ProblemInstanceResultCollector());
		configuration.setSessionCallback(new DelegatingResultHandler());

		engine = new DiagnosisEngine<>(configuration);
		executor.execute(this);
		if (log.isInfoEnabled()) {
			log.info("|-Diagnosis Service active...");
		}
	}

	private static class DiagnosisInput {
		private final InvocationSequenceData invocation;
		private final double baseline;

		/**
		 * @param invocation
		 * @param baseline
		 */
		public DiagnosisInput(InvocationSequenceData invocation, double baseline) {
			this.invocation = invocation;
			this.baseline = baseline;
		}

		/**
		 * Gets {@link #invocation}.
		 *
		 * @return {@link #invocation}
		 */
		public InvocationSequenceData getInvocation() {
			return invocation;
		}

		/**
		 * Gets {@link #baseline}.
		 *
		 * @return {@link #baseline}
		 */
		public double getBaseline() {
			return baseline;
		}
	}

	private class DelegatingResultHandler implements ISessionCallback<List<ProblemOccurrence>> {
		/**
		 * {@inheritDoc}
		 */
		@Override
		public void onSuccess(List<ProblemOccurrence> result) {
			// diagnosisResultService.onNewDiagnosisResult(result);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void onFailure(Throwable t) {
			log.warn("Failed conducting diagnosis!", t);
		}
	}

}
