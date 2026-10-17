#!/usr/bin/env python3
from __future__ import annotations

import os
import random
import string
import subprocess
import tempfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
CONFIG = ROOT / ".gitleaks.toml"
GITLEAKS = os.environ.get("GITLEAKS_BIN", "gitleaks")
rng = random.Random(20261017)

def chars(alphabet: str, length: int) -> str:
    return "".join(rng.choice(alphabet) for _ in range(length))

ALNUM = string.ascii_letters + string.digits
WORD = ALNUM + "_"
AWS = string.ascii_uppercase + "234567"

def synthetic_cases() -> dict[str, str]:
    return {
        "github-fine-grained-pat": "github_" + "pat_" + chars(WORD, 82),
        "aws-sts-access-key": "AS" + "IA" + chars(AWS, 16),
        "openai-api-key": "sk-" + chars(ALNUM, 20) + "T3" + "BlbkFJ" + chars(ALNUM, 20),
        "anthropic-api-key": "sk-ant-" + "api03-" + chars(ALNUM + "_-", 93) + "AA",
        "stripe-live-secret": "sk_" + "live_" + chars(ALNUM, 32),
        "slack-bot-token": "xox" + "b-" + chars(string.digits, 12) + "-" + chars(string.digits, 12) + "-" + chars(ALNUM, 24),
        "jwt": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9."
               "eyJzdWIiOiJzZWN1cml0eS1yZWdyZXNzaW9uIiwicm9sZSI6InNlcnZpY2UifQ."
               + chars(ALNUM + "_-", 43),
        "oauth-client-secret": 'client_' + 'secret = "' + chars(ALNUM + "_-", 40) + '"',
        "database-credential-uri": "postgres" + "ql://appuser:" + chars(ALNUM, 36) + "@db.example.invalid/app",
        "service-role-secret": 'service_' + 'role_key = "' + chars(ALNUM + "_-", 48) + '"',
    }

def run(cmd: list[str], cwd: Path, check: bool = True) -> subprocess.CompletedProcess[str]:
    return subprocess.run(cmd, cwd=cwd, check=check, text=True, stdout=subprocess.DEVNULL, stderr=subprocess.PIPE)

def init_repo(path: Path) -> None:
    run(["git", "init", "-q"], path)
    run(["git", "config", "user.name", "Security Regression"], path)
    run(["git", "config", "user.email", "security-regression@example.invalid"], path)

def commit_all(path: Path, message: str) -> None:
    run(["git", "add", "-A"], path)
    run(["git", "commit", "-q", "-m", message], path)

def scan_expect_leak(path: Path, case_name: str) -> None:
    result = run(
        [GITLEAKS, "git", "--config", str(CONFIG), "--no-banner", "--redact=100", str(path)],
        ROOT,
        check=False,
    )
    if result.returncode == 0:
        raise AssertionError(f"Gitleaks NÃO detectou o caso sintético: {case_name}")
    if result.returncode != 1:
        raise RuntimeError(f"Gitleaks falhou inesperadamente em {case_name}: exit={result.returncode}; {result.stderr[-400:]}")

def main() -> int:
    for name, value in synthetic_cases().items():
        with tempfile.TemporaryDirectory(prefix="gitleaks-case-") as tmp:
            test_repo = Path(tmp)
            init_repo(test_repo)
            (test_repo / "fixture.txt").write_text(value + "\n", encoding="utf-8")
            commit_all(test_repo, "add synthetic credential")
            scan_expect_leak(test_repo, name)
            print(f"PASS family: {name}")

    with tempfile.TemporaryDirectory(prefix="gitleaks-history-") as tmp:
        test_repo = Path(tmp)
        init_repo(test_repo)
        target = test_repo / "historical.txt"
        target.write_text("github_" + "pat_" + chars(WORD, 82) + "\n", encoding="utf-8")
        commit_all(test_repo, "add historical synthetic credential")
        target.unlink()
        commit_all(test_repo, "remove credential from head")
        if target.exists():
            raise AssertionError("fixture histórica ainda existe no HEAD")
        scan_expect_leak(test_repo, "history-only-secret")
        print("PASS history-only-secret")

    print("SECRET_SCANNER_REGRESSION=PASS")
    return 0

if __name__ == "__main__":
    raise SystemExit(main())
