#!/usr/bin/env python3
"""
Master runner for the MATHGATE Dataset Acquisition, Normalization, Validation & SQLite Pipeline.
"""

from download_huggingface import download_enabled_sources
from normalize_and_classify import run_normalization
from validate_dataset import run_validation
from build_sqlite import compile_sqlite


def main() -> None:
    dl = download_enabled_sources()
    norm = run_normalization()
    val = run_validation()
    db_stats = compile_sqlite()
    assert val["total_validated_records"] > 0, "Expected validated records > 0"
    assert db_stats["questions_count"] == val["total_validated_records"], "SQLite count mismatch"
    print(
        f"Pipeline OK: sources={len(dl['sources'])}, "
        f"normalized={len(norm)}, validated={val['total_validated_records']}, "
        f"sqlite_bytes={db_stats['size_bytes']}"
    )


if __name__ == "__main__":
    main()
