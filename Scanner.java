package brainfuck5dtt;

import java.util.LinkedList;

import static brainfuck5dtt.TokenType.*;

public class Scanner {
    private int current = 0;
    private final String source;
    private LinkedList<Token> tokens = new LinkedList<>();

    Scanner(String source) {
        this.source = source;
    }

    LinkedList<Token> scanTokens() {
        while (!isAtEnd()) {
            scanToken();
            current++;
        }
        tokens.add(new Token(EOF, 'n'));
        return tokens;
    }

    private boolean isAtEnd() {
        return (current >= source.length());
    }

    private void scanToken() {
        char c = source.charAt(current);
        switch (c) {
            case '+' -> addToken(INC, c);
            case '-' -> addToken(DEC, c);
            case '>' -> addToken(RIGHT, c);
            case '<' -> addToken(LEFT, c);
            case '[' -> addToken(START_L, c);
            case ']' -> addToken(END_L, c);
            case ',' -> addToken(GET, c);
            case '.' -> addToken(PRINT, c);
            case '(' -> addToken(SPAWN, c);
            case ')' -> addToken(KILL, c);
            case '^' -> addToken(PASS_UP, c);
            case 'v' -> addToken(PASS_DOWN, c);
            case '~' -> addToken(REWIND, c);
            case '@' -> addToken(FREEZE, c);
            default -> Brainfuck.error("Unexpected character.");
        }
    }

    private void addToken(TokenType type, char character) {
        tokens.add(new Token(type, character));
    }
}
