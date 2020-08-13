package org.reactome.release;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

public class ResourceParser {

	/**
	 * Returns a list of Resource objects representing resources as described in a CSV or JSON file.
	 *
	 * @param resourceFilePath The path to a CSV of JSON file describing the resources for which to get a list of
	 * Resource objects.
	 * @return A list of Resource objects
	 * @throws IOException Thrown if the file path passed to the resourceFilePath can not be found or if the file can
	 * not be parsed
	 * @throws IllegalArgumentException Thrown if the resourceFilePath parameter passed does not end with
	 * a ".csv" or a ".json" extension
	 */
	public List<Resource> getResources(String resourceFilePath) throws IOException {
		if (resourceFilePath.endsWith(".json")) {
			return getResourcesFromJSONFile(resourceFilePath);
		} else if (resourceFilePath.endsWith(".csv")) {
			return getResourcesFromCSVFile(resourceFilePath);
		} else {
			throw new IllegalArgumentException("Extension for the file " + resourceFilePath + " is not recognized");
		}
	}

	private List<Resource> getResourcesFromJSONFile(String jsonResourceFilePath) throws FileNotFoundException {
		JsonParser jsonParser = new JsonParser();
		JsonArray jsonResourceArray = jsonParser.parse(new FileReader(jsonResourceFilePath)).getAsJsonArray();
		Iterator<JsonElement> jsonResourceIterator = jsonResourceArray.iterator();

		List<Resource> resources = new ArrayList<>();
		while (jsonResourceIterator.hasNext()) {
			JsonObject jsonResourceObject = jsonResourceIterator.next().getAsJsonObject();
			resources.add(new Resource(jsonResourceObject));
		}
		return resources;
	}

	private List<Resource> getResourcesFromCSVFile(String csvResourceFilePath) throws IOException {
		CSVParser csvParser = CSVParser.parse(
			Paths.get(csvResourceFilePath),
			StandardCharsets.UTF_8,
			CSVFormat.DEFAULT.withHeader()
		);
		List<CSVRecord> csvResourceRecords = csvParser.getRecords();

		List<Resource> resources = new ArrayList<>();
		for (CSVRecord csvResourceRecord : csvResourceRecords) {
			resources.add(new Resource(csvResourceRecord));
		}
		return resources;
	}
}
