package com.ccsw.teammanager.batch.ponabsence;

/**
 * @author pajimene
 *
 */
public interface PonAbsenceService {

  /**
   * Realiza una carga del PON a la tabla temporal
   * @return
   */
  void loadDataFromPON();

  /**
   * Actualiza la fecha de carga
   */
  void updateLastDate();
}
