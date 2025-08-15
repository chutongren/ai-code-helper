package com.example.aicodehelper.ai.rag;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.splitter.DocumentByParagraphSplitter;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import java.util.List;

@Configuration
public class RagConfig {
    
    @Autowired
    private EmbeddingModel embeddingModel;

    @Bean
    public EmbeddingStore<TextSegment> embeddingStore() {
        return new InMemoryEmbeddingStore<>();
    }

    @Bean
    @Lazy
    public ContentRetriever contentRetriever(){
        try {
            List<Document> documents = FileSystemDocumentLoader.loadDocuments("src/main/resources/docs");
            DocumentByParagraphSplitter paragraphSplitter = new DocumentByParagraphSplitter(1000,200);
            EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                    .documentSplitter(paragraphSplitter)
                    .textSegmentTransformer(textSegment -> TextSegment.from(textSegment.metadata().getString("file_name") + "\n" + textSegment.text(), textSegment.metadata()))
                    .embeddingModel(embeddingModel)
                    .embeddingStore(embeddingStore())
                    .build();

            ingestor.ingest(documents);

            ContentRetriever contentRetriever = EmbeddingStoreContentRetriever.builder()
                    .embeddingStore(embeddingStore())
                    .embeddingModel(embeddingModel)
                    .maxResults(5)
                    .minScore(0.75)
                    .build();
            return contentRetriever;
        } catch (Exception e) {
            // 如果文档处理失败，返回一个简单的ContentRetriever
            // 这样Spring上下文仍然可以启动
            return EmbeddingStoreContentRetriever.builder()
                    .embeddingStore(embeddingStore())
                    .embeddingModel(embeddingModel)
                    .maxResults(5)
                    .minScore(0.75)
                    .build();
        }
    }
}
