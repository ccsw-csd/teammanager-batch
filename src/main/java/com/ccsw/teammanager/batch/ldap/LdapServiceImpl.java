package com.ccsw.teammanager.batch.ldap;

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
public class LdapServiceImpl implements LdapService {

    private static final Logger LOG = LoggerFactory.getLogger(LdapServiceImpl.class);

    @Autowired
    private PersonLdapRepository personLdap;

    @Autowired
    private GroupLdapRepository groupLdap;

    /**
    * {@inheritDoc}
    */
    @Override
    @Transactional(readOnly = false)
    public void loadDataFromLdap() {

        loadPersonDataFromLdap();
        loadGroupDataFromLdap();
    }

    private void loadGroupDataFromLdap() {

        LOG.info("********** CARGA GRUPOS LDAP *************");
        LOG.info("Borramos tabla temporal");
        this.groupLdap.deleteAllTemporary();

        LOG.info("Insertamos tabla temporal");
        this.groupLdap.persistAllTemporary();

        LOG.info("Movemos a tabla real");
        this.groupLdap.moveTemporaryToRealGroup();

        LOG.info("********** FIN CARGA GRUPOS LDAP *************");
    }

    private void loadPersonDataFromLdap() {

        LOG.info("********** CARGA PERSONAS LDAP *************");
        LOG.info("Borramos tabla temporal");
        this.personLdap.deleteAllTemporary();

        LOG.info("Insertamos tabla temporal");
        this.personLdap.persistAllTemporary();

        LOG.info("Movemos a tabla real");
        this.personLdap.moveTemporaryToRealPerson();

        LOG.info("********** FIN CARGA PERSONAS LDAP *************");
    }

    /**
    * {@inheritDoc}
    */
    @Transactional(readOnly = false)
    @Override
    public void updatePersonInfo() {
        LOG.info("Saneamos codigo SAGA");
        this.personLdap.updatePersonSagaFromGlobalId();

        LOG.info("Limpiamos personal inactivo");
        this.personLdap.updatePersonInfoAndRemoveInactivePerson();

    }

}
