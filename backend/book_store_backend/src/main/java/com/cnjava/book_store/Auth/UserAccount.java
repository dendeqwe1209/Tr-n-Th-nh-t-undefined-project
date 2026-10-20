package com.cnjava.book_store.Auth;

import java.time.LocalDateTime;
import jakarta.persistence.*;

@Entity
@Table(name="user_account",uniqueConstraints={
  @UniqueConstraint(name="uk_user_account_email",columnNames="email"),
  @UniqueConstraint(name="uk_user_account_username",columnNames="username")
})
public class UserAccount {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
  private long id;

  @Column(nullable=false,length=120)
  private String fullName;

  @Column(nullable=false,length=180)
  private String email;

  @Column(length=80)
  private String username;

  @Column(nullable=false,length=500)
  private String passwordHash;

  @Enumerated(EnumType.STRING)
  @Column(nullable=false,length=20)
  private UserRole role=UserRole.USER;

  @Column(nullable=false)
  private LocalDateTime createdAt;

  @PrePersist
  public void onCreate(){
    if(createdAt==null) createdAt=LocalDateTime.now();
    if(email!=null) email=email.trim().toLowerCase();
    if(username!=null && username.isBlank()) username=null;
  }

  public long getId(){return id;} public void setId(long id){this.id=id;}
  public String getFullName(){return fullName;} public void setFullName(String v){fullName=v;}
  public String getEmail(){return email;} public void setEmail(String v){email=v;}
  public String getUsername(){return username;} public void setUsername(String v){username=v;}
  public String getPasswordHash(){return passwordHash;} public void setPasswordHash(String v){passwordHash=v;}
  public UserRole getRole(){return role;} public void setRole(UserRole v){role=v==null?UserRole.USER:v;}
  public LocalDateTime getCreatedAt(){return createdAt;} public void setCreatedAt(LocalDateTime v){createdAt=v;}
}
