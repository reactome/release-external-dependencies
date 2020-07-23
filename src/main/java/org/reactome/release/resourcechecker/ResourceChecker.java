package org.reactome.release.resourcechecker;

import java.io.IOException;
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

	String getReport() throws IOException;
}
