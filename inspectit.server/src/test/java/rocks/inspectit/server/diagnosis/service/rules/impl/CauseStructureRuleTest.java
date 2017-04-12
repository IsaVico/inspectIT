package rocks.inspectit.server.diagnosis.service.rules.impl;


import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import rocks.inspectit.server.diagnosis.service.results.ProblemOccurrence.CauseStructure;
import rocks.inspectit.server.diagnosis.service.results.ProblemOccurrence.CauseType;
import rocks.inspectit.shared.all.communication.data.AggregatedInvocationSequenceData;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;
import rocks.inspectit.shared.all.communication.data.TimerData;
import rocks.inspectit.shared.all.testbase.TestBase;

public class CauseStructureRuleTest extends TestBase {

	@InjectMocks
	CauseStructureRule causeStructureRule;

	@Mock
	InvocationSequenceData problemContext;

	@Mock
	AggregatedInvocationSequenceData cause;

	CauseStructure causeStructure;

	private static final Random RANDOM = new Random();

	private static final long METHOD_IDENT_EQUAL = new Long(108);
	private static final long METHOD_IDENT_DIFF = RANDOM.nextLong();

	public class ActionMethod extends CauseStructureRuleTest {
		List<InvocationSequenceData> rawInvocations = new ArrayList<>();

		@BeforeMethod
		private void init() {
			rawInvocations.add(new InvocationSequenceData());
			when(cause.getRawInvocationsSequenceElements()).thenReturn(rawInvocations);
			when(cause.getMethodIdent()).thenReturn(METHOD_IDENT_EQUAL);
		}

		@AfterMethod
		private void clear() {
			rawInvocations.clear();
		}

		/**
		 * Populates the problem context with the identifiers and method identifiers indicated.
		 *
		 * @param idSeq1
		 *            Id for the first sequence
		 * @param methodIdentSeq1
		 *            Id for the method in first sequence
		 * @param idSeq2
		 *            Id for the second sequence
		 * @param methodIdentSeq2
		 *            Id for the method in second sequence
		 * @param idSeq3
		 *            Id for the third sequence
		 * @param methodIdentSeq3
		 *            Id for the method in third sequence
		 */
		private void populateProblemContext(int idSeq1, long methodIdentSeq1, int idSeq2, long methodIdentSeq2, int idSeq3, long methodIdentSeq3) {
			rawInvocations.add(generateSequence(2, METHOD_IDENT_DIFF));
			when(cause.getRawInvocationsSequenceElements()).thenReturn(rawInvocations);

			problemContext = generateSequence(idSeq1, methodIdentSeq1);
			InvocationSequenceData parentSequence = generateSequence(idSeq2, methodIdentSeq2);
			parentSequence.setParentSequence(generateSequence(idSeq3, methodIdentSeq3));
			problemContext.setParentSequence(parentSequence);
		}

		/**
		 * Generates a new invocation sequence with the identifier and method identifier indicated.
		 *
		 * @param id
		 *            Id for the sequence
		 * @param methodIdent
		 *            Id for the method
		 * @return Returns a new instance of an invocation sequence.
		 */
		private InvocationSequenceData generateSequence(int id, long methodIdent) {
			InvocationSequenceData invocationSequence = new InvocationSequenceData(new Timestamp(new Date().getTime()), (id * 10), (id * 10), methodIdent);
			invocationSequence.setTimerData(new TimerData(new Timestamp(System.currentTimeMillis()), 10L, 20L, 30L));
			return invocationSequence;
		}

		@Test
		public void actionMethodMustReturnAnInstanceOfSingleCauseTypeIfTheCauseHasJustOneElement() {
			when(cause.size()).thenReturn(1);
			causeStructure = causeStructureRule.action();
			assertEquals(causeStructure.getCauseType(), CauseType.SINGLE);
		}

		@Test
		public void actionMethodMustReturnAnInstanceOfRecursiveCauseTypeIfTheCauseHasMoreThanOneSequenceWithTheSameMethodIdent() {
			when(cause.size()).thenReturn(3);
			InvocationSequenceData problemContextParent = generateSequence(2, METHOD_IDENT_DIFF);
			problemContextParent.setParentSequence(generateSequence(3, METHOD_IDENT_EQUAL));
			when(problemContext.getParentSequence()).thenReturn(problemContextParent);
			when(problemContext.getMethodIdent()).thenReturn(METHOD_IDENT_EQUAL);
			when(problemContext.getTimerData()).thenReturn(new TimerData(new Timestamp(System.currentTimeMillis()), 10L, 20L, 30L));
			causeStructure = causeStructureRule.action();
			assertEquals(causeStructure.getCauseType(), CauseType.RECURSIVE);
		}

		@Test
		public void actionMethodMustReturnAnInstanceOfIterativeCauseTypeIfTheCauseHasNotMoreThanOneSequenceWithTheSameMethodIdent() {
			populateProblemContext(1, METHOD_IDENT_DIFF, 2, METHOD_IDENT_DIFF, 3, METHOD_IDENT_DIFF);
			causeStructure = causeStructureRule.action();
			assertEquals(causeStructure.getCauseType(), CauseType.ITERATIVE);
		}

		@Test
		public void actionMethodMustReturnAnInstanceOfIterativeCauseTypeIfTheCauseHasNoElements() {
			when(cause.size()).thenReturn(0);
			causeStructure = causeStructureRule.action();
			assertEquals(causeStructure.getCauseType(), CauseType.ITERATIVE);
		}
	}
}
