package org.reactome.release.testutilities;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import org.reactome.release.Resource;

public class ResourceTestUtils {
	public static Resource getResource() {
		return new Resource(getTestResourceJsonObject());
	}

	public static JsonObject getTestResourceJsonObject() {
		JsonParser jsonParser = new JsonParser();
		JsonArray testResourcesJsonArray = jsonParser.parse(getTestResourceJsonFileReader()).getAsJsonArray();
		return testResourcesJsonArray.get(0).getAsJsonObject();
	}

	private static Reader getTestResourceJsonFileReader()  {
		String jsonFilePath = ResourceTestUtils.class
			.getClassLoader()
			.getResource("External_Resources_Single_Test_Resource.json")
			.getPath();

		try {
			return new FileReader(jsonFilePath);
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
	}
}
