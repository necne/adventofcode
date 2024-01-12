package net.necne.adventofcode.year2023;

import com.google.common.io.Resources;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.java.Log;
import util.Timer;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.stream.LongStream;

@Log
public class Day05 {

    @Data
    @RequiredArgsConstructor
    static class Almanac {
        List<Long> seeds;
        Convert seed_soil;
        Convert soil_fertilizer;
        Convert fertilizer_water;
        Convert water_light;
        Convert light_temperature;
        Convert temperature_humidity;
        Convert humidity_location;

        Convert[] seedLocationPath;

        final static String SEP = " -> ";

        public void init(){
            seedLocationPath = new Convert[]{seed_soil, soil_fertilizer, fertilizer_water, water_light, light_temperature, temperature_humidity, humidity_location};
        }

        public long mapSeedLocation(long seed){
            var value = seed;
            for(Convert convert : seedLocationPath){
                value = convert.map(value);
            }
            return value;
        }

        public void mapSeedLocations(){
            seeds.forEach(this::mapSeedLocation);
        }

        public long minSeedLocation() {
            return seeds.parallelStream()
                    .mapToLong(this::mapSeedLocation)
                    .min()
                    .orElseThrow();
        }

        public long minSeedRangeLocation() {
            long minLocation = Long.MAX_VALUE;
            for(int i = 0; i <seeds.size(); i += 2){
                log.info("at seed start " + seeds.get(i));
                minLocation = Math.min(
                        minLocation,
                        LongStream.range(seeds.get(i), seeds.get(i) + seeds.get(i + 1))
                                .parallel()
                                .map(this::mapSeedLocation)
                                .min().orElse(Long.MAX_VALUE)
                );
            }

            return minLocation;
        }
    }

    record OverrideRange(long fromKeyStart, long fromKeyEnd, long offset) {}

    @Data
    @Accessors(fluent = true)
    static class Convert {
        TreeMap<Long, OverrideRange> overrides = new TreeMap<>();

        long map(long key){
            var entry = overrides().floorEntry(key);
            if(entry != null){
                var override = entry.getValue();
                if(override.fromKeyStart <= key && key < override.fromKeyEnd) return override.offset + key;
            }
            return key;
        }
    }


    static Almanac parseData(String resource) throws IOException {
        Almanac almanac = new Almanac();
        String[] blocks = Resources.toString(Resources.getResource(resource), Charset.defaultCharset()).split("\n\n");
        for(String block : blocks) {
            String[] blockGroups = block.split(":");
            String data = blockGroups[1].substring(1);
            switch (blockGroups[0]){
                case "seeds":
                    almanac.seeds = Arrays.stream(data.split(" ")).map(Long::valueOf).toList();
                    break;
                case "seed-to-soil map":
                    almanac.seed_soil = parseOverrides(data);
                    break;
                case "soil-to-fertilizer map":
                    almanac.soil_fertilizer = parseOverrides(data);
                    break;
                case "fertilizer-to-water map":
                    almanac.fertilizer_water = parseOverrides(data);
                    break;
                case "water-to-light map":
                    almanac.water_light = parseOverrides(data);
                    break;
                case "light-to-temperature map":
                    almanac.light_temperature = parseOverrides(data);
                    break;
                case "temperature-to-humidity map":
                    almanac.temperature_humidity = parseOverrides(data);
                    break;
                case "humidity-to-location map":
                    almanac.humidity_location = parseOverrides(data);
                    break;
                default:
                    throw new IllegalArgumentException("Unrecognized data block: " + blockGroups[0]);
            }
        }
        almanac.init();

        return almanac;
    }

    private static Convert parseOverrides(String data) {
        var convert = new Convert();
        for(String line : data.split("\n")){
            var numbers = Arrays.stream(line.split(" ")).mapToLong(Long::valueOf).toArray();
            var fromKeyStart = numbers[1];
            convert.overrides.put(fromKeyStart, new OverrideRange(
                    fromKeyStart,
                    numbers[1] + numbers[2],
                    numbers[0] - numbers[1]
            ));
        }
        return convert;
    }

    private static void printRange(long start, long end, Convert convert){
        String format = String.format("\n  %%%dd %%d", (int)Math.log10(end) + 1);
        StringBuilder sb = new StringBuilder();
        for(long i = start; i < end; ++i) {
            sb.append(String.format(format, i, convert.map(i)));
        }
        System.out.println(sb);
    }

    public static void main(String[] args) {
        try {
            Almanac sampleAlmanac = parseData("2023/05/sample");
            log.info("sample data " + sampleAlmanac);
            sampleAlmanac.mapSeedLocations();
            log.info("sample min seed location " + sampleAlmanac.minSeedLocation()); //35
            log.info("sample min seed-range location " + sampleAlmanac.minSeedRangeLocation()); //46

            Almanac puzzleAlmanac = parseData("2023/05/puzzle");
            log.info("puzzle min seed location " + puzzleAlmanac.minSeedLocation()); //289863851
            var timer = new Timer();
            log.info("puzzle min seed-range location " + puzzleAlmanac.minSeedRangeLocation()); //60568880 34347ms
            timer.stop();

        }
        catch(Exception e){
            log.log(Level.SEVERE, "main", e);
        }
    }

}
