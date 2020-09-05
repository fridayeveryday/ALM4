import java.io.*;
import java.sql.Array;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Script {


    public static String path = "C:\\Temp\\ALM4.txt";
    public static Scanner scanner;
    private static Arithmetic arithmetic;

    ArrayList<String> lexemesType = new ArrayList<>(Arrays.asList("num", "str", "if", "else", "in", "out"));
    public static ArrayList<Lexeme> lexemes = new ArrayList<>();

    public static void main(String[] args) {
//        System.out.println(TypeLexemes.num);
        arithmetic = new Arithmetic();
        openFile(path);
        int numberOfLine = 1;
        while (scanner.hasNext()) {
            String oneLine = readOneLine(scanner);
            Lexeme currentLexeme = parseLine(oneLine);
            lexemes.add(currentLexeme);
            TypeLexemes typeCurrentLexeme = currentLexeme.type;
            if (typeCurrentLexeme == TypeLexemes.num) {
                if (!prepareNum(currentLexeme)) {
                    System.out.printf("Error at %d line", numberOfLine);
                    return;
                }
            } else if (typeCurrentLexeme == TypeLexemes.in) {
                if (!prepareInput(currentLexeme)) {
                    System.out.printf("Error at %d line", numberOfLine);
                    return;
                }
            } else if (typeCurrentLexeme == TypeLexemes.out) {
                prepareOutput(currentLexeme);
            }
            numberOfLine++;

        }
//        lexemes.add(parseLine(readOneLine(scanner)));
//        lexemes.add(parseLine(readOneLine(scanner)));
//        lexemes.add(parseLine(readOneLine(scanner)));
//        System.out.println(lexemes.get(0).content);
//
//        prepareNum(lexemes.get(0));
//        prepareNum(lexemes.get(1));
//        prepareNum(lexemes.get(2));
//        ArrayList<String> codeLines = new ArrayList<>();
//        while (scanner.hasNext()) {
////            String oneLine = readCode(scanner);
//
//            System.out.println(readCode(scanner));
//        }


    }

    public static void prepareOutput(Lexeme lexeme) {
        String lexemeContent = lexeme.content;
        if (lexemeContent.contains("\"")) {
            System.out.println(lexemeContent.replaceAll("\"", ""));
        } else if (lexemeContent.matches("[-]?\\d+[.,]?\\d*")) {
            System.out.println(lexemeContent);
        } else {
            prepareNum(lexeme);
            System.out.println(lexeme.content);
        }
    }

    public static boolean prepareInput(Lexeme lexeme) {
        if (lexeme.content.contains(" ")) {
            return false;
        }
        Scanner scanner = new Scanner(System.in);
        String value = scanner.nextLine();
        TypeLexemes tL;
        try {
            value = String.valueOf(Double.parseDouble(value));
            tL = TypeLexemes.num;
        } catch (Exception ignored) {
            tL = TypeLexemes.str;
        }
        for (int i = 0; i < lexemes.size(); i++) {
            if (lexemes.get(i).name.equals(lexeme.content)) {
                lexemes.set(i, new Lexeme(tL, lexeme.name, value));
                return true;
            }
        }
        lexemes.add(new Lexeme(tL, lexeme.name, value));
        return true;
    }
//
//    public static boolean prepareCondition(Lexeme lexeme) {
//
//    }

    public static boolean prepareNum(Lexeme lexeme) {
        Pattern p = Pattern.compile("[a-zA-Z]+");
        String candidate = lexeme.content;
        Matcher m = p.matcher(candidate);
        if (m.find()) {
            lexeme.content = substituteLexemes2Expression(lexeme);
        }
        p = Pattern.compile("[-+*/^]");
        candidate = lexeme.content;
        m = p.matcher(candidate);
        if (m.find()) {
            Double res = (Double) arithmetic.solveMathExpression(lexeme.content);
            if (res != null) {
                lexeme.content = res.toString();
                return true;
            } else {
                return false;
            }
        }

        return true;
    }

    public static String substituteLexemes2Expression(Lexeme lexeme) {
        ArrayList<String> elemOfContents = new ArrayList<>();
        StringBuilder operands = new StringBuilder();
        for (char ch : lexeme.content.toCharArray()) {
            if (arithmetic.operations.contains(Character.toString(ch))) {
                if (operands.length() != 0) {
                    elemOfContents.add(operands.toString());
                    operands.setLength(0);
                }
                elemOfContents.add(Character.toString(ch));
            } else {
                operands.append(ch);
            }
        }
        if (operands.length() != 0) {
            elemOfContents.add(operands.toString());
            operands.setLength(0);
        }
        for (int i = 0; i < lexemes.size() - 1; i++) {
            Lexeme elem = lexemes.get(i);
            for (int j = 0; j < elemOfContents.size(); j++) {
                String elemofCon = elemOfContents.get(j);
                if (elem.name.equals(elemofCon)) {
                    elemOfContents.set(j, elem.content);
                    break;
                }
            }

        }
        StringBuilder strContent = new StringBuilder();
        for (String str : elemOfContents) {
            strContent.append(str);
        }
        return strContent.toString();
    }

    public static Lexeme parseLine(String line) {
        line = line.replaceAll("^ +| +$|( )+", "$1");
        String beginPartOfStr = line.length() > 4 ? line.substring(0, 5) : line.substring(0, 3);
        TypeLexemes type;
        String name = "";
        String content = "";
        if (beginPartOfStr.contains("if ")) {
            type = TypeLexemes.conIf;
            name = "if";
            content = line.substring(3);
        } else if (beginPartOfStr.contains("els")) {
            type = TypeLexemes.conEl;
        } else if (beginPartOfStr.contains("in ")) {
            type = TypeLexemes.in;
            content = line.substring(3);
        } else if (beginPartOfStr.contains("out ")) {
            type = TypeLexemes.out;
            content = line.substring(4);
        } else {
//            int indexOfSignEqual = line.indexOf('=');
            if (line.contains("\"")) {
                type = TypeLexemes.str;
            } else {
                type = TypeLexemes.num;

            }
            name = line.substring(0, line.indexOf('=') - 1);
            content = line.substring(line.indexOf('=') + 2);
        }
        return new Lexeme(type, name, content);
    }

    public static boolean openFile(String path) {
        File file = new File(path);

        try {
            scanner = new Scanner(file);
            return true;
        } catch (FileNotFoundException | NoSuchElementException e) {
            System.out.println("File doesn't exist.");
            return false;
        }
    }

    public static String readOneLine(Scanner scanner) {
        String oneCodeLine = "";
        // считаем сначала первую строку
        oneCodeLine = scanner.nextLine();
        return oneCodeLine;
    }
}

class Lexeme {
    TypeLexemes type;
    String name;
    String content;

    public Lexeme(TypeLexemes type, String name, String content) {
        this.type = type;
        this.name = name;
        this.content = content;
    }
}

enum TypeLexemes {
    num, str, conIf, conEl, in, out
}









