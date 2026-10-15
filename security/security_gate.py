#!/usr/bin/env python3
from __future__ import annotations
import re,subprocess
from pathlib import Path
REQUIRED=[Path("SECURITY_BASELINE.md"),Path("SECURITY_CHECKLIST.md"),Path("SECURITY_PROFILE.yml")]
PATTERNS=[("private-key",re.compile(r"-----BEGIN (?:RSA |EC |OPENSSH )?PRIVATE KEY-----")),("github-token",re.compile(r"\bgh[pousr]_[A-Za-z0-9_]{20,}\b")),("aws-access-key",re.compile(r"\bAKIA[0-9A-Z]{16}\b")),("google-api-key",re.compile(r"\bAIza[0-9A-Za-z_-]{30,}\b"))]
TEXT={".md",".txt",".yml",".yaml",".json",".toml",".ini",".cfg",".conf",".properties",".gradle",".kts",".kt",".java",".cs",".py",".js",".ts",".tsx",".jsx",".html",".xml",".sh",".ps1",".bat",".cmd"}
EXCLUDED={".git","build","dist","bin","obj",".gradle",".idea",".venv","node_modules","vendor"}
def tracked_files():
    try: raw=subprocess.check_output(["git","ls-files","-z"]); return [Path(x.decode()) for x in raw.split(b"\0") if x]
    except Exception: return [p for p in Path(".").rglob("*") if p.is_file()]
errors=[]
for required in REQUIRED:
    if not required.exists(): errors.append(f"arquivo obrigatório ausente: {required}")
for path in tracked_files():
    if any(part in EXCLUDED for part in path.parts): continue
    low=path.name.lower()
    if low==".env" or (low.startswith(".env.") and "example" not in low and "sample" not in low): errors.append(f"arquivo de ambiente sensível versionado: {path}")
    if path.suffix.lower() in {".pem",".p12",".pfx",".jks",".keystore",".key"} and not any(w in low for w in ("debug","test","sample","example")): errors.append(f"material de chave/keystore requer revisão: {path}")
    if path.suffix.lower() not in TEXT and low!=".env": continue
    if "example" in low or "sample" in low: continue
    try: data=path.read_text("utf-8",errors="ignore")
    except Exception: continue
    for label,pattern in PATTERNS:
        if pattern.search(data): errors.append(f"possível segredo ({label}) em {path}"); break
if errors:
    print("SECURITY_GATE=BLOCKED")
    for err in sorted(set(errors)): print("- "+err)
    raise SystemExit(1)
print("SECURITY_GATE=PASS")
print("Gate genérico aprovado; checks específicos da stack continuam obrigatórios.")
