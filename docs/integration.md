# Integrating safely

## Dependency and lifecycle

Compile against the published API with Gradle `compileOnly` or Maven `provided`.
The installed plugin supplies the runtime implementation and class definitions.
Shading the API into another plugin can break service lookup through class-loader
identity differences.

Prefer Bukkit's service registry:

```java
DeluxeGradientApi api = Bukkit.getServicesManager().load(DeluxeGradientApi.class);
```

Alternatively use `DeluxeGradientProvider.getOrNull()` and handle an unavailable
provider. `get()` throws when the provider is absent. Configure plugin load order
and account for enable/disable transitions. The public `set` method exists for
the runtime owner; integration plugins must not replace the registered provider.

## Thread ownership

Methods that resolve a live player by UUID must run on that player's owning
thread. On Folia, use the appropriate entity scheduler. Pure model construction
and administrator-format parsing do not require player access.

## Input trust and output bounds

`parse` and `colorize` are permissive APIs for trusted administrator-authored
formats. They are not permission gates for raw player input. Enforce permissions,
input size limits, and the allowed formatting syntax before handling untrusted
text. Parsing once and reusing a tree also avoids repeated work.

`Nodes.text` constructs literal content rather than interpreting formatting
syntax. A complete integration must still define output handling, placeholder
expansion, and authorization. Apply length limits to rendered output because
colour codes increase its size.

## Client actions

For click actions, validate the final value **after** placeholder expansion using
`ClickAction.accepts`. The helper rejects control characters and oversized values;
URL actions allow HTTP/HTTPS with a host and without user information. Command
actions require a leading slash. Validation alone does not authorize a command;
integrations must also define their own permitted actions and values.

```java
String finalValue = "https://example.com/help";
if (ClickAction.OPEN_URL.accepts(finalValue)) {
    // Use only an action permitted by the integration's policy.
}
```

## Public API boundaries

| Package/type | Purpose |
| --- | --- |
| `DeluxeGradientApi` | Runtime service interface |
| `DeluxeGradientProvider` | Optional static service access |
| `Slot` | Chat/name selection identifiers |
| `api.color` | Colour, gradient, interpolation, and wave model |
| `api.text.Decoration` | Supported text styles |
| `api.text.tree` | Immutable nodes, visitors, and factories |
| `api.event.PlayerGradientChangeEvent` | Server-independent change payload |

The change payload is not a Bukkit `Event` and cannot itself be registered as one.
The installed plugin supplies its event adapter. This repository builds without
Paper or Bukkit and does not implement the runtime service or formatting engine.
