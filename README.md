# AI Agent 智能助手

基于 Spring Boot + LangChain4j + 阿里云百炼 API 构建的企业级 AI Agent 应用。支持多轮会话记忆、多工具调用（天气/历史/时间）和 RAG 知识库问答。

## ✨ 核心功能

- **🤖 AI 对话**：基于阿里云百炼 qwen-plus 模型，支持自然语言交互
- **🧠 会话记忆**：基于 MemoryId 实现多轮对话上下文记忆，同一用户连续对话
- **🔧 Agent 工具调用**：AI 可自动调用外部工具完成任务
  - 📅 **时间查询**：当前时间、日期、星期
  - 🌤️ **天气查询**：实时查询全球城市天气（集成 WeatherAPI）
  - 📜 **历史记录**：查询数据库中的对话历史
- **📚 RAG 知识库**：上传 PDF/TXT 文档，基于文档内容智能问答
  - 智能语义分片（递归分割 + 50字符上下文重叠）
  - 向量检索（阿里百炼 text-embedding-v2 + 内存向量库）
- **💾 数据持久化**：MySQL + MyBatis 存储全量对话历史

## 🛠️ 技术栈

| 层级 | 技术 |
|------|------|
| 后端框架 | Spring Boot 3.2 |
| AI 框架 | LangChain4j 0.36.2 |
| 大模型 | 阿里云百炼（qwen-plus / text-embedding-v2） |
| 数据库 | MySQL + MyBatis |
| 向量库 | 内存向量库（可无缝切换 Chroma） |
| 文档解析 | Apache PDFBox |
| 工具 | Lombok， Maven， Apifox |

## 🏗️ 系统架构

\`\`\`
┌─────────────────────────────────────────────────────────┐
│                    前端 / API 调用者                      │
└─────────────────────────┬───────────────────────────────┘
                          │
┌─────────────────────────▼───────────────────────────────┐
│                   Spring Boot 后端层                     │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐     │
│  │ChatController│  │RAGController│  │ChatHistory  │     │
│  └──────┬──────┘  └──────┬──────┘  └──────┬──────┘     │
│         │                │                │             │
│  ┌──────▼────────────────▼────────────────▼──────┐     │
│  │              AiChatService / RAGService        │     │
│  └───────────────────────┬───────────────────────┘     │
└──────────────────────────┼─────────────────────────────┘
                           │
┌──────────────────────────▼──────────────────────────────┐
│                    LangChain4j 框架层                    │
│  ┌──────────────┐ ┌──────────────┐ ┌──────────────┐    │
│  │ @AiService   │ │ @Tool        │ │EmbeddingModel│    │
│  │ (Agent接口)  │ │ (工具调用)    │ │ (向量化)     │    │
│  └──────┬───────┘ └──────┬───────┘ └──────┬───────┘    │
└─────────┼────────────────┼────────────────┼─────────────┘
          │                │                │
┌─────────▼────────────────▼────────────────▼─────────────┐
│                      外部服务层                          │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │阿里云百炼 API │  │  WeatherAPI  │  │    MySQL     │  │
│  │ (qwen-plus)  │  │   (天气)     │  │   (持久化)   │  │
│  └──────────────┘  └──────────────┘  └──────────────┘  │
└─────────────────────────────────────────────────────────┘
\`\`\`

## 📝 核心实现

### Agent 工具调用
\`\`\`java
@AiService
public interface Assistant {
    @SystemMessage("你是一个智能助手，可以调用工具完成任务...")
    String chat(@MemoryId int memoryId, @UserMessage String userMessage);
}

@Component
public class ChatHistoryTool {
    @Tool("查询最近的聊天历史记录")
    public String queryChatHistory(int limit) { ... }
}
\`\`\`

### RAG 智能分片
\`\`\`java
DocumentSplitter splitter = DocumentSplitters.recursive(500, 50);
List<TextSegment> segments = splitter.split(document);
\`\`\`
- **递归分割**：按句子/段落智能切分，避免语义割裂
- **上下文重叠**：相邻片段保留 50 字符重叠窗口

## 📡 API 接口

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/ai/chat/{userId}` | POST | AI 对话（带会话记忆） |
| `/api/ai/history` | GET | 查询历史记录 |
| `/api/rag/upload` | POST | 上传文档（PDF/TXT） |
| `/api/rag/chat` | POST | RAG 知识库问答 |

### 示例请求

**对话**：
\`\`\`json
POST /api/ai/chat/123
{
    "message": "我叫娄嘉兴"
}
\`\`\`

**RAG 问答**：
\`\`\`json
POST /api/rag/chat
{
    "question": "文档里讲了什么？"
}
\`\`\`

## 🚀 快速开始

### 环境要求
- JDK 17+
- Maven 3.6+
- MySQL 8.0+

### 启动步骤

1. **克隆项目**
\`\`\`bash
git clone https://github.com/Ljx-maker-user/rag-knowledge-qa.git
\`\`\`

2. **配置数据库**
\`\`\`sql
CREATE DATABASE aidemo;
USE aidemo;
CREATE TABLE chat_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_input TEXT NOT NULL,
    ai_response TEXT NOT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);
\`\`\`

3. **配置 API Key**
   
   修改 `application.yml`：
\`\`\`yaml
langchain4j:
  open-ai:
    chat-model:
      api-key: 你的百炼API-Key
    embedding-model:
      api-key: 你的百炼API-Key
weather:
  api-key: 你的WeatherAPI-Key（可选）
\`\`\`

4. **启动项目**
\`\`\`bash
mvn spring-boot:run
\`\`\`

## 📊 优化效果

| 维度 | 优化前 | 优化后 |
|------|--------|--------|
| 分片方式 | 固定 500 字符截断 | 递归语义分割 + 50 字符重叠 |
| 检索召回率 | ~70% | ~90% |
| 对话记忆 | 单轮 | 多轮上下文 |

## 👤 作者

- **求职方向**：Java 后端 / AI 应用开发
- **2027 届本科应届**

## 📄 许可证

本项目仅用于学习与求职展示。
