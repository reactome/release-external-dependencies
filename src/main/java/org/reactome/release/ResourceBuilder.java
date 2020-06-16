package org.reactome.release;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

public class ResourceBuilder {

	public List<Resource> getResources(String csvResourceFilePath) throws IOException {
		CSVParser csvParser = CSVParser.parse(Paths.get(csvResourceFilePath), StandardCharsets.UTF_8, CSVFormat.DEFAULT.withHeader());
		List<CSVRecord> csvRecords = csvParser.getRecords();

		List<Resource> resources = new ArrayList<>();
		for (CSVRecord csvRecord : csvRecords) {
			resources.add(new Resource(csvRecord));
		}
		return resources;
	}
}
