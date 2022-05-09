package com.ccsw.teammanager.batch.ponabsence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author pajimene
 *
 */
@Service
@Transactional(readOnly = true)
public class PonAbsenceServiceImpl implements PonAbsenceService {

   private static final Logger LOG = LoggerFactory.getLogger(PonAbsenceServiceImpl.class);

   @Autowired
   PonAbsenceRepository ponAbsenceRepository;

   /**
   * {@inheritDoc}
   */
   @Override
   @Transactional(readOnly = false)
   public void loadDataFromPON() {

      LOG.info("********** CARGA AUSENCIAS PON *************");

      LOG.info("Borramos tabla temporal");
      this.ponAbsenceRepository.deleteAllTemporary();

      LOG.info("Insertamos tabla temporal");
      this.ponAbsenceRepository.persistAllTemporary();

      LOG.info("Movemos a tabla real");
      this.ponAbsenceRepository.moveTemporaryToRealAbsence();

      LOG.info("********** FIN CARGA AUSENCIAS PON *************");

   }

   /**
   * {@inheritDoc}
   */
   @Transactional(readOnly = false)
   @Override
   public void updateLastDate() {

      this.ponAbsenceRepository.updateLastDate();

   }

}
