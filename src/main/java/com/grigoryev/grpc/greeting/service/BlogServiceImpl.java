package com.grigoryev.grpc.greeting.service;

import com.google.protobuf.Empty;
import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.Timestamp;
import com.grigoryev.blog.BlogResponse;
import com.grigoryev.blog.BlogServiceGrpc;
import com.grigoryev.blog.DeleteByIdBlogRequest;
import com.grigoryev.blog.DeleteByIdBlogResponse;
import com.grigoryev.blog.FindByIdBlogRequest;
import com.grigoryev.blog.SaveBlogRequest;
import com.grigoryev.blog.UpdateByIdBlogRequest;
import com.grigoryev.grpc.greeting.mapper.BlogMapper;
import com.grigoryev.grpc.greeting.mapper.impl.BlogMapperImpl;
import com.grigoryev.grpc.greeting.util.RequestValidator;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.DeleteResult;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static com.grigoryev.grpc.greeting.util.BlogMongoConstants.AUTHOR;
import static com.grigoryev.grpc.greeting.util.BlogMongoConstants.CONTENT;
import static com.grigoryev.grpc.greeting.util.BlogMongoConstants.CREATED_TIME;
import static com.grigoryev.grpc.greeting.util.BlogMongoConstants.ID;
import static com.grigoryev.grpc.greeting.util.BlogMongoConstants.TITLE;
import static com.grigoryev.grpc.greeting.util.BlogMongoConstants.UPDATED_TIME;
import static com.mongodb.client.model.Filters.eq;

@Slf4j
public class BlogServiceImpl extends BlogServiceGrpc.BlogServiceImplBase {

    private final MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017");
    private final MongoDatabase mongoDatabase = mongoClient.getDatabase("blog_db");
    private final MongoCollection<Document> mongoCollection = mongoDatabase.getCollection("blog");
    private final BlogMapper blogMapper = new BlogMapperImpl();

    @Override
    public void save(SaveBlogRequest request, StreamObserver<BlogResponse> responseObserver) {
        if (RequestValidator.validateRequest(request, responseObserver)) {
            return;
        }
        Instant now = LocalDateTime.now().toInstant(ZoneOffset.UTC);
        Timestamp timestamp = getTimestamp(now);

        Document document = new Document(AUTHOR, request.getAuthor())
                .append(TITLE, request.getTitle())
                .append(CONTENT, request.getContent())
                .append(CREATED_TIME, now)
                .append(UPDATED_TIME, now);

        mongoCollection.insertOne(document);

        String id = document.getObjectId(ID).toString();
        BlogResponse response = blogMapper.toBlogResponse(id, request, timestamp);

        log.info("Save:\n{}", response);
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void findById(FindByIdBlogRequest request, StreamObserver<BlogResponse> responseObserver) {
        if (RequestValidator.validateRequest(request, responseObserver)) {
            return;
        }
        String blogId = request.getId();
        Document document = mongoCollection.find(eq(ID, new ObjectId(blogId))).first();

        if (document == null) {
            throwStatusRuntimeException("There is no blog with ID " + blogId,
                    "FindById error:\n{}", responseObserver);
        } else {
            Instant createdInstant = document.getDate(CREATED_TIME).toInstant();
            Timestamp createdTimestamp = getTimestamp(createdInstant);
            Instant updatedInstant = document.getDate(UPDATED_TIME).toInstant();
            Timestamp updatedTimestamp = getTimestamp(updatedInstant);

            BlogResponse response = blogMapper.toBlogResponse(blogId, document, createdTimestamp, updatedTimestamp);

            log.info("FindById:\n{}", response);
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }

    @Override
    public void updateById(UpdateByIdBlogRequest request, StreamObserver<BlogResponse> responseObserver) {
        if (RequestValidator.validateRequest(request, responseObserver)) {
            return;
        }
        String blogId = request.getId();
        Document document = mongoCollection.find(eq(ID, new ObjectId(blogId))).first();

        if (document == null) {
            throwStatusRuntimeException("There is no blog with ID " + blogId + " to update",
                    "UpdateById error:\n{}", responseObserver);
        } else {
            Instant createdInstant = document.getDate(CREATED_TIME).toInstant();
            Timestamp createdTimestamp = getTimestamp(createdInstant);
            Instant updatedInstant = LocalDateTime.now().toInstant(ZoneOffset.UTC);
            Timestamp updatedTimestamp = getTimestamp(updatedInstant);

            Document documentToUpdate = new Document(AUTHOR, request.getAuthor())
                    .append(TITLE, request.getTitle())
                    .append(CONTENT, request.getContent())
                    .append(CREATED_TIME, createdInstant)
                    .append(UPDATED_TIME, updatedInstant);

            mongoCollection.replaceOne(eq(ID, document.getObjectId(ID)), documentToUpdate);

            BlogResponse response = blogMapper.toBlogResponse(blogId, request, createdTimestamp, updatedTimestamp);

            log.info("UpdateById:\n{}", response);
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }

    @Override
    public void deleteById(DeleteByIdBlogRequest request, StreamObserver<DeleteByIdBlogResponse> responseObserver) {
        if (RequestValidator.validateRequest(request, responseObserver)) {
            return;
        }
        String blogId = request.getId();
        DeleteResult result = mongoCollection.deleteOne(eq(ID, new ObjectId(blogId)));

        if (result.getDeletedCount() == 0) {
            throwStatusRuntimeException("There is no blog with ID " + blogId + " to delete",
                    "DeleteById error:\n{}", responseObserver);
        } else {
            DeleteByIdBlogResponse response = DeleteByIdBlogResponse.newBuilder()
                    .setMessage("Blog with ID " + blogId + " was successfully deleted")
                    .build();

            log.info("DeleteById:\n{}", response);
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }

    @Override
    public void findAll(Empty request, StreamObserver<BlogResponse> responseObserver) {
        mongoCollection.find()
                .map(document -> {
                    Instant createdInstant = document.getDate(CREATED_TIME).toInstant();
                    Timestamp createdTimestamp = getTimestamp(createdInstant);
                    Instant updatedInstant = document.getDate(UPDATED_TIME).toInstant();
                    Timestamp updatedTimestamp = getTimestamp(updatedInstant);
                    return blogMapper.toBlogResponse(document, createdTimestamp, updatedTimestamp);
                })
                .forEach(response -> {
                    log.info("FindAll:\n{}", response);
                    responseObserver.onNext(response);
                });

        responseObserver.onCompleted();
    }

    private static Timestamp getTimestamp(Instant instant) {
        return Timestamp.newBuilder()
                .setSeconds(instant.getEpochSecond())
                .setNanos(instant.getNano())
                .build();
    }

    private static void throwStatusRuntimeException(String description,
                                                    String logMessage,
                                                    StreamObserver<? extends GeneratedMessageV3> responseObserver) {
        StatusRuntimeException runtimeException = Status.NOT_FOUND
                .withDescription(description)
                .asRuntimeException();

        log.error(logMessage, description);
        responseObserver.onError(runtimeException);
    }

}
