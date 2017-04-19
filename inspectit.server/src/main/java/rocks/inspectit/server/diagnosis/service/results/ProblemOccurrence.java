/**
 *
 */
package rocks.inspectit.server.diagnosis.service.results;

import rocks.inspectit.shared.all.communication.data.AggregatedInvocationSequenceData;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceDataHelper;
import rocks.inspectit.shared.all.communication.data.TimerData;

/**
 * @author Alexander Wert
 *
 */
public class ProblemOccurrence {
	private InvocationIdentifier requestRoot;
	private InvocationIdentifier globalContext;
	private InvocationIdentifier problemContext;
	private RootCause rootCause;
	private CauseStructure causeStructure;

	/**
	 * Default constructor.
	 */
	public ProblemOccurrence() {
	}

	/**
	 * @param requestRoot
	 * @param globalContext
	 * @param problemContext
	 * @param rootCause
	 * @param causeStructure
	 */
	public ProblemOccurrence(InvocationIdentifier requestRoot, InvocationIdentifier globalContext, InvocationIdentifier problemContext, RootCause rootCause, CauseStructure causeStructure) {
		super();
		this.requestRoot = requestRoot;
		this.globalContext = globalContext;
		this.problemContext = problemContext;
		this.rootCause = rootCause;
		this.causeStructure = causeStructure;
	}

	public ProblemOccurrence(InvocationSequenceData requestRoot, InvocationSequenceData globalContext, InvocationSequenceData problemContext, AggregatedInvocationSequenceData rootCause,
			CauseStructure causeStructure) {
		this.requestRoot = new InvocationIdentifier(requestRoot.getMethodIdent(), requestRoot.getId());
		this.globalContext = new InvocationIdentifier(globalContext.getMethodIdent(), globalContext.getId());
		this.problemContext = new InvocationIdentifier(problemContext.getMethodIdent(), problemContext.getId());
		TimerData timerData = null;
		if (InvocationSequenceDataHelper.hasTimerData(rootCause)) {
			timerData = rootCause.getTimerData();
		} else if (InvocationSequenceDataHelper.hasSQLData(rootCause)) {
			timerData = rootCause.getSqlStatementData();
		}

		this.rootCause = new RootCause(rootCause.getMethodIdent(), timerData);
		for (InvocationSequenceData invocation : rootCause.getRawInvocationsSequenceElements()) {
			this.rootCause.getInvocationIds().add(invocation.getId());
		}
		this.causeStructure = causeStructure;
	}

	/**
	 * Gets {@link #requestRoot}.
	 *
	 * @return {@link #requestRoot}
	 */
	public InvocationIdentifier getRequestRoot() {
		return requestRoot;
	}

	/**
	 * Sets {@link #requestRoot}.
	 *
	 * @param requestRoot
	 *            New value for {@link #requestRoot}
	 */
	public void setRequestRoot(InvocationIdentifier requestRoot) {
		this.requestRoot = requestRoot;
	}

	/**
	 * Gets {@link #globalContext}.
	 *
	 * @return {@link #globalContext}
	 */
	public InvocationIdentifier getGlobalContext() {
		return globalContext;
	}

	/**
	 * Sets {@link #globalContext}.
	 *
	 * @param globalContext
	 *            New value for {@link #globalContext}
	 */
	public void setGlobalContext(InvocationIdentifier globalContext) {
		this.globalContext = globalContext;
	}

	/**
	 * Gets {@link #problemContext}.
	 *
	 * @return {@link #problemContext}
	 */
	public InvocationIdentifier getProblemContext() {
		return problemContext;
	}

	/**
	 * Sets {@link #problemContext}.
	 *
	 * @param problemContext
	 *            New value for {@link #problemContext}
	 */
	public void setProblemContext(InvocationIdentifier problemContext) {
		this.problemContext = problemContext;
	}

	/**
	 * Gets {@link #rootCause}.
	 *
	 * @return {@link #rootCause}
	 */
	public RootCause getRootCause() {
		return rootCause;
	}

	/**
	 * Sets {@link #rootCause}.
	 *
	 * @param rootCause
	 *            New value for {@link #rootCause}
	 */
	public void setRootCause(RootCause rootCause) {
		this.rootCause = rootCause;
	}

	/**
	 * Gets {@link #causeStructure}.
	 *
	 * @return {@link #causeStructure}
	 */
	public CauseStructure getCauseStructure() {
		return causeStructure;
	}

	/**
	 * Sets {@link #causeStructure}.
	 *
	 * @param causeStructure
	 *            New value for {@link #causeStructure}
	 */
	public void setCauseStructure(CauseStructure causeStructure) {
		this.causeStructure = causeStructure;
	}

	public static class CauseStructure {
		private CauseType causeType;
		private int depth;

		/**
		 * @param causeType
		 * @param depth
		 */
		public CauseStructure(CauseType causeType, int depth) {
			super();
			this.causeType = causeType;
			this.depth = depth;
		}

		/**
		 * Gets {@link #causeType}.
		 *
		 * @return {@link #causeType}
		 */
		public CauseType getCauseType() {
			return causeType;
		}

		/**
		 * Sets {@link #causeType}.
		 *
		 * @param causeType
		 *            New value for {@link #causeType}
		 */
		public void setCauseType(CauseType causeType) {
			this.causeType = causeType;
		}

		/**
		 * Gets {@link #depth}.
		 *
		 * @return {@link #depth}
		 */
		public int getDepth() {
			return depth;
		}

		/**
		 * Sets {@link #depth}.
		 *
		 * @param depth
		 *            New value for {@link #depth}
		 */
		public void setDepth(int depth) {
			this.depth = depth;
		}

	}

	public static enum CauseType {
		SINGLE, ITERATIVE, RECURSIVE
	}

}
