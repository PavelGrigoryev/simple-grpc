package com.grigoryev.grpc.greeting.service;

import com.grigoryev.calculator.CalculatorServiceGrpc;
import com.grigoryev.calculator.SumRequest;
import com.grigoryev.calculator.SumResponse;
import io.grpc.stub.StreamObserver;

public class CalculatorServiceImpl extends CalculatorServiceGrpc.CalculatorServiceImplBase {

    @Override
    public void sum(SumRequest request, StreamObserver<SumResponse> responseObserver) {
        SumResponse sumResponse = SumResponse.newBuilder()
                .setSumResult(request.getFirstNumber() + request.getSecondNumber())
                .build();

        responseObserver.onNext(sumResponse);

        responseObserver.onCompleted();
    }

}
