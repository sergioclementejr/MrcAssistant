package com.mchouse.mcrassistant.exceptions;

import java.util.LinkedList;

public class ExceptionNotifier {
    private static LinkedList<ExceptionListener> listeners = new LinkedList<>();

    static void add(ExceptionListener el){
        listeners.add(el);
    }

    static void remove(ExceptionListener el){
        listeners.remove(el);
    }

    public static void notifyException(Throwable t){
        for(ExceptionListener listener : listeners){
            listener.notifyException(t);
        }
        t.printStackTrace(System.out);
    }
}
