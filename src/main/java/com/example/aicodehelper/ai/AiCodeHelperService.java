package com.example.aicodehelper.ai;

import dev.langchain4j.service.SystemMessage;

import java.util.List;

public interface AiCodeHelperService {
    @SystemMessage(fromResource = "system-prompt.txt")
    String chat(String userMessage);

    // 结构化输出 prompt + 结构化输出的方法
    @SystemMessage(fromResource = "system-prompt.txt")
    Report chatForReport(String userMessage);
    record Report(String name, List<String> suggestionList){}

    // RAG

}
