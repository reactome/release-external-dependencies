package org.reactome.release;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.reactome.release.testutilities.ResourceTestUtils.getResource;
import static org.reactome.release.testutilities.ResourceTestUtils.getTestResourceJsonObject;

import com.google.gson.JsonObject;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.reactome.release.Resource.ResourceType;

public class ResourceTest {
	private static Resource resource;

	@BeforeAll
	public static void createResourceFromTestJson() throws IOException {
		resource = getResource();
	}

	@Test
	public void resourceReleaseStepIsCorrect() {
		final String expectedReleaseStep = "AddLinks";

		assertThat(resource.getReleaseStep(), is(equalTo(expectedReleaseStep)));
	}

	@Test
	public void resourceMainProgramIsCorrect() {
		final String expectedMainProgramName = "add-links/Main.java";

		assertThat(resource.getMainProgramName(), is(equalTo(expectedMainProgramName)));
	}

	@Test
	public void resourceSourceCodeDependencyIsCorrect() {
		final String expectedSourceCodeDependency = "add-links/src/main/resources/basic-file-retrievers.xml#L145";

		assertThat(resource.getSourceCodeDependency(), is(equalTo(expectedSourceCodeDependency)));
	}

	@Test
	public void resourceNameIsCorrect() {
		final String expectedFileName = "rhea2reactome.tsv";

		assertThat(resource.getResourceName(), is(equalTo(expectedFileName)));
	}

	@Test
	public void resourceDescriptionIsCorrect() {
		final String expectedDescription = "Rhea Data";

		assertThat(resource.getResourceDescription(), is(equalTo(expectedDescription)));
	}

	@Test
	public void resourceTypeIsCorrect() {
		final ResourceType expectedResourceType = ResourceType.FILE;

		assertThat(resource.getResourceType(), is(equalTo(expectedResourceType)));
	}

	@Test
	public void resourceURLIsCorrect() throws MalformedURLException {
		final URL expectedURL = new URL("ftp://ftp.ebi.ac.uk/pub/databases/rhea/tsv/rhea2reactome.tsv");

		assertThat(resource.getResourceURL(), is(equalTo(expectedURL)));
	}

	@Test
	public void resourceReturnsAnEmptyStringForExpectedResponseTextWhenAbsent() {
		assertThat(resource.getExpectedResponseText(), is(equalTo("")));
	}

	@Test
	public void resourceReturnsAnEmptyStringForErrorResponseTextWhenAbsent() {
		assertThat(resource.getErrorResponseText(), is(equalTo("")));
	}

	@Test
	public void resourceReturnsNegativeOneForExpectedFileSizeInBytesWhenAbsent() {
		assertThat(resource.getExpectedFileSizeInBytes(), is(equalTo(-1L)));
	}

	@Test
	public void illegalStateExceptionIsThrownIfMandatoryResourceAttributeIsMissing() throws FileNotFoundException {
		final String mandatoryResourceAttributeName = "Release Step";

		JsonObject testResourceJson = getTestResourceJsonObject();
		testResourceJson.remove(mandatoryResourceAttributeName);

		IllegalStateException thrown = assertThrows(
			IllegalStateException.class,
			() -> new Resource(testResourceJson),
			"Expected a missing mandatory attribute from a test resource to throw an IllegalStateException, but it"
				+ "didn't"
		);

		assertThat(
			thrown.getMessage(),
			containsString(mandatoryResourceAttributeName + " is empty or does not exist")
		);
	}

	@Test
	public void runtimeExceptionIsThrownIfResourceURLIsMissing() throws FileNotFoundException {
		JsonObject testResourceJson = getTestResourceJsonObject();
		testResourceJson.remove("Resource URL");

		RuntimeException thrown = assertThrows(
			RuntimeException.class,
			() -> new Resource(testResourceJson),
			"Expected a missing \"Resource URL\" in a test resource to throw a RuntimeException, but it"
				+ "didn't"
		);

		assertThat(
			thrown.getMessage(),
			containsString("Unable to create URL")
		);
	}
}