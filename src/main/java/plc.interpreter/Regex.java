package plc.interpreter;

import java.util.regex.Pattern;

/**
 * Contains {@link Pattern} constants, which are compiled regular expressions.
 * See the assignment page for resources on regex's as needed.
 */
public class Regex {

    public static final Pattern
            EMAIL = Pattern.compile("[A-Za-z0-9._-]+@[A-Za-z0-9-]*\\.[a-z]{2,3}"),
            FILE_NAMES = Pattern.compile("^((.)+(\\..)?\\.(class|java))$"),
            EVEN_STRINGS = Pattern.compile("(.){20}|(.){18}|(.){16}|(.){14}|(.){12}|(.){10}"),
            INTEGER_LIST = Pattern.compile("\\[((,(\\s){0,1}|)[(0-9)]+|)+\\]"),
            IDENTIFIER = Pattern.compile("[a-zA-Z_*/:!?<>=+-][0-9a-zA-Z_.*/:!?<>=+-]*"),
            NUMBER = Pattern.compile("^(\\+|-)?(\\d+\\.)?\\d+$"),
            STRING = Pattern.compile("^(\")([^'\"\\\\]*(\\\\[bnrt'\"\\\\])*)*(\")$");

    //https://www.oreilly.com/library/view/java-cookbook-3rd/9781449338794/ch04.html

}
