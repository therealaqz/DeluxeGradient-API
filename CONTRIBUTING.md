# Contributing

Open an issue for an ordinary bug or proposal, then submit a focused pull request.
Use the private channel in [SECURITY.md](SECURITY.md) for vulnerabilities.

Use Conventional Commit subjects, such as `docs: clarify provider lifecycle` or
`fix: validate malformed input`. Mark incompatible changes explicitly and explain
their effect on downstream integrations. Keep pull requests small and include
behavioural tests for security-relevant changes.

Before submitting, run:

```sh
mvn --batch-mode verify
python tools/verify_export.py
```

Production sources are initially an unchanged executable-code export of a
published API. Changes to Java behaviour require maintainer review and an
explicit provenance-manifest update; do not silently rewrite hashes to bypass
verification. Documentation and tests can evolve independently.

Keep the public source boundary: supported API contracts and data types only.
Do not contribute plugin implementations, formatting engines, credentials,
personal information, production logs, server configuration, binary archives,
or private-repository history. Use synthetic examples.

Contributions are provided under this repository's MIT license.
