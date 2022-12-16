package brainfuck5dtt;

import static brainfuck5dtt.timelinegenerator.*;

public class Support extends Thread{
    @Override
    public void run() {
        while (main_timeline.isAlive()) {
            if (permits_needed > 0) {
                gate.release(permits_needed);
            }
            else if (permits_needed < 0) {
                try {
                    gate.acquire(-permits_needed);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
