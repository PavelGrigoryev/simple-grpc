package com.grigoryev.grpc.greeting.server;

import com.grigoryev.grpc.greeting.service.BlogServiceImpl;
import com.grigoryev.grpc.greeting.service.CalculatorServiceImpl;
import com.grigoryev.grpc.greeting.service.GreetServiceImpl;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class GreetingServer {

    public static void main(String[] args) throws IOException, InterruptedException {
        log.info("Hello ice2 gRPC)");

        Server server = ServerBuilder.forPort(50051)
                .addService(new GreetServiceImpl())
                .addService(new CalculatorServiceImpl())
                .addService(new BlogServiceImpl())
                .build()
                .start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Received Shutdown Request");
            server.shutdown();
            log.info("Successfully stopped the server");
        }));

        server.awaitTermination();
    }

}
