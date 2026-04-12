package com.example.aidemo.service;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentParser;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import dev.langchain4j.data.document.parser.apache.pdfbox.ApachePdfBoxDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class RAGService {

    @Value("${langchain4j.open-ai.embedding-model.api-key}")
    private String apiKey;

    @Value("${langchain4j.open-ai.embedding-model.base-url}")
    private String baseUrl;

    private EmbeddingModel embeddingModel;
    private EmbeddingStore<TextSegment> embeddingStore;

    @PostConstruct
    public void init() {
        this.embeddingModel = OpenAiEmbeddingModel.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .modelName("text-embedding-v2")
                .timeout(Duration.ofSeconds(60))
                .build();

        // 用内存向量库，不需要 Docker
        this.embeddingStore = new InMemoryEmbeddingStore<>();
        log.info("RAGService 初始化完成，使用内存向量库");
    }

    public void uploadDocument(MultipartFile file) throws IOException {
        log.info("开始处理文档: {}", file.getOriginalFilename());

        Document document = parseDocument(file);
        String content = document.text();

        List<TextSegment> segments = splitContent(content);

        for (TextSegment segment : segments) {
            Embedding embedding = embeddingModel.embed(segment).content();
            embeddingStore.add(embedding, segment);
        }

        log.info("文档处理完成，共存入 {} 个片段", segments.size());
    }

    private Document parseDocument(MultipartFile file) throws IOException {
        String fileName = file.getOriginalFilename();
        DocumentParser parser;

        if (fileName != null && fileName.toLowerCase().endsWith(".pdf")) {
            parser = new ApachePdfBoxDocumentParser();
        } else {
            parser = new TextDocumentParser();
        }

        return parser.parse(file.getInputStream());
    }

    private List<TextSegment> splitContent(String content) {
        DocumentSplitter splitter = DocumentSplitters.recursive(500, 50);
        return splitter.split(new Document(content));
    }

    public List<String> search(String query, int maxResults) {
        Embedding queryEmbedding = embeddingModel.embed(query).content();
        var matches = embeddingStore.findRelevant(queryEmbedding, maxResults);

        return matches.stream()
                .map(match -> match.embedded().text())
                .collect(Collectors.toList());
    }
}