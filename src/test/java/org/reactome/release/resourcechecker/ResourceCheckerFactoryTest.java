package org.reactome.release.resourcechecker;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.net.MalformedURLException;
import java.net.URL;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.reactome.release.Resource;
import org.reactome.release.Resource.ResourceType;

public class ResourceCheckerFactoryTest {
	@Mock
	private static Resource resource;

	@BeforeEach
	public void initMocks() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void fileResourceWithFTPProtocolReturnsFTPFileResourceChecker() throws MalformedURLException {
		final URL mockFTPURL = new URL("ftp://someftpserver.org/someftpresource.txt");

		Mockito.when(resource.getResourceType()).thenReturn(ResourceType.FILE);
		Mockito.when(resource.getResourceURL()).thenReturn(mockFTPURL);

		ResourceChecker resourceChecker = ResourceCheckerFactory.getInstance(resource);

		assertThat(resourceChecker, is(instanceOf(FTPFileResourceChecker.class)));
	}

	@Test
	public void fileResourceWithHTTPProtocolReturnsHTTPFileResourceChecker() throws MalformedURLException {
		final URL mockHTTPURL = new URL("http://someserver.org/someresource.txt");

		Mockito.when(resource.getResourceType()).thenReturn(ResourceType.FILE);
		Mockito.when(resource.getResourceURL()).thenReturn(mockHTTPURL);

		ResourceChecker resourceChecker = ResourceCheckerFactory.getInstance(resource);

		assertThat(resourceChecker, is(instanceOf(HTTPResourceChecker.class)));
	}

	@Test
	public void fileResourceWithHTTPSProtocolReturnsHTTPFileResourceChecker() throws MalformedURLException {
		final URL mockHTTPSURL = new URL("https://someserver.org/someresource.txt");

		Mockito.when(resource.getResourceType()).thenReturn(ResourceType.FILE);
		Mockito.when(resource.getResourceURL()).thenReturn(mockHTTPSURL);

		ResourceChecker resourceChecker = ResourceCheckerFactory.getInstance(resource);

		assertThat(resourceChecker, is(instanceOf(HTTPResourceChecker.class)));
	}

	@Test
	public void fileResourceWithUnrecognizedProtocolThrowsIllegalArgumentException() throws MalformedURLException {
		final String resourceName = "someresource.txt";
		final String unknownProtocol = "file";
		final URL mockHTTPURL = new URL(unknownProtocol + "://someserver.org/" + resourceName);

		Mockito.when(resource.getResourceName()).thenReturn(resourceName);
		Mockito.when(resource.getResourceType()).thenReturn(ResourceType.FILE);
		Mockito.when(resource.getResourceURL()).thenReturn(mockHTTPURL);

		IllegalArgumentException thrown = assertThrows(
			IllegalArgumentException.class,
			() -> ResourceCheckerFactory.getInstance(resource),
			"Expected a resource with type of FILE and an unknown protocol (i.e. not http(s) or ftp) to throw an "
				+ "IllegalArgumentException, but it didn't"
		);

		assertThat(
			thrown.getMessage(),
			containsString(
				"The protocol " + unknownProtocol + " to check a file resource is not supported for " + resourceName
			)
		);
	}

	@Test
	public void restEndpointResourceReturnsRESTfulAPIResourceChecker() {
		Mockito.when(resource.getResourceType()).thenReturn(ResourceType.REST_ENDPOINT);

		ResourceChecker resourceChecker = ResourceCheckerFactory.getInstance(resource);

		assertThat(resourceChecker, is(instanceOf(RESTfulAPIResourceChecker.class)));
	}

	@Test
	public void webPageResourceReturnsWebPageResourceChecker() {
		Mockito.when(resource.getResourceType()).thenReturn(ResourceType.WEB_PAGE);

		ResourceChecker resourceChecker = ResourceCheckerFactory.getInstance(resource);

		assertThat(resourceChecker, is(instanceOf(WebPageResourceChecker.class)));
	}

	@Test
	@Disabled
	/*
	TODO This test currently does not work because Mockito can not mock an "enum" type.  Experimented with PowerMock,
	 but need to do more research to determine how to properly test when an unexpected enum value is provided for
	 the ResourceType
	 */
	public void unrecognizedResourceTypeThrowsIllegalArgumentException() {
		final String unrecognizedResourceType = "UNRECOGNIZED_RESOURCE_TYPE";

		ResourceType resourceType = Mockito.mock(ResourceType.class);

		Mockito.when(resourceType.toString()).thenReturn(unrecognizedResourceType);
		Mockito.when(resource.getResourceType()).thenReturn(resourceType);

		IllegalArgumentException thrown = assertThrows(
			IllegalArgumentException.class,
			() -> ResourceCheckerFactory.getInstance(resource),
			"Expected a resource with an unrecognized type to throw an IllegalArgumentException, but it didn't"
		);

		assertThat(
			thrown.getMessage(),
			containsString("The type " + unrecognizedResourceType + " is not recognized")
		);
	}
}
