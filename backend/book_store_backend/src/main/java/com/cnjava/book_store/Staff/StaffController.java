package com.cnjava.book_store.Staff;
import java.util.List;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/admin/staff-management")
public class StaffController {
  private final StaffRepository repo; public StaffController(StaffRepository repo){this.repo=repo;}
  @GetMapping("/staffs") public List<Staff> all(){return repo.findAll();}
  @PostMapping public ResponseEntity<Staff> create(@RequestBody Staff s){s.setId(0);return ResponseEntity.status(HttpStatus.CREATED).body(repo.save(s));}
  @PutMapping("/staffs/{id}") public ResponseEntity<Staff> update(@PathVariable long id,@RequestBody Staff s){return repo.findById(id).map(x->{x.setFullName(s.getFullName());x.setEmail(s.getEmail());x.setPhone(s.getPhone());return ResponseEntity.ok(repo.save(x));}).orElse(ResponseEntity.notFound().build());}
  @DeleteMapping("/staffs/{id}") public ResponseEntity<Void> delete(@PathVariable long id){if(!repo.existsById(id))return ResponseEntity.notFound().build();repo.deleteById(id);return ResponseEntity.noContent().build();}
}
