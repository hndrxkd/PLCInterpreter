package plc.interpreter;

import com.sun.org.apache.xpath.internal.Arg;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Contains JUnit tests for {@link Regex}. Tests declarations for steps 1 & 2
 * are provided, you must add your own for step 3.
 *
 * To run tests, either click the run icon on the left margin or execute the
 * gradle test task, which can be done by clicking the Gradle tab in the right
 * sidebar and navigating to Tasks > verification > test Regex(double click to run).
 */
public class RegexTests {

    /**
     * This is a parameterized test for the {@link Regex#EMAIL} regex. The
     * {@link ParameterizedTest} annotation defines this method as a
     * parameterized test, and {@link MethodSource} tells JUnit to look for the
     * static method {@link #testEmailRegex()}.
     *
     * For personal preference, I include a test name as the first parameter
     * which describes what that test should be testing - this is visible in
     * IntelliJ when running the tests.
     */
    @ParameterizedTest
    @MethodSource
    public void testEmailRegex(String test, String input, boolean success) {
        test(input, Regex.EMAIL, success);
    }

    /**
     * This is the factory method providing test cases for the parameterized
     * test above - note that it is static, takes no arguments, and has the same
     * name as the test. The {@link Arguments} object contains the arguments for
     * each test to be passed to the function above
     */
    public static Stream<Arguments> testEmailRegex() {
        return Stream.of(
                Arguments.of("Alphanumeric", "thelegend27@gmail.com", true),
                Arguments.of("UF Domain", "otherdomain@ufl.edu", true),
                Arguments.of("Missing Domain Dot", "missingdot@gmailcom", false),
                Arguments.of("Symbols", "symbols#$%@gmail.com", false),
                Arguments.of("Domain Only", "@hotmail.com", false),
                Arguments.of("Dot user only" , ".@outlook.com" , true),
                Arguments.of("Different TLD", "somoa@gmail.ws" ,true),
                Arguments.of("Multiple Domain", "student@ufl.cise.edu", false),
                Arguments.of("Symbol User", "\'Test User\'@yahoo.com", false),
                Arguments.of("Underscore and Hyphen", "-_-_-_-@-------.com", true)
        );
    }

    @ParameterizedTest
    @MethodSource
    public void testFileNamesRegex(String test, String input, boolean success) {
        //this one is different as we're also testing the file name capture
        Matcher matcher = test(input, Regex.FILE_NAMES, success);
        if (success) {
            Assertions.assertEquals(input.substring(0, input.indexOf(".")), matcher.group("name"));
        }
    }

    public static Stream<Arguments> testFileNamesRegex() {
        return Stream.of(
                Arguments.of("Java File", "Regex.tar.java", true),
                Arguments.of("Java Class", "RegexTests.class", true),
                Arguments.of("Directory", "directory", false),
                Arguments.of("Python File", "scrippy.py", false),
                Arguments.of("Illegal Symbols", "<>|?\"\"\\*.java", false),
                Arguments.of("Multiple extensions", "testClass.zip.tar.class" , true),
                Arguments.of("No file name", ".java", false),
                Arguments.of("123 Name", "123.class" , true),
                Arguments.of("Multiple Periods" , "toomanyperiods.test..java", false),
                Arguments.of("Lots of extensions" , "test.tar.gz.zip.swift.jpg.class", true)
        );
    }

    @ParameterizedTest
    @MethodSource
    public void testEvenStringsRegex(String test, String input, boolean success) {
        test(input, Regex.EVEN_STRINGS, success);
    }

    public static Stream<Arguments> testEvenStringsRegex() {
        return Stream.of(
                Arguments.of("14 Characters", "thishas14chars", true),
                Arguments.of("12 Characters", "i<3pancakes!", true),
                Arguments.of("6 Characters", "6chars", false),
                Arguments.of("13 Characters", "i<3pancakes!!", false),
                Arguments.of("5 symbols only", "\\/':*", false),
                Arguments.of("12 Symbols only", "!@#$%^&*()_+", true),
                Arguments.of("20 Characters", "12345678901234567890", true),
                Arguments.of("[1,2,3,4,5,6,7,8,]", "[1,2,3,4,5,6,7,8,]", true),
                Arguments.of("21 Characters", "thisis21characterlong", false),
                Arguments.of("13 characters including space" , "i<3pancakes! ", false)
        );
    }

    @ParameterizedTest
    @MethodSource
    public void testIntegerListRegex(String test, String input, boolean success) {
        test(input, Regex.INTEGER_LIST, success);
    }

    public static Stream<Arguments> testIntegerListRegex() {
        return Stream.of(
                Arguments.of("Empty List", "[]", true),
                Arguments.of("Single Element", "[1]", true),
                Arguments.of("Multiple Elements", "[1,2,3]", true),
                Arguments.of("Missing Brackets", "1,2,3", false),
                Arguments.of("Missing Commas", "[1 2 3]", false),
                Arguments.of("Trailing Comma", "[1,2,3,]", false),
                Arguments.of("Spaces", "[ 1 , 2 , 3 ]", false),
                Arguments.of("One bracket missing", " [1,2,3", false),
                Arguments.of("More than a single digit", "[1,222,3333333,124124]", true),
                Arguments.of("Just a bracket", "[", false),
                Arguments.of("Single spcace after the comma", "[1, 32, 123]" , true),
                Arguments.of("Single space before the comma", "[1 ,23 ,2234 ,123]", false)
        );
    }

    @ParameterizedTest
    @MethodSource
    public void testIdentifierRegex(String test, String input, boolean success) {
        test(input, Regex.IDENTIFIER, success);
    }

    public static Stream<Arguments> testIdentifierRegex() {
        return  Stream.of(
                Arguments.of("Alphanumeric", "getName5", true),
                Arguments.of("Underscores", "_myvariable3_", true),
                Arguments.of("Special Characters", "is-empty?", true),
                Arguments.of("Begins with Digit", "42=life", false),
                Arguments.of("Period", ".", false),
                Arguments.of("Nonspecial Characters", "why,are,there,commas", false),
                Arguments.of("Assigning function result" , "number=function.max" , true),
                Arguments.of("Begins with comma", ",=test", false),
                Arguments.of("symbols" , "!@#$%", false),
                Arguments.of("my.variable.", "my.variable", true)

        );
    }

    @ParameterizedTest
    @MethodSource
    public void testNumberRegex(String test, String input, boolean success) {
        test(input, Regex.NUMBER, success);
    }

    public static Stream<Arguments> testNumberRegex() {
        return  Stream.of(
                Arguments.of("Long Decimal", "123423.2322342334324", true),
                Arguments.of("+ sign", "+5.123", true),
                Arguments.of("- Sign", "-123" , true),
                Arguments.of("Single digit + ", "+5", true),
                Arguments.of("Single digit -","-23", true),
                Arguments.of("No Sign One digit", "43", true),
                Arguments.of("Multiple periods", "234.234.234", false),
                Arguments.of("Digit and period no decimal" , " 50.", false),
                Arguments.of("Period only", ".", false),
                Arguments.of("Signed digit", "-.", false),
                Arguments.of("Decimal without leading coefficient" , ".90" , false),
                Arguments.of("Signed decimal without leading coefficient", "-.430234" , false)
        );
    }

    @ParameterizedTest
    @MethodSource
    public void testStringRegex(String test, String input, boolean success) {
        test(input, Regex.STRING, success);
    }

    public static Stream<Arguments> testStringRegex() {
        return  Stream.of(
                Arguments.of("Start and End with Double Quote", "\" \"", true),
                Arguments.of("Alphabetic Characters", "\"abc\"", true),
                Arguments.of("Valid Escape Sequence", "\"Hello,\nWorld!\"", true),
                Arguments.of("Missing Double Quote", "\"unterminated", false),
                Arguments.of("Invalid Escape Sequence", "\"invalid\\escape\"", false),
                Arguments.of("Missing quote" , "No quotes to mark string" , false),
                Arguments.of("Random character without escape", "\"\\x\"" , false),
                Arguments.of("\"", "\"" , false),
                Arguments.of("Accidentally repeated escaped character" , "\"Hello,\nn World!\"", true),
                Arguments.of("Multiple escaped characters" , "\"Testing \b spaces\n \t them \b\"", true)
        );
    }

    /**
     * Asserts that the input matches the given pattern and returns the matcher
     * for additional assertions.
     */
    private static Matcher test(String input, Pattern pattern, boolean success) {
        Matcher matcher = pattern.matcher(input);
        Assertions.assertEquals(success, matcher.matches());
        return matcher;
    }

}
