package org.reactome.release;

import java.net.URL;
import org.reactome.release.Resource.ResourceType;
import org.reactome.release.resourcechecker.FTPFileResourceChecker;
import org.reactome.release.resourcechecker.HTTPFileResourceChecker;

public abstract class ResourceChecker {
	private Resource resource;

	protected ResourceChecker(Resource resource) {
		this.resource = resource;
	}

	public Resource getResource() {
		return resource;
	}

	public URL getResourceURL() {
		return resource.getResourceURL();
	}

	protected abstract boolean resourceExists() throws Exception;

	public static ResourceChecker getInstance(Resource resource) {
		if (resource.getResourceType().equals(ResourceType.FILE)) {
			if (resource.getResourceURL().getProtocol().equals("ftp")) {
				return new FTPFileResourceChecker(resource);
			} else if (resource.getResourceURL().getProtocol().startsWith("http")) {
				return new HTTPFileResourceChecker(resource);
			}
		}
		return null;
	}
}
