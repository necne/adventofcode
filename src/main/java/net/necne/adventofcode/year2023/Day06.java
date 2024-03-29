package net.necne.adventofcode.year2023;

import com.google.common.io.Resources;
import lombok.extern.java.Log;
import util.Timer;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.stream.LongStream;

@Log
public class Day06 {

    record Race(long time, long distance){
    }

    static Set<Race> parseRaces(String resource) throws IOException {
        String[] lines = Resources.toString(Resources.getResource(resource), Charset.defaultCharset()).split("\n");
        if(lines.length != 2) throw new IllegalArgumentException("Expected 2 lines and found " + lines.length);

        String[] times = lines[0].split(" +");
        String[] distances = lines[1].split(" +");
        if(times.length != distances.length) throw new IllegalArgumentException("Mismatches in time and distance counts.");

        var races = new HashSet<Race>();
        for(int i = 1; i < times.length; ++i){
            races.add(new Race(
                    Long.parseLong(times[i]),
                    Long.parseLong(distances[i])
            ));
        }
        return races;
    }

    static Race parseRace(String resource) throws IOException {
        String[] lines = Resources.toString(Resources.getResource(resource), Charset.defaultCharset())
                .replace(" ", "")
                .split("\n");
        if(lines.length != 2) throw new IllegalArgumentException("Expected 2 lines and found " + lines.length);

        return new Race(
                Long.parseLong(lines[0].split(":")[1]),
                Long.parseLong(lines[1].split(":")[1])
        );
    }


    static long raceDistance(long time, long hold){
        return hold * (time - hold);
    }

    static final String RACE_FORMAT = "\n  %3d  %4d";
    static void logRace(Race race) {
        StringBuilder sb = new StringBuilder();
        LongStream.rangeClosed(0, race.time).forEach(
                i -> sb.append(RACE_FORMAT.formatted(i, raceDistance(race.time, i)))
        );
        log.info("Race " + race + sb);
    }

    static long countWinnable(Race race){
        return LongStream.rangeClosed(0, race.time)
                .filter(hold -> race.distance < raceDistance(race.time, hold))
                .count();
    }

    static long multipleWinnable(Collection<Race> races){
        return races.stream()
                .map(Day06::countWinnable)
                .reduce(Math::multiplyExact)
                .orElse(0L);
    }

    public static void main(String[] args) {
        try {
            var timer = new Timer();
            var sampleRaces = parseRaces("2023/06/sample");
            var puzzleRaces = parseRaces("2023/06/puzzle");
            timer.split("load multiple");
            logRace(sampleRaces.stream().findFirst().orElseThrow());
            log.info("sample x winnable " + multipleWinnable(sampleRaces)); //288
            timer.split();
            log.info("puzzle x winnable " + multipleWinnable(puzzleRaces)); //1195150
            timer.split();

            var sampleRace = parseRace("2023/06/sample");
            var puzzleRace = parseRace("2023/06/puzzle");
            timer.split("load single");
            log.info("sample single winnable " + countWinnable(sampleRace)); //71503
            timer.split();
            log.info("puzzle single winnable " + countWinnable(puzzleRace)); //42550411
            timer.split();
        }
        catch(Exception e){
            log.log(Level.SEVERE, "main", e);
        }
    }
}
