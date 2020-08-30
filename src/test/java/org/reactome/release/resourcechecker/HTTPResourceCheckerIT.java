package org.reactome.release.resourcechecker;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.reactome.release.Resource;

public class HTTPResourceCheckerIT {
	private HTTPResourceChecker httpResourceChecker;

	@Mock
	private Resource resource;

	@BeforeEach
	public void initializeHTTPResourceChecker() throws MalformedURLException {
		MockitoAnnotations.initMocks(this);
		Mockito.when(resource.getResourceURL()).thenReturn(getTestResourceURL());

		httpResourceChecker = new HTTPResourceChecker() {
			@Override
			public Resource getResource() {
				return resource;
			}
		};
	}

	@Test
	public void contentIteratorObtainedSuccessfully() throws IOException {
		final int expectedNumberOfBytes = 4096;
		final String expectedSubString = "Gene";

		Iterator<String> contentIterator = httpResourceChecker.getContentIterator();

		String firstChunkOfContent = contentIterator.next();

		assertThat(firstChunkOfContent.getBytes().length, is(equalTo(expectedNumberOfBytes)));
		assertThat(firstChunkOfContent, containsString(expectedSubString));
	}

	@Test
	public void httpURLConnectionWithRequestPropertiesIsAchievedSuccessfully() throws IOException {
		HttpURLConnection httpURLConnection = httpResourceChecker.getHttpURLConnection(
			"GET", getTestRequestProperties()
		);

		assertThat(httpURLConnection.getResponseCode(), is(equalTo(HttpURLConnection.HTTP_OK)));
	}

	private Map<String, String> getTestRequestProperties() {
		Map<String, String> testRequestProperties = new HashMap<>();
		testRequestProperties.put("Accept-Charset", "utf-8");
		return testRequestProperties;
	}

	private URL getTestResourceURL() {
		try {
			return new URL("http://www.informatics.jax.org/downloads/reports/HGNC_homologene.rpt");
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
	}
}
