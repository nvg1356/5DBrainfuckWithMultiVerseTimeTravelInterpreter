package brainfuck5dtt;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;

public class Brainfuck {
    static boolean hadError = false;

    public static void main(String[] args) throws IOException {
        if (args.length > 1) {
            System.out.println("Usage: Brainfuck.java [path]");
            System.exit(64);
        }
        else if (args.length == 1) {
            runFile(args[0]);
        }
        else {
            runPrompt();
        }
    }

    public static void runFile(String path) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        run(new String(bytes, Charset.defaultCharset()));
        if (hadError) {
            System.exit(65);
        }
    }

    public static void runPrompt() throws IOException {
        InputStreamReader input  =new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);
        while (true) {
            System.out.println("> ");
            String line = reader.readLine();
            if (line == null) {
                break;
            }
            run(line);
            hadError = false;
        }
    }

    public static void run(String source) {
        Scanner scanner = new Scanner(source);
        LinkedList<Token> tokens = scanner.scanTokens();
        Parser parser = new Parser(tokens);
        LinkedList<Expr> expressions = parser.parse();
        timelinegenerator generator = new timelinegenerator(expressions);
        generator.start();
    }

    static void error(String message) {
        System.err.println("Error: " + message);
        hadError = true;
    }
}
