package rocks.inspectit.server.diagnosis.engine.session;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import rocks.inspectit.server.diagnosis.engine.rule.annotation.Action;
import rocks.inspectit.server.diagnosis.engine.rule.annotation.Condition;
import rocks.inspectit.server.diagnosis.engine.rule.annotation.Rule;
import rocks.inspectit.server.diagnosis.engine.rule.annotation.TagValue;
import rocks.inspectit.server.diagnosis.engine.rule.factory.Rules;
import rocks.inspectit.server.diagnosis.engine.session.exception.SessionException;
import rocks.inspectit.server.diagnosis.engine.tag.Tags;
import rocks.inspectit.shared.all.testbase.TestBase;

/**
 * @author Claudio Waldvogel
 */
@SuppressWarnings("all")
public class SessionTest extends TestBase {

	Session<String, DefaultSessionResult<String>> session;

	@BeforeMethod
	public void setupSession() {
		session = Session.<String, DefaultSessionResult<String>> builder().setNumRuleWorkers(1).setSessionResultCollector(new DefaultSessionResultCollector<String>())
				.setRuleDefinitions(Rules.define(R1.class, R2.class, R3.class)).build();
	}

	public static class Activate extends SessionTest {

		@Test
		public void shouldActivateFromNew() {
			session.activate("Input");
			assertThat(session.getState(), is(Session.State.ACTIVATED));
		}

		@Test
		public void shouldReActivateFromPassivate() {
			session.activate("Input");
			assertThat(session.getState(), is(Session.State.ACTIVATED));
			session.passivate();
			assertThat(session.getState(), is(Session.State.PASSIVATED));
			session.activate("String");
			assertThat(session.getState(), is(Session.State.ACTIVATED));
		}

		@Test(expectedExceptions = SessionException.class)
		public void shouldNotActivateBecauseActivated() {
			session.activate("Input");
			// should fail because session is already activated
			session.activate("String");
		}

	}

	@Rule
	public static class R1 {

		@TagValue(type = Tags.ROOT_TAG, injectionStrategy = TagValue.InjectionStrategy.BY_VALUE)
		private String input;

		@Action(resultTag = "Tag1")
		public String action() {
			return input + "Enhanced";
		}

	}

	@Rule
	public static class R2 {

		@TagValue(type = "Tag1", injectionStrategy = TagValue.InjectionStrategy.BY_VALUE)
		private String input;

		@Action(resultTag = "Tag2", resultQuantity = Action.Quantity.MULTIPLE)
		public String[] action() {
			return new String[] { input + "AgainEnhanced", input + "AgainEnhanced2" };
		}

	}

	@Rule(name = "FailingRule")
	public static class R3 {

		@TagValue(type = "Tag2", injectionStrategy = TagValue.InjectionStrategy.BY_VALUE)
		private String input;

		@Condition(name = "shouldFail", hint = "Also no problem")
		public boolean fail() {
			return false;
		}

		@Condition(name = "shouldAlsoFail", hint = "No problem")
		public boolean fail2() {
			return false;
		}

		@Action(resultTag = "Tag3")
		public String action() {
			return "Never executed!";
		}
	}

}
