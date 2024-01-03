package net.necne.adventofcode.year2023;

import com.google.common.io.Resources;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.java.Log;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * https://adventofcode.com/2023/day/3
 */

@Log
public class Day03 {

    static Pattern NUMBER = Pattern.compile("(\\d+)");

    @Data
    @Accessors(fluent = true)
    @RequiredArgsConstructor(staticName = "of")
    static class Pos {
        final int x;
        final int y;
    }

    @Data
    @Accessors(fluent = true)
    @RequiredArgsConstructor(staticName = "of")
    static class Number {
        final Pos pos;
        final int num;
    }

    @Data
    @Accessors(fluent = true)
    @Builder
    static class Schematic {
        List<Number> numbers;
        Set<Pos> symbols;
    }

    static Schematic parse(String resource) throws IOException {
        Schematic.SchematicBuilder schematic = Schematic.builder().numbers(new ArrayList<>()).symbols(new HashSet<>());
        String[] lines = Resources.toString(Resources.getResource(resource), Charset.defaultCharset()).split("\n");
        int y = -1;
        for(String line : lines) {
            ++y;

            int x = 0;
            while(!line.isEmpty()) {
                switch (line.charAt(0)){
                    case '.':
                        ++x;
                        line = line.substring(1);
                        break;
                    case '*':
                    case '#':
                    case '+':
                    case '$':
                    case '=':
                    case '-':
                    case '/':
                    case '@':
                    case '%':
                    case '&':
                        schematic.symbols.add(Pos.of(x, y));
                        ++x;
                        line = line.substring(1);
                        break;
                    default:
                        //attempt to treat as number
                        Matcher m = NUMBER.matcher(line);
                        if(!m.find()) throw new IllegalArgumentException("Number not found: " + line);
                        schematic.numbers.add(Number.of(Pos.of(x,y), Integer.parseInt(m.group(1))));
                        x += m.group(1).length();
                        line = line.substring(m.group(1).length());
                        break;
                }

            }
        }
        return schematic.build();
    }

    static boolean isPartNumber(Number number, Pos symbol){
        Pos pos = number.pos;
        //top
        if(symbol.y < pos.y - 1) return false;
        //bottom
        if(symbol.y > pos.y + 1) return false;
        //left
        if(symbol.x < pos.x - 1) return false;
        //right
        return symbol.x <= pos.x + (int)Math.log10(number.num) + 1;
    }

    static boolean isPartNumber(Number number, Set<Pos> symbols){
        return symbols.parallelStream().anyMatch(symbol -> isPartNumber(number, symbol));
    }

    static int sumPartNumbers(String resource) throws IOException {
        Schematic schematic = parse(resource);

        return schematic.numbers.stream()
                .filter(number -> isPartNumber(number, schematic.symbols()))
                .map(Number::num)
                .reduce(Integer::sum)
                .orElse(0);
    }

    public static void main(String[] args) {
        try {
            log.info("part1 sample " + sumPartNumbers("2023/03/sample")); // 4361
            log.info("part1 puzzle " + sumPartNumbers("2023/03/puzzle")); // 509115
        }
        catch(Exception e){
            log.log(Level.SEVERE, "main", e);
        }
    }
}
