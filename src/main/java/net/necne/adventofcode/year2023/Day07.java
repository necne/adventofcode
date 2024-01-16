package net.necne.adventofcode.year2023;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.Resources;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.java.Log;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.stream.Collectors;

@Log
public class Day07 {

    enum Card implements Comparator<Card> {
        A,K,Q,J,T,_9,_8,_7,_6,_5,_4,_3,_2,;

        final Integer character;

        static final ImmutableMap<Integer, Card> label_cards = Arrays.stream(Card.values()).collect(ImmutableMap.toImmutableMap(
                i -> i.character,
                i -> i
        ));

        Card(){
            character = (int) name().replace("_", "").charAt(0);
        }

        public static Card of(int character){
            return label_cards.get(character);
        }

        @Override
        public int compare(Card card0, Card card1) {
            return Integer.compare(card0.ordinal(), card1.ordinal());
        }
    }

    enum Rank implements Comparator<Rank> {
        KIND_5, KIND_4, FULL_HOUSE, KIND_3, PAIR_2, PAIR_1, HIGH;

        public static Rank of(Hand hand){
            if(hand.cards.length != 5) throw new IllegalArgumentException("Unsupported hand size " + hand);

            var card_count = Arrays.stream(hand.cards)
                    .collect(Collectors.groupingBy(Function.identity()))
                    .entrySet()
                    .stream()
                    .sorted(Map.Entry.comparingByValue(Collections.reverseOrder(Comparator.comparingInt(List::size))))
                    .toList();

            return switch(card_count.size()){
                case 1 -> KIND_5;
                case 2 -> switch(card_count.get(0).getValue().size()) {
                    case 1, 4 -> KIND_4;
                    default -> FULL_HOUSE;
                };
                case 3 -> card_count.get(0).getValue().size() == 3 ? KIND_3 : PAIR_2;
                case 4 -> PAIR_1;
                case 5 -> HIGH;
                default -> throw new IllegalArgumentException("Unsupported card group count.");
            };
        }


        @Override
        public int compare(Rank rank0, Rank rank1) {
            return Integer.compare(rank0.ordinal(), rank1.ordinal());
        }
    }

    @Accessors(fluent = true)
    @Data
    @RequiredArgsConstructor(staticName = "of")
    static class Hand {
        final Card[] cards;
        final int bid;
    }

    static final Comparator<Hand> HAND_COMPARATOR = (hand0, hand1) -> Comparator
            .comparing(Rank::of)
            .thenComparing(Hand::cards, (cards0, cards1) -> {
                int minLength = Math.min(cards0.length, cards1.length);
                for(int i = 0; i < minLength; ++i) {
                    int cmp = cards0[i].compareTo(cards1[i]);
                    if(cmp != 0) return cmp;
                }
                return Integer.compare(cards0.length, cards1.length);
            })
            .reversed()
            .compare(hand0, hand1);

    static List<Hand> parseHands(String resource) throws IOException {
        return Arrays.stream(Resources.toString(Resources.getResource(resource), Charset.defaultCharset()).split("\n"))
                .map( line -> {
                    String[] parts = line.split(" ");
                    if (parts.length != 2) throw new IllegalArgumentException("Unexpected hand bid " + line);
                    return Hand.of(
                            parts[0].chars().boxed().map(Card::of).toArray(Card[]::new),
                            Integer.parseInt(parts[1])
                    );
                })
                .toList();
    }

    static int winnings(Collection<Hand> hands){
        final var ai = new AtomicInteger();
        return hands.stream()
                .sorted(HAND_COMPARATOR)
                .map(hand -> ai.addAndGet(1) * hand.bid)
                .reduce(Integer::sum)
                .orElse(0);
    }

    public static void main(String[] args) {
        try {
            var sampleHands = parseHands("2023/07/sample");
            log.info("sample hands " + sampleHands);
            log.info("sample winnings " + winnings(sampleHands)); //6440

            var puzzleHands = parseHands("2023/07/puzzle");
            log.info("puzzle winnings " + winnings(puzzleHands)); //251545216

        } catch (Exception e) {
            log.log(Level.SEVERE, "main", e);
        }
    }
}
