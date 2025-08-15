package com.example.aicodehelper.ai;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
//import dev.langchain4j.model.chat.ChatLanguageModel;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import org.springframework.beans.factory.annotation.Autowired;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AiCodeHelper {
    // 全局唯一 覆盖
//    private static final String SYSTEM_MESSAGE = """
//
//            """;

    @Autowired
    private ChatModel chatModel; // ChatLanguageModel?
//编程式写法
    public String chat(String message){
//        SystemMessage systemMessage = SystemMessage.from(SYSTEM_MESSAGE);
        UserMessage userMessage = UserMessage.from(message);
//        ChatResponse chatResponse  = openAiChatModel.chat(systemMessage, userMessage);
        ChatResponse chatResponse  = chatModel.chat(userMessage);
        AiMessage aiMessage = chatResponse.aiMessage();
        log.info(aiMessage.toString());
        return aiMessage.text();
    }


}
