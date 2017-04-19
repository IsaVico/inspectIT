/**
 *
 */
package rocks.inspectit.server.diagnosis.service.rules.impl;

import java.util.Stack;

import rocks.inspectit.server.diagnosis.engine.rule.annotation.Action;
import rocks.inspectit.server.diagnosis.engine.rule.annotation.Rule;
import rocks.inspectit.server.diagnosis.engine.rule.annotation.TagValue;
import rocks.inspectit.server.diagnosis.service.results.ProblemOccurrence.CauseStructure;
import rocks.inspectit.server.diagnosis.service.results.ProblemOccurrence.CauseType;
import rocks.inspectit.server.diagnosis.service.rules.InvocationSequenceDataIterator;
import rocks.inspectit.server.diagnosis.service.rules.RuleConstants;
import rocks.inspectit.shared.all.communication.data.AggregatedInvocationSequenceData;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceDataHelper;

/**
 * @author Alexander Wert
 *
 */
@Rule(name = "CauseStructureRule")
public class CauseStructureRule {

	@TagValue(type = RuleConstants.TAG_PROBLEM_CONTEXT)
	private InvocationSequenceData problemContext;

	@TagValue(type = RuleConstants.TAG_PROBLEM_CAUSE)
	private AggregatedInvocationSequenceData cause;

	@Action(resultTag = RuleConstants.TAG_CAUSE_STRUCTURE)
	public CauseStructure action() {
		if (cause.size() == 1) {
			return new CauseStructure(CauseType.SINGLE, 0);
		}

		InvocationSequenceDataIterator iterator = new InvocationSequenceDataIterator(problemContext, true);

		Stack<Integer> recursionStack = new Stack<>();
		int maxRecursionDepth = 0;
		while (iterator.hasNext()) {
			InvocationSequenceData invocation = iterator.next();
			if (!recursionStack.isEmpty() && (recursionStack.peek() >= iterator.currentDepth())) {
				recursionStack.pop();
			}

			if (isCauseInvocation(invocation)) {
				recursionStack.push(iterator.currentDepth());
				if (recursionStack.size() > maxRecursionDepth) {
					maxRecursionDepth = recursionStack.size();
				}
			}
		}

		if (maxRecursionDepth > 0) {
			return new CauseStructure(CauseType.RECURSIVE, maxRecursionDepth);
		} else {
			return new CauseStructure(CauseType.ITERATIVE, 0);
		}
	}

	private boolean isCauseInvocation(InvocationSequenceData invocation) {
		if (InvocationSequenceDataHelper.hasSQLData(invocation) && InvocationSequenceDataHelper.hasSQLData(cause)) {
			return (invocation.getMethodIdent() == cause.getMethodIdent()) && invocation.getSqlStatementData().getSql().equals(cause.getSqlStatementData().getSql());
		} else if (InvocationSequenceDataHelper.hasTimerData(invocation)) {
			return invocation.getMethodIdent() == cause.getMethodIdent();
		}

		return false;
	}
}
