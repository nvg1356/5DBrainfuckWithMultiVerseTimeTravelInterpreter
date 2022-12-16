package brainfuck5dtt;

enum TokenType {
    START_L, // if all pointed cells have zero values, jump to matching end of loop. otherwise, do nothing. '['
    END_L, // if any pointed cells have non-zero values, jump to matching start of loop. otherwise, do nothing. ']'
    RIGHT, //shifts all memory pointers in the current timeline to the right by one cell '>'
    LEFT, //shifts all memory pointers in the current timeline to the left by one cell '<'
    INC, //increment values of all pointed cells in the current timeline '+'
    DEC, //decrement values of all pointed cells in the current timeline '-'
    GET, //gets character input from user and inserts ascii value of character into all pointed cells in current timeline ','
    PRINT, //prints the average value of all pointed cells
    SPAWN, //spawns a lower parallel timeline which has a perfect copy of the instruction and memory pointers of the current timeline.
            // the instruction pointer of the current timeline jumps to the next nearest ')' and the newly spawned timeline starts executing immediately. '('
    KILL, // if current timeline is not the main initial timeline, kill the timeline. otherwise, do nothing. ')'
    REWIND, //undo the previous operation without unwinding the instruction pointer. '~'
    PASS_UP, //after previous higher timeline has completed its instructions for current timestep, replace all of its memory pointers with counterparts from current timeline. '^'
    PASS_DOWN, //after previous higher timeline has completed its instructions for current timestep, replace all of its memory pointers with counterparts from current timeline. 'v'
    FREEZE, //pause execution of current timeline until the next lower timeline has zero memory pointers or is killed. '@'
    EOF //marks end of line 'n'
}
