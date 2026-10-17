#!/usr/bin/env python3
from __future__ import annotations
from pathlib import Path

REQUIRED = [
    Path("SECURITY_BASELINE.md"),
    Path("SECURITY_CHECKLIST.md"),
    Path("SECURITY_PROFILE.yml"),
    Path(".gitleaks.toml"),
]

errors = [f"arquivo obrigatório ausente: {path}" for path in REQUIRED if not path.exists()]

if errors:
    print("SECURITY_POLICY_GATE=BLOCKED")
    for error in errors:
        print("- " + error)
    raise SystemExit(1)

print("SECURITY_POLICY_GATE=PASS")
print("Este gate valida a presença da política. A detecção de segredos é responsabilidade do Gitleaks ou scanner equivalente, incluindo histórico Git.")
