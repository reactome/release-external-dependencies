package org.reactome.release.resourcechecker;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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

	JsonObject getReport();

	default String getResourceJsonWithReport() {
		JsonObject resourceJson = getResource().getResourceAsJsonObject().deepCopy();
		resourceJson.add("Report", getReport());

		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		return gson.toJson(resourceJson);
	}

	boolean resourcePassesAllChecks();

	default String getResourceName() {
		return getResource().getResourceName();
	};
}
