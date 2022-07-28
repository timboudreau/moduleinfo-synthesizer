package com.mastfrog.asmgraph.parsing;

import com.mastfrog.asmgraph.miniparser.MiniParser;
import com.mastfrog.asmgraph.miniparser.Sequence;
import java.util.function.Supplier;

/**
 * Ensures we don't loop endlessly.  Our parsers can do that on garbage input,
 * so we simply handle that by bailing if needed.  Ordinarily we are consuming
 * signatures produced by Asm which will be valid.
 *
 * @author Tim Boudreau
 */
final class LoopLimiter {

    private static final int MAX_LOOPS = 150;
    private int loops;
    private final Sequence seq;
    private final Class<?> parserClass;
    private final Supplier<String> msg;
    private boolean broken;

    LoopLimiter(Sequence seq, MiniParser<?> parser, Supplier<String> msg) {
        this.seq = seq;
        parserClass = parser.getClass();
        this.msg = msg;
    }

    public void breakLoop() {
        broken = true;
    }

    public void onLoop() {
        loops++;
        if (loops >= MAX_LOOPS) {
            throw new AssertionError(parserClass.getSimpleName() + ": Too many loops. " + msg.get() + "\n" + seq);
        }
    }

    public void loop(Runnable run) {
        while (!seq.isAtEnd() && !broken) {
            try {
                run.run();
            } finally {
                onLoop();
            }
        }
    }
    
    public void loop2(Runnable run) {
        while (!seq.isDone() && !broken) {
            try {
                run.run();
            } finally {
                onLoop();
            }
        }
    }
    
}
