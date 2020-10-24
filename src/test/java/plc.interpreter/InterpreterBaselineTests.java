package plc.interpreter;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

final class InterpreterBaselineTests {

    @Test
    void testTerm() {
        test(new Ast.Term("print", Arrays.asList(
                new Ast.StringLiteral("Hello, World!"))
        ), Interpreter.VOID, Collections.emptyMap());
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
    void testBoolean(String test, boolean value) {
        test(new Ast.Identifier(String.valueOf(value)), value, Collections.emptyMap());
    }

    private static Stream<Arguments> testBoolean() {
        return Stream.of(
                Arguments.of("True", true),
                Arguments.of("False", false)
        );
    }

    @Test
    void testDefine() {
        Scope parent = new Scope(null);
        parent.define("x", BigDecimal.ONE);
        Interpreter interpreter = new Interpreter(new PrintWriter(System.out, true), new Scope(parent));
        Object result = interpreter.eval(new Ast.Term("define", Arrays.asList(
                new Ast.Identifier("x"),
                new Ast.NumberLiteral(BigDecimal.TEN)
        )));
        Assertions.assertAll(
                () -> Assertions.assertEquals(Interpreter.VOID, result),
                () -> Assertions.assertEquals(BigDecimal.ONE, parent.lookup("x")),
                () -> Assertions.assertEquals(BigDecimal.TEN, interpreter.scope.lookup("x"))
        );
    }

    @Test
    void testSet() {
        Scope parent = new Scope(null);
        parent.define("x", BigDecimal.ONE);
        Interpreter interpreter = new Interpreter(new PrintWriter(System.out, true), new Scope(parent));
        Object result = interpreter.eval(new Ast.Term("set!", Arrays.asList(
                new Ast.Identifier("x"),
                new Ast.NumberLiteral(BigDecimal.TEN)
        )));
        Assertions.assertAll(
                () -> Assertions.assertEquals(Interpreter.VOID, result),
                () -> Assertions.assertEquals(BigDecimal.TEN, parent.lookup("x"))
        );
    }

    @ParameterizedTest
    @MethodSource
    void testDo(String test, Ast ast, Object expected) {
        test(ast, expected, Collections.emptyMap());
    }

    private static Stream<Arguments> testDo() {
        return Stream.of(
                Arguments.of("Zero Arguments", new Ast.Term("do", Arrays.asList()), Interpreter.VOID),
                Arguments.of("Multiple Arguments", new Ast.Term("do", Arrays.asList(
                        new Ast.StringLiteral("string"),
                        new Ast.Term("+", Arrays.asList(
                                new Ast.NumberLiteral(BigDecimal.ONE),
                                new Ast.NumberLiteral(BigDecimal.valueOf(2)),
                                new Ast.NumberLiteral(BigDecimal.valueOf(3))
                        ))
                )), BigDecimal.valueOf(6))
        );
    }

    @Test
    void testDoScope() {
        Scope scope = new Scope(null);
        scope.define("x", BigDecimal.ONE);
        Interpreter interpreter = new Interpreter(new PrintWriter(System.out, true), scope);
        Object result = interpreter.eval(new Ast.Term("do", Arrays.asList(
                new Ast.Term("define", Arrays.asList(
                        new Ast.Identifier("x"),
                        new Ast.NumberLiteral(BigDecimal.TEN)
                )),
                new Ast.Identifier("x")
        )));
        Assertions.assertAll(
                () -> Assertions.assertEquals(BigDecimal.TEN, result),
                () -> Assertions.assertEquals(BigDecimal.ONE, interpreter.scope.lookup("x"))
        );
    }

    @Test
    void testScopeReuse() {
        Interpreter interpreter = new Interpreter(new PrintWriter(System.out, true), new Scope(null));
        Scope parent = interpreter.scope;
        Assertions.assertAll(
                () -> {
                    interpreter.scope = new Scope(parent);
                    interpreter.scope.define("x", BigDecimal.ZERO);
                    Assertions.assertEquals(BigDecimal.ZERO, interpreter.eval(new Ast.Identifier("x")));
                },
                () -> {
                    interpreter.scope = new Scope(parent);
                    interpreter.scope.define("x", BigDecimal.ONE);
                    Assertions.assertEquals(BigDecimal.ONE, interpreter.eval(new Ast.Identifier("x")));
                }
        );
    }

    private static void test(Ast ast, Object expected, Map<String, Object> map) {
        Scope scope = new Scope(null);
        map.forEach(scope::define);
        Interpreter interpreter = new Interpreter(new PrintWriter(System.out, true), scope);
        if (expected != null) {
            Assertions.assertEquals(expected, interpreter.eval(ast));
        } else {
            Assertions.assertThrows(EvalException.class, () -> interpreter.eval(ast));
        }
    }

}
