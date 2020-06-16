package org.reactome.release;

import java.io.IOException;
import java.util.List;

public class Main {
	public static void main(String[] args) throws Exception {
		for (Resource resourceToCheck: getResourcesToCheck("External_Resources2.csv")) {
			ResourceChecker resourceChecker = ResourceChecker.getInstance(resourceToCheck);
//			System.out.println("Host: " + resourceToCheck.getResourceURL().getHost());
//			if (resourceChecker == null || !resourceToCheck.getResourceURL().getHost().equals("www.uniprot.org"))
//				continue;
//			System.out.println(resourceToCheck.getResourceURL() + ": " + resourceChecker.resourceExists());
		}
	}

	private static List<Resource> getResourcesToCheck(String csvFileName) throws IOException {
		String resourcesCSVPath = Main.class.getClassLoader().getResource(csvFileName).getPath();
		ResourceBuilder resourceBuilder = new ResourceBuilder();
		return resourceBuilder.getResources(resourcesCSVPath);
	}
}
