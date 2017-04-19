/**
 *
 */
package rocks.inspectit.server.diagnosis.service.results;

/**
 * @author Alexander Wert
 *
 */
public class InvocationIdentifier {
	private final long methodIdent;
	private final long invocationId;

	/**
	 * @param methodIdent
	 * @param invocationId
	 */
	public InvocationIdentifier(long methodIdent, long invocationId) {
		super();
		this.methodIdent = methodIdent;
		this.invocationId = invocationId;
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
	 * Gets {@link #invocationId}.
	 *
	 * @return {@link #invocationId}
	 */
	public long getInvocationId() {
		return invocationId;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (invocationId ^ (invocationId >>> 32));
		result = prime * result + (int) (methodIdent ^ (methodIdent >>> 32));
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
		InvocationIdentifier other = (InvocationIdentifier) obj;
		if (invocationId != other.invocationId) {
			return false;
		}
		if (methodIdent != other.methodIdent) {
			return false;
		}
		return true;
	}
}
