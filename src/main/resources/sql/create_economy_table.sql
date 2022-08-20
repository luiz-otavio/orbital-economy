CREATE IF NOT EXISTS `player_economy`(
    unique_id CHAR(36) NOT NULL PRIMARY KEY,
    nickname VARCHAR(16) NOT NULL UNIQUE,
    currency DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
