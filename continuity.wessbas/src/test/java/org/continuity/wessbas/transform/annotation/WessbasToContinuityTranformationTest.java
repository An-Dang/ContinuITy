package org.continuity.wessbas.transform.annotation;

import java.io.IOException;

import org.continuity.annotation.dsl.ann.SystemAnnotation;
import org.continuity.annotation.dsl.system.HttpInterface;
import org.continuity.annotation.dsl.system.ServiceInterface;
import org.continuity.annotation.dsl.system.SystemModel;
import org.continuity.wessbas.entities.WessbasDslInstance;
import org.continuity.wessbas.transform.annotation.DslFromWessbasExtractor;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import m4jdsl.WorkloadModel;

/**
 * @author Henning Schulz
 *
 */
public class WessbasToContinuityTranformationTest {

	private static WorkloadModel createdWessbasModel;
	private static WorkloadModel parsedWessbasModel;

	@BeforeClass
	public static void loadWessbasModel() throws IOException {
		parsedWessbasModel = WessbasDslInstance.SPECJ_PARSED.get();
	}

	@BeforeClass
	public static void setupWessbasModel() {
		createdWessbasModel = WessbasDslInstance.SIMPLE.get();
	}

	@Test
	public void testCreatedModel() {
		DslFromWessbasExtractor extractor = new DslFromWessbasExtractor(createdWessbasModel, "test");
		SystemModel system = extractor.extractSystemModel();

		Assert.assertEquals("Expected the system to have 3 interfaces.", 3, system.getInterfaces().size());

		for (ServiceInterface<?> interf : system.getInterfaces()) {
			switch (interf.getId()) {
			case "Login":
				Assert.assertEquals("Wrong method for Login!", "GET", ((HttpInterface) interf).getMethod());
				break;
			case "Add2Cart":
				Assert.assertEquals("Wrong method for Add2Cart!", "POST", ((HttpInterface) interf).getMethod());
				break;
			case "Purchase":
				Assert.assertEquals("Wrong method for Purchase!", "GET", ((HttpInterface) interf).getMethod());
				break;
			default:
				Assert.fail("Found the unexpected interface " + interf.getId());
				break;
			}
		}

		SystemAnnotation annotation = extractor.extractInitialAnnotation();
		Assert.assertEquals("Extracted system and annotation should have the same number of interfaces!", system.getInterfaces().size(), annotation.getInterfaceAnnotations().size());
	}

	@Test
	public void testParsedModel() {
		DslFromWessbasExtractor extractor = new DslFromWessbasExtractor(parsedWessbasModel, "specj");
		SystemModel system = extractor.extractSystemModel();

		Assert.assertEquals("Expected the system to have 13 interfaces.", 13, system.getInterfaces().size());
	}

}
