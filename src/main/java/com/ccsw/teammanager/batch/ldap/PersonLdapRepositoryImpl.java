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

        this.jdbcTemplate.update("update person p join t_person t on p.email like '%-external@capgemini.com' and REPLACE(p.email, '-external@capgemini.com', '@capgemini.com') = t.email " //
                + "set p.saga = t.saga, " //
                + "    p.username = t.username, " //
                + "    p.email = t.email, " //
                + "    p.name = t.name, " //
                + "    p.lastname = t.lastname, " //
                + "    p.businesscode = t.businesscode");

        this.jdbcTemplate.update("update person p join t_person t on p.username = t.username and p.active = 1 and p.email <> t.email " //
                + "set p.saga = t.saga, " //
                + "    p.username = t.username, " //
                + "    p.email = t.email, " //
                + "    p.name = t.name, " //
                + "    p.lastname = t.lastname, " //
                + "    p.businesscode = t.businesscode");

        this.jdbcTemplate.update("INSERT INTO person (saga, username, email, name, lastname, center_id, businesscode, active, created_by_ldap) " + //
                "select p.saga, p.username, p.email, p.name, p.lastname, ct.center_id, p.businesscode, 1, 1 " + //
                "from t_person p left join center_transcode ct on p.center = ct.name " + //
                "where p.saga != '' and not exists (select 1 from person where email=p.email and active = 1)");

        this.jdbcTemplate.update("update person p set p.active = 0 where not exists (select 1 from t_person where email = p.email)");

        this.jdbcTemplate.update("update person set grade = (select grade from t_person where t_person.email = person.email) where created_by_ldap = 1 and active = 1");
        this.jdbcTemplate.update("update person set saga = (select saga from t_person where t_person.email = person.email) where created_by_ldap = 1 and active = 1");

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

    @Override
    public void updatePersonSagaFromGlobalId() {

        this.jdbcTemplate.update("delete from saga_transcode");

        this.jdbcTemplate.update("insert into saga_transcode(username) " //
                + "select distinct username from person " //
                + "where saga not in (select saga from absence_pon where extract(year from now())-year <= 1) " //
                + "and active = 1");

        this.jdbcTemplate.update("update saga_transcode t set saga = (select global_employee_id from t_person where username = t.username)");

        this.jdbcTemplate.update("delete from saga_transcode where saga is null");

        this.jdbcTemplate.update("update person p " //
                + "set saga = (select saga from saga_transcode s where s.username = p.username) "//
                + "where p.active = 1 and p.username in (select username from saga_transcode)");

    }

}
