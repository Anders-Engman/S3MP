-- Import.sql
-- Author: Anders Engman
-- Date: 6/3/22
-- This file seeds the db with mock data on server start.

INSERT INTO coresoftware (versionNumber, stable, fileSize, contents) VALUES (1.0, 1, 325.22, "kk3wtkNTct");
INSERT INTO coresoftware (versionNumber, stable, fileSize, contents) VALUES (1.1, 1, 421.68, "Va2caDjNFL");
INSERT INTO coresoftware (versionNumber, stable, fileSize, contents) VALUES (1.11, 0, 450.54, "WZFJ1OFTz0");

-- The 'password' string is an encrypted version of 'pass', effectively each mock user uses 'pass' as a password
INSERT INTO user (username, password, role) VALUES ("JohnGeneric", "$2a$10$sOfE9TBKQX5fKLF9PWAMk.Nzw0CvYmleJEn/iUEJXTWwXCbeEsWny", "GENERIC");
INSERT INTO user (username, password, role) VALUES ("BobExperimental", "$2a$10$sOfE9TBKQX5fKLF9PWAMk.Nzw0CvYmleJEn/iUEJXTWwXCbeEsWny", "EXPERIMENTAL");