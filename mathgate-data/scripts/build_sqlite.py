#!/usr/bin/env python3
"""
MATHGATE — SQLite Question Bank Compiler (Part 8).
Compiles validated JSONL records into output/sqlite/mathgate_question_bank.db
with all 10 recommended tables and all 8 required indices:
Tables:
  1. subjects
  2. topics
  3. questions
  4. question_options
  5. question_sources
  6. question_attempts
  7. question_packs
  8. question_pack_versions
  9. user_skill_profiles
  10. saved_questions
Indices on:
  subject_id, topic_id, education_level, exam_id, difficulty,
  question_type, source_dataset, fingerprint
"""

import json
import sqlite3
from datetime import datetime, timezone
from pathlib import Path
from typing import Any, Dict

BASE_DIR = Path(__file__).resolve().parent.parent
VALIDATED_JSONL = BASE_DIR / "output" / "jsonl" / "mathgate_validated.jsonl"
SQLITE_DIR = BASE_DIR / "output" / "sqlite"
STATS_DIR = BASE_DIR / "output" / "statistics"
DB_PATH = SQLITE_DIR / "mathgate_question_bank.db"

SCHEMA_SQL = """
PRAGMA foreign_keys = ON;

CREATE TABLE IF NOT EXISTS subjects (
    id TEXT PRIMARY KEY NOT NULL,
    name TEXT NOT NULL,
    parent_group TEXT NOT NULL,
    description TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS topics (
    id TEXT PRIMARY KEY NOT NULL,
    subject_id TEXT NOT NULL,
    name TEXT NOT NULL,
    subtopic TEXT,
    FOREIGN KEY(subject_id) REFERENCES subjects(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS question_packs (
    id TEXT PRIMARY KEY NOT NULL,
    name TEXT NOT NULL,
    license TEXT NOT NULL,
    is_installed INTEGER NOT NULL DEFAULT 1,
    created_at INTEGER NOT NULL
);

CREATE TABLE IF NOT EXISTS question_pack_versions (
    id TEXT PRIMARY KEY NOT NULL,
    pack_id TEXT NOT NULL,
    version_code INTEGER NOT NULL,
    record_count INTEGER NOT NULL,
    sha256 TEXT NOT NULL,
    released_at INTEGER NOT NULL,
    FOREIGN KEY(pack_id) REFERENCES question_packs(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS questions (
    id TEXT PRIMARY KEY NOT NULL,
    pack_id TEXT NOT NULL,
    subject_id TEXT NOT NULL,
    topic_id TEXT NOT NULL,
    subtopic TEXT,
    education_level TEXT NOT NULL,
    class_level TEXT,
    exam_id TEXT,
    difficulty INTEGER NOT NULL,
    question_type TEXT NOT NULL,
    language TEXT NOT NULL,
    question_text TEXT NOT NULL,
    correct_answer_type TEXT NOT NULL,
    correct_answer_value TEXT NOT NULL,
    explanation TEXT,
    solution_steps_json TEXT NOT NULL,
    supporting_facts_json TEXT NOT NULL,
    source_dataset TEXT NOT NULL,
    fingerprint TEXT NOT NULL,
    verified INTEGER NOT NULL DEFAULT 0,
    content_version INTEGER NOT NULL DEFAULT 1
);

CREATE TABLE IF NOT EXISTS question_options (
    id TEXT PRIMARY KEY NOT NULL,
    question_id TEXT NOT NULL,
    option_id TEXT NOT NULL,
    option_text TEXT NOT NULL,
    is_correct INTEGER NOT NULL DEFAULT 0,
    FOREIGN KEY(question_id) REFERENCES questions(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS question_sources (
    question_id TEXT PRIMARY KEY NOT NULL,
    source_dataset TEXT NOT NULL,
    source_record_id TEXT NOT NULL,
    dataset_url TEXT NOT NULL,
    license TEXT NOT NULL,
    source_split TEXT NOT NULL,
    source_revision TEXT NOT NULL,
    FOREIGN KEY(question_id) REFERENCES questions(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS question_attempts (
    id TEXT PRIMARY KEY NOT NULL,
    question_id TEXT NOT NULL,
    subject_id TEXT NOT NULL,
    topic_id TEXT NOT NULL,
    submitted_answer TEXT NOT NULL,
    is_correct INTEGER NOT NULL,
    response_time_ms INTEGER NOT NULL,
    attempted_at INTEGER NOT NULL
);

CREATE TABLE IF NOT EXISTS user_skill_profiles (
    subject_id TEXT NOT NULL,
    topic_id TEXT NOT NULL,
    skill_score REAL NOT NULL,
    accuracy REAL NOT NULL,
    current_difficulty INTEGER NOT NULL,
    attempts_count INTEGER NOT NULL,
    updated_at INTEGER NOT NULL,
    PRIMARY KEY(subject_id, topic_id)
);

CREATE TABLE IF NOT EXISTS saved_questions (
    question_id TEXT PRIMARY KEY NOT NULL,
    saved_at INTEGER NOT NULL,
    notes TEXT
);

CREATE INDEX IF NOT EXISTS idx_questions_subject_id ON questions(subject_id);
CREATE INDEX IF NOT EXISTS idx_questions_topic_id ON questions(topic_id);
CREATE INDEX IF NOT EXISTS idx_questions_education_level ON questions(education_level);
CREATE INDEX IF NOT EXISTS idx_questions_exam_id ON questions(exam_id);
CREATE INDEX IF NOT EXISTS idx_questions_difficulty ON questions(difficulty);
CREATE INDEX IF NOT EXISTS idx_questions_question_type ON questions(question_type);
CREATE INDEX IF NOT EXISTS idx_questions_source_dataset ON questions(source_dataset);
CREATE INDEX IF NOT EXISTS idx_questions_fingerprint ON questions(fingerprint);
CREATE INDEX IF NOT EXISTS idx_topics_subject_id ON topics(subject_id);
CREATE INDEX IF NOT EXISTS idx_question_options_question_id ON question_options(question_id);
"""


def compile_sqlite() -> Dict[str, Any]:
    SQLITE_DIR.mkdir(parents=True, exist_ok=True)
    STATS_DIR.mkdir(parents=True, exist_ok=True)
    if DB_PATH.exists():
        DB_PATH.unlink()

    conn = sqlite3.connect(str(DB_PATH))
    try:
        conn.executescript(SCHEMA_SQL)
        now_ms = int(datetime.now(timezone.utc).timestamp() * 1000)

        pack_id = "PACK_CORE_PERMISSIVE_V1"
        conn.execute(
            "INSERT INTO question_packs (id, name, license, is_installed, created_at) VALUES (?, ?, ?, 1, ?)",
            (pack_id, "MATHGATE Permissive Starter Pack", "MIT / Apache-2.0 / CC-BY-4.0", now_ms),
        )

        lines = VALIDATED_JSONL.read_text(encoding="utf-8").splitlines() if VALIDATED_JSONL.exists() else []
        subjects_seen = set()
        topics_seen = set()
        question_count = 0
        option_count = 0

        for line in lines:
            if not line.strip():
                continue
            rec = json.loads(line)
            subj_id = rec["subject"]
            topic_id = f"{subj_id}_{rec['topic']}"
            if subj_id not in subjects_seen:
                conn.execute(
                    "INSERT OR IGNORE INTO subjects (id, name, parent_group, description) VALUES (?, ?, ?, ?)",
                    (subj_id, subj_id.replace("_", " ").title(), rec["education_level"], f"{subj_id} Practice Bank"),
                )
                subjects_seen.add(subj_id)

            if topic_id not in topics_seen:
                conn.execute(
                    "INSERT OR IGNORE INTO topics (id, subject_id, name, subtopic) VALUES (?, ?, ?, ?)",
                    (topic_id, subj_id, rec["topic"].replace("_", " ").title(), rec.get("subtopic")),
                )
                topics_seen.add(topic_id)

            exam_tags = rec.get("exam_tags") or []
            primary_exam = exam_tags[0] if exam_tags else None
            ans_obj = rec["correct_answer"]

            conn.execute(
                """
                INSERT INTO questions (
                    id, pack_id, subject_id, topic_id, subtopic, education_level,
                    class_level, exam_id, difficulty, question_type, language,
                    question_text, correct_answer_type, correct_answer_value,
                    explanation, solution_steps_json, supporting_facts_json,
                    source_dataset, fingerprint, verified, content_version
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                (
                    rec["id"],
                    pack_id,
                    subj_id,
                    topic_id,
                    rec.get("subtopic"),
                    rec["education_level"],
                    rec.get("class_level"),
                    primary_exam,
                    int(rec["difficulty"]),
                    rec["question_type"],
                    rec.get("language", "en"),
                    rec["question"],
                    ans_obj["type"],
                    str(ans_obj["value"]),
                    rec.get("explanation"),
                    json.dumps(rec.get("solution_steps", []), ensure_ascii=False),
                    json.dumps(rec.get("supporting_facts", []), ensure_ascii=False),
                    rec["source_dataset"],
                    rec["fingerprint"],
                    1 if rec.get("quality", {}).get("verified") else 0,
                    int(rec.get("content_version", 1)),
                ),
            )
            question_count += 1

            for opt in rec.get("options", []):
                opt_pk = f"{rec['id']}_{opt['id']}"
                is_corr = 1 if opt["id"] == ans_obj["value"] else 0
                conn.execute(
                    "INSERT INTO question_options (id, question_id, option_id, option_text, is_correct) VALUES (?, ?, ?, ?, ?)",
                    (opt_pk, rec["id"], opt["id"], opt["text"], is_corr),
                )
                option_count += 1

            s_meta = rec["source_metadata"]
            conn.execute(
                """
                INSERT INTO question_sources (
                    question_id, source_dataset, source_record_id,
                    dataset_url, license, source_split, source_revision
                ) VALUES (?, ?, ?, ?, ?, ?, ?)
                """,
                (
                    rec["id"],
                    rec["source_dataset"],
                    rec["source_record_id"],
                    s_meta["dataset_url"],
                    s_meta["license"],
                    s_meta["source_split"],
                    s_meta["source_revision"],
                ),
            )

        conn.execute(
            """
            INSERT INTO question_pack_versions (id, pack_id, version_code, record_count, sha256, released_at)
            VALUES (?, ?, 1, ?, 'verified-pack-v1', ?)
            """,
            (f"{pack_id}_V1", pack_id, question_count, now_ms),
        )
        conn.commit()
    finally:
        conn.close()

    stats = {
        "sqlite_file": str(DB_PATH.relative_to(BASE_DIR)),
        "size_bytes": DB_PATH.stat().st_size,
        "subjects_count": len(subjects_seen),
        "topics_count": len(topics_seen),
        "questions_count": question_count,
        "options_count": option_count,
    }
    (STATS_DIR / "db_statistics.json").write_text(
        json.dumps(stats, indent=2) + "\n", encoding="utf-8"
    )
    return stats


if __name__ == "__main__":
    stats = compile_sqlite()
    print(f"SQLite compilation complete: {stats}")
