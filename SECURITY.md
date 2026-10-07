# Security policy

## Supported code

Security maintenance currently covers the default branch of this public API
repository and its supported-contract source snapshot based on API 1.0.0.
No fixed response-time guarantee is offered.

## Report privately

Use GitHub's
[Report a vulnerability](https://github.com/therealaqz/DeluxeGradient-API/security/advisories/new)
form. **Do not put exploit details, credentials, player data, client data, or
private server configurations in a public issue.** If the private reporting
form is unavailable, open an issue requesting a private reporting channel
without including vulnerability details.

Include the affected type/method and version, expected and observed behaviour,
security impact, and a minimal reproduction using synthetic data. Reports may
cover malformed input, unsafe client actions, API misuse that crosses a trust
boundary, mutable data exposure, or build/dependency risks in this repository.

The security maintainer is [Aqz / @therealaqz](MAINTAINERS.md). Reports are triaged,
reproduced where possible, and addressed with a fix and regression coverage.
Disclosure timing is coordinated with the reporter and any affected upstream
maintainer. Confirmed public advisories document affected versions and mitigation.

## Testing scope

Use local builds, synthetic inputs, and test servers you own or are explicitly
authorized to assess. This policy grants no authorization to test public servers,
customer systems, Paper infrastructure, Maven Central, or other services.

The API is a trusted-plugin integration boundary. Parsing and colouring methods
accept administrator-authored formats; callers must enforce authorization and
input limits before processing player text. Client actions must be validated
after placeholder expansion. Provider registration is owned by the installed
plugin's lifecycle; integration plugins should retrieve the service, not replace it.

Proprietary plugin source, credentials, signing material, and server data are
outside the public source scope. A report that also affects the installed plugin
can be routed privately to its maintainer without publishing its implementation.
