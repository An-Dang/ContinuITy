package org.continuity.commons.workload.dsl;

import org.continuity.annotation.dsl.ann.SystemAnnotation;
import org.continuity.annotation.dsl.system.SystemModel;
import org.continuity.annotation.dsl.visitor.ContinuityModelVisitor;

/**
 * Utility calss for extracting initial annotations from system models.
 *
 * @author Henning Schulz
 *
 */
public class AnnotationExtractor {

	/**
	 * Extracts the annotations from the specified system model.
	 *
	 * @param system
	 *            The system model.
	 * @return The extracted annotations.
	 */
	public SystemAnnotation extractAnnotation(SystemModel system) {
		SystemToAnnotationTransformer transformer = new SystemToAnnotationTransformer();
		ContinuityModelVisitor visitor = new ContinuityModelVisitor(transformer::onModelElementVisited);
		visitor.visit(system);
		return transformer.getExtractedAnnotation();
	}

}
