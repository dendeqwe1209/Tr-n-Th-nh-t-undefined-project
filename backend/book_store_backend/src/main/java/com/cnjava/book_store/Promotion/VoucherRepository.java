package com.cnjava.book_store.Promotion;

import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VoucherRepository extends JpaRepository<Voucher,Long> {
  Optional<Voucher> findByCodeIgnoreCase(String code);
  Optional<Voucher> findFirstByUserIdAndWelcomeTrue(long userId);
  List<Voucher> findByUserIdOrderByCreatedAtDesc(long userId);
  boolean existsByCodeIgnoreCase(String code);
}
