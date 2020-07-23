package org.reactome.release;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import org.reactome.release.resourcechecker.FTPFileResourceChecker;
import org.reactome.release.resourcechecker.FileResourceChecker.ByteUnit;
import org.reactome.release.resourcechecker.ResourceChecker;
import org.reactome.release.resourcechecker.ResourceCheckerFactory;

public class Main {
	public static void main(String[] args) throws Exception {
		System.out.println("ResourceExists\tFileSize");
		for (Resource resourceToCheck: getResourcesToCheck("External_Resources2.csv")) {
			ResourceChecker resourceChecker = ResourceCheckerFactory.getInstance(resourceToCheck);
//			System.out.println("Host: " + resourceToCheck.getResourceURL().getHost());
//			System.out.println(resourceToCheck.getResourceURL() + " exists? " + resourceChecker.resourceExists());
			System.out.println(resourceChecker instanceof FTPFileResourceChecker ? ((FTPFileResourceChecker) resourceChecker).getFileSizeAs(
				ByteUnit.MEGABYTE) : "");
			if (resourceChecker instanceof FTPFileResourceChecker) {
				((FTPFileResourceChecker) resourceChecker).saveFileContents(
					Paths.get(resourceChecker.getResource().getResourceName()));
			}


			System.out.println(resourceChecker.getReport());
		}
	}

	private static List<Resource> getResourcesToCheck(String csvFileName) throws IOException {
		String resourcesCSVPath = Main.class.getClassLoader().getResource(csvFileName).getPath();
		ResourceBuilder resourceBuilder = new ResourceBuilder();
		return resourceBuilder.getResources(resourcesCSVPath);
	}
}
