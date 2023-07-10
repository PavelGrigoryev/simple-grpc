package com.grigoryev.grpc.greeting.service;

import com.grigoryev.greet.GreetManyTimesRequest;
import com.grigoryev.greet.GreetManyTimesResponse;
import com.grigoryev.greet.GreetRequest;
import com.grigoryev.greet.GreetResponse;
import com.grigoryev.greet.GreetServiceGrpc;
import com.grigoryev.greet.Greeting;
import com.grigoryev.greet.LongGreatResponse;
import com.grigoryev.greet.LongGreetRequest;
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

}
