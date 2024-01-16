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
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.stream.Collectors;

@Log
public class Day07 {

    enum Card {
        A,K,Q,J{{wild = true;}},T,_9,_8,_7,_6,_5,_4,_3,_2,;

        final Integer character;
        boolean wild = false;

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

        public static int compareStraight(Card card0, Card card1) {
            return Integer.compare(card0.ordinal(), card1.ordinal());
        }

        public static int compareWildcard(Card card0, Card card1) {
            return Integer.compare(wildSort(card0), wildSort(card1));
        }

        static int wildSort(Card card){
            return card.wild ? Integer.MAX_VALUE : card.ordinal();
        }
    }

    enum Rank implements Comparator<Rank> {
        KIND_5, KIND_4, FULL_HOUSE, KIND_3, PAIR_2, PAIR_1, HIGH;

        public static Rank valueWithoutWildcards(Hand hand) {
            return of(hand, false);
        }

        public static Rank valueWithWildcards(Hand hand) {
            return of(hand, true);
        }

        public static Rank of(Hand hand, boolean wildcards){
            if(hand.cards.length != 5) throw new IllegalArgumentException("Unsupported hand size " + hand);

            var totalWildcards = new AtomicLong(0);
            var card_counts = Arrays.stream(hand.cards)
                    .filter(card -> {
                        if(wildcards && Card.J.equals(card)) {
                           totalWildcards.addAndGet(1);
                           return false;
                        }
                        return true;
                    })
                    .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                    .entrySet()
                    .stream()
                    .sorted(Map.Entry.comparingByValue(Collections.reverseOrder()))
                    .toList();

            if(totalWildcards.get() > 0) {
                if(card_counts.isEmpty()) return KIND_5;
                var card_count = card_counts.get(0);
                card_count.setValue(totalWildcards.get() + card_count.getValue());
            }

            return switch(card_counts.size()){
                case 1 -> KIND_5;
                case 2 -> switch(Math.toIntExact(card_counts.get(0).getValue())) {
                    case 1, 4 -> KIND_4;
                    default -> FULL_HOUSE;
                };
                case 3 -> card_counts.get(0).getValue() == 3 ? KIND_3 : PAIR_2;
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

    static final Comparator<Hand> HAND_STRAIGHT_COMPARATOR = (hand0, hand1) -> Comparator
            .comparing(Rank::valueWithoutWildcards)
            .thenComparing(Hand::cards, (cards0, cards1) -> {
                int minLength = Math.min(cards0.length, cards1.length);
                for(int i = 0; i < minLength; ++i) {
                    int cmp = Card.compareStraight(cards0[i], cards1[i]);
                    if(cmp != 0) return cmp;
                }
                return Integer.compare(cards0.length, cards1.length);
            })
            .reversed()
            .compare(hand0, hand1);

    static final Comparator<Hand> HAND_WILDCARD_COMPARATOR = (hand0, hand1) -> Comparator
            .comparing(Rank::valueWithWildcards)
            .thenComparing(Hand::cards, (cards0, cards1) -> {
                int minLength = Math.min(cards0.length, cards1.length);
                for(int i = 0; i < minLength; ++i) {
                    int cmp = Card.compareWildcard(cards0[i], cards1[i]);
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

    static int winnings(Collection<Hand> hands, Comparator<Hand> handComparator){
        final var ai = new AtomicInteger();
        return hands.stream()
                .sorted(handComparator)
                .map(hand -> ai.addAndGet(1) * hand.bid)
                .reduce(Integer::sum)
                .orElse(0);
    }

    public static void main(String[] args) {
        try {
            var sampleHands = parseHands("2023/07/sample");
            log.info("sample hands " + sampleHands);
            log.info("sample winnings straight " + winnings(sampleHands, HAND_STRAIGHT_COMPARATOR)); //6440
            log.info("sample winnings wildcard " + winnings(sampleHands, HAND_WILDCARD_COMPARATOR)); //5905

            var puzzleHands = parseHands("2023/07/puzzle");
            log.info("puzzle winnings straight " + winnings(puzzleHands, HAND_STRAIGHT_COMPARATOR)); //251545216
            log.info("puzzle winnings wildcard " + winnings(puzzleHands, HAND_WILDCARD_COMPARATOR)); //250384185

        } catch (Exception e) {
            log.log(Level.SEVERE, "main", e);
        }
    }
}
