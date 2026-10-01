package com.cnjava.book_store.Review;

import java.time.LocalDateTime;
import jakarta.persistence.*;

@Entity
@Table(name="review")
public class Review {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private long id;
  @Column(nullable=false) private long bookId;
  private Long userId;
  @Column(nullable=false,length=120) private String reviewerName;
  @Column(nullable=false) private int rating;
  @Column(nullable=false,length=2000) private String comment;
  @Column(nullable=false) private LocalDateTime createdAt;

  @PrePersist public void onCreate(){ if(createdAt==null) createdAt=LocalDateTime.now(); }
  public long getId(){return id;} public void setId(long id){this.id=id;}
  public long getBookId(){return bookId;} public void setBookId(long bookId){this.bookId=bookId;}
  public Long getUserId(){return userId;} public void setUserId(Long userId){this.userId=userId;}
  public String getReviewerName(){return reviewerName;} public void setReviewerName(String reviewerName){this.reviewerName=reviewerName;}
  public int getRating(){return rating;} public void setRating(int rating){this.rating=rating;}
  public String getComment(){return comment;} public void setComment(String comment){this.comment=comment;}
  public LocalDateTime getCreatedAt(){return createdAt;} public void setCreatedAt(LocalDateTime createdAt){this.createdAt=createdAt;}
}
