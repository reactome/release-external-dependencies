package org.reactome.release.resourcechecker;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.NoSuchElementException;
import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.reactome.release.Resource;
import org.reactome.release.resourcechecker.HTTPResourceChecker.ContentChunkGenerator;
import org.reactome.release.testutilities.HTTPResourceCheckerBuilder;
import org.skyscreamer.jsonassert.JSONAssert;

public class HTTPResourceCheckerTest {
	private static final String DEFAULT_TEST_CONTENT = "Test Content";
	private static final String EXPECTED_RESPONSE_TEXT = "This is the expected response text";
	private static final String ERROR_RESPONSE_TEXT = "Error: Couldn't retrieve content";

	@Mock
	private Resource resource;

	@Mock
	private HttpURLConnection httpURLConnection;

	@BeforeEach
	public void initializeMocks() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void getReportReturnsExpectedJsonObject() throws JSONException {
		final boolean assertWithStrictEquality = true;

		HTTPResourceChecker httpResourceChecker = Mockito.spy(new HTTPResourceCheckerBuilder(resource).build());

		Mockito.doReturn(true).when(httpResourceChecker).resourcePassesAllChecks();
		Mockito.doReturn(true).when(httpResourceChecker).resourceExists();
		Mockito.doReturn(true).when(httpResourceChecker).hasExpectedContent();
		Mockito.doReturn(true).when(httpResourceChecker).isExpectedResponseTextPresent();
		Mockito.doReturn(false).when(httpResourceChecker).isErrorResponseTextPresent();

		JSONAssert.assertEquals(
		"{"
			+ "\"Passed Checks\":true,"
			+ "\"Resource Exists\":true,"
			+ "\"Response Text\": {"
			+     "\"Has Expected Content\":true,"
			+     "\"Error Response Text Present\":false,"
			+     "\"Expected Response Text Present\":true"
			+ "}"
			+ "}",
			httpResourceChecker.getReport().toString(),
			assertWithStrictEquality
		);
	}

	@Test
	public void getResponseTextReportReturnsExpectedJsonObject() throws JSONException {
		final boolean assertWithStrictEquality = true;

		HTTPResourceChecker httpResourceChecker = Mockito.spy(new HTTPResourceCheckerBuilder(resource).build());

		Mockito.doReturn(true).when(httpResourceChecker).hasExpectedContent();
		Mockito.doReturn(true).when(httpResourceChecker).isExpectedResponseTextPresent();
		Mockito.doReturn(false).when(httpResourceChecker).isErrorResponseTextPresent();


		JSONAssert.assertEquals(
			"{\"Has Expected Content\":true,\"Error Response Text Present\":false,\"Expected Response Text Present\":true}",
			httpResourceChecker.getResponseTextReport().toString(),
			assertWithStrictEquality
		);
	}

	@Test
	public void resourcePassesAllChecksReturnsTrueWhenResourceExistsAndHasExpectedContent() {
		Mockito.when(resource.getErrorResponseText()).thenReturn("");
		Mockito.when(resource.getExpectedResponseText()).thenReturn(EXPECTED_RESPONSE_TEXT);

		HTTPResourceChecker httpResourceChecker = new HTTPResourceCheckerBuilder(resource)
			.withMockGetHTTPURLConnectionMethodObtaining(EXPECTED_RESPONSE_TEXT)
			.build();

		assertThat(
			httpResourceChecker.resourcePassesAllChecks(),
			is(equalTo(true))
		);
	}

	@Test
	public void resourcePassesAllChecksReturnsFalseWhenResourceDoesNotExist() {
		HTTPResourceChecker httpResourceChecker = new HTTPResourceCheckerBuilder(resource)
			.withMockGetHTTPURLConnectionMethodObtaining(EXPECTED_RESPONSE_TEXT)
			.withMockGetResponseCodeMethodReturningNoResponseCode()
			.build();

		assertThat(
			httpResourceChecker.resourcePassesAllChecks(),
			is(equalTo(false))
		);
	}

	@Test
	public void resourcePassesAllChecksReturnsFalseWhenExpectedTextContentIsNotReturned() {
		Mockito.when(resource.getErrorResponseText()).thenReturn("");
		Mockito.when(resource.getExpectedResponseText()).thenReturn(EXPECTED_RESPONSE_TEXT);

		HTTPResourceChecker httpResourceChecker = new HTTPResourceCheckerBuilder(resource)
			.withMockGetHTTPURLConnectionMethodObtaining(DEFAULT_TEST_CONTENT)
			.build();

		assertThat(
			httpResourceChecker.resourcePassesAllChecks(),
			is(equalTo(false))
		);
	}

	@Test
	public void resourcePassesAllChecksReturnsFalseWhenErrorTextContentIsReturned() {
		Mockito.when(resource.getErrorResponseText()).thenReturn(ERROR_RESPONSE_TEXT);
		Mockito.when(resource.getExpectedResponseText()).thenReturn(EXPECTED_RESPONSE_TEXT);

		HTTPResourceChecker httpResourceChecker = new HTTPResourceCheckerBuilder(resource)
			.withMockGetHTTPURLConnectionMethodObtaining(ERROR_RESPONSE_TEXT)
			.build();

		assertThat(
			httpResourceChecker.resourcePassesAllChecks(),
			is(equalTo(false))
		);
	}

	@Test
	public void resourceExistsReturnsTrueWhenResponseCodeIsOK() {
		HTTPResourceChecker httpResourceChecker = new HTTPResourceCheckerBuilder(resource)
			.withMockGetHTTPURLConnectionMethodObtaining(DEFAULT_TEST_CONTENT)
			.build();

		assertThat(
			httpResourceChecker.resourceExists(),
			is(equalTo(true))
		);
	}

	@Test
	public void getResponseCodeReturnsHTTPOKCode() {
		HTTPResourceChecker httpResourceChecker = new HTTPResourceCheckerBuilder(resource)
			.withMockGetHTTPURLConnectionMethodObtaining(DEFAULT_TEST_CONTENT)
			.build();

		assertThat(
			httpResourceChecker.getResponseCode(),
			is(equalTo(HttpURLConnection.HTTP_OK))
		);
	}

	@Test
	public void getResponseCodeReturnsNegativeOneWhenHttpURLConnectionCannotGetResponseCode() {
		final int NO_RESPONSE_CODE = -1;

		HTTPResourceChecker httpResourceChecker = new HTTPResourceCheckerBuilder(resource)
			.withMockGetHTTPURLConnectionMethodObtaining(DEFAULT_TEST_CONTENT)
			.withMockGetResponseCodeMethodReturningNoResponseCode()
			.build();

		assertThat(
			httpResourceChecker.getResponseCode(),
			is(equalTo(NO_RESPONSE_CODE))
		);
	}

	@Test
	public void hasExpectedContentIsTrueWhenHasExpectedTextContentAndHasNoErrorTextContent() {
		Mockito.when(resource.getErrorResponseText()).thenReturn("");
		Mockito.when(resource.getExpectedResponseText()).thenReturn(EXPECTED_RESPONSE_TEXT);

		HTTPResourceChecker httpResourceChecker = new HTTPResourceCheckerBuilder(resource)
			.withMockGetHTTPURLConnectionMethodObtaining(EXPECTED_RESPONSE_TEXT)
			.build();

		assertThat(
			httpResourceChecker.hasExpectedContent(),
			is(equalTo(true))
		);
	}

	@Test
	public void hasExpectedContentIsFalseWhenExpectedTextContentIsNotReturned() {
		Mockito.when(resource.getErrorResponseText()).thenReturn("");
		Mockito.when(resource.getExpectedResponseText()).thenReturn(EXPECTED_RESPONSE_TEXT);

		HTTPResourceChecker httpResourceChecker = new HTTPResourceCheckerBuilder(resource)
			.withMockGetHTTPURLConnectionMethodObtaining(DEFAULT_TEST_CONTENT)
			.build();

		assertThat(
			httpResourceChecker.hasExpectedContent(),
			is(equalTo(false))
		);
	}

	@Test
	public void hasExpectedContentIsFalseWhenErrorTextContentIsReturned() {
		Mockito.when(resource.getErrorResponseText()).thenReturn(ERROR_RESPONSE_TEXT);
		Mockito.when(resource.getExpectedResponseText()).thenReturn(EXPECTED_RESPONSE_TEXT);

		HTTPResourceChecker httpResourceChecker = new HTTPResourceCheckerBuilder(resource)
			.withMockGetHTTPURLConnectionMethodObtaining(ERROR_RESPONSE_TEXT)
			.build();

		assertThat(
			httpResourceChecker.hasExpectedContent(),
			is(equalTo(false))
		);
	}

	@Test
	public void isExpectedResponseTextPresentReturnsTrueWhenContentHasExpectedResponseText() {
		Mockito.when(resource.getExpectedResponseText()).thenReturn(EXPECTED_RESPONSE_TEXT);

		HTTPResourceChecker httpResourceChecker = new HTTPResourceCheckerBuilder(resource)
			.withMockGetHTTPURLConnectionMethodObtaining(EXPECTED_RESPONSE_TEXT)
			.build();

		assertThat(httpResourceChecker.isExpectedResponseTextPresent(), is(equalTo(true)));
	}

	@Test
	public void isExpectedResponseTextPresentReturnsTrueWhenResourceHasNoExpectedResponseTextAttribute() {
		Mockito.when(resource.getExpectedResponseText()).thenReturn("");

		HTTPResourceChecker httpResourceChecker = new HTTPResourceCheckerBuilder(resource)
			.withMockGetHTTPURLConnectionMethodObtaining(EXPECTED_RESPONSE_TEXT)
			.build();

		assertThat(httpResourceChecker.isExpectedResponseTextPresent(), is(equalTo(true)));
	}

	@Test
	public void isExpectedResponseTextPresentReturnsFalseWhenContentDoesNotHaveExpectedResponseText() {
		Mockito.when(resource.getExpectedResponseText()).thenReturn(EXPECTED_RESPONSE_TEXT);

		HTTPResourceChecker httpResourceChecker = new HTTPResourceCheckerBuilder(resource)
			.withMockGetHTTPURLConnectionMethodObtaining(DEFAULT_TEST_CONTENT)
			.build();

		assertThat(httpResourceChecker.isExpectedResponseTextPresent(), is(equalTo(false)));
	}

	@Test
	public void isErrorResponseTextPresentReturnsFalseWhenResourceHasNoErrorResponseTextAttribute() {
		Mockito.when(resource.getErrorResponseText()).thenReturn("");

		HTTPResourceChecker httpResourceChecker = new HTTPResourceCheckerBuilder(resource)
			.withMockGetHTTPURLConnectionMethodObtaining(ERROR_RESPONSE_TEXT)
			.build();

		assertThat(httpResourceChecker.isErrorResponseTextPresent(), is(equalTo(false)));
	}

	@Test
	public void isErrorResponseTextPresentReturnsFalseWhenContentDoesNotHaveExpectedResponseText() {
		Mockito.when(resource.getErrorResponseText()).thenReturn(ERROR_RESPONSE_TEXT);

		HTTPResourceChecker httpResourceChecker = new HTTPResourceCheckerBuilder(resource)
			.withMockGetHTTPURLConnectionMethodObtaining(DEFAULT_TEST_CONTENT)
			.build();

		assertThat(httpResourceChecker.isErrorResponseTextPresent(), is(equalTo(false)));
	}


	@Test
	public void isErrorResponseTextPresentReturnsTrueWhenContentHasErrorResponseText() {
		Mockito.when(resource.getErrorResponseText()).thenReturn(ERROR_RESPONSE_TEXT);

		HTTPResourceChecker httpResourceChecker = new HTTPResourceCheckerBuilder(resource)
			.withMockGetHTTPURLConnectionMethodObtaining(ERROR_RESPONSE_TEXT)
			.build();

		assertThat(httpResourceChecker.isErrorResponseTextPresent(), is(equalTo(true)));
	}

	@Test
	public void getAllContentReturnsExpectedString() {
		HTTPResourceChecker httpResourceChecker = new HTTPResourceCheckerBuilder(resource)
			.withMockGetHTTPURLConnectionMethodObtaining(DEFAULT_TEST_CONTENT)
			.build();

		assertThat(
			httpResourceChecker.getAllContent(),
			is(equalTo(DEFAULT_TEST_CONTENT))
		);
	}

	@Test
	public void getAllContentReturnsAnEmptyStringWhenHttpURLConnectionCannotGetInputStream() {
		HTTPResourceChecker httpResourceChecker = new HTTPResourceCheckerBuilder(resource)
			.withMockGetHTTPURLConnectionMethodUsingBadInputStream()
			.build();

		assertThat(
			httpResourceChecker.getAllContent(),
			is(equalTo(""))
		);
	}

	@Test
	public void getLastModifiedDateTimeReturnsExpectedDate() throws IOException {
		final long expectedDateInSecondsSinceUnixEpoch = 1598293841537L;
		final String expectedDate = "Mon Aug 24 14:30:41";
		final DateTimeFormatter dateTimeFormat = DateTimeFormatter.ofPattern("E MMM d HH:mm:ss");

		HTTPResourceChecker httpResourceChecker = Mockito.spy(HTTPResourceChecker.class);
		Mockito.doReturn(httpURLConnection).when(httpResourceChecker).getHttpURLConnection();
		Mockito.doReturn(expectedDateInSecondsSinceUnixEpoch).when(httpURLConnection).getLastModified();

		assertThat(
			httpResourceChecker.getLastModifiedDateTime().format(dateTimeFormat),
			is(equalTo(expectedDate))
		);
	}

	@Test
	public void getLastModifiedDateTimeReturnsNullWhenAnIOExceptionFromHttpURLConnectionIsThrown() throws IOException {
		final URL mockResourceURL = new URL("http://fake.org");
		HTTPResourceChecker httpResourceChecker = Mockito.spy(HTTPResourceChecker.class);

		Mockito.doReturn(mockResourceURL).when(httpResourceChecker).getResourceURL();
		Mockito.doThrow(IOException.class).when(httpResourceChecker).getHttpURLConnection();

		assertThat(
			httpResourceChecker.getLastModifiedDateTime(),
			is(nullValue())
		);
	}

	@Test
	public void multipleHasNextCallsOnContentChunkGeneratorReturnsTrue() {
		Iterator<String> contentChunkGeneratorIterator = new ContentChunkGenerator(createTestInputStream()).iterator();

		final int numberOfCallsToHasNextMethod = 3;
		for (int i = 1 ; i <= numberOfCallsToHasNextMethod; i++) {
			assertThat(
				"Number " + i + " call to hasNext() method",
				contentChunkGeneratorIterator.hasNext(),
				is(equalTo(true))
			);
		}
	}

	@Test
	public void nextReturnsTheFirstValueAfterNoCallsToHasNextForContentChunkGeneratorIterator() {
		Iterator<String> contentChunkGeneratorIterator = new ContentChunkGenerator(createTestInputStream()).iterator();

		assertThat(contentChunkGeneratorIterator.next(), is(equalTo(DEFAULT_TEST_CONTENT)));
	}

	@Test
	public void nextReturnsTheFirstValueAsAfterSingleCallToHasNextForContentChunkGeneratorIterator() {
		Iterator<String> contentChunkGeneratorIterator = new ContentChunkGenerator(createTestInputStream()).iterator();

		contentChunkGeneratorIterator.hasNext();

		assertThat(contentChunkGeneratorIterator.next(), is(equalTo(DEFAULT_TEST_CONTENT)));
	}

	@Test
	public void nextReturnsTheFirstValueAfterMultipleCallsToHasNextForContentChunkGeneratorIterator() {
		Iterator<String> contentChunkGeneratorIterator =  new ContentChunkGenerator(createTestInputStream()).iterator();

		contentChunkGeneratorIterator.hasNext();
		contentChunkGeneratorIterator.hasNext();
		contentChunkGeneratorIterator.hasNext();

		assertThat(contentChunkGeneratorIterator.next(), is(equalTo(DEFAULT_TEST_CONTENT)));
	}

	@Test
	public void noSuchElementExceptionThrownIfNoValueAvailableWhenCallingNextForContentChunkGeneratorIterator() {
		final String EMPTY_CONTENT_STRING = "";
		Iterator<String> contentChunkGeneratorIterator =
			new ContentChunkGenerator(createTestInputStream(EMPTY_CONTENT_STRING)).iterator();

		NoSuchElementException thrown = assertThrows(
			NoSuchElementException.class,
			contentChunkGeneratorIterator::next,
			"Expected ContentChunkGeneratorIterator to throw a NoSuchElementException when no value is available " +
				" when calling the next() method, but it didn't"
		);

		assertThat(thrown.getMessage(), containsString("No more elements available"));
	}

	@Test
	public void hasNextReturnsFalseIfInputStreamThrowsAnIOExceptionOnReading() throws IOException {
		InputStream inputStream = Mockito.mock(InputStream.class);
		Mockito.doThrow(IOException.class).when(inputStream).read(any(), anyInt(), anyInt());

		Iterator<String> contentChunkGeneratorIterator = new ContentChunkGenerator(inputStream).iterator();

		assertThat(contentChunkGeneratorIterator.hasNext(), is(equalTo(false)));
	}

	@Test
	public void hasNextReturnsFalseIfInputStreamThrowsAnIOExceptionOnReadingAndClosing() throws IOException {
		InputStream inputStream = Mockito.mock(InputStream.class);
		Mockito.doThrow(IOException.class).when(inputStream).read(any(), anyInt(), anyInt());
		Mockito.doThrow(IOException.class).when(inputStream).close();

		Iterator<String> contentChunkGeneratorIterator = new ContentChunkGenerator(inputStream).iterator();

		assertThat(contentChunkGeneratorIterator.hasNext(), is(equalTo(false)));
	}


	private InputStream createTestInputStream() {
		return createTestInputStream(DEFAULT_TEST_CONTENT);
	}

	private InputStream createTestInputStream(String testContent) {
		return IOUtils.toInputStream(testContent, StandardCharsets.UTF_8);
	}
}
