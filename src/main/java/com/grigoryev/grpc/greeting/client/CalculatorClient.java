package com.grigoryev.grpc.greeting.client;

import com.grigoryev.calculator.CalculatorServiceGrpc;
import com.grigoryev.calculator.ComputeAverageRequest;
import com.grigoryev.calculator.ComputeAverageResponse;
import com.grigoryev.calculator.FindMaximumRequest;
import com.grigoryev.calculator.FindMaximumResponse;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Slf4j
public class CalculatorClient {

    public static void main(String[] args) {
        log.info("Hello I am a gRPC client");

        CalculatorClient main = new CalculatorClient();
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
        CalculatorServiceGrpc.CalculatorServiceStub asyncClient = CalculatorServiceGrpc.newStub(channel);
        CountDownLatch latch = new CountDownLatch(1);
        StreamObserver<ComputeAverageRequest> requestObserver = asyncClient.computeAverage(new StreamObserver<>() {

            @Override
            public void onNext(ComputeAverageResponse value) {
                log.info("Received a response from the server\n{}", value.getAverage());
            }

            @Override
            public void onError(Throwable t) {
                log.error(t.getMessage());
                latch.countDown();
            }

            @Override
            public void onCompleted() {
                log.info("Server has completed sending us data...");
                latch.countDown();
            }

        });

        for (int i = 0; i < 100_000; i++) {
            requestObserver.onNext(ComputeAverageRequest.newBuilder()
                    .setNumber(i)
                    .build());
        }

        requestObserver.onCompleted();

        try {
            latch.await(10L, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            log.error(e.getMessage());
            Thread.currentThread().interrupt();
        }
    }

    private void doBiDiStreamingCall(ManagedChannel channel) {
        CalculatorServiceGrpc.CalculatorServiceStub asyncClient = CalculatorServiceGrpc.newStub(channel);
        CountDownLatch latch = new CountDownLatch(1);
        StreamObserver<FindMaximumRequest> requestObserver = asyncClient.findMaximum(new StreamObserver<>() {

            @Override
            public void onNext(FindMaximumResponse value) {
                log.info("Got new maximum from Server: {}", value.getMaximum());
            }

            @Override
            public void onError(Throwable t) {
                log.error(t.getMessage());
                latch.countDown();
            }

            @Override
            public void onCompleted() {
                log.info("Server is done sending messages...");
            }

        });

        Arrays.asList(3, 5, 17, 9, 8, 30, 12).forEach(
                number -> {
                    log.info("Sending number: {}", number);
                    requestObserver.onNext(FindMaximumRequest.newBuilder()
                            .setNumber(number)
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
