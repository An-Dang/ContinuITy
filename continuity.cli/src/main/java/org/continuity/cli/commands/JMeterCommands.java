package org.continuity.cli.commands;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.continuity.api.entities.artifact.JMeterTestPlanBundle;
import org.continuity.api.entities.config.LoadTestType;
import org.continuity.api.entities.report.OrderReport;
import org.continuity.api.rest.RestApi.Orchestrator.Loadtest;
import org.continuity.cli.config.PropertiesProvider;
import org.continuity.cli.process.JMeterProcess;
import org.continuity.cli.storage.OrderStorage;
import org.continuity.commons.jmeter.JMeterPropertiesCorrector;
import org.continuity.commons.jmeter.TestPlanWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import org.springframework.web.client.RestTemplate;

/**
 * @author Henning Schulz
 *
 */
@ShellComponent
public class JMeterCommands {

	private static final String KEY_JMETER_HOME = "jmeter.home";

	private static final String KEY_JMETER_CONFIG = "jmeter.configuration";

	private static final String DEFAULT_LINK = "DEFAULT";

	@Autowired
	private PropertiesProvider propertiesProvider;

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private OrderStorage storage;

	private TestPlanWriter testPlanWriter;

	private JMeterPropertiesCorrector propertiesCorrector = new JMeterPropertiesCorrector();

	@ShellMethod(key = { "jmeter-home" }, value = "Sets the home directory of JMeter (where the bin directory is placed).")
	public String setJMeterHome(String jmeterHome) {
		jmeterHome = jmeterHome.replace("\\", "/");
		Object old = propertiesProvider.get().put(KEY_JMETER_HOME, jmeterHome);
		return old == null ? "Set JMeter home." : "Replaced old JMeter home: " + old;
	}

	@ShellMethod(key = { "jmeter-config" }, value = "Sets the configuration directory of JMeter.")
	public String setJMeterConfig(String jmeterConfig) {
		jmeterConfig = jmeterConfig.replace("\\", "/");
		Object old = propertiesProvider.get().put(KEY_JMETER_CONFIG, jmeterConfig);
		testPlanWriter = new TestPlanWriter(jmeterConfig);
		return old == null ? "Set JMeter config dir." : "Replaced old JMeter config dir: " + old;
	}

	@ShellMethod(key = { "jmeter-download" }, value = "Downloads and opens a JMeter load test specified by a link.")
	public String downloadLoadTest(@ShellOption(defaultValue = DEFAULT_LINK) String loadTestLink) throws IOException {
		String jmeterHome = propertiesProvider.get().getProperty(KEY_JMETER_HOME);

		if (testPlanWriter == null) {
			String jmeterConfig = propertiesProvider.get().getProperty(KEY_JMETER_CONFIG);

			if (jmeterConfig == null) {
				return "Please set the jmeter config path first (call 'jmeter-config [path]')";
			} else {
				testPlanWriter = new TestPlanWriter(jmeterConfig);
			}
		} else if (jmeterHome == null) {
			return "Please set the jmeter home path first (call 'jmeter-home [path]')";
		}

		if (DEFAULT_LINK.equals(loadTestLink)) {
			OrderReport report = storage.getReport(OrderStorage.ID_LATEST);

			if ((report == null) || (report.getCreatedArtifacts() == null) || (report.getCreatedArtifacts().getLoadTestLinks().getLink() == null)) {
				return "Cannot download the JMeter test of the latest order. The link is missing!";
			} else if (report.getCreatedArtifacts().getLoadTestLinks().getType() != LoadTestType.JMETER) {
				return "Cannot download the JMeter test of the latest order. The link points to a " + report.getCreatedArtifacts().getLoadTestLinks().getType().toPrettyString() + " test!";
			} else {
				loadTestLink = report.getCreatedArtifacts().getLoadTestLinks().getLink();
			}
		}

		ResponseEntity<JMeterTestPlanBundle> response = restTemplate.getForEntity(loadTestLink, JMeterTestPlanBundle.class);

		if (!response.getStatusCode().is2xxSuccessful()) {
			return response.toString();
		}

		List<String> params = Loadtest.GET.parsePathParameters(loadTestLink);
		Path testPlanDir = Paths.get(propertiesProvider.get().getProperty(PropertiesProvider.KEY_WORKING_DIR), params.get(1));
		testPlanDir.toFile().mkdirs();

		JMeterTestPlanBundle testPlanBundle = response.getBody();
		propertiesCorrector.correctPaths(testPlanBundle.getTestPlan(), testPlanDir.toAbsolutePath());
		propertiesCorrector.configureResultFile(testPlanBundle.getTestPlan(), testPlanDir.resolve("results.csv").toAbsolutePath());
		propertiesCorrector.prepareForHeadlessExecution(testPlanBundle.getTestPlan());
		Path testPlanPath = testPlanWriter.write(testPlanBundle.getTestPlan(), testPlanBundle.getBehaviors(), testPlanDir);
		new JMeterProcess(jmeterHome).run(testPlanPath);

		return "Stored and opened JMeter test plan at " + testPlanPath;
	}

}
