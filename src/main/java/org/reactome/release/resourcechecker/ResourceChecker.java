package org.reactome.release.resourcechecker;

import com.google.gson.JsonObject;
import java.net.URL;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.reactome.release.Resource;

public interface ResourceChecker {
	Logger logger = LogManager.getLogger();

	Resource getResource();

	default URL getResourceURL() {
		return getResource().getResourceURL();
	}

	boolean resourceExists();

	String getReport();

	default String getResourceJsonWithReport() {
		JsonObject resourceJson = getResource().getResourceAsJsonObject().deepCopy();
		resourceJson.addProperty("Report", getReport());
		return resourceJson.toString();
	}

	boolean resourcePassesAllChecks();
}
