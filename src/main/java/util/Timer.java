package util;

import lombok.extern.java.Log;

@Log
public class Timer {
    final long startMs;
    long splitMs;

    public Timer(){
        splitMs = startMs = System.currentTimeMillis();
    }

    final static String SPLIT_FORMAT = "split %dms %s";
    public void split(String label) {
        var currentMs = System.currentTimeMillis();
        log.info(SPLIT_FORMAT.formatted(currentMs - startMs, label));
        splitMs = currentMs;
    }

    public void split(){
        split("");
    }

    public void stop(){
        log.info("stopped " + (System.currentTimeMillis() - startMs) + "ms");
    }
}
