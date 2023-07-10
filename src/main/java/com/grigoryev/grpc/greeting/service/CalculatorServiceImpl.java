package com.grigoryev.grpc.greeting.service;

import com.grigoryev.calculator.CalculatorServiceGrpc;
import com.grigoryev.calculator.ComputeAverageRequest;
import com.grigoryev.calculator.ComputeAverageResponse;
import com.grigoryev.calculator.FindMaximumRequest;
import com.grigoryev.calculator.FindMaximumResponse;
import com.grigoryev.calculator.PrimeNumberDecompositionRequest;
import com.grigoryev.calculator.PrimeNumberDecompositionResponse;
import com.grigoryev.calculator.SquareRootRequest;
import com.grigoryev.calculator.SquareRootResponse;
import com.grigoryev.calculator.SumRequest;
import com.grigoryev.calculator.SumResponse;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Slf4j
public class CalculatorServiceImpl extends CalculatorServiceGrpc.CalculatorServiceImplBase {

    @Override
    public void sum(SumRequest request, StreamObserver<SumResponse> responseObserver) {
        SumResponse sumResponse = SumResponse.newBuilder()
                .setSumResult(request.getFirstNumber() + request.getSecondNumber())
                .build();

        log.info(sumResponse.toString());
        responseObserver.onNext(sumResponse);
        responseObserver.onCompleted();
    }

    @Override
    public void primeNumberDecomposition(PrimeNumberDecompositionRequest request,
                                         StreamObserver<PrimeNumberDecompositionResponse> responseObserver) {
        long number = request.getNumber();
        long divisor = 2L;

        while (number > 1) {
            if (number % divisor == 0) {
                number = number / divisor;
                PrimeNumberDecompositionResponse response = PrimeNumberDecompositionResponse.newBuilder()
                        .setPrimeFactor(divisor)
                        .build();
                log.info(response.toString());
                responseObserver.onNext(response);
            } else {
                divisor = divisor + 1;
            }
        }
        responseObserver.onCompleted();
    }

    @Override
    public StreamObserver<ComputeAverageRequest> computeAverage(StreamObserver<ComputeAverageResponse> responseObserver) {
        return new StreamObserver<>() {

            private int sum = 0;
            private int count = 0;

            @Override
            public void onNext(ComputeAverageRequest value) {
                sum += value.getNumber();
                count += 1;
            }

            @Override
            public void onError(Throwable t) {
                log.error(t.getMessage());
            }

            @Override
            public void onCompleted() {
                BigDecimal average = BigDecimal.valueOf(sum)
                        .divide(BigDecimal.valueOf(count), 4, RoundingMode.UP)
                        .stripTrailingZeros();
                ComputeAverageResponse response = ComputeAverageResponse.newBuilder()
                        .setAverage(Double.parseDouble(average.toString()))
                        .build();

                log.info(response.toString());
                responseObserver.onNext(response);
                responseObserver.onCompleted();
            }

        };
    }

    @Override
    public StreamObserver<FindMaximumRequest> findMaximum(StreamObserver<FindMaximumResponse> responseObserver) {
        return new StreamObserver<>() {

            private long currentMaximum = 0;

            @Override
            public void onNext(FindMaximumRequest value) {
                long currentNumber = value.getNumber();
                if (currentNumber > currentMaximum) {
                    currentMaximum = currentNumber;
                    FindMaximumResponse response = FindMaximumResponse.newBuilder()
                            .setMaximum(currentMaximum)
                            .build();

                    log.info(response.toString());
                    responseObserver.onNext(response);
                }
            }

            @Override
            public void onError(Throwable t) {
                log.error(t.getMessage());
                responseObserver.onCompleted();
            }

            @Override
            public void onCompleted() {
                FindMaximumResponse response = FindMaximumResponse.newBuilder()
                        .setMaximum(currentMaximum)
                        .build();

                log.info(response.toString());
                responseObserver.onNext(response);
                responseObserver.onCompleted();
            }

        };
    }

    @Override
    public void squareRoot(SquareRootRequest request, StreamObserver<SquareRootResponse> responseObserver) {
        long number = request.getNumber();

        if (number > 0) {
            double numberRoot = Math.sqrt(number);
            SquareRootResponse response = SquareRootResponse.newBuilder()
                    .setNumberRoot(numberRoot)
                    .build();

            log.info(response.toString());
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } else {
            log.error("The number being sent is not positive: {}", number);
            responseObserver.onError(
                    Status.INVALID_ARGUMENT
                            .withDescription("The number being sent is not positive")
                            .augmentDescription("Number sent: " + number)
                            .asRuntimeException()
            );
        }
    }

}
