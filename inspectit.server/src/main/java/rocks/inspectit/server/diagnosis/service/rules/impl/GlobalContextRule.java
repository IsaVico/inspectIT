package rocks.inspectit.server.diagnosis.service.rules.impl;

import java.util.List;

import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;

import rocks.inspectit.server.diagnosis.engine.rule.annotation.Action;
import rocks.inspectit.server.diagnosis.engine.rule.annotation.Rule;
import rocks.inspectit.server.diagnosis.engine.rule.annotation.SessionVariable;
import rocks.inspectit.server.diagnosis.engine.rule.annotation.TagValue;
import rocks.inspectit.server.diagnosis.engine.tag.Tags;
import rocks.inspectit.server.diagnosis.service.rules.RuleConstants;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;

/**
 * @author Alexander Wert
 *
 */
@Rule(name = "GlobalContextRule")
public class GlobalContextRule {
	private static final double PROPORTION = 0.8;

	@TagValue(type = Tags.ROOT_TAG)
	private InvocationSequenceData invocationSequenceRoot;

	@SessionVariable(name = RuleConstants.VAR_BASELINE, optional = false)
	double baseline;

	@Action(resultTag = RuleConstants.TAG_GLOBAL_CONTEXT)
	public InvocationSequenceData action() {
		// TODO: reimplement with 3 Sigma approach of selecting whether to dig deeper instead of the
		// baseline comparison
		double traceDuration = invocationSequenceRoot.getDuration();
		InvocationSequenceData childWithMaxDuration = invocationSequenceRoot;
		InvocationSequenceData currentInvocationSequence;
		do {
			currentInvocationSequence = childWithMaxDuration;
			childWithMaxDuration = getChildWithMaxDuration(currentInvocationSequence);
			// if (null != childWithMaxDuration) {
			// System.out.println("+ " +
			// rocks.inspectit.server.diagnosis.rules.UTIL.instance.invocToStr(childWithMaxDuration));
			// }

		} while ((null != childWithMaxDuration) && isDominatingCall(childWithMaxDuration, traceDuration));

		return currentInvocationSequence;
	}

	private boolean isDominatingCall(InvocationSequenceData childWithMaxDuration, double traceDuration) {
		return ((traceDuration - childWithMaxDuration.getDuration()) < baseline) && (childWithMaxDuration.getDuration() > (traceDuration * PROPORTION));
	}

	/**
	 * @param childWithMaxDuration
	 * @param nestedSequences
	 * @return
	 */
	private boolean isOutlier(InvocationSequenceData childWithMaxDuration, List<InvocationSequenceData> nestedSequences) {
		if ((nestedSequences.size() == 1) && (nestedSequences.get(0) == childWithMaxDuration)) {
			return true;
		}
		double sum = 0.0;
		int numElements = nestedSequences.size() - 1;
		double[] durations = new double[numElements];
		int i = 0;
		for (InvocationSequenceData child : nestedSequences) {
			if (child != childWithMaxDuration) { // NO-PMD not equals on purpose
				sum += child.getDuration();
				durations[i] = child.getDuration();
				i++;
			}
		}
		double mean = sum / numElements;
		StandardDeviation standardDeviation = new StandardDeviation(false);
		double sd = standardDeviation.evaluate(durations, mean);
		return childWithMaxDuration.getDuration() > (mean + (3 * sd));
	}

	/**
	 * @param currentInvocationSequence
	 * @param childWithMaxDuration
	 * @return
	 */
	private InvocationSequenceData getChildWithMaxDuration(InvocationSequenceData currentInvocationSequence) {
		boolean first = true;
		InvocationSequenceData childWithMaxDuration = null;
		for (InvocationSequenceData child : currentInvocationSequence.getNestedSequences()) {
			if (first) {
				childWithMaxDuration = child;
				first = false;
			} else if (child.getDuration() > childWithMaxDuration.getDuration()) {
				childWithMaxDuration = child;
			}
		}
		return childWithMaxDuration;
	}
	//
	// @Action(resultTag = RuleConstants.TAG_GLOBAL_CONTEXT, resultQuantity =
	// Action.Quantity.SINGLE)
	// public InvocationSequenceData action() {
	// // TODO: reimplement with 3 Sigma approach of selecting whether to dig deeper instead of the
	// // baseline comparison
	//
	// double traceDuration = invocationSequenceRoot.getDuration();
	//
	// InvocationSequenceData childWithMaxDuration = invocationSequenceRoot;
	// InvocationSequenceData current = childWithMaxDuration;
	//
	// do {
	// current = childWithMaxDuration;
	//
	// childWithMaxDuration = null;
	// childWithMaxDuration = getChildWithMaxDuration(current, childWithMaxDuration);
	//
	// } while (until(traceDuration, childWithMaxDuration));
	// UTIL.instance.invocToStr(current);
	// return current;
	// }
	//
	// private boolean until(double traceDuration, InvocationSequenceData next) {
	// if (next != null) {
	// System.out.println(UTIL.instance.invocToStr(next) + " : " + next.getDuration() + " | " +
	// baseline + " | " + traceDuration);
	// } else {
	// System.out.println("NULL");
	// }
	//
	// return next != null && traceDuration - next.getDuration() < baseline;
	// }

}
