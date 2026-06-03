package db_2026_team06.model;

public class Attraction {
    private int attractionId;
    private String attractionName;
    private String aDescription;

    // 기본 생성자
    public Attraction() {}

    // Getter & Setter
    public int getAttractionId() { return attractionId; }
    public void setAttractionId(int attractionId) { this.attractionId = attractionId; }

    public String getAttractionName() { return attractionName; }
    public void setAttractionName(String attractionName) { this.attractionName = attractionName; }

    public String getADescription() { return aDescription; }
    public void setADescription(String aDescription) { this.aDescription = aDescription; }
}