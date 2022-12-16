package brainfuck5dtt;

import java.util.LinkedList;

class Expr {

    class regular extends Expr {
        final Token token;
        regular(Token token) {
            this.token = token;
        }
    }

    class loop extends Expr {
        final LinkedList<Expr> toloop;
        loop(LinkedList<Expr> toloop) {
            this.toloop = toloop;
        }
    }
}
