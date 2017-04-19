package rocks.inspectit.shared.all.communication.data;

import java.util.ArrayList;
import java.util.List;

import rocks.inspectit.shared.all.communication.IAggregatedData;

/**
 * Aggregated {@link InvocationSequenceData} object.
 *
 * @author Alexander Wert
 *
 */
public class AggregatedInvocationSequenceData extends InvocationSequenceData implements IAggregatedData<InvocationSequenceData> {
	/**
	 * Generated UID.
	 */
	private static final long serialVersionUID = 3389275017405936921L;
	/**
	 * List of {@link InvocationSequenceData} instances that are part of the aggregation.
	 */
	private final List<InvocationSequenceData> rawInvocationsSequenceElements = new ArrayList<InvocationSequenceData>(1);

	/**
	 * {@inheritDoc}
	 */
	public void aggregate(InvocationSequenceData data) {
		TimerData timerData;
		if (InvocationSequenceDataHelper.hasTimerData(this)) {
			timerData = getTimerData();
		} else if (InvocationSequenceDataHelper.hasSQLData(this)) {
			timerData = getSqlStatementData();
		} else {
			throw new IllegalArgumentException("No timer data available!");
		}
		setDuration(timerData.getDuration());

		rawInvocationsSequenceElements.add(data);
	}

	/**
	 * Gets {@link #rawInvocationsSequenceElements}.
	 *
	 * @return {@link #rawInvocationsSequenceElements}
	 */
	public List<InvocationSequenceData> getRawInvocationsSequenceElements() {
		return rawInvocationsSequenceElements;
	}

	/**
	 * {@inheritDoc}
	 */
	public InvocationSequenceData getData() {
		return this;
	}

	/**
	 * Returns the number of {@link InvocationSequenceData} instances that are part of the
	 * aggregation.
	 *
	 * @return Returns the number of {@link InvocationSequenceData} instances that are part of the
	 *         aggregation.
	 */
	public int size() {
		return rawInvocationsSequenceElements.size();
	}
}
