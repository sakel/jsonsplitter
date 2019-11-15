package si.digics;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import java.io.*;

public class JsonSplitter {
    public static void main(String[] args) throws IOException {
        if(args.length != 3) {
            System.out.println("Help:");
            System.out.println("Run it with: java -jar <JARName> <batchSize> <inputFile> <outputFilePrefix>");
            System.exit(1);
        }

        int batchSize = Integer.parseInt(args[0]);
        String fileName = args[1];
        String filePrefix = args[2];

        int batchNumber = 1;

        System.out.println(String.format("Settings: %d -> %s", batchSize, fileName));

        JsonFactory jsonFactory = new JsonFactory();
        JsonParser parser = jsonFactory.createParser(new BufferedReader(new InputStreamReader(new FileInputStream(fileName))));

        JsonGenerator generator = getGenerator(filePrefix, batchNumber, jsonFactory);
        generator.writeStartArray();

        if (parser.nextToken() != JsonToken.START_ARRAY) {
            throw new IllegalStateException("Expected content to be an array");
        }

        int counter = 0;
        while(parser.nextToken() != JsonToken.END_ARRAY) {
            if(counter == batchSize) {
                counter = 0;
                generator.writeEndArray();
                generator.close();
                batchNumber++;

                generator = getGenerator(filePrefix, batchNumber, jsonFactory);
                generator.writeStartArray();
            }

            parser.nextToken();
            String name = parser.getCurrentName();
            parser.nextToken();
            String value = parser.getText();
            if(counter%1000 == 0) System.out.println("Count: " + counter);

            generator.writeStartObject();
            generator.writeStringField(name, value);
            generator.writeEndObject();
            parser.nextToken();
            counter++;
        }

        generator.writeEndArray();
        generator.close();

        parser.close();
    }

    private static JsonGenerator getGenerator(String filePrefix, int batchNumber, JsonFactory jsonFactory) throws IOException {
        return jsonFactory.createGenerator(new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(
                        new File(String.format("%s-%d.json", filePrefix, batchNumber))))));
    }
}
