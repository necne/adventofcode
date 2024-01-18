package net.necne.adventofcode.year2023;

import com.google.common.io.Resources;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.java.Log;
import util.Timer;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Log
public class Day08 {
    final static Pattern NODE_PATTERN = Pattern.compile("(\\w+) = \\((\\w+), (\\w+)\\)");

    enum Direction {
        R,L,
    }

    @Data
    @Accessors(fluent = true)
    static
    class Node {
        final String id;
        final boolean end;

        Node nodeLeft;
        Node nodeRight;

        Node(String id) {
            this.id = id;
            end = id.endsWith("Z");
        }

        @Override
        public String toString() {
            return "Node{" +
                    "id=" + id +
                    ", end=" + end +
                    ", nodeLeft.id=" + nodeLeft.id +
                    ", nodeRight.id=" + nodeRight.id +
                    '}';
        }

        public Node follow(Direction dir) {
            return switch (dir){
                case L -> nodeLeft;
                case R -> nodeRight;
            };
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, nodeLeft.id, nodeRight.id);
        }
    }

    @Data
    @Accessors(fluent = true)
    @RequiredArgsConstructor(staticName = "of")
    static class Navigate {
        final List<Direction> directions;
        Map<String, Node> id_nodes = new HashMap<>();

        Iterator<Direction> loopingDirectionIterator(){
            return new Iterator<>() {
                Iterator<Direction> iterator;

                @Override
                public boolean hasNext() {
                    return true;
                }

                @Override
                public Direction next() {
                    if(iterator == null || !iterator.hasNext()) iterator = directions.iterator();
                    return iterator.next();
                }
            };
        }

        Node assertNode(String id){
            Node node = id_nodes.get(id);
            if(node == null) {
                node = new Node(id);
                id_nodes.put(id, node);
            }
            return node;
        }
    }

    static Navigate parseNavigate(String resource) throws IOException {
        String[] groups = Resources.toString(Resources.getResource(resource), Charset.defaultCharset()).split("\n\n");
        assert groups.length == 2;

        var navigate = Navigate.of(
                groups[0].chars().mapToObj(chr -> Direction.valueOf(String.valueOf((char)chr))).toList()
        );

        Matcher mNode = NODE_PATTERN.matcher(groups[1]);
        while(mNode.find()) {
            Node node = navigate.assertNode(mNode.group(1));
            node.nodeLeft(navigate.assertNode(mNode.group(2)));
            node.nodeRight(navigate.assertNode(mNode.group(3)));
        }
        return navigate;
    }

    static int follow(Navigate navigate){
        Node at = navigate.id_nodes.get("AAA");
        Iterator<Direction> it = navigate.loopingDirectionIterator();
        int steps = 0;
        while(!"ZZZ".equals(at.id)) {
            at = at.follow(it.next());
            ++steps;
        }
        return steps;
    }

    static int followAll(Navigate navigate) {
        var positions = navigate.id_nodes.keySet().stream()
                .filter(id -> id.endsWith("A"))
                .map(id -> navigate.id_nodes.get(id))
                .toArray(Node[]::new);

        Iterator<Direction> it = navigate.loopingDirectionIterator();
        int steps = 0;
        while(!Arrays.stream(positions).allMatch(Node::end)) {
            var dir = it.next();
            positions = Arrays.stream(positions)
                    .parallel()
                    .map(node -> node.follow(dir))
                    .toArray(Node[]::new);
            ++steps;

            if(steps % 1000000 == 0) log.info("step " + steps);
        }
        return steps;
    }

    static boolean isPrime(int number){
        return number > 1
                && IntStream.rangeClosed(2, (int)Math.sqrt(number))
                .noneMatch(n -> number % n == 0);
    }

    static boolean loopsBack(Node startNode, Iterator<Direction> iDirection, int maxSteps){
        Node node = startNode;
        for(int steps = 0; steps < maxSteps; ++steps) node = node.follow(iDirection.next());
        return startNode.equals(node);
    }

    // this abuses properties of the puzzle data, which are not indicated by the general problem as described
    static BigInteger followQuick(Navigate navigate) {
        final int directionsTotal = navigate.directions.size();
        log.info("directions count: " + navigate.directions.size());

        var start_current = navigate.id_nodes.keySet().stream()
                .filter(id -> id.endsWith("A"))
                .map(id -> navigate.id_nodes.get(id))
                .collect(Collectors.groupingBy(Function.identity(), Collectors.reducing(null, (a, b) -> b)));

        Iterator<Direction> it = navigate.loopingDirectionIterator();
        final var steps = new AtomicInteger(0);
        var lcmCandidates = new ArrayList<Integer>();
        while(!start_current.isEmpty()) {
            var dir = it.next();
            start_current.replaceAll((k, v) -> v.follow(dir));
            steps.addAndGet(1);

            start_current.entrySet().removeIf(entry -> {
                Node node = entry.getValue();
                if(node.end){
                    if(steps.get() % directionsTotal != 0) throw new IllegalArgumentException("Loop end not multiple of direction steps.");
                    if(!loopsBack(node, navigate.loopingDirectionIterator(), steps.get())) throw new IllegalArgumentException("Loop does not repeat.");

                    lcmCandidates.add(Math.floorDiv(steps.get(), directionsTotal));

                    log.info("Loop terminated after " + steps + " at " + node);
                    return true;
                }
                return false;
            });
        }
        //add in common factor of direction count we removed from each node end step value
        lcmCandidates.add(directionsTotal);

        //code is optimizing against specific properties of the puzzle, it doesn't happen to need further LCM breakdown
        if(lcmCandidates.stream().anyMatch(Predicate.not(Day08::isPrime))) throw new IllegalArgumentException("Loop counts are not prime, consider lcm breakdown");

        return lcmCandidates.stream()
                .map(BigInteger::valueOf)
                .reduce(BigInteger::multiply)
                .orElseThrow();
    }


    public static void main(String[] args) {
        try {
            var sampleNavigate = parseNavigate("2023/08/sample");
            log.info("sample navigate: " + sampleNavigate);
            log.info("sample follow steps: " + follow(sampleNavigate));

            var sample2Navigate = parseNavigate("2023/08/sample2");
            log.info("sample2 follow steps: " + follow(sample2Navigate));

            var puzzleNavigate = parseNavigate("2023/08/puzzle");
            log.info("puzzle follow steps: " + follow(puzzleNavigate)); // 16409

            var sample3Navigate = parseNavigate("2023/08/sample3");
            log.info("sample3 follow-all steps: "+ followAll(sample3Navigate));


            var timer = new Timer();
            try {
                log.info("puzzle quick follow-all steps: " + followQuick(puzzleNavigate)); //11795205644011
                timer.split("quick success");
            }
            catch(IllegalArgumentException e) {
                log.info("puzzle not compatible with quick method: " + e);
                timer.split("quick failure");

                log.info("puzzle follow-all steps: "+ followAll(puzzleNavigate));
                timer.split("complete success");
            }

        } catch (Exception e) {
            log.log(Level.SEVERE, "main", e);
        }
    }

}
