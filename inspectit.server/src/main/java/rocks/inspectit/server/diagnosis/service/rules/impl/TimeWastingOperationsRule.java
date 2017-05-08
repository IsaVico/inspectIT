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
 * @author Alexander Wert
 *
 */
@Rule(name = "TimeWastingOperationsRule")
public class TimeWastingOperationsRule {
	private static final Double RT_RALATION_THRESHOLD = 0.8;

	@SessionVariable(name = RuleConstants.VAR_BASELINE, optional = false)
	private double baseline;

	@TagValue(type = RuleConstants.TAG_GLOBAL_CONTEXT)
	private InvocationSequenceData globalContext;

	@Action(resultTag = RuleConstants.TAG_TIME_WASTING_OPERATIONS, resultQuantity = Action.Quantity.MULTIPLE)
	public List<AggregatedInvocationSequenceData> action() {
		List<InvocationSequenceData> invocationSequenceDataList = asInvocationSequenceDataList(Collections.singletonList(globalContext),
				new ArrayList<InvocationSequenceData>(globalContext.getNestedSequences().size()));

		AggregationPerformer<InvocationSequenceData> aggregationPerformer = new AggregationPerformer<InvocationSequenceData>(new InvocationSequenceDataAggregator());
		aggregationPerformer.processCollection(invocationSequenceDataList);
		invocationSequenceDataList = aggregationPerformer.getResultList();
		Collections.sort(invocationSequenceDataList, new Comparator<InvocationSequenceData>() {

			@Override
			public int compare(InvocationSequenceData o1, InvocationSequenceData o2) {
				return Double.compare(InvocationSequenceDataHelper.calculateExclusiveTime(o2), InvocationSequenceDataHelper.calculateExclusiveTime(o1));
			}
		});

		List<AggregatedInvocationSequenceData> timeWastingOperations = new ArrayList<>();
		double sumExecTime = 0;
		for (InvocationSequenceData invocSeqData : invocationSequenceDataList) {
			if (((globalContext.getDuration() - sumExecTime) > baseline) || (sumExecTime < (RT_RALATION_THRESHOLD * globalContext.getDuration()))) {
				sumExecTime += InvocationSequenceDataHelper.calculateExclusiveTime(invocSeqData);
				timeWastingOperations.add((AggregatedInvocationSequenceData) invocSeqData);
			} else {
				break;
			}
		}
		return timeWastingOperations;
	}

	private List<InvocationSequenceData> asInvocationSequenceDataList(List<InvocationSequenceData> invocationSequences, final List<InvocationSequenceData> resultList) {
		for (InvocationSequenceData invocationSequence : invocationSequences) {
			if ((null != invocationSequence.getTimerData()) && invocationSequence.getTimerData().isExclusiveTimeDataAvailable()) {
				resultList.add(invocationSequence);
			} else if ((null != invocationSequence.getSqlStatementData()) && invocationSequence.getSqlStatementData().isExclusiveTimeDataAvailable()) {
				invocationSequence.setTimerData(invocationSequence.getSqlStatementData());
				resultList.add(invocationSequence);
			}
			asInvocationSequenceDataList(invocationSequence.getNestedSequences(), resultList);
		}

		return resultList;
	}
}
