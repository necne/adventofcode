package net.necne.adventofcode.year2023;

import com.google.common.io.Resources;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.java.Log;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.stream.Collectors;

@Log
public class Day05 {

    @Data
    @RequiredArgsConstructor
    static class Almanac {
        Set<Long> seeds;
        Convert seed_soil;
        Convert soil_fertilizer;
        Convert fertilizer_water;
        Convert water_light;
        Convert light_temperature;
        Convert temperature_humidity;
        Convert humidity_location;

        final static String SEP = " -> ";

        public long mapSeedLocation(long seed){
            var value = seed;
            for(Convert convert : new Convert[]{seed_soil, soil_fertilizer, fertilizer_water, water_light, light_temperature, temperature_humidity, humidity_location}){
                value = convert.map(value);
            }
            return value;
        }

        public void mapSeedLocations(){
            seeds.forEach(this::mapSeedLocation);
        }

        public long minSeedLocation() {
            return seeds.stream()
                    .mapToLong(this::mapSeedLocation)
                    .min()
                    .orElseThrow();
        }

    }

    record OverrideRange(long fromKeyStart, long fromKeyEnd, long offset) {}

    static OverrideRange NO_OVERRIDE = new OverrideRange(Long.MIN_VALUE, Long.MAX_VALUE, 0);

    @Data
    @Accessors(fluent = true)
    static class Convert {
        List<OverrideRange> overrides = new ArrayList<>();

        long map(long key){
            return overrides.stream()
                    .filter(override -> override.fromKeyStart <= key && key < override.fromKeyEnd)
                    .findFirst()
                    .orElse(NO_OVERRIDE)
                    .offset + key;
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
                    almanac.seeds = Arrays.stream(data.split(" ")).map(Long::valueOf).collect(Collectors.toSet());
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

        return almanac;
    }

    private static Convert parseOverrides(String data) {
        var convert = new Convert();
        for(String line : data.split("\n")){
            long[] numbers = Arrays.stream(line.split(" ")).mapToLong(Long::valueOf).toArray();
            convert.overrides.add(new OverrideRange(
                    numbers[1],
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

            Almanac puzzleAlmanac = parseData("2023/05/puzzle");
            log.info("puzzle min seed location " + puzzleAlmanac.minSeedLocation()); //289863851
        }
        catch(Exception e){
            log.log(Level.SEVERE, "main", e);
        }
    }

}
