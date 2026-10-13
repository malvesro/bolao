package com.bolao2027.modules.prediction.domain;

public class PredictionLockedException extends RuntimeException {
    public PredictionLockedException(String message) {
        super(message);
    }
}
