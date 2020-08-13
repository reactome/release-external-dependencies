package org.reactome.release.resourcechecker;

import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import java.util.Iterator;
import org.reactome.release.Resource;

public class HTTPFileResourceChecker implements HTTPResourceChecker, FileResourceChecker {
	private Resource resource;

	public HTTPFileResourceChecker(Resource resource) {
		this.resource = resource;
	}

	@Override
	public long getFileSize() {
		try {
			return getHttpURLConnection().getContentLength();
		} catch (IOException e) {
			logger.error("Unable to get file size for HTTP URL Connection to " + getResourceURL(), e);
			return -1;
		}
	}

	@Override
	public void saveFileContents(Path downloadDestination) throws IOException {
		Files.deleteIfExists(downloadDestination);

		Iterator<String> contentIterator = getContentIterator();
		while (contentIterator.hasNext()) {
			Files.write(
				downloadDestination,
				contentIterator.next().getBytes(),
				StandardOpenOption.CREATE, StandardOpenOption.APPEND
			);
		}
	}

	@Override
	public Resource getResource() {
		return this.resource;
	}

	@Override
	public boolean resourcePassesAllChecks() {
		return HTTPResourceChecker.super.resourcePassesAllChecks() &&
			FileResourceChecker.super.resourcePassesAllChecks();
	}

	@Override
	public String getReport() {
		return FileResourceChecker.super.getReport();
	}
}
