package rocks.inspectit.server.diagnosis.service.rules.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;
import rocks.inspectit.shared.all.testbase.TestBase;

public class GlobalContextRuleTest extends TestBase {

	@InjectMocks
	GlobalContextRule globalContextRule;

	@Mock
	InvocationSequenceData invocationSequenceRoot;

	private static final long BASELINE = 1000L;

	private static final long METHOD_IDENT = 108L;
	private static final Timestamp DEF_DATE = new Timestamp(new Date().getTime());

	public class ActionMethod extends GlobalContextRuleTest {

		private InvocationSequenceData currentGlobalContextRule;
		private InvocationSequenceData higherDurationChild;
		private List<InvocationSequenceData> nestedSequences = new ArrayList<>();

		@BeforeMethod
		private void init() {
			globalContextRule.baseline = BASELINE;
			when(invocationSequenceRoot.getDuration()).thenReturn(4700d);
		}

		@AfterMethod
		private void clear() {
			nestedSequences.clear();
		}

		/**
		 * Populates the sequence root with three sequences with the determined duration.
		 *
		 * @param maxDuration
		 *            Maximum duration for the sequence which is consider as the higher one.
		 */
		private void populateInvocationSequenceRoot(double duration) {
			populateSequenceData(10, 200d);
			populateChildWithMaxDuration(20, duration);
			populateSequenceData(30, 500d);
		}

		/**
		 * Populates a sequence data with the id and duration determined.
		 *
		 * @param id
		 *            Id for the platform and sensor type.
		 * @param duration
		 *            Duration of the sequence.
		 */
		private void populateSequenceData(int id, double duration) {
			InvocationSequenceData childSequence = new InvocationSequenceData(DEF_DATE, id, id, METHOD_IDENT);
			childSequence.setDuration(duration);
			nestedSequences.add(childSequence);
			when(invocationSequenceRoot.getNestedSequences()).thenReturn(nestedSequences);
		}

		/**
		 * Populates the child which have the maximum duration
		 *
		 * @param id
		 *            Id for the platform and sensor type.
		 * @param duration
		 *            Duration of the sequence.
		 */
		private void populateChildWithMaxDuration(int id, double duration) {
			higherDurationChild = new InvocationSequenceData(DEF_DATE, id, id, METHOD_IDENT);
			higherDurationChild.setDuration(duration);
			nestedSequences.add(higherDurationChild);
			when(invocationSequenceRoot.getNestedSequences()).thenReturn(nestedSequences);
		}

		@Test
		private void currentGlobalContextRuleMustNotBeNull() {
			currentGlobalContextRule = globalContextRule.action();
			assertNotNull("Invocation sequence root must not be null", currentGlobalContextRule);
		}

		@Test
		private void currentGlobalContextRuleMustBeTheSequenceWithMaximumDuration() {
			populateInvocationSequenceRoot(4000d);
			currentGlobalContextRule = globalContextRule.action();
			assertThat("The returned global context rule must be the child with higher duration", currentGlobalContextRule, is(equalTo(higherDurationChild)));
		}

		@Test
		private void currentGlobalContextRuleMustNotBeTheSequenceWithMaximumDuration() {
			populateInvocationSequenceRoot(3000d);
			currentGlobalContextRule = globalContextRule.action();
			assertThat("The returned global context rule must be the child with higher duration", currentGlobalContextRule, not(is(equalTo(higherDurationChild))));
		}
	}
}
