package com.capgemini.ccsw.teammanager.batch.ldap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * @author pajimene
 *
 */
@Repository
public class GroupLdapRepositoryImpl implements GroupLdapRepository {

   @Autowired
   private JdbcTemplate jdbcTemplate;

   /**
   * {@inheritDoc}
   */
   @Override
   public void moveTemporaryToRealGroup() {

      //Insertamos subgrupos
      this.jdbcTemplate.update("insert into t_subgroups " + "select group_cn, user_cn from t_members tm where user_cn in (select cn from t_group)");

      //Borramos los miembros que no son personas
      this.jdbcTemplate.update("delete from t_members where user_cn not in (select username from person)");
      this.jdbcTemplate.update("delete from t_admins where user_cn not in (select username from person)");

      //Borramos los grupos dinámicos
      this.jdbcTemplate.update("delete from t_group where group_type = 'Dynamic'");

      //Actualizamos el tipo de miembros que tiene el grupo
      this.jdbcTemplate.update("update t_group set operation_type = ''");
      this.jdbcTemplate.update("update t_group set operation_type = concat(operation_type, 'A') " + "where cn in (select distinct group_cn from t_admins)");
      this.jdbcTemplate.update("update t_group set operation_type = concat(operation_type, 'M') " + "where cn in (select distinct group_cn from t_members)");
      this.jdbcTemplate.update("update t_group set operation_type = concat(operation_type, 'S') " + "where cn in (select distinct group_cn from t_subgroups)");

      //Borramos los grupos que NO tienen admins o que SOLO tienen admins
      this.jdbcTemplate.update("delete from t_group where operation_type not like 'A%'");
      this.jdbcTemplate.update("delete from t_group where operation_type = 'A'");

      //Borramos miembros sin grupo
      this.jdbcTemplate.update("delete from t_operations");
      this.jdbcTemplate
            .update("insert into t_operations(value_1, value_2) " + "select group_cn, user_cn from t_members where group_cn in (select cn from t_group)");
      this.jdbcTemplate.update("delete from t_members");
      this.jdbcTemplate.update("insert into t_members select value_1, value_2 from t_operations");

      //Borramos admins sin grupo
      this.jdbcTemplate.update("delete from t_operations");
      this.jdbcTemplate
            .update("insert into t_operations(value_1, value_2) " + "select group_cn, user_cn from t_admins where group_cn in (select cn from t_group)");
      this.jdbcTemplate.update("delete from t_admins");
      this.jdbcTemplate.update("insert into t_admins select value_1, value_2 from t_operations");

      //Borramos subgrupos sin grupo
      this.jdbcTemplate.update("delete from t_operations");
      this.jdbcTemplate.update(
            "insert into t_operations(value_1, value_2) " + "select group_cn, group_child_cn from t_subgroups where group_cn in (select cn from t_group)");
      this.jdbcTemplate.update("delete from t_subgroups");
      this.jdbcTemplate.update("insert into t_subgroups select value_1, value_2 from t_operations");

      //Marcamos grupos para insertar o updatear
      this.jdbcTemplate.update("update t_group set operation_type = null");
      this.jdbcTemplate.update("update t_group set operation_type = 'I' where not exists (select 1 from `group` where external_id = cn)");
      this.jdbcTemplate.update("update t_group set operation_type = 'U' where exists (select 1 from `group` where external_id = cn)");

      //Insertamos los grupos nuevos y actualizamos los viejos
      this.jdbcTemplate.update("insert into `group`(name, external_id) select name, cn from t_group where operation_type = 'I'");
      this.jdbcTemplate.update("update `group` set name = (select name from t_group where cn = external_id) "
            + "where external_id in (select cn from t_group where operation_type = 'U')");

      //Borramos la info de los grupos automaticos
      this.jdbcTemplate.update("delete from group_manager where group_id in (select id from `group` where external_id is not null)");
      this.jdbcTemplate.update("delete from group_members where group_id in (select id from `group` where external_id is not null)");
      this.jdbcTemplate.update("delete from group_subgroup where group_id in (select id from `group` where external_id is not null)");

      //Insertamos la info de los grupos automaticos
      this.jdbcTemplate.update("insert into group_manager(group_id, person_id) " + //
            "select g.id, p.id " + //
            "from t_admins a " + //
            "    join `group` g on a.group_cn = g.external_id " + //
            "    join person p on a.user_cn = p.username");
      this.jdbcTemplate.update("insert into group_members(group_id, person_id) " + //
            "select g.id, p.id " + //
            "from t_members a " + //
            "    join `group` g on a.group_cn = g.external_id " + //
            "    join person p on a.user_cn = p.username ");
      this.jdbcTemplate.update("insert into group_subgroup(group_id, subgroup_id) " + //
            "select g1.id, g2.id " + //
            "from t_subgroups ts " + //
            "    join `group` g1 on ts.group_cn = g1.external_id " + //
            "    join `group` g2 on ts.group_child_cn = g2.external_id ");
   }

   /**
   * {@inheritDoc}
   */
   @Override
   public void deleteAllTemporary() {

      this.jdbcTemplate.update("delete from t_group");
      this.jdbcTemplate.update("delete from t_members");
      this.jdbcTemplate.update("delete from t_admins");
      this.jdbcTemplate.update("delete from t_subgroups");

   }

   /**
   * {@inheritDoc}
   */
   @Override
   public void persistAllTemporary() {

      this.jdbcTemplate.update("insert into t_group select * from personal.t_group");
      this.jdbcTemplate.update("insert into t_members select * from personal.t_members");
      this.jdbcTemplate.update("insert into t_admins select * from personal.t_admins");
      this.jdbcTemplate.update("insert into t_subgroups select * from personal.t_subgroups");

   }

}
