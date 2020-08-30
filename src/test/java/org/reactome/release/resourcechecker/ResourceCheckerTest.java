package org.reactome.release.resourcechecker;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import com.google.gson.JsonObject;
import java.net.MalformedURLException;
import java.net.URL;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.reactome.release.Resource;
import org.reactome.release.testutilities.ResourceTestUtils;

public class ResourceCheckerTest {
	private static ResourceChecker resourceChecker;

	private static Resource resource;

	@BeforeAll
	public static void initializeResourceChecker() {
		resourceChecker = new ResourceChecker() {
			@Override
			public Resource getResource() {
				if (resource == null) {
					resource = ResourceTestUtils.getResource();
				}

				return resource;
			}

			@Override
			public boolean resourceExists() {
				return true;
			}

			@Override
			public JsonObject getReport() {
				return getMockReport();
			}

			@Override
			public boolean resourcePassesAllChecks() {
				return true;
			}
		};
	}

	@Test
	public void getResourceURLReturnsExpectedURL() throws MalformedURLException {
		final URL expectedURL = new URL("ftp://ftp.ebi.ac.uk/pub/databases/rhea/tsv/rhea2reactome.tsv");

		assertThat(resourceChecker.getResourceURL(), is(equalTo(expectedURL)));
	}

	@Test
	public void getResourceJsonWithReportReturnsExpectedJsonString() {
		final String expectedJsonStringWithReport = getExpectedJsonStringWithReport();

		assertThat(resourceChecker.getResourceJsonWithReport(), is(equalTo(expectedJsonStringWithReport)));
	}

	@Test
	public void getResourceNameReturnsExpectedName() {
		final String expectedName = "rhea2reactome.tsv";

		assertThat(resourceChecker.getResourceName(), is(equalTo(expectedName)));
	}

	private static JsonObject getMockReport() {
		JsonObject mockReportJson = new JsonObject();
		mockReportJson.addProperty("Passed Checks", true);
		mockReportJson.addProperty("Resource Exists", true);

		return mockReportJson;
	}

	private String getExpectedJsonStringWithReport() {
		return String.join(System.lineSeparator(),
			"{",
				"  \"Release Step\": \"AddLinks\",",
				"  \"Main Program\": \"add-links/Main.java\",",
				"  \"Dependency in Source Code\": \"add-links/src/main/resources/basic-file-retrievers.xml#L145\",",
				"  \"Resource\": \"rhea2reactome.tsv\",",
				"  \"Resource Description\": \"Rhea Data\",",
				"  \"Resource Type\": \"File\",",
				"  \"Resource URL\": \"ftp://ftp.ebi.ac.uk/pub/databases/rhea/tsv/rhea2reactome.tsv\",",
				"  \"Report\": {",
					"    \"Passed Checks\": true,",
					"    \"Resource Exists\": true",
				"  }",
			"}"
		);
	}
}
