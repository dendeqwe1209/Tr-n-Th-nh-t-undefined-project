package com.cnjava.book_store.Auth;

import java.time.LocalDateTime;
import jakarta.persistence.*;

@Entity
@Table(name="user_account", uniqueConstraints=@UniqueConstraint(name="uk_user_account_email", columnNames="email"))
public class UserAccount {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
  private long id;

  @Column(nullable=false,length=120)
  private String fullName;

  @Column(nullable=false,length=180)
  private String email;

  @Column(nullable=false,length=500)
  private String passwordHash;

  @Column(nullable=false)
  private LocalDateTime createdAt;

  @PrePersist
  public void onCreate(){
    if(createdAt==null) createdAt=LocalDateTime.now();
    if(email!=null) email=email.trim().toLowerCase();
  }

  public long getId(){return id;}
  public void setId(long id){this.id=id;}
  public String getFullName(){return fullName;}
  public void setFullName(String fullName){this.fullName=fullName;}
  public String getEmail(){return email;}
  public void setEmail(String email){this.email=email;}
  public String getPasswordHash(){return passwordHash;}
  public void setPasswordHash(String passwordHash){this.passwordHash=passwordHash;}
  public LocalDateTime getCreatedAt(){return createdAt;}
  public void setCreatedAt(LocalDateTime createdAt){this.createdAt=createdAt;}
}
