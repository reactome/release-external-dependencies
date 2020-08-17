package org.reactome.release;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.matchesPattern;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class ResourceParserTest {
	private static String singleResourceAsCSVFilePath;
	private static String singleResourceAsJsonFilePath;
	private static ResourceParser resourceParser;

	@BeforeAll
	public static void initializeResourceParserAndResourceDataFilePaths() throws IOException {
		resourceParser = new ResourceParser();
		singleResourceAsCSVFilePath = getPath("External_Resources_Single_Test_Resource.csv");
		singleResourceAsJsonFilePath = getPath("External_Resources_Single_Test_Resource.json");
	}

	@Test
	public void oneResourceFromTestJSONData() throws IOException {
		List<Resource> resourcesFromTestJsonData = resourceParser.getResources(singleResourceAsJsonFilePath);

		assertThat(resourcesFromTestJsonData, hasSize(1));
	}

	@Test
	public void oneResourceFromTestCSVData() throws IOException {
		List<Resource> resourcesFromTestCSVData = resourceParser.getResources(singleResourceAsCSVFilePath);

		assertThat(resourcesFromTestCSVData, hasSize(1));
	}

	@Test
	public void throwsExceptionForNonExistentResourceFile() {
		FileNotFoundException thrown = assertThrows(
			FileNotFoundException.class,
			() -> resourceParser.getResources("/file/does/not/exist.json"),
			"Expected Resource Parser receiving a path to a non-existent file to throw a FileNotFoundExtension, " +
				"but it didn't"
		);
	}

	@Test
	public void exceptionThrownForAResourceFileWithoutAJSONOrCSVExtension() throws IOException {
		String resourceFileWithoutAJSONOrCSVExtension = "External_Resources_Single_Test_Resource.txt";

		IllegalArgumentException thrown = assertThrows(
			IllegalArgumentException.class,
			() -> resourceParser.getResources(getPath(resourceFileWithoutAJSONOrCSVExtension)),
			"Expected Resource Parser receiving a file without a .json or .csv extension to throw an " +
				"IllegalArgumentException, but it didn't"
		);

		assertThat(
			thrown.getMessage(),
			matchesPattern("Extension for the file .*? is not recognized")
		);
	}

	private static String getPath(String resourceName) {
		return Objects.requireNonNull(
			ResourceParserTest.class.getClassLoader().getResource(resourceName)
		).getPath();
	}
}
