package net.necne.adventofcode.year2023;

import com.google.common.io.Resources;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.java.Log;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Log
public class Day08 {
    final static Pattern NODE_PATTERN = Pattern.compile("(\\w+) = \\((\\w+), (\\w+)\\)");

    enum Direction {
        R,L,;
    }

    @Data
    @Accessors(fluent = true)
    @RequiredArgsConstructor(staticName = "of")
    static
    class Node {
        final String id;

        Node nodeLeft;
        Node nodeRight;

        @Override
        public String toString() {
            return "Node{" +
                    "id=" + id +
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
    }

    @Data
    @Accessors(fluent = true)
    @RequiredArgsConstructor(staticName = "of")
    static class Navigate {
        final List<Direction> directions;
        Map<String, Node> id_nodes = new HashMap<>();

        Iterator<Direction> loopingDirectionIterator(){
            return new Iterator<Direction>() {
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
                node = Node.of(id);
                id_nodes.put(id, node);
            }
            return node;
        }
    }

    private static Navigate parseNavigate(String resource) throws IOException {
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

    public static void main(String[] args) {
        try {
            var sampleNavigate = parseNavigate("2023/08/sample");
            log.info("sample navigate: " + sampleNavigate);
            log.info("sample follow steps: " + follow(sampleNavigate));

            var sample2Navigate = parseNavigate("2023/08/sample2");
            log.info("sample2 follow steps: " + follow(sample2Navigate));

            var puzzleNavigate = parseNavigate("2023/08/puzzle");
            log.info("puzzle follow steps: " + follow(puzzleNavigate));

        } catch (Exception e) {
            log.log(Level.SEVERE, "main", e);
        }
    }

}
