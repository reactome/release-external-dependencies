package org.reactome.release.resourcechecker;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyString;
import static org.reactome.release.testutilities.ResourceTestUtils.getResource;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class FTPFileResourceCheckerTest {
	@Mock
	private FTPClient ftpClient;
	@Mock
	private FTPFile ftpFile;

	@InjectMocks
	private FTPFileResourceChecker ftpFileResourceChecker;

	private FTPFile[] ftpFiles;

	@BeforeEach
	public void initializeFTPFileResourceCheckerAndMocks() throws IOException {
		initializeFTPFileResourceChecker();
		initializeMocks();
		initializeFTPFilesArrayWithMockFTPFile();
	}

	@Test
	public void resourceExistsIfFTPFileNameExists() {
		Mockito.when(ftpFile.getName()).thenReturn("rhea2reactome.tsv");

		assertThat(
			ftpFileResourceChecker.resourceExists(),
			is(equalTo(true))
		);
	}

	@Test
	public void resourceDoesNotExistWhenFileNameReturnsNullOnFTPServer() {
		Mockito.when(ftpFile.getName()).thenReturn(null);

		assertThat(
			ftpFileResourceChecker.resourceExists(),
			is(equalTo(false))
		);
	}

	@Test
	public void resourceDoesNotExistWhenFileNameReturnsEmptyStringOnFTPServer() {
		Mockito.when(ftpFile.getName()).thenReturn("");

		assertThat(
			ftpFileResourceChecker.resourceExists(),
			is(equalTo(false))
		);
	}

	@Test
	public void fileSizeFromFTPServerIsCorrect() {
		final long expectedFileSize = 1000;

		Mockito.when(ftpFile.getSize()).thenReturn(expectedFileSize);

		assertThat(
			ftpFileResourceChecker.getFileSize(),
			is(equalTo(expectedFileSize))
		);
	}

	@Test
	public void correctURLIsReturnedForFTPServerHost() {
		assertThat(
			ftpFileResourceChecker.getFtpServer(),
			is(equalTo("ftp.ebi.ac.uk"))
		);
	}

	@Test
	public void correctFilePathIsReturnedForFileLocationOnFTPServer() {
		assertThat(
			ftpFileResourceChecker.getFtpFilePath(),
			is(equalTo("/pub/databases/rhea/tsv/rhea2reactome.tsv"))
		);
	}

	private void initializeFTPFileResourceChecker() throws FileNotFoundException {
		this.ftpFileResourceChecker = new FTPFileResourceChecker(getResource());
	}

	private void initializeMocks() throws IOException {
		MockitoAnnotations.initMocks(this);
		setUpFTPClientMockBehaviour();
	}

	private void setUpFTPClientMockBehaviour() throws IOException {
		Mockito.doNothing().when(ftpClient).connect(anyString());
		Mockito.doNothing().when(ftpClient).enterLocalPassiveMode();
		Mockito.when(
			ftpClient.login(
				ftpFileResourceChecker.getUserName(),
				ftpFileResourceChecker.getPassword()
			)
		).thenReturn(true);
		Mockito.when(ftpClient.listFiles(anyString())).thenReturn(ftpFiles);
		Mockito.when(ftpClient.logout()).thenReturn(true);
		Mockito.doNothing().when(ftpClient).disconnect();
	}

	private void initializeFTPFilesArrayWithMockFTPFile() {
		ftpFiles = new FTPFile[]{ftpFile};
	}
}
