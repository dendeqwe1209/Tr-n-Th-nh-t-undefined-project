package com.cnjava.book_store.Author;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthorRepository extends JpaRepository<Author,Long> {
  Optional<Author> findByFullNameIgnoreCase(String fullName);
}
