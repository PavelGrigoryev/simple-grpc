package com.grigoryev.grpc.greeting.client;

import com.grigoryev.greet.GreetEveryoneRequest;
import com.grigoryev.greet.GreetEveryoneResponse;
import com.grigoryev.greet.GreetServiceGrpc;
import com.grigoryev.greet.Greeting;
import com.grigoryev.greet.LongGreatResponse;
import com.grigoryev.greet.LongGreetRequest;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Slf4j
public class GreetingClient {

    public static void main(String[] args) {
        log.info("Hello I am a gRPC client");

        GreetingClient main = new GreetingClient();
        main.run();
    }

    private void run() {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 50051)
                .usePlaintext()
                .build();

        doClientStreamingCall(channel);
        doBiDiStreamingCall(channel);

        log.info("Shutting down channel");
        channel.shutdown();
    }

    private void doClientStreamingCall(ManagedChannel channel) {
        GreetServiceGrpc.GreetServiceStub asyncClient = GreetServiceGrpc.newStub(channel);
        CountDownLatch latch = new CountDownLatch(1);
        StreamObserver<LongGreetRequest> requestObserver = asyncClient.longGreet(new StreamObserver<>() {

            @Override
            public void onNext(LongGreatResponse value) {
                log.info("Received a response from the server\n{}", value.getResult());
            }

            @Override
            public void onError(Throwable t) {
                log.error(t.getMessage());
                latch.countDown();
            }

            @Override
            public void onCompleted() {
                log.info("Server has completed sending us messages...");
                latch.countDown();
            }

        });

        log.info("Sending message #1");
        requestObserver.onNext(LongGreetRequest.newBuilder()
                .setGreeting(Greeting.newBuilder()
                        .setFirstName("Jim")
                        .setLastName("Root")
                        .build())
                .build());

        log.info("Sending message #2");
        requestObserver.onNext(LongGreetRequest.newBuilder()
                .setGreeting(Greeting.newBuilder()
                        .setFirstName("Chris")
                        .setLastName("Fehn")
                        .build())
                .build());

        log.info("Sending message #3");
        requestObserver.onNext(LongGreetRequest.newBuilder()
                .setGreeting(Greeting.newBuilder()
                        .setFirstName("Sid")
                        .setLastName("Wilson")
                        .build())
                .build());

        requestObserver.onCompleted();

        try {
            latch.await(3L, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            log.error(e.getMessage());
            Thread.currentThread().interrupt();
        }
    }

    private void doBiDiStreamingCall(ManagedChannel channel) {
        GreetServiceGrpc.GreetServiceStub asyncClient = GreetServiceGrpc.newStub(channel);
        CountDownLatch latch = new CountDownLatch(1);
        StreamObserver<GreetEveryoneRequest> requestObserver = asyncClient.greetEveryone(new StreamObserver<>() {

            @Override
            public void onNext(GreetEveryoneResponse value) {
                log.info("Response from server: {}", value.getResult());
            }

            @Override
            public void onError(Throwable t) {
                log.error(t.getMessage());
                latch.countDown();
            }

            @Override
            public void onCompleted() {
                log.info("Server is done sending data...");
                latch.countDown();
            }

        });

        Arrays.asList("Jim", "Sid", "Shawn", "Craig", "Mike").forEach(
                name -> {
                    log.info("Sending: " + name);
                    requestObserver.onNext(GreetEveryoneRequest.newBuilder()
                            .setGreeting(Greeting.newBuilder()
                                    .setFirstName(name)
                                    .build())
                            .build());
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        log.error(e.getMessage());
                        Thread.currentThread().interrupt();
                    }
                }
        );

        requestObserver.onCompleted();

        try {
            latch.await(3L, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            log.error(e.getMessage());
            Thread.currentThread().interrupt();
        }
    }

}
