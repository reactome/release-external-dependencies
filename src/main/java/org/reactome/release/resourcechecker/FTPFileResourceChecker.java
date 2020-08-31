package org.reactome.release.resourcechecker;

import java.io.FileOutputStream;
import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Path;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import org.reactome.release.Resource;

public class FTPFileResourceChecker implements FileResourceChecker {
	private static final String DEFAULT_USER_NAME = "anonymous";

	// It is common practice to give an e-mail for the "anonymous" password
	// as a courtesy to the FTP server's site operators so they know who is
	// accessing their service (https://stackoverflow.com/a/20031581)
	private static final String DEFAULT_PASSWORD  = "help@reactome.org";

	private String userName;
	private String password;

	private Resource resource;
	private FTPFile ftpFile;
	private FTPClient ftpClient;

	/**
	 * Constructs an FTPFileResourceChecker object for the given resource.  Connection to the relevant FTP Server will
	 * be passive and as an anonymous user
	 *
	 * @param resource The FTP file resource to be checked
	 */
	public FTPFileResourceChecker(Resource resource) {
		this(resource, DEFAULT_USER_NAME, DEFAULT_PASSWORD);
	}

	/**
	 * Constructs an FTPFileResourceChecker object for the given resource.  Connection to the relevant FTP server will
	 * be passive and using the provided user name and password credentials.
	 *
	 * @param resource The FTP file resource to be checked
	 * @param userName User name used to connect to the FTP server
	 * @param password Password used to connect to the FTP server
	 */
	public FTPFileResourceChecker(Resource resource, String userName, String password) {
		this.resource = resource;
		this.userName = userName;
		this.password = password;
	}

	@Override
	public Resource getResource() {
		return this.resource;
	}

	/**
	 * Returns <code>true</code> if a unique resource was found on the FTP server and path provided by the Resource object;
	 * <code>false</code> otherwise
	 *
	 * @return True if the FTP file exists on the FTP server; false otherwise
	 */
	@Override
	public boolean resourceExists() {
		return getFtpFile().getName() != null && !getFtpFile().getName().isEmpty();
	}

	@Override
	public void saveFileContents(Path downloadDestination) throws IOException {
		FTPClient ftpClient = connectToFTPClient();

		Files.deleteIfExists(downloadDestination);
		ftpClient.retrieveFile(
			getFtpFilePath(),
			new FileOutputStream(downloadDestination.toFile())
		);

		logoutAndDisconnect(ftpClient);
	}

	/**
	 * Returns the size of the FTP file being checked in bytes (-1 if the file does not exist)
	 *
	 * @return Returns the FTP file's size in bytes
	 */
	@Override
	public long getFileSize() {
		return getFtpFile().getSize();
	}

	/**
	 * Returns the name of the host server on which the FTP file being checked resides (e.g. ftp.uniprot.org)
	 *
	 * @return Name of the FTP host server
	 */
	public String getFtpServerName() {
		return this.getResourceURL().getHost();
	}

	/**
	 * Returns the path to the FTP file on its host server as a String
	 *
	 * @return Remote path of the FTP file as a String
	 */
	public String getFtpFilePath() {
		return this.getResourceURL().getPath();
	}

	/**
	 * Returns the user name used to connect to the FTP server on which the file being checked resides (Defaults to
	 * "anonymous" if not specified when the instance, on which this method is called, was constructed
	 *
	 * @return User name to connect to FTP server
	 *
	 * @see #getPassword()
	 */
	public String getUserName() {
		return this.userName;
	}

	/**
	 * Returns the password used to connect to the FTP server on which the file being checked resides (Defaults to
	 * "help@reactome.org" if not specified (i.e. using the "anonymous" user name) when the instance, on which this
	 * method is called, was constructed.
	 *
	 * NOTE: An e-mail address is used as the password when connecting anonymously to an FTP server as a courtesy
	 * to the FTP server's site operators so they know who is accessing their service
	 * (see https://stackoverflow.com/a/20031581)
	 *
	 * @return User name to connect to FTP server
	 *
	 * @see #getUserName()
	 */
	public String getPassword() {
		return this.password;
	}

	FTPClient getFTPClient() {
		if (this.ftpClient == null) {
			this.ftpClient = new FTPClient();
		}

		return this.ftpClient;
	}

	private FTPFile getFtpFile() {
		if (this.ftpFile == null) {
			try {
				this.ftpFile = retrieveFtpFile();
			} catch (IOException e) {
				logger.error("Unable to retrieve file " + getFtpFilePath() + " from FTP Server " + getFtpServerName(), e);
				this.ftpFile = new FTPFile();
			}
		}

		return this.ftpFile;
	}

	private FTPFile retrieveFtpFile() throws IOException {
		FTPClient ftpClient = connectToFTPClient();

		List<FTPFile> ftpFiles = Arrays.asList(
			ftpClient.listFiles(getFtpFilePath())
		);

		logoutAndDisconnect(ftpClient);

		if (ftpFiles.size() == 1) {
			return ftpFiles.get(0);
		} else {
			logger.error(getErrorMessageForNonUniqueFile(ftpFiles, getFtpFilePath()));
			return new FTPFile();
		}
	}

	private FTPClient connectToFTPClient() throws IOException {
		FTPClient ftpClient = getFTPClient();

		ftpClient.connect(getFtpServerName());
		ftpClient.enterLocalPassiveMode();

		if (ftpClient.login(getUserName(), getPassword())) {
			logger.info("Login successful to " + getFtpServerName());
		} else {
			logger.error("Login to " + getFtpServerName() + " failed");
		}

		return ftpClient;
	}

	private void logoutAndDisconnect(FTPClient ftpClient) {
		final String ftpCloseConnectionErrorMessage = "Unable to close connection to FTP Server " + getFtpServerName();

		try {
			ftpClient.logout();
			ftpClient.disconnect();
		} catch (IOException e) {
			logger.error(ftpCloseConnectionErrorMessage, e);
		}
	}

	private String getErrorMessageForNonUniqueFile(List<FTPFile> ftpFiles, String ftpFilePath) {
		return ftpFiles.isEmpty() ? "No file " : "Multiple files " +
			"found on " + getFtpServerName() + " for path " + ftpFilePath;
	}
}