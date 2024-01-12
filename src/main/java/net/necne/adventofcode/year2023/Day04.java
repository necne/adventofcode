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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Log
public class Day04 {

    static final Pattern CARD_REGEX = Pattern.compile("Card +(\\d+): +((?: *\\d+)+) \\| +((?: *\\d+)+)");

    @Data
    @RequiredArgsConstructor(staticName = "of")
    @Accessors(fluent = true)
    static class Card {
        final int id;
        final Set<Integer> winners;
        final Set<Integer> candidates;

        long matches(){
            return candidates.stream().filter(winners::contains).count();
        }

        long points() {
            var matches = matches();
            return matches == 0L ? 0L : (long)Math.pow(2, matches-1);
        }
    }

    static Set<Integer> parseInts(String str) {
        return Arrays.stream(str.split(" +")).map(Integer::parseInt).collect(Collectors.toSet());
    }

    static List<Card> parseData(String resource) throws IOException {
        String data = Resources.toString(Resources.getResource(resource), Charset.defaultCharset());
        var cards = new ArrayList<Card>();

        Matcher m = CARD_REGEX.matcher(data);
        while(m.find()){
            cards.add(Card.of(
                    Integer.parseInt(m.group(1)),
                    parseInts(m.group(2)),
                    parseInts(m.group(3))
            ));
        }

        return cards;
    }

    static long sumPoints(List<Card> cards) {
        return cards.stream().map(Card::points)
                .reduce(Long::sum).orElse(0L);
    }

    static long countCards(List<Card> cards) {
        var count = 0L;
        // number of extra cards that are outstanding
        List<Long> dupesDue = new ArrayList<>();
        for(Card card : cards){
            var copies = 1 + dupesDue.size();
            count += copies;

            dupesDue = dupesDue.stream()
                    .map(i -> i -= 1)
                    .filter(i -> i > 0)
                    .collect(Collectors.toList());

            var matches = card.matches();
            if(matches > 0) {
                for (long i = 0; i < copies; ++i) dupesDue.add(matches);
            }
        }
        return count;
    }

    public static void main(String[] args) {
        try {
            var sampleCards = parseData("2023/04/sample");
            log.info("sample cards " + sampleCards);
            log.info("sample points " + sumPoints(sampleCards)); //13
            log.info("sample count " + countCards(sampleCards)); //30

            var puzzleCards = parseData("2023/04/puzzle");
            log.info("puzzle points " + sumPoints(puzzleCards)); //32001
            log.info("puzzle count " + countCards(puzzleCards)); //5037841
        }
        catch(Exception e){
            log.log(Level.SEVERE, "main", e);
        }
    }
}
