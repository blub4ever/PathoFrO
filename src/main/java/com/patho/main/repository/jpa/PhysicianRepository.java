package com.patho.main.repository.jpa;

import com.patho.main.model.Physician;
import com.patho.main.model.person.Person;
import com.patho.main.repository.jpa.custom.PhysicianRepositoryCustom;

import java.util.List;
import java.util.Optional;

public interface PhysicianRepository extends BaseRepository<Physician, Long>, PhysicianRepositoryCustom {

    Optional<Physician> findOptionalByPerson(Person person);

    Optional<Physician> findOptionalByUid(String uid);

    Optional<Physician> findOptionalByPersonLastName(String name);

    List<Physician> findAllByPersonLastName(String name);

}
