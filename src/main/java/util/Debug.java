package util;

import lombok.extern.java.Log;

import java.util.logging.Level;
import java.util.regex.Matcher;

@Log
public class Debug {

    public static String toStringGroups(Matcher matcher){
        StringBuilder sb = new StringBuilder();
        String format = String.format("  %%%dd %%s", (int) (Math.floor(Math.log(matcher.groupCount()))) + 1);
        for(int g = 0; g <= matcher.groupCount(); ++g) {
            if(!sb.isEmpty()) sb.append("\n");
            sb.append(String.format(format, g, matcher.group(g)));
        }
        return sb.toString();
    }

    public static <T> boolean log(T type){
        log.info("" + type);
        return true;
    }

}
