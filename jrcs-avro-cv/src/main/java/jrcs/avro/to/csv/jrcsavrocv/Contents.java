package jrcs.avro.to.csv.jrcsavrocv;

import java.util.List;

public class Contents {

    private String pointId;

    private List<List<String>> contents;

    public List<List<String>> getContents() {
        return contents;
    }

    public void setContents(List<List<String>> contents) {
        this.contents = contents;
    }

    public String getPointId() {
        return pointId;
    }

    public void setPointId(String pointId) {
        this.pointId = pointId;
    }
}
