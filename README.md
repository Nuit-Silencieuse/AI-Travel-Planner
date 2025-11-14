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

项目通过根目录下的 `.env` 文件读取所有必需的密钥和配置。

1.  **复制模板文件**: 在项目根目录下，将 `env.template` 文件复制并重命名为 `.env`。
2.  **填写密钥**: 打开新的 `.env` 文件，并填入以下所有必需的值。这些值将被 `docker-compose.yml` 自动读取并注入到对应的容器中。

    ```env
    # 后端环境变量 (Spring Boot)
    DB_URL=jdbc:postgresql://<your-supabase-db-host-and-port>/postgres
    DB_USERNAME=postgres
    DB_PASSWORD=<your-supabase-db-password>
    JWT_JWK_SET_URI=https://<your-supabase-project-ref>.supabase.co/auth/v1/.well-known/jwks.json
    ALIYUN_LLM_API_KEY=<your-aliyun-api-key>
    
    # 前端环境变量 (Vite)
    VITE_AMAP_KEY=<your-amap-key>
    ```
    > **提示**: Supabase 相关的 URL 和密码可以在您的 Supabase 项目的 `Settings -> Database` 和 `Settings -> API` 中找到。后端的 `application.properties` 文件已配置为从这些环境变量中读取值。

### 3. 启动应用

在项目根目录下，运行以下命令：

```bash
docker-compose up --build
```

- `--build` 参数会强制重新构建镜像，确保代码更改生效。
- 启动成功后，您可以通过浏览器访问 `http://localhost` 来使用本应用。

---

## ☁️ 部署与运行 (从 GitHub Releases)

本项目的 GitHub Actions 会在代码推送到 `main` 分支后，自动构建前后端镜像，并将其打包成 `.tar` 文件上传到 GitHub Releases。

### 1. 下载并加载镜像

1.  前往本项目的 [GitHub Releases](https://github.com/your-username/your-repo/releases) 页面。
2.  下载最新的 `backend-image.tar` 和 `frontend-image.tar` 文件。
3.  使用 `docker load` 命令将镜像加载到本地 Docker 中：

    ```bash
    # 加载后端镜像
    docker load < backend-image.tar

    # 加载前端镜像
    docker load < frontend-image.tar
    ```
    加载成功后，您可以使用 `docker images` 命令查看到导入的镜像。

### 2. 运行容器

#### 运行后端容器

您**必须**通过 `-e` 参数将所有环境变量注入到容器中。请将 `<...>` 替换为您的实际密钥。

```bash
docker run -d \
  -p 8080:8080 \
  -e DB_URL="jdbc:postgresql://<...>:5432/postgres" \
  -e DB_USERNAME="postgres" \
  -e DB_PASSWORD="<...>" \
  -e JWT_JWK_SET_URI="https://<...>.supabase.co/auth/v1/.well-known/jwks.json" \
  -e ALIYUN_LLM_API_KEY="sk-<...>" \
  --name ai-travel-planner-backend \
  registry.cn-hangzhou.aliyuncs.com/nuit_ai_travel_planner/nuit_ai_travel_planner:backend-latest
```
> **注意**: 请将命令最后的镜像名替换为您通过 `docker images` 查看到的实际镜像名和标签。

#### 运行前端容器

前端镜像在构建时已经包含了运行所需的配置，可以直接启动。

```bash
docker run -d \
  -p 80:80 \
  --name ai-travel-planner-frontend \
  registry.cn-hangzhou.aliyuncs.com/nuit_ai_travel_planner/nuit_ai_travel_planner:frontend-latest
```
> **注意**: 前端容器默认会连接 `http://localhost:8080` 上的后端服务。如果您的后端部署在其他地址，您需要修改 `frontend/src/components/MainDashboard.vue` 文件中的 API 请求地址，并重新构建前端镜像。

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
