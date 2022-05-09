package com.ccsw.teammanager.batch.ldap;

/**
 * @author pajimene
 *
 */
public interface LdapService {

  /**
   * Realiza una carga del LDAP a la tabla temporal
   * @return
   */
  void loadDataFromLdap();

  /**
   *
   */
  void updatePersonInfo();

}
