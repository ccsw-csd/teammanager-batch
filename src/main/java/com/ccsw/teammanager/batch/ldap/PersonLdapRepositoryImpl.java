package com.ccsw.teammanager.batch.ldap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * @author pajimene
 *
 */
@Repository
public class PersonLdapRepositoryImpl implements PersonLdapRepository {

   @Autowired
   private JdbcTemplate jdbcTemplate;

   /**
   * {@inheritDoc}
   */
   @Override
   public void deleteAllTemporary() {

      this.jdbcTemplate.update("delete from t_person");
   }

   /**
   * {@inheritDoc}
   */
   @Override
   public void moveTemporaryToRealPerson() {

      this.jdbcTemplate.update("INSERT INTO person (saga, username, email, name, lastname, center_id, businesscode, active, created_by_ldap) " + //
            "select p.saga, p.username, p.email, p.name, p.lastname, ct.center_id, p.businesscode, 1, 1 " + //
            "from " + //
            "    t_person p left join center_transcode ct on p.center = ct.name " + //
            "where " + //
            "    p.saga != '' and p.saga not in (select saga from person)");
      this.jdbcTemplate.update("update person set active = 0 where created_by_ldap = 1");
      this.jdbcTemplate.update("update person set active = 1 where saga in (select saga from t_person)");

      this.jdbcTemplate.update(
            "update person set grade = (select grade from t_person where t_person.saga = person.saga and t_person.username = person.username) where created_by_ldap = 1");

   }

   /**
   * {@inheritDoc}
   */
   @Override
   public void updatePersonInfoAndRemoveInactivePerson() {

      this.jdbcTemplate.update("update person set with_absence = 0");
      this.jdbcTemplate.update("update person set with_absence = 1 " + //
            "where ( " + //
            "    exists (select 1 from absence_pon ap where ap.saga = person.saga) " + //
            "    or exists (select 1 from absence_local ap where ap.saga = person.saga) " + //
            ")");

      this.jdbcTemplate.update("update person set with_pon = 0");
      this.jdbcTemplate.update("update person set with_pon = 1 " + //
            "where  exists (select 1 from absence_pon ap where ap.saga = person.saga) ");

      String inactivePerson = "select id from person where active = 0 and with_absence = 0";

      this.jdbcTemplate.update("delete from group_manager where person_id in (" + inactivePerson + ")");
      this.jdbcTemplate.update("delete from group_members where person_id in (" + inactivePerson + ")");
      this.jdbcTemplate.update("delete from person where active = 0 and with_absence = 0");
   }

   /**
   * {@inheritDoc}
   */
   @Override
   public void persistAllTemporary() {

      this.jdbcTemplate.update("insert into t_person select * from personal.t_person");

   }

}
