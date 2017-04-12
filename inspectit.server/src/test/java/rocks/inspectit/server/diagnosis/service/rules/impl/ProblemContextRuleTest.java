package rocks.inspectit.server.diagnosis.service.rules.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import rocks.inspectit.shared.all.communication.data.AggregatedInvocationSequenceData;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;
import rocks.inspectit.shared.all.communication.data.TimerData;
import rocks.inspectit.shared.all.testbase.TestBase;

public class ProblemContextRuleTest extends TestBase {

	@InjectMocks
	ProblemContextRule problemContextRule;

	@Mock
	InvocationSequenceData globalContext;

	@Mock
	AggregatedInvocationSequenceData timeWastingOperation;

	private static final int CHILD_OFFSET = 10;
	private static final int PARENT_OFFSET = 100;
	private static final long METHOD_IDENT = 108L;
	private static final Random RANDOM = new Random();
	private static final Timestamp DEF_DATE = new Timestamp(new Date().getTime());

	public class ActionMethod extends ProblemContextRuleTest {

		private InvocationSequenceData parentSequence = new InvocationSequenceData(DEF_DATE, PARENT_OFFSET, PARENT_OFFSET, METHOD_IDENT);
		private InvocationSequenceData childSequence = new InvocationSequenceData(DEF_DATE, 20, 20, METHOD_IDENT);
		private InvocationSequenceData significantContext = createNestedSequence(20, RANDOM.nextDouble(), RANDOM.nextDouble() + 10);
		private InvocationSequenceData problemContext;
		List<InvocationSequenceData> rawInvocations = new ArrayList<InvocationSequenceData>();


		@AfterMethod
		public void clear() {
			rawInvocations.clear();
		}

		/**
		 * Populates the raw invocations with child sequences.
		 */
		private void populateRawInvocationsWithChildSequence() {
			parentSequence.getNestedSequences().add(childSequence);
			rawInvocations.add(parentSequence);
			when(timeWastingOperation.getRawInvocationsSequenceElements()).thenReturn(rawInvocations);
		}

		/**
		 * Populates the raw invocations with a parent sequence.
		 */
		private void populateRawInvocationsWithParentSequence() {
			childSequence.setParentSequence(parentSequence);
			rawInvocations.add(childSequence);
			when(timeWastingOperation.getRawInvocationsSequenceElements()).thenReturn(rawInvocations);
		}

		/**
		 * Populates the raw invocations with 3 different sequences.
		 */
		private void populateRawInvocations() {
			rawInvocations.add(createNestedSequence(10, RANDOM.nextDouble(), RANDOM.nextDouble()));
			rawInvocations.add(significantContext);
			rawInvocations.add(createNestedSequence(30, -1, RANDOM.nextDouble()));
			when(timeWastingOperation.getRawInvocationsSequenceElements()).thenReturn(rawInvocations);
		}

		/**
		 * Creates a new sequence data with the data provided and nested sequence instantiated.
		 *
		 * @param id
		 *            Id of the sequence
		 * @param exclusiveMin
		 *            Exclusive minimum time for the Timer data
		 * @param exclusiveDuration
		 *            Exclusive duration for the Timer data
		 * @return Returns a sequence initialize with the data provided and with nested sequences
		 *         added.
		 */
		private InvocationSequenceData createNestedSequence(int id, double exclusiveMin, double exclusiveDuration) {
			InvocationSequenceData rawInvocation = new InvocationSequenceData(DEF_DATE, (id * PARENT_OFFSET), (id * PARENT_OFFSET), METHOD_IDENT);
			rawInvocation.setTimerData(initializeTimerData(RANDOM.nextDouble(), exclusiveDuration));
			rawInvocation.getNestedSequences().add(createSequenceWithParent(id, exclusiveMin, exclusiveDuration));
			return rawInvocation;
		}

		/**
		 * Creates a subsequence with timer data and set a parent sequence
		 *
		 * @param id
		 *            Id of the sequence
		 * @param exclusiveMin
		 *            Exclusive minimum time for the Timer data
		 * @param exclusiveDuration
		 *            Exclusive duration for the Timer data
		 * @return Returns a sequence initialize with the data provided and with a parent sequence
		 *         set.
		 */
		private InvocationSequenceData createSequenceWithParent(int id, double exclusiveMin, double exclusiveDuration) {
			InvocationSequenceData childSequence = new InvocationSequenceData(DEF_DATE, (id * CHILD_OFFSET), (id * CHILD_OFFSET), METHOD_IDENT);
			if (exclusiveMin == -1) {
				childSequence.setTimerData(initializeTimerData(exclusiveMin, exclusiveDuration));
			}
			childSequence.setParentSequence(new InvocationSequenceData(DEF_DATE, (id * CHILD_OFFSET), (id * CHILD_OFFSET), METHOD_IDENT));
			return childSequence;
		}

		/**
		 * Initialize the timer data. With the fields exclusiveMIn and exclusiveDuration
		 * initialized.
		 *
		 * @return The initialized timer data.
		 */
		@SuppressWarnings("PMD")
		TimerData initializeTimerData(double exclusiveMin, double exclusiveDuration) {
			Class<?> timerDataClass = null;
			TimerData timerData = new TimerData(new Timestamp(System.currentTimeMillis()), 10L, 20L, 30L);

			try {
				timerDataClass = Class.forName(timerData.getClass().getName());
				timerData = (TimerData) timerDataClass.newInstance();
				initField(timerData, timerDataClass, "exclusiveMin", exclusiveMin);
				initField(timerData, timerDataClass, "exclusiveDuration", exclusiveDuration);
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

		@Test
		public void actionMethodMustReturnTheSameInvocationIfItIsTheOnlyOneAndIsTheInvoker() {
			populateRawInvocationsWithChildSequence();
			problemContext = problemContextRule.action();
			assertThat("The returned problemContext must be the invoker", problemContext, is(equalTo(parentSequence)));
		}

		@Test
		public void actionMethodMustReturnTheProperInvocationIfThereOneAndIsTheInvokerWithAParentSequence() {
			populateRawInvocationsWithParentSequence();
			problemContext = problemContextRule.action();
			assertThat("The returned problemContext must be the invoker", problemContext, is(equalTo(childSequence.getParentSequence())));
		}

		@Test
		public void actionMethodMustReturnTheMostSignificantClusterContext() {
			populateRawInvocations();
			problemContext = problemContextRule.action();
			assertThat("The returned problemContext must be the most significant cluster context", problemContext, is(equalTo(significantContext)));
		}
	}

}
