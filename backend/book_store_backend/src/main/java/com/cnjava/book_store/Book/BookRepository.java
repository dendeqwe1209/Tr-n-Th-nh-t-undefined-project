package com.cnjava.book_store.Book;

import java.util.*;
import org.springframework.data.jpa.repository.*;

public interface BookRepository extends JpaRepository<Book,Long> {
  @Override
  @EntityGraph(attributePaths={"author","category"})
  List<Book> findAll();

  @Override
  @EntityGraph(attributePaths={"author","category"})
  Optional<Book> findById(Long id);

  @EntityGraph(attributePaths={"author","category"})
  Optional<Book> findByTitleIgnoreCase(String title);

  boolean existsByTitleIgnoreCase(String title);
  boolean existsByCategory_Id(long categoryId);
}
