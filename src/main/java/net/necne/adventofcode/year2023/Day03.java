package net.necne.adventofcode.year2023;

import com.google.common.io.Resources;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.java.Log;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
    @RequiredArgsConstructor(staticName = "of")
    static class Symbol {
        final Pos pos;
        final char symbol;
    }

    @Data
    @Accessors(fluent = true)
    @Builder
    static class Schematic {
        Set<Number> numbers;
        Set<Symbol> symbols;
    }

    static Schematic parse(String resource) throws IOException {
        Schematic.SchematicBuilder schematic = Schematic.builder().numbers(new HashSet<>()).symbols(new HashSet<>());
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
                        schematic.symbols.add(Symbol.of(Pos.of(x, y), line.charAt(0)));
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

    static boolean isAdjacent(Number number, Pos symbol){
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

    static boolean isPartNumber(Number number, Set<Symbol> symbols){
        return symbols.parallelStream().anyMatch(symbol -> isAdjacent(number, symbol.pos));
    }

    static int sumPartNumbers(Schematic schematic) {
        return schematic.numbers.stream()
                .filter(number -> isPartNumber(number, schematic.symbols()))
                .map(Number::num)
                .reduce(Integer::sum)
                .orElse(0);
    }

    static Set<Number> adjacentPartNumbers(Symbol symbol, Set<Number> numbers){
        return numbers.stream().filter(number -> isAdjacent(number, symbol.pos)).collect(Collectors.toSet());
    }

    static int sumGearRatio(Schematic schematic) {
        return schematic.symbols.parallelStream()
                    .filter(symbol -> symbol.symbol == '*')
                    .map(symbol -> adjacentPartNumbers(symbol, schematic.numbers))
                    .filter(adjacentPartNumbers -> adjacentPartNumbers.size() == 2)
                    .mapToInt(adjacentPartNumbers -> adjacentPartNumbers.stream().mapToInt(Number::num).reduce(1, (i0, i1) -> i0*i1)) //multiply pairs
                    .sum();
    }

    public static void main(String[] args) {
        try {
            Schematic sample = parse("2023/03/sample");
            Schematic puzzle = parse("2023/03/puzzle");
            log.info("part1 sample " + sumPartNumbers(sample)); // 4361
            log.info("part1 puzzle " + sumPartNumbers(puzzle)); // 509115
            log.info("part2 sample " + sumGearRatio(sample)); // 467835
            log.info("part2 puzzle " + sumGearRatio(puzzle)); // 75220503
        }
        catch(Exception e){
            log.log(Level.SEVERE, "main", e);
        }
    }
}
