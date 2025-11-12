# AI Travel Planner

AI Travel Planner 是一款基于大语言模型（LLM）的智能旅行规划 Web 应用。用户可以通过自然语言（文本或语音）输入旅行需求，AI 将自动生成包含交通、住宿、景点、餐厅的个性化旅行计划，并通过地图提供直观的交互体验。

## ✨ 功能特性

- **智能行程规划**: 基于阿里百炼大模型，根据用户输入的目的地、时间、预算、偏好等信息，自动生成详细的行程。
- **地图中心交互**: 主界面以高德地图为中心，行程中的地点会在地图上进行标记。
- **语音输入**: 支持通过语音输入旅行偏好，简化用户操作。
- **用户系统**: 集成 Supabase Auth，提供安全可靠的注册和登录功能。
- **数据云同步**: 所有行程数据与用户账户绑定，并存储于 Supabase 的云端 PostgreSQL 数据库。
- **Docker 化部署**: 整个应用（前端 + 后端）被容器化，支持通过 Docker 和 Docker Compose 一键启动。
- **CI/CD**: 通过 GitHub Actions 实现代码提交后自动构建 Docker 镜像并推送到阿里云容器镜像服务 (ACR)。

## 🛠️ 技术栈

- **前端**:
  - **框架**: Vue 3
  - **状态管理**: Pinia
  - **地图服务**: 高德地图 JavaScript API 2.0
  - **语音识别**: Web Speech API
- **后端**:
  - **框架**: Spring Boot 3
  - **语言**: Java 17
  - **构建工具**: Maven
  - **大语言模型**: 阿里百炼 (Dashscope SDK)
- **数据库/认证**:
  - **认证**: Supabase Auth
  - **数据库**: Supabase (PostgreSQL)
- **部署**:
  - **容器化**: Docker & Docker Compose
  - **CI/CD**: GitHub Actions
  - **镜像仓库**: 阿里云容器镜像服务 (ACR)

## 📸 项目截图

*(在此处可以放置一张应用主界面的截图)*
![App Screenshot](placeholder.png)

---

## 🚀 本地开发 (使用 Docker Compose)

这是在本地环境中最快启动整个应用的方式。

### 1. 环境准备

- 安装 [Docker](https://www.docker.com/products/docker-desktop/) 和 Docker Compose。
- 克隆本仓库到本地。

### 2. 配置环境变量

项目启动需要配置一些密钥和 API Key。

1.  **复制模板文件**: 在项目根目录下，将 `env.template` 文件复制并重命名为 `.env`。
2.  **填写密钥**: 打开新的 `.env` 文件，并填入以下所有必需的值：

    ```env
    # 后端环境变量
    DB_URL=jdbc:postgresql://<your-supabase-db-host-and-port>/postgres
    DB_USERNAME=postgres
    DB_PASSWORD=<your-supabase-db-password>
    JWT_JWK_SET_URI=https://<your-supabase-project-ref>.supabase.co/auth/v1/.well-known/jwks.json
    ALIYUN_LLM_API_KEY=<your-aliyun-api-key>

    # 前端环境变量
    VITE_AMAP_KEY=<your-amap-key>
    ```
    > **提示**: Supabase 相关的 URL 和密码可以在您的 Supabase 项目的 `Settings -> Database` 和 `Settings -> API` 中找到。

### 3. 启动应用

在项目根目录下，运行以下命令：

```bash
docker-compose up --build
```

- `--build` 参数会强制重新构建镜像，确保代码更改生效。
- 启动成功后，您可以通过浏览器访问 `http://localhost` 来使用本应用。

---

## ☁️ 部署与运行 (从阿里云 ACR)

本项目的 GitHub Actions 会在代码推送到 `main` 分支后，自动将前后端镜像推送到阿里云 ACR。以下是如何从 ACR 拉取并运行这些镜像的说明。

### 1. 拉取镜像

请将 `<your-registry-url>`, `<your-namespace>`, 和 `<sha-commit-hash>` 替换为您的实际信息。

```bash
# 拉取后端镜像
docker pull <your-registry-url>/<your-namespace>/<your-namespace>:backend-<sha-commit-hash>

# 拉取前端镜像
docker pull <your-registry-url>/<your-namespace>/<your-namespace>:frontend-<sha-commit-hash>
```
> **示例**: `docker pull crpi-xxx.cn-hangzhou.personal.cr.aliyuncs.com/nuit_ai_travel_planner/nuit_ai_travel_planner:backend-a1b2c3d`

### 2. 运行容器

#### 运行后端容器

您**必须**通过 `-e` 参数将所有环境变量注入到容器中。

```bash
docker run -d \
  -p 8080:8080 \
  -e DB_URL="jdbc:postgresql://<...>:5432/postgres" \
  -e DB_USERNAME="postgres" \
  -e DB_PASSWORD="<...>" \
  -e JWT_JWK_SET_URI="https://<...>.supabase.co/auth/v1/.well-known/jwks.json" \
  -e ALIYUN_LLM_API_KEY="sk-<...>" \
  --name ai-travel-planner-backend \
  <your-registry-url>/<your-namespace>/<your-namespace>:backend-<sha-commit-hash>
```

#### 运行前端容器

前端容器需要通过 `VITE_AMAP_KEY` 来加载高德地图。由于我们的 Dockerfile 设计是在构建时注入此 Key，因此从 ACR 拉取的镜像已经包含了这个 Key，可以直接运行。

**但是**，如果需要在运行时覆盖或提供 Key，则需要更复杂的 Nginx 配置。基于当前简单的部署需求，我们假设构建时 Key 已注入。

```bash
docker run -d \
  -p 80:80 \
  --name ai-travel-planner-frontend \
  <your-registry-url>/<your-namespace>/<your-namespace>:frontend-<sha-commit-hash>
```
> **注意**: 前端容器依赖后端服务。在上面的命令中，前端容器会尝试连接到 `http://localhost:8080`。如果您的后端容器没有部署在宿主机的 8080 端口，您需要重建前端镜像，并在 `MainDashboard.vue` 中修改 `fetch` 请求的 URL。

### 3. 访问应用

部署成功后，访问部署服务器的 IP 地址或域名即可使用。

## 🔑 环境变量参考

| 变量名 | 描述 | 来源 |
| :--- | :--- | :--- |
| `DB_URL` | Supabase 数据库的 JDBC 连接字符串。 | Supabase Project > Settings > Database |
| `DB_USERNAME` | 数据库用户名 (通常是 `postgres`)。 | Supabase Project > Settings > Database |
| `DB_PASSWORD` | 数据库密码。 | Supabase Project > Settings > Database |
| `JWT_JWK_SET_URI` | 用于验证 JWT 的 JWKS 地址。 | Supabase Project > Settings > API |
| `ALIYUN_LLM_API_KEY` | 阿里百炼大模型的 API Key。 | 阿里云百炼平台 |
| `VITE_AMAP_KEY` | 前端使用的高德地图 JS API Key。 | 高德开放平台 |
