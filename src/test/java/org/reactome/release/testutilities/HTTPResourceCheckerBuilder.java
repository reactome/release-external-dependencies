package org.reactome.release.testutilities;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import org.apache.commons.io.IOUtils;
import org.mockito.Mockito;
import org.mockito.stubbing.OngoingStubbing;
import org.reactome.release.Resource;
import org.reactome.release.resourcechecker.HTTPResourceChecker;

public class HTTPResourceCheckerBuilder {
	private Resource resource;
	private BiFunction<String, Map<String, String>, HttpURLConnection> mockGetHttpURLConnectionFunction;
	private Consumer<HttpURLConnection> mockGetResponseCodeFunction;

	public HTTPResourceCheckerBuilder(Resource resource) {
		this.resource = resource;
		this.mockGetResponseCodeFunction = getMockGetResponseCodeFunctionReturningOKResponseCode();
		this.mockGetHttpURLConnectionFunction = getMockGetHttpUrlConnectionMethod("");
	}

	public HTTPResourceCheckerBuilder withMockGetHTTPURLConnectionMethodObtaining(String resourceContent) {
		this.mockGetHttpURLConnectionFunction = getMockGetHttpUrlConnectionMethod(resourceContent);
		return this;
	}

	public HTTPResourceCheckerBuilder withMockGetHTTPURLConnectionMethodUsingBadInputStream() {
		this.mockGetHttpURLConnectionFunction = getMockGetHttpUrlConnectionMethodWithBadInputStream();
		return this;
	}

	public HTTPResourceCheckerBuilder withMockGetResponseCodeMethodReturningNoResponseCode() {
		this.mockGetResponseCodeFunction = getMockGetResponseCodeFunctionReturningNoResponseCode();
		return this;
	}

	public HTTPResourceChecker build() {
		return new HTTPResourceChecker() {
			@Override
			public Resource getResource() {
				return resource;
			}

			@Override
			public HttpURLConnection getHttpURLConnection(String requestMethod, Map<String, String> requestProperties) {
				HttpURLConnection httpURLConnection = mockGetHttpURLConnectionFunction.apply(requestMethod, requestProperties);
				mockGetResponseCodeFunction.accept(httpURLConnection);
				return httpURLConnection;
			}
		};
	}

	private BiFunction<String, Map<String, String>, HttpURLConnection> getMockGetHttpUrlConnectionMethod(String mockHttpResourceContent) {
		return getMockGetHttpUrlConnectionMethod(
			(inputStream) -> Mockito.when(inputStream).thenReturn(
				IOUtils.toInputStream(mockHttpResourceContent, StandardCharsets.UTF_8)
			)
		);
	}

	private BiFunction<String, Map<String, String>, HttpURLConnection> getMockGetHttpUrlConnectionMethodWithBadInputStream() {
		return getMockGetHttpUrlConnectionMethod(
			(inputStream) -> Mockito.when(inputStream).thenThrow(IOException.class)
		);
	}

	private BiFunction<String, Map<String, String>, HttpURLConnection> getMockGetHttpUrlConnectionMethod
		(Consumer<InputStream> mockInputStreamConsumer) {
		return new BiFunction<String, Map<String, String>, HttpURLConnection>() {
			@Override
			public HttpURLConnection apply(String s, Map<String, String> stringStringMap) {
				HttpURLConnection httpURLConnection = Mockito.mock(HttpURLConnection.class);

				mockInputStreamConsumer.accept(getInputStream(httpURLConnection));

				return httpURLConnection;
			}

			private InputStream getInputStream(HttpURLConnection httpURLConnection) {
				try {
					return httpURLConnection.getInputStream();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		};
	}

	private Consumer<HttpURLConnection> getMockGetResponseCodeFunctionReturningOKResponseCode() {
		return httpURLConnection -> whenGetResponseCode(httpURLConnection).thenReturn(HttpURLConnection.HTTP_OK);
	}

	private Consumer<HttpURLConnection> getMockGetResponseCodeFunctionReturningNoResponseCode() {
		return httpURLConnection -> whenGetResponseCode(httpURLConnection).thenThrow(IOException.class);
	}

	private OngoingStubbing<Integer> whenGetResponseCode(HttpURLConnection httpURLConnection) {
		try {
			return Mockito.when(httpURLConnection.getResponseCode());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
