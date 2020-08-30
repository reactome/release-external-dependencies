package org.reactome.release.resourcechecker;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyLong;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.reactome.release.Resource;

public class HTTPFileResourceCheckerTest {
	@Mock
	private Resource resource;

	private HTTPFileResourceChecker httpFileResourceChecker;

	@BeforeEach
	public void initializeClassFields() {
		MockitoAnnotations.initMocks(this);
		httpFileResourceChecker = Mockito.spy(new HTTPFileResourceChecker(resource));
	}

	@Test
	public void getFileSizeReturnsExpectedSize() throws IOException {
		final long testFileSize = 100;

		HttpURLConnection httpURLConnection = Mockito.mock(HttpURLConnection.class);
		Mockito.doReturn(httpURLConnection).when(httpFileResourceChecker).getHttpURLConnection();
		Mockito.when(httpURLConnection.getContentLength()).thenReturn((int) testFileSize);

		assertThat(httpFileResourceChecker.getFileSize(), is(equalTo(testFileSize)));
	}

	@Test
	public void getFileSizeReturnsNegativeOneWhenIOExceptionThrownByHttpURLConnection() throws IOException {
		final String dummyURL = "http://testresource.com";
		Mockito.doReturn(new URL(dummyURL)).when(httpFileResourceChecker).getResourceURL();
		Mockito.doThrow(IOException.class).when(httpFileResourceChecker).getHttpURLConnection();

		assertThat(httpFileResourceChecker.getFileSize(), is(equalTo(-1L)));
	}

	@Test
	public void saveFileContentsWritesExpectedContentToProvidedPath() throws IOException {
		final Path testFile = Paths.get("src", "test", "resources", "http_file_test_resource_file_contents.txt");
		final List<String> testContentLines = Arrays.asList("Test 1", "Test 2");
		final String expectedTestFileContent = String.join("", testContentLines);

		Mockito.doReturn(
			getMockContentIterator(testContentLines)
		).when(httpFileResourceChecker).getContentIterator();

		httpFileResourceChecker.saveFileContents(testFile);

		assertThat(
			contentOf(testFile),
			is(equalTo(expectedTestFileContent))
		);

		// Clean up
		Files.deleteIfExists(testFile);
	}

	@Test
	public void getResourceReturnsExpectedObject() {
		assertThat(httpFileResourceChecker.getResource(), is(equalTo(resource)));
	}

	@Test
	public void resourcePassesAllChecksReturnsTrueWhenResourceExistsAndFileSizeAndContentAreExpected() {
		Mockito.doReturn(true).when(httpFileResourceChecker).resourceExists();
		Mockito.doReturn(true).when(httpFileResourceChecker).isFileSizeAcceptable(anyLong());
		Mockito.doReturn(true).when(httpFileResourceChecker).hasExpectedContent();

		assertThat(httpFileResourceChecker.resourcePassesAllChecks(), is(equalTo(true)));
	}

	@Test
	public void resourcePassesAllChecksReturnsFalseWhenResourceDoesNotExist() {
		Mockito.doReturn(false).when(httpFileResourceChecker).resourceExists();
		Mockito.doReturn(true).when(httpFileResourceChecker).isFileSizeAcceptable(anyLong());
		Mockito.doReturn(true).when(httpFileResourceChecker).hasExpectedContent();

		assertThat(httpFileResourceChecker.resourcePassesAllChecks(), is(equalTo(false)));
	}

	@Test
	public void resourcePassesAllChecksReturnsFalseWhenFileSizeIsNotAcceptable() {
		Mockito.doReturn(true).when(httpFileResourceChecker).resourceExists();
		Mockito.doReturn(false).when(httpFileResourceChecker).isFileSizeAcceptable(anyLong());
		Mockito.doReturn(true).when(httpFileResourceChecker).hasExpectedContent();

		assertThat(httpFileResourceChecker.resourcePassesAllChecks(), is(equalTo(false)));
	}

	@Test
	public void resourcePassesAllChecksReturnsFalseExpectedContentIsNotPresent() {
		Mockito.doReturn(true).when(httpFileResourceChecker).resourceExists();
		Mockito.doReturn(true).when(httpFileResourceChecker).isFileSizeAcceptable(anyLong());
		Mockito.doReturn(false).when(httpFileResourceChecker).hasExpectedContent();

		assertThat(httpFileResourceChecker.resourcePassesAllChecks(), is(equalTo(false)));
	}

	@Test
	public void getReportReturnsExpectedJsonObject() {
		// Implementation of httpFileResourceChecker.getReport() is based on FileResourceChecker.getReport(),
		// so the testExpectedFileSize and expected report are obtained from the FileResourceCheckerTest class

		long testExpectedFileSize = 950;
		Mockito.doReturn(true).when(httpFileResourceChecker).resourceExists();
		Mockito.doReturn(true).when(httpFileResourceChecker).resourcePassesAllChecks();
		Mockito.doReturn(testExpectedFileSize).when(httpFileResourceChecker).getFileSize();
		Mockito.doReturn(testExpectedFileSize).when(resource).getExpectedFileSizeInBytes();

		assertThat(
			httpFileResourceChecker.getReport(),
			is(equalTo(FileResourceCheckerTest.getExpectedReportJsonObject()))
		);
	}

	@SuppressWarnings("unchecked")
	private Iterator<String> getMockContentIterator(List<String> contents) {
		Iterator<String> mockContentIterator = Mockito.mock(Iterator.class);

		List<Boolean> hasNextValues = createBooleanListWithAllTrue(contents.size());
		hasNextValues.add(false);

		Mockito.doReturn(firstValue(contents), allValuesAfterFirst(contents)).when(mockContentIterator).next();
		Mockito.doReturn(firstValue(hasNextValues), allValuesAfterFirst(hasNextValues)).when(mockContentIterator).hasNext();

		return mockContentIterator;
	}

	private String contentOf(Path testFile) throws IOException {
		return new String(Files.readAllBytes(testFile));
	}

	private List<Boolean> createBooleanListWithAllTrue(int size) {
		return new ArrayList<>(Collections.nCopies(size, true));
	}

	private <T> T firstValue(List<T> list) {
		return list.get(0);
	}

	private <T> Object[] allValuesAfterFirst(List<T> list) {
		return list.subList(1, list.size()).toArray();
	}
}
