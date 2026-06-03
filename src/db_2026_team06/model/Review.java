package db_2026_team06.model;

import java.time.LocalDate;

/**
 * Review 테이블에 대응하는 DTO 클래스
 * 호텔에 달린 리뷰 정보를 담습니다.
 */
public class Review {
    private int reviewId;
    private int rating;
    private LocalDate reviewDate;
    private String review;
    private int hotelId;
    private int customerId;

    public Review() {}

    public Review(int reviewId, int rating, LocalDate reviewDate, String review, int hotelId, int customerId) {
        this.reviewId   = reviewId;
        this.rating     = rating;
        this.reviewDate = reviewDate;
        this.review     = review;
        this.hotelId    = hotelId;
        this.customerId = customerId;
    }

    // ── Getters & Setters ──────────────────────────────────────────────

    public int getReviewId()                { return reviewId; }
    public void setReviewId(int v)          { this.reviewId = v; }

    public int getRating()                  { return rating; }
    public void setRating(int v)            { this.rating = v; }

    public LocalDate getReviewDate()             { return reviewDate; }
    public void setReviewDate(LocalDate v)       { this.reviewDate = v; }

    public String getReview()               { return review; }
    public void setReview(String v)         { this.review = v; }

    public int getHotelId()                 { return hotelId; }
    public void setHotelId(int v)           { this.hotelId = v; }

    public int getCustomerId()              { return customerId; }
    public void setCustomerId(int v)        { this.customerId = v; }
}