package com.cnjava.book_store.Auth;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public final class PasswordUtil {
  private static final int ITERATIONS=120000;
  private static final int KEY_LENGTH=256;
  private static final SecureRandom RANDOM=new SecureRandom();

  private PasswordUtil(){}

  public static String hash(String password){
    try{
      byte[] salt=new byte[16];
      RANDOM.nextBytes(salt);
      byte[] derived=derive(password.toCharArray(),salt);
      return Base64.getEncoder().encodeToString(salt)+":"+Base64.getEncoder().encodeToString(derived);
    }catch(Exception e){
      throw new IllegalStateException("Could not hash password",e);
    }
  }

  public static boolean matches(String password,String stored){
    try{
      if(stored==null) return false;
      String[] parts=stored.split(":",2);
      if(parts.length!=2) return false;
      byte[] salt=Base64.getDecoder().decode(parts[0]);
      byte[] expected=Base64.getDecoder().decode(parts[1]);
      byte[] actual=derive(password.toCharArray(),salt);
      return MessageDigest.isEqual(expected,actual);
    }catch(Exception e){
      return false;
    }
  }

  private static byte[] derive(char[] password,byte[] salt) throws Exception{
    PBEKeySpec spec=new PBEKeySpec(password,salt,ITERATIONS,KEY_LENGTH);
    try{
      return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
    }finally{
      spec.clearPassword();
    }
  }
}
