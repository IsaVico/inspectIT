package rocks.inspectit.server.diagnosis.service.rules.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.testng.Assert.assertNotNull;

import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Random;

import org.mockito.InjectMocks;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import rocks.inspectit.shared.all.communication.data.AggregatedInvocationSequenceData;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;
import rocks.inspectit.shared.all.communication.data.SqlStatementData;
import rocks.inspectit.shared.all.communication.data.TimerData;
import rocks.inspectit.shared.all.testbase.TestBase;

/**
 * @author Isabel Vico Peinado
 */
public class TimeWastingOperationsRuleTest extends TestBase {

	@InjectMocks
	TimeWastingOperationsRule timeWastingOperationsRule;

	List<AggregatedInvocationSequenceData> timeWastingOperations;

	private static final Random RANDOM = new Random();

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

	@BeforeTest
	private void init() {
		if (timeWastingOperationsRule == null) {
			timeWastingOperationsRule = new TimeWastingOperationsRule();
		}
		timeWastingOperationsRule.baseline = new Double(1000L);
	}

	public static class ActionTimerData extends TimeWastingOperationsRuleTest {

		/**
		 * Initializes global context with the proper data.
		 *
		 * @return An instance of invocation data sequence with fake data.
		 */
		void initializeGlobalContext(double duration) {
			initializeGlobalContext();
			timeWastingOperationsRule.globalContext.setDuration(duration);
		}

		/**
		 * Initializes the globalContext with timer Data
		 *
		 * @return The invocation data which contains the global context
		 */
		void initializeGlobalContext() {
			timeWastingOperationsRule.globalContext = new InvocationSequenceData();
			timeWastingOperationsRule.globalContext.setMethodIdent(1L);
			timeWastingOperationsRule.globalContext.setDuration(RANDOM.nextDouble() + 1000);

			initializeNestedSequences();
		}

		/**
		 * Initializes the timer data. With the exclusiveMin initialized.
		 *
		 * @return The initialized timer data.
		 */
		@SuppressWarnings("PMD.AvoidPrintStackTrace")
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
		 * Initializes the nested sequences.
		 *
		 * @return A list of invocation data which contains the nested sequences
		 */
		void initializeNestedSequences() {
			timeWastingOperationsRule.globalContext.getNestedSequences().add(generateSequence(1, RANDOM.nextDouble()));
			timeWastingOperationsRule.globalContext.getNestedSequences().add(generateSequence(2, -1));
		}

		/**
		 * Generates a sequence with timer data. And initializing it if necessary.
		 *
		 * @param id
		 *            If of the sequence in order to give different sequences
		 * @param offset
		 * @param exclusiveMin
		 * @return
		 */
		InvocationSequenceData generateSequence(int id, double exclusiveMin) {
			InvocationSequenceData sequenceData = new InvocationSequenceData(new Timestamp(new Date().getTime()), (id * 10), (id * 10), new Long(id));
			if (exclusiveMin != -1) {
				sequenceData.setTimerData(initializeTimerData(exclusiveMin));
			}
			return sequenceData;
		}



		/**
		 * Tests that the action method of the rule is not returning a null group of rules.
		 */
		@Test
		public void actionMethodMustReturnANotNullGroupOfRules() {
			initializeGlobalContext();
			timeWastingOperations = timeWastingOperationsRule.action();
			assertNotNull(timeWastingOperations, "The returned list of rules must not be null");
		}

		/**
		 * Tests that the action method of the rule is not returning an empty group of rules.
		 */
		@Test
		public void actionMethodMustReturnANotEmptyGroupOfRulesWhenTheDurationIsTooLong() {
			initializeGlobalContext();
			timeWastingOperations = timeWastingOperationsRule.action();
			assertThat("Action method must return a list of rules not empty", timeWastingOperations, not(hasSize(0)));
		}

		/**
		 * Tests that the action method of the rule is returning an empty group of rules since the
		 * duration is too short.
		 */
		@Test
		public void actionMethodMustReturnAEmptyGroupOfRulesWhenTheDurationIsTooShort() {
			initializeGlobalContext(new Double(200));
			timeWastingOperations = timeWastingOperationsRule.action();
			assertThat("Action method must return an array of rules not empty", timeWastingOperations, not(hasSize(0)));
		}

		/**
		 * Tests that the action method of the rule is not returning the correct number of elements.
		 */
		@Test
		public void actionMethodMustReturnAGroupOfRulesWithOneElement() {
			initializeGlobalContext();
			timeWastingOperations = timeWastingOperationsRule.action();
			assertThat("Action method must return a list of one rule", timeWastingOperations, hasSize(1));
		}

		/**
		 * Tests that the action method of the rule is not returning the correct number of elements.
		 */
		@Test
		public void actionMethodMustReturnAGroupOfRulesWithTwoElements() {
			initializeGlobalContext();
			timeWastingOperationsRule.globalContext.getNestedSequences().add(generateSequence(3, RANDOM.nextDouble()));
			timeWastingOperations = timeWastingOperationsRule.action();
			assertThat("Action method must return a list with two rules", timeWastingOperations, hasSize(2));
		}

		/**
		 * Tests that the action method of the rule is not returning a valid group of rules.
		 */
		@Test
		public void actionMethodMustReturnAValidGroupOfRules() {
			initializeGlobalContext();
			timeWastingOperations = timeWastingOperationsRule.action();
			assertThat("Action method must return a list of rules not empty", timeWastingOperations, not(hasSize(0)));
			for (AggregatedInvocationSequenceData aggregatedInvocationSequenceData : timeWastingOperations) {
				isAValidRule(aggregatedInvocationSequenceData);
			}
		}
	}

	public static class ActionSqlStatementData extends TimeWastingOperationsRuleTest {

		/**
		 * Initialize global context with the proper data
		 *
		 * @return An instance of invocation data
		 */
		InvocationSequenceData initializeGlobalContext(double duration) {
			InvocationSequenceData invocationData = initializeGlobalContext();
			invocationData.setDuration(duration);

			return invocationData;
		}

		/**
		 * Initializes the globalContext with Sql Statement Data
		 *
		 * @return The invocation data which contains the global context
		 */
		InvocationSequenceData initializeGlobalContext() {
			InvocationSequenceData invocationData = new InvocationSequenceData();
			invocationData.setMethodIdent(1L);
			invocationData.setDuration(RANDOM.nextDouble() + 1000);

			initializeNestedSequences(invocationData);

			return invocationData;
		}

		/**
		 * Initialize the Sql Statement Data. With the exclusiveMin initialized.
		 *
		 * @return The initialized SqlStatementData.
		 */
		@SuppressWarnings("PMD")
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
		 * Initialize the nested sequences.
		 *
		 * @return A list of invocation data which contains the nested sequences
		 */
		void initializeNestedSequences(InvocationSequenceData invocationData) {
			invocationData.getNestedSequences().add(generateSequenceWithSqlStatementData(1, RANDOM.nextDouble()));
			invocationData.getNestedSequences().add(generateSequenceWithSqlStatementData(2, -1));
		}

		/**
		 * Generates a sequence with SQL Statement Data. And initializing it if necessary.
		 *
		 * @param id
		 *            If of the sequence in order to give different sequences
		 * @param exclusiveMin
		 * @return
		 */
		InvocationSequenceData generateSequenceWithSqlStatementData(int id, double exclusiveMin) {
			InvocationSequenceData sequenceData = new InvocationSequenceData(new Timestamp(new Date().getTime()), (id * 10), (id * 10), new Long(id));
			if (exclusiveMin != -1) {
				sequenceData.setSqlStatementData(initializeSqlStatementData(exclusiveMin));
			}
			return sequenceData;
		}

		/**
		 * Tests that the action method of the rule is not returning a null group of rules.
		 */
		@Test
		public void actionMethodMustReturnANotNullGroupOfRules() {
			timeWastingOperationsRule.globalContext = initializeGlobalContext();
			timeWastingOperations = timeWastingOperationsRule.action();
			assertNotNull(timeWastingOperations, "The returned list of rules must not be null");
		}

		/**
		 * Tests that the action method of the rule is not returning an empty group of rules.
		 */
		@Test
		public void actionMethodMustReturnANotEmptyGroupOfRulesWhenTheDurationIsTooHigh() {
			timeWastingOperationsRule.globalContext = initializeGlobalContext();
			timeWastingOperations = timeWastingOperationsRule.action();
			assertThat("Action method must return a list of rules not empty", timeWastingOperations, not(hasSize(0)));
		}

		@Test
		public void actionMethodMustReturnAEmptyGroupOfRulesWhenTheDurationIsTooLow() {
			timeWastingOperationsRule.globalContext = initializeGlobalContext(new Double(200));
			timeWastingOperations = timeWastingOperationsRule.action();
			assertThat("Action method must return an array of rules not empty", timeWastingOperations, not(hasSize(0)));
		}

		/**
		 * Tests that the action method of the rule is not returning the correct number of elements.
		 */
		@Test
		public void actionMethodMustReturnAGroupOfRulesWithOneElement() {
			timeWastingOperationsRule.globalContext = initializeGlobalContext();
			timeWastingOperations = timeWastingOperationsRule.action();
			assertThat("Action method must return a list of one rule", timeWastingOperations, hasSize(1));
		}

		/**
		 * Tests that the action method of the rule is not returning the correct number of elements.
		 */
		@Test
		public void actionMethodMustReturnAGroupOfRulesWithTwoElements() {
			timeWastingOperationsRule.globalContext = initializeGlobalContext();
			timeWastingOperationsRule.globalContext.getNestedSequences().add(generateSequenceWithSqlStatementData(3, RANDOM.nextDouble()));
			timeWastingOperations = timeWastingOperationsRule.action();
			assertThat("Action method must return a list with two rules", timeWastingOperations, hasSize(2));
		}

		/**
		 * Tests that the action method of the rule is not returning a valid group of rules.
		 */
		@Test
		public void actionMethodMustReturnAValidGroupOfRules() {
			timeWastingOperationsRule.globalContext = initializeGlobalContext();
			timeWastingOperations = timeWastingOperationsRule.action();
			assertThat("Action method must return a list of rules not empty", timeWastingOperations, not(hasSize(0)));
			for (AggregatedInvocationSequenceData aggregatedInvocationSequenceData : timeWastingOperations) {
				isAValidRule(aggregatedInvocationSequenceData);
			}
		}
	}

}
