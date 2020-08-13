package org.reactome.release;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import com.google.gson.JsonParser;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.csv.CSVRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Resource {
	private final Logger logger = LogManager.getLogger();
	private JsonObject resourceAsJson;

	public Resource(CSVRecord resourceInfoCSV) {
		this.resourceAsJson = convertCSVToJson(resourceInfoCSV);
		checkMandatoryAttributesExist(this.resourceAsJson.toString());
	}

	public Resource(JsonObject resourceInfoJson) {
		this.resourceAsJson = resourceInfoJson;
		checkMandatoryAttributesExist(this.resourceAsJson.toString());
	}

	private JsonObject convertCSVToJson(CSVRecord resourceInfoCSV) {
		String resourceInfoJson = convertCSVToJsonString(resourceInfoCSV);

		return new JsonParser().parse(resourceInfoJson).getAsJsonObject();
	}

	private String convertCSVToJsonString(CSVRecord resourceInfoCSV) {
		String resourceInfoJson = "";
		try {
			ObjectMapper mapper = new ObjectMapper();
			mapper.enable(SerializationFeature.INDENT_OUTPUT);

			resourceInfoJson = mapper.writeValueAsString(resourceInfoCSV.toMap());
		} catch (JsonProcessingException e) {
			logger.error("Unable to parse the CSV record's header to value map into JSON String", e);
		}

		return resourceInfoJson;
	}

	private void checkMandatoryAttributesExist(String originalRecord) throws IllegalArgumentException {
		Map<String, String> mandatoryAttributesMap = new HashMap<>();

		mandatoryAttributesMap.put("Release Step", getReleaseStep());
		mandatoryAttributesMap.put("Main Program", getMainProgramName());
		mandatoryAttributesMap.put("Dependency in Source Code", getSourceCodeDependency());
		mandatoryAttributesMap.put("Resource", getResourceName());
		mandatoryAttributesMap.put("Resource Description", getResourceDescription());
		mandatoryAttributesMap.put("Resource Type", getResourceType().toString());
		mandatoryAttributesMap.put("Resource URL", getResourceURL().toString());

		for (Entry<String, String> mandatoryAttributeEntry: mandatoryAttributesMap.entrySet()) {
			String mandatoryAttributeName = mandatoryAttributeEntry.getKey();
			String mandatoryAttributeValue = mandatoryAttributeEntry.getValue();

			if (mandatoryAttributeValue == null || mandatoryAttributeValue.isEmpty()) {
				throw new IllegalStateException(
					mandatoryAttributeName + " is empty or does not exist for " + originalRecord
				);
			}
		}
	}

	public String getReleaseStep() {
		return this.resourceAsJson.get("Release Step").getAsString();
	}

	public String getMainProgramName() {
		return this.resourceAsJson.get("Main Program").getAsString();
	}

	public String getSourceCodeDependency() {
		return this.resourceAsJson.get("Dependency in Source Code").getAsString();
	}

	public String getResourceName() {
		return this.resourceAsJson.get("Resource").getAsString();
	}

	public String getResourceDescription() {
		return this.resourceAsJson.get("Resource Description").getAsString();
	}

	public ResourceType getResourceType() {
		String resourceType = this.resourceAsJson.get("Resource Type").getAsString().toUpperCase().replace(" ", "_");
		return ResourceType.valueOf(resourceType);
	}

	public JsonObject getResourceAsJsonObject() {
		return this.resourceAsJson;
	}

//	public String getHeaderAsTSVString() {
//		return String.join("\t", getHeaderNameToValueMap().keySet());
//	}
//
//	public String getValuesAsTSVString() {
//		return String.join("\t", getHeaderNameToValueMap().values());
//	}

	public URL getResourceURL() {
		String resourceURL = this.resourceAsJson.get("Resource URL").getAsString();
		try {
			return new URL(resourceURL);
		} catch (MalformedURLException e) {
			String errorMessage = "Unable to create URL for " + resourceURL;

			logger.error(errorMessage, e);
			throw new RuntimeException(errorMessage, e);
		}
	}

	public String getErrorResponseText() {
		JsonElement errorResponseText = this.resourceAsJson.get("Error Response Text");
		return errorResponseText != null ? errorResponseText.getAsString() : "";
	}

	public String getExpectedResponseText() {
		JsonElement expectedResponseText = this.resourceAsJson.get("Expected Response Text");
		return expectedResponseText != null ? expectedResponseText.getAsString() : "";
	}

	public long getExpectedFileSizeInBytes() {
		final long fileSizeNotApplicable = -1;

		JsonElement expectedFileSize = this.resourceAsJson.get("Expected File Size");
		return expectedFileSize != null ? expectedFileSize.getAsLong() : fileSizeNotApplicable;
	}

	@Override
	public String toString() {
		return this.resourceAsJson.toString();
	}

	public enum ResourceType {
		FILE("File"),
		REST_ENDPOINT("REST EndPoint"),
		WEB_PAGE("Web Page"),
		WEB_SERVICE("Web Service"),
		FTP_SERVER("FTP Server");

		private final String resourceType;

		ResourceType(final String resourceType) {
			this.resourceType = resourceType;
		}

		@Override
		public String toString() {
			return this.resourceType;
		}
	}
}
