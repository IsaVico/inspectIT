package rocks.inspectit.server.diagnosis.service;

import java.util.Collection;

import org.apache.commons.math3.util.Pair;

import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;

/**
 * @author Alexander Wert
 *
 */
public interface IDiagnosisService {

	boolean diagnose(InvocationSequenceData invocation, double baseline);

	int diagnose(Collection<Pair<InvocationSequenceData, Double>> invocationBaselinePairs);
}
