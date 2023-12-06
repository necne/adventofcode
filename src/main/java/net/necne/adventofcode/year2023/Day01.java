package net.necne.adventofcode.year2023;

import com.google.common.io.Resources;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Locale;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

<<<<<<< HEAD
=======
/**
 * https://adventofcode.com/2023/day/1
 */

>>>>>>> 359d5cc (2023 day 1 (#1))
@Log
public class Day01 {
    @RequiredArgsConstructor
    enum Digit {
        ONE(1),TWO(2),THREE(3),FOUR(4),FIVE(5),SIX(6),SEVEN(7),EIGHT(8),NINE(9);

        final int digit;
    }

    static final Pattern DIGIT_SINGLE = Pattern.compile("(\\d)");
    static final Pattern DIGIT_WORD = Pattern.compile("(\\d|" + Arrays.stream(Digit.values()).map(d -> d.name().toLowerCase(Locale.ROOT)).collect(Collectors.joining("|")) + ")");

    static int parseInt(String digit){
        return digit.length() > 1
                ? Digit.valueOf(digit.toUpperCase(Locale.ROOT)).digit
                : Integer.parseInt(digit);
    }
    static int calibrate(String str, Pattern digit){
        Matcher mLead = digit.matcher(str);
        if(!mLead.find()) throw new IllegalArgumentException("Missing lead in " + str);

        for(int t = str.length()-1; t >=0; --t) {
            Matcher mTail = digit.matcher(str.substring(t));
            if (mTail.lookingAt()) {
                return parseInt(mLead.group(1)) * 10 + parseInt(mTail.group());
            }
        }
        throw new IllegalArgumentException("Missing tail in " + str);
    }

    private static void calibrateSum(String resource, Pattern digit) throws IOException {
        String[] lines = Resources.toString(Resources.getResource(resource), Charset.defaultCharset()).split("\n");
        int sum = 0;
        for(String line : lines){
            int calibrate = calibrate(line, digit);
            sum += calibrate;
        }
        log.info("calibration sum " + sum);
    }


    public static void main(String[] args) {
        try {
            //calibrateSum("2023/01/sample", DIGIT_SINGLE);
            //calibrateSum("2023/01/puzzle", DIGIT_SINGLE); // 55123
            //calibrateSum("2023/01/sample2", DIGIT_WORD); //281
            calibrateSum("2023/01/puzzle", DIGIT_WORD); //55260
        }
        catch(Exception e){
            log.log(Level.SEVERE, "main", e);
        }
    }
}
