package com.cnjava.book_store.Book;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/admin/book-managerment")
public class BookController {
 private BookService bookService; @Autowired public BookController(BookService bookService){this.bookService=bookService;}
 @GetMapping("/books") public ResponseEntity<List<Book>> findAllBooks(){return ResponseEntity.ok(bookService.findAll());}
 @GetMapping("/books/{id}") public ResponseEntity<Book> getBookById(@PathVariable Long id){Book b=bookService.getBookById(id);return b!=null?new ResponseEntity<>(b,HttpStatus.OK):new ResponseEntity<>(HttpStatus.NOT_FOUND);}
 @PostMapping() public ResponseEntity<String> addNewBook(@RequestBody Book newBook){bookService.createBook(newBook);return new ResponseEntity<>("Book added successfully",HttpStatus.OK);}
 @DeleteMapping("/books/{id}") public ResponseEntity<String> deleteBookById(@PathVariable Long id){return bookService.deleteBookById(id)?new ResponseEntity<>("Book deleted successfully",HttpStatus.OK):new ResponseEntity<>(HttpStatus.NOT_FOUND);}
 @PutMapping("/books/{id}") public ResponseEntity<String> updateBook(@PathVariable Long id,@RequestBody Book updatedBook){return bookService.updateBook(id,updatedBook)?new ResponseEntity<>("Updated",HttpStatus.OK):new ResponseEntity<>(HttpStatus.NOT_FOUND);}
}
