package org.reactome.release.resourcechecker;

import org.reactome.release.Resource;

public class ResourceCheckerFactory {
	public static ResourceChecker getInstance(Resource resource) {
		switch(resource.getResourceType()) {
			case FILE:
				if (resource.getResourceURL().getProtocol().equals("ftp")) {
					return new FTPFileResourceChecker(resource);
				} else if (resource.getResourceURL().getProtocol().startsWith("http")) {
					return new HTTPFileResourceChecker(resource);
				} else {
					throw new IllegalArgumentException(
						"The protocol " + resource.getResourceURL().getProtocol() +
						" to check a file resource is not supported for " + resource.getResourceName() + "\n"
					);
				}
			case REST_ENDPOINT:
				return new RESTfulAPIResourceChecker(resource);
			case WEB_PAGE:
				return new WebPageResourceChecker(resource);
			case WEB_SERVICE:
			case FTP_SERVER:
			default:
				throw new IllegalArgumentException(
					"The type " + resource.getResourceType() + "is not recognized for " + resource.getResourceName()
				);
		}
	}
}
