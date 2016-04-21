package rocks.inspectit.server.diagnosis.results;

import java.util.HashSet;
import java.util.Set;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import rocks.inspectit.shared.all.communication.data.diagnosis.results.ProblemOccurrence;

/**
 *
 * Stores diagnosis results as a set of {@link #ProblemOccurrence} objects. This class will later be
 * replaced by a solution where the {@link #ProblemOccurrence} are persistently stored.
 *
 * @author Christian Voegele
 *
 */
@Component
@Scope(value = "singleton")
public class DiagnosisResults implements IDiagnosisResults<ProblemOccurrence> {

	/**
	 * Stores the resulting ProblemOccurrences.
	 */
	Set<ProblemOccurrence> resultingSet = new HashSet<ProblemOccurrence>(1000);

	/**
	 * Gets {@link #resultingSet}.
	 *
	 * @return {@link #resultingSet}
	 */
	@Override
	public final Set<ProblemOccurrence> getDiagnosisResults() {
		return this.resultingSet;
	}

}