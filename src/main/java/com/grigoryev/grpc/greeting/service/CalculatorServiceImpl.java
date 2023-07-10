package com.grigoryev.grpc.greeting.service;

import com.grigoryev.calculator.CalculatorServiceGrpc;
import com.grigoryev.calculator.ComputeAverageRequest;
import com.grigoryev.calculator.ComputeAverageResponse;
import com.grigoryev.calculator.PrimeNumberDecompositionRequest;
import com.grigoryev.calculator.PrimeNumberDecompositionResponse;
import com.grigoryev.calculator.SumRequest;
import com.grigoryev.calculator.SumResponse;
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

}
