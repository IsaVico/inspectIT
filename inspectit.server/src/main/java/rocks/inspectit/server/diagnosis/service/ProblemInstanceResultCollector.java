/**
 *
 */
package rocks.inspectit.server.diagnosis.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import rocks.inspectit.server.diagnosis.engine.session.ISessionResultCollector;
import rocks.inspectit.server.diagnosis.engine.session.SessionContext;
import rocks.inspectit.server.diagnosis.engine.tag.Tag;
import rocks.inspectit.server.diagnosis.engine.tag.TagState;
import rocks.inspectit.server.diagnosis.service.results.ProblemOccurrence;
import rocks.inspectit.server.diagnosis.service.results.ProblemOccurrence.CauseStructure;
import rocks.inspectit.server.diagnosis.service.rules.RuleConstants;
import rocks.inspectit.shared.all.communication.data.AggregatedInvocationSequenceData;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;

/**
 * @author Alexander Wert
 *
 */
public class ProblemInstanceResultCollector implements ISessionResultCollector<InvocationSequenceData, List<ProblemOccurrence>> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<ProblemOccurrence> collect(SessionContext<InvocationSequenceData> sessionContext) {
		List<ProblemOccurrence> problems = new ArrayList<>();
		InvocationSequenceData inputInvocationSequence = sessionContext.getInput();
		Collection<Tag> leafTags = sessionContext.getStorage().mapTags(TagState.LEAF).values();
		for (Tag leafTag : leafTags) {
			ProblemOccurrence problem = new ProblemOccurrence(inputInvocationSequence, getGlobalContext(leafTag), getProblemContext(leafTag), getRootCauseInvocations(leafTag),
					getCauseStructure(leafTag));
			problems.add(problem);
		}
		return problems;
	}

	private InvocationSequenceData getGlobalContext(Tag leafTag) {
		while (null != leafTag) {
			if (leafTag.getType().equals(RuleConstants.TAG_GLOBAL_CONTEXT)) {
				return (InvocationSequenceData) leafTag.getValue();
			}
			leafTag = leafTag.getParent();
		}

		throw new RuntimeException("Global context could not be found!");
	}

	private InvocationSequenceData getProblemContext(Tag leafTag) {
		while (null != leafTag) {
			if (leafTag.getType().equals(RuleConstants.TAG_PROBLEM_CONTEXT)) {
				return (InvocationSequenceData) leafTag.getValue();
			}
			leafTag = leafTag.getParent();
		}

		throw new RuntimeException("Problem context could not be found!");
	}

	private AggregatedInvocationSequenceData getRootCauseInvocations(Tag leafTag) {
		while (null != leafTag) {
			if (leafTag.getType().equals(RuleConstants.TAG_PROBLEM_CAUSE)) {
				return (AggregatedInvocationSequenceData) leafTag.getValue();
			}
			leafTag = leafTag.getParent();
		}

		throw new RuntimeException("Problem root cause could not be found!");
	}

	private CauseStructure getCauseStructure(Tag leafTag) {
		while (null != leafTag) {
			if (leafTag.getType().equals(RuleConstants.TAG_CAUSE_STRUCTURE)) {
				return (CauseStructure) leafTag.getValue();
			}
			leafTag = leafTag.getParent();
		}

		throw new RuntimeException("Cause structure could not be found!");
	}
}
