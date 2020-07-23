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

	@Override
	public String getReport() throws IOException {
		return getContentsAsJSON();
	}

	public String getContentsAsJSON() throws IOException {
		return getContents("json");
	}

	public String getContentsAsXML() throws  IOException {
		return getContents("xml");
	}

	private String getContents(String contentFormat) throws IOException {
		String requestMethod = "GET";
		Map<String, String> requestProperties = new HashMap<>();
		requestProperties.put("Content-Type", "application/" + contentFormat);

		return getAllContent(getHttpURLConnection(requestMethod, requestProperties));
	}
}
