package com.capgemini.ccsw.teammanager.batch.ponabsence;

/**
 * @author pajimene
 *
 */
public interface PonAbsenceRepository {

   /**
   * Borra los registros de la tabla temporal
   */
   void deleteAllTemporary();

   /**
   * Persiste las ausencias en la tabla temporal
   */
   void persistAllTemporary();

   /**
   * Mueve las ausencias de la tabla temporal a la tabla real
   */
   void moveTemporaryToRealAbsence();

   /**
   * Rellena la fecha de la ultima actualización
   */
   void updateLastDate();

}
