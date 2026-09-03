# Config Server Credential Hardening Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Remove committed Config Server Git credentials and require them from environment variables while keeping TLS certificate validation enabled.

**Architecture:** The existing Spring Boot YAML remains the only runtime configuration file changed. Git authentication values are resolved by Spring from process environment variables, while removal of the SSL-bypass setting restores the client default of certificate validation. A deployment note documents the required variables and the operational credential-rotation follow-up.

**Tech Stack:** Java, Spring Boot / Spring Cloud Config Server 2.0.2.RELEASE, Maven, YAML.

**Spec:** `docs/superpowers/specs/2026-09-04-config-server-credential-hardening-design.md`

## Global Constraints

- Keep the current Config Server Git URI (`https://github.com/jayzhoou/springcloud.git`) and `label: master` unchanged.
- Use exactly `${CONFIG_GIT_USERNAME}` and `${CONFIG_GIT_PASSWORD}` for credential resolution.
- Remove `skip-ssl-validation: true`; do not replace it with another TLS-bypass option.
- Never commit a literal credential or an example secret.
- Do not change the authentication method, Spring Cloud version, or CI setup.

---

## File Structure

- Modify `configserver/src/main/resources/application.yml`: retain the current Git URI, search path, and label; exchange literal credentials for environment placeholders and remove the SSL bypass.
- Create `configserver/README.md`: state the two required environment variables, a safe PowerShell launch example, TLS behavior, and required credential rotation.

### Task 1: Replace committed configuration credentials

**Files:**

- Modify: `configserver/src/main/resources/application.yml:7-15`
- Test: `configserver/src/main/resources/application.yml` (static configuration verification)

**Interfaces:**

- Consumes: process environment variables `CONFIG_GIT_USERNAME` and `CONFIG_GIT_PASSWORD`.
- Produces: Spring properties `spring.cloud.config.server.git.username` and `spring.cloud.config.server.git.password` resolved at application startup.

- [ ] **Step 1: Capture the unsafe baseline**

Run: `rg -n "password:|skip-ssl-validation" configserver/src/main/resources/application.yml`

Expected: output identifies a literal `password:` value and `skip-ssl-validation: true`.

- [ ] **Step 2: Replace the Git credential values and remove the SSL bypass**

Set the Git section to:

```yaml
          uri: https://github.com/jayzhoou/springcloud.git
          searchPaths: config
          username: ${CONFIG_GIT_USERNAME}
          password: ${CONFIG_GIT_PASSWORD}
```

Delete this line entirely:

```yaml
          skip-ssl-validation: true
```

- [ ] **Step 3: Verify the safe configuration**

Run: `rg -n "CONFIG_GIT_(USERNAME|PASSWORD)|skip-ssl-validation|password:" configserver/src/main/resources/application.yml`

Expected: exactly the two placeholder lines are returned; no `skip-ssl-validation` entry or literal password is returned.

- [ ] **Step 4: Compile the Config Server module**

Run: `mvn -pl configserver -am test`

Expected: Maven exits with status 0. If dependency resolution fails because the original project uses unavailable historical dependencies, record the command output in the PR; do not weaken TLS or restore credentials.

- [ ] **Step 5: Commit the configuration change**

```bash
git add configserver/src/main/resources/application.yml
git commit -m "fix: externalize config server Git credentials"
```

### Task 2: Document secure deployment requirements

**Files:**

- Create: `configserver/README.md`
- Test: `configserver/README.md` (documentation verification)

**Interfaces:**

- Consumes: `CONFIG_GIT_USERNAME` and `CONFIG_GIT_PASSWORD` supplied by the deployment environment.
- Produces: a copyable deployment contract for operators; it contains no credential values.

- [ ] **Step 1: Create the deployment note**

Write `configserver/README.md` with these sections:

```markdown
# Config Server

## Git credentials

Before starting the Config Server, set both environment variables:

- `CONFIG_GIT_USERNAME`
- `CONFIG_GIT_PASSWORD`

PowerShell example (replace the placeholders locally; do not commit values):

```powershell
$env:CONFIG_GIT_USERNAME = '<GitHub username>'
$env:CONFIG_GIT_PASSWORD = '<GitHub token or password>'
mvn -pl configserver spring-boot:run
```

TLS certificate validation is enabled. Do not add `skip-ssl-validation`.

## Credential rotation

The credential previously committed to repository history must be revoked or rotated in GitHub and replaced through the deployment secret store.
```

- [ ] **Step 2: Verify documentation does not add a secret**

Run: `rg -n "09708030g|skip-ssl-validation: true" configserver/README.md configserver/src/main/resources/application.yml`

Expected: no matches.

- [ ] **Step 3: Inspect the complete branch diff**

Run: `git diff master...HEAD -- configserver/src/main/resources/application.yml configserver/README.md`

Expected: only environment placeholders, SSL-bypass removal, and the deployment note are present; no literal credential appears.

- [ ] **Step 4: Commit the documentation change**

```bash
git add configserver/README.md
git commit -m "docs: describe config server credential setup"
```

### Task 3: Prepare the pull request

**Files:**

- Modify: no additional repository files.
- Test: pull-request diff and GitHub status checks.

**Interfaces:**

- Consumes: the two commits from Tasks 1 and 2.
- Produces: a draft pull request from `codex/remove-config-git-credentials` to `master` with deployment and credential-rotation guidance.

- [ ] **Step 1: Create a draft pull request**

Use title: `fix: externalize Config Server Git credentials`.

Use this body:

```markdown
## Summary

- replace committed Config Server Git credentials with `CONFIG_GIT_USERNAME` and `CONFIG_GIT_PASSWORD`
- remove `skip-ssl-validation` so TLS certificate validation remains enabled
- document secure deployment and credential rotation requirements

## Verification

- static YAML inspection confirms no literal password or SSL bypass remains
- `mvn -pl configserver -am test`

## Operational follow-up

Revoke or rotate the credential that was previously committed to repository history, then store its replacement in the deployment secret manager.
```

- [ ] **Step 2: Review the PR diff and checks**

Confirm the PR contains no literal credential. If the Maven command in Task 1 failed, retain the failure details in the PR and do not mark checks as passing.

- [ ] **Step 3: Request review**

Request reviewer approval only after the diff and verification record match this plan.
