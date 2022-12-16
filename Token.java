package brainfuck5dtt;

public class Token {
    final char text;
    final TokenType type;

    Token(TokenType type, char text) {
        this.type = type;
        this.text = text;
    }
}
