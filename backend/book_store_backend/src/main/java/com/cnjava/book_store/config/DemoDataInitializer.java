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
      }else{
        if(book.getDescription()==null||book.getDescription().isBlank()) book.setDescription(description(seed));
      }
      if(book.getPreviewText()==null||book.getPreviewText().isBlank()) book.setPreviewText(preview(seed));

      // Category artwork is the default visual identity for the demo catalog.
      // This also migrates existing seed books away from external/placeholder covers.
      book.setBook_cover(categoryCover(seed.category()));

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

  private String preview(SeedBook b){
    return switch(b.category()){
      case "Classics" -> b.title()+" opens a window onto enduring human conflicts: ambition, dignity, social pressure and the choices people make when the world around them begins to change. This demo preview is written for the app and is not copied from the published book.";
      case "Fiction" -> "In "+b.title()+", the reader is invited into a character-driven world where memory, relationships and difficult decisions gradually shape the story. This short preview is an original summary-style sample created for the demo.";
      case "Fantasy" -> b.title()+" introduces a world larger than ordinary life, where unfamiliar places, loyalties and dangers pull the characters toward an adventure that tests what they believe. This is an app-written preview rather than text from the book.";
      case "Science Fiction" -> b.title()+" begins from a speculative idea about technology, society or the future, then asks what that change might mean for ordinary people. The text shown here is an original preview written for this bookstore demo.";
      case "Mystery & Thriller" -> "A question sits at the centre of "+b.title()+": something does not fit, and every new clue changes how the reader understands what happened. This spoiler-light sample is written by the app team and does not reproduce the novel.";
      case "Romance" -> b.title()+" focuses on two people whose feelings are shaped by timing, vulnerability and the choices they make around one another. This is a short original preview for the demo storefront.";
      case "History & Biography" -> b.title()+" follows real people, events and ideas to show how personal decisions connect with larger historical change. This preview is a concise editorial introduction created for the app.";
      case "Self-Help & Business" -> b.title()+" presents practical ideas that readers can examine, test and apply to work or everyday life. This preview summarises the learning direction of the book without reproducing its copyrighted text.";
      case "Technology" -> b.title()+" introduces technical ideas through principles, patterns and problem-solving habits that can be applied to software and engineering work. This is an original preview produced for the demo.";
      case "Philosophy" -> b.title()+" invites the reader to consider judgment, values and how to respond to events that cannot always be controlled. This short preview is an original editorial sample for the app.";
      default -> "A short editorial preview created for the Bookstore demo. It gives readers a sense of the book without reproducing the original text.";
    };
  }

  private String categoryCover(String category){
    return switch(category){
      case "Classics" -> "/images/categories/classics.svg";
      case "Fiction" -> "/images/categories/fiction.svg";
      case "Fantasy" -> "/images/categories/fantasy.svg";
      case "Science Fiction" -> "/images/categories/science-fiction.svg";
      case "Mystery & Thriller" -> "/images/categories/mystery-thriller.svg";
      case "Romance" -> "/images/categories/romance.svg";
      case "History & Biography" -> "/images/categories/history-biography.svg";
      case "Self-Help & Business" -> "/images/categories/self-help-business.svg";
      case "Technology" -> "/images/categories/technology.svg";
      case "Philosophy" -> "/images/categories/philosophy.svg";
      default -> "/images/categories/fiction.svg";
    };
  }
}
