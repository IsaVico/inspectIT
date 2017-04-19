/**
 *
 */
package rocks.inspectit.server.diagnosis.service.rules;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import rocks.inspectit.shared.all.cmr.model.MethodIdent;
import rocks.inspectit.shared.all.cmr.service.ICachedDataService;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;

/**
 * @author Alexander Wert
 *
 */
@Component
public class UTIL {
	public static UTIL instance;

	@Autowired
	ICachedDataService cachedDataService;

	/**
	 *
	 */
	public UTIL() {
		instance = this;
	}

	public String invocToStr(InvocationSequenceData isd) {
		MethodIdent mIdent = cachedDataService.getMethodIdentForId(isd.getMethodIdent());
		return mIdent.getFQN() + "." + mIdent.getMethodName();
	}
}
