package com.grigoryev.grpc.greeting.dao.impl;

import com.grigoryev.blog.SaveBlogRequest;
import com.grigoryev.blog.UpdateByIdBlogRequest;
import com.grigoryev.grpc.greeting.dao.BlogDao;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.time.Instant;

import static com.grigoryev.grpc.greeting.util.BlogMongoConstants.AUTHOR;
import static com.grigoryev.grpc.greeting.util.BlogMongoConstants.CONTENT;
import static com.grigoryev.grpc.greeting.util.BlogMongoConstants.CREATED_TIME;
import static com.grigoryev.grpc.greeting.util.BlogMongoConstants.TITLE;
import static com.grigoryev.grpc.greeting.util.BlogMongoConstants.UPDATED_TIME;

public class BlogDaoImpl implements BlogDao {

    private final MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017");
    private final MongoDatabase mongoDatabase = mongoClient.getDatabase("blog_db");
    private final MongoCollection<Document> mongoCollection = mongoDatabase.getCollection("blog");

    @Override
    public Document insertOne(SaveBlogRequest request, Instant instant) {
        Document document = new Document(AUTHOR, request.getAuthor())
                .append(TITLE, request.getTitle())
                .append(CONTENT, request.getContent())
                .append(CREATED_TIME, instant)
                .append(UPDATED_TIME, instant);

        mongoCollection.insertOne(document);

        return document;
    }

    @Override
    public Document find(Bson filter) {
        return mongoCollection.find(filter).first();
    }

    @Override
    public void replaceOne(Bson filter, UpdateByIdBlogRequest request, Instant createdInstant, Instant updatedInstant) {
        Document documentToUpdate = new Document(AUTHOR, request.getAuthor())
                .append(TITLE, request.getTitle())
                .append(CONTENT, request.getContent())
                .append(CREATED_TIME, createdInstant)
                .append(UPDATED_TIME, updatedInstant);

        mongoCollection.replaceOne(filter, documentToUpdate);
    }

    @Override
    public Long deleteOne(Bson filter) {
        return mongoCollection.deleteOne(filter).getDeletedCount();
    }

    @Override
    public FindIterable<Document> findAll() {
        return mongoCollection.find();
    }

}
