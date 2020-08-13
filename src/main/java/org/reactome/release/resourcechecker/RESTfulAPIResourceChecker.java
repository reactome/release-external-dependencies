package org.reactome.release.resourcechecker;

import java.io.IOException;

import java.util.HashMap;
import java.util.Map;
import org.reactome.release.Resource;

public class RESTfulAPIResourceChecker implements HTTPResourceChecker {
	private Resource resource;

	public RESTfulAPIResourceChecker(Resource resource) {
		this.resource = resource;
	}

	@Override
	public Resource getResource() {
		return this.resource;
	}

	public String getContentsAsJSON() {
		return getContents("json");
	}

	public String getContentsAsXML() {
		return getContents("xml");
	}

	private String getContents(String contentFormat)  {
		String requestMethod = "GET";
		Map<String, String> requestProperties = new HashMap<>();
		requestProperties.put("Content-Type", "application/" + contentFormat);

		try {

			return getAllContent(getHttpURLConnection(requestMethod, requestProperties));
		} catch (IOException e) {
			logger.error("Unable to make an HTTP URL Connection to ", e);
			return "";
		}
	}
}
