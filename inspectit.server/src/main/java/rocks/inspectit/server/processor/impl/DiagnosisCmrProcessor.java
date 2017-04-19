/**
 *
 */
package rocks.inspectit.server.processor.impl;

import javax.persistence.EntityManager;

import org.springframework.beans.factory.annotation.Autowired;

import rocks.inspectit.server.diagnosis.service.IDiagnosisService;
import rocks.inspectit.server.processor.AbstractCmrDataProcessor;
import rocks.inspectit.shared.all.communication.DefaultData;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;

/**
 * @author Claudio Waldvogel
 *
 */
public class DiagnosisCmrProcessor extends AbstractCmrDataProcessor {

	@Autowired
	private IDiagnosisService diagnosisService;

	private final double baseline = 1000.0;

	public DiagnosisCmrProcessor() {

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void processData(DefaultData defaultData, EntityManager entityManager) {
		diagnosisService.diagnose((InvocationSequenceData) defaultData, baseline);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean canBeProcessed(DefaultData defaultData) {
		return (defaultData instanceof InvocationSequenceData) && (((InvocationSequenceData) defaultData).getDuration() > baseline);
	}

}
