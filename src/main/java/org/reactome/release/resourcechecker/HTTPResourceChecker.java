package org.reactome.release.resourcechecker;

import com.google.gson.JsonObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public interface HTTPResourceChecker extends ResourceChecker {
	default HttpURLConnection getHttpURLConnection() throws IOException {
		return getHttpURLConnection("HEAD");
	}

	default HttpURLConnection getHttpURLConnection(String requestMethod, Map<String, String> requestProperties)
		throws IOException {

		HttpURLConnection httpURLConnection = (HttpURLConnection) getResourceURL().openConnection();
		httpURLConnection.setRequestMethod(requestMethod);
		for (Entry<String, String> requestProperty : requestProperties.entrySet()) {
			httpURLConnection.setRequestProperty(requestProperty.getKey(), requestProperty.getValue());
		}
		httpURLConnection.setInstanceFollowRedirects(true);
		httpURLConnection.connect();

		return httpURLConnection;
	}

	default HttpURLConnection getHttpURLConnection(String requestMethod) throws IOException {
		return getHttpURLConnection(requestMethod, Collections.emptyMap());
	}

	default InputStream getContentStream(HttpURLConnection httpURLConnection) throws IOException {
		return httpURLConnection.getInputStream();
	}

	default Iterator<String> getContentIterator() throws IOException {
		return getContentIterator(getHttpURLConnection("GET"));
	}

	default Iterator<String> getContentIterator(HttpURLConnection httpURLConnection) throws IOException {
		ContentChunkGenerator contentChunkGenerator = new ContentChunkGenerator(
			getContentStream(httpURLConnection)
		);
		return contentChunkGenerator.iterator();
	}

	default String getAllContent(HttpURLConnection httpURLConnection) throws IOException {
		Iterator<String> contentIterator = getContentIterator(httpURLConnection);

		StringBuilder stringBuilder = new StringBuilder();
		while (contentIterator.hasNext()) {
			stringBuilder.append(contentIterator.next());
		}
		return stringBuilder.toString();
	}

	default String getAllContent() {
		try {
			return getAllContent(getHttpURLConnection("GET"));
		} catch (IOException e) {
			logger.error("Unable to get content for " + getResourceURL(), e);
			return "";
		}
	}

	default Date getLastModifiedDateTime() {
		try {
			return new Date(getHttpURLConnection().getLastModified());
		} catch (IOException e) {
			logger.error("Unable to get last modified datetime for HTTP URL Connection to " + getResourceURL(), e);
			return null;
		}
	}

	default int getResponseCode() {
		try {
			return getHttpURLConnection().getResponseCode();
		} catch (IOException e) {
			logger.error("Unable to get response code for HTTP URL Connection to " + getResourceURL(), e);
			return -1;
		}
	}

	default boolean hasExpectedContent() {
		return !isErrorResponseTextPresent() && isExpectedResponseTextPresent();
	}

	default boolean isErrorResponseTextPresent() {
		return !getResource().getErrorResponseText().isEmpty() &&
			getAllContent().contains(getResource().getErrorResponseText());
	}

	default boolean isExpectedResponseTextPresent() {
		return getResource().getExpectedResponseText().isEmpty() ||
			getAllContent().contains(getResource().getExpectedResponseText());
	}

	@Override
	default boolean resourceExists() {
		return getResponseCode() == HttpURLConnection.HTTP_OK;
	}

	@Override
	default JsonObject getReport() {
		JsonObject reportJson = new JsonObject();
		reportJson.addProperty("Passed Checks", resourcePassesAllChecks());
		reportJson.addProperty("Resource Exists", resourceExists());
		reportJson.add("Response Text", getResponseTextReport());

		return reportJson;
	}

	default JsonObject getResponseTextReport() {
		JsonObject responseTextReportJson = new JsonObject();
		responseTextReportJson.addProperty("Has Expected Content", hasExpectedContent());
		responseTextReportJson.addProperty("Error Response Text Present", isErrorResponseTextPresent());
		responseTextReportJson.addProperty("Expected Response Text Present", isExpectedResponseTextPresent());
		return responseTextReportJson;
	}

	@Override
	default boolean resourcePassesAllChecks() {
		return resourceExists() && hasExpectedContent();
	}

	class ContentChunkGenerator implements Iterable<String> {
		private InputStream inputStream;

		public ContentChunkGenerator(InputStream inputStream) {
			this.inputStream = inputStream;
		}

		@Override
		public Iterator<String> iterator() {
			return new ContentChunkGeneratorIterator();
		}

		private class ContentChunkGeneratorIterator implements Iterator<String> {
			private String contentChunk;

			@Override
			public boolean hasNext() {
				byte[] buffer = new byte[4096];
				try {
					int bytesRead;
					if ((bytesRead = inputStream.read(buffer, 0, buffer.length)) > 0) {
						this.contentChunk = new String(buffer, 0, bytesRead, StandardCharsets.UTF_8);
						return true;
					}
				} catch (IOException e) {
					logger.error("Unable to fetch next chunk of content from HTTP Resource", e);
					try {
						inputStream.close();
					} catch (IOException ex) {
						logger.error("Unable to close the input stream for HTTP Resource", e);
					}
				}
				return false;
			}

			@Override
			public String next() {
				return this.contentChunk;
			}
		}
	}
}
