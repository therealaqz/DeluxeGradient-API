# Project status and public evidence

Status as of October 7, 2026:

| Fact | Evidence |
| --- | --- |
| Project | DeluxeGradient API: Java contracts and model types for plugin integrations |
| Publisher / maintainer | Aqz, GitHub [@therealaqz](https://github.com/therealaqz) |
| Public artifact | [`io.github.therealaqz:deluxegradient-api:1.0.0`](https://central.sonatype.com/artifact/io.github.therealaqz/deluxegradient-api/1.0.0) |
| Open-source scope | Files published in this repository, under [MIT](../LICENSE) |
| Public source release | October 7, 2026 |
| Security-maintainer responsibilities | Documented in [MAINTAINERS.md](../MAINTAINERS.md), beginning October 7, 2026 |
| Vulnerability reporting | [SECURITY.md](../SECURITY.md) |
| Source provenance | [Published artifact and checksums](provenance.json) |
| Third-party dependency evidence | No independently verified downstream integrations are documented here yet |
| Public security track record | No CVE, accepted disclosure, or published security advisory is claimed by this initial repository release |

The published artifact is available for developers to integrate against.
Availability on Maven Central is distinct from evidence that independent
projects actually depend on it. As verified downstream integrations or security
advisories become public, add links with the maintainers' or reporters' consent.

## Intended defensive work

Security review and vulnerability research on the API contracts and model types
maintained here, and authorized integrations with Bukkit/Paper. Work includes
input handling, client-action validation, service lifecycle, dependency hygiene,
and API compatibility. Validated third-party findings should be disclosed to the
affected maintainers through their designated channels.

Program applications should distinguish these intended responsibilities from
completed, publicly evidenced work. This repository does not establish
incorporation, country of residence, third-party adoption, or approval by a
verification program; those require separate verification.
