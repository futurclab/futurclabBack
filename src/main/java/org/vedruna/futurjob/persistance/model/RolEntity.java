package org.vedruna.futurjob.persistance.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.List;
import lombok.Data;

@Data
@Entity
@Table(name = "roles")
public class RolEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "rol_id")
  Integer userId;

  @Column(name = "rol_name")
  String rolName;

  @OneToMany(mappedBy = "userRol", cascade = CascadeType.ALL)
  List<UserEntity> users;
}
