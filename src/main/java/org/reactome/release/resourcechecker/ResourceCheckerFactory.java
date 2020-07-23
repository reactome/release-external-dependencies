package org.reactome.release.resourcechecker;

import org.reactome.release.Resource;
import org.reactome.release.Resource.ResourceType;

public class ResourceCheckerFactory {
//	private Resource resource;
//
//	protected ResourceChecker(Resource resource) {
//		this.resource = resource;
//	}
//
//	public Resource getResource() {
//		return resource;
//	}
//
//	public URL getResourceURL() {
//		return resource.getResourceURL();
//	}
//
//	public abstract boolean resourceExists();
//
//	public abstract String getReport() throws IOException;

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
