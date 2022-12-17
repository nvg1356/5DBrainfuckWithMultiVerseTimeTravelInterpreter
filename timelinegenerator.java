package brainfuck5dtt;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadPoolExecutor;

class timelinegenerator {
    int[] mem_array;
    LinkedList<Integer> mem_pointers;
    LinkedList<Expr> expressions;
    LinkedList<Integer> instruction_pointer;
    static ThreadPoolExecutor executor;
    static ArrayList<Thread> alive_threads = new ArrayList<>();
    static Thread main_timeline;
    static Iterator<Thread> threadIterator;
    static Semaphore gate = new Semaphore(1);
    static int permits_needed = 0;

    timelinegenerator(int[] mem_array, LinkedList<Integer> mem_pointers, LinkedList<Expr> expressions, LinkedList<Integer> instruction_pointer) {
        this.mem_array = mem_array;
        this.mem_pointers = mem_pointers;
        this.expressions = expressions;
        this.instruction_pointer = instruction_pointer;
    }

    timelinegenerator(LinkedList<Expr> expressions) {
        executor = (ThreadPoolExecutor) Executors.newCachedThreadPool();
        this.expressions = expressions;
    }

    void create_thread(boolean first_thread) {
        if (first_thread) {
            main_timeline = new Intepreter(expressions);
            Thread support = new Support();
            executor.execute(main_timeline);
            support.start();
            alive_threads.add(main_timeline);
        }
        else {
            Thread created_thread = new Intepreter(expressions, instruction_pointer, mem_pointers, mem_array);
            executor.execute(created_thread);
            alive_threads.add(created_thread);
        }

    }
}
