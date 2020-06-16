package org.reactome.release.resourcechecker;

import java.io.IOException;
import java.net.HttpURLConnection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.reactome.release.Resource;
import org.reactome.release.ResourceChecker;

public class HTTPFileResourceChecker extends ResourceChecker {
	private final Logger logger = LogManager.getLogger();

	public HTTPFileResourceChecker(Resource resource) {
		super(resource);
	}

	protected boolean resourceExists() throws IOException {
		HttpURLConnection httpURLConnection = (HttpURLConnection) getResourceURL().openConnection();
		httpURLConnection.setRequestMethod("HEAD");
		httpURLConnection.setInstanceFollowRedirects(true);
		httpURLConnection.connect();

		//System.out.println(httpURLConnection.getHeaderFields());
		int responseCode = httpURLConnection.getResponseCode();
		return responseCode == HttpURLConnection.HTTP_OK;
	}
}
