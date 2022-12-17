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
                System.out.println("START_LOOP");
                Printer printer = new Printer(((Expr.loop) expression).toloop);
                printer.print();
                System.out.println("END_LOOP");
            }
        }
    }
}
