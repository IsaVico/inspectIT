/**
 *
 */
package rocks.inspectit.server.diagnosis.service.rules.impl;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import rocks.inspectit.server.diagnosis.engine.rule.annotation.Action;
import rocks.inspectit.server.diagnosis.engine.rule.annotation.Rule;
import rocks.inspectit.server.diagnosis.engine.rule.annotation.TagValue;
import rocks.inspectit.server.diagnosis.service.rules.InvocationSequenceDataIterator;
import rocks.inspectit.server.diagnosis.service.rules.RuleConstants;
import rocks.inspectit.shared.all.communication.data.AggregatedInvocationSequenceData;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;

/**
 * @author Alexander Wert
 *
 */
@Rule(name = "ProblemContextRule")
public class ProblemContextRule {

	@TagValue(type = RuleConstants.TAG_GLOBAL_CONTEXT)
	private InvocationSequenceData globalContext;

	@TagValue(type = RuleConstants.TAG_TIME_WASTING_OPERATIONS)
	private AggregatedInvocationSequenceData timeWastingOperation;

	@Action(resultTag = RuleConstants.TAG_PROBLEM_CONTEXT)
	public InvocationSequenceData action() {
		List<InvocationSequenceData> causeInvocations = timeWastingOperation.getRawInvocationsSequenceElements();

		if (causeInvocations.size() > 1) {
			double overallExclusiveDuration = 0.0;

			List<CauseCluster> causeClusters = new LinkedList<>();
			for (InvocationSequenceData invocation : causeInvocations) {
				causeClusters.add(new CauseCluster(invocation));
				overallExclusiveDuration += invocation.getTimerData().isExclusiveTimeDataAvailable() ? invocation.getTimerData().getExclusiveDuration() : 0.0;
			}
			CauseCluster significantCluster = getSignificantCluster(causeClusters, overallExclusiveDuration);
			while (null == significantCluster) {
				// distance
				calculateDistancesToNextCluster(causeClusters);

				// merge
				causeClusters = mergeClusters(causeClusters);

				// significant cluster
				significantCluster = getSignificantCluster(causeClusters, overallExclusiveDuration);
			}
			return significantCluster.getCommonContext();
		} else {
			InvocationSequenceData parent = causeInvocations.get(0).getParentSequence();
			return parent != null ? parent : causeInvocations.get(0);
		}
	}

	/**
	 * @param causeClusters
	 * @return
	 */
	private List<CauseCluster> mergeClusters(List<CauseCluster> causeClusters) {
		boolean merged = false;
		int distance = 0;
		List<CauseCluster> newClusters = new LinkedList<>();
		List<CauseCluster> clustersToMerge = new LinkedList<>();
		while (!merged) {
			clustersToMerge.clear();
			newClusters.clear();
			for (CauseCluster cluster : causeClusters) {
				clustersToMerge.add(cluster);
				if (cluster.getDistanceToNextCluster() > distance) {
					if (clustersToMerge.size() > 1) {
						newClusters.add(new CauseCluster(clustersToMerge));
						merged = true;
					} else {
						newClusters.add(cluster);
					}
					clustersToMerge.clear();
				}
			}
			distance++;
		}
		return newClusters;
	}

	private CauseCluster getSignificantCluster(List<CauseCluster> causeClusters, double overallExclusiveDuration) {
		for (CauseCluster cluster : causeClusters) {
			double exclusiveDurationSum = 0.0;
			for (InvocationSequenceData invocation : cluster.getCauseInvocations()) {
				exclusiveDurationSum += invocation.getTimerData().isExclusiveTimeDataAvailable() ? invocation.getTimerData().getExclusiveDuration() : 0.0;
			}
			if (exclusiveDurationSum > (0.8 * overallExclusiveDuration)) {
				return cluster;
			}
		}
		return null;
	}

	/**
	 * @param causeInvocations
	 * @param iterator
	 */
	private void calculateDistancesToNextCluster(List<CauseCluster> causeClusters) {
		int nextClusterIndex = 0;
		CauseCluster nextCluster = causeClusters.get(nextClusterIndex);
		CauseCluster currentCluster = null;
		InvocationSequenceDataIterator iterator = new InvocationSequenceDataIterator(globalContext);
		int currentCauseDepth = -1;
		int minDepth = Integer.MAX_VALUE;

		InvocationSequenceData invocation;
		while (iterator.hasNext() && (nextClusterIndex < causeClusters.size())) {
			invocation = iterator.next();
			if (iterator.currentDepth() < minDepth) {
				minDepth = iterator.currentDepth();
			}
			if (nextCluster.getCommonContext() == invocation) {
				if (null != currentCluster) {
					int depthDistance = Math.max((currentCauseDepth - minDepth) + 1, 0);
					currentCluster.setDistanceToNextCluster(depthDistance);
				}

				// reset
				currentCluster = nextCluster;
				nextClusterIndex++;
				if (nextClusterIndex < causeClusters.size()) {
					nextCluster = causeClusters.get(nextClusterIndex);
				}
				currentCauseDepth = iterator.currentDepth();
				minDepth = Integer.MAX_VALUE;
			}
		}
		currentCluster.setDistanceToNextCluster(Integer.MAX_VALUE);
	}

	private static class CauseCluster {

		private final List<InvocationSequenceData> causeInvocations = new ArrayList<>();

		private int distanceToNextCluster = Integer.MAX_VALUE;

		private final InvocationSequenceData commonContext;

		/**
		 *
		 */
		public CauseCluster(InvocationSequenceData causeInvocation) {
			causeInvocations.add(causeInvocation);
			commonContext = causeInvocation;
		}

		public CauseCluster(List<CauseCluster> clustersToMerge) {
			int distanceToParent = clustersToMerge.get(0).getDistanceToNextCluster();
			InvocationSequenceData parent = clustersToMerge.get(0).getCommonContext();
			for (int i = 0; i < distanceToParent; i++) {
				parent = parent.getParentSequence();
			}
			commonContext = parent;

			for (CauseCluster cluster : clustersToMerge) {
				causeInvocations.addAll(cluster.getCauseInvocations());
			}
		}

		/**
		 * Gets {@link #causeInvocations}.
		 *
		 * @return {@link #causeInvocations}
		 */
		public List<InvocationSequenceData> getCauseInvocations() {
			return causeInvocations;
		}

		/**
		 * Gets {@link #distanceToNextCluster}.
		 *
		 * @return {@link #distanceToNextCluster}
		 */
		public int getDistanceToNextCluster() {
			return distanceToNextCluster;
		}

		/**
		 * Sets {@link #distanceToNextCluster}.
		 *
		 * @param distanceToNextCluster
		 *            New value for {@link #distanceToNextCluster}
		 */
		public void setDistanceToNextCluster(int distanceToNextCluster) {
			this.distanceToNextCluster = distanceToNextCluster;
		}

		/**
		 * Gets {@link #commonContext}.
		 *
		 * @return {@link #commonContext}
		 */
		public InvocationSequenceData getCommonContext() {
			return commonContext;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String toString() {
			return rocks.inspectit.server.diagnosis.service.rules.UTIL.instance.invocToStr(commonContext);
		}
	}
}
