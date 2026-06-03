package db_2026_team06.model;

import java.time.LocalDate;

public class Review {
    private int reviewId;
    private int rating;
    private LocalDate reviewDate;
    private String review;
    private int hotelId;
    private int customerId;

    public Review() {
    }

    public Review(int reviewId, int rating, LocalDate reviewDate, String review, int hotelId, int customerId) {
        this.reviewId = reviewId;
        this.rating = rating;
        this.reviewDate = reviewDate;
        this.review = review;
        this.hotelId = hotelId;
        this.customerId = customerId;
    }

    public int getReviewId() {
        return reviewId;
    }

    public void setReviewId(int reviewId) {
        this.reviewId = reviewId;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public LocalDate getReviewDate() {
        return reviewDate;
    }

    public void setReviewDate(LocalDate reviewDate) {
        this.reviewDate = reviewDate;
    }

    public String getReview() {
        return review;
    }

    public void setReview(String review) {
        this.review = review;
    }

    public int getHotelId() {
        return hotelId;
    }

    public void setHotelId(int hotelId) {
        this.hotelId = hotelId;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }
}