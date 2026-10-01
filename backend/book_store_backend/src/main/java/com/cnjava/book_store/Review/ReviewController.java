package com.cnjava.book_store.Review;

import java.util.*;
import com.cnjava.book_store.Auth.*;
import com.cnjava.book_store.Book.BookRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/books/{bookId}/reviews")
public class ReviewController {
  private final ReviewRepository reviews;
  private final BookRepository books;
  private final AuthService auth;

  public ReviewController(ReviewRepository reviews,BookRepository books,AuthService auth){
    this.reviews=reviews;this.books=books;this.auth=auth;
  }

  public record ReviewRequest(Integer rating,String comment){}

  @GetMapping
  public ResponseEntity<Map<String,Object>> list(@PathVariable long bookId){
    if(!books.existsById(bookId)) return ResponseEntity.notFound().build();
    List<Review> data=reviews.findByBookIdOrderByCreatedAtDesc(bookId);
    double average=data.stream().mapToInt(Review::getRating).average().orElse(0);
    Map<String,Object> body=new LinkedHashMap<>();
    body.put("averageRating",Math.round(average*10.0)/10.0);
    body.put("reviewCount",data.size());
    body.put("reviews",data);
    return ResponseEntity.ok(body);
  }

  @PostMapping
  public ResponseEntity<?> create(@PathVariable long bookId,@RequestBody ReviewRequest request,HttpServletRequest http){
    UserAccount user=auth.currentUser(http).orElse(null);
    if(user==null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message","Please sign in to leave a review."));
    if(!books.existsById(bookId)) return ResponseEntity.notFound().build();
    if(request.rating()==null || request.rating()<1 || request.rating()>5)
      return ResponseEntity.badRequest().body(Map.of("message","Rating must be between 1 and 5."));
    if(request.comment()==null || request.comment().trim().length()<3)
      return ResponseEntity.badRequest().body(Map.of("message","Comment must contain at least 3 characters."));

    Review review=new Review();
    review.setBookId(bookId);
    review.setUserId(user.getId());
    review.setReviewerName(user.getFullName());
    review.setRating(request.rating());
    review.setComment(request.comment().trim());
    return ResponseEntity.status(HttpStatus.CREATED).body(reviews.save(review));
  }
}
