package org.reactome.release.resourcechecker;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.reactome.release.testutilities.ResourceTestUtils.getResource;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class FTPFileResourceCheckerIT {
	private FTPFileResourceChecker ftpFileResourceChecker;

	@BeforeEach
	public void initializeFTPFileResourceChecker() throws FileNotFoundException {
		this.ftpFileResourceChecker = new FTPFileResourceChecker(getResource());
	}

	@Test
	public void ftpFileContentsAreProperlySaved() throws IOException {
		final String expectedFileHeader = "RHEA_ID\tDIRECTION\tMASTER_ID\tID\n";
		final Path testDirectoryForSavingFTPFileContents = Paths.get("src", "test", "resources", "temp_test_files");
		final Path testFileForSavingFTPFileContents = testDirectoryForSavingFTPFileContents.resolve("ftp_file_contents.txt");
		Files.createDirectories(testDirectoryForSavingFTPFileContents);

		this.ftpFileResourceChecker.saveFileContents(testFileForSavingFTPFileContents);

		assertThat(
			getFileContents(testFileForSavingFTPFileContents),
			containsString(expectedFileHeader)
		);

		// Clean-up
		Files.deleteIfExists(testFileForSavingFTPFileContents);
		Files.deleteIfExists(testDirectoryForSavingFTPFileContents);
	}

	private String getFileContents(Path testFileForSavingFTPFileContents) throws IOException {
		return new String(Files.readAllBytes(testFileForSavingFTPFileContents));
	}
}
