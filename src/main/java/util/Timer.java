package util;

import lombok.extern.java.Log;

@Log
public class Timer {
    final long startMs;

    public Timer(){
        startMs = System.currentTimeMillis();
    }

    public void stop(){
        log.info("stopped " + (System.currentTimeMillis() - startMs) + "ms");
    }
}
