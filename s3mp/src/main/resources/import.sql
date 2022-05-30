-- INSERT INTO coresoftware (versionNumber, stable, fileSize) VALUES (1.0, 1, 325.22);
-- INSERT INTO coresoftware (versionNumber, stable, fileSize) VALUES (1.1, 1, 420.69);
-- INSERT INTO coresoftware (versionNumber, stable, fileSize) VALUES (1.11, 0, 450.54);

-- These are stop gaps so some sort of body is available to represent the substance of the software
INSERT INTO coresoftware (versionNumber, stable, fileSize, contents) VALUES (1.0, 1, 325.22, "kk3wtkNTct");
INSERT INTO coresoftware (versionNumber, stable, fileSize, contents) VALUES (1.1, 1, 420.69, "Va2caDjNFL");
INSERT INTO coresoftware (versionNumber, stable, fileSize, contents) VALUES (1.11, 0, 450.54, "WZFJ1OFTz0");

INSERT INTO user (username, password, role) VALUES ("JohnGeneric", "$2a$10$sOfE9TBKQX5fKLF9PWAMk.Nzw0CvYmleJEn/iUEJXTWwXCbeEsWny", "GENERIC");
INSERT INTO user (username, password, role) VALUES ("BobExperimental", "$2a$10$sOfE9TBKQX5fKLF9PWAMk.Nzw0CvYmleJEn/iUEJXTWwXCbeEsWny", "EXPERIMENTAL");
INSERT INTO user (username, password, role) VALUES ("TomAdmin", "$2a$10$sOfE9TBKQX5fKLF9PWAMk.Nzw0CvYmleJEn/iUEJXTWwXCbeEsWny", "ADMIN");