package org.reactome.release.resourcechecker;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyString;
import static org.reactome.release.testutilities.ResourceTestUtils.getResource;

import java.io.IOException;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.reactome.release.Resource;

public class FTPFileResourceCheckerTest {
	@Mock
	private FTPClient ftpClient;

	@Mock
	private FTPFile ftpFile;

	@Mock
	private Resource resource;

	private FTPFileResourceChecker ftpFileResourceChecker;

	@BeforeEach
	public void initializeFTPFileResourceCheckerAndMocks() {
		ftpFileResourceChecker = Mockito.spy(new FTPFileResourceChecker(resource));
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void resourceExistsIfFTPFileNameExists() throws IOException {
		mockExpectedFTPInteractions();

		Mockito.doReturn("rhea2reactome.tsv").when(ftpFile).getName();

		assertThat(ftpFileResourceChecker.resourceExists(), is(equalTo(true)));
	}

	@Test
	public void resourceDoesNotExistWhenFileNameReturnsNullOnFTPServer() throws IOException {
		mockExpectedFTPInteractions();

		Mockito.doReturn(null).when(ftpFile).getName();

		assertThat(ftpFileResourceChecker.resourceExists(), is(equalTo(false)));
	}

	@Test
	public void resourceDoesNotExistWhenFileNameReturnsEmptyStringOnFTPServer() throws IOException {
		mockExpectedFTPInteractions();

		Mockito.doReturn("").when(ftpFile).getName();

		assertThat(ftpFileResourceChecker.resourceExists(), is(equalTo(false)));
	}

	@Test
	public void fileSizeFromFTPServerIsCorrect() throws IOException {
		final long expectedFileSize = 1000;
		mockExpectedFTPInteractions();

		Mockito.when(ftpFile.getSize()).thenReturn(expectedFileSize);

		assertThat(ftpFileResourceChecker.getFileSize(), is(equalTo(expectedFileSize)));
	}

	@Test
	public void correctURLIsReturnedForFTPServerHost() {
		final String expectedFTPServerName = "ftp.ebi.ac.uk";

		Mockito.doReturn(getResource().getResourceURL()).when(ftpFileResourceChecker).getResourceURL();

		assertThat(ftpFileResourceChecker.getFtpServerName(), is(equalTo(expectedFTPServerName)));
	}

	@Test
	public void correctFilePathIsReturnedForFileLocationOnFTPServer() {
		final String expectedFTPFilePath = "/pub/databases/rhea/tsv/rhea2reactome.tsv";

		Mockito.doReturn(getResource().getResourceURL()).when(ftpFileResourceChecker).getResourceURL();

		assertThat(ftpFileResourceChecker.getFtpFilePath(), is(equalTo(expectedFTPFilePath)));
	}

	@Test
	public void ftpFileIsEmptyWhenAnIOExceptionIsThrownRetrievingTheFileSize() throws IOException {
		mockExpectedFTPInteractionsButWithAnIOExceptionOnFTPClientConnecting();

		assertThatFTPFileIsEmpty();
	}

	@Test
	public void ftpFileIsEmptyWhenMultipleFilesAreReturnedByTheFTPServer() throws IOException {
		mockExpectedFTPInteractionsWhenReturningMultipleFTPFiles();

		assertThatFTPFileIsEmpty();
	}

	private void mockExpectedFTPInteractions() throws IOException {
		mockFTPClientReturningSingleFTPFile();
		mockDummyServerNameAndFTPFilePath();
	}

	private void mockExpectedFTPInteractionsButWithAnIOExceptionOnFTPClientConnecting() throws IOException {
		mockExpectedFTPInteractions();
		Mockito.doThrow(IOException.class).when(ftpClient).connect(anyString());
	}

	private void mockExpectedFTPInteractionsWhenReturningMultipleFTPFiles() throws IOException {
		mockFTPClientReturningMultipleFTPFiles();
		mockDummyServerNameAndFTPFilePath();
	}

	private void mockFTPClientReturningSingleFTPFile() throws IOException {
		mockFTPClient(new FTPFile[]{ftpFile});
	}

	private void mockFTPClientReturningMultipleFTPFiles() throws IOException {
		FTPFile secondMockFTPFile = Mockito.mock(FTPFile.class);
		mockFTPClient(new FTPFile[]{ftpFile, secondMockFTPFile});
	}

	private void mockFTPClient(FTPFile[] ftpFiles) throws IOException {
		Mockito.when(ftpFileResourceChecker.getFTPClient()).thenReturn(ftpClient);
		Mockito.doNothing().when(ftpClient).connect(anyString());
		Mockito.doNothing().when(ftpClient).enterLocalPassiveMode();
		Mockito.when(
			ftpClient.login(
				ftpFileResourceChecker.getUserName(),
				ftpFileResourceChecker.getPassword()
			)
		).thenReturn(true);
		Mockito.doReturn(ftpFiles).when(ftpClient).listFiles(anyString());
		Mockito.when(ftpClient.logout()).thenReturn(true);
		Mockito.doNothing().when(ftpClient).disconnect();
	}

	private void mockDummyServerNameAndFTPFilePath() {
		Mockito.doReturn("dummy_server_name").when(ftpFileResourceChecker).getFtpServerName();
		Mockito.doReturn("dummy_ftp_file_path").when(ftpFileResourceChecker).getFtpFilePath();
	}

	private void assertThatFTPFileIsEmpty() {
		final long undefinedFileSize = -1L;

		assertThat(ftpFileResourceChecker.getFileSize(), is(equalTo(undefinedFileSize)));
		assertThat(ftpFileResourceChecker.resourceExists(), is(equalTo(false)));
	}
}
