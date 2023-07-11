package com.grigoryev.grpc.greeting.util;

import com.google.protobuf.GeneratedMessageV3;
import io.envoyproxy.pgv.ReflectiveValidatorIndex;
import io.envoyproxy.pgv.ValidationException;
import io.envoyproxy.pgv.ValidatorIndex;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UtilityClass
public class RequestValidator {

    public boolean validateRequest(Object request, StreamObserver<? extends GeneratedMessageV3> responseObserver) {
        ValidatorIndex index = new ReflectiveValidatorIndex();
        try {
            index.validatorFor(request.getClass()).assertValid(request);
        } catch (ValidationException e) {
            StatusRuntimeException runtimeException = Status.INVALID_ARGUMENT
                    .withDescription(e.getMessage())
                    .asRuntimeException();

            log.error("Validation error:\n{}", e.getMessage());
            responseObserver.onError(runtimeException);
            return true;
        }
        return false;
    }

}
