package com.ccsw.teammanager.batch.ponabsence;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * @author pajimene
 *
 */
@Repository
public class PonAbsenceRepositoryImpl implements PonAbsenceRepository {

    private static final Logger LOG = LoggerFactory.getLogger(PonAbsenceRepositoryImpl.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
    * {@inheritDoc}
    */
    @Override
    public void deleteAllTemporary() {

        this.jdbcTemplate.update("delete from t_absence");
    }

    /**
    * {@inheritDoc}
    */
    @Override
    public void moveTemporaryToRealAbsence() {

        this.jdbcTemplate.update("delete from t_absence where status = 'R'");
        List<Integer> years = this.jdbcTemplate.queryForList("select distinct year from t_absence", Integer.class);

        for (int year : years) {

            for (int month = 1; month <= 12; month++) {

                LOG.info("\tMovemos año [" + year + "] y mes [" + month + "]");

                this.jdbcTemplate.update("delete from absence_pon where year = " + year + " and month = " + month);
                this.jdbcTemplate.update("insert into absence_pon(saga, year, month, date, status, type) " + //
                        "select distinct saga, year, month(d.date) as month, d.date, status, " + //
                        "(case when type = 'VAC' then 'VAC' else 'OTH' end) " + //
                        "    from t_absence a join dates d on d.date between a.from_date and a.to_date " + //
                        "    where year = " + year + " and month(d.date) = " + month);
            }

            this.jdbcTemplate.update("delete from absence_pon " + //
                    "where year = " + year + " and (saga, date) in " + //
                    "(select saga, f.date from person p join festive f on p.center_id = f.center_id and f.year = " + year + ")");
        }

        this.jdbcTemplate.update("delete from absence_pon where WEEKDAY(date) >= 5");
    }

    /**
    * {@inheritDoc}
    */
    @Override
    public void persistAllTemporary() {

        this.jdbcTemplate.update("insert into t_absence select * from personal.t_absence");

    }

    /**
    * {@inheritDoc}
    */
    @Override
    public void updateLastDate() {

        this.jdbcTemplate.update("update properties set value = (select value from personal.properties where code = 'LOAD_LDAP_PON') where code = 'LOAD_LDAP_PON'");

        this.jdbcTemplate.update("update properties set value = now() where code = 'LOAD_PERSONAL'");

    }
}
