package rocks.inspectit.server.diagnosis.service.rules.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Random;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import rocks.inspectit.shared.all.communication.data.AggregatedInvocationSequenceData;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;
import rocks.inspectit.shared.all.communication.data.TimerData;
import rocks.inspectit.shared.all.testbase.TestBase;

public class ProblemCauseRuleTest extends TestBase {

	@InjectMocks
	ProblemCauseRule problemCauseRule;

	@Mock(name = "problemContext")
	InvocationSequenceData problemContext;

	@Mock(name = "timeWastingOperation")
	AggregatedInvocationSequenceData timeWastingOperation;

	AggregatedInvocationSequenceData rootCause;

	@BeforeTest
	private void init() {
		if (problemCauseRule == null) {
			problemCauseRule = new ProblemCauseRule();
		}
	}

	private static final Random RANDOM = new Random();

	private static final long METHOD_IDENT_EQUAL = new Long(108);
	private static final long METHOD_IDENT_DIFF = new Long(2);

	public static class ActionTimerData extends ProblemCauseRuleTest {

		/**
		 * Initializes the data of the class
		 *
		 * @param hastTimerData
		 *            Boolean which indicates if the sequences are expected to have TimerData or
		 *            not.
		 * @param methodIdent
		 *            Identifier of the method to set it to the timeWastingOperation, since is used
		 *            to identify the candidates causeRule
		 */
		private void init(boolean hastTimerData, long methodIdent) {
			initializeProblemContext(hastTimerData);

			problemCauseRule.timeWastingOperation = new AggregatedInvocationSequenceData();
			problemCauseRule.timeWastingOperation.setMethodIdent(methodIdent);
		}

		/**
		 * Initializes the problem context with or without timerData
		 *
		 * @param hasTimerData
		 *            Boolean data to indicate if the nested sequences must have timer data or not.
		 */
		void initializeProblemContext(boolean hasTimerData) {
			initDefaultProblemContext();
			if (hasTimerData) {
				initializeNestedSequencesWithTimerData();
			} else {
				initializeNestedSequencesWithoutTimerData();
			}
		}

		/**
		 * Initializes the default data of the problem context.
		 */
		void initDefaultProblemContext() {
			problemCauseRule.problemContext = new InvocationSequenceData();
			problemCauseRule.problemContext.setMethodIdent(1L);
			problemCauseRule.problemContext.setDuration(RANDOM.nextDouble() + 1000);
		}

		/**
		 * Initializes the nested sequences of the problem context with timer data.
		 *
		 * @return A list of invocation data which contains the nested sequences
		 */
		void initializeNestedSequencesWithTimerData() {
			problemCauseRule.problemContext.getNestedSequences().add(generateSequenceWithTimerData(1, RANDOM.nextDouble(), METHOD_IDENT_EQUAL));
			problemCauseRule.problemContext.getNestedSequences().add(generateSequenceWithTimerData(2, -1, METHOD_IDENT_DIFF));
		}

		/**
		 * Generate a sequence with a timer data.
		 *
		 * @param id
		 *            Id to set the the data of the invocation sequence.
		 * @param exclusiveMin
		 *            Double with the data to set the field of the timer data. -1 is consider as
		 *            default value.
		 * @param methodIdent
		 *            Identifier of the method.
		 * @return Returns the created instance of an invocation sequence data.
		 */
		InvocationSequenceData generateSequenceWithTimerData(int id, double exclusiveMin, long methodIdent) {
			InvocationSequenceData sequenceData = new InvocationSequenceData(new Timestamp(new Date().getTime()), (id * 10), (id * 10), methodIdent);
			if (exclusiveMin != -1) {
				sequenceData.setTimerData(initializeTimerData(exclusiveMin));
			}
			return sequenceData;
		}

		/**
		 * Initialize the timer data. With the fields exclusiveMIn and exclusiveDuration
		 * initialized.
		 *
		 * @return The initialized timer data.
		 */
		@SuppressWarnings("PMD")
		TimerData initializeTimerData(double exclusiveMin) {
			Class<?> timerDataClass = null;
			TimerData timerData = new TimerData(new Timestamp(System.currentTimeMillis()), 10L, 20L, 30L);

			try {
				timerDataClass = Class.forName(timerData.getClass().getName());
				timerData = (TimerData) timerDataClass.newInstance();
				initField(timerData, timerDataClass, "exclusiveMin", exclusiveMin);
				initField(timerData, timerDataClass, "exclusiveDuration", RANDOM.nextDouble());
			} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | SecurityException e) {
				e.printStackTrace();
			}

			return timerData;
		}


		/**
		 * Sets the field of the timer data with the value indicated.
		 *
		 * @param timerData
		 *            Instance of the timer data which contains the field.
		 * @param timerDataClass
		 *            Class of the timer data to get the field.
		 * @param string
		 *            Name of the field
		 * @param exclusiveMin
		 *            Value to set to the field.
		 */
		@SuppressWarnings("PMD")
		private void initField(TimerData timerData, Class<?> timerDataClass, String fieldName, double fieldValue) {
			Field field;
			try {
				field = timerDataClass.getDeclaredField(fieldName);
				field.setAccessible(true);
				field.set(timerData, fieldValue);
			} catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException e) {
				e.printStackTrace();
			}
		}

		/**
		 * Initializes the nested sequences of the problem context without timer data.
		 */
		void initializeNestedSequencesWithoutTimerData() {
			problemCauseRule.problemContext.getNestedSequences().add(new InvocationSequenceData(new Timestamp(new Date().getTime()), 10, 10, METHOD_IDENT_EQUAL));
			problemCauseRule.problemContext.getNestedSequences().add(new InvocationSequenceData(new Timestamp(new Date().getTime()), 20, 20, METHOD_IDENT_DIFF));
		}



		@Test
		public void actionMethodMustReturnANotNullRootCauseRuleWhenMethodIdentIsEqualAndTheInvocationHasTimerData() {
			init(true, METHOD_IDENT_EQUAL);
			rootCause = problemCauseRule.action();
			assertNotNull(rootCause, "The returned root cause rule must not be null");
		}

		@Test
		public void actionMethodMustReturnANullRootCauseRuleWhenMethodIdentIsEqualAndInvocationHasNotTimerData() {
			init(false, METHOD_IDENT_EQUAL);
			rootCause = problemCauseRule.action();
			assertNull(rootCause, "The returned root cause rule must be null");
		}

		@Test
		public void actionMethodMustReturnANullRootCauseRuleWhenMethodIdentIsNotEqualAndTheInvocationHasTimerData() {
			init(true, RANDOM.nextLong());
			rootCause = problemCauseRule.action();
			assertNull(rootCause, "The returned root cause rule must not be null");
		}

		@Test
		public void actionMethodMustReturnANullRootCauseRuleWhenMethodIdentIsNotEqualAndInvocationHasNotTimerData() {
			init(false, RANDOM.nextLong());
			rootCause = problemCauseRule.action();
			assertNull(rootCause, "The returned root cause rule must be null");
		}

		@Test
		public void actionMethodMustReturnACauseRuleWithOneElementInRawInvocationSequence() {
			init(true, METHOD_IDENT_EQUAL);
			rootCause = problemCauseRule.action();
			assertThat("Raw invocation sequence must have one element", rootCause.getRawInvocationsSequenceElements(), hasSize(1));
		}

		@Test
		public void actionMethodMustReturnACauseRuleWithTwoElementsInRawInvocationSequence() {
			init(true, METHOD_IDENT_EQUAL);
			problemCauseRule.problemContext.getNestedSequences().add(generateSequenceWithTimerData(3, RANDOM.nextDouble(), METHOD_IDENT_EQUAL));
			rootCause = problemCauseRule.action();
			assertThat("Raw invocation sequence must have two elements", rootCause.getRawInvocationsSequenceElements(), hasSize(2));
		}
	}

}
