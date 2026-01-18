# Spring-AI

## 项目概览

本项目实现了一个基于 Spring AI 的多阶段演示：

1. **Phase 1：纯聊天** `/api/chat`
2. **Phase 2：RAG 检索增强** `/api/ask`
3. **Phase 3：工具调用** `/api/assist`
4. **Phase 4：流式输出 + 记忆** `/api/stream`
5. **Phase 5：前端页面** `src/main/resources/static/index.html`

## 快速开始

<<<<<<< codex/implement-spring-ai-chat-project-phases-yofwp1
### Windows 11 环境

1. 安装 **JDK 17** 与 **Maven 3.9+**，并配置环境变量：

   - `JAVA_HOME` 指向 JDK 安装目录
   - `Path` 中包含 `%JAVA_HOME%\\bin` 和 Maven 的 `bin`

2. 设置 Moonshot API Key（PowerShell）：

```powershell
$env:MOONSHOT_API_KEY="你的Key"
```

3. 启动应用：

```powershell
mvn spring-boot:run
```

4. 打开浏览器访问：`http://localhost:8080`

> 如果需要长期保存环境变量，可在「系统属性 → 高级 → 环境变量」中新增 `MOONSHOT_API_KEY`。

### macOS / Linux

=======
>>>>>>> main
```bash
export MOONSHOT_API_KEY=你的Key
mvn spring-boot:run
```

浏览器打开 `http://localhost:8080` 即可看到聊天页面。

## 配置说明

`src/main/resources/application.yml` 默认配置：

- 模型接口：`https://api.moonshot.cn/anthropic`
- 模型名称：`moonshot-v1`
- RAG TopK：4

## API 示例

### 纯聊天

```bash
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d '{"sessionId":"demo","message":"你好"}'
```

### RAG 检索

```bash
curl -X POST http://localhost:8080/api/ask \
  -H "Content-Type: application/json" \
  -d '{"sessionId":"demo","message":"退款多久到账？"}'
```

### 工具调用

```bash
curl -X POST http://localhost:8080/api/assist \
  -H "Content-Type: application/json" \
  -d '{"sessionId":"demo","message":"帮我查订单 123 的状态"}'
```

### SSE 流式输出

```bash
curl -N -X POST http://localhost:8080/api/stream \
  -H "Content-Type: application/json" \
  -d '{"sessionId":"demo","message":"介绍一下 Spring AI"}'
```
