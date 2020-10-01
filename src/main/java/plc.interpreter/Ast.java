package plc.interpreter;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Contains classes for the Abstract Syntax Tree (AST), which stores a
 * structural representation of the program.
 *
 * As a Lisp language, Whisp has a very simple AST that effectively consists of
 * terms (aka function calls, like {@code (print "Hello, World")}), identifiers,
 * and number/string literals. There are other ways to design this but this is
 * what was selected.
 *
 * There is a fair bit of Java overhead in these classes for getters and
 * equals/toString, which are be needed for the interpreter, JUnit tests, and
 * debugging.
 */
public class Ast {

    public static final class Term extends Ast {

        private final String name;
        private final List<Ast> args;

        public Term(String name, List<Ast> args) {
            this.name = name;
            this.args = args;
        }

        public String getName() {
            return name;
        }

        public List<Ast> getArgs() {
            return args;
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof Term && name.equals(((Term) obj).name) && args.equals(((Term) obj).args);
        }

        @Override
        public String toString() {
            return "(" + name + " " + args.stream().map(Object::toString).collect(Collectors.joining(" ")) + ")";
        }

    }

    public static final class Identifier extends Ast {

        private final String name;

        public Identifier(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof Identifier && name.equals(((Identifier) obj).name);
        }

        @Override
        public String toString() {
            return name;
        }

    }

    public static final class NumberLiteral extends Ast {

        /**
         * This bears special attention. {@link BigDecimal} is a Java class for
         * arbitrary-precision decimals, as opposed to floating point which can
         * be inaccurate.
         *
         * We're using BigDecimal for this project for two reasons: many Lisp
         * languages support infinite precision numbers (and some use them by
         * default), and we think this is a worthwhile concept to be familiar
         * with in general.
         */
        private final BigDecimal value;

        public NumberLiteral(BigDecimal value) {
            this.value = value;
        }

        public BigDecimal getValue() {
            return value;
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof NumberLiteral && value.equals(((NumberLiteral) obj).value);
        }

        @Override
        public String toString() {
            return value.toString();
        }

    }

    public static final class StringLiteral extends Ast {

        private final String value;

        public StringLiteral(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof StringLiteral && value.equals(((StringLiteral) obj).value);
        }

        @Override
        public String toString() {
            return "\"" + value + "\"";
        }

    }

}
