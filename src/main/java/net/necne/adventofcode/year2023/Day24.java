package net.necne.adventofcode.year2023;

import com.google.common.io.Resources;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.java.Log;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Log
public class Day24 {
    static final Pattern HAILSTONE = Pattern.compile("(-?\\d+), +(-?\\d+), +(-?\\d+) @ +(-?\\d+), +(-?\\d+), +(-?\\d+)");
    static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_EVEN;
    static final int SCALE = 20;

    record Bounds(BigDecimal lower, BigDecimal upper) {}

    @Data
    @Accessors(fluent = true)
    @RequiredArgsConstructor(staticName = "of")
    static class Pos {
        final BigDecimal x;
        final BigDecimal y;
    }

    @Data
    @Builder
    static class Hailstone {
        BigInteger xStart;
        BigInteger yStart;

        BigInteger velocityX;
        BigInteger velocityY;

        // ax + by + c = 0
        BigDecimal lineAX;
        BigDecimal lineBY;
        BigDecimal lineC;

        static BigInteger newBI(int value){
            return new BigInteger(String.valueOf(value));
        }

        public static Hailstone of(int px, int py, int pz, int vx, int vy, int vz) {
            return of(newBI(px), newBI(py), newBI(pz), newBI(vx), newBI(vy), newBI(vz));
        }

        public static Hailstone of(BigInteger px, BigInteger py, BigInteger pz, BigInteger vx, BigInteger vy, BigInteger vz){
            // y = mx + b (m is slope)
            // modified to line format ax + by + c = 0
            var slope = new BigDecimal(vy).divide(new BigDecimal(vx), SCALE, ROUNDING_MODE);
            return Hailstone.builder()
                    .xStart(px)
                    .yStart(py)
                    .velocityX(vx)
                    .velocityY(vy)
                    .lineAX(slope.negate())
                    .lineBY(BigDecimal.ONE)
                    .lineC(new BigDecimal(py).negate().add(new BigDecimal(px).multiply(slope)))//-py+px*slope
                    .build();
        }
    }

    static Pos intersection(Hailstone h0, Hailstone h1) {
        // intersection of two standard lines in form ax + by + c = 0
        // (x, y) = ((b1*c2-b2*c1)/(a1*b2-a2*b1), (c1*a2-c2*a1)/(a1*b2-a2*b1))
        /*
        return Pos.of(
                (h0.lineBY*h1.lineC-h1.lineBY*h0.lineC)/(h0.lineAX*h1.lineBY-h1.lineAX*h0.lineBY),
                (h0.lineC*h1.lineAX-h1.lineC*h0.lineAX)/(h0.lineAX*h1.lineBY-h1.lineAX*h0.lineBY)
        );
         */
        var divisor = h0.lineAX.multiply(h1.lineBY).subtract(h1.lineAX.multiply(h0.lineBY));
        if (divisor.compareTo(BigDecimal.ZERO) == 0) return null;
        return Pos.of(
                (h0.lineBY.multiply(h1.lineC).subtract(h1.lineBY.multiply(h0.lineC))).divide(divisor, SCALE, ROUNDING_MODE),
                (h0.lineC.multiply(h1.lineAX).subtract(h1.lineC.multiply(h0.lineAX))).divide(divisor, SCALE, ROUNDING_MODE)
        );

    }

    static void poc(){
        Hailstone a = Hailstone.of(19, 13, 30, -2, 1, -2);
        Hailstone b = Hailstone.of(18, 19, 22, -1, -1, -2);

        log.info("line a " + a);
        log.info("line b " + b);

        log.info("intersection " + intersection(a, b));
    }

    static Set<Hailstone> parse(String resource) throws IOException {
        var hailstones = new HashSet<Hailstone>();
        String[] lines = Resources.toString(Resources.getResource(resource), Charset.defaultCharset()).split("\n");
        for(String line : lines){
            Matcher m = HAILSTONE.matcher(line);
            if(!m.find()) throw new IllegalArgumentException("Unrecognized hailstone: " + line);
            hailstones.add(Hailstone.of(
                    new BigInteger(m.group(1)), new BigInteger(m.group(2)), new BigInteger(m.group(3)),
                    new BigInteger(m.group(4)), new BigInteger(m.group(5)), new BigInteger(m.group(6))
                    ));

        }
        return hailstones;
    }

    static boolean isFuture(BigInteger start, BigInteger velocity, BigDecimal target){
        var dStart = new BigDecimal(start);
        if(velocity.equals(BigInteger.ZERO)) return dStart.compareTo(target) == 0;
        else if(velocity.compareTo(BigInteger.ZERO) < 0) return target.compareTo(dStart) < 0;
        else return dStart.compareTo(target) < 0;
    }

    static boolean isFuture(Hailstone hailstone, Pos intersect){
        if(intersect == null) return false;
        if(hailstone.velocityX.compareTo(BigInteger.ZERO) != 0) {
            return isFuture(hailstone.xStart, hailstone.velocityX, intersect.x);
        }
        else if(hailstone.velocityY.compareTo(BigInteger.ZERO) != 0) {
            return isFuture(hailstone.yStart, hailstone.velocityY, intersect.y);
        }
        return true;
    }

    static boolean isBounds(Pos pos, Bounds bounds){
        return pos != null &&
                bounds.lower.compareTo(pos.x)<0 && pos.x.compareTo(bounds.upper)<0 &&
                bounds.lower.compareTo(pos.y)<0 && pos.y.compareTo(bounds.upper)<0;
    }

    static int countIntersections(Collection<Hailstone> hailstoneCollection, Bounds bounds){
        var hailstones = hailstoneCollection.toArray(new Hailstone[0]);
        int intersections = 0;
        for(int i = 0; i < hailstones.length; ++i){
            for(int j = i+1; j < hailstones.length; ++j){
                var intersect = intersection(hailstones[i], hailstones[j]);
                log.info(String.format("""
                        
                        Hailstone %s
                        Hailstone %s
                        Intersection %s
                        FutureA %s
                        FutureB %s
                        Bounds %s
                        """, hailstones[i], hailstones[j], intersect, isFuture(hailstones[i], intersect), isFuture(hailstones[j], intersect), isBounds(intersect, bounds)));
                if(isBounds(intersect, bounds)
                        && isFuture(hailstones[i], intersect)
                        && isFuture(hailstones[j], intersect)) ++intersections;
            }
        }

        return intersections;
    }

    public static void main(String[] args) {
        try {
            poc();
            var sampleHailstones = parse("2023/24/sample");
            var puzzleHailstones = parse("2023/24/puzzle");
            var sampleBounds = new Bounds(new BigDecimal("7"), new BigDecimal("27"));
            var puzzleBounds = new Bounds(new BigDecimal("200000000000000"), new BigDecimal("400000000000000"));
            log.info("part1 sample " + countIntersections(sampleHailstones, sampleBounds));
            log.info("part1 puzzle " + countIntersections(puzzleHailstones, puzzleBounds)); // 16172

        }
        catch(Exception e){
            log.log(Level.SEVERE, "main", e);
        }
    }


}
