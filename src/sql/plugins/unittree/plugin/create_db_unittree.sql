--
-- Table structure for table form_action
--
DROP TABLE IF EXISTS unittree_unit;
CREATE TABLE unittree_unit (
	id_unit INT DEFAULT 0 NOT NULL,
	id_parent INT DEFAULT 0 NOT NULL,
	label VARCHAR(255) DEFAULT '' NOT NULL,
	description VARCHAR(255) DEFAULT '' NOT NULL,
	PRIMARY KEY (id_unit)
);

--
-- Table structure for table unittree_unit_user
--
DROP TABLE IF EXISTS unittree_unit_user;
CREATE TABLE unittree_unit_user (
	id_unit INT DEFAULT 0 NOT NULL,
	id_user INT DEFAULT 0 NOT NULL,
	PRIMARY KEY (id_unit, id_user)
);

--
-- Table structure for table unittree_unit_action
--
DROP TABLE IF EXISTS unittree_action;
CREATE TABLE unittree_action (
	id_action INT DEFAULT 0 NOT NULL,
	name_key VARCHAR(100) DEFAULT '' NOT NULL,
	description_key VARCHAR(100) DEFAULT '' NOT NULL,
	action_url VARCHAR(255) DEFAULT '' NOT NULL,
	icon_url VARCHAR(255) DEFAULT '' NOT NULL,
	action_permission VARCHAR(50) DEFAULT '' NOT NULL,
	action_type VARCHAR(50) DEFAULT '' NOT NULL,
	PRIMARY KEY (id_action)
);

--
-- Table structure for table form_action
--
DROP TABLE IF EXISTS unittree_sector;
CREATE TABLE unittree_sector (
	id_sector INT DEFAULT 0 NOT NULL,
	name VARCHAR(255) DEFAULT '' NOT NULL,
	number_sector VARCHAR(20) DEFAULT '' NOT NULL,
	PRIMARY KEY (id_sector)
);

--
-- Table structure for table unittree_unit_user
--
DROP TABLE IF EXISTS unittree_unit_sector;
CREATE TABLE unittree_unit_sector (
	id_unit INT DEFAULT 0 NOT NULL,
	id_sector INT DEFAULT 0 NOT NULL,
	PRIMARY KEY (id_unit, id_sector)
);
