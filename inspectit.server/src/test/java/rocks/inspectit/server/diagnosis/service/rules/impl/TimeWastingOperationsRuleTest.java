package rocks.inspectit.server.diagnosis.service.rules.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.testng.annotations.Test;

import rocks.inspectit.shared.all.communication.data.AggregatedInvocationSequenceData;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;
import rocks.inspectit.shared.all.communication.data.SqlStatementData;
import rocks.inspectit.shared.all.communication.data.TimerData;
import rocks.inspectit.shared.all.testbase.TestBase;

/**
 * @author Isabel Vico Peinado
 */
@SuppressWarnings("PMD")
public class TimeWastingOperationsRuleTest extends TestBase {

	@InjectMocks
	TimeWastingOperationsRule timeWastingOperationsRule;

	@Mock
	InvocationSequenceData globalContext;

	List<AggregatedInvocationSequenceData> timeWastingOperationsResults;
	private static final Random RANDOM = new Random();
	private static final Double DURATION = RANDOM.nextDouble() + 1000;
	private static final Timestamp DATE = new Timestamp(new Date().getTime());

	/**
	 * Checks that the sequenceData have all the mandatory attributes.
	 *
	 * @param aggregatedInvocationSequenceData
	 *            Sequence data to check if has all the mandatory data.
	 */
	void isAValidRule(AggregatedInvocationSequenceData aggregatedInvocationSequenceData) {
		for (InvocationSequenceData aggregatedSequence : aggregatedInvocationSequenceData.getRawInvocationsSequenceElements()) {
			assertNotNull(aggregatedSequence, "The aggregated sequence cannot be null");
			assertNotNull(aggregatedSequence.getDuration(), "Duration of the aggregated sequence cannot be null");
			assertNotNull(aggregatedSequence.getStart(), "Start time of the aggregated cannot be null");
			assertNotNull(aggregatedSequence.getEnd(), "End time of the aggregated cannot be null");
			assertNotNull(aggregatedSequence.getChildCount(), "Child count of the aggregated sequence cannot be null");
			assertNotNull(aggregatedSequence.getApplicationId(), "ApplicationId of the aggregated sequence cannot be null");
			assertNotNull(aggregatedSequence.getBusinessTransactionId(), "Business transaction id of the aggregated sequence cannot be null");
		}
	}

	public class ActionTimerData extends TimeWastingOperationsRuleTest {
		/**
		 * Populates the nested sequences.
		 *
		 * @return A list of invocation data which contains the nested sequences
		 */
		private List<InvocationSequenceData> populateNestedSequences() {
			List<InvocationSequenceData> nestedSequences = new ArrayList<>();
			nestedSequences.add(generateSequence(1, RANDOM.nextDouble()));
			nestedSequences.add(generateSequence(2, -1));
			return nestedSequences;
		}

		/**
		 * Initializes the timer data. With the exclusiveMin initialized.
		 *
		 * @return The initialized timer data.
		 */
		TimerData initializeTimerData(double exclusiveMin) {
			Class<?> timerDataClass = null;
			TimerData timerData = new TimerData(new Timestamp(System.currentTimeMillis()), 10L, 20L, 30L);

			try {
				timerDataClass = Class.forName(timerData.getClass().getName());
				timerData = (TimerData) timerDataClass.newInstance();
				Field exclusiveMinField = timerDataClass.getDeclaredField("exclusiveMin");
				exclusiveMinField.setAccessible(true);
				exclusiveMinField.set(timerData, exclusiveMin);
			} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
				e.printStackTrace();
			}

			return timerData;
		}

		/**
		 * Generates a sequence with timer data. And initializing it if necessary.
		 *
		 * @param id
		 *            If of the sequence in order to give different sequences
		 * @param exclusiveMin
		 *            Value to initialize the timerData
		 * @return Returns a timerData with exclusiveMin initialize
		 */
		InvocationSequenceData generateSequence(int id, double exclusiveMin) {
			InvocationSequenceData sequenceData = new InvocationSequenceData(DATE, id, id, new Long(id));
			if (exclusiveMin != -1) {
				sequenceData.setTimerData(initializeTimerData(exclusiveMin));
			}
			return sequenceData;
		}

		/**
		 * Initialize mock data to be returned in the proper place
		 *
		 * @param duration
		 *            Duration to be returned
		 */
		private void initMock(double duration) {
			timeWastingOperationsRule.baseline = 1000d;
			when(globalContext.getDuration()).thenReturn(duration);
			when(globalContext.getNestedSequences()).thenReturn(populateNestedSequences());
		}

		private void initContextWithThreeSequences() {
			when(globalContext.getDuration()).thenReturn(DURATION);
			List<InvocationSequenceData> nestedSequences = populateNestedSequences();
			nestedSequences.add(generateSequence(3, RANDOM.nextDouble()));
			when(globalContext.getNestedSequences()).thenReturn(nestedSequences);
		}

		/**
		 * Tests that the action method of the rule is not returning a null group of rules.
		 */
		@Test
		public void actionMethodMustReturnANotNullGroupOfRules() {
			initMock(DURATION);
			timeWastingOperationsResults = timeWastingOperationsRule.action();
			assertNotNull(timeWastingOperationsResults, "The returned list of rules must not be null");
		}

		/**
		 * Tests that the action method of the rule is not returning an empty group of rules.
		 */
		@Test
		public void actionMethodMustReturnANotEmptyGroupOfRulesWhenTheDurationIsTooLong() {
			initMock(DURATION);
			timeWastingOperationsResults = timeWastingOperationsRule.action();
			assertThat("Action method must return a list of rules not empty", timeWastingOperationsResults, not(hasSize(0)));
		}

		/**
		 * Tests that the action method of the rule is returning an empty group of rules since the
		 * duration is too short.
		 */
		@Test
		public void actionMethodMustReturnAEmptyGroupOfRulesWhenTheDurationIsZero() {
			initMock(new Double(0));
			timeWastingOperationsResults = timeWastingOperationsRule.action();
			assertThat("Action method must return an array of rules not empty", timeWastingOperationsResults, hasSize(0));
		}

		/**
		 * Tests that the action method of the rule is not returning the correct number of elements.
		 */
		@Test
		public void actionMethodMustReturnAGroupOfRulesWithOneElement() {
			initMock(DURATION);
			timeWastingOperationsResults = timeWastingOperationsRule.action();
			assertThat("Action method must return a list of one rule", timeWastingOperationsResults, hasSize(1));
		}

		/**
		 * Tests that the action method of the rule is not returning the correct number of elements.
		 */
		@Test
		public void actionMethodMustReturnAGroupOfRulesWithTwoElements() {
			initContextWithThreeSequences();
			timeWastingOperationsResults = timeWastingOperationsRule.action();
			assertThat("Action method must return a list with two rules", timeWastingOperationsResults, hasSize(2));
		}

		/**
		 * Tests that the action method of the rule is not returning a valid group of rules.
		 */
		@Test
		public void actionMethodMustReturnAValidGroupOfRules() {
			initMock(DURATION);
			List<AggregatedInvocationSequenceData> timeWastingOperations = timeWastingOperationsRule.action();
			assertThat("Action method must return a list of rules not empty", timeWastingOperations, not(hasSize(0)));
			for (AggregatedInvocationSequenceData aggregatedInvocationSequenceData : timeWastingOperations) {
				isAValidRule(aggregatedInvocationSequenceData);
			}
		}

		/**
		 * Tests that checks that the results are the expected.
		 */
		@Test
		public void actionMethodMustReturnTheExpectedRules() {
			initContextWithThreeSequences();
			timeWastingOperationsResults = timeWastingOperationsRule.action();
			assertEquals(timeWastingOperationsResults.get(0).getMethodIdent(), 1, "Identifier is not the expected one, the first result must have 1 as method identifier");
			assertEquals(timeWastingOperationsResults.get(1).getMethodIdent(), 3, "Identifier is not the expected one, the first result must have 3 as method identifier");
		}
	}

	public class ActionSqlStatementData extends TimeWastingOperationsRuleTest {

		/**
		 * Populates the nested sequences.
		 *
		 * @return A list of invocation data which contains the nested sequences
		 */
		private List<InvocationSequenceData> populateNestedSequences() {
			List<InvocationSequenceData> nestedSequences = new ArrayList<>();
			nestedSequences.add(generateSequence(1, RANDOM.nextDouble()));
			nestedSequences.add(generateSequence(2, -1));
			return nestedSequences;
		}

		/**
		 * Initializes the timer data. With the exclusiveMin initialized.
		 *
		 * @return The initialized timer data.
		 */
		SqlStatementData initializeSqlStatementData(double exclusiveMin) {
			Class<?> sqlStatementDataClass = null;
			SqlStatementData sqlStatementData = new SqlStatementData();

			try {
				sqlStatementDataClass = Class.forName(sqlStatementData.getClass().getName());
				Class<?> timerDataClass = sqlStatementDataClass.getSuperclass();
				sqlStatementData = (SqlStatementData) sqlStatementDataClass.newInstance();
				Field exclusiveMinField = timerDataClass.getDeclaredField("exclusiveMin");
				exclusiveMinField.setAccessible(true);
				exclusiveMinField.set(sqlStatementData, exclusiveMin);
			} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
				e.printStackTrace();
			}

			return sqlStatementData;
		}

		/**
		 * Generates a sequence with sqlStatementData and timer data. And initializing it if
		 * necessary.
		 *
		 * @param id
		 *            If of the sequence in order to give different sequences
		 * @param exclusiveMin
		 *            Value to initialize the SqlStatementData
		 * @return Returns an invocation sequence with the sqlStatmentData initializes
		 */
		InvocationSequenceData generateSequence(int id, double exclusiveMin) {
			InvocationSequenceData sequenceData = new InvocationSequenceData(DATE, id, id, new Long(id));
			if (exclusiveMin != -1) {
				sequenceData.setSqlStatementData(initializeSqlStatementData(exclusiveMin));
			}
			return sequenceData;
		}

		/**
		 * Initialize mock data to be returned in the proper place
		 *
		 * @param duration
		 *            Duration to be returned
		 */
		private void initMock(double duration) {
			timeWastingOperationsRule.baseline = 1000d;
			when(globalContext.getDuration()).thenReturn(duration);
			when(globalContext.getNestedSequences()).thenReturn(populateNestedSequences());
		}

		private void initContextWithThreeSequences() {
			when(globalContext.getDuration()).thenReturn(DURATION);
			List<InvocationSequenceData> nestedSequences = populateNestedSequences();
			nestedSequences.add(generateSequence(3, RANDOM.nextDouble()));
			when(globalContext.getNestedSequences()).thenReturn(nestedSequences);
		}

		/**
		 * Tests that the action method of the rule is not returning a null group of rules.
		 */
		@Test
		public void actionMethodMustReturnANotNullGroupOfRules() {
			initMock(DURATION);
			timeWastingOperationsResults = timeWastingOperationsRule.action();
			assertNotNull(timeWastingOperationsResults, "The returned list of rules must not be null");
		}

		/**
		 * Tests that the action method of the rule is not returning an empty group of rules.
		 */
		@Test
		public void actionMethodMustReturnANotEmptyGroupOfRulesWhenTheDurationIsTooLong() {
			initMock(DURATION);
			timeWastingOperationsResults = timeWastingOperationsRule.action();
			assertThat("Action method must return a list of rules not empty", timeWastingOperationsResults, not(hasSize(0)));
		}

		/**
		 * Tests that when the duration is zero the result list must be empty.
		 */
		@Test
		public void actionMethodMustReturnAEmptyGroupOfRulesWhenTheDurationIsZero() {
			initMock(new Double(0));
			timeWastingOperationsResults = timeWastingOperationsRule.action();
			assertThat("Action method must return an array of rules not empty", timeWastingOperationsResults, hasSize(0));
		}

		/**
		 * Tests that the action method of the rule is not returning the correct number of elements.
		 */
		@Test
		public void actionMethodMustReturnAGroupOfRulesWithOneElement() {
			initMock(DURATION);
			timeWastingOperationsResults = timeWastingOperationsRule.action();
			assertThat("Action method must return a list of one rule", timeWastingOperationsResults, hasSize(1));
		}

		/**
		 * Tests that the action method of the rule is not returning the correct number of elements.
		 */
		@Test
		public void actionMethodMustReturnAGroupOfRulesWithTwoElements() {
			initContextWithThreeSequences();
			timeWastingOperationsResults = timeWastingOperationsRule.action();
			assertThat("Action method must return a list with two rules", timeWastingOperationsResults, hasSize(2));
		}

		/**
		 * Tests that the action method of the rule is not returning a valid group of rules.
		 */
		@Test
		public void actionMethodMustReturnAValidGroupOfRules() {
			initMock(DURATION);
			timeWastingOperationsResults = timeWastingOperationsRule.action();
			assertThat("Action method must return a list of rules not empty", timeWastingOperationsResults, not(hasSize(0)));
			for (AggregatedInvocationSequenceData aggregatedInvocationSequenceData : timeWastingOperationsResults) {
				isAValidRule(aggregatedInvocationSequenceData);
			}
		}

		/**
		 * Tests that checks that the results are the expected.
		 */
		@Test
		public void actionMethodMustReturnTheExpectedRules() {
			initContextWithThreeSequences();
			timeWastingOperationsResults = timeWastingOperationsRule.action();
			assertEquals(timeWastingOperationsResults.get(0).getMethodIdent(), 1, "Identifier is not the expected one, the first result must have 1 as method identifier");
			assertEquals(timeWastingOperationsResults.get(1).getMethodIdent(), 3, "Identifier is not the expected one, the first result must have 3 as method identifier");
		}
	}

}
