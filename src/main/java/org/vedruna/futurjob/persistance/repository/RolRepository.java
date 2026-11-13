package org.vedruna.futurjob.persistance.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.vedruna.futurjob.persistance.model.RolEntity;

@Repository
public interface RolRepository extends JpaRepository<RolEntity, Integer> {
  Optional<RolEntity> findByRolName(String rolName);
}
