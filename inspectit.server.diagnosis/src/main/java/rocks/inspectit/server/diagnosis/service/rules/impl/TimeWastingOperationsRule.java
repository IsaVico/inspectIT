package rocks.inspectit.server.diagnosis.service.rules.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import rocks.inspectit.server.diagnosis.engine.rule.annotation.Action;
import rocks.inspectit.server.diagnosis.engine.rule.annotation.Rule;
import rocks.inspectit.server.diagnosis.engine.rule.annotation.SessionVariable;
import rocks.inspectit.server.diagnosis.engine.rule.annotation.TagValue;
import rocks.inspectit.server.diagnosis.service.rules.RuleConstants;
import rocks.inspectit.shared.all.communication.data.AggregatedInvocationSequenceData;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceDataHelper;
import rocks.inspectit.shared.cs.indexing.aggregation.impl.AggregationPerformer;
import rocks.inspectit.shared.cs.indexing.aggregation.impl.InvocationSequenceDataAggregator;

/**
 * Rule for detecting <code>Time Wasting Operations</code> within an {@link InvocationSequenceData}.
 * The search starts from the <code>Global Context</code>. A <code>Time Wasting Operation</code> is
 * an {@link AggregatedInvocationSequenceData} that holds all methods with the same key which
 * together have a high exclusive time or simply put, are the biggest time waster. This rule is
 * triggered second in the rule pipeline.
 *
 * @author Alexander Wert, Alper Hidiroglu
 *
 */
@Rule(name = "TimeWastingOperationsRule")
public class TimeWastingOperationsRule {

	/**
	 * Defines the minimum number of calls to one method. If one method is called more often it is
	 * considered to be a <code>Time Wasting Operation</code>.
	 */
	private static final int MIN_NUMBER_OF_CALLS_TO_SAME_METHOD = 20;

	/**
	 * An {@link AggregatedInvocationSequenceData} is considered as a
	 * <code>Time Wasting Operation</code>, if the cumulative exclusive time of already found
	 * <code>Time Wasting Operations</code> is lower than 80 percent of the
	 * <code>Global Context's</code> duration.
	 */
	private static final Double PROPORTION = 0.8;

	/**
	 * An {@link AggregatedInvocationSequenceData} is considered as a
	 * <code>Time Wasting Operation</code>, if the cumulative exclusive time of already found
	 * <code>Time Wasting Operations</code> subtracted from the <code>Global Context's</code>
	 * duration is higher than the baseline (= 1000).
	 */
	@SessionVariable(name = RuleConstants.VAR_BASELINE, optional = false)
	private double baseline;

	/**
	 * The search for <code>Time Wasting Operations</code> starts from the
	 * <code>Global Context</code>.
	 */
	@TagValue(type = RuleConstants.TAG_GLOBAL_CONTEXT)
	private InvocationSequenceData globalContext;

	/**
	 * Rule execution.
	 *
	 * @return TAG_TIME_WASTING_OPERATIONS
	 */
	@Action(resultTag = RuleConstants.TAG_TIME_WASTING_OPERATIONS, resultQuantity = Action.Quantity.MULTIPLE)
	public List<AggregatedInvocationSequenceData> action() {

		List<InvocationSequenceData> invocationSequenceDataList = asInvocationSequenceDataList(Collections.singletonList(globalContext),
				new ArrayList<InvocationSequenceData>(globalContext.getNestedSequences().size()));
		AggregationPerformer<InvocationSequenceData> aggregationPerformer = new AggregationPerformer<InvocationSequenceData>(new InvocationSequenceDataAggregator());

		// Aggregates all methods with the same key to an object of type
		// AggregatedInvocationSequenceData. Exclusive times are summed up.
		aggregationPerformer.processCollection(invocationSequenceDataList);
		invocationSequenceDataList = aggregationPerformer.getResultList();

		Collections.sort(invocationSequenceDataList, new Comparator<InvocationSequenceData>() {
			/**
			 * Sorts list with aggregated {@link InvocationSequenceData} with the help of the summed
			 * up exclusive times.
			 */
			@Override
			public int compare(InvocationSequenceData o1, InvocationSequenceData o2) {
				return Double.compare(InvocationSequenceDataHelper.calculateExclusiveTime(o2), InvocationSequenceDataHelper.calculateExclusiveTime(o1));
			}
		});

		// Only AggregatedInvocationSequenceData with highest exclusive
		// times are Time Wasting Operations or when the AggregatedInvocationSequenceData consists
		// of more than 20 methods.
		List<AggregatedInvocationSequenceData> timeWastingOperations = new ArrayList<>();
		double sumExecTime = 0;
		for (InvocationSequenceData invocSeqData : invocationSequenceDataList) {
			AggregatedInvocationSequenceData aggInvocSeqData = (AggregatedInvocationSequenceData) invocSeqData;
			if (((globalContext.getDuration() - sumExecTime) > baseline) || (sumExecTime < (PROPORTION * globalContext.getDuration())) || (aggInvocSeqData.size() > MIN_NUMBER_OF_CALLS_TO_SAME_METHOD)) {
				// increase sumExclusiveTime by duration of Time Wasting Operation.
				sumExecTime += InvocationSequenceDataHelper.calculateExclusiveTime(invocSeqData);
				timeWastingOperations.add(aggInvocSeqData);
			} else {
				break;
			}
		}
		return timeWastingOperations;
	}

	/**
	 * Saves beside the <code>Global Context</code> all reachable {@link InvocationSequenceData}
	 * from the <code>Global Context</code> in {@link resultList}.
	 *
	 * @param invocationSequences
	 *            List that only holds the <code>Global Context</code>.
	 * @param resultList
	 *            List with <code>Global Context</code> and invocation sequences reachable from the
	 *            <code>Global Context</code>.
	 * @return List with {@link InvocationSequenceData}.
	 */
	private List<InvocationSequenceData> asInvocationSequenceDataList(List<InvocationSequenceData> invocationSequences, final List<InvocationSequenceData> resultList) {
		for (InvocationSequenceData invocationSequence : invocationSequences) {
			// Either timer data has to be available
			if ((null != invocationSequence.getTimerData()) && invocationSequence.getTimerData().isExclusiveTimeDataAvailable()) {
				resultList.add(invocationSequence);
				// Or SQL statement data
			} else if ((null != invocationSequence.getSqlStatementData()) && invocationSequence.getSqlStatementData().isExclusiveTimeDataAvailable()) {
				invocationSequence.setTimerData(invocationSequence.getSqlStatementData());
				resultList.add(invocationSequence);
			}
			asInvocationSequenceDataList(invocationSequence.getNestedSequences(), resultList);
		}
		return resultList;
	}
}