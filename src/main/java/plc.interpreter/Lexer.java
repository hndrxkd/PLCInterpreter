package plc.interpreter;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * The lexer works through three main functions:
 *
 *  - {@link #lex()}, which repeatedly calls lexToken() and skips whitespace
 *  - {@link #lexToken()}, which lexes the next token
 *  - {@link CharStream}, which manages the state of the lexer and literals
 *
 * If the lexer fails to parse something (such as an unterminated string) you
 * should throw a {@link ParseException}.
 *
 * The {@link #peek(String...)} and {@link #match(String...)} functions are
 * helpers, they're not necessary but their use will make the implementation a
 * lot easier. Regex isn't the most performant way to go but it gets the job
 * done, and the focus here is on the concept.
 */
public final class Lexer {

    final CharStream chars;

    Lexer(String input) {
        chars = new CharStream(input);
    }

    /**
     * Lexes the input and returns the list of tokens.
     */
    public static List<Token> lex(String input) throws ParseException {
        return new Lexer(input).lex();
    }

    /**
     * Repeatedly lexes the next token using {@link #lexToken()} until the end
     * of the input is reached, returning the list of tokens lexed. This should
     * also handle skipping whitespace.
     */
    List<Token> lex() throws ParseException {
        List<Token> tokens = new ArrayList<>();
        int v = 0x0B;
        char vEscape = (char) v;

        while(chars.has(0)){
            while(peek(" ") | peek("[\\\f\\\b\\\n\\\r\\\t]") | peek( String.valueOf(vEscape))){
                chars.advance();
                chars.reset();
            }
            if(chars.has(0))
                tokens.add(lexToken());

        }

        return tokens;


        //throw new UnsupportedOperationException(); //TODO
    }

    /**
     * Lexes the next token. It may be helpful to have this call other methods,
     * such as {@code lexIdentifier()} or {@code lexNumber()}, based on the next
     * character(s).
     *
     * Additionally, here is an example of lexing a character literal (not used
     * in this assignment) using the peek/match methods below.
     *
     * <pre>
     * {@code
     *     Token lexCharacter() {
     *         if (!match("\'")) {
     *             //Your lexer should prevent this from happening, as it should
     *             // only try to lex a character literal if the next character
     *             // begins a character literal.
     *             //Additionally, the index being passed back is a 'ballpark'
     *             // value. If we were doing proper diagnostics, we would want
     *             // to provide a range covering the entire error. It's really
     *             // only for debugging / proof of concept.
     *             throw new ParseException("Next character does not begin a character literal.", chars.index);
     *         }
     *         if (!chars.has(0) || match("\'")) {
     *             throw new ParseException("Empty character literal.",  chars.index);
     *         } else if (match("\\")) {
     *             //lex escape characters...
     *         } else {
     *             chars.advance();
     *         }
     *         if (!match("\'")) {
     *             throw new ParseException("Unterminated character literal.", chars.index);
     *         }
     *         }
     *         return chars.emit(Token.Type.CHARACTER);
     *     }
     * }
     * </pre>
     */
    Token lexToken() throws ParseException {

        if (peek("[0-9]") | peek("[\\+-]","[0-9]")){
            return lexNumber();
        }else if(peek("[a-zA-Z\\*\\?:!/<>_\\\\=\\+-]") | (peek("\\.", "[0-9a-zA-Z\\\\*\\\\?:!/<>_\\\\\\\\=\\\\+-]"))) {
            return lexIdentifier();
        }else if (peek("\"")){
            return lexString();
        }else  if(peek("\\."," " ) | peek("[@(#)\\[\\]\\'\\.]")){
            if(match("[..]") | match("[@(#)\\[\\]\\'\\.]"))
                return chars.emit(Token.Type.OPERATOR);
        }
        throw new ParseException(" couldnt parse", chars.index); //TODO



//        if (match("[0-9]") | peek("[\\+-]","[0-9]")){
//            return lexNumber();
//        }else if(match("[a-zA-Z\\*\\?:!/<>_\\\\=\\+-]") | (match("\\.", "[a-zA-Z\\\\*\\\\?:!/<>_\\\\\\\\=\\\\+-]"))) {
//            return lexIdentifier();
//        }else if (match("\"")){
//            return lexString();
//        }else if(match("[(#)\\[\\]]") | match("\\."," " )) {
//            return chars.emit(Token.Type.OPERATOR);
//        }
//
//
//       throw new ParseException(" couldnt parse", chars.index); //TODO
    }

    Token lexIdentifier() {
        while (match("[a-zA-Z0-9\\.\\+\\-=!_\\?:/\\*<>]") ){}
        return chars.emit(Token.Type.IDENTIFIER);
    }

    Token lexNumber() {
        match("[\\+-]");
        while((match("[0-9]"))){}
        if(match("\\.","[0-9]")){
            while((match("[0-9]"))){}
        }

        return chars.emit(Token.Type.NUMBER);

        //TODO
    }

    Token lexString() throws ParseException {
        if(match("\"")) {
            while (chars.has(0)) {
                if (peek("\\\\")) {
                    if (!match("\\\\", "[bnrt\'\"\\\\]")) {
                        throw new ParseException("could not parse", chars.index);
                    }
                } else if (match("\"")) {
                    return chars.emit(Token.Type.STRING);
                } else if (peek(".")) {
                    match(".");
                }
            }
        }
        throw new ParseException("could not parse", chars.index); //TODO
    }

    /**
     * Returns true if the next sequence of characters match the given patterns,
     * which should be a regex. For example, {@code peek("a", "b", "c")} would
     * return true for the sequence {@code 'a', 'b', 'c'}
     */
    boolean peek(String... patterns) {
        try {
            int pattern = 0;
            for (String regex : patterns) {
                if (Pattern.matches(regex, Character.toString(chars.get(pattern)))) {
                    pattern++;
                } else {
                    return false;
                }
            }
            return true;
        }catch(Exception e){
            return false;
        }

    }


    /**
     * Returns true in the same way as peek, but also advances the CharStream to
     * if the characters matched.
     */
    boolean match(String... patterns) {
        int index = chars.index;
        int length = chars.length;
        try {
            for (String regex : patterns) {
                if (Pattern.matches(regex, Character.toString(chars.get(0)))) {
                        chars.advance();
                } else {
                    chars.index = index;
                    chars.length = length;
                    return false;
                }
            }
            return true;
        }catch(Exception e){
            chars.index = index;
            chars.length = length;
            return false;
        }
    }

    /**
     * This is basically a sequence of characters. The index is used to maintain
     * where in the input string the lexer currently is, and the builder
     * accumulates characters into the literal value for the next token.
     */
    static final class CharStream {

        final String input;
        int index = 0;
        int length = 0;

        CharStream(String input) {
            this.input = input;
        }

        /**
         * Returns true if there is a character at index + offset.
         */
        boolean has(int offset) {
               return index + offset < input.length(); //TODO
        }

        /**
         * Gets the character at index + offset.
         */
        char get(int offset) {
            try{
                return input.charAt(index + offset);
            }catch(Exception e){
                throw new UnsupportedOperationException(); //TODO
            }
        }

        /**
         * Advances to the next character, incrementing the current index and
         * length of the literal being built.
         */
        void advance() {
            index++;
            length++;
             //TODO
        }

        /**
         * Resets the length to zero, skipping any consumed characters.
         */
        void reset() {
            length = 0; //TODO
        }

        /**
         * Returns a token of the given type with the built literal and resets
         * the length to zero. The index of the token should be the
         * <em>starting</em> index.
         */
        Token emit(Token.Type type) {
            int start = index - length;
            reset(); //
            return new Token(type, input.substring(start, index), start);
            //TODO
        }

    }

}
