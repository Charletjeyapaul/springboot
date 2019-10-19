package com.test.app.payments.repository;

import com.test.app.payments.model.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for read and write operations from database
 */
@Repository
public interface UserAccountRepository extends JpaRepository<UserAccount, String> {

    UserAccount findUserByAccountNo(String accountNo);

    UserAccount findUserByPayId(String payId);
}
