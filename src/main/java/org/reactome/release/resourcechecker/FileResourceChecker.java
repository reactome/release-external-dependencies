package org.reactome.release.resourcechecker;

import com.google.gson.JsonObject;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;

public interface FileResourceChecker extends ResourceChecker {
	void saveFileContents(Path fileDestination) throws IOException;

	long getFileSize();

	/**
	 * Returns true if the current file size has not dropped more than an acceptable percentage when compared with
	 * the previous file size.
	 * @param previousFileSize Last known acceptable file size in bytes
	 * @param acceptablePercentageDrop Percentage of the drop in file size which is acceptable between the current and
	 * previous file size
	 * @return True if the file size has not dropped more than the acceptable percentage; False otherwise
	 * @see #isFileSizeAcceptable(long)
	 */
	default boolean isFileSizeAcceptable(long previousFileSize, double acceptablePercentageDrop) {
		long differenceInFileSize = getFileSize() - previousFileSize;
		double percentChangeInFileSize = differenceInFileSize * 100.0d / previousFileSize;

		return percentChangeInFileSize >= 0 ||
			Math.abs(percentChangeInFileSize) < acceptablePercentageDrop;
	}

	/**
	 * Returns true if the current file size has not dropped more than an acceptable percentage (default of
	 * 5.0%) when compared with the previous file size.
	 * @param previousFileSize Last known acceptable file size in bytes
	 * @return True if the file size has not dropped more than the acceptable percentage; False otherwise
	 * @see #isFileSizeAcceptable(long, double)
	 */
	default boolean isFileSizeAcceptable(long previousFileSize) {
		final double acceptableFileSizePercentageDrop = 5.0;

		return isFileSizeAcceptable(previousFileSize, acceptableFileSizePercentageDrop);
	}

	@Override
	default String getReport() {
		JsonObject reportJson = new JsonObject();
		reportJson.addProperty("Passed Checks", resourcePassesAllChecks());
		reportJson.addProperty("Resource Exists", resourceExists());
		reportJson.add("File Size", getFileSizeReport(getResource().getExpectedFileSizeInBytes()));
		return reportJson.toString();
	}

	default JsonObject getFileSizeReport(long previousFileSize) {
		JsonObject fileSizeReportJson = new JsonObject();
		fileSizeReportJson.addProperty("File Size Found", getFileSizeFound());
		fileSizeReportJson.addProperty("File Size Acceptable",  isFileSizeAcceptable(previousFileSize));
		return fileSizeReportJson;
	}

	default String getFileSizeFound() {
		long fileSizeInBytes = getFileSize();

		return String.format(
			"%s (%d bytes)",
			ByteUnit.getHumanReadableFileSize(fileSizeInBytes),
			fileSizeInBytes
		);
	}

	@Override
	default boolean resourcePassesAllChecks() {
		return resourceExists() && isFileSizeAcceptable(getResource().getExpectedFileSizeInBytes());
	}

	enum ByteUnit {
		BYTE(0, ""),
		KILOBYTE(1,"KB"),
		MEGABYTE(2,"MB"),
		GIGABYTE(3,"GB");

		private static final double BYTE_UNIT_CONVERSION_FACTOR = 1024.0;
		private int byteUnitMagnitude;
		private String byteUnitSuffix;

		ByteUnit(int byteUnitMagnitude, String byteUnitSuffix) {
			this.byteUnitMagnitude = byteUnitMagnitude;
			this.byteUnitSuffix = byteUnitSuffix;
		}

		public int getByteUnitMagnitude() {
			return this.byteUnitMagnitude;
		}

		public String getByteUnitSuffix() {
			return this.byteUnitSuffix;
		}

		/**
		 * Converts the file size passed in bytes to the requested ByteUnit.  If the ByteUnit passed is BYTE, there
		 * will be no change other than receiving the result as a String.
		 *
		 * @param fileSizeInBytes Size of the file in bytes
		 * @param byteUnit Type of unit to retrieve file size as (i.e. BYTE, KILOBYTE, MEGABYTE, GIGABYTE)
		 * @return Returns the file's size with the appropriate byte unit as a suffix (e.g. calling this method as
		 * follows - ByteUnit.getFileSizeAs(1536, ByteUnit.MEGABYTE) - will produce the result "1.5 MB")
		 * @throws IllegalArgumentException Thrown if the value of fileSizeInBytes is less than 0.
		 */
		public static String getFileSizeAs(long fileSizeInBytes, ByteUnit byteUnit) {
			if (fileSizeInBytes < 0) {
				throw new IllegalArgumentException("The fileSizeInBytes argument must have a positive value");
			}

			double convertedFileSize = fileSizeInBytes /
				Math.pow(BYTE_UNIT_CONVERSION_FACTOR, byteUnit.getByteUnitMagnitude());
			String convertedFileSizeSuffix = byteUnit.getByteUnitSuffix();

			return appendByteUnitSuffixToFileSize(convertedFileSize, convertedFileSizeSuffix);
		}

		public static String getHumanReadableFileSize(long fileSizeInBytes) {
			int magnitudeOfByteConversionFactor = 0;
			double fileSize = fileSizeInBytes;
			while (fileSize > BYTE_UNIT_CONVERSION_FACTOR) {
				magnitudeOfByteConversionFactor += 1;
				fileSize = fileSize / BYTE_UNIT_CONVERSION_FACTOR;
			}

			return appendByteUnitSuffixToFileSize(
				fileSize, getSuffixByByteUnitMagnitude(magnitudeOfByteConversionFactor)
			);
		}

		private static String getSuffixByByteUnitMagnitude(int byteUnitMagnitude) {
			return Arrays.stream(ByteUnit.values())
				.filter(byteUnit -> byteUnit.getByteUnitMagnitude() == byteUnitMagnitude)
				.findFirst()
				.orElseThrow(() -> new IllegalArgumentException(
					"Could not find suffix for the ByteUnit magnitude of " + byteUnitMagnitude
				))
				.getByteUnitSuffix();
		}

		private static String appendByteUnitSuffixToFileSize(double fileSize, String fileSizeSuffix) {
			return String.format("%.2f %s", fileSize, fileSizeSuffix).trim();
		}
	}
}
