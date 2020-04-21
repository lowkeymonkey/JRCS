import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import jrcs.avro.to.csv.jrcsavrocv.Contents;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.CDL;
import org.json.JSONArray;
import org.json.JSONException;
import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.file.DataFileReader;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.commons.io.FileUtils;
import org.json.JSONObject;

public class AvroToCSV {

    public static void readAvro(File file) {
        // Read Avro ,parse Schema to get field names and parse it to json
        try {

            GenericDatumReader<GenericData.Record> datum = new GenericDatumReader<GenericData.Record>();
            DataFileReader<GenericData.Record> reader = new DataFileReader<GenericData.Record>(file, datum);

            GenericData.Record record = new GenericData.Record(reader.getSchema());
            Schema schema = reader.getSchema();
            List<String> fieldValues = new ArrayList<>();
            JSONArray jsonArray = new JSONArray();
            for (Field field : schema.getFields()) {
                fieldValues.add(field.name());
            }

            List<List<String>> csvInput = new ArrayList<>();
            while (reader.hasNext()) {
                reader.next(record);
                Map<String, String> jsonFileds = new HashMap<String, String>();
                for (String item : fieldValues) {

                    if(item.equals("Body")){
                        ByteBuffer paramByteBuffer = (ByteBuffer) record.get(item);
                        JSONObject jsonObject = new JSONObject( new String(paramByteBuffer.array(),"UTF-8"));

                        Contents content = new ObjectMapper().readValue(new String(paramByteBuffer.array(),"UTF-8"), Contents.class);

                        csvInput.addAll(content.getContents());
                        for(List<String> stringList: content.getContents()){
                            System.out.println("NEXT LINE");
                            for(String str: stringList){
                                System.out.print(str);
                            }
                        }

                    }
                }
                jsonArray.put(jsonFileds);
            }
            System.out.println(jsonArray.toString());
            reader.close();
            jsonToCSV(csvInput);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public static void jsonToCSV(List<List<String>> input) throws IOException {

        FileWriter writer = new FileWriter("avroToJson5.csv");

        for(List<String> stringList : input) {
            writer.write(stringList.stream().collect(Collectors.joining(",")));
            writer.write("\n"); // newline
        }

        writer.close();
    }

    public static void main(String[] args) {
        File f = new File("src/main/resources/02.avro");
        readAvro(f);
    }
}