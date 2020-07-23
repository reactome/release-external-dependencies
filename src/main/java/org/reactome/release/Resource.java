package org.reactome.release;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import org.apache.commons.csv.CSVRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Resource {
	private final Logger logger = LogManager.getLogger();
	private CSVRecord csvRecord;
	private Map<String, String> csvHeaderNameToValueMap;

	public Resource(CSVRecord csvRecord) {
		this.csvRecord = csvRecord;
		unpackCSVRecord(csvRecord);
	}

	private void unpackCSVRecord(CSVRecord csvRecord) {
		this.csvHeaderNameToValueMap = csvRecord.toMap();
	}

	public String getReleaseStep() {
		return this.csvHeaderNameToValueMap.get("Release Step");
	}

	public String getMainProgramName() {
		return this.csvHeaderNameToValueMap.get("Main Program");
	}

	public String getSourceCodeDependency() {
		return this.csvHeaderNameToValueMap.get("Dependency in Source Code");
	}

	public String getResourceName() {
		return this.csvHeaderNameToValueMap.get("Resource");
	}

	public String getResourceDescription() {
		return this.csvHeaderNameToValueMap.get("Resource Description");
	}

	public ResourceType getResourceType() {
		String resourceType = this.csvHeaderNameToValueMap.get("Resource Type").toUpperCase().replace(" ", "_");
		return ResourceType.valueOf(resourceType);
	}

	public Map<String, String> getCsvHeaderNameToValueMap() {
		return this.csvHeaderNameToValueMap;
	}

	public URL getResourceURL() {
		String resourceURL = this.csvHeaderNameToValueMap.get("Resource URL");
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
		return this.csvRecord.toString();
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
