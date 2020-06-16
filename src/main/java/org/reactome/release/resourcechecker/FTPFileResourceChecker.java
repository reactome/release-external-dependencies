package org.reactome.release.resourcechecker;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPConnectionClosedException;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.reactome.release.Resource;
import org.reactome.release.ResourceChecker;

public class FTPFileResourceChecker extends ResourceChecker {
	private final Logger logger = LogManager.getLogger();

	private static final String DEFAULT_USER_NAME = "anonymous";

	// It is common practice to give an e-mail for the "anonymous" password
	// as a courtesy to the FTP server's site operators so they know who is
	// accessing their service (https://stackoverflow.com/a/20031581)
	private static final String DEFAULT_PASSWORD  = "help@reactome.org";

	private String ftpServer;
	private String ftpFilePath;

	private String userName;
	private String password;

	public FTPFileResourceChecker(Resource resource) {
		this(resource, DEFAULT_USER_NAME, DEFAULT_PASSWORD);
	}

	public FTPFileResourceChecker(Resource resource, String userName, String password) {
		super(resource);
		this.ftpServer = resource.getResourceURL().getHost();
		this.ftpFilePath = resource.getResourceURL().getPath();

		this.userName = userName;
		this.password = password;
	}

	public boolean resourceExists() throws IOException {
		final String ftpCloseConnectionErrorMessage = "Unable to close connection to FTP Server " + getFtpServer();

		FTPClient ftpClient = new FTPClient();
		try {
			ftpClient.connect(getFtpServer());
			ftpClient.enterLocalPassiveMode();
			if (ftpClient.login(getUserName(), getPassword())) {
				logger.info("Login successful to " + getFtpServer());
			} else {
				logger.error("Login to " + getFtpServer() + " failed");
				return false;
			}

			if (hasSingleNonZeroSizeFile(ftpClient, getFtpFilePath())) {
				ftpClient.logout();
				ftpClient.disconnect();
				return true;
			}
		} catch (FTPConnectionClosedException e) {
			logger.error(ftpCloseConnectionErrorMessage, e);
		} finally {
			try {
				ftpClient.disconnect();
			} catch (FTPConnectionClosedException e) {
				logger.error(ftpCloseConnectionErrorMessage, e);
			}
		}
		return false;
	}

	private boolean hasSingleNonZeroSizeFile(FTPClient ftpClient, String ftpFilePath) throws IOException {
		List<FTPFile> ftpFiles = Arrays.asList(
			ftpClient.listFiles(ftpFilePath)
		);

		System.out.println(ftpFilePath + " has file size of " + ftpFiles.get(0).getSize() + " bytes");
		return ftpFiles.size() == 1 && ftpFiles.get(0).getSize() > 0;
	}

	public String getFtpServer() {
		return ftpServer;
	}

	public String getFtpFilePath() {
		return ftpFilePath;
	}

	public String getUserName() {
		return userName;
	}

	public String getPassword() {
		return password;
	}
}
