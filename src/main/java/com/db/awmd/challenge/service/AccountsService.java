package com.db.awmd.challenge.service;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.domain.AccountUpdate;
import com.db.awmd.challenge.domain.Transfer;
import com.db.awmd.challenge.exception.AccountNotFoundException;
import com.db.awmd.challenge.exception.NotEnoughFundsException;
import com.db.awmd.challenge.exception.TransferBetweenSameAccountException;
import com.db.awmd.challenge.repository.AccountsRepository;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AccountsService {

  @Getter
  private final AccountsRepository accountsRepository;

  @Autowired
  public AccountsService(AccountsRepository accountsRepository,NotificationService notificationService) {
    this.accountsRepository = accountsRepository;
    this.notificationService = notificationService;
   }

  public void createAccount(Account account) {
    this.accountsRepository.createAccount(account);
  }

  public Account getAccount(String accountId) {
    return this.accountsRepository.getAccount(accountId);
  }
  //Make a transction
  public void makeTransfer(Transfer transfer) throws AccountNotFoundException, NotEnoughFundsException, TransferBetweenSameAccountException {

      final Account accountFrom = accountsRepository.getAccount(transfer.getAccountFromId());
      final Account accountTo = accountsRepository.getAccount(transfer.getAccountToId());
      final BigDecimal amount = transfer.getAmount();

      transferValidator.validate(accountFrom, accountTo, transfer);  
    
      boolean successful = accountsRepository.updateAccountsBatch(Arrays.asList(
              new AccountUpdate(accountTo.getAccountId(), amount)
              ));

      if (successful){
          notificationService.notifyAboutTransfer(accountFrom, " Your account " + transfer.getAccountId() +"is debited by"+ accountTo.getAmount() +".");
          notificationService.notifyAboutTransfer(accountTo, "Your account " + accountFrom.getAccountId() + "is credited by " + transfer.getAmount() + ".");
      }
  }
}
