package org.reactome.release;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;

import java.io.IOException;
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
	private Map<String, String> headerNameToValueMap;

	public Resource(CSVRecord csvRecord) {
		unpackRecord(csvRecord);
		checkMandatoryAttributesExist(csvRecord.toString());
	}

	public Resource(JsonObject jsonObject) {
		unpackRecord(jsonObject);
		checkMandatoryAttributesExist(jsonObject.toString());
	}

	private void unpackRecord(CSVRecord csvRecord) {
		this.headerNameToValueMap = csvRecord.toMap();
	}

	@SuppressWarnings("unchecked")
	private void unpackRecord(JsonObject jsonObject) {
		ObjectMapper mapper = new ObjectMapper();
		try {
			this.headerNameToValueMap = mapper.readValue(jsonObject.toString(), HashMap.class);
		} catch (IOException e) {
			logger.error("Unable to parse the JsonObject " + jsonObject + " as a HashMap", e);
		}
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
		return this.headerNameToValueMap.get("Release Step");
	}

	public String getMainProgramName() {
		return this.headerNameToValueMap.get("Main Program");
	}

	public String getSourceCodeDependency() {
		return this.headerNameToValueMap.get("Dependency in Source Code");
	}

	public String getResourceName() {
		return this.headerNameToValueMap.get("Resource");
	}

	public String getResourceDescription() {
		return this.headerNameToValueMap.get("Resource Description");
	}

	public ResourceType getResourceType() {
		String resourceType = this.headerNameToValueMap.get("Resource Type").toUpperCase().replace(" ", "_");
		return ResourceType.valueOf(resourceType);
	}

	public Map<String, String> getHeaderNameToValueMap() {
		return this.headerNameToValueMap;
	}

	public URL getResourceURL() {
		String resourceURL = this.headerNameToValueMap.get("Resource URL");
		try {
			return new URL(resourceURL);
		} catch (MalformedURLException e) {
			String errorMessage = "Unable to create URL for " + resourceURL;

			logger.error(errorMessage, e);
			throw new RuntimeException(errorMessage, e);
		}
	}

	@Override
	public String toString() {
		return this.headerNameToValueMap.toString();
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
