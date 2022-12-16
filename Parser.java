package brainfuck5dtt;

import java.util.LinkedList;
import static brainfuck5dtt.TokenType.*;

public class Parser {
    final LinkedList<Token> tokens;
    private int current = 0;
    Expr expr = new Expr();

    Parser(LinkedList<Token> tokens) {
        this.tokens = tokens;
    }

    LinkedList<Expr> parse() {
        LinkedList<Expr> expressions = new LinkedList<>();
        while (tokens.get(current).type != EOF) {
            expressions.add(evaluate(tokens.get(current)));
            current++;
        }
        return expressions;
    }

    private Expr evaluate(Token token) {
        if (token.type == START_L) {
            current++;
            LinkedList<Expr> toloop = new LinkedList<>();
            try {
                while (tokens.get(current).type != END_L) {
                    toloop.add(evaluate(tokens.get(current)));
                    current++;
                }
            } catch (IndexOutOfBoundsException e) {
                Brainfuck.error("Unterminated loop caused by missing ].");
            }
            return expr.new loop(toloop);
        }
        return expr.new regular(token);
    }
}
