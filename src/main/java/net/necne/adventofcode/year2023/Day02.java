package net.necne.adventofcode.year2023;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.Resources;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.java.Log;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Log
public class Day02 {

    enum Color {
        RED, GREEN, BLUE, ;
    }

    @Data
    @Accessors(fluent = true)
    @RequiredArgsConstructor(staticName = "of")
    static class Game {
        final int id;
        Set<Handful> handfuls = new HashSet<>();
    }

    @Data
    @Builder
    @Accessors(fluent = true)
    static class Handful {
        Map<Color, Integer> color_counts;
    }

    static Pattern PATTERN_GAME = Pattern.compile("Game (\\d+): (.+)");
    static Pattern PATTERN_HANDFUL = Pattern.compile("(\\d+ \\w+(?:, )?)+;?");
    static Pattern PATTERN_COLOR = Pattern.compile("(\\d+) (\\w+)");

    static Set<Game> parse(String resource) throws IOException {
        Set<Game> games = new HashSet<>();
        String[] lines = Resources.toString(Resources.getResource(resource), Charset.defaultCharset()).split("\n");
        for(String line : lines){
            Matcher mGame = PATTERN_GAME.matcher(line);
            if(!mGame.find()) throw new IllegalArgumentException("No game found in line: " + line);

            Game game = Game.of(Integer.parseInt(mGame.group(1)));
            Matcher mHandful = PATTERN_HANDFUL.matcher(mGame.group(2));
            while(mHandful.find()){
                Handful handful = Handful.builder().color_counts(new HashMap<>()).build();
                Matcher mColor = PATTERN_COLOR.matcher(mHandful.group(0));
                while(mColor.find()) {
                    handful.color_counts().put(Color.valueOf(mColor.group(2).toUpperCase()), Integer.parseInt(mColor.group(1)));
                }
                game.handfuls().add(handful);
            }
            games.add(game);
        }
        log.info(games.toString());
        return games;
    }

    static boolean handfulPossible(Handful bag, Handful handful){
        for (Map.Entry<Color, Integer> entry : handful.color_counts().entrySet()) {
            Integer bagCount = bag.color_counts().get(entry.getKey());
            if (bagCount == null || bagCount < entry.getValue()) return false;
        }
        return true;
    }

    static boolean handfulPossible(Handful bag, Collection<Handful> handfuls){
        for(Handful handful : handfuls){
            if(!handfulPossible(bag, handful)) return false;
        }
        return true;
    }

    static void sumPossibleGameId(String resource) throws IOException {
        Handful bag = Handful.builder().color_counts(ImmutableMap.of(Color.RED, 12, Color.GREEN, 13, Color.BLUE, 14)).build();

        Set<Game> games = parse(resource);
        int sumId = 0;
        for(Game game : games){
            if(handfulPossible(bag, game.handfuls())){
                sumId += game.id();
            }
        }
        log.info("sum of ids " + sumId);
    }

    static int minimumGamePower(Game game){
        Map<Color, Integer> color_counts = new HashMap<>();
        for(Handful handful : game.handfuls()){
            for(Map.Entry<Color, Integer> color_count : handful.color_counts().entrySet()){
                Color color = color_count.getKey();
                if(!color_counts.containsKey(color)){
                    color_counts.put(color, color_count.getValue());
                }
                else {
                    color_counts.put(color, Math.max(color_counts.get(color), color_count.getValue()));
                }
            }
        }

        int power = 1;
        for(Map.Entry<Color, Integer> color_count : color_counts.entrySet()){
            power *= color_count.getValue();
        }
        return power;
    }

    static void sumMinimumGamePower(String resource) throws IOException {
        Set<Game> games = parse(resource);
        int sumPower = 0;
        for(Game game : games){
            sumPower += minimumGamePower(game);
        }
        log.info("power sum " + sumPower);
    }

    public static void main(String[] args) {
        try {
            sumPossibleGameId("2023/02/sample"); //8
            sumPossibleGameId("2023/02/puzzle"); //2771
            sumMinimumGamePower("2023/02/sample"); //2286
            sumMinimumGamePower("2023/02/puzzle"); //70924
        }
        catch(Exception e){
            log.log(Level.SEVERE, "main", e);
        }

    }
}
