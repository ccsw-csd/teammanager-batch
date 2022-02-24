package com.capgemini.ccsw.teammanager.batch.ldap;

/**
 * @author pajimene
 *
 */
public interface GroupLdapRepository {

   /**
   * Borra los grupos de la tabla temporal
   */
   void deleteAllTemporary();

   /**
   * Guarda el listado de grupos la tabla temporal
   */
   void persistAllTemporary();

   /**
   * Mueve los datos temporales a la tabla real
   */
   void moveTemporaryToRealGroup();

}
