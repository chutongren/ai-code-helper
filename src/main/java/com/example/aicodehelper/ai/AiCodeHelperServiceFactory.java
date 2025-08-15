package com.example.aicodehelper.ai;

import com.example.aicodehelper.ai.tool.InterviewQuestionTool;
import dev.langchain4j.mcp.McpToolProvider;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.service.AiServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiCodeHelperServiceFactory {
    @Autowired
    private ChatModel chatModel;

    @Autowired
    private StreamingChatModel streamingChatModel;

    @Autowired
    private ContentRetriever contentRetriever;

    @Autowired
    private McpToolProvider mcpToolProvider;

//    声明式：自动创建出这个接口的实现类
    @Bean
    public AiCodeHelperService aiCodeHelperService() {
//        return AiServices.create(AiCodeHelperService.class, chatModel);

        // Chat Memory
        ChatMemory chatMemory = MessageWindowChatMemory.withMaxMessages(10);
        // AiService Builder
        AiCodeHelperService aiCodeHelperService = AiServices.builder(AiCodeHelperService.class)
                .chatModel(chatModel)
                .streamingChatModel(streamingChatModel)
                .chatMemory(chatMemory)
                .chatMemoryProvider(memoryId -> MessageWindowChatMemory.withMaxMessages(10))
                .contentRetriever(contentRetriever) // RAG
                .tools(new InterviewQuestionTool())
                .toolProvider(mcpToolProvider) // MCP 工具调用
                .build();
        return aiCodeHelperService;

    }
}
