package org.continuity.workload.dsl.visitor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.continuity.workload.dsl.ContinuityModelElement;
import org.continuity.workload.dsl.annotation.DataInput;
import org.continuity.workload.dsl.annotation.ExtractedInput;
import org.continuity.workload.dsl.annotation.InterfaceAnnotation;
import org.continuity.workload.dsl.annotation.ParameterAnnotation;
import org.continuity.workload.dsl.annotation.RegExExtraction;
import org.continuity.workload.dsl.annotation.SystemAnnotation;
import org.continuity.workload.dsl.annotation.ext.AnnotationExtension;
import org.continuity.workload.dsl.system.Parameter;
import org.continuity.workload.dsl.system.ServiceInterface;
import org.continuity.workload.dsl.system.TargetSystem;

/**
 * Utility for extracting the nested elements of {@link ContinuityModelElement}s.
 *
 * @author Henning Schulz
 *
 */
public enum NestedElementExtractor {

	TARGET_SYSTEM(TargetSystem.class) {
		@Override
		protected Collection<ContinuityModelElement> extractNestedElements(ContinuityModelElement element) {
			TargetSystem system = (TargetSystem) element;
			return new ArrayList<>(system.getInterfaces());
		}
	},

	INTERFACE(ServiceInterface.class) {
		@Override
		protected Collection<ContinuityModelElement> extractNestedElements(ContinuityModelElement element) {
			ServiceInterface<?> interf = (ServiceInterface<?>) element;
			return new ArrayList<>(interf.getParameters());
		}
	},

	/**
	 * For {@link SystemAnnotation}.
	 */
	SYSTEM_ANNOTATION(SystemAnnotation.class) {
		@Override
		protected Collection<ContinuityModelElement> extractNestedElements(ContinuityModelElement element) {
			SystemAnnotation annotation = (SystemAnnotation) element;
			List<ContinuityModelElement> join = new ArrayList<>(annotation.getInputs().size() + annotation.getInterfaceAnnotations().size());
			join.addAll(annotation.getInputs());
			join.addAll(annotation.getInterfaceAnnotations());
			return join;
		}
	},

	/**
	 * For {@link InterfaceAnnotation}.
	 */
	INTERFACE_ANNOTATION(InterfaceAnnotation.class) {
		@Override
		protected Collection<ContinuityModelElement> extractNestedElements(ContinuityModelElement element) {
			InterfaceAnnotation annotation = (InterfaceAnnotation) element;
			return new ArrayList<>(annotation.getParameterAnnotations());
		}
	},

	/**
	 * For {@link ExtractedInput}.
	 */
	EXTRACTED_INPUT(ExtractedInput.class) {
		@Override
		protected Collection<ContinuityModelElement> extractNestedElements(ContinuityModelElement element) {
			ExtractedInput input = (ExtractedInput) element;
			return new ArrayList<>(input.getExtractions());
		}
	},

	/**
	 * For all other elements that do not have nested elements.
	 */
	EMPTY(Parameter.class, ParameterAnnotation.class, DataInput.class, RegExExtraction.class) {
		@Override
		protected Collection<ContinuityModelElement> extractNestedElements(ContinuityModelElement element) {
			return Collections.emptyList();
		}
	},

	ANNOTATION_EXTENSION(AnnotationExtension.class) {

		@Override
		protected Collection<ContinuityModelElement> extractNestedElements(ContinuityModelElement element) {
			AnnotationExtension extension = (AnnotationExtension) element;
			return new ArrayList<>(extension.getElements().values());
		}

	};

	private static final Map<Class<? extends ContinuityModelElement>, NestedElementExtractor> extractorPerType;

	static {
		extractorPerType = new HashMap<>();

		for (NestedElementExtractor extractor : values()) {
			for (Class<? extends ContinuityModelElement> type : extractor.types) {
				extractorPerType.put(type, extractor);
			}
		}
	}

	private final Class<? extends ContinuityModelElement>[] types;

	@SafeVarargs
	private NestedElementExtractor(Class<? extends ContinuityModelElement>... types) {
		this.types = types;
	}

	/**
	 * Returns all nested elements of the passed one. Will throw an {@link IllegalArgumentException}
	 * if the class of the passed element does not match.
	 *
	 * @param element
	 *            The element whose nested elements should be returned.
	 * @return The nested elements of {@code element}.
	 */
	public Collection<ContinuityModelElement> getNestedElements(ContinuityModelElement element) {
		boolean assignable = false;
		for (Class<? extends ContinuityModelElement> type : types) {
			if (type.isAssignableFrom(element.getClass())) {
				assignable = true;
				break;
			}
		}

		if (!assignable) {
			throw new IllegalArgumentException(toString() + " cannot process " + element.getClass());
		}

		return extractNestedElements(element);
	}

	protected abstract Collection<ContinuityModelElement> extractNestedElements(ContinuityModelElement element);

	/**
	 * Returns the extractor for the passed type.
	 *
	 * @param type
	 *            The class of the {@link ContinuityModelElement} to be processed.
	 * @return A NestedElementExtractor that is able to process the passed type.
	 */
	public static NestedElementExtractor forType(Class<? extends ContinuityModelElement> type) {
		NestedElementExtractor extractor = extractorPerType.get(type);

		if (extractor == null) {
			for (Entry<Class<? extends ContinuityModelElement>, NestedElementExtractor> entry : extractorPerType.entrySet()) {
				if (entry.getKey().isAssignableFrom(type)) {
					return entry.getValue();
				}
			}
		}

		return extractor;
	}

}
