package com.cnjava.book_store.config;

import java.math.BigDecimal;
import java.util.*;
import com.cnjava.book_store.Author.*;
import com.cnjava.book_store.Book.*;
import com.cnjava.book_store.Category.*;
import com.cnjava.book_store.Staff.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DemoDataInitializer implements CommandLineRunner {
  private final BookRepository books;
  private final AuthorRepository authors;
  private final CategoryRepository categories;
  private final StaffRepository staff;

  @Value("${app.seed-demo:false}") private boolean enabled;

  public DemoDataInitializer(BookRepository books,AuthorRepository authors,CategoryRepository categories,StaffRepository staff){
    this.books=books;this.authors=authors;this.categories=categories;this.staff=staff;
  }

  record SeedBook(String title,String author,String category){}

  @Override
  public void run(String... args){
    if(!enabled) return;

    List<SeedBook> library=List.of(
      new SeedBook("1984","George Orwell","Classics"),
      new SeedBook("Animal Farm","George Orwell","Classics"),
      new SeedBook("The Great Gatsby","F. Scott Fitzgerald","Classics"),
      new SeedBook("To Kill a Mockingbird","Harper Lee","Classics"),
      new SeedBook("The Catcher in the Rye","J. D. Salinger","Classics"),
      new SeedBook("Pride and Prejudice","Jane Austen","Classics"),
      new SeedBook("Jane Eyre","Charlotte Bronte","Classics"),
      new SeedBook("Of Mice and Men","John Steinbeck","Classics"),

      new SeedBook("Norwegian Wood","Haruki Murakami","Fiction"),
      new SeedBook("Kafka on the Shore","Haruki Murakami","Fiction"),
      new SeedBook("The Alchemist","Paulo Coelho","Fiction"),
      new SeedBook("The Kite Runner","Khaled Hosseini","Fiction"),
      new SeedBook("The Little Prince","Antoine de Saint-Exupery","Fiction"),

      new SeedBook("Harry Potter and the Philosopher's Stone","J. K. Rowling","Fantasy"),
      new SeedBook("The Hobbit","J. R. R. Tolkien","Fantasy"),
      new SeedBook("The Fellowship of the Ring","J. R. R. Tolkien","Fantasy"),
      new SeedBook("A Game of Thrones","George R. R. Martin","Fantasy"),
      new SeedBook("The Name of the Wind","Patrick Rothfuss","Fantasy"),
      new SeedBook("The Lion, the Witch and the Wardrobe","C. S. Lewis","Fantasy"),

      new SeedBook("Dune","Frank Herbert","Science Fiction"),
      new SeedBook("Foundation","Isaac Asimov","Science Fiction"),
      new SeedBook("Neuromancer","William Gibson","Science Fiction"),
      new SeedBook("Ender's Game","Orson Scott Card","Science Fiction"),
      new SeedBook("Fahrenheit 451","Ray Bradbury","Science Fiction"),
      new SeedBook("The Martian","Andy Weir","Science Fiction"),

      new SeedBook("The Girl with the Dragon Tattoo","Stieg Larsson","Mystery & Thriller"),
      new SeedBook("Gone Girl","Gillian Flynn","Mystery & Thriller"),
      new SeedBook("The Da Vinci Code","Dan Brown","Mystery & Thriller"),
      new SeedBook("Murder on the Orient Express","Agatha Christie","Mystery & Thriller"),
      new SeedBook("The Silent Patient","Alex Michaelides","Mystery & Thriller"),

      new SeedBook("Me Before You","Jojo Moyes","Romance"),
      new SeedBook("The Fault in Our Stars","John Green","Romance"),

      new SeedBook("Sapiens","Yuval Noah Harari","History & Biography"),
      new SeedBook("Homo Deus","Yuval Noah Harari","History & Biography"),
      new SeedBook("The Diary of a Young Girl","Anne Frank","History & Biography"),
      new SeedBook("Steve Jobs","Walter Isaacson","History & Biography"),
      new SeedBook("Long Walk to Freedom","Nelson Mandela","History & Biography"),

      new SeedBook("Atomic Habits","James Clear","Self-Help & Business"),
      new SeedBook("The 7 Habits of Highly Effective People","Stephen R. Covey","Self-Help & Business"),
      new SeedBook("Think and Grow Rich","Napoleon Hill","Self-Help & Business"),
      new SeedBook("Rich Dad Poor Dad","Robert T. Kiyosaki","Self-Help & Business"),
      new SeedBook("The Lean Startup","Eric Ries","Self-Help & Business"),
      new SeedBook("Zero to One","Peter Thiel","Self-Help & Business"),
      new SeedBook("Start with Why","Simon Sinek","Self-Help & Business"),
      new SeedBook("Deep Work","Cal Newport","Self-Help & Business"),

      new SeedBook("Clean Code","Robert C. Martin","Technology"),
      new SeedBook("The Pragmatic Programmer","Andrew Hunt and David Thomas","Technology"),
      new SeedBook("Design Patterns","Erich Gamma et al.","Technology"),
      new SeedBook("Introduction to Algorithms","Thomas H. Cormen et al.","Technology"),

      new SeedBook("Meditations","Marcus Aurelius","Philosophy")
    );

    int index=0;
    for(SeedBook seed:library){
      Author author=authors.findByFullNameIgnoreCase(seed.author())
        .orElseGet(()->authors.save(new Author(0,seed.author())));
      Category category=categories.findByNameIgnoreCase(seed.category())
        .orElseGet(()->categories.save(new Category(seed.category())));

      Optional<Book> existing=books.findByTitleIgnoreCase(seed.title());
      Book book=existing.orElseGet(Book::new);
      book.setTitle(seed.title());
      book.setAuthor(author);
      book.setCategory(category);
      book.setPrice(BigDecimal.ZERO);

      if(existing.isEmpty()){
        book.setStock(6+(index%15));
        book.setDescription(description(seed));
        book.setBook_cover(cover(seed.title()));
      }else{
        if(book.getDescription()==null||book.getDescription().isBlank()) book.setDescription(description(seed));
        if(book.getBook_cover()==null||book.getBook_cover().isBlank()) book.setBook_cover(cover(seed.title()));
      }

      books.save(book);
      index++;
    }

    if(staff.count()==0) staff.save(new Staff("Demo Admin","admin@bookstore.demo","0900000000"));
  }

  private String description(SeedBook b){
    return switch(b.category()){
      case "Classics" -> "A landmark classic selected for the Bookstore demo library.";
      case "Fiction" -> "A memorable work of fiction focused on character, emotion and storytelling.";
      case "Fantasy" -> "An imaginative fantasy title filled with adventure, world-building and wonder.";
      case "Science Fiction" -> "A science-fiction title exploring technology, society and possible futures.";
      case "Mystery & Thriller" -> "A suspense-driven mystery or thriller built around secrets and discovery.";
      case "Romance" -> "A relationship-focused story about love, choice and personal growth.";
      case "History & Biography" -> "A history or biography title offering perspective on people and society.";
      case "Self-Help & Business" -> "A practical title about habits, work, business and personal effectiveness.";
      case "Technology" -> "A technology and software-engineering title for technical readers.";
      case "Philosophy" -> "A reflective philosophy title about values, meaning and how to live.";
      default -> "A curated title in the Bookstore demo catalog.";
    };
  }

  private String cover(String title){
    return "https://placehold.co/320x460?text="+title
      .replace(" ","+").replace(",","").replace("'","");
  }
}
