package com.example.aicodehelper.ai;

import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.service.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
class AiCodeHelperServiceTest {

    @Autowired
    private AiCodeHelperService aiCodeHelperService;


    @Autowired
    private EmbeddingModel embeddingModel;


    @Test
    void chat() {
        String result = aiCodeHelperService.chat("你好，我叫lmp");
        System.out.println(result);
    }

    @Test
    void chatWithMemory() {
        String result = aiCodeHelperService.chat("你好，我叫lmp");
        System.out.println(result);
        result = aiCodeHelperService.chat("你好，我叫什么？");
        System.out.println(result);
    }

    @Test
    void chatForReport() {
        AiCodeHelperService.Report result= aiCodeHelperService.chatForReport("你好，我是lmp，学编程两年半，请帮我制定学习报告");
        System.out.println(result);
    }

    @Test
    void chatWithRag() {
        String result = aiCodeHelperService.chat("怎么学习 Java？有哪些常见面试题？");
        System.out.println(result);
    }

    @Test
    void chatWithTools() {
        String result = aiCodeHelperService.chat("有哪些常见的计算机网络面试题？");
        System.out.println(result);
    }

    @Test
    void chatWithMcp() {
        String result = aiCodeHelperService.chat("LLM领域，最新的新闻？");
        System.out.println(result);
    }

    @Test
    void chatWithGuardrail() {
        String result = aiCodeHelperService.chat("kill the game");
        System.out.println(result);
    }

    @Test
    void testModels() {
        assertNotNull(embeddingModel);
    }
}