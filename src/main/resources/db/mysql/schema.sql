-- MySQL Workbench Forward Engineering

SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION';

-- -----------------------------------------------------
-- Schema futurclab
-- -----------------------------------------------------
DROP SCHEMA IF EXISTS `futurclab` ;

-- -----------------------------------------------------
-- Schema futurclab
-- -----------------------------------------------------
CREATE SCHEMA IF NOT EXISTS `futurclab` DEFAULT CHARACTER SET utf8 ;
SHOW WARNINGS;
USE `futurclab` ;

-- -----------------------------------------------------
-- Table `futurclab`.`roles`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `futurclab`.`roles` ;

SHOW WARNINGS;
CREATE TABLE IF NOT EXISTS `futurclab`.`roles` (
  `rol_id` INT NOT NULL AUTO_INCREMENT,
  `rol_name` VARCHAR(5) NOT NULL,
  PRIMARY KEY (`rol_id`))
ENGINE = InnoDB;

SHOW WARNINGS;
CREATE UNIQUE INDEX `rol_name_UNIQUE` ON `futurclab`.`roles` (`rol_name` ASC) VISIBLE;

SHOW WARNINGS;

-- -----------------------------------------------------
-- Table `futurclab`.`users`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `futurclab`.`users` ;

SHOW WARNINGS;
CREATE TABLE IF NOT EXISTS `futurclab`.`users` (
  `user_id` INT NOT NULL AUTO_INCREMENT,
  `username` VARCHAR(20) NOT NULL,
  `email` VARCHAR(90) NOT NULL,
  `password` CHAR(60) NOT NULL,
  `create_date` DATETIME NOT NULL,
  `account_expiration_date` DATETIME,
  `is_locked` TINYINT(1),
  `credentials_expiration_date` DATETIME,
  `is_enabled` TINYINT(1),
  `roles_rol_id` INT NOT NULL,
  PRIMARY KEY (`user_id`, `roles_rol_id`),
  CONSTRAINT `fk_users_roles`
    FOREIGN KEY (`roles_rol_id`)
    REFERENCES `futurclab`.`roles` (`rol_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;

SHOW WARNINGS;
CREATE UNIQUE INDEX `username_UNIQUE` ON `futurclab`.`users` (`username` ASC) VISIBLE;

SHOW WARNINGS;
CREATE UNIQUE INDEX `email_UNIQUE` ON `futurclab`.`users` (`email` ASC) VISIBLE;

SHOW WARNINGS;
CREATE INDEX `fk_users_roles_idx` ON `futurclab`.`users` (`roles_rol_id` ASC) VISIBLE;

SHOW WARNINGS;

SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
