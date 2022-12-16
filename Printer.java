package brainfuck5dtt;

import java.util.LinkedList;

public class Printer {
    private LinkedList<Expr> expressions;
    Printer(LinkedList<Expr> expressions) {this.expressions = expressions;}
    void print() {
        for (Expr expression : expressions) {
            if (expression instanceof Expr.regular) {
                System.out.println(((Expr.regular) expression).token.type);
            } else {
                System.out.println("LOOP");
            }
        }
    }
}
