import java.io.*;
import java.sql.Array;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Script {


    public static String path = "C:\\Temp\\ALM4.txt";
    public static Scanner scanner;
    private static Arithmetic arithmetic;
    public static int currentLvlOfNesting = 0;
    public static boolean inCondition = false;

    public static ArrayList<Lexeme> lexemes = new ArrayList<>();

    public static void main(String[] args) {
        arithmetic = new Arithmetic();
        if (!openFile(path)){
            System.out.print("File is not exist.");
            return;
        }

        int numberOfLine = 1;
        while (scanner.hasNext()) {
            String oneLine = readOneLine(scanner);
            Lexeme currentLexeme = parseLine(oneLine);
            if (currentLexeme == null) {
                System.out.printf("Error at %d line", numberOfLine);
                return;
            }
            if (currentLexeme.lvlOfNesting != currentLvlOfNesting) {
                continue;
            }
            lexemes.add(currentLexeme);
            TypeLexemes typeCurrentLexeme = currentLexeme.type;
            if (typeCurrentLexeme == TypeLexemes.num) {
                if (!treatNum(currentLexeme)) {
                    System.out.printf("Error at %d line", numberOfLine);
                    return;
                }
            } else if (typeCurrentLexeme == TypeLexemes.in) {
                if (!treatInput(currentLexeme)) {
                    System.out.printf("Error at %d line", numberOfLine);
                    return;
                }
            } else if (typeCurrentLexeme == TypeLexemes.out) {
                if (!treatOutput(currentLexeme)) {
                    System.out.printf("Error at %d line", numberOfLine);
                    return;
                }
            } else if (typeCurrentLexeme == TypeLexemes.conIf) {
                Object resIf = treatIfCondition(currentLexeme);
                if (resIf == null) {
                    System.out.printf("Error at %d line", numberOfLine);
                    return;
                }
                currentLexeme.type = TypeLexemes.conIfFalse;
                if ((boolean) resIf) {
                    currentLvlOfNesting++;
                    inCondition = true;
                    currentLexeme.type = TypeLexemes.conIfTrue;
                }
            } else if (typeCurrentLexeme == TypeLexemes.conEl) {
                treatElse(currentLexeme);
            }
            numberOfLine++;

        }



    }

    public static void treatElse(Lexeme lexeme) {
        for (int i = lexemes.size() - 1; i >= 0; i--) {
            if (lexemes.get(i).lvlOfNesting == lexeme.lvlOfNesting
                    && lexemes.get(i).type == TypeLexemes.conIfFalse) {
                currentLvlOfNesting++;
                return;
            }
        }
    }

    public static Object treatIfCondition(Lexeme lexeme) {
        ArrayList<String> conditionParts = parseIfCondition(lexeme.content.trim());
        if (conditionParts == null) {
            return null;
        }
        Lexeme lex4LeftExpr = new Lexeme(lexeme.lvlOfNesting, lexeme.type, "leftCondition", conditionParts.get(0));
        boolean successCheckLeft = treatNum(lex4LeftExpr);
        if (!successCheckLeft) {
            return null;
        }
        Double leftRes = Double.parseDouble(lex4LeftExpr.content);
        Lexeme lex4RightExpr = new Lexeme(lexeme.lvlOfNesting, lexeme.type, "rightCondition", conditionParts.get(2));
        boolean successCheckRight = treatNum(lex4RightExpr);
        if (!successCheckRight) {
            return null;
        }
        Double rightRes = Double.parseDouble(lex4RightExpr.content);

        return switch (conditionParts.get(1)) {
            case ">" -> leftRes > rightRes;
            case "<" -> leftRes < rightRes;
            case ">=" -> leftRes >= rightRes;
            case "<=" -> leftRes <= rightRes;
            case "==" -> leftRes.equals(rightRes);
            case "!=" -> !leftRes.equals(rightRes);
            default -> null;
        };
    }

    public static ArrayList<String> conditions = new ArrayList<String>(Arrays.asList(">", "<", ">=", "<=", "==", "!="));
    public static ArrayList<String> charsOfConditions = new ArrayList<String>(Arrays.asList(">", "<", "=", "!"));

    public static ArrayList<String> parseIfCondition(String condition) {
        ArrayList<String> partsOfCondition = new ArrayList<>();
        StringBuilder partOfCondition = new StringBuilder();
        char[] conditionByChar = condition.toCharArray();
        char oneChar = conditionByChar[0];
        int i = 0;
        while (!charsOfConditions.contains(Character.toString(oneChar))) {
            partOfCondition.append(oneChar);
            i++;
            if (i > conditionByChar.length)
                return null;
            oneChar = conditionByChar[i];

        }
        partsOfCondition.add(partOfCondition.toString().trim());
        partOfCondition.setLength(0);
        //take a current condition char
        partOfCondition.append(oneChar);

        i++;
        oneChar = conditionByChar[i];
        if (charsOfConditions.contains(Character.toString(conditionByChar[i]))) {
            partOfCondition.append(oneChar);
            i++;
        }

        partsOfCondition.add(partOfCondition.toString().trim());
        partOfCondition.setLength(0);

        while (i < conditionByChar.length) {
            oneChar = conditionByChar[i];
            partOfCondition.append(oneChar);
            i++;

        }
        partsOfCondition.add(partOfCondition.toString().trim());
        return partsOfCondition;
    }

    public static boolean treatOutput(Lexeme lexeme) {
        String lexemeContent = lexeme.content;
        // if output a number
        if (lexemeContent.matches("[-]?\\d+[.,]?\\d*")) {
            System.out.println(lexemeContent);
        } else {
            treatNum(lexeme);
            lexemeContent = lexeme.content;
            if (lexeme.content.matches("[-]?\\d+[.,]?\\d*"))
                System.out.println(lexeme.content);
        }
        if (lexemeContent.contains("\"")) {
            String outputStr = checkIsCorrectString(lexemeContent);
            if (outputStr == null)
                return false;
            if (outputStr.contains("+")) {
                outputStr = outputStr.replaceAll("[+]", "");
            }
            System.out.println(outputStr.replaceAll("\"", ""));
        }
        return true;
    }

    public static boolean treatInput(Lexeme lexeme) {
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
            StringBuilder stringBuffer = new StringBuilder(value);
            stringBuffer.insert(0, '\"');
            stringBuffer.insert(stringBuffer.length(), '\"');
            value = stringBuffer.toString();
            tL = TypeLexemes.str;
        }
        for (int i = 0; i < lexemes.size(); i++) {
            if (lexemes.get(i).name.equals(lexeme.content)) {
                lexemes.set(i, new Lexeme(lexeme.lvlOfNesting, tL, lexemes.get(i).name, value));
                return true;
            }
        }
        lexemes.add(new Lexeme(lexeme.lvlOfNesting, tL, lexeme.content, value));
        return true;
    }

    public static boolean treatNum(Lexeme lexeme) {
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
        // parse expression into simple elements
        for (char ch : lexeme.content.toCharArray()) {
            if (arithmetic.operations.contains(Character.toString(ch))) {
                if (operands.length() != 0) {
                    elemOfContents.add(operands.toString().trim());
                    operands.setLength(0);
                }
                elemOfContents.add(Character.toString(ch).trim());
            } else {
                operands.append(ch);
            }
        }
        if (operands.length() != 0) {
            elemOfContents.add(operands.toString().trim());
            operands.setLength(0);
        }
        // substitute from global lexemes to local one
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
//        line = line.replaceAll("^ +| +$|( )+", "$1");
        int lineLengthBefore = line.length();
        line = line.replaceAll("\t", "");
        int lineLengthAfter = line.length();
        int lvlOfNesting = lineLengthBefore - lineLengthAfter;
        if (lvlOfNesting < currentLvlOfNesting) {
            currentLvlOfNesting = lvlOfNesting;
            inCondition = false;
        }
        if (line.length()<3)
            return null;
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
                content = checkIsCorrectString(line);
                if (content == null)
                    return null;
            } else {
                type = TypeLexemes.num;
                content = line.substring(line.indexOf('=') + 1).trim();
                if (content.contains(" "))
                    return null;
            }
            // name is chars between 0 and index of "=" - 1
            name = line.substring(0, line.indexOf('='));
            name = name.trim();

        }
        return new Lexeme(lvlOfNesting, type, name, content);
    }

    public static String checkIsCorrectString(String string) {
        int startQuotationMark = string.indexOf("\"", string.indexOf('=') + 1);
        int endQuotationMark = string.indexOf('"', startQuotationMark + 1);
        if (string.length() > endQuotationMark + 1)
            return null;
        else
            return string.substring(startQuotationMark, endQuotationMark + 1);
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
    int lvlOfNesting;
    TypeLexemes type;
    String name;
    String content;

    public Lexeme(int lvlOfNesting, TypeLexemes type, String name, String content) {
        this.lvlOfNesting = lvlOfNesting;
        this.type = type;
        this.name = name;
        this.content = content;
    }
}

enum TypeLexemes {
    num, str, conIf, conEl, in, out, conIfTrue, conIfFalse
}









