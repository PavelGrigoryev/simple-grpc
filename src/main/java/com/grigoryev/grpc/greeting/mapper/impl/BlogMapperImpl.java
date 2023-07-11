package com.grigoryev.grpc.greeting.mapper.impl;

import com.google.protobuf.Timestamp;
import com.grigoryev.blog.Blog;
import com.grigoryev.blog.BlogResponse;
import com.grigoryev.blog.SaveBlogRequest;
import com.grigoryev.blog.UpdateByIdBlogRequest;
import com.grigoryev.grpc.greeting.mapper.BlogMapper;
import org.bson.Document;

import static com.grigoryev.grpc.greeting.util.BlogMongoConstants.AUTHOR;
import static com.grigoryev.grpc.greeting.util.BlogMongoConstants.CONTENT;
import static com.grigoryev.grpc.greeting.util.BlogMongoConstants.ID;
import static com.grigoryev.grpc.greeting.util.BlogMongoConstants.TITLE;

public class BlogMapperImpl implements BlogMapper {

    @Override
    public BlogResponse toBlogResponse(String id,
                                       SaveBlogRequest request,
                                       Timestamp timestamp) {
        return BlogResponse.newBuilder()
                .setBlog(Blog.newBuilder()
                        .setId(id)
                        .setAuthor(request.getAuthor())
                        .setTitle(request.getTitle())
                        .setContent(request.getContent())
                        .setCreatedTime(timestamp)
                        .setUpdatedTime(timestamp)
                        .build())
                .build();
    }

    @Override
    public BlogResponse toBlogResponse(String id,
                                       Document document,
                                       Timestamp createdTimestamp,
                                       Timestamp updatedTimestamp) {
        return BlogResponse.newBuilder()
                .setBlog(Blog.newBuilder()
                        .setId(id)
                        .setAuthor(document.getString(AUTHOR))
                        .setTitle(document.getString(TITLE))
                        .setContent(document.getString(CONTENT))
                        .setCreatedTime(createdTimestamp)
                        .setUpdatedTime(updatedTimestamp)
                        .build())
                .build();
    }

    @Override
    public BlogResponse toBlogResponse(String id,
                                       UpdateByIdBlogRequest request,
                                       Timestamp createdTimestamp,
                                       Timestamp updatedTimestamp) {
        return BlogResponse.newBuilder()
                .setBlog(Blog.newBuilder()
                        .setId(id)
                        .setAuthor(request.getAuthor())
                        .setTitle(request.getTitle())
                        .setContent(request.getContent())
                        .setCreatedTime(createdTimestamp)
                        .setUpdatedTime(updatedTimestamp)
                        .build())
                .build();
    }

    @Override
    public BlogResponse toBlogResponse(Document document,
                                       Timestamp createdTimestamp,
                                       Timestamp updatedTimestamp) {
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
    }

}
