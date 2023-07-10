package com.grigoryev.grpc.greeting.service;

import com.grigoryev.greet.GreetManyTimesRequest;
import com.grigoryev.greet.GreetManyTimesResponse;
import com.grigoryev.greet.GreetRequest;
import com.grigoryev.greet.GreetResponse;
import com.grigoryev.greet.GreetServiceGrpc;
import com.grigoryev.greet.Greeting;
import io.grpc.stub.StreamObserver;

public class GreetServiceImpl extends GreetServiceGrpc.GreetServiceImplBase {

    @Override
    public void greet(GreetRequest request, StreamObserver<GreetResponse> responseObserver) {
        Greeting greeting = request.getGreeting();

        String result = "Hello " + greeting.getFirstName() + " " + greeting.getLastName();
        GreetResponse response = GreetResponse.newBuilder()
                .setResult(result)
                .build();

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

                responseObserver.onNext(response);
                Thread.sleep(1000L);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        } finally {
            responseObserver.onCompleted();
        }
    }

}
