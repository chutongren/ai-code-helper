<template>
  <div class="chat-room">
    <div class="chat-header">
      <h1>🤖 AI Code Helper</h1>
      <p class="session-id">chatID: {{ memoryId }}</p>
    </div>

    <div class="messages" ref="messagesContainer">
      <div
        v-for="(message, index) in messages"
        :key="index"
        :class="['message-item', message.sender]"
      >
        <div class="message-bubble">
          <div class="message-avatar">
            {{ message.sender === "user" ? "👤" : "🤖" }}
          </div>
          <div class="message-content">
            {{ message.text }}
          </div>
        </div>
      </div>
    </div>

    <div class="input-area">
      <div class="input-container">
        <input
          v-model="inputMessage"
          @keyup.enter="sendMessage"
          placeholder="Input your coding problem or interview question..."
          :disabled="isLoading"
          class="message-input"
        />
        <button
          @click="sendMessage"
          :disabled="isLoading || !inputMessage.trim()"
          class="send-button"
        >
          <span v-if="!isLoading">发送</span>
          <span v-else>发送中...</span>
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick, onUnmounted } from "vue";

interface Message {
  sender: "user" | "ai";
  text: string;
  timestamp: Date;
}

const API_BASE_URL = "http://localhost:8081/api/ai";
const memoryId = ref<number>(0);
const messages = ref<Message[]>([]);
const inputMessage = ref<string>("");
const isLoading = ref<boolean>(false);
const messagesContainer = ref<HTMLElement | null>(null);
const currentEventSource = ref<EventSource | null>(null); // 添加当前连接引用

// 生成唯一的会话 ID
const generateMemoryId = () => {
  return Math.floor(Math.random() * 1000000);
};

// 滚动到最新消息
const scrollToBottom = () => {
  nextTick(() => {
    if (messagesContainer.value) {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight;
    }
  });
};

const sendMessage = async () => {
  if (!inputMessage.value.trim() || isLoading.value) {
    return;
  }

  // 强制关闭之前的连接
  if (currentEventSource.value) {
    currentEventSource.value.close();
    currentEventSource.value = null;
  }

  const userMessage = inputMessage.value;
  messages.value.push({
    sender: "user",
    text: userMessage,
    timestamp: new Date(),
  });
  inputMessage.value = "";
  isLoading.value = true;
  scrollToBottom();

  try {
    // 创建 AI 消息占位符
    const aiMessageIndex = messages.value.length;
    messages.value.push({
      sender: "ai",
      text: "",
      timestamp: new Date(),
    });

    // 创建新的 EventSource
    const eventSource = new EventSource(
      `${API_BASE_URL}/chat?memoryId=${
        memoryId.value
      }&message=${encodeURIComponent(userMessage)}`
    );

    // 保存当前连接引用
    currentEventSource.value = eventSource;

    let aiResponseText = "";
    let isCompleted = false;

    // 消息处理
    eventSource.onmessage = (event) => {
      if (!isCompleted) {
        aiResponseText += event.data;
        messages.value[aiMessageIndex].text = aiResponseText;
        scrollToBottom();
      }
    };

    // 连接打开
    eventSource.onopen = () => {
      console.log("SSE connection opened for message:", userMessage);
    };

    // 连接关闭 - 这是关键！
    eventSource.addEventListener("close", () => {
      console.log("SSE connection closed for message:", userMessage);
      isCompleted = true;
      isLoading.value = false;
      currentEventSource.value = null;

      // 如果消息为空，设置默认回复
      if (!messages.value[aiMessageIndex].text) {
        messages.value[aiMessageIndex].text = "❌ 连接已关闭，请重试";
      }
      scrollToBottom();
    });

    // 错误处理
    eventSource.onerror = (error) => {
      console.error("SSE Error for message:", userMessage, error);
      isCompleted = true;
      isLoading.value = false;
      currentEventSource.value = null;

      if (!messages.value[aiMessageIndex].text) {
        messages.value[aiMessageIndex].text = "❌ 连接错误，请重试";
      }
      scrollToBottom();

      // 强制关闭连接
      eventSource.close();
    };

    // 超时处理
    const timeoutId = setTimeout(() => {
      if (eventSource.readyState === EventSource.OPEN) {
        console.log("SSE connection timeout, closing...");
        isCompleted = true;
        isLoading.value = false;
        currentEventSource.value = null;

        if (!messages.value[aiMessageIndex].text) {
          messages.value[aiMessageIndex].text = "⏰ 响应超时，请重试";
        }

        eventSource.close();
        scrollToBottom();
      }
    }, 30000); // 30秒超时

    // 监听连接关闭事件，清理超时
    eventSource.addEventListener("close", () => {
      clearTimeout(timeoutId);
    });
  } catch (error) {
    console.error("Error sending message:", error);
    isLoading.value = false;
    messages.value.push({
      sender: "ai",
      text: "❌ 发送失败，请检查网络或稍后重试",
      timestamp: new Date(),
    });
    scrollToBottom();
  }
};

onMounted(() => {
  memoryId.value = generateMemoryId();
  messages.value.push({
    sender: "ai",
    text: "👋 Hello! I’m your AI Programming Assistant 🤖\n\nI can help you:\n• Answer programming learning questions\n• Provide interview question suggestions\n• Review and optimize code\n• Plan your learning roadmap\n\nPlease tell me what you need help with?",
    timestamp: new Date(),
  });
  scrollToBottom();
});

onUnmounted(() => {
  // 强制关闭当前连接
  if (currentEventSource.value) {
    console.log("Component unmounting, closing SSE connection");
    currentEventSource.value.close();
    currentEventSource.value = null;
  }
  // 重置状态
  isLoading.value = false;
});
</script>

<style scoped>
.chat-room {
  display: flex;
  flex-direction: column;
  height: 100vh;
  width: 100vw;
  max-width: 100%;
  margin: 0;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 0;
  overflow: hidden;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
}

.chat-header {
  background: rgba(255, 255, 255, 0.95);
  padding: 20px;
  text-align: center;
  border-bottom: 1px solid rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(10px);
}

.chat-header h1 {
  margin: 0 0 8px 0;
  color: #333;
  font-size: 24px;
  font-weight: 600;
}

.session-id {
  margin: 0;
  color: #666;
  font-size: 14px;
  font-family: "Courier New", monospace;
}

.messages {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
}

.messages::-webkit-scrollbar {
  width: 6px;
}

.messages::-webkit-scrollbar-track {
  background: rgba(255, 255, 255, 0.1);
}

.messages::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.3);
  border-radius: 3px;
}

.message-item {
  display: flex;
  margin-bottom: 20px;
  animation: fadeInUp 0.3s ease-out;
}

.message-item.user {
  justify-content: flex-end;
}

.message-item.ai {
  justify-content: flex-start;
}

.message-bubble {
  display: flex;
  align-items: flex-start;
  max-width: 75%;
  gap: 12px;
}

.message-avatar {
  font-size: 24px;
  flex-shrink: 0;
}

.message-content {
  background: rgba(255, 255, 255, 0.95);
  padding: 16px 20px;
  border-radius: 20px;
  line-height: 1.6;
  word-wrap: break-word;
  white-space: pre-wrap;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
  backdrop-filter: blur(10px);
  max-width: 400px;
  overflow-wrap: break-word;
  word-break: break-word;
}

.message-item.user .message-content {
  background: linear-gradient(135deg, #007bff, #0056b3);
  color: white;
  border-bottom-right-radius: 8px;
}

.message-item.ai .message-content {
  background: rgba(255, 255, 255, 0.95);
  color: #333;
  border-bottom-left-radius: 8px;
}

.input-area {
  background: rgba(255, 255, 255, 0.95);
  padding: 20px;
  border-top: 1px solid rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(10px);
}

.input-container {
  display: flex;
  gap: 12px;
  align-items: center;
}

.message-input {
  flex: 1;
  padding: 16px 20px;
  border: 2px solid rgba(102, 126, 234, 0.3);
  border-radius: 25px;
  outline: none;
  font-size: 16px;
  background: rgba(255, 255, 255, 0.9);
  transition: all 0.3s ease;
}

.message-input:focus {
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
  background: white;
}

.message-input:disabled {
  background: rgba(255, 255, 255, 0.6);
  cursor: not-allowed;
}

.send-button {
  padding: 16px 24px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  border: none;
  border-radius: 25px;
  cursor: pointer;
  font-size: 16px;
  font-weight: 600;
  transition: all 0.3s ease;
  white-space: nowrap;
  min-width: 80px;
}

.send-button:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
}

.send-button:disabled {
  background: #ccc;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .chat-room {
    max-width: 100%;
    border-radius: 0;
  }

  .message-bubble {
    max-width: 85%;
  }

  .chat-header h1 {
    font-size: 20px;
  }

  .message-input,
  .send-button {
    padding: 14px 18px;
    font-size: 14px;
  }
}
</style>
