package com.cnjava.book_store.Author;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/author-managermant")
public class AuthorController {
	AuthorService authorService;
	@Autowired public AuthorController(AuthorService authorService) { this.authorService = authorService; }
	@GetMapping("/authors") public ResponseEntity<List<Author>> findAllAuthors() { return ResponseEntity.ok(authorService.findAll()); }
	@GetMapping("/authors/{id}") public ResponseEntity<Author> getAuthorById(@PathVariable Long id) { Author a=authorService.getAuthorById(id); return a!=null?new ResponseEntity<>(a,HttpStatus.OK):new ResponseEntity<>(HttpStatus.NOT_FOUND); }
	@PostMapping() public ResponseEntity<String> addNewAuthor(@RequestBody Author newAuthor) { authorService.createAuthor(newAuthor); return new ResponseEntity<>("Author added successfully",HttpStatus.OK); }
	@DeleteMapping("/authors/{id}") public ResponseEntity<String> deleteAuthorById(@PathVariable Long id) { return authorService.deleteAuthorById(id)?new ResponseEntity<>("Author deleted successfully",HttpStatus.OK):new ResponseEntity<>(HttpStatus.NOT_FOUND); }
	@PutMapping("/authors/{id}") public ResponseEntity<String> updateAuthor(@PathVariable Long id,@RequestBody Author updatedAuthor) { return authorService.updateAuthor(id,updatedAuthor)?new ResponseEntity<>("Updated",HttpStatus.OK):new ResponseEntity<>(HttpStatus.NOT_FOUND); }
}
