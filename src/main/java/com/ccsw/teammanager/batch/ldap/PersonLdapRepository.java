package com.ccsw.teammanager.batch.ldap;

/**
 * @author pajimene
 *
 */
public interface PersonLdapRepository {

    /**
    * Borra las personas de la tabla temporal
    */
    void deleteAllTemporary();

    /**
    * Guarda el listado de personas la tabla temporal
    */
    void persistAllTemporary();

    /**
    * Mueve las personas de la tabla temporal a la tabla real
    */
    void moveTemporaryToRealPerson();

    /**
    * Actualiza informacion de las personas y limpia las personas inactivas que no tienen ausencias
    */
    void updatePersonInfoAndRemoveInactivePerson();

    /**
    * Los códigos SAGA no encontrados, los actualiza con los globalId
    */
    void updatePersonSagaFromGlobalId();

}
