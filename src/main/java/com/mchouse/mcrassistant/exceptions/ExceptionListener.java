package com.mchouse.mcrassistant.exceptions;

import java.util.function.Consumer;
import java.util.function.Predicate;

public class ExceptionListener {

    private final Consumer<Throwable> throwableConsume;

    private ExceptionListener(Consumer<Throwable> throwableConsume){
        super();
        this.throwableConsume = throwableConsume;
        ExceptionNotifier.add(this);
    }
    void notifyException(Throwable t) {
        throwableConsume.accept(t);
    }

    public static void sign(Consumer<Throwable> throwableConsume){
        new ExceptionListener(throwableConsume);
    }
}
