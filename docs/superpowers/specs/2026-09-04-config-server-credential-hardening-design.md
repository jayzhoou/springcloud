# Config server credential hardening design

## Goal

Remove committed Git credentials and restore normal TLS certificate validation without changing the Config Server's repository URI or label.

## Scope

- Replace the current inline \`username\` and \`password\` in \`configserver/src/main/resources/application.yml\` with Spring property placeholders: \`\${CONFIG_GIT_USERNAME}\` and \`\${CONFIG_GIT_PASSWORD}\`.
- Remove \`skip-ssl-validation: true\`.
- Add a short deployment note that names the two required environment variables and states that TLS verification remains enabled.
- Do not rotate or expose any existing credential in the repository. Credential rotation is required operational follow-up.

## Configuration flow

At startup, Spring resolves \`CONFIG_GIT_USERNAME\` and \`CONFIG_GIT_PASSWORD\` from the process environment. The Config Server passes the resulting values to its Git backend. If either variable is missing, startup fails explicitly instead of silently using a committed secret. GitHub's standard TLS certificate validation is used.

## Error handling and verification

- A deployment missing either variable should fail fast with an unresolved-placeholder configuration error.
- Verification will inspect the final diff to ensure no literal credential remains, and run the Maven test/compile command if the repository's build configuration supports it.
- The proposed pull request will document the required environment variables and the need to revoke/rotate the previously exposed credential.

## Out of scope

- Changing the Git repository, authentication method, Spring Cloud version, or CI setup.
- Automatically rotating the existing GitHub credential.
