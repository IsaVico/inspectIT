package rocks.inspectit.server.dao.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

import rocks.inspectit.server.test.AbstractTransactionalTestNGLogSupport;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;
import rocks.inspectit.shared.all.communication.data.TimerData;
import rocks.inspectit.shared.all.instrumentation.classcache.util.ArraySet;
import rocks.inspectit.shared.cs.communication.data.diagnosis.CauseStructure;
import rocks.inspectit.shared.cs.communication.data.diagnosis.CauseStructure.CauseType;
import rocks.inspectit.shared.cs.communication.data.diagnosis.ProblemOccurrence;
import rocks.inspectit.shared.cs.communication.data.diagnosis.ProblemOccurrenceData;
import rocks.inspectit.shared.cs.communication.data.diagnosis.RootCause;

/**
 * @author Isabel Vico Peinado
 *
 */
@ContextConfiguration(locations = { "classpath:spring/spring-context-global.xml", "classpath:spring/spring-context-database.xml", "classpath:spring/spring-context-beans.xml",
		"classpath:spring/spring-context-processors.xml", "classpath:spring/spring-context-storage-test.xml" })
@SuppressWarnings("PMD")
public class ProblemOccurrenceDaoTest extends AbstractTransactionalTestNGLogSupport {

	@Autowired
	ProblemOccurrenceDaoImpl problemOccurrenceDaoImpl;

	/**
	 * Tests that the saving and deleting the {@link ProblemOccurrenceDaoImpl} works.
	 *
	 * @throws IOException
	 */
	@Test
	public void saveProblemOccurrence() throws IOException {
		InvocationSequenceData requestRoot = new InvocationSequenceData(new Timestamp(new Date().getTime()), 108L, 10L, 20L);
		TimerData timerData = new TimerData(new Timestamp(System.currentTimeMillis()), 10L, 20L, 30L);
		requestRoot.setTimerData(timerData);
		InvocationSequenceData globalContext = new InvocationSequenceData();
		globalContext.setTimerData(timerData);
		InvocationSequenceData problemContext = new InvocationSequenceData();
		problemContext.setTimerData(timerData);
		List<InvocationSequenceData> invocationSequenceDataList = new ArrayList<InvocationSequenceData>();
		InvocationSequenceData childInvocation = new InvocationSequenceData(new Timestamp(new Date().getTime()), 108L, 10L, 20L);
		childInvocation.setTimerData(timerData);
		invocationSequenceDataList.add(childInvocation);
		InvocationSequenceData rootInvocation = new InvocationSequenceData(new Timestamp(new Date().getTime()), 108L, 10L, 20L);
		rootInvocation.setTimerData(timerData);
		RootCause rootCause = new RootCause(105L, rootInvocation, invocationSequenceDataList);
		CauseStructure causeStructure = new CauseStructure(CauseType.ITERATIVE, 3);
		ProblemOccurrence problemOccurrence = new ProblemOccurrence(requestRoot, globalContext, problemContext, rootCause, causeStructure);

		problemOccurrenceDaoImpl.saveOrUpdate(problemOccurrence);

		assertThat("The database must contains one element", problemOccurrenceDaoImpl.findAll(), hasSize(1));
	}

	/**
	 * Tests that the deleting the {@link ProblemOccurrenceDaoImpl} works.
	 *
	 * @throws IOException
	 */
	@Test
	public void deleteProblemOccurrence() throws IOException {
		InvocationSequenceData requestRoot = new InvocationSequenceData(new Timestamp(new Date().getTime()), 108L, 10L, 20L);
		TimerData timerData = new TimerData(new Timestamp(System.currentTimeMillis()), 10L, 20L, 30L);
		requestRoot.setTimerData(timerData);
		InvocationSequenceData globalContext = new InvocationSequenceData();
		globalContext.setTimerData(timerData);
		InvocationSequenceData problemContext = new InvocationSequenceData();
		problemContext.setTimerData(timerData);
		List<InvocationSequenceData> invocationSequenceDataList = new ArrayList<InvocationSequenceData>();
		InvocationSequenceData childInvocation = new InvocationSequenceData(new Timestamp(new Date().getTime()), 108L, 10L, 20L);
		childInvocation.setTimerData(timerData);
		invocationSequenceDataList.add(childInvocation);
		InvocationSequenceData rootInvocation = new InvocationSequenceData(new Timestamp(new Date().getTime()), 108L, 10L, 20L);
		rootInvocation.setTimerData(timerData);
		RootCause rootCause = new RootCause(105L, rootInvocation, invocationSequenceDataList);
		CauseStructure causeStructure = new CauseStructure(CauseType.ITERATIVE, 3);
		ProblemOccurrence problemOccurrence = new ProblemOccurrence(requestRoot, globalContext, problemContext, rootCause, causeStructure);

		problemOccurrenceDaoImpl.saveOrUpdate(problemOccurrence);
		// assertThat(problemOccurrenceDaoImpl.load(invocationId), is(notNullValue()));
		assertThat("Database must contains one element", problemOccurrenceDaoImpl.findAll(), hasSize(1));

		problemOccurrenceDaoImpl.delete(problemOccurrence);

		assertThat("The database must not contains any elements", problemOccurrenceDaoImpl.load(problemOccurrence.getRequestRoot().getInvocationId()), is(nullValue()));
		// assertThat(problemOccurrenceDaoImpl.findAll(), hasSize(0));
	}

	@Test
	public void saveAllMustSaveAllTheIntancesOfTheList() throws IOException {
		InvocationSequenceData requestRoot = new InvocationSequenceData(new Timestamp(new Date().getTime()), 108L, 10L, 20L);
		TimerData timerData = new TimerData(new Timestamp(System.currentTimeMillis()), 10L, 20L, 30L);
		requestRoot.setTimerData(timerData);
		InvocationSequenceData globalContext = new InvocationSequenceData();
		globalContext.setTimerData(timerData);
		InvocationSequenceData problemContext = new InvocationSequenceData();
		problemContext.setTimerData(timerData);
		List<InvocationSequenceData> invocationSequenceDataList = new ArrayList<InvocationSequenceData>();
		InvocationSequenceData childInvocation = new InvocationSequenceData(new Timestamp(new Date().getTime()), 108L, 10L, 20L);
		childInvocation.setTimerData(timerData);
		invocationSequenceDataList.add(childInvocation);
		InvocationSequenceData rootInvocation = new InvocationSequenceData(new Timestamp(new Date().getTime()), 108L, 10L, 20L);
		rootInvocation.setTimerData(timerData);
		RootCause rootCause = new RootCause(105L, rootInvocation, invocationSequenceDataList);
		CauseStructure causeStructure = new CauseStructure(CauseType.ITERATIVE, 3);
		ProblemOccurrence problemOccurrence = new ProblemOccurrence(requestRoot, globalContext, problemContext, rootCause, causeStructure);
		Set<ProblemOccurrence> problemOccurrences = new ArraySet<>();
		problemOccurrences.add(problemOccurrence);
		problemOccurrences.add(problemOccurrence);
		problemOccurrences.add(problemOccurrence);

		problemOccurrenceDaoImpl.saveOrUpdateAll(problemOccurrences);
		assertThat("The database must contains 3 elements", problemOccurrenceDaoImpl.findAll(), hasSize(3));
	}

	// @Test
	// public void findByAgentIdMustReturnTheCorrectElement() throws IOException {
	// InvocationSequenceData requestRoot = new InvocationSequenceData(new Timestamp(new
	// Date().getTime()), 108L, 10L, 20L);
	// TimerData timerData = new TimerData(new Timestamp(System.currentTimeMillis()), 10L, 20L,
	// 30L);
	// requestRoot.setTimerData(timerData);
	// InvocationSequenceData globalContext = new InvocationSequenceData();
	// globalContext.setTimerData(timerData);
	// InvocationSequenceData problemContext = new InvocationSequenceData();
	// problemContext.setTimerData(timerData);
	// List<InvocationSequenceData> invocationSequenceDataList = new
	// ArrayList<InvocationSequenceData>();
	// InvocationSequenceData childInvocation = new InvocationSequenceData(new Timestamp(new
	// Date().getTime()), 108L, 10L, 20L);
	// childInvocation.setTimerData(timerData);
	// invocationSequenceDataList.add(childInvocation);
	// InvocationSequenceData rootInvocation = new InvocationSequenceData(new Timestamp(new
	// Date().getTime()), 108L, 10L, 20L);
	// rootInvocation.setTimerData(timerData);
	// RootCause rootCause = new RootCause(105L, rootInvocation, invocationSequenceDataList);
	// CauseStructure causeStructure = new CauseStructure(CauseType.ITERATIVE, 3);
	// ProblemOccurrence problemOccurrence = new ProblemOccurrence(requestRoot, globalContext,
	// problemContext, rootCause, causeStructure);
	// long agentId = 1;
	//
	// List<ProblemOccurrenceData> foundProblemOccurrences =
	// problemOccurrenceDaoImpl.findByAgentId(agentId);
	//
	// assertThat("The id of the agent must be añdljfkadñlfkj",
	// foundProblemOccurrences.get(0).getProblemOccurrence().getAgentId(), hasSize(1));
	// }

	@Test
	public void findByAppNameMustReturnTheCorrectElement() throws IOException {
		InvocationSequenceData requestRoot = new InvocationSequenceData(new Timestamp(new Date().getTime()), 108L, 10L, 20L);
		TimerData timerData = new TimerData(new Timestamp(System.currentTimeMillis()), 10L, 20L, 30L);
		requestRoot.setTimerData(timerData);
		InvocationSequenceData globalContext = new InvocationSequenceData();
		globalContext.setTimerData(timerData);
		InvocationSequenceData problemContext = new InvocationSequenceData();
		problemContext.setTimerData(timerData);
		List<InvocationSequenceData> invocationSequenceDataList = new ArrayList<InvocationSequenceData>();
		InvocationSequenceData childInvocation = new InvocationSequenceData(new Timestamp(new Date().getTime()), 108L, 10L, 20L);
		childInvocation.setTimerData(timerData);
		invocationSequenceDataList.add(childInvocation);
		InvocationSequenceData rootInvocation = new InvocationSequenceData(new Timestamp(new Date().getTime()), 108L, 10L, 20L);
		rootInvocation.setTimerData(timerData);
		RootCause rootCause = new RootCause(105L, rootInvocation, invocationSequenceDataList);
		CauseStructure causeStructure = new CauseStructure(CauseType.ITERATIVE, 3);
		ProblemOccurrence problemOccurrence = new ProblemOccurrence(requestRoot, globalContext, problemContext, rootCause, causeStructure);
		String appName ="";

		List<ProblemOccurrenceData> foundProblemOccurrences = problemOccurrenceDaoImpl.findByAppName(appName);

		assertThat("The id of the agent must be añdljfkadñlfkj", foundProblemOccurrences.get(0).getProblemOccurrence().getApplicationNameIdent(), is(1));
	}

	@Test
	public void findByBusinessTransactionMustReturnTheCorrectElement() throws IOException {
		InvocationSequenceData requestRoot = new InvocationSequenceData(new Timestamp(new Date().getTime()), 108L, 10L, 20L);
		TimerData timerData = new TimerData(new Timestamp(System.currentTimeMillis()), 10L, 20L, 30L);
		requestRoot.setTimerData(timerData);
		InvocationSequenceData globalContext = new InvocationSequenceData();
		globalContext.setTimerData(timerData);
		InvocationSequenceData problemContext = new InvocationSequenceData();
		problemContext.setTimerData(timerData);
		List<InvocationSequenceData> invocationSequenceDataList = new ArrayList<InvocationSequenceData>();
		InvocationSequenceData childInvocation = new InvocationSequenceData(new Timestamp(new Date().getTime()), 108L, 10L, 20L);
		childInvocation.setTimerData(timerData);
		invocationSequenceDataList.add(childInvocation);
		InvocationSequenceData rootInvocation = new InvocationSequenceData(new Timestamp(new Date().getTime()), 108L, 10L, 20L);
		rootInvocation.setTimerData(timerData);
		RootCause rootCause = new RootCause(105L, rootInvocation, invocationSequenceDataList);
		CauseStructure causeStructure = new CauseStructure(CauseType.ITERATIVE, 3);
		ProblemOccurrence problemOccurrence = new ProblemOccurrence(requestRoot, globalContext, problemContext, rootCause, causeStructure);
		String businessTransaction = "";

		List<ProblemOccurrenceData> foundProblemOccurrences = problemOccurrenceDaoImpl.findByBusinessTransaction(businessTransaction);

		assertThat("The identifier of the businessTransaction must be añdljfkadñlfkj", foundProblemOccurrences.get(0).getProblemOccurrence().getBusinessTransactionNameIdent(), is(1));
	}

	@Test
	public void findByCauseTypeMustReturnTheCorrectElement() throws IOException {
		InvocationSequenceData requestRoot = new InvocationSequenceData(new Timestamp(new Date().getTime()), 108L, 10L, 20L);
		TimerData timerData = new TimerData(new Timestamp(System.currentTimeMillis()), 10L, 20L, 30L);
		requestRoot.setTimerData(timerData);
		InvocationSequenceData globalContext = new InvocationSequenceData();
		globalContext.setTimerData(timerData);
		InvocationSequenceData problemContext = new InvocationSequenceData();
		problemContext.setTimerData(timerData);
		List<InvocationSequenceData> invocationSequenceDataList = new ArrayList<InvocationSequenceData>();
		InvocationSequenceData childInvocation = new InvocationSequenceData(new Timestamp(new Date().getTime()), 108L, 10L, 20L);
		childInvocation.setTimerData(timerData);
		invocationSequenceDataList.add(childInvocation);
		InvocationSequenceData rootInvocation = new InvocationSequenceData(new Timestamp(new Date().getTime()), 108L, 10L, 20L);
		rootInvocation.setTimerData(timerData);
		RootCause rootCause = new RootCause(105L, rootInvocation, invocationSequenceDataList);
		CauseStructure causeStructure = new CauseStructure(CauseType.ITERATIVE, 3);
		ProblemOccurrence problemOccurrence = new ProblemOccurrence(requestRoot, globalContext, problemContext, rootCause, causeStructure);

		List<ProblemOccurrenceData> foundProblemOccurrences = problemOccurrenceDaoImpl.findByCauseType(CauseType.ITERATIVE.toString());

		assertThat("The cause structure must be añdljfkadñlfkj", foundProblemOccurrences.get(0).getProblemOccurrence().getCauseStructure().getCauseType(),
				is(CauseType.ITERATIVE));
	}

	@Test
	public void findByGlobalContextMustReturnTheCorrectElement() throws IOException {
		InvocationSequenceData requestRoot = new InvocationSequenceData(new Timestamp(new Date().getTime()), 108L, 10L, 20L);
		TimerData timerData = new TimerData(new Timestamp(System.currentTimeMillis()), 10L, 20L, 30L);
		requestRoot.setTimerData(timerData);
		InvocationSequenceData globalContext = new InvocationSequenceData();
		globalContext.setTimerData(timerData);
		InvocationSequenceData problemContext = new InvocationSequenceData();
		problemContext.setTimerData(timerData);
		List<InvocationSequenceData> invocationSequenceDataList = new ArrayList<InvocationSequenceData>();
		InvocationSequenceData childInvocation = new InvocationSequenceData(new Timestamp(new Date().getTime()), 108L, 10L, 20L);
		childInvocation.setTimerData(timerData);
		invocationSequenceDataList.add(childInvocation);
		InvocationSequenceData rootInvocation = new InvocationSequenceData(new Timestamp(new Date().getTime()), 108L, 10L, 20L);
		rootInvocation.setTimerData(timerData);
		RootCause rootCause = new RootCause(105L, rootInvocation, invocationSequenceDataList);
		CauseStructure causeStructure = new CauseStructure(CauseType.ITERATIVE, 3);
		ProblemOccurrence problemOccurrence = new ProblemOccurrence(requestRoot, globalContext, problemContext, rootCause, causeStructure);
		String globalContextString = "";

		List<ProblemOccurrenceData> foundProblemOccurrences = problemOccurrenceDaoImpl.findByGlobalContext(globalContextString);

		assertThat("The identifier of the globalContext must be añdljfkadñlfkj", foundProblemOccurrences.get(0).getProblemOccurrence().getGlobalContext().getMethodIdent(), is(1L));
	}

	@Test
	public void findByProblemContextMustReturnTheCorrectElement() throws IOException {
		InvocationSequenceData requestRoot = new InvocationSequenceData(new Timestamp(new Date().getTime()), 108L, 10L, 20L);
		TimerData timerData = new TimerData(new Timestamp(System.currentTimeMillis()), 10L, 20L, 30L);
		requestRoot.setTimerData(timerData);
		InvocationSequenceData globalContext = new InvocationSequenceData();
		globalContext.setTimerData(timerData);
		InvocationSequenceData problemContext = new InvocationSequenceData();
		problemContext.setTimerData(timerData);
		List<InvocationSequenceData> invocationSequenceDataList = new ArrayList<InvocationSequenceData>();
		InvocationSequenceData childInvocation = new InvocationSequenceData(new Timestamp(new Date().getTime()), 108L, 10L, 20L);
		childInvocation.setTimerData(timerData);
		invocationSequenceDataList.add(childInvocation);
		InvocationSequenceData rootInvocation = new InvocationSequenceData(new Timestamp(new Date().getTime()), 108L, 10L, 20L);
		rootInvocation.setTimerData(timerData);
		RootCause rootCause = new RootCause(105L, rootInvocation, invocationSequenceDataList);
		CauseStructure causeStructure = new CauseStructure(CauseType.ITERATIVE, 3);
		ProblemOccurrence problemOccurrence = new ProblemOccurrence(requestRoot, globalContext, problemContext, rootCause, causeStructure);
		String problemContextMethod = "";

		List<ProblemOccurrenceData> foundProblemOccurrences = problemOccurrenceDaoImpl.findByProblemContext(problemContextMethod);

		assertThat("The identifier of the globalContext must be añdljfkadñlfkj", foundProblemOccurrences.get(0).getProblemOccurrence().getProblemContext().getMethodIdent(), is(1L));
	}

	@Test
	public void findByRequestRootMustReturnTheCorrectElement() throws IOException {
		InvocationSequenceData requestRoot = new InvocationSequenceData(new Timestamp(new Date().getTime()), 108L, 10L, 20L);
		TimerData timerData = new TimerData(new Timestamp(System.currentTimeMillis()), 10L, 20L, 30L);
		requestRoot.setTimerData(timerData);
		InvocationSequenceData globalContext = new InvocationSequenceData();
		globalContext.setTimerData(timerData);
		InvocationSequenceData problemContext = new InvocationSequenceData();
		problemContext.setTimerData(timerData);
		List<InvocationSequenceData> invocationSequenceDataList = new ArrayList<InvocationSequenceData>();
		InvocationSequenceData childInvocation = new InvocationSequenceData(new Timestamp(new Date().getTime()), 108L, 10L, 20L);
		childInvocation.setTimerData(timerData);
		invocationSequenceDataList.add(childInvocation);
		InvocationSequenceData rootInvocation = new InvocationSequenceData(new Timestamp(new Date().getTime()), 108L, 10L, 20L);
		rootInvocation.setTimerData(timerData);
		RootCause rootCause = new RootCause(105L, rootInvocation, invocationSequenceDataList);
		CauseStructure causeStructure = new CauseStructure(CauseType.ITERATIVE, 3);
		ProblemOccurrence problemOccurrence = new ProblemOccurrence(requestRoot, globalContext, problemContext, rootCause, causeStructure);
		String requestRootMethod = "";

		List<ProblemOccurrenceData> foundProblemOccurrences = problemOccurrenceDaoImpl.findByProblemContext(requestRootMethod);

		assertThat("The identifier of the globalContext must be añdljfkadñlfkj", foundProblemOccurrences.get(0).getProblemOccurrence().getRequestRoot().getMethodIdent(), is(1L));
	}
}
