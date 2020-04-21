package jrcs.avro.to.csv.jrcsavrocv;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.file.DataFileReader;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericDatumReader;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AVROtoCSV {

    public static void main(String[] args) throws IOException {

        if(args.length<2){
            throw new IllegalArgumentException("Please provide correct number of inputs");
        }

        File f = new File(args[0]);
        List<List<String>> inputFromAvro = retrieveContentsFromAvro(f);
        writeInputToCSV( inputFromAvro, args[1]);

        System.out.print("Successfully generated csv file");
    }

    /**
     * Retrieves contents from Avro file
     *
     * Contains the following steps:
     *  1. Reads avro file
     *  2. Parses Schema to retrieve field names
     *  3. Loops through input to find values with 'Body' field
     *  4. Converts contents of Body (ByteBuffer type) to String
     *  5. Converts String to Contents POJO and retrieves contents
     *
     * @param file
     * @return
     */
    private static List<List<String>> retrieveContentsFromAvro(File file) throws IOException {
        // Read Avro ,parse Schema to get field names and parse it to json
        GenericDatumReader<GenericData.Record> datum = new GenericDatumReader<>();
        List<List<String>> avroInput = new ArrayList<>();

        try(DataFileReader<GenericData.Record> reader = new DataFileReader<>(file, datum)) {
            GenericData.Record record = new GenericData.Record(reader.getSchema());
            Schema schema = reader.getSchema();
            List<String> fieldValues = new ArrayList<>();
            for (Field field : schema.getFields()) {
                fieldValues.add(field.name());
            }

            while (reader.hasNext()) {
                reader.next(record);
                for (String item : fieldValues) {
                    if(item.equals("Body")){
                        ByteBuffer paramByteBuffer = (ByteBuffer) record.get(item);
                        String jsonString = new String(paramByteBuffer.array(),"UTF-8");
                        Contents content = new ObjectMapper().readValue(jsonString, Contents.class);
                        avroInput.addAll(content.getContents());
                    }
                }
            }
        } catch (IOException ioException) {
            System.out.print("Unable to read AVRO file");
            ioException.printStackTrace();
            throw ioException;
        }
        return avroInput;
    }

    /**
     * Writes Input to CSV File
     *
     * Outer List (List of List<String>) separates input to rows
     *    i.e. if the size of the outer list is 3, the number of rows in the csv file is also 3
     *
     * Inner List (List of Strings) separates input to columns
     *    i.e. if the size of the inner list is 4, the number of columns for that given row is also 4
     *
     * @param input
     * @param fileName
     *
     */
    private static void writeInputToCSV(List<List<String>> input, String fileName) throws IOException {
        try(FileWriter writer = new FileWriter(String.format("%s.csv", fileName))){
            for(List<String> stringList : input) {
                writer.write(stringList.stream().collect(Collectors.joining(",")));
                writer.write("\n"); // new row
            }
        }catch (IOException ioException){
            System.out.print("Unable to write to CSV File");
            ioException.printStackTrace();
            throw ioException;
        }
    }

}