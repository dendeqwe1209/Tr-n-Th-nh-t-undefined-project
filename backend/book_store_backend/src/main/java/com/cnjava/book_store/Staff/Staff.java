package com.cnjava.book_store.Staff;
import jakarta.persistence.*;
@Entity @Table(name="staff")
public class Staff {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private long id;
  private String fullName; private String email; private String phone;
  public Staff(){}
  public Staff(String fullName,String email,String phone){this.fullName=fullName;this.email=email;this.phone=phone;}
  public long getId(){return id;} public void setId(long id){this.id=id;}
  public String getFullName(){return fullName;} public void setFullName(String fullName){this.fullName=fullName;}
  public String getEmail(){return email;} public void setEmail(String email){this.email=email;}
  public String getPhone(){return phone;} public void setPhone(String phone){this.phone=phone;}
}
