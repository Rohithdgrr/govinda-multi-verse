#!/usr/bin/env python3
"""
MATHGATE — Unified JSONL Normalization & Deterministic Subject/Topic Classification.
- Converts enabled raw sources into the Unified MATHGATE JSONL Schema (Part 4)
- Classifies records conservatively using the Part 5 Subject & Topic Hierarchy
- Routes ambiguous records to 'UNMAPPED' rather than guessing Physics or inventing class levels
"""

import hashlib
import json
import re
from pathlib import Path
from typing import Any, Dict, List, Optional, Tuple

BASE_DIR = Path(__file__).resolve().parent.parent
CONFIG_PATH = BASE_DIR / "manifests" / "datasets_config.json"
SOURCES_DIR = BASE_DIR / "sources"
NORMALIZED_DIR = BASE_DIR / "normalized"
OUTPUT_JSONL_DIR = BASE_DIR / "output" / "jsonl"

SUBJECT_FOLDERS = {
    "MATHEMATICS": "mathematics",
    "PHYSICS": "physics",
    "CHEMISTRY": "chemistry",
    "BIOLOGY": "biology",
    "SCHOOL_SCIENCE": "school_science",
    "GENERAL_KNOWLEDGE": "general_knowledge",
    "COMPETITIVE_EXAMS": "competitive_exams",
}


def compute_fingerprint(source_dataset: str, question_text: str, answer_val: str) -> str:
    norm_q = re.sub(r"\s+", " ", question_text.strip().lower())
    raw = f"{source_dataset}|{norm_q}|{answer_val.strip().lower()}"
    return hashlib.sha256(raw.encode("utf-8")).hexdigest()[:24]


def classify_science_question(text: str) -> Tuple[str, str, str]:
    """
    Conservative science classification across BIOLOGY, CHEMISTRY, PHYSICS, or UNMAPPED.
    Never defaults every science question to PHYSICS.
    """
    lower = text.lower()
    bio_kw = ["cell", "chloroplast", "photosynthesis", "mitochondri", "atp", "organelle", "eye", "pupil", "iris", "plant", "dna", "gene"]
    chem_kw = ["acid", "zinc", "hydrogen gas", "element", "atomic", "electron", "subatomic", "reaction", "ph ", "hcl", "tungsten"]
    phys_kw = ["mass", "gravity", "force", "ohm", "resistance", "voltage", "current", "velocity", "acceleration", "si unit"]

    bio_hits = sum(1 for k in bio_kw if k in lower)
    chem_hits = sum(1 for k in chem_kw if k in lower)
    phys_hits = sum(1 for k in phys_kw if k in lower)

    if bio_hits > chem_hits and bio_hits > phys_hits:
        return ("BIOLOGY", "CELL_AND_HUMAN_BIOLOGY", "PHYSIOLOGY")
    if chem_hits > bio_hits and chem_hits > phys_hits:
        return ("CHEMISTRY", "MATTER_AND_REACTIONS", "ATOMIC_AND_ACIDS")
    if phys_hits > bio_hits and phys_hits > chem_hits:
        return ("PHYSICS", "MECHANICS_AND_ELECTRICITY", "FUNDAMENTAL_LAWS")
    return ("UNMAPPED", "UNMAPPED", "UNMAPPED")


def classify_math_question(text: str, raw_topic: Optional[str] = None) -> Tuple[str, str, str]:
    lower = f"{text} {raw_topic or ''}".lower()
    if any(k in lower for k in ["derivative", "d/dx", "determinant", "matrix", "integral"]):
        return ("MATHEMATICS", "CALCULUS", "DERIVATIVES_AND_MATRICES")
    if any(k in lower for k in ["average", "median", "mean", "mode", "dataset"]):
        return ("MATHEMATICS", "STATISTICS", "CENTRAL_TENDENCY")
    if any(k in lower for k in ["perimeter", "triangle", "area", "rectangular", "geometry"]):
        return ("MATHEMATICS", "GEOMETRY", "MENSURATION")
    if any(k in lower for k in ["solve for x", "algebra", "equation"]):
        return ("MATHEMATICS", "ALGEBRA", "LINEAR_EQUATIONS")
    return ("MATHEMATICS", "ARITHMETIC", "WORD_PROBLEMS")


def normalize_qasc_row(row: Dict[str, Any], split: str, meta: Dict[str, Any]) -> Dict[str, Any]:
    q_obj = row.get("question", {})
    stem = q_obj.get("stem", "").strip()
    raw_choices = q_obj.get("choices", [])
    answer_key = str(row.get("answerKey", "")).strip()

    # Select up to 4 options (preserving the correct answer + first 3 distractors) for clean 4-option practice
    correct_choice = next((c for c in raw_choices if c.get("label") == answer_key), None)
    distractors = [c for c in raw_choices if c.get("label") != answer_key][:3]
    trimmed = sorted(
        ([correct_choice] if correct_choice else []) + distractors,
        key=lambda c: c.get("label", "Z"),
    )
    relabeled_options: List[Dict[str, str]] = []
    new_correct_label = answer_key
    for idx, ch in enumerate(trimmed):
        new_id = chr(ord("A") + idx)
        relabeled_options.append({"id": new_id, "text": str(ch.get("text", "")).strip()})
        if ch.get("label") == answer_key:
            new_correct_label = new_id

    subject, topic, subtopic = classify_science_question(stem)
    facts = [f for f in [row.get("fact1"), row.get("fact2"), row.get("combinedfact")] if f]

    return {
        "id": f"QASC_{row.get('id', 'UNKNOWN')}",
        "source_dataset": meta["repository"],
        "source_record_id": str(row.get("id", "")),
        "subject": subject,
        "topic": topic,
        "subtopic": subtopic,
        "education_level": "SCHOOL_FOUNDATION",
        "class_level": "CLASS_5_TO_8",
        "exam_tags": [],
        "difficulty": 2,
        "question_type": "MCQ_SINGLE",
        "language": "en",
        "question": stem,
        "options": relabeled_options,
        "correct_answer": {
            "type": "OPTION",
            "value": new_correct_label,
        },
        "explanation": row.get("combinedfact"),
        "solution_steps": facts,
        "supporting_facts": facts,
        "source_metadata": {
            "dataset_url": meta["dataset_url"],
            "license": meta["license"],
            "source_split": split,
            "source_revision": meta["pinned_revision"],
        },
        "quality": {
            "verified": False,
            "validation_status": "PENDING",
            "review_status": "UNREVIEWED",
        },
        "content_version": 1,
        "fingerprint": compute_fingerprint(meta["repository"], stem, new_correct_label),
    }


def normalize_gsm8k_row(row: Dict[str, Any], split: str, meta: Dict[str, Any]) -> Dict[str, Any]:
    question_text = str(row.get("question", "")).strip()
    raw_answer = str(row.get("answer", "")).strip()
    match = re.search(r"####\s*([-+]?\d+(?:\.\d+)?)", raw_answer)
    final_val = match.group(1) if match else ""
    steps = [
        re.sub(r"<<[^>]+>>", "", line).strip()
        for line in raw_answer.splitlines()
        if line.strip() and not line.strip().startswith("####")
    ]
    subject, topic, subtopic = classify_math_question(question_text)

    return {
        "id": f"GSM8K_{row.get('id', 'UNKNOWN')}",
        "source_dataset": meta["repository"],
        "source_record_id": str(row.get("id", "")),
        "subject": subject,
        "topic": topic,
        "subtopic": subtopic,
        "education_level": "SCHOOL",
        "class_level": "CLASS_5_TO_8",
        "exam_tags": [],
        "difficulty": 2,
        "question_type": "NUMERICAL",
        "language": "en",
        "question": question_text,
        "options": [],
        "correct_answer": {
            "type": "NUMERICAL",
            "value": final_val,
        },
        "explanation": "\n".join(steps) if steps else None,
        "solution_steps": steps,
        "supporting_facts": [],
        "source_metadata": {
            "dataset_url": meta["dataset_url"],
            "license": meta["license"],
            "source_split": split,
            "source_revision": meta["pinned_revision"],
        },
        "quality": {
            "verified": False,
            "validation_status": "PENDING",
            "review_status": "UNREVIEWED",
        },
        "content_version": 1,
        "fingerprint": compute_fingerprint(meta["repository"], question_text, final_val),
    }


def normalize_mathqa_row(row: Dict[str, Any], split: str, meta: Dict[str, Any]) -> Dict[str, Any]:
    problem = str(row.get("Problem", "")).strip()
    rationale = str(row.get("Rationale", "")).strip()
    raw_opts = str(row.get("options", ""))
    correct_letter = str(row.get("correct", "")).strip().upper()
    options: List[Dict[str, str]] = []
    for part in raw_opts.split(","):
        m = re.match(r"\s*([a-eA-E])\s*\)\s*(.+)$", part.strip())
        if m:
            options.append({"id": m.group(1).upper(), "text": m.group(2).strip()})

    subject, topic, subtopic = classify_math_question(problem, row.get("category"))
    return {
        "id": f"MATHQA_{row.get('id', 'UNKNOWN')}",
        "source_dataset": meta["repository"],
        "source_record_id": str(row.get("id", "")),
        "subject": subject,
        "topic": "QUANTITATIVE_APTITUDE" if row.get("category") == "gain" else topic,
        "subtopic": subtopic,
        "education_level": "COMPETITIVE_EXAMS",
        "class_level": None,
        "exam_tags": ["SSC", "BANKING", "RRB"],
        "difficulty": 3,
        "question_type": "MCQ_SINGLE",
        "language": "en",
        "question": problem,
        "options": options,
        "correct_answer": {
            "type": "OPTION",
            "value": correct_letter,
            "numeric_value": row.get("numeric_answer"),
        },
        "explanation": rationale or None,
        "solution_steps": [rationale] if rationale else [],
        "supporting_facts": [],
        "source_metadata": {
            "dataset_url": meta["dataset_url"],
            "license": meta["license"],
            "source_split": split,
            "source_revision": meta["pinned_revision"],
        },
        "quality": {
            "verified": False,
            "validation_status": "PENDING",
            "review_status": "UNREVIEWED",
        },
        "content_version": 1,
        "fingerprint": compute_fingerprint(meta["repository"], problem, correct_letter),
    }


def normalize_synthetic_math_row(row: Dict[str, Any], split: str, meta: Dict[str, Any]) -> Dict[str, Any]:
    q_text = str(row.get("question", "")).strip()
    choices = row.get("choices", [])
    correct_idx = int(row.get("correct_index", 0))
    options = [
        {"id": chr(ord("A") + idx), "text": str(choice).strip()}
        for idx, choice in enumerate(choices)
    ]
    correct_letter = chr(ord("A") + correct_idx)
    subject, topic, subtopic = classify_math_question(q_text, row.get("topic"))
    return {
        "id": f"SYNTH_{row.get('id', 'UNKNOWN')}",
        "source_dataset": meta["repository"],
        "source_record_id": str(row.get("id", "")),
        "subject": subject,
        "topic": topic,
        "subtopic": subtopic,
        "education_level": "SCHOOL",
        "class_level": "CLASS_11_TO_12" if topic == "CALCULUS" else "CLASS_9_TO_10",
        "exam_tags": [],
        "difficulty": int(row.get("difficulty", 2)),
        "question_type": "MCQ_SINGLE",
        "language": "en",
        "question": q_text,
        "options": options,
        "correct_answer": {
            "type": "OPTION",
            "value": correct_letter,
        },
        "explanation": row.get("explanation"),
        "solution_steps": [row["explanation"]] if row.get("explanation") else [],
        "supporting_facts": [],
        "source_metadata": {
            "dataset_url": meta["dataset_url"],
            "license": meta["license"],
            "source_split": split,
            "source_revision": meta["pinned_revision"],
        },
        "quality": {
            "verified": False,
            "validation_status": "PENDING",
            "review_status": "UNREVIEWED",
        },
        "content_version": 1,
        "fingerprint": compute_fingerprint(meta["repository"], q_text, correct_letter),
    }


def normalize_learnclash_row(row: Dict[str, Any], split: str, meta: Dict[str, Any]) -> Dict[str, Any]:
    q_text = str(row.get("question", "")).strip()
    raw_opts = row.get("options", [])
    ans_text = str(row.get("answer", "")).strip()
    options: List[Dict[str, str]] = []
    correct_id = "A"
    for idx, opt in enumerate(raw_opts):
        oid = chr(ord("A") + idx)
        otext = str(opt).strip()
        options.append({"id": oid, "text": otext})
        if otext.lower() == ans_text.lower():
            correct_id = oid

    cat = str(row.get("category", "General Knowledge")).strip().upper()
    if cat == "SCIENCE":
        subject, topic, subtopic = classify_science_question(q_text)
        if subject == "UNMAPPED":
            subject, topic, subtopic = ("GENERAL_KNOWLEDGE", "SCIENCE_AND_SPACE", "ASTRONOMY_AND_EARTH")
    else:
        subject = "GENERAL_KNOWLEDGE"
        topic = cat.replace(" ", "_")
        subtopic = "FACTUAL_TRIVIA"

    return {
        "id": f"LCT_{row.get('id', 'UNKNOWN')}",
        "source_dataset": meta["repository"],
        "source_record_id": str(row.get("id", "")),
        "subject": subject,
        "topic": topic,
        "subtopic": subtopic,
        "education_level": "GENERAL",
        "class_level": None,
        "exam_tags": ["UPSC", "SSC"],
        "difficulty": 2,
        "question_type": "MCQ_SINGLE",
        "language": "en",
        "question": q_text,
        "options": options,
        "correct_answer": {
            "type": "OPTION",
            "value": correct_id,
        },
        "explanation": row.get("explanation"),
        "solution_steps": [row["explanation"]] if row.get("explanation") else [],
        "supporting_facts": [],
        "source_metadata": {
            "dataset_url": meta["dataset_url"],
            "license": meta["license"],
            "source_split": split,
            "source_revision": meta["pinned_revision"],
        },
        "quality": {
            "verified": False,
            "validation_status": "PENDING",
            "review_status": "UNREVIEWED",
        },
        "content_version": 1,
        "fingerprint": compute_fingerprint(meta["repository"], q_text, correct_id),
    }


def run_normalization() -> List[Dict[str, Any]]:
    for folder in SUBJECT_FOLDERS.values():
        (NORMALIZED_DIR / folder).mkdir(parents=True, exist_ok=True)
    OUTPUT_JSONL_DIR.mkdir(parents=True, exist_ok=True)

    config = json.loads(CONFIG_PATH.read_text(encoding="utf-8"))
    all_normalized: List[Dict[str, Any]] = []
    by_folder: Dict[str, List[Dict[str, Any]]] = {k: [] for k in SUBJECT_FOLDERS.values()}

    for name, meta in config["datasets"].items():
        if not meta.get("enabled"):
            continue
        src_dir = SOURCES_DIR / name
        if not src_dir.exists():
            continue
        for jsonl_file in sorted(src_dir.glob("*.jsonl")):
            split = jsonl_file.stem
            for line in jsonl_file.read_text(encoding="utf-8").splitlines():
                if not line.strip():
                    continue
                raw = json.loads(line)
                if name == "qasc":
                    norm = normalize_qasc_row(raw, split, meta)
                elif name == "gsm8k":
                    norm = normalize_gsm8k_row(raw, split, meta)
                elif name == "mathqa":
                    norm = normalize_mathqa_row(raw, split, meta)
                elif name == "synthetic_math":
                    norm = normalize_synthetic_math_row(raw, split, meta)
                elif name == "learnclash_trivia":
                    norm = normalize_learnclash_row(raw, split, meta)
                else:
                    continue

                all_normalized.append(norm)
                subj = norm["subject"]
                target_folder = SUBJECT_FOLDERS.get(subj, "school_science")
                if norm["education_level"] == "COMPETITIVE_EXAMS":
                    by_folder["competitive_exams"].append(norm)
                if name == "qasc":
                    by_folder["school_science"].append(norm)
                if target_folder in by_folder:
                    by_folder[target_folder].append(norm)

    for folder, records in by_folder.items():
        out_path = NORMALIZED_DIR / folder / "questions.jsonl"
        with out_path.open("w", encoding="utf-8") as fh:
            for rec in records:
                fh.write(json.dumps(rec, ensure_ascii=False) + "\n")

    unified_path = OUTPUT_JSONL_DIR / "mathgate_unified.jsonl"
    with unified_path.open("w", encoding="utf-8") as fh:
        for rec in all_normalized:
            fh.write(json.dumps(rec, ensure_ascii=False) + "\n")

    return all_normalized


if __name__ == "__main__":
    records = run_normalization()
    print(f"Normalized {len(records)} records into unified MATHGATE JSONL schema.")
