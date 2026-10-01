package com.cnjava.book_store.Author;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class AuthorServiceImp implements AuthorService {
	AuthorRepository authorRepository;
	public AuthorServiceImp(AuthorRepository authorRepository) { this.authorRepository = authorRepository; }
	@Override public List<Author> findAll() { return authorRepository.findAll(); }
	@Override public void createAuthor(Author newAuthor) { authorRepository.save(newAuthor); }
	@Override public Author getAuthorById(Long id) { return authorRepository.findById(id).orElse(null); }
	@Override public boolean deleteAuthorById(Long id) { try { authorRepository.deleteById(id); return true; } catch (Exception e) { return false; } }
	@Override public boolean updateAuthor(Long id, Author updatedAuthor) { Optional<Author> o=authorRepository.findById(id); if(o.isPresent()){ Author a=o.get(); a.setFullName(updatedAuthor.getFullName()); authorRepository.save(a); return true;} return false; }
}
