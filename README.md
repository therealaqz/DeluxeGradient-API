# DeluxeGradient API

Open-source integration contracts and data types for developers integrating with
DeluxeGradient on Bukkit/Paper servers. Maintained by **Aqz
([therealaqz](https://github.com/therealaqz))**.

This repository contains the supported API surface extracted from the already
published `io.github.therealaqz:deluxegradient-api:1.0.0` sources. It includes the
service interface and provider, colour and gradient model, immutable text-tree
types, decorations, slots, and the server-independent gradient-change payload.
The formatting parser, lexer, renderers, server implementation, and plugin
distribution are outside this repository.

## Use the published API JAR

The existing integration artifact is on
[Maven Central](https://central.sonatype.com/artifact/io.github.therealaqz/deluxegradient-api/1.0.0).

Gradle Kotlin DSL:

```kotlin
repositories { mavenCentral() }
dependencies {
    compileOnly("io.github.therealaqz:deluxegradient-api:1.0.0")
}
```

Maven:

```xml
<dependency>
  <groupId>io.github.therealaqz</groupId>
  <artifactId>deluxegradient-api</artifactId>
  <version>1.0.0</version>
  <scope>provided</scope>
</dependency>
```

Use `compileOnly` / `provided`: the installed DeluxeGradient plugin supplies the
API at runtime. Do not shade or relocate the API classes into your integration.
Add `softdepend: [DeluxeGradient]` to your `plugin.yml` for an optional integration,
or `depend: [DeluxeGradient]` when it is required.

```java
DeluxeGradientApi api = DeluxeGradientProvider.getOrNull();
if (api != null) {
    // This format is owned by the server administrator, not supplied by a player.
    Node title = api.parse("<gradient:#ff5555:#5555ff>Welcome</gradient>");
    String rendered = api.render(title, true);
}
```

Imports: `me.therealaqz.deluxegradient.api.DeluxeGradientApi`,
`me.therealaqz.deluxegradient.api.DeluxeGradientProvider`, and
`me.therealaqz.deluxegradient.api.text.tree.Node`.
See [integration guidance](docs/integration.md) for threading, input trust,
provider lifecycle, and client-action validation.

## Build this source project

Requires JDK 21 and Maven 3.9+. Main classes target Java 8 bytecode. The only
compile-time dependency is JetBrains annotations; JUnit is used for tests.

```sh
mvn --batch-mode verify
python tools/verify_export.py
```

This creates `target/deluxegradient-api-contracts-1.0.0-SNAPSHOT.jar`, a sources
JAR, and a Javadoc JAR. This is a supported-contract subset, not a byte-for-byte
replacement for the full existing Central artifact. Its distinct local artifact
name prevents accidental replacement. This repository does not publish to Central.

The [provenance manifest](docs/provenance.json) records the upstream source JAR
checksum and exported files. The verification script checks every production
source against its upstream Java tokens and checks the built JAR's contents.

## Maintenance and security

- [Maintainer and security responsibilities](MAINTAINERS.md)
- [Private vulnerability reporting](SECURITY.md)
- [Contribution process](CONTRIBUTING.md)
- [Project status and public evidence](docs/project-status.md)

## License

The source, tests, and documentation in **this repository** are released under
the [MIT license](LICENSE), effective October 7, 2026. This grant covers the files
published here. DeluxeGradient's separately distributed plugin and omitted
implementation remain under their own terms.

The existing immutable Central `1.0.0` release retains its
[published integration terms](https://aqz.gitbook.io/aqz-docs/server-administration/api/terms).
Its POM was published before this repository and does not advertise this MIT
release. Do not assume that this license covers additional classes or files in
that artifact which are absent here.
