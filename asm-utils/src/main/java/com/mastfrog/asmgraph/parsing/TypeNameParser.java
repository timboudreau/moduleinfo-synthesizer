package com.mastfrog.asmgraph.parsing;

import com.mastfrog.asmgraph.miniparser.MiniParser;
import com.mastfrog.asmgraph.miniparser.Sequence;

/**
 *
 * @author timb
 */
class TypeNameParser implements MiniParser<String> {
    
    @Override
    public String parse(Sequence text) {
        StringBuilder sb = new StringBuilder();
        boolean foundType = text.provisionally(() -> {
            boolean atStart = true;
            for (int ix = 0; ix < text.size(); ix++) {
                char c = text.curr();
                if (c == 0) {
                    break;
                }
                if (c == '/') {
                    sb.append(c);
                    atStart = true;
                    text.consume();
                    continue;
                }
                if (atStart && Character.isJavaIdentifierStart(c)) {
                    sb.append(c);
                    text.consume();
                } else if (!atStart && Character.isJavaIdentifierPart(c)) {
                    sb.append(c);
                    text.consume();
                } else {
                    text.consumeIf(';');
                    break;
                }
                atStart = false;
            }
            return sb.length() > 0;
        });
        return foundType ? sb.toString() : null;
    }
    
}
