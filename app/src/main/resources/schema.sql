CREATE TABLE IF NOT EXISTS EVENT (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  start_date TIMESTAMP NOT NULL,
  end_date TIMESTAMP NOT NULL,
  announcement_start TIMESTAMP NOT NULL,
  announcement_end TIMESTAMP NOT NULL,
  max_participants INT NOT NULL,
  winning_numbers VARCHAR(255) NOT NULL,
  pre_assigned_phone VARCHAR(20)
);

CREATE TABLE IF NOT EXISTS PARTICIPANT (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  event_id BIGINT NOT NULL,
  phone_number VARCHAR(255) NOT NULL UNIQUE,
  registered_at TIMESTAMP NOT NULL,
  is_verified BOOLEAN NOT NULL,
  check_count INT NOT NULL,
  FOREIGN KEY (event_id) REFERENCES EVENT(id)
);

CREATE TABLE IF NOT EXISTS lotto_number (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  participant_id BIGINT NOT NULL,
  numbers VARCHAR(6) NOT NULL,
  prize_rank INT NOT NULL,
  issued_at TIMESTAMP,
  FOREIGN KEY (participant_id) REFERENCES PARTICIPANT(id)
);

CREATE INDEX IF NOT EXISTS idx_lotto_participant ON lotto_number(participant_id);

CREATE TABLE IF NOT EXISTS sms_history (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  participant_id BIGINT,
  target_phone_number VARCHAR(20) NOT NULL,
  category VARCHAR(50) NOT NULL,
  content VARCHAR(500) NOT NULL,
  sent_at TIMESTAMP,
  FOREIGN KEY (participant_id) REFERENCES PARTICIPANT(id)
);

CREATE INDEX IF NOT EXISTS idx_sms_participant ON sms_history(participant_id);

CREATE TABLE IF NOT EXISTS WINNING_SLOT (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  event_id BIGINT NOT NULL,
  sequence_no INT NOT NULL,
  prize_rank INT NOT NULL,
  FOREIGN KEY (event_id) REFERENCES EVENT(id)
);

CREATE INDEX IF NOT EXISTS idx_slot_event_seq ON WINNING_SLOT(event_id, sequence_no);
