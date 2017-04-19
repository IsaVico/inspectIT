/**
 *
 */
package rocks.inspectit.server.diagnosis.service.results;

import java.util.ArrayList;
import java.util.List;

import rocks.inspectit.shared.all.communication.data.TimerData;

/**
 * @author Alexander Wert
 *
 */
public class RootCause {
	private final long methodIdent;
	private final TimerData timerData;
	private final List<Long> invocationIds = new ArrayList<>();

	/**
	 * @param methodIdent
	 * @param timerData
	 * @param invocationIds
	 */
	public RootCause(long methodIdent, TimerData timerData) {
		this.methodIdent = methodIdent;
		this.timerData = timerData;
	}

	/**
	 * Gets {@link #methodIdent}.
	 *
	 * @return {@link #methodIdent}
	 */
	public long getMethodIdent() {
		return methodIdent;
	}

	/**
	 * Gets {@link #timerData}.
	 *
	 * @return {@link #timerData}
	 */
	public TimerData getTimerData() {
		return timerData;
	}

	/**
	 * Gets {@link #invocationIds}.
	 *
	 * @return {@link #invocationIds}
	 */
	public List<Long> getInvocationIds() {
		return invocationIds;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((invocationIds == null) ? 0 : invocationIds.hashCode());
		result = prime * result + (int) (methodIdent ^ (methodIdent >>> 32));
		result = prime * result + ((timerData == null) ? 0 : timerData.hashCode());
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		RootCause other = (RootCause) obj;
		if (invocationIds == null) {
			if (other.invocationIds != null) {
				return false;
			}
		} else if (!invocationIds.equals(other.invocationIds)) {
			return false;
		}
		if (methodIdent != other.methodIdent) {
			return false;
		}
		if (timerData == null) {
			if (other.timerData != null) {
				return false;
			}
		} else if (!timerData.equals(other.timerData)) {
			return false;
		}
		return true;
	}

}
