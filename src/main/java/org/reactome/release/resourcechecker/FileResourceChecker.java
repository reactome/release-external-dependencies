package org.reactome.release.resourcechecker;

import com.google.gson.JsonObject;

import java.io.IOException;
import java.nio.file.Path;
import java.text.DecimalFormat;

/**
 * Represents the methods available, in any implementing class, intended to check/investigate an external file resource
 *
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 */
public interface FileResourceChecker extends ResourceChecker {

	/**
	 * Writes the contents of the file that this FileResourceChecker is checking to a file location the Path object
	 * parameter 'fileDestination' specifies
	 *
	 * @param fileDestination Path object of where the file contents should be written
	 * @throws IOException Thrown if unable to write the file contents to the Path object parameter specified
	 */
	void saveFileContents(Path fileDestination) throws IOException;

	/**
	 * Returns the size of the file, in bytes, that this FileResourceChecker is checking
	 *
	 * @return File size in bytes
	 */
	long getFileSize();

	/**
	 * Returns true if the current file size has not dropped more than an acceptable percentage when compared with
	 * the previous file size.
	 *
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
	 *
	 * @param previousFileSize Last known acceptable file size in bytes
	 * @return True if the file size has not dropped more than the acceptable percentage; False otherwise
	 * @see #isFileSizeAcceptable(long, double)
	 */
	default boolean isFileSizeAcceptable(long previousFileSize) {
		final double acceptableFileSizePercentageDrop = 5.0;

		return isFileSizeAcceptable(previousFileSize, acceptableFileSizePercentageDrop);
	}

	/**
	 * Returns a report as a JsonObject describing if the file resource being checked has passed its required checks,
	 * if the resource exists, the size of the file found, and if the file size found is acceptable compared to the
	 * expected size of the file from previous times the resource was accessed
	 *
	 * @return Report as a JsonObject detailing the results of the check performed on the file resource
	 */
	@Override
	default JsonObject getReport() {
		JsonObject reportJson = new JsonObject();
		reportJson.addProperty("Passed Checks", resourcePassesAllChecks());
		reportJson.addProperty("Resource Exists", resourceExists());
		reportJson.add("File Size", getFileSizeReport(getResource().getExpectedFileSizeInBytes()));

		return reportJson;
	}

	/**
	 * Returns a report as a JsonObject describing the size of the file resource being checked and if that file size
	 * is acceptable compared against a previously known value of the resource's file size.
	 *
	 * @param previousFileSize Previous size of the file in bytes as a benchmark to check if the current size is
	 * acceptable (i.e. has it fallen significantly)
	 * @return Report as a JsonObject detailing the results of the check performed on the file resource specifically
	 * for its size
	 */
	default JsonObject getFileSizeReport(long previousFileSize) {
		JsonObject fileSizeReportJson = new JsonObject();
		fileSizeReportJson.addProperty("File Size Found", getFileSizeFound());
		fileSizeReportJson.addProperty("File Size Acceptable",  isFileSizeAcceptable(previousFileSize));
		return fileSizeReportJson;
	}

	/**
	 * Returns a String describing the resource's file size in a human readable format with the number of bytes
	 * in brackets, e.g. 1.5 MB (1536 bytes)
	 *
	 * @return String describing the resource's file size
	 */
	default String getFileSizeFound() {
		long fileSizeInBytes = getFileSize();

		return String.format(
			"%s (%d bytes)",
			ByteUnit.getHumanReadableFileSize(fileSizeInBytes),
			fileSizeInBytes
		);
	}

	/**
	 * Returns true if the file resource being checked exists and has an acceptable file size when compared to its
	 * expected file size (i.e. its last known and accepted file size)
	 *
	 * @return <code>true</code> if the resource is found and in an expected state, <code>false</code> otherwise
	 */
	@Override
	default boolean resourcePassesAllChecks() {
		return resourceExists() && isFileSizeAcceptable(getResource().getExpectedFileSizeInBytes());
	}

	/**
	 * This enum describes common units for file size along with their magnitude factor of 1024 relative to the byte
	 * and the suffix for each unit
	 */
	enum ByteUnit {
		BYTE(0, "B"),
		KILOBYTE(1,"KB"),
		MEGABYTE(2,"MB"),
		GIGABYTE(3,"GB");

		private static final double BYTE_UNIT_CONVERSION_FACTOR = 1024.0;

		private int byteUnitMagnitude;
		private String byteUnitSuffix;

		/**
		 * Creates a representation of a common byte unit
		 *
		 * @param byteUnitMagnitude Order of magnitude by which the unit raises the byte unit conversion factor of 1024
		 * (e.g. the magnitude for MEGABYTE is 2 because dividing the number of bytes by 1024^2 will return the number
		 * megabytes)
		 * @param byteUnitSuffix Suffix identifying the byte unit (e.g. MEGABYTE has a suffix of "MB")
		 */
		ByteUnit(int byteUnitMagnitude, String byteUnitSuffix) {
			this.byteUnitMagnitude = byteUnitMagnitude;
			this.byteUnitSuffix = byteUnitSuffix;
		}


		/**
		 * Converts the file size passed in bytes to a "human readable" String format.  This means the number of
		 * bytes will be converted into the byte unit that has a numeric value equal to no more than 1024 (i.e. the
		 * byte unit conversion factor) to express it as a smaller number with a larger byte unit.  The precision of
		 * the conversion will be to no more than two decimal places.  For example, 1073741824 bytes will be expressed
		 * as "1.0 GB".  If the number of bytes passed to this method is less than 1024, the same number will be
		 * returned but with the prefix "B" appended (e.g. 1023 will return "1023.0 B").
		 *
		 * @param fileSizeInBytes Size of the file in number of bytes
		 * @return A String that expresses the number of bytes passed to this method with a number no more than 1024
		 * (i.e. the byte unit conversion factor) and the appropriate byte unit suffix.  For example, 1073741824 bytes
		 * will return the String "1.0 GB".
		 * @throws IllegalArgumentException Thrown if the value of fileSizeInBytes is less than 0.
		 */
		public static String getHumanReadableFileSize(long fileSizeInBytes) {
			throwIllegalArgumentExceptionIfNumberOfBytesIsNegative(fileSizeInBytes);

			if (GIGABYTE.isLessThanOrEqualTo(fileSizeInBytes)) {
				return GIGABYTE.convertFromBytes(fileSizeInBytes);
			} else if (MEGABYTE.isLessThanOrEqualTo(fileSizeInBytes)) {
				return MEGABYTE.convertFromBytes(fileSizeInBytes);
			} else if (KILOBYTE.isLessThanOrEqualTo(fileSizeInBytes)) {
				return KILOBYTE.convertFromBytes(fileSizeInBytes);
			} else {
				return BYTE.convertFromBytes(fileSizeInBytes);
			}
		}

		/**
		 * Returns the order of magnitude the byte unit conversion factor of 1024 is raised to get the value used to
		 * convert from BYTE to the unit this method is called on (e.g. calling this method on the unit MEGABYTE would
		 * return 2 as the conversion from BYTE to MEGABYTE is done by dividing by 1024 raised to the exponent 2)
		 *
		 * @return Order of magnitude by which this object's unit raises the byte unit conversion factor (relative to
		 * the BYTE)
		 */
		public int getByteUnitMagnitude() {
			return this.byteUnitMagnitude;
		}

		/**
		 * Returns the suffix that identifies this object's unit.  For the BYTE, an empty string is returned
		 * as it has no suffix
		 *
		 * @return Suffix identifying the byte unit (e.g. MEGABYTE has a suffix of "MB")
		 */
		public String getByteUnitSuffix() {
			return this.byteUnitSuffix;
		}

		/**
		 * Converts the file size passed in bytes to the ByteUnit type on which this method is called.
		 * If the ByteUnit passed is BYTE, there will be no change other than receiving the result as a String with "B"
		 * as a suffix.
		 *
		 * @param fileSizeInBytes Size of the file in bytes
		 * @return Returns the file's size with the appropriate byte unit as a suffix (e.g. calling this method as
		 * follows - ByteUnit.MEGABYTE.convertFromBytes(1536) - will produce the result "1.5 MB")
		 * @throws IllegalArgumentException Thrown if the value of fileSizeInBytes is less than 0.
		 */
		public String convertFromBytes(long fileSizeInBytes) {
			throwIllegalArgumentExceptionIfNumberOfBytesIsNegative(fileSizeInBytes);

			double convertedFileSize = fileSizeInBytes / this.getNumberOfBytesInByteUnit();
			return appendByteUnitSuffixToFileSize(convertedFileSize, getByteUnitSuffix());
		}

		private static String appendByteUnitSuffixToFileSize(double fileSize, String fileSizeSuffix) {
			DecimalFormat oneOrTwoDecimalPlaces = new DecimalFormat("0.0#");
			return String.format("%s %s", oneOrTwoDecimalPlaces.format(fileSize), fileSizeSuffix).trim();
		}

		private static void throwIllegalArgumentExceptionIfNumberOfBytesIsNegative(long numberOfBytes) {
			if (numberOfBytes < 0) {
				throw new IllegalArgumentException("The number of bytes must be greater than or equal to zero");
			}
		}

		private boolean isLessThanOrEqualTo(long fileSizeInBytes) {
			return this.getNumberOfBytesInByteUnit() <= fileSizeInBytes;
		}

		private double getNumberOfBytesInByteUnit() {
			return Math.pow(BYTE_UNIT_CONVERSION_FACTOR, getByteUnitMagnitude());
		}
	}
}
