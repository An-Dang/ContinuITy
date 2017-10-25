package continuity.workload.dsl.annotation;

import java.io.IOException;

import org.continuity.workload.dsl.WeakReference;
import org.continuity.workload.dsl.annotation.CsvInput;
import org.continuity.workload.dsl.annotation.CustomDataInput;
import org.continuity.workload.dsl.annotation.DirectDataInput;
import org.continuity.workload.dsl.annotation.ExtractedInput;
import org.continuity.workload.dsl.annotation.Input;
import org.continuity.workload.dsl.annotation.InterfaceAnnotation;
import org.continuity.workload.dsl.annotation.ParameterAnnotation;
import org.continuity.workload.dsl.annotation.PropertyOverride;
import org.continuity.workload.dsl.annotation.PropertyOverrideKey;
import org.continuity.workload.dsl.annotation.RegExExtraction;
import org.continuity.workload.dsl.annotation.SystemAnnotation;
import org.continuity.workload.dsl.annotation.ext.AnnotationExtension;
import org.continuity.workload.dsl.annotation.ext.AnnotationExtensionElement;
import org.continuity.workload.dsl.system.HttpInterface;
import org.continuity.workload.dsl.system.HttpParameter;
import org.continuity.workload.dsl.system.HttpParameterType;
import org.continuity.workload.dsl.system.TargetSystem;
import org.continuity.workload.dsl.yaml.ContinuityYamlSerializer;

/**
 * @author Henning Schulz
 *
 */
public enum ContinuityModelTestInstance {

	SIMPLE {

		@Override
		protected TargetSystem setupSystemModel() {
			TargetSystem system = new TargetSystem();

			HttpInterface interf = new HttpInterface();
			interf.setDomain("mydomain");
			interf.setId("login");

			HttpParameter param = new HttpParameter();
			param.setId("user");
			param.setParameterType(HttpParameterType.REQ_PARAM);
			interf.getParameters().add(param);

			system.addInterface(interf);

			return system;
		}

		@Override
		protected SystemAnnotation setupAnnotation(TargetSystem system) {
			SystemAnnotation annotation = new SystemAnnotation();

			HttpInterface interf = (HttpInterface) system.getInterfaces().get(0);

			// Input

			DirectDataInput input = new DirectDataInput();
			input.setId("DAT1");
			input.getData().add("foo");
			input.getData().add("bar");

			CsvInput csvInput = new CsvInput();
			csvInput.setFilename("myfile.csv");
			csvInput.setColumn(3);
			csvInput.getAssociated().add(input);

			ExtractedInput extrInput = new ExtractedInput();
			RegExExtraction extr = new RegExExtraction();
			extr.setFrom(WeakReference.create(interf));
			extr.setPattern("(.*)");
			extrInput.getExtractions().add(extr);

			CustomDataInput unknownInput = new CustomDataInput();
			unknownInput.setId("UNK1");
			unknownInput.setType("MyCustomDataInput");

			// Annotation

			annotation = new SystemAnnotation();
			annotation.getInputs().add(input);
			annotation.getInputs().add(csvInput);
			annotation.getInputs().add(extrInput);
			annotation.getInputs().add(unknownInput);
			annotation.setId("ANN");

			InterfaceAnnotation interfaceAnn = new InterfaceAnnotation();
			interfaceAnn.setAnnotatedInterface(WeakReference.create(interf));
			PropertyOverride<PropertyOverrideKey.InterfaceLevel> ov = new PropertyOverride<>();
			ov.setKey(PropertyOverrideKey.HttpInterface.DOMAIN);
			ov.setValue("localhost");
			interfaceAnn.addOverride(ov);

			ParameterAnnotation paramAnn = new ParameterAnnotation();
			paramAnn.setAnnotatedParameter(WeakReference.create(interf.getParameters().get(0)));
			paramAnn.setInput(input);

			interfaceAnn.getParameterAnnotations().add(paramAnn);
			annotation.getInterfaceAnnotations().add(interfaceAnn);

			return annotation;
		}

		@Override
		protected AnnotationExtension setupAnnotationExtension(TargetSystem system, SystemAnnotation annotation) {
			AnnotationExtension extension = new AnnotationExtension();
			AnnotationExtensionElement ext = new AnnotationExtensionElement();
			extension.addElement(ext);

			Input unknownInput = annotation.getInputs().get(3);
			ext.setReference(WeakReference.create(unknownInput));
			ext.addExtension("driver", "freak-load-driver");
			ext.addExtension("foo", "bar");

			return extension;
		}

	},

	DVDSTORE_PARSED {
		@Override
		protected TargetSystem setupSystemModel() {
			ContinuityYamlSerializer<TargetSystem> serializer = new ContinuityYamlSerializer<>(TargetSystem.class);

			try {
				return serializer.readFromYaml(getClass().getResource("/dvdstore-systemmodel.yml"));
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
		}

		@Override
		protected SystemAnnotation setupAnnotation(TargetSystem system) {
			ContinuityYamlSerializer<SystemAnnotation> serializer = new ContinuityYamlSerializer<>(SystemAnnotation.class);
			try {
				return serializer.readFromYaml(getClass().getResource("/dvdstore-annotation.yml"));
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
		}

		@Override
		protected AnnotationExtension setupAnnotationExtension(TargetSystem system, SystemAnnotation annotation) {
			return null;
		}
	};

	private final TargetSystem systemModel;
	private final SystemAnnotation annotation;
	private final AnnotationExtension annotationExtension;

	/**
	 *
	 */
	private ContinuityModelTestInstance() {
		this.systemModel = setupSystemModel();
		this.annotation = setupAnnotation(systemModel);
		this.annotationExtension = setupAnnotationExtension(systemModel, annotation);
	}

	protected abstract TargetSystem setupSystemModel();

	protected abstract SystemAnnotation setupAnnotation(TargetSystem system);

	protected abstract AnnotationExtension setupAnnotationExtension(TargetSystem system, SystemAnnotation annotation);

	/**
	 * Gets {@link #systemModel}.
	 *
	 * @return {@link #systemModel}
	 */
	public TargetSystem getSystemModel() {
		return this.systemModel;
	}

	/**
	 * Gets {@link #annotation}.
	 *
	 * @return {@link #annotation}
	 */
	public SystemAnnotation getAnnotation() {
		return this.annotation;
	}

	/**
	 * Gets {@link #annotationExtension}.
	 *
	 * @return {@link #annotationExtension}
	 */
	public AnnotationExtension getAnnotationExtension() {
		return this.annotationExtension;
	}

}
