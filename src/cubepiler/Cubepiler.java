package cubepiler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Scanner;

import cubepiler.lexer.Lexer;
import cubepiler.lexer.SourceException;
import cubepiler.lexer.Token;
import cubepiler.syntaxchecker.SyntaxChecker;

public class Cubepiler {
    private static String sourceString;

    public static void main(String[] args) {
        Scanner s = new Scanner(System.in);
        String fileName;
        StringBuilder source = new StringBuilder();

        while (true) {
            System.out.print("Enter cube source code file : ");
            fileName = s.nextLine();

            if (!fileName.endsWith(".cube")) {
                System.out.println("Error: File is not a cube source code file!");
                continue;
            }

            try {
                File file = new File(fileName);
                
                if(file.isFile()) {
                    BufferedReader br = new BufferedReader(new FileReader(file));

                    String line;
                    while ((line = br.readLine()) != null) {
                        source.append(line + "\n");
                    }

                    sourceString = new String(source);
                    br.close();
                    break;
                } else {
                    System.out.println("Error: File does not exist!");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        s.close();
        
        Lexer lexer = new Lexer();
        
        try {
            System.out.println("Test Source: " + fileName);
            System.out.println("Tokens");
            int itemNumber = 0;
            for (Token token : lexer.getTokens(sourceString)) {
                System.out.println(String.format("%d) %s\t%s\tl:%d c:%d", itemNumber, token.getValue(), token.getType(), token.getStartingRow(), token.getStartingColumn()));
                itemNumber++;
            }
            System.out.println("Source String "+sourceString);
            SyntaxChecker sc = new SyntaxChecker(lexer.getTokens(sourceString));
            sc.start();
        } catch (SourceException se) {
            System.out.println(se.getMessage());
        }
    }
}
