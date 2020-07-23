package org.reactome.release.resourcechecker;

import java.io.IOException;
import java.nio.file.Path;

public interface FileResourceChecker extends ResourceChecker {
	void saveFileContents(Path fileDestination) throws IOException;

	long getFileSize();

	/**
	 * Returns the size of the FTP file being checked in bytes (0 if the file does not exist)
	 *
	 * @param byteUnit Type of unit to retrieve file size as (i.e. BYTE, KILOBYTE, MEGABYTE, GIGABYTE)
	 * @return Returns the FTP file's size in byte unit
	 */
	default String getFileSizeAs(ByteUnit byteUnit) {
		long sizeInBytes = getFileSize();

		switch (byteUnit) {
			case GIGABYTE:
				return sizeInBytes / (float) Math.pow(1024, 3) + " GB";
			case MEGABYTE:
				return sizeInBytes / (float) Math.pow(1024, 2) + " MB";
			case KILOBYTE:
				return sizeInBytes / 1024.0f + " KB";
			case BYTE:
				return Long.toString(sizeInBytes);
			default:
				throw new RuntimeException("Unrecognized byteUnit " + byteUnit.toString());
		}
	}

	enum ByteUnit {
		BYTE,
		KILOBYTE,
		MEGABYTE,
		GIGABYTE
	}
}
