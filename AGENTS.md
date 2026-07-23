# Repository Instructions

## AI Rule References

Before making backend code changes in this repository, read and follow:

- `docs/ai-rules/README.md`
- `docs/ai-rules/backend-ai-rule.md`

For code review tasks, also read:

- `docs/ai-rules/code-review-rule.md`

For test creation or test modification tasks, also read:

- `docs/ai-rules/test-generation-rule.md`

For security, configuration, dependency, secret, Kafka, OpenSearch, or infrastructure-related changes, also read:

- `docs/ai-rules/security-check-rule.md`

Treat `docs/ai-rules/infra/` as supporting infrastructure reference. Do not modify infrastructure files unless the user explicitly asks.

## HTTP API Response Shape

For backend HTTP APIs, return successful responses with `ApiResponse.success(...)` and return failures with `ApiResponse.fail(...)`.

Use this envelope shape:

- `success`: boolean request outcome
- `data`: successful response body, or `null` on failure
- `meta`: pagination or response metadata, or `null` when unused
- `error`: standard error body, or `null` on success

Keep `ErrorResponse` fields as `timestamp`, `path`, `code`, `message`, and `details`.

If these rules conflict with an explicit user request, follow the user request and call out the conflict.
