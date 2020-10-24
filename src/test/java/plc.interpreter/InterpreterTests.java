package plc.interpreter;

import com.sun.org.apache.xpath.internal.operations.Bool;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import sun.awt.image.ImageWatched;
import sun.nio.cs.ext.Big5_HKSCS;

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
                        new Ast.NumberLiteral(BigDecimal.valueOf(2)),
                        new Ast.NumberLiteral(BigDecimal.valueOf(3))
                )), new LinkedList<Object>(Arrays.asList(1,2,3))),
                Arguments.of("list", new Ast.Term("list", Arrays.asList(
                        new Ast.NumberLiteral(BigDecimal.ONE),
                        new Ast.StringLiteral("BigDecimal.valueOf(2)"),
                        new Ast.NumberLiteral(BigDecimal.valueOf(3))
                )), null)
        );
    }

    @Test
    void testLists() {
        Scope scope = new Scope(null);
        Interpreter interpreter = new Interpreter(new PrintWriter(System.out, true), scope);
        Object result = interpreter.eval(new Ast.Term("list", Arrays.asList(
                new Ast.NumberLiteral(BigDecimal.ONE),
                new Ast.NumberLiteral(BigDecimal.valueOf(2)),
                new Ast.NumberLiteral(BigDecimal.valueOf(3))
        )));
        LinkedList<Object> exp = new LinkedList<>();
        exp.add(BigDecimal.ONE);
        exp.add(BigDecimal.valueOf(2));
        exp.add(BigDecimal.valueOf(3));
        Assertions.assertAll(
                () -> Assertions.assertEquals(exp, result),
                () -> Assertions.assertEquals(new LinkedList<Object>(Arrays.asList(BigDecimal.ONE,BigDecimal.valueOf(2),BigDecimal.valueOf(3))) , result)
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
                Arguments.of("1 to 1", new Ast.Term("range", Arrays.asList(
                        new Ast.NumberLiteral(BigDecimal.ONE),
                        new Ast.NumberLiteral(BigDecimal.ONE)
                )), new LinkedList<>()),
                Arguments.of("Too many Arguments", new Ast.Term("range", Arrays.asList(
                        new Ast.NumberLiteral(BigDecimal.ZERO),
                        new Ast.NumberLiteral(BigDecimal.valueOf(3)),
                        new Ast.NumberLiteral(BigDecimal.TEN)
                )), null),
                Arguments.of("Decimal values", new Ast.Term("range", Arrays.asList(
                        new Ast.NumberLiteral(BigDecimal.valueOf(1.5)),
                        new Ast.NumberLiteral(BigDecimal.valueOf(3))
                )), null),
                Arguments.of("Positive to negative", new Ast.Term("range", Arrays.asList(
                        new Ast.NumberLiteral(BigDecimal.ZERO),
                        new Ast.NumberLiteral(BigDecimal.valueOf(-3))
                )), null),
                Arguments.of("No bounds", new Ast.Term("range", Arrays.asList()
                ), null)
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

    @ParameterizedTest
    @MethodSource
    void testComparisons(String test, Ast ast, Boolean expected) {
        test(ast, expected, Collections.emptyMap());
    }

    private static Stream<Arguments> testComparisons() {
        return Stream.of(
//                Arguments.of(">", new Ast.Term(">", Arrays.asList(
//                        new Ast.NumberLiteral(BigDecimal.TEN),
//                        new Ast.NumberLiteral(BigDecimal.valueOf(8)),
//                        new Ast.NumberLiteral(BigDecimal.valueOf(7)),
//                        new Ast.NumberLiteral(BigDecimal.valueOf(5))
//                )), true),
//                Arguments.of(">", new Ast.Term(">", Arrays.asList(
//                        new Ast.NumberLiteral(BigDecimal.TEN),
//                        new Ast.NumberLiteral(BigDecimal.valueOf(8)),
//                        new Ast.NumberLiteral(BigDecimal.valueOf(7)),
//                        new Ast.NumberLiteral(BigDecimal.valueOf(7))
//                )), false),
//                Arguments.of(">=", new Ast.Term(">=", Arrays.asList(
//                        new Ast.NumberLiteral(BigDecimal.TEN),
//                        new Ast.NumberLiteral(BigDecimal.valueOf(8)),
//                        new Ast.NumberLiteral(BigDecimal.valueOf(7)),
//                        new Ast.NumberLiteral(BigDecimal.valueOf(7)),
//                        new Ast.NumberLiteral(BigDecimal.valueOf(5))
//                )), true),
//                Arguments.of("<", new Ast.Term("<", Arrays.asList(
//                        new Ast.NumberLiteral(BigDecimal.TEN),
//                        new Ast.NumberLiteral(BigDecimal.valueOf(18)),
//                        new Ast.NumberLiteral(BigDecimal.valueOf(17)),
//                        new Ast.NumberLiteral(BigDecimal.valueOf(7))
//                )), false),
//                Arguments.of("<", new Ast.Term("<", Arrays.asList(
//                        new Ast.NumberLiteral(BigDecimal.TEN),
//                        new Ast.NumberLiteral(BigDecimal.valueOf(18)),
//                        new Ast.NumberLiteral(BigDecimal.valueOf(19)),
//                        new Ast.NumberLiteral(BigDecimal.valueOf(19))
//                )), false),
//                Arguments.of("<", new Ast.Term("<", Arrays.asList(
//                        new Ast.NumberLiteral(BigDecimal.TEN),
//                        new Ast.NumberLiteral(BigDecimal.valueOf(18)),
//                        new Ast.NumberLiteral(BigDecimal.valueOf(19))
//                )), true),
//                Arguments.of("<=", new Ast.Term("<=", Arrays.asList(
//                        new Ast.NumberLiteral(BigDecimal.TEN),
//                        new Ast.NumberLiteral(BigDecimal.valueOf(18)),
//                        new Ast.NumberLiteral(BigDecimal.valueOf(19)),
//                        new Ast.NumberLiteral(BigDecimal.valueOf(19)),
//                        new Ast.NumberLiteral(BigDecimal.valueOf(20))
//                )), true),
                Arguments.of("<=", new Ast.Term("<=", Arrays.asList(
                        new Ast.NumberLiteral(BigDecimal.TEN),
                        new Ast.StringLiteral("BigDecimal.valueOf()"),
                        new Ast.NumberLiteral(BigDecimal.valueOf(19)),
                        new Ast.NumberLiteral(BigDecimal.valueOf(19)),
                        new Ast.NumberLiteral(BigDecimal.valueOf(20))
                )), null),
                Arguments.of("<=", new Ast.Term("<=", Arrays.asList(
                        new Ast.StringLiteral("BigDecimal.TEN"),
                        new Ast.StringLiteral("BigDecimal.valueOf()"),
                        new Ast.StringLiteral("BigDecimal.valueOf(19)")
                )), true),
                Arguments.of("<=", new Ast.Term("<=", Arrays.asList(
                        new Ast.NumberLiteral(BigDecimal.TEN),
                        new Ast.NumberLiteral(BigDecimal.TEN),
                        new Ast.NumberLiteral(BigDecimal.TEN)
                )), true),
                Arguments.of("<=", new Ast.Term("<=", Arrays.asList(
                        new Ast.StringLiteral("aba"),
                        new Ast.StringLiteral("abb"),
                        new Ast.StringLiteral("abc")
                )), true),
                Arguments.of("<", new Ast.Term("<", Arrays.asList(
                        new Ast.NumberLiteral(BigDecimal.TEN),
                        new Ast.StringLiteral("test"),
                        new Ast.NumberLiteral(BigDecimal.valueOf(19)),
                        new Ast.NumberLiteral(BigDecimal.valueOf(19))
                )), null),
                Arguments.of("<", new Ast.Term("<=", Arrays.asList(
                        new Ast.NumberLiteral(BigDecimal.TEN),
                        new Ast.Identifier("x"),
                        new Ast.NumberLiteral(BigDecimal.valueOf(19)),
                        new Ast.NumberLiteral(BigDecimal.valueOf(19))
                )), true)
        );
    }

    @ParameterizedTest
    @MethodSource
    void testDefine(String test, Ast ast, Object expected) {
        test(ast, expected, Collections.emptyMap());
    }

    private static Stream<Arguments> testDefine() {
        return Stream.of(
                Arguments.of("Define x = 10", new Ast.Term("define", Arrays.asList(
                        new Ast.Identifier("x"),
                        new Ast.NumberLiteral(BigDecimal.TEN)
                )), Interpreter.VOID),
                Arguments.of("Define", new Ast.Term("define", Arrays.asList()), null),
                Arguments.of("Define", new Ast.Term("define", Arrays.asList(
                        new Ast.StringLiteral("x"),
                        new Ast.NumberLiteral(BigDecimal.valueOf(10))
                )), null)

        );
    }

    @ParameterizedTest
    @MethodSource
    void testSet(String test, Ast ast, Void expected) {
        test(ast, expected, Collections.emptyMap());
    }

    private static Stream<Arguments> testSet() {
        return Stream.of(
                Arguments.of("Set x = 10", new Ast.Term("set!", Arrays.asList(
                        new Ast.Identifier("x"),
                        new Ast.NumberLiteral(BigDecimal.TEN)
                )), null)

        );
    }

    @ParameterizedTest
    @MethodSource
    void testDo(String test, Ast ast, Object expected) {
        test(ast, expected, Collections.emptyMap());
    }

    private static Stream<Arguments> testDo() {
        return Stream.of(
                Arguments.of("do", new Ast.Term("do", Arrays.asList(
                        new Ast.Term("define", Arrays.asList(
                                new Ast.Identifier("x"),
                                new Ast.NumberLiteral(BigDecimal.valueOf(20))
                        )),
                        new Ast.Identifier("x")
                )), BigDecimal.valueOf(20)),
                Arguments.of("do", new Ast.Term("do", Arrays.asList()), Interpreter.VOID)
        );
    }

    @ParameterizedTest
    @MethodSource
    void testWhile(String test, Ast ast, Object expected) {
        test(ast, expected, Collections.emptyMap());
    }

    private static Stream<Arguments> testWhile() {
        return Stream.of(
                Arguments.of("while", new Ast.Term("while", Arrays.asList(
                        new Ast.Term("<=", Arrays.asList(
                                new Ast.Identifier("x"),
                                new Ast.NumberLiteral(BigDecimal.valueOf(20))
                        )),
                        new Ast.Term("set!" , Arrays.asList(
                          new Ast.Identifier("x"),
                          new Ast.Term("+" , Arrays.asList(
                                  new Ast.Identifier("x"),
                                  new Ast.NumberLiteral(BigDecimal.valueOf(1))
                          ))
                        ))
                )), Interpreter.VOID)
        );
    }

    @ParameterizedTest
    @MethodSource
    void testFor(String test, Ast ast, Object expected) {
        test(ast, expected, Collections.emptyMap());
    }

    private static Stream<Arguments> testFor() {
        return Stream.of(
                Arguments.of("for", new Ast.Term("for", Arrays.asList(
                        new Ast.Term("i", Arrays.asList(
                                new Ast.Term("range", Arrays.asList(
                                        new Ast.NumberLiteral(BigDecimal.ZERO),
                                        new Ast.NumberLiteral(BigDecimal.TEN)
                                ))
                        )),
                        new Ast.Term("print" , Arrays.asList(
                                new Ast.Identifier("i")
                        ))
                )), Interpreter.VOID),
                Arguments.of("for", new Ast.Term("for", Arrays.asList(
                        new Ast.Term("i", Arrays.asList())
                )), null)
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
