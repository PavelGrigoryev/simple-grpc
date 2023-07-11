package com.grigoryev.grpc.greeting.mapper;

import com.google.protobuf.Timestamp;
import com.grigoryev.blog.BlogResponse;
import com.grigoryev.blog.SaveBlogRequest;
import com.grigoryev.blog.UpdateByIdBlogRequest;
import org.bson.Document;

public interface BlogMapper {

    BlogResponse toBlogResponse(String id,
                                SaveBlogRequest request,
                                Timestamp timestamp);

    BlogResponse toBlogResponse(String id,
                                Document document,
                                Timestamp createdTimestamp,
                                Timestamp updatedTimestamp);

    BlogResponse toBlogResponse(String id,
                                UpdateByIdBlogRequest request,
                                Timestamp createdTimestamp,
                                Timestamp updatedTimestamp);

    BlogResponse toBlogResponse(Document document,
                                Timestamp createdTimestamp,
                                Timestamp updatedTimestamp);

}
