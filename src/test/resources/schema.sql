CREATE SCHEMA IF NOT EXISTS `library` ;
USE `library` ;
CREATE TABLE IF NOT EXISTS `library`.`tbl_author` (`authorId` INT(11) AUTO_INCREMENT NOT NULL, `authorName` VARCHAR(45) NOT NULL, PRIMARY KEY (`authorId`))
CREATE TABLE IF NOT EXISTS `library`.`tbl_publisher` (`publisherId` INT(11) AUTO_INCREMENT NOT NULL, `publisherName` VARCHAR(45) NOT NULL, `publisherAddress` VARCHAR(45) NULL DEFAULT NULL, `publisherPhone` VARCHAR(45) NULL DEFAULT NULL, PRIMARY KEY (`publisherId`))
CREATE TABLE IF NOT EXISTS `library`.`tbl_book` (`bookId` INT(11) AUTO_INCREMENT NOT NULL, `title` VARCHAR(45) NOT NULL, `authId` INT(11) NULL DEFAULT NULL, `pubId` INT(11) NULL DEFAULT NULL, PRIMARY KEY (`bookId`), INDEX `fk_author` (`authId` ASC), INDEX `fk_publisher` (`pubId` ASC), CONSTRAINT `fk_publisher` FOREIGN KEY (`pubId`) REFERENCES `library`.`tbl_publisher` (`publisherId`) ON DELETE CASCADE ON UPDATE CASCADE, CONSTRAINT `fk_author` FOREIGN KEY (`authId`) REFERENCES `library`.`tbl_author` (`authorId`) ON DELETE CASCADE ON UPDATE CASCADE)
CREATE TABLE IF NOT EXISTS `library`.`tbl_library_branch` (`branchId` INT(11) AUTO_INCREMENT NOT NULL, `branchName` VARCHAR(45) NULL DEFAULT NULL, `branchAddress` VARCHAR(45) NULL DEFAULT NULL, PRIMARY KEY (`branchId`))
CREATE TABLE IF NOT EXISTS `library`.`tbl_book_copies` (`bookId` INT(11) NOT NULL, `branchId` INT(11) NOT NULL, `noOfCopies` INT(11) NULL DEFAULT NULL, PRIMARY KEY (`bookId`, `branchId`), INDEX `fk_bc_book` (`bookId` ASC), INDEX `fk_bc_branch` (`branchId` ASC), CONSTRAINT `fk_bc_branch` FOREIGN KEY (`branchId`) REFERENCES `library`.`tbl_library_branch` (`branchId`) ON DELETE CASCADE ON UPDATE CASCADE, CONSTRAINT `fk_bc_book` FOREIGN KEY (`bookId`) REFERENCES `library`.`tbl_book` (`bookId`) ON DELETE CASCADE ON UPDATE CASCADE)
CREATE TABLE IF NOT EXISTS `library`.`tbl_borrower` (`cardNo` INT(11) AUTO_INCREMENT NOT NULL, `name` VARCHAR(45) NULL DEFAULT NULL, `address` VARCHAR(45) NULL DEFAULT NULL, `phone` VARCHAR(45) NULL DEFAULT NULL, PRIMARY KEY (`cardNo`))
CREATE TABLE IF NOT EXISTS `library`.`tbl_book_loans` (`bookId` INT(11) NOT NULL, `branchId` INT(11) NOT NULL, `cardNo` INT(11) NOT NULL, `dateOut` DATETIME NULL DEFAULT NULL, `dueDate` DATETIME NULL DEFAULT NULL, PRIMARY KEY (`bookId`, `branchId`, `cardNo`), INDEX `fk_bl_book` (`bookId` ASC), INDEX `fk_bl_branch` (`branchId` ASC), INDEX `fk_bl_borrower` (`cardNo` ASC), CONSTRAINT `fk_bl_branch` FOREIGN KEY (`branchId`) REFERENCES `library`.`tbl_library_branch` (`branchId`) ON DELETE CASCADE ON UPDATE CASCADE, CONSTRAINT `fk_bl_book` FOREIGN KEY (`bookId`) REFERENCES `library`.`tbl_book` (`bookId`) ON DELETE CASCADE ON UPDATE CASCADE, CONSTRAINT `fk_bl_borrower` FOREIGN KEY (`cardNo`) REFERENCES `library`.`tbl_borrower` (`cardNo`) ON DELETE CASCADE ON UPDATE CASCADE)
