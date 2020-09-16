package plc.interpreter;

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
                Arguments.of("test", "test@test.com", true)
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
                Arguments.of("Python File", "scrippy.py", false)
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
                Arguments.of("10 Characters", "i<3pancakes!", true),
                Arguments.of("6 Characters", "6chars", false),
                Arguments.of("15 Characters", "i<3pancakes!!", false)
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
                Arguments.of("Trailing Comma", "[1,2,3,]", false)
        );
    }

    @ParameterizedTest
    @MethodSource
    public void testIdentifierRegex(String test, String input, boolean success) {
        test(input, Regex.IDENTIFIER, success);
    }

    public static Stream<Arguments> testIdentifierRegex() {
        return Stream.of(
                Arguments.of("Alphanumeric", "getName5", true),
                Arguments.of("Underscores", "_myvariable3_", true),
                Arguments.of("Special Characters", "is-empty?", true),
                Arguments.of("Begins with Digit", "42=life", false),
                Arguments.of("Period", ".", false),
                Arguments.of("Nonspecial Characters", "why,are,there,commas", false)

        );
    }


    @ParameterizedTest
    @MethodSource
    public void testNumberRegex(String test, String input, boolean success) {
        test(input, Regex.NUMBER, success);
    }

    public static Stream<Arguments> testNumberRegex() {
        return Stream.of(
                Arguments.of("One or More Digits", "1", true),
                Arguments.of("Positive or Negative", "-1.0", true),
                Arguments.of("Followed by Decimal Point and One or More Digits", "007.000", true),
                Arguments.of("Begins with Decimal Point", ".5", false),
                Arguments.of("Ends with Decimal Point", "1.", false)
        );
    }

    @ParameterizedTest
    @MethodSource
    public void testStringRegex(String test, String input, boolean success) {
        test(input, Regex.STRING, success);
    }

    public static Stream<Arguments> testStringRegex() {
        return Stream.of(
                Arguments.of("Start and End with Double Quote", "\" \"", true),
                Arguments.of("Alphabetic Characters", "\"abc\"", true),
                Arguments.of("Valid Escape Sequence", "\"Hello,\nWorld!\"", true),
                Arguments.of("Missing Double Quote", "\"unterminated", false),
                Arguments.of("Invalid Escape Sequence", "\"invalid\\escape\"", false)
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
