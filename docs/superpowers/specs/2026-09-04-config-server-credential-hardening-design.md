# Config server credential hardening design / 配置服务器凭据加固设计

## 中文版

### 目标

移除仓库中提交的 Git 凭据，恢复正常的 TLS 证书校验；不修改配置服务器使用的仓库地址或分支标签。

### 范围

- 将 `configserver/src/main/resources/application.yml` 中明文的 `username` 与 `password` 改为 Spring 属性占位符：`${CONFIG_GIT_USERNAME}` 和 `${CONFIG_GIT_PASSWORD}`。
- 删除 `skip-ssl-validation: true`。
- 增加简短部署说明，列出两个必需的环境变量，并明确 TLS 校验保持启用。
- 不在仓库中轮换或暴露现有凭据；凭据轮换应作为独立的运维后续工作完成。

### 配置流程

启动时，Spring 从进程环境中解析 `CONFIG_GIT_USERNAME` 与 `CONFIG_GIT_PASSWORD`，并传给 Config Server 的 Git 后端。任一变量缺失时，应用会因无法解析配置而明确启动失败，而不会回退到仓库中保存的密钥。GitHub 的标准 TLS 证书校验会正常生效。

### 错误处理与验证

- 缺少任一环境变量的部署应因未解析的占位符而快速失败。
- 验证时检查最终差异，确保不再有字面凭据；如仓库构建配置允许，则运行 Maven 测试或编译。
- 拟议 PR 会说明所需环境变量，以及撤销或轮换已暴露凭据的必要性。

### 不在本次范围内

- 不变更 Git 仓库、认证方式、Spring Cloud 版本或 CI 设置。
- 不自动轮换现有 GitHub 凭据。

---

## English version

### Goal

Remove committed Git credentials and restore normal TLS certificate validation without changing the Config Server's repository URI or label.

### Scope

- Replace the current inline `username` and `password` in `configserver/src/main/resources/application.yml` with Spring property placeholders: `${CONFIG_GIT_USERNAME}` and `${CONFIG_GIT_PASSWORD}`.
- Remove `skip-ssl-validation: true`.
- Add a short deployment note that names the two required environment variables and states that TLS verification remains enabled.
- Do not rotate or expose any existing credential in the repository. Credential rotation is required operational follow-up.

### Configuration flow

At startup, Spring resolves `CONFIG_GIT_USERNAME` and `CONFIG_GIT_PASSWORD` from the process environment. The Config Server passes the resulting values to its Git backend. If either variable is missing, startup fails explicitly instead of silently using a committed secret. GitHub's standard TLS certificate validation is used.

### Error handling and verification

- A deployment missing either variable should fail fast with an unresolved-placeholder configuration error.
- Verification will inspect the final diff to ensure no literal credential remains, and run the Maven test/compile command if the repository's build configuration supports it.
- The proposed pull request will document the required environment variables and the need to revoke/rotate the previously exposed credential.

### Out of scope

- Changing the Git repository, authentication method, Spring Cloud version, or CI setup.
- Automatically rotating the existing GitHub credential.
