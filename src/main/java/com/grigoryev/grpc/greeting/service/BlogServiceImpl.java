package com.grigoryev.grpc.greeting.service;

import com.google.protobuf.Empty;
import com.google.protobuf.Timestamp;
import com.grigoryev.blog.Blog;
import com.grigoryev.blog.BlogResponse;
import com.grigoryev.blog.BlogServiceGrpc;
import com.grigoryev.blog.DeleteByIdBlogRequest;
import com.grigoryev.blog.DeleteByIdBlogResponse;
import com.grigoryev.blog.FindByIdBlogRequest;
import com.grigoryev.blog.SaveBlogRequest;
import com.grigoryev.blog.UpdateByIdBlogRequest;
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

import static com.grigoryev.grpc.greeting.util.BlogMongoConstant.AUTHOR;
import static com.grigoryev.grpc.greeting.util.BlogMongoConstant.CONTENT;
import static com.grigoryev.grpc.greeting.util.BlogMongoConstant.CREATED_TIME;
import static com.grigoryev.grpc.greeting.util.BlogMongoConstant.ID;
import static com.grigoryev.grpc.greeting.util.BlogMongoConstant.TITLE;
import static com.grigoryev.grpc.greeting.util.BlogMongoConstant.UPDATED_TIME;
import static com.mongodb.client.model.Filters.eq;

@Slf4j
public class BlogServiceImpl extends BlogServiceGrpc.BlogServiceImplBase {

    private final MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017");
    private final MongoDatabase mongoDatabase = mongoClient.getDatabase("blog_db");
    private final MongoCollection<Document> mongoCollection = mongoDatabase.getCollection("blog");

    @Override
    public void save(SaveBlogRequest request, StreamObserver<BlogResponse> responseObserver) {
        Instant now = LocalDateTime.now().toInstant(ZoneOffset.UTC);
        Timestamp timestamp = Timestamp.newBuilder()
                .setSeconds(now.getEpochSecond())
                .setNanos(now.getNano())
                .build();

        Document document = new Document(AUTHOR, request.getAuthor())
                .append(TITLE, request.getTitle())
                .append(CONTENT, request.getContent())
                .append(CREATED_TIME, now)
                .append(UPDATED_TIME, now);

        mongoCollection.insertOne(document);

        BlogResponse response = BlogResponse.newBuilder()
                .setBlog(Blog.newBuilder()
                        .setId(document.getObjectId(ID).toString())
                        .setAuthor(request.getAuthor())
                        .setTitle(request.getTitle())
                        .setContent(request.getContent())
                        .setCreatedTime(timestamp)
                        .setUpdatedTime(timestamp)
                        .build())
                .build();

        log.info("Save:\n{}", response);
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void findById(FindByIdBlogRequest request, StreamObserver<BlogResponse> responseObserver) {
        String blogId = request.getId();
        Document document = mongoCollection.find(eq(ID, new ObjectId(blogId))).first();

        if (document == null) {
            StatusRuntimeException runtimeException = Status.NOT_FOUND
                    .withDescription("There is no blog with ID " + blogId)
                    .asRuntimeException();

            log.error("FindById error:\n{}", runtimeException.getStatus());
            responseObserver.onError(runtimeException);
        } else {
            Instant createdInstant = document.getDate(CREATED_TIME).toInstant();
            Timestamp createdTimestamp = Timestamp.newBuilder()
                    .setSeconds(createdInstant.getEpochSecond())
                    .setNanos(createdInstant.getNano())
                    .build();
            Instant updatedInstant = document.getDate(UPDATED_TIME).toInstant();
            Timestamp updatedTimestamp = Timestamp.newBuilder()
                    .setSeconds(updatedInstant.getEpochSecond())
                    .setNanos(updatedInstant.getNano())
                    .build();
            BlogResponse response = BlogResponse.newBuilder()
                    .setBlog(Blog.newBuilder()
                            .setId(blogId)
                            .setAuthor(document.getString(AUTHOR))
                            .setTitle(document.getString(TITLE))
                            .setContent(document.getString(CONTENT))
                            .setCreatedTime(createdTimestamp)
                            .setUpdatedTime(updatedTimestamp)
                            .build())
                    .build();

            log.info("FindById:\n{}", response);
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }

    @Override
    public void updateById(UpdateByIdBlogRequest request, StreamObserver<BlogResponse> responseObserver) {
        String blogId = request.getId();
        Document document = mongoCollection.find(eq(ID, new ObjectId(blogId))).first();

        if (document == null) {
            StatusRuntimeException runtimeException = Status.NOT_FOUND
                    .withDescription("There is no blog with ID " + blogId + " to update")
                    .asRuntimeException();

            log.error("UpdateById error:\n{}", runtimeException.getStatus());
            responseObserver.onError(runtimeException);
        } else {
            Instant createdInstant = document.getDate(CREATED_TIME).toInstant();
            Timestamp createdTimestamp = Timestamp.newBuilder()
                    .setSeconds(createdInstant.getEpochSecond())
                    .setNanos(createdInstant.getNano())
                    .build();
            Instant updatedInstant = LocalDateTime.now().toInstant(ZoneOffset.UTC);
            Timestamp updatedTimestamp = Timestamp.newBuilder()
                    .setSeconds(updatedInstant.getEpochSecond())
                    .setNanos(updatedInstant.getNano())
                    .build();

            Document documentToUpdate = new Document(AUTHOR, request.getAuthor())
                    .append(TITLE, request.getTitle())
                    .append(CONTENT, request.getContent())
                    .append(CREATED_TIME, createdInstant)
                    .append(UPDATED_TIME, updatedInstant);

            mongoCollection.replaceOne(eq(ID, document.getObjectId(ID)), documentToUpdate);

            String id = document.getObjectId(ID).toString();
            BlogResponse response = BlogResponse.newBuilder()
                    .setBlog(Blog.newBuilder()
                            .setId(id)
                            .setAuthor(request.getAuthor())
                            .setTitle(request.getTitle())
                            .setContent(request.getContent())
                            .setCreatedTime(createdTimestamp)
                            .setUpdatedTime(updatedTimestamp)
                            .build())
                    .build();

            log.info("UpdateById:\n{}", response);
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }

    @Override
    public void deleteById(DeleteByIdBlogRequest request, StreamObserver<DeleteByIdBlogResponse> responseObserver) {
        String blogId = request.getId();
        DeleteResult result = mongoCollection.deleteOne(eq(ID, new ObjectId(blogId)));

        if (result.getDeletedCount() == 0) {
            StatusRuntimeException runtimeException = Status.NOT_FOUND
                    .withDescription("There is no blog with ID " + blogId + " to delete")
                    .asRuntimeException();

            log.error("DeleteById error:\n{}", runtimeException.getStatus());
            responseObserver.onError(runtimeException);
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
                    Timestamp createdTimestamp = Timestamp.newBuilder()
                            .setSeconds(createdInstant.getEpochSecond())
                            .setNanos(createdInstant.getNano())
                            .build();
                    Instant updatedInstant = document.getDate(UPDATED_TIME).toInstant();
                    Timestamp updatedTimestamp = Timestamp.newBuilder()
                            .setSeconds(updatedInstant.getEpochSecond())
                            .setNanos(updatedInstant.getNano())
                            .build();
                    return BlogResponse.newBuilder()
                            .setBlog(Blog.newBuilder()
                                    .setId(document.getObjectId(ID).toString())
                                    .setAuthor(document.getString(AUTHOR))
                                    .setTitle(document.getString(TITLE))
                                    .setContent(document.getString(CONTENT))
                                    .setCreatedTime(createdTimestamp)
                                    .setUpdatedTime(updatedTimestamp)
                                    .build())
                            .build();
                })
                .forEach(response -> {
                    log.info("FindAll:\n{}", response);
                    responseObserver.onNext(response);
                });

        responseObserver.onCompleted();
    }

}
