package org.reactome.release.resourcechecker;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.gson.JsonObject;
import java.io.IOException;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.reactome.release.Resource;
import org.reactome.release.resourcechecker.FileResourceChecker.ByteUnit;

public class FileResourceCheckerTest {
	private static final ByteUnit DUMMY_BYTE_UNIT = ByteUnit.MEGABYTE;
	private static FileResourceChecker fileResourceChecker;

	@Mock
	private static Resource resource;

	@BeforeAll
	public static void initializeFileResourceChecker() {
		long testActualFileSize = 950;
		fileResourceChecker = getFileResourceChecker(testActualFileSize);
	}

	@BeforeEach
	public void initMocks() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void correctReportIsProduced() {
		long testExpectedFileSize = 950;
		Mockito.when(resource.getExpectedFileSizeInBytes()).thenReturn(testExpectedFileSize);

		assertThat(
			fileResourceChecker.getReport(),
			is(equalTo(getExpectedReportJsonObject()))
		);
	}

	@Test
	public void expectedFileSizeReturnsTrueForIsFileSizeAcceptable() {
		int previousFileSize = 999;

		assertThat(
			fileResourceChecker.isFileSizeAcceptable(previousFileSize),
			is(true)
		);
	}

	@Test
	public void unexpectedFileSizeReturnsFalseForIsFileSizeAcceptable() {
		long previousFileSize = 1000;

		Mockito.when(resource.getExpectedFileSizeInBytes()).thenReturn(previousFileSize);

		assertThat(
			fileResourceChecker.isFileSizeAcceptable(),
			is(false)
		);
	}

	@Test
	public void expectedFileSizeReturnsTrueForIsFileSizeAcceptableWithCustomAcceptablePercentageDrop() {
		long previousFileSize = 1000;
		double acceptablePercentDropJustAboveDefault = 5.000000000000001;

		Mockito.when(resource.getExpectedFileSizeInBytes()).thenReturn(previousFileSize);

		assertThat(
			fileResourceChecker.isFileSizeAcceptable(acceptablePercentDropJustAboveDefault),
			is(true)
		);
	}

	@Test
	public void unexpectedFileSizeReturnsFalseForIsFileSizeAcceptableWithCustomAcceptablePercentageDrop() {
		long previousFileSize = 1000;
		double acceptablePercentDropJustBelowDefault = 4.999999999999999;

		Mockito.when(resource.getExpectedFileSizeInBytes()).thenReturn(previousFileSize);

		assertThat(
			fileResourceChecker.isFileSizeAcceptable(acceptablePercentDropJustBelowDefault),
			is(false)
		);
	}

	@Test
	public void throwsExceptionForFileSizeConversionOfNegativeBytesToAnyByteUnitValue() {
		final int negativeNumberOfBytes = -1;

		IllegalArgumentException thrown = assertThrows(
			IllegalArgumentException.class,
			() -> DUMMY_BYTE_UNIT.convertFromBytes(negativeNumberOfBytes),
			"Expected a negative number of bytes to throw an IllegalArgumentException when passed as a parameter to " +
				" the method 'getFileSizeAs(long, ByteUnit)', but it didn't"
		);

		assertThat(thrown.getMessage(), containsString("must be greater than or equal to zero"));
	}

	@Test
	public void correctFileSizeConversionToKilobytesForZeroBytes() {
		final int zeroBytes = 0;
		String fileSizeAsKilobytes = ByteUnit.KILOBYTE.convertFromBytes(zeroBytes);

		assertThat(fileSizeAsKilobytes, is(equalTo("0.0 KB")));
	}

	@Test
	public void correctFileSizeConversionToKilobytesFromBytes() {
		final int numberOfBytes = 1536;
		String fileSizeAsKilobytes = ByteUnit.KILOBYTE.convertFromBytes(numberOfBytes);

		assertThat(fileSizeAsKilobytes, is(equalTo("1.5 KB")));
	}

	@Test
	public void correctFileSizeConversionToKilobytesWithTwoDecimalPlaces() {
		final int bytesForAKilobyteMeasureToTwoDecimalPlaces = 1034;
		String fileSizeAsKilobytes = ByteUnit.KILOBYTE.convertFromBytes(bytesForAKilobyteMeasureToTwoDecimalPlaces);

		assertThat(fileSizeAsKilobytes, is(equalTo("1.01 KB")));
	}

	@Test
	public void correctFileSizeConversionToMegabytesFromBytes() {
		final int numberOfBytes = 1572864;
		String fileSizeAsMegabytes = ByteUnit.MEGABYTE.convertFromBytes(numberOfBytes);

		assertThat(fileSizeAsMegabytes, is(equalTo("1.5 MB")));
	}

	@Test
	public void correctFileSizeConversionToGigabytesFromBytes() {
		final int numberOfBytes = 1610612736;
		String fileSizeAsGigabytes = ByteUnit.GIGABYTE.convertFromBytes(numberOfBytes);

		assertThat(fileSizeAsGigabytes, is(equalTo("1.5 GB")));
	}

	@Test
	public void throwsExceptionForHumanReadableFileSizeConversionOfNegativeBytes() {
		final int negativeNumberOfBytes = -1;

		IllegalArgumentException thrown = assertThrows(
			IllegalArgumentException.class,
			() -> ByteUnit.getHumanReadableFileSize(negativeNumberOfBytes),
			"Expected a negative number of bytes to throw an IllegalArgumentException when passed as a parameter to " +
				" the method 'getHumanReadableSize(long)', but it didn't"
		);

		assertThat(thrown.getMessage(), containsString("must be greater than or equal to zero"));
	}

	@Test
	public void correctHumanReadableFileSizeConversionToBytesForZeroBytes() {
		final int zeroBytes = 0;
		String humanReadableFileSize = ByteUnit.getHumanReadableFileSize(zeroBytes);

		assertThat(humanReadableFileSize, is(equalTo("0.0 B")));
	}

	@Test
	public void correctHumanReadableFileSizeConversionToBytesWithOneByteLessThanAKilobyte() {
		final int bytesInOneKilobyteMinusOneByte = 1023;
		String humanReadableFileSize = ByteUnit.getHumanReadableFileSize(bytesInOneKilobyteMinusOneByte);

		assertThat(humanReadableFileSize, is(equalTo("1023.0 B")));
	}

	@Test
	public void correctHumanReadableFileSizeConversionToKilobytesWithExactlyOneKilobyte() {
		final int bytesInOneKilobyte = 1024;
		String humanReadableFileSize = ByteUnit.getHumanReadableFileSize(bytesInOneKilobyte);

		assertThat(humanReadableFileSize, is(equalTo("1.0 KB")));
	}

	@Test
	public void correctHumanReadableFileSizeConversionToKilobytesWithTwoDecimalPlaces() {
		final int bytesForAKilobyteMeasureToTwoDecimalPlaces = 1034;
		String humanReadableFileSize = ByteUnit.getHumanReadableFileSize(bytesForAKilobyteMeasureToTwoDecimalPlaces);

		assertThat(humanReadableFileSize, is(equalTo("1.01 KB")));
	}

	@Test
	public void correctHumanReadableFileSizeConversionToKilobytesWithOneLessByteThanAMegabyte() {
		final int bytesInOneMegabyteMinusOneByte = 1048575;
		String humanReadableFileSize = ByteUnit.getHumanReadableFileSize(bytesInOneMegabyteMinusOneByte);

		assertThat(humanReadableFileSize, is(equalTo("1024.0 KB")));
	}

	@Test
	public void correctHumanReadableFileSizeConversionToMegabytes() {
		int bytesInOneMegabyte = 1048576;
		String humanReadableFileSize = ByteUnit.getHumanReadableFileSize(bytesInOneMegabyte);

		assertThat(humanReadableFileSize, is(equalTo("1.0 MB")));
	}

	@Test
	public void correctHumanReadableFileSizeConversionToMegabytesWithTwoDecimalPlaces() {
		final int bytesForAMegabyteMeasureToTwoDecimalPlaces = 1059062;
		String humanReadableFileSize = ByteUnit.getHumanReadableFileSize(bytesForAMegabyteMeasureToTwoDecimalPlaces);

		assertThat(humanReadableFileSize, is(equalTo("1.01 MB")));
	}

	@Test
	public void correctHumanReadableFileSizeConversionToMegabytesWithOneLessByteThanAGigabyte() {
		int bytesInOneGigabyteMinusOneByte = 1073741823;
		String humanReadableFileSize = ByteUnit.getHumanReadableFileSize(bytesInOneGigabyteMinusOneByte);

		assertThat(humanReadableFileSize, is(equalTo("1024.0 MB")));
	}

	@Test
	public void correctHumanReadableFileSizeConversionToGigabytes() {
		int bytesInOneGigabyte = 1073741824;
		String humanReadableFileSize = ByteUnit.getHumanReadableFileSize(bytesInOneGigabyte);

		assertThat(humanReadableFileSize, is(equalTo("1.0 GB")));
	}

	@Test
	public void correctHumanReadableFileSizeConversionToGigabytesWithTwoDecimalPlaces() {
		final int bytesForAGigabyteMeasureToTwoDecimalPlaces = 1084479242;
		String humanReadableFileSize = ByteUnit.getHumanReadableFileSize(bytesForAGigabyteMeasureToTwoDecimalPlaces);

		assertThat(humanReadableFileSize, is(equalTo("1.01 GB")));
	}

	private static FileResourceChecker getFileResourceChecker(long testActualFileSize) {
		return new FileResourceChecker() {
			@Override
			public void saveFileContents(Path fileDestination) throws IOException {
				/*
				This method has no default implementation and will be tested on interfaces or classes which implement
				this method
				*/
				return;
			}

			@Override
			public long getFileSize() {
				return testActualFileSize;
			}

			@Override
			public Resource getResource() {
				return FileResourceCheckerTest.resource;
			}

			@Override
			public boolean resourceExists() {
				return true;
			}
		};
	}

	static JsonObject getExpectedReportJsonObject() {
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("Passed Checks", true);
		jsonObject.addProperty("Resource Exists", true);

		JsonObject fileSizeJsonObject = new JsonObject();
		fileSizeJsonObject.addProperty("File Size Found", "950.0 B (950 bytes)");
		fileSizeJsonObject.addProperty("File Size Acceptable", true);

		jsonObject.add("File Size", fileSizeJsonObject);

		return jsonObject;
	}
}
