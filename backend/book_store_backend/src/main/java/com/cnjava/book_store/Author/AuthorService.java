package com.cnjava.book_store.Author;
import java.util.List;
public interface AuthorService { List<Author> findAll(); void createAuthor(Author newAuthor); Author getAuthorById(Long id); boolean deleteAuthorById(Long id); boolean updateAuthor(Long id, Author updatedAuthor); }
