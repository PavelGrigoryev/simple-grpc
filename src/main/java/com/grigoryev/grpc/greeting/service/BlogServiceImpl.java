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
import com.grigoryev.grpc.greeting.dao.BlogDao;
import com.grigoryev.grpc.greeting.dao.impl.BlogDaoImpl;
import com.grigoryev.grpc.greeting.mapper.BlogMapper;
import com.grigoryev.grpc.greeting.mapper.impl.BlogMapperImpl;
import com.grigoryev.grpc.greeting.util.RequestValidator;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static com.grigoryev.grpc.greeting.util.BlogMongoConstants.CREATED_TIME;
import static com.grigoryev.grpc.greeting.util.BlogMongoConstants.ID;
import static com.grigoryev.grpc.greeting.util.BlogMongoConstants.UPDATED_TIME;
import static com.mongodb.client.model.Filters.eq;

@Slf4j
public class BlogServiceImpl extends BlogServiceGrpc.BlogServiceImplBase {

    private final BlogDao blogDao = new BlogDaoImpl();
    private final BlogMapper blogMapper = new BlogMapperImpl();

    @Override
    public void save(SaveBlogRequest request, StreamObserver<BlogResponse> responseObserver) {
        if (RequestValidator.validateRequest(request, responseObserver)) {
            return;
        }

        Instant now = LocalDateTime.now().toInstant(ZoneOffset.UTC);
        Timestamp timestamp = getTimestamp(now);

        Document document = blogDao.insertOne(request, now);
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
        Document document = blogDao.find(eq(ID, new ObjectId(blogId)));

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
        Document document = blogDao.find(eq(ID, new ObjectId(blogId)));

        if (document == null) {
            throwStatusRuntimeException("There is no blog with ID " + blogId + " to update",
                    "UpdateById error:\n{}", responseObserver);
        } else {
            Instant createdInstant = document.getDate(CREATED_TIME).toInstant();
            Timestamp createdTimestamp = getTimestamp(createdInstant);
            Instant updatedInstant = LocalDateTime.now().toInstant(ZoneOffset.UTC);
            Timestamp updatedTimestamp = getTimestamp(updatedInstant);

            blogDao.replaceOne(eq(ID, document.getObjectId(ID)), request, createdInstant, updatedInstant);

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
        long result = blogDao.deleteOne(eq(ID, new ObjectId(blogId)));

        if (result == 0) {
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
        blogDao.findAll()
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
