package com.cnjava.book_store.Review;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review,Long> {
  List<Review> findByBookIdOrderByCreatedAtDesc(long bookId);
  void deleteByBookId(long bookId);
}
