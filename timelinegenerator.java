package brainfuck5dtt;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadPoolExecutor;

import static java.lang.Thread.State.TERMINATED;

class timelinegenerator extends Thread{
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
            executor.execute(main_timeline);
            alive_threads.add(main_timeline);
        }
        else {
            Thread created_thread = new Intepreter(expressions, instruction_pointer, mem_pointers, mem_array);
            executor.execute(created_thread);
            alive_threads.add(created_thread);
        }

    }

    static boolean allWaiting() {
        for (Thread thread: alive_threads) {
            if (thread.getState() != Thread.State.WAITING) {
                return false;
            }
        }
        return true;
    }
    /*
    synchronized void subprocess() {
        while (true) {
            remove_terminated();
            if (allWaiting()) {
                System.out.println(2);
                allWaiting.signalAll();
            }
            if (!main_timeline.isAlive()) {
                System.out.println("gay");
                executor.shutdownNow();
                break;
            }
            try {
                sleep(100);
            } catch (InterruptedException ignored) {}
        }
    }
     */

    static void remove_terminated() {
        threadIterator = alive_threads.iterator();
        while (true) {
            try {
                Thread current_thread = threadIterator.next();
                if (current_thread.getState() == TERMINATED) {
                    try {
                        sleep(5000);
                    } catch (InterruptedException ignored) {
                    }
                    alive_threads.remove(current_thread);
                }
            } catch (NoSuchElementException e) {
                break;
            }
        }
    }

    @Override
    public void run() {
        main_timeline = new Intepreter(expressions);
        Thread support = new Support();
        main_timeline.start();
        support.start();
        /*
        create_thread(true);
        while (true) {
            resumeAll();
            if (!main_timeline.isAlive()) {
                executor.shutdownNow();
                break;
            }
        }
        */
    }
}
