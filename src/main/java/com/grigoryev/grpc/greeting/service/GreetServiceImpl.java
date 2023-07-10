package com.grigoryev.grpc.greeting.service;

import com.grigoryev.greet.GreetEveryoneRequest;
import com.grigoryev.greet.GreetEveryoneResponse;
import com.grigoryev.greet.GreetManyTimesRequest;
import com.grigoryev.greet.GreetManyTimesResponse;
import com.grigoryev.greet.GreetRequest;
import com.grigoryev.greet.GreetResponse;
import com.grigoryev.greet.GreetServiceGrpc;
import com.grigoryev.greet.GreetWithDeadlineRequest;
import com.grigoryev.greet.GreetWithDeadlineResponse;
import com.grigoryev.greet.Greeting;
import com.grigoryev.greet.LongGreatResponse;
import com.grigoryev.greet.LongGreetRequest;
import io.grpc.Context;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GreetServiceImpl extends GreetServiceGrpc.GreetServiceImplBase {

    @Override
    public void greet(GreetRequest request, StreamObserver<GreetResponse> responseObserver) {
        Greeting greeting = request.getGreeting();

        String result = "Hello " + greeting.getFirstName() + " " + greeting.getLastName();
        GreetResponse response = GreetResponse.newBuilder()
                .setResult(result)
                .build();

        log.info(response.toString());
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void greetManyTimes(GreetManyTimesRequest request, StreamObserver<GreetManyTimesResponse> responseObserver) {
        try {
            for (int i = 0; i < 10; i++) {
                String result = "Hello " + request.getGreeting().getFirstName() + ", response number: " + i;
                GreetManyTimesResponse response = GreetManyTimesResponse.newBuilder()
                        .setResult(result)
                        .build();

                log.info(response.toString());
                responseObserver.onNext(response);
                Thread.sleep(1000L);
            }
        } catch (InterruptedException e) {
            log.error(e.getMessage());
            Thread.currentThread().interrupt();
        } finally {
            responseObserver.onCompleted();
        }
    }

    @Override
    public StreamObserver<LongGreetRequest> longGreet(StreamObserver<LongGreatResponse> responseObserver) {
        return new StreamObserver<>() {

            private final StringBuilder result = new StringBuilder();

            @Override
            public void onNext(LongGreetRequest value) {
                result.append("Hello ")
                        .append(value.getGreeting().getFirstName())
                        .append(" ")
                        .append(value.getGreeting().getLastName())
                        .append("! ");
            }

            @Override
            public void onError(Throwable t) {
                log.error(t.getMessage());
            }

            @Override
            public void onCompleted() {
                LongGreatResponse longGreatResponse = LongGreatResponse.newBuilder()
                        .setResult(result.toString())
                        .build();
                log.info(longGreatResponse.toString());
                responseObserver.onNext(longGreatResponse);
                responseObserver.onCompleted();
            }

        };
    }

    @Override
    public StreamObserver<GreetEveryoneRequest> greetEveryone(StreamObserver<GreetEveryoneResponse> responseObserver) {
        return new StreamObserver<>() {

            @Override
            public void onNext(GreetEveryoneRequest value) {
                String result = "Hello " + value.getGreeting().getFirstName();
                GreetEveryoneResponse greetEveryoneResponse = GreetEveryoneResponse.newBuilder()
                        .setResult(result)
                        .build();

                log.info(greetEveryoneResponse.toString());
                responseObserver.onNext(greetEveryoneResponse);
            }

            @Override
            public void onError(Throwable t) {
                log.error(t.getMessage());
            }

            @Override
            public void onCompleted() {
                responseObserver.onCompleted();
            }

        };
    }

    @Override
    public void greetWithDeadline(GreetWithDeadlineRequest request, StreamObserver<GreetWithDeadlineResponse> responseObserver) {
        Context context = Context.current();

        try {
            for (int i = 0; i < 3; i++) {
                if (!context.isCancelled()) {
                    log.info("Sleep for 100 ms");
                    Thread.sleep(100);
                } else {
                    return;
                }
            }

            GreetWithDeadlineResponse response = GreetWithDeadlineResponse.newBuilder()
                    .setResult("Hello! " + request.getGreeting().getFirstName())
                    .build();

            log.info(response.toString());
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (InterruptedException e) {
            log.error(e.getMessage());
            Thread.currentThread().interrupt();
        }
    }

}
