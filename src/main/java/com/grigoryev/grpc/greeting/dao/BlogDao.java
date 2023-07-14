package com.grigoryev.grpc.greeting.dao;

import com.grigoryev.blog.SaveBlogRequest;
import com.grigoryev.blog.UpdateByIdBlogRequest;
import com.mongodb.client.FindIterable;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.time.Instant;

public interface BlogDao {

    Document insertOne(SaveBlogRequest request, Instant instant);

    Document find(Bson filter);

    void replaceOne(Bson filter, UpdateByIdBlogRequest request, Instant createdInstant, Instant updatedInstant);

    Long deleteOne(Bson filter);

    FindIterable<Document> findAll();

}
