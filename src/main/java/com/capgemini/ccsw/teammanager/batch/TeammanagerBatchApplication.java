package com.capgemini.ccsw.teammanager.batch;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.Transactional;

import com.capgemini.ccsw.teammanager.batch.ldap.LdapService;
import com.capgemini.ccsw.teammanager.batch.ponabsence.PonAbsenceService;

@SpringBootApplication
public class TeammanagerBatchApplication implements CommandLineRunner {

  @Autowired
  LdapService ldapService;

  @Autowired
  PonAbsenceService ponAbsenceService;

  public static void main(String[] args) {

    SpringApplication.run(TeammanagerBatchApplication.class, args);
  }

  @Override
  @Transactional(readOnly = false)
  public void run(String... args) throws Exception {

    this.ldapService.loadDataFromLdap();
    this.ponAbsenceService.loadDataFromPON();

    this.ldapService.updatePersonInfo();
    this.ponAbsenceService.updateLastDate();
  }

}
