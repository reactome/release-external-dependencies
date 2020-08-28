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
		return getAsStringOrEmptyStringIfNull(this.resourceAsJson.get("Release Step"));
	}

	public String getMainProgramName() {
		return getAsStringOrEmptyStringIfNull(this.resourceAsJson.get("Main Program"));
	}

	public String getSourceCodeDependency() {
		return getAsStringOrEmptyStringIfNull(this.resourceAsJson.get("Dependency in Source Code"));
	}

	public String getResourceName() {
		return getAsStringOrEmptyStringIfNull(this.resourceAsJson.get("Resource"));
	}

	public String getResourceDescription() {
		return getAsStringOrEmptyStringIfNull(this.resourceAsJson.get("Resource Description"));
	}

	public ResourceType getResourceType() {
		String resourceType =
			getAsStringOrEmptyStringIfNull(this.resourceAsJson.get("Resource Type"))
			.toUpperCase()
			.replace(" ", "_");

		return ResourceType.valueOf(resourceType);
	}

	public JsonObject getResourceAsJsonObject() {
		return this.resourceAsJson;
	}

	public URL getResourceURL() {
		String resourceURL = getAsStringOrEmptyStringIfNull(this.resourceAsJson.get("Resource URL"));
		try {
			return new URL(resourceURL);
		} catch (MalformedURLException e) {
			String errorMessage = "Unable to create URL for '" + resourceURL + "'";

			logger.error(errorMessage, e);
			throw new RuntimeException(errorMessage, e);
		}
	}

	public String getErrorResponseText() {
		return getAsStringOrEmptyStringIfNull(this.resourceAsJson.get("Error Response Text"));
	}

	public String getExpectedResponseText() {
		return getAsStringOrEmptyStringIfNull(this.resourceAsJson.get("Expected Response Text"));
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

	private String getAsStringOrEmptyStringIfNull(JsonElement jsonElement) {
		return jsonElement != null ? jsonElement.getAsString() : "";
	}
}
