package plc.interpreter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * The parser takes the sequence of tokens emitted by the lexer and turns that
 * into a structured representation of the program, called the Abstract Syntax
 * Tree (AST).
 *
 * The parser has a similar architecture to the lexer, just with {@link Token}s
 * instead of characters. As before, {@link #peek(Object...)} and {@link
 * #match(Object...)} are helpers to make the implementation easier.
 *
 * This type of parser is called <em>recursive descent</em>. Each rule in our
 * grammar will have it's own function, and reference to other rules correspond
 * to calling that functions.
 */
public final class Parser {

    private final TokenStream tokens;

    private Parser(String input) {
        tokens = new TokenStream(Lexer.lex(input));
    }

    /**
     * Parses the input and returns the AST
     */
    public static Ast parse(String input) {
        return new Parser(input).parse();
    }
    /**
     * Repeatedly parses a list of ASTs, returning the list as arguments of an
     * {@link Ast.Term} with the identifier {@code "source"}.
     */
    private Ast parse() {

        if(tokens.has(0)) {
            List<Ast> ast = new ArrayList<>();
            while(tokens.has(0)) {
                ast.add(parseAst());
            }
            return new Ast.Term("source", ast);
        }
        return new Ast.Term("source" , new ArrayList<>());
//        throw new ParseException("Was expecting there to be tokens", tokens.index ); //TODO
    }

    /**
     * Parses an AST from the given tokens based on the provided grammar. Like
     * the lexToken method, you may find it helpful to have this call other
     * methods like {@code parseTerm()}. In a recursive descent parser, each
     * rule in the grammar would correspond with a {@code parseX()} function.
     *
     * Additionally, here is an example of parsing a function call in a language
     * like Java, which has the form {@code name(args...)}.
     *
     * <pre>
     * {@code
     *     private Ast.FunctionExpr parseFunctionExpr() {
     *         //In a real parser this would be more complex, as the parser
     *         //wouldn't know this should be a function call until reaching the
     *         //opening parenthesis, like name(... <- here. You won't have this
     *         //problem in this project, but will for the compiler project.
     *         if (!match(Token.Type.IDENTIFIER)) {
     *             throw new ParseException("Expected the name of a function.");
     *         }
     *         String name = tokens.get(-1).getLiteral();
     *         if (!match("(")) {
     *             throw new ParseException("Expected opening bra
     *         }
     *         List<Ast> args = new ArrayList<>();
     *         while (!match(")")) {
     *             //recursive call to parseExpr(), not shown here
     *             args.add(parseExpr());
     *             //next token must be a closing parenthesis or comma
     *             if (!peek(")") && !match(",")) {
     *                 throw new ParseException("Expected closing parenthesis or comma after argument.", tokens.get(-1).getIndex());
     *             }
     *         }
     *         return new Ast.FunctionExpr(name, args);
     *     }
     * }
     * </pre>
     */
    private Ast parseAst() {

        while(tokens.has(0)) {
            if (peek(Token.Type.STRING)) {
                return parseStringLiteral();
            } else if (peek(Token.Type.NUMBER)) {
                return parseNumberLiteral();
            } else if (peek(Token.Type.IDENTIFIER)) {
                return parseIdentifier();
            } else if (peek("(") | peek("[")) {
                return parseTerm();
            }
            throw new ParseException("Closing parenthesis before opening" , tokens.index );
        }

        throw new ParseException("Closing parenthesis before opening" , tokens.index );

    }

//gets a list adds term.
    private Ast parseTerm() {
        List<Ast> args = new ArrayList<>();
        String term;

        String wrongClosing;
        String operator = tokens.get(0).getLiteral();
        if(match(Token.Type.OPERATOR)) {
            operator = operator.compareTo("(") == 0 ? ")" : "]";
            wrongClosing = operator.compareTo(")") == 0 ? "]" : ")";
            if(peek(")") | peek("]")){
                throw new ParseException("Was expecting identifier, got function end" , tokens.index);
            }
            if(!peek(Token.Type.IDENTIFIER)){
                throw new ParseException("Was expecting an Identifier" , tokens.index);
            }
            term = tokens.get(0).getLiteral();

            tokens.advance();

            while(!match(operator) ) {
                if(peek(wrongClosing)){
                    throw new ParseException("Expected " + operator + "as closing operator, got " + wrongClosing , tokens.index);
                }
                args.add(parseAst());
            }
            return new Ast.Term(term , args );
        }
        throw new ParseException("Expected an opening parenthesis " , tokens.index);

    }
//this parses identifier
    private Ast parseIdentifier() {
        Ast ident = new Ast.Identifier(tokens.get(0).getLiteral());
        tokens.advance();
        return ident;

//        List<Ast> ident = new ArrayList<>();
//        if(match(Token.Type.IDENTIFIER)) {
//            Ast temp = new Ast.Identifier(tokens.get(0).getLiteral());
//            ident.add(temp);
//        }
//
//        return ident;

        //TODO
    }

    private Ast parseNumberLiteral() {
        BigDecimal big = new BigDecimal(tokens.get(0).getLiteral());
        Ast number = new Ast.NumberLiteral(big);
        tokens.advance();
        return number;
    }

    private Ast parseStringLiteral() {

        //this may still need work - october 27th

        String text = tokens.get(0).getLiteral();
        text = text.replaceAll("\\\\\"" , "\"");
        text = text.replaceAll("\"", "");
        text = text.replaceAll("\\\\n", "\n" );
        text = text.replaceAll("\\\\r", "\r" );
        text = text.replaceAll("\\\\t", "\t" );
        text = text.replaceAll("\\\\b" , "\b");
        text.replaceAll("\\\\'" , "\'");

        Ast string = new Ast.StringLiteral(text);
        tokens.advance();
        return string;

    }

    /**
     * As in the lexer, returns {@code true} if the current sequence of tokens
     * matches the given patterns. Unlike the lexer, the pattern is not a regex;
     * instead it is either a {@link Token.Type}, which matches if the token's
     * type is the same, or a {@link String}, which matches if the token's
     * literal is the same.
     *
     * In other words, {@code Token(IDENTIFIER, "literal")} is matched by both
     * {@code peek(Token.Type.IDENTIFIER)} and {@code peek("literal")}.
     */
    private boolean peek(Object... patterns) {
        try {
            int index = 0;
            for (int i = 0; i < patterns.length; i++) {
//            if(patterns[i] instanceof Token.Type){
                if (patterns[i] == tokens.get(index).getType()) {
                    index++;
                } else if (patterns[i].toString().equals(tokens.get(index).getLiteral())) {
                    index++;
                } else {
                    return false;
                }
            }
            return true;
        }catch (Exception e){
            throw new ParseException(e.getMessage(), tokens.index);
        }
    }

    /**
     * As in the lexer, returns {@code true} if {@link #peek(Object...)} is true
     * and advances the token stream.
     */
    private boolean match(Object... patterns) {
        try {
            int index = tokens.index;

            for (Object type : patterns) {
                if (tokens.has(0) && peek(type)) {
                    tokens.advance();
                } else {
                    tokens.index = index;
                    return false;
                }
            }
            return true;
        }catch (Exception e) {
            throw new ParseException(e.getMessage(), tokens.index);
        }

    }

    private static final class TokenStream {

        private final List<Token> tokens;
        private int index = 0;

        private TokenStream(List<Token> tokens) {
            this.tokens = tokens;
        }

        /**
         * Returns true if there is a token at index + offset.
         */
        public boolean has(int offset) {
            return index + offset < tokens.size(); //TODO
        }

        /**
         * Gets the token at index + offset.
         */
        public Token get(int offset) {
            return tokens.get(index + offset); //TODO
        }

        /**
         * Advances to the next token, incrementing the index.
         */
        public void advance() {
            if(has(0))
                index++;

             //TODO
        }

    }

}
