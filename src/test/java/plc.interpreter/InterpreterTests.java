package plc.interpreter;

import com.sun.org.apache.xpath.internal.operations.Bool;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import sun.awt.image.ImageWatched;

import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;
import java.util.stream.Stream;

final class InterpreterTests {

    @Test
    void testTerm() {
        test(new Ast.Term("print", Arrays.asList()), Interpreter.VOID, Collections.emptyMap());
    }

    @Test
    void testIdentifier() {
        test(new Ast.Identifier("num"), 10, Collections.singletonMap("num", 10));
    }

    @Test
    void testNumber() {
        test(new Ast.NumberLiteral(BigDecimal.ONE), BigDecimal.ONE, Collections.emptyMap());
    }

    @Test
    void testString() {
        test(new Ast.StringLiteral("string"), "string", Collections.emptyMap());
    }

    @ParameterizedTest
    @MethodSource
    void testAddition(String test, Ast ast, BigDecimal expected) {
        test(ast, expected, Collections.emptyMap());
    }

    private static Stream<Arguments> testAddition() {
        return Stream.of(
               Arguments.of("Zero Arguments", new Ast.Term("+", Arrays.asList()), BigDecimal.valueOf(0)),
                Arguments.of("Multiple Arguments", new Ast.Term("+", Arrays.asList(
                        new Ast.NumberLiteral(BigDecimal.ONE),
                        new Ast.NumberLiteral(BigDecimal.valueOf(2)),
                        new Ast.NumberLiteral(BigDecimal.valueOf(3))
                )), BigDecimal.valueOf(6))
        );
    }

    @ParameterizedTest
    @MethodSource
    void testSubtraction(String test, Ast ast, BigDecimal expected) {
        test(ast, expected, Collections.emptyMap());
    }

    private static Stream<Arguments> testSubtraction() {
        return Stream.of(
                Arguments.of("Zero Arguments", new Ast.Term("-", Arrays.asList()), null),
                Arguments.of("Single Argument", new Ast.Term("-", Arrays.asList(
                        new Ast.NumberLiteral(BigDecimal.ONE)
                )), BigDecimal.valueOf(-1)),
                Arguments.of("Multiple Arguments", new Ast.Term("-", Arrays.asList(
                        new Ast.NumberLiteral(BigDecimal.ONE),
                        new Ast.NumberLiteral(BigDecimal.valueOf(2)),
                        new Ast.NumberLiteral(BigDecimal.valueOf(3))
                )), BigDecimal.valueOf(-4))
        );
    }

    @ParameterizedTest
        @MethodSource
        void testDivision(String test, Ast ast, BigDecimal expected) {
            test(ast, expected, Collections.emptyMap());
        }

        private static Stream<Arguments> testDivision() {
            return Stream.of(
                    Arguments.of("Zero Arguments", new Ast.Term("/", Arrays.asList()), null),
                    Arguments.of("Single Argument", new Ast.Term("/", Arrays.asList(
                            new Ast.NumberLiteral(BigDecimal.TEN)
                    )), BigDecimal.valueOf(0)),
                    Arguments.of("Multiple Arguments", new Ast.Term("/", Arrays.asList(
                            new Ast.NumberLiteral(new BigDecimal("1.000")),
                        new Ast.NumberLiteral(BigDecimal.valueOf(2)),
                        new Ast.NumberLiteral(BigDecimal.valueOf(3))
                )), BigDecimal.valueOf(0.167))
        );
    }

    @ParameterizedTest
    @MethodSource
    void testAnd(String test, Ast ast, Boolean expected) {
        test(ast, expected, Collections.emptyMap());
    }

    private static Stream<Arguments> testAnd() {
        return Stream.of(
                Arguments.of("and true", new Ast.Term("and", Arrays.asList(
                        new Ast.Identifier("true")
                )), Boolean.TRUE),
                Arguments.of("and true false", new Ast.Term("and", Arrays.asList(
                        new Ast.Identifier("true"),
                        new Ast.Identifier("false")
                )), false)
        );
    }
//TODO: make sure that all the math functions evaluate the ast so that it is not only expecting ast.numberliterals
    @ParameterizedTest
    @MethodSource
    void testOr(String test, Ast ast, Boolean expected) {
        test(ast, expected, Collections.emptyMap());
    }

    private static Stream<Arguments> testOr() {
        return Stream.of(
                Arguments.of("and true", new Ast.Term("or", Arrays.asList(
                        new Ast.Identifier("true")
                )), true)
        );
    }

    @ParameterizedTest
    @MethodSource
    void testList(String test, Ast ast, LinkedList expected) {
        test(ast, expected, Collections.emptyMap());
    }

    private static Stream<Arguments> testList() {
        return Stream.of(
                Arguments.of("list", new Ast.Term("list", Arrays.asList(
                        new Ast.NumberLiteral(BigDecimal.ONE),
                        new Ast.StringLiteral("BigDecimal.valueOf(2)"),
                        new Ast.NumberLiteral(BigDecimal.valueOf(3))
                )), new LinkedList<>(Arrays.asList(1,2,3)))
        );
    }

    @ParameterizedTest
    @MethodSource
    void testRange(String test, Ast ast, LinkedList<BigDecimal> expected) {
        test(ast, expected, Collections.emptyMap());
    }

    private static Stream<Arguments> testRange() {
        return Stream.of(
                Arguments.of("0 - 3", new Ast.Term("range", Arrays.asList(
                        new Ast.NumberLiteral(BigDecimal.ZERO),
                        new Ast.NumberLiteral(BigDecimal.valueOf(3))
                )), new LinkedList<>(Arrays.asList(0,1,2))),
                Arguments.of("0 - 3", new Ast.Term("range", Arrays.asList(
                        new Ast.NumberLiteral(BigDecimal.ONE),
                        new Ast.NumberLiteral(BigDecimal.ONE)
                )), new LinkedList<>())
        );
    }

    @ParameterizedTest
    @MethodSource
    void testNot(String test, Ast ast, Boolean expected) {
        test(ast, expected, Collections.emptyMap());
    }

    private static Stream<Arguments> testNot() {
        return Stream.of(
                Arguments.of("not true", new Ast.Term("not", Arrays.asList(
                        new Ast.Identifier("true")
                )), false),
                Arguments.of("not 0", new Ast.Term("not", Arrays.asList(
                        new Ast.Identifier("0")
                )), null)
        );
    }

    @ParameterizedTest
    @MethodSource
    void testEquals(String test, Ast ast, Boolean expected) {
        test(ast, expected, Collections.emptyMap());
    }

    private static Stream<Arguments> testEquals() {
        return Stream.of(
                Arguments.of("10 == \"10\"", new Ast.Term("equals?", Arrays.asList(
                        new Ast.NumberLiteral(BigDecimal.TEN),
                        new Ast.StringLiteral("10")
                )), false),
                Arguments.of("10 == 10", new Ast.Term("equals?", Arrays.asList(
                        new Ast.NumberLiteral(BigDecimal.TEN),
                        new Ast.NumberLiteral(BigDecimal.TEN)
                )), true),
                Arguments.of("10 == ", new Ast.Term("equals?", Arrays.asList(
                        new Ast.NumberLiteral(BigDecimal.TEN)
                )), null),
                Arguments.of("10 == print", new Ast.Term("equals?", Arrays.asList(
                        new Ast.NumberLiteral(BigDecimal.TEN),
                        new Ast.Term("print" , Arrays.asList(
                                new Ast.StringLiteral("unreached")
                        )))), false)
        );
    }

    private static void test(Ast ast, Object expected, Map<String, Object> map) {
        Scope scope = new Scope(null);
        map.forEach(scope::define);
        Interpreter interpreter = new Interpreter(new PrintWriter(System.out), scope);
        if (expected != null) {
            Assertions.assertEquals(expected, interpreter.eval(ast));
        } else {
            Assertions.assertThrows(EvalException.class, () -> interpreter.eval(ast));
        }
    }

}
