package plc.interpreter;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

/**
 * You know the drill...
 */
final class ParserTests {

    @ParameterizedTest
    @MethodSource
    void testSource(String test, String input, List<Ast> expected) {
        test(input, expected);
    }

    static Stream<Arguments> testSource() {
        return Stream.of(
                Arguments.of("Empty", "", Arrays.asList()),
                Arguments.of("Single", "(print x)", Arrays.asList(
                        new Ast.Term("print", Arrays.asList(new Ast.Identifier("x")))
                )),
                Arguments.of("Multiple", "(print x)\n(print y)\n(print z)", Arrays.asList(
                        new Ast.Term("print", Arrays.asList(new Ast.Identifier("x"))),
                        new Ast.Term("print", Arrays.asList(new Ast.Identifier("y"))),
                        new Ast.Term("print", Arrays.asList(new Ast.Identifier("z")))
                ))
        );
    }

    @ParameterizedTest
    @MethodSource
    void testTerm(String test, String input, List<Ast> expected) {
        test(input, expected);
    }

    static Stream<Arguments> testTerm() {
        return Stream.of(
                Arguments.of("Empty Args", "(print)", Arrays.asList(
                        new Ast.Term("print", Arrays.asList())
                )),
                Arguments.of("Single Arg", "(print x)", Arrays.asList(
                        new Ast.Term("print", Arrays.asList(new Ast.Identifier("x")))
                )),
                Arguments.of("Multiple Args", "(print x y z)", Arrays.asList(
                        new Ast.Term("print", Arrays.asList(
                                new Ast.Identifier("x"),
                                new Ast.Identifier("y"),
                                new Ast.Identifier("z")
                        ))
                )),
                Arguments.of("Nested", "(print (f x))", Arrays.asList(
                        new Ast.Term("print", Arrays.asList(
                                new Ast.Term("f", Arrays.asList(new Ast.Identifier("x")))
                        ))
                )),
                Arguments.of("Missing Identifier", "()", null),
                Arguments.of("Mixed seperators 1", "(print x]", null),
                Arguments.of("Mixed seperators 2", "[print (f x))", null)
                );
    }

    @ParameterizedTest
    @MethodSource
    void testIdentifier(String test, String input, List<Ast> expected) {
        test(input, expected);
    }

    static Stream<Arguments> testIdentifier() {
        return Stream.of(
                Arguments.of("Alphanumeric", "(print abc123)", Arrays.asList(
                        new Ast.Term("print", Arrays.asList(new Ast.Identifier("abc123")))
                )),
                Arguments.of("Symbols", "(print <=>)", Arrays.asList(
                        new Ast.Term("print", Arrays.asList(new Ast.Identifier("<=>")))
                ))
        );
    }

    @ParameterizedTest
    @MethodSource
    void testNumber(String test, String input, List<Ast> expected) {
        test(input, expected);
    }

    static Stream<Arguments> testNumber() {
        return Stream.of(
                Arguments.of("Decimal", "(print 1.5)", Arrays.asList(
                        new Ast.Term("print", Arrays.asList(new Ast.NumberLiteral(BigDecimal.valueOf(1.5))))
                )),
                Arguments.of("Signed", "(print -2)", Arrays.asList(
                        new Ast.Term("print", Arrays.asList(new Ast.NumberLiteral(BigDecimal.valueOf(-2))))
                )),
                Arguments.of("Precision", "(print 123456789123456789.123456789123456789)", Arrays.asList(
                        new Ast.Term("print", Arrays.asList(
                                new Ast.NumberLiteral(new BigDecimal("123456789123456789.123456789123456789"))
                        ))
                ))
        );
    }

    @ParameterizedTest
    @MethodSource
    void testString(String test, String input, List<Ast> expected) {
        test(input, expected);
    }

    static Stream<Arguments> testString() {
        return Stream.of(
                Arguments.of("Empty", "(print \"\")", Arrays.asList(
                        new Ast.Term("print", Arrays.asList(new Ast.StringLiteral("")))
                )),
                Arguments.of("Alphanumeric", "(print \"abc123\")", Arrays.asList(
                        new Ast.Term("print", Arrays.asList(new Ast.StringLiteral("abc123")))
                )),
                Arguments.of("Escape", "(print \"new\\nline\")", Arrays.asList(
                        new Ast.Term("print", Arrays.asList(new Ast.StringLiteral("new\nline")))
                )),
                Arguments.of("Escape", "(print \"new\\tline\")", Arrays.asList(
                        new Ast.Term("print", Arrays.asList(new Ast.StringLiteral("new\tline")))
                )),
                Arguments.of("Escape", "(print \"new\\rline\")", Arrays.asList(
                        new Ast.Term("print", Arrays.asList(new Ast.StringLiteral("new\rline")))
                )),
                Arguments.of("Escape", "(print \"\\b\\n\\r\\t\\\'\\\"\\\\\")", Arrays.asList(
                        new Ast.Term("print", Arrays.asList(new Ast.StringLiteral("\\\"␈␊␍␉\\'\\\"\\\\\\\"")))
                ))
        );
    }

    @Test
    void testInvalidCharacter() {
        test("(print $)", null);
    }

    @Test
    void testInvalidParen() {
        test(")invalid(", null);
    }

    @Test
    void testE() {
        test("(", null);
    }

    @Test
    void testNoIdentifier() {
        test("(55)", null);
    }

    @Test
    void testExample1() {
        String input = "(+ 1 -2.0)";
        Ast expected = new Ast.Term("+", Arrays.asList(
                new Ast.NumberLiteral(BigDecimal.ONE),
                new Ast.NumberLiteral(BigDecimal.valueOf(-2.0))
        ));
        test(input, Arrays.asList(expected));
    }

    @Test
    void testExample2() {
        String input = "(print \"Hello, World!\")";
        Ast expected = new Ast.Term("print", Arrays.asList(
                new Ast.StringLiteral("Hello, World!")
        ));
        test(input, Arrays.asList(expected));
    }

    @Test
    void testExample3() {
        String input = "(let [x 10] (assert-equals? x 10))";
        Ast expected = new Ast.Term("let", Arrays.asList(
                new Ast.Term("x", Arrays.asList(
                        new Ast.NumberLiteral(BigDecimal.TEN)
                )),
                new Ast.Term("assert-equals?", Arrays.asList(
                        new Ast.Identifier("x"),
                        new Ast.NumberLiteral(BigDecimal.TEN)
                ))
        ));
        test(input, Arrays.asList(expected));
    }

    void test(String input, List<Ast> expected) {
        if (expected != null) {
            Ast ast = new Ast.Term("source", expected);
            Assertions.assertEquals(ast, Parser.parse(input));
        } else {
            Assertions.assertThrows(ParseException.class, () -> Parser.parse(input));
        }
    }

}
