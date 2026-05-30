package db_2026_team06.model;

/**
 * Attraction 테이블에 대응하는 DTO 클래스
 * 호텔 주변 관광지 정보를 담습니다.
 */
public class Attraction {
    private int attractionId;
    private String attractionName;
    private String aDescription;

    public Attraction() {}

    public Attraction(int attractionId, String attractionName, String aDescription) {
        this.attractionId   = attractionId;
        this.attractionName = attractionName;
        this.aDescription   = aDescription;
    }

    // ── Getters & Setters ──────────────────────────────────────────────

    public int getAttractionId()                { return attractionId; }
    public void setAttractionId(int v)          { this.attractionId = v; }

    public String getAttractionName()           { return attractionName; }
    public void setAttractionName(String v)     { this.attractionName = v; }

    public String getADescription()             { return aDescription; }
    public void setADescription(String v)       { this.aDescription = v; }
}
