#!/usr/bin/env python3
"""
MATHGATE — Data Quality, Answer Validation & Audit Report Generator (Part 6).
Generates all 9 required reports in mathgate-data/reports/:
1. dataset_summary.json
2. dataset_summary.csv
3. licence_audit.json
4. validation_errors.json
5. duplicate_report.json
6. topic_distribution.json
7. difficulty_distribution.json
8. unmapped_questions.json
9. import_manifest.json
"""

import csv
import difflib
import hashlib
import json
import re
from collections import Counter, defaultdict
from datetime import datetime, timezone
from pathlib import Path
from typing import Any, Dict, List, Tuple

BASE_DIR = Path(__file__).resolve().parent.parent
CONFIG_PATH = BASE_DIR / "manifests" / "datasets_config.json"
UNIFIED_JSONL = BASE_DIR / "output" / "jsonl" / "mathgate_unified.jsonl"
VALIDATED_JSONL = BASE_DIR / "output" / "jsonl" / "mathgate_validated.jsonl"
REJECTED_DIR = BASE_DIR / "rejected"
REPORTS_DIR = BASE_DIR / "reports"

REQUIRED_FIELDS = {
    "id",
    "source_dataset",
    "source_record_id",
    "subject",
    "topic",
    "subtopic",
    "education_level",
    "difficulty",
    "question_type",
    "language",
    "question",
    "options",
    "correct_answer",
    "source_metadata",
    "quality",
    "content_version",
    "fingerprint",
}


def validate_record(rec: Dict[str, Any]) -> List[str]:
    errors: List[str] = []
    missing = REQUIRED_FIELDS - set(rec.keys())
    if missing:
        errors.append(f"Missing required fields: {sorted(missing)}")

    q_text = str(rec.get("question", "")).strip()
    if not q_text:
        errors.append("Question text is empty")

    q_type = rec.get("question_type")
    if q_type not in ("MCQ_SINGLE", "NUMERICAL"):
        errors.append(f"Invalid question_type: {q_type}")

    ans_obj = rec.get("correct_answer", {})
    ans_val = str(ans_obj.get("value", "")).strip()
    if not ans_val:
        errors.append("Missing correct_answer.value")

    if q_type == "MCQ_SINGLE":
        opts = rec.get("options", [])
        if not isinstance(opts, list) or len(opts) < 2:
            errors.append("MCQ_SINGLE requires at least 2 options")
        else:
            opt_ids = [str(o.get("id", "")).strip() for o in opts]
            opt_texts = [str(o.get("text", "")).strip().lower() for o in opts]
            if ans_val not in opt_ids:
                errors.append(f"correct_answer '{ans_val}' not found in options {opt_ids}")
            if len(set(opt_texts)) != len(opt_texts):
                errors.append("Duplicate option text detected in MCQ options")

    if q_type == "NUMERICAL":
        if not re.match(r"^[-+]?\d+(?:\.\d+)?(?:/[1-9]\d*)?$", ans_val):
            errors.append(f"Invalid numerical answer format: '{ans_val}'")

    # Physics sanity check: resistance/mass/kelvin must not be negative
    if rec.get("subject") == "PHYSICS" and q_type == "NUMERICAL":
        try:
            if float(ans_val) < 0 and any(w in q_text.lower() for w in ["mass", "resistance", "speed", "kelvin"]):
                errors.append("Physically inconsistent negative scalar quantity")
        except ValueError:
            pass

    return errors


def run_validation() -> Dict[str, Any]:
    REJECTED_DIR.mkdir(parents=True, exist_ok=True)
    REPORTS_DIR.mkdir(parents=True, exist_ok=True)

    config = json.loads(CONFIG_PATH.read_text(encoding="utf-8"))
    allowed_licenses = set(config["policy"]["allowed_licenses"])

    raw_lines = UNIFIED_JSONL.read_text(encoding="utf-8").splitlines() if UNIFIED_JSONL.exists() else []
    seen_ids: set[str] = set()
    seen_source_ids: Dict[str, str] = {}
    exact_hashes: Dict[str, str] = {}
    norm_texts: List[Tuple[str, str]] = []

    validated_records: List[Dict[str, Any]] = []
    rejected_records: List[Dict[str, Any]] = []
    validation_errors: List[Dict[str, Any]] = []
    duplicate_relations: List[Dict[str, Any]] = []
    unmapped_questions: List[Dict[str, Any]] = []

    per_dataset_counts: Dict[str, Dict[str, int]] = defaultdict(
        lambda: {"total": 0, "valid": 0, "rejected": 0, "unmapped": 0}
    )
    topic_dist: Counter[str] = Counter()
    difficulty_dist: Counter[str] = Counter()

    for idx, line in enumerate(raw_lines, start=1):
        if not line.strip():
            continue
        rec = json.loads(line)
        ds = rec.get("source_dataset", "unknown")
        per_dataset_counts[ds]["total"] += 1

        rec_errors = validate_record(rec)
        rec_id = str(rec.get("id", f"LINE_{idx}"))
        if rec_id in seen_ids:
            rec_errors.append(f"Duplicate primary ID: {rec_id}")
        seen_ids.add(rec_id)

        # License check
        lic = rec.get("source_metadata", {}).get("license", "")
        if lic not in allowed_licenses:
            rec_errors.append(f"Disallowed license: {lic}")

        # Duplicate detection (exact + normalized + fuzzy)
        q_text = str(rec.get("question", "")).strip()
        exact_h = hashlib.sha256(q_text.encode("utf-8")).hexdigest()
        norm_q = re.sub(r"[^a-z0-9]+", " ", q_text.lower()).strip()

        src_key = f"{ds}:{rec.get('source_record_id')}"
        if src_key in seen_source_ids:
            duplicate_relations.append(
                {
                    "type": "DUPLICATE_SOURCE_ID",
                    "record_id": rec_id,
                    "matches": seen_source_ids[src_key],
                }
            )
        else:
            seen_source_ids[src_key] = rec_id

        if exact_h in exact_hashes:
            duplicate_relations.append(
                {
                    "type": "EXACT_TEXT_HASH",
                    "record_id": rec_id,
                    "matches": exact_hashes[exact_h],
                }
            )
        else:
            exact_hashes[exact_h] = rec_id

        for prev_id, prev_norm in norm_texts:
            ratio = difflib.SequenceMatcher(None, norm_q, prev_norm).ratio()
            if ratio >= 0.92 and prev_id != rec_id:
                duplicate_relations.append(
                    {
                        "type": "FUZZY_SIMILARITY",
                        "record_id": rec_id,
                        "matches": prev_id,
                        "similarity": round(ratio, 4),
                    }
                )
        norm_texts.append((rec_id, norm_q))

        if rec.get("subject") == "UNMAPPED" or rec.get("topic") == "UNMAPPED":
            per_dataset_counts[ds]["unmapped"] += 1
            unmapped_questions.append(
                {
                    "id": rec_id,
                    "source_dataset": ds,
                    "question": q_text,
                    "reason": "Conservative classifier requires manual subject review",
                }
            )

        if rec_errors:
            per_dataset_counts[ds]["rejected"] += 1
            rec["quality"]["validation_status"] = "REJECTED"
            rejected_records.append(rec)
            validation_errors.append(
                {"id": rec_id, "source_dataset": ds, "errors": rec_errors}
            )
        else:
            per_dataset_counts[ds]["valid"] += 1
            rec["quality"]["validation_status"] = "VALIDATED"
            validated_records.append(rec)
            topic_dist[f"{rec['subject']}::{rec['topic']}"] += 1
            difficulty_dist[str(rec["difficulty"])] += 1

    # Write validated & rejected JSONL
    with VALIDATED_JSONL.open("w", encoding="utf-8") as fh:
        for r in validated_records:
            fh.write(json.dumps(r, ensure_ascii=False) + "\n")

    with (REJECTED_DIR / "rejected_questions.jsonl").open("w", encoding="utf-8") as fh:
        for r in rejected_records:
            fh.write(json.dumps(r, ensure_ascii=False) + "\n")

    # 1. dataset_summary.json
    summary_payload = {
        "generated_at": datetime.now(timezone.utc).isoformat(),
        "total_records_inspected": len(raw_lines),
        "total_validated_records": len(validated_records),
        "total_rejected_records": len(rejected_records),
        "total_unmapped_records": len(unmapped_questions),
        "datasets": dict(per_dataset_counts),
    }
    (REPORTS_DIR / "dataset_summary.json").write_text(
        json.dumps(summary_payload, indent=2) + "\n", encoding="utf-8"
    )

    # 2. dataset_summary.csv
    with (REPORTS_DIR / "dataset_summary.csv").open("w", newline="", encoding="utf-8") as csv_fh:
        writer = csv.writer(csv_fh)
        writer.writerow(["source_dataset", "total", "valid", "rejected", "unmapped"])
        for ds_name, stats in sorted(per_dataset_counts.items()):
            writer.writerow(
                [ds_name, stats["total"], stats["valid"], stats["rejected"], stats["unmapped"]]
            )

    # 3. licence_audit.json
    licence_audit = {
        "policy_mode": config["policy"]["mode"],
        "allowed_licenses": config["policy"]["allowed_licenses"],
        "audited_sources": {
            k: {
                "enabled": v.get("enabled", False),
                "license": v.get("license"),
                "commercial_safe": v.get("commercial_safe", False),
                "repository": v.get("repository"),
                "reason": v.get("reason", "Permissive commercial-safe license verified"),
            }
            for k, v in config["datasets"].items()
        },
    }
    (REPORTS_DIR / "licence_audit.json").write_text(
        json.dumps(licence_audit, indent=2) + "\n", encoding="utf-8"
    )

    # 4. validation_errors.json
    (REPORTS_DIR / "validation_errors.json").write_text(
        json.dumps({"error_count": len(validation_errors), "errors": validation_errors}, indent=2)
        + "\n",
        encoding="utf-8",
    )

    # 5. duplicate_report.json
    (REPORTS_DIR / "duplicate_report.json").write_text(
        json.dumps(
            {"duplicate_count": len(duplicate_relations), "duplicates": duplicate_relations},
            indent=2,
        )
        + "\n",
        encoding="utf-8",
    )

    # 6. topic_distribution.json
    (REPORTS_DIR / "topic_distribution.json").write_text(
        json.dumps(dict(topic_dist), indent=2) + "\n", encoding="utf-8"
    )

    # 7. difficulty_distribution.json
    (REPORTS_DIR / "difficulty_distribution.json").write_text(
        json.dumps(dict(difficulty_dist), indent=2) + "\n", encoding="utf-8"
    )

    # 8. unmapped_questions.json
    (REPORTS_DIR / "unmapped_questions.json").write_text(
        json.dumps(
            {"unmapped_count": len(unmapped_questions), "questions": unmapped_questions},
            indent=2,
        )
        + "\n",
        encoding="utf-8",
    )

    # 9. import_manifest.json
    import_manifest = {
        "generated_at": datetime.now(timezone.utc).isoformat(),
        "validated_jsonl": "output/jsonl/mathgate_validated.jsonl",
        "sqlite_target": "output/sqlite/mathgate_question_bank.db",
        "validated_record_count": len(validated_records),
        "content_version": 1,
    }
    (REPORTS_DIR / "import_manifest.json").write_text(
        json.dumps(import_manifest, indent=2) + "\n", encoding="utf-8"
    )

    return summary_payload


if __name__ == "__main__":
    summary = run_validation()
    print(
        f"Validation complete: {summary['total_validated_records']} valid, "
        f"{summary['total_rejected_records']} rejected."
    )
