package org.continuity.cli.commands;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;

import org.continuity.annotation.dsl.ContinuityModelElement;
import org.continuity.annotation.dsl.ann.SystemAnnotation;
import org.continuity.annotation.dsl.system.SystemModel;
import org.continuity.annotation.dsl.yaml.ContinuityYamlSerializer;
import org.continuity.cli.config.PropertiesProvider;
import org.continuity.commons.utils.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

/**
 * CLI for annotation handling.
 *
 * @author Henning Schulz
 *
 */
@ShellComponent
public class AnnotationCommands {

	@Autowired
	private PropertiesProvider propertiesProvider;

	@Autowired
	private RestTemplate restTemplate;

	@ShellMethod(key = { "download-annotation", "download-ann" }, value = "Downloads and opens the annotation with the specified tag.")
	public String downloadAnnotation(String tag) throws JsonGenerationException, JsonMappingException, IOException {
		String url = WebUtils.addProtocolIfMissing(propertiesProvider.get().getProperty(PropertiesProvider.KEY_URL));

		ResponseEntity<SystemModel> systemResponse = restTemplate.getForEntity(url + "/annotation/" + tag + "/system", SystemModel.class);
		ResponseEntity<SystemAnnotation> annotationResponse = restTemplate.getForEntity(url + "/annotation/" + tag + "/annotation", SystemAnnotation.class);

		if (!systemResponse.getStatusCode().is2xxSuccessful()) {
			return "Could not get system model: " + systemResponse;
		}

		if (!annotationResponse.getStatusCode().is2xxSuccessful()) {
			return "Could not get annotation: " + annotationResponse;
		}

		ContinuityYamlSerializer<ContinuityModelElement> serializer = new ContinuityYamlSerializer<>(ContinuityModelElement.class);
		String workingDir = propertiesProvider.get().getProperty(PropertiesProvider.KEY_WORKING_DIR);
		File systemFile = new File(workingDir + "/system-model-" + tag + ".yml");
		File annotationFile = new File(workingDir + "/annotation-" + tag + ".yml");
		serializer.writeToYaml(systemResponse.getBody(), systemFile);
		serializer.writeToYaml(annotationResponse.getBody(), annotationFile);

		Desktop.getDesktop().open(systemFile);
		Desktop.getDesktop().open(annotationFile);

		return "Downloaded and opened the system model and the annotation.";
	}

	@ShellMethod(key = { "open-annotation", "open-ann" }, value = "Opens an already downloaded annotation with the specified tag.")
	public String openAnnotation(String tag) throws IOException {
		String workingDir = propertiesProvider.get().getProperty(PropertiesProvider.KEY_WORKING_DIR);
		File systemFile = new File(workingDir + "/system-model-" + tag + ".yml");
		File annotationFile = new File(workingDir + "/annotation-" + tag + ".yml");

		Desktop.getDesktop().open(systemFile);
		Desktop.getDesktop().open(annotationFile);

		return "Opened the system model and the annotation with tag " + tag + " from " + workingDir;
	}

	@ShellMethod(key = { "upload-annotation", "upload-ann" }, value = "Uploads and the annotation with the specified tag.")
	public String uploadAnnotation(String tag) throws JsonParseException, JsonMappingException, IOException {
		String workingDir = propertiesProvider.get().getProperty(PropertiesProvider.KEY_WORKING_DIR);
		ContinuityYamlSerializer<SystemAnnotation> serializer = new ContinuityYamlSerializer<>(SystemAnnotation.class);
		SystemAnnotation annotation = serializer.readFromYaml(workingDir + "/annotation-" + tag + ".yml");

		String url = WebUtils.addProtocolIfMissing(propertiesProvider.get().getProperty(PropertiesProvider.KEY_URL));
		ResponseEntity<String> response = restTemplate.postForEntity(url + "/annotation/" + tag + "/annotation", annotation, String.class);

		if (!response.getStatusCode().is2xxSuccessful()) {
			return "Error during upload: " + response;
		} else {
			return "Successfully uploaded the annotation with tag " + tag + ".";
		}
	}

}
