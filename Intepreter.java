package brainfuck5dtt;

import java.lang.reflect.Array;
import java.util.*;

import static brainfuck5dtt.TokenType.*;
import static brainfuck5dtt.timelinegenerator.*;

public class Intepreter extends Thread{ //each Interpreter instance is a thread representing a timeline
    int[] mem_array = new int[3000]; //3000 is a size chosen arbitrarily
    LinkedList<Integer> mem_pointers = new LinkedList<>();
    LinkedList<Expr> expressions;
    LinkedList<Integer> instruction_pointer = new LinkedList<>();
    int self_pos = alive_threads.indexOf(Thread.currentThread());

    Intepreter(LinkedList<Expr> expressions) {
        instruction_pointer.addLast(0);
        mem_pointers.add(0);
        this.expressions = expressions;
    }

    Intepreter(LinkedList<Expr> expressions, LinkedList<Integer> instruction_pointer, LinkedList<Integer> mem_pointers, int[] mem_array) {
        this.expressions = expressions;
        this.instruction_pointer = instruction_pointer;
        this.mem_pointers = mem_pointers;
        this.mem_array = mem_array;
    }

    @Override
    public void run(){
        while (true) {
            try {
                evaluate(expressions.get(instruction_pointer.getFirst()));
                instruction_pointer.set(0, instruction_pointer.getFirst() + 1);
            } catch (IndexOutOfBoundsException e) {
                System.out.println("interpretation ended");
                return;
            }
        }
    }

    private void evaluate(Expr expr) {
        try {
            if (expr instanceof Expr.regular) {
                switch (((Expr.regular) expr).token.type) {
                    case RIGHT -> right();
                    case LEFT -> left();
                    case INC -> inc();
                    case DEC -> dec();
                    case PRINT -> print();
                    case GET -> get(expr);
                    case REWIND -> rewind();
                    case SPAWN -> spawn();
                    case KILL -> kill_self();
                    case PASS_UP -> pass_up();
                    case PASS_DOWN -> pass_down();
                    case FREEZE -> freeze();
                }
            }
            else {
                loop(expr);
            }
            hold();
        } catch (InterruptedException e) {
            kill_self();
        }

    }

    private void hold() throws InterruptedException {
        gate.acquire();
        while ((gate.availablePermits() > 0) || (gate.hasQueuedThreads())) {
            sleep(100);
        }
        gate.release();
    }

    //basic methods
    private void loop(Expr expr) throws InterruptedException{
        LinkedList<Expr> toloop = ((Expr.loop) expr).toloop;
        instruction_pointer.addLast(0);
        if (checkAllZero()) {
            instruction_pointer.removeLast();
            return;
        }
        for (int i = 0; i < toloop.size(); i++) {
            try {
                if (((Expr.regular) toloop.get(i)).token.type == REWIND) {
                    rewind(toloop, (i -1), 0);
                }
                else {
                    evaluate(toloop.get(i));
                }
            } catch (ClassCastException e) { //loop within loop
                evaluate(toloop.get(i));
            }
            instruction_pointer.set(instruction_pointer.size() - 1, i);
        }
        if (!checkAllZero()) {
            instruction_pointer.removeLast();
            loop(expr);
        }
    }

    private void print() {
        System.out.println(Array.get(mem_array, mem_pointers.getLast()));
    }

    private void right() {
        mem_pointers.replaceAll(integer -> integer + 1);
    }

    private void left() {
        mem_pointers.replaceAll(integer -> integer + 1);
    }

    private void inc() {
        mem_pointers.forEach((n) -> Array.set(mem_array, n, (int) Array.get(mem_array, n) + 1));
    }

    private void dec() {
        mem_pointers.forEach((n) -> Array.set(mem_array, n, (int) Array.get(mem_array, n) - 1));
    }

    private void get(Expr expr) {
        mem_pointers.forEach((n) -> Array.set(mem_array, n, (int) ((Expr.regular) expr).token.text));
    }

    //

    //timeline methods

    private void spawn() {
        timelinegenerator generate = new timelinegenerator(mem_array, mem_pointers, expressions, instruction_pointer);
        generate.create_thread(false);
        permits_needed++;
        try {
            spawn_skip();
        } catch (RuntimeException e){
            Brainfuck.error("Could not find ) to jump to after generating new timeline.");
        }
    }

    private void spawn_skip() {
        LinkedList<Integer> last_known_kill = new LinkedList<>(); // stores location of nearest )
        LinkedList<Expr> current_level = expressions;
        //check through each traversed level
        for (int j = 0; j < instruction_pointer.size(); j++) {
            if (j != 0) {
                current_level = ((Expr.loop) current_level.get(j)).toloop;
            }
            // check through all elements in current level
            for (int i = instruction_pointer.get(j); i < current_level.size(); i++) {
                // conditions to check if expression is a )
                try {
                    if (((Expr.regular) current_level.get(i)).token.type == KILL) {
                        // update location of nearest )
                        last_known_kill.clear();
                        for (int k = 0; k < j - 1; k++) {
                            last_known_kill.addLast(instruction_pointer.get(k));
                        }
                        last_known_kill.addLast(i);
                        break;
                    }
                } catch (ClassCastException ignored) {}
            }
        }
        // throw error if no ) to jump to
        if (last_known_kill.isEmpty()) {
            throw new RuntimeException();
        }
        // jump to next nearest )
        else {
            instruction_pointer = last_known_kill;
        }
    }

    private void kill_spawned_timeline() {
        alive_threads.get(self_pos + 1).interrupt();
    }

    private void kill_self() { //for now, kill_self cannot be rewound
        permits_needed = permits_needed - 1;
        Thread.currentThread().interrupt();
    }

    private void pass_up() throws InterruptedException { //cannot be rewound
        while (alive_threads.get(self_pos - 1).getState() != State.WAITING) {
            sleep(500); //check every half second if previous higher timeline has finished execution for current timestep
        }
        ((Intepreter) alive_threads.get(self_pos - 1)).setMem_pointers(this.mem_pointers);
    }

    private void pass_down() throws InterruptedException { //cannot be rewound
        while (alive_threads.get(self_pos + 1).getState() != State.WAITING) {
            sleep(500); //check every half second if next lower timeline has finished execution for current timestep
        }
        ((Intepreter) alive_threads.get(self_pos + 1)).setMem_pointers(this.mem_pointers);
    }

    private synchronized void freeze() throws InterruptedException { //cannot be rewound
        while (alive_threads.get(self_pos + 1).getState() == State.RUNNABLE) {
            sleep(200);
        }
        if (alive_threads.get(self_pos + 1).getState() != State.TERMINATED) {
            hold();
            freeze();
        }
    }

    private void rewind() { //use when not in loop
        rewind(expressions, instruction_pointer.getFirst() - 1, 0);
    }

    private boolean rewind (LinkedList<Expr> expressions, int prev_pos, int acc_rewind) { //use when in loop
        if (acc_rewind == 0) { acc_rewind++;}
        try {
            Expr prev_expression = expressions.get(prev_pos);
            if (prev_expression instanceof Expr.regular) {
                TokenType prev_expression_type = ((Expr.regular) prev_expression).token.type;
                if (prev_expression_type == REWIND) {
                    acc_rewind++;
                    rewind(expressions, prev_pos - 1, acc_rewind);
                }
                else if (acc_rewind > 1) { rewind(expressions, prev_pos - (acc_rewind - 1), 0); }
                else {
                    switch (prev_expression_type) {
                        case RIGHT: left();
                        case LEFT: right();
                        case INC: dec();
                        case DEC: inc();
                        case SPAWN: kill_spawned_timeline();
                    }
                    return true;
                }
            }
            else {
                LinkedList<Expr> loop = ((Expr.loop) prev_expression).toloop;
                if (rewind(loop, loop.size() - 1, acc_rewind)) { return true; }
                else { rewind(expressions, prev_pos - 2, acc_rewind); }
            }
        } catch (IndexOutOfBoundsException e) { return false; }
        throw new AssertionError("This will not get reached."); //filler to prevent compilation error
    }

    //

    //custom locks

    //

    void setMem_pointers(LinkedList<Integer> mem_pointers) {
        this.mem_pointers = mem_pointers;
    }

    private LinkedList<Integer> getvalues() {
        LinkedList<Integer> values = new LinkedList<>();
        mem_pointers.forEach((n) -> values.add((Integer) Array.get(mem_array, n)));
        return values;
    }

    private boolean checkAllZero() {
        Iterator<Integer> iterator = getvalues().iterator();
        while (true) {
            try {
                if (iterator.next() != 0) {
                    return false;
                }
            } catch (NoSuchElementException e) {
                return true;
            }
        }
    }
}
