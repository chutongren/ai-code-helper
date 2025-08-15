<template>
  <div class="chat-room">
    <div class="chat-header">AI Code Helper (chatID: {{ memoryId }})</div>
    <div class="messages" ref="messagesContainer">
      <div
        v-for="(message, index) in messages"
        :key="index"
        :class="['message-item', message.sender]"
      >
        <div class="message-bubble">
          {{ message.text }}
        </div>
      </div>
    </div>
    <div class="input-area">
      <input
        v-model="inputMessage"
        @keyup.enter="sendMessage"
        placeholder="Input your coding problem or interview question..."
        :disabled="isLoading"
      />
      <button @click="sendMessage" :disabled="isLoading">
        {{ isLoading ? "发送中..." : "发送" }}
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick } from "vue";
import axios from "axios";

interface Message {
  sender: "user" | "ai";
  text: string;
}

const API_BASE_URL = "http://localhost:8081/api/ai";
const memoryId = ref<number>(0);
const messages = ref<Message[]>([]);
const inputMessage = ref<string>("");
const isLoading = ref<boolean>(false);
const messagesContainer = ref<HTMLElement | null>(null);

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

  const userMessage = inputMessage.value;
  messages.value.push({ sender: "user", text: userMessage });
  inputMessage.value = "";
  isLoading.value = true;
  scrollToBottom();

  try {
    const eventSource = new EventSource(
      `${API_BASE_URL}/chat?memoryId=${
        memoryId.value
      }&message=${encodeURIComponent(userMessage)}`
    );

    let aiResponseText = "";
    messages.value.push({ sender: "ai", text: "" }); // 预留 AI 消息位置

    eventSource.onmessage = (event) => {
      aiResponseText += event.data;
      messages.value[messages.value.length - 1].text = aiResponseText; // 实时更新 AI 消息
      scrollToBottom();
    };

    eventSource.onerror = (error) => {
      console.error("SSE Error:", error);
      eventSource.close();
      isLoading.value = false;
      // Handle error, e.g., show an error message to the user
      messages.value[messages.value.length - 1].text =
        (messages.value[messages.value.length - 1].text || "") +
        "\n(消息接收失败，请重试)";
      scrollToBottom();
    };

    eventSource.onopen = () => {
      console.log("SSE connection opened");
    };

    eventSource.onclose = () => {
      console.log("SSE connection closed");
      isLoading.value = false;
    };
  } catch (error) {
    console.error("Error sending message:", error);
    isLoading.value = false;
    messages.value.push({
      sender: "ai",
      text: "发送失败，请检查网络或稍后重试。",
    });
    scrollToBottom();
  }
};

onMounted(() => {
  memoryId.value = generateMemoryId();
  messages.value.push({
    sender: "ai",
    text: "你好！我是你的 AI 编程小助手，有什么可以帮助你的吗？",
  });
  scrollToBottom();
});
</script>

<style scoped>
.chat-room {
  display: flex;
  flex-direction: column;
  height: 100vh;
  max-width: 800px;
  margin: 0 auto;
  border: 1px solid #e0e0e0;
  box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  font-family: "Arial", sans-serif;
}

.chat-header {
  padding: 15px;
  background-color: #f5f5f5;
  border-bottom: 1px solid #e0e0e0;
  text-align: center;
  font-weight: bold;
  color: #333;
}

.messages {
  flex-grow: 1;
  padding: 15px;
  overflow-y: auto;
  background-color: #f9f9f9;
}

.message-item {
  display: flex;
  margin-bottom: 10px;
}

.message-item.user {
  justify-content: flex-end;
}

.message-item.ai {
  justify-content: flex-start;
}

.message-bubble {
  max-width: 70%;
  padding: 10px 15px;
  border-radius: 20px;
  line-height: 1.5;
  word-wrap: break-word;
}

.message-item.user .message-bubble {
  background-color: #007bff;
  color: white;
  border-bottom-right-radius: 5px;
}

.message-item.ai .message-bubble {
  background-color: #e0e0e0;
  color: #333;
  border-bottom-left-radius: 5px;
}

.input-area {
  display: flex;
  padding: 15px;
  border-top: 1px solid #e0e0e0;
  background-color: #f5f5f5;
}

.input-area input {
  flex-grow: 1;
  padding: 10px 15px;
  border: 1px solid #ccc;
  border-radius: 20px;
  outline: none;
  font-size: 1rem;
}

.input-area button {
  margin-left: 10px;
  padding: 10px 20px;
  background-color: #007bff;
  color: white;
  border: none;
  border-radius: 20px;
  cursor: pointer;
  font-size: 1rem;
  transition: background-color 0.3s ease;
}

.input-area button:hover {
  background-color: #0056b3;
}

.input-area button:disabled {
  background-color: #cccccc;
  cursor: not-allowed;
}
</style>
