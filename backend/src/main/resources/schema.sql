CREATE TABLE IF NOT EXISTS portfolio_events (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  event_type VARCHAR(32) NOT NULL,
  page_path VARCHAR(500) NOT NULL DEFAULT '/',
  metadata JSON NOT NULL,
  session_id VARCHAR(120),
  visitor_id VARCHAR(120),
  referrer VARCHAR(1000),
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX portfolio_events_created_at_idx (created_at),
  INDEX portfolio_events_type_idx (event_type),
  INDEX portfolio_events_session_idx (session_id),
  INDEX portfolio_events_visitor_idx (visitor_id)
);

CREATE TABLE IF NOT EXISTS portfolio_contacts (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(100) NOT NULL,
  email VARCHAR(200) NOT NULL,
  subject VARCHAR(160),
  message VARCHAR(4000) NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX portfolio_contacts_created_at_idx (created_at)
);
