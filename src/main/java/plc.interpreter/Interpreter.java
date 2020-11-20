package plc.interpreter;

import com.sun.corba.se.spi.ior.ObjectKey;

import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class Interpreter {

    /**
     * The VOID constant represents a value that has no useful information. It
     * is used as the return value for functions which only perform side
     * effects, such as print, similar to Java.
     */
    public static final Object VOID = new Function<List<Ast>, Object>() {

        @Override
        public Object apply(List<Ast> args) {
            return VOID;
        }

    };

    public final PrintWriter out;
    public Scope scope;

    public Interpreter(PrintWriter out, Scope scope) {
        this.out = out;
        this.scope = scope;
        init(scope);
    }

    /**
     * Delegates evaluation to the method for the specific instance of AST. This
     * is another approach to implementing the visitor pattern.
     */
    public Object eval(Ast ast) {
        if (ast instanceof Ast.Term) {
            return eval((Ast.Term) ast);
        } else if (ast instanceof Ast.Identifier) {
            return eval((Ast.Identifier) ast);
        } else if (ast instanceof Ast.NumberLiteral) {
            return eval((Ast.NumberLiteral) ast);
        } else if (ast instanceof Ast.StringLiteral) {
            return eval((Ast.StringLiteral) ast);
        } else {
            throw new AssertionError(ast.getClass());
        }
    }

    /**
     * Evaluations the Term ast, which returns the value resulting by calling
     * the function stored under the term's name in the current scope. You will
     * need to check that the type of the value is a {@link Function}, and cast
     * to the type {@code Function<List<Ast>, Object>}.
     */
    private Object eval(Ast.Term ast) {
        Object obj = scope.lookup(ast.getName());

        Function<List<Ast>, Object> function = requireType(  Function.class,  obj);
        return function.apply(ast.getArgs());


        //throw new UnsupportedOperationException(); //TODO
    }

    /**
     * Evaluates the Identifier ast, which returns the value stored under the
     * identifier's name in the current scope.
     */
    private Object eval(Ast.Identifier ast) {
        return scope.lookup(ast.getName());
        //throw new UnsupportedOperationException(); //TODO
    }

    /**
     * Evaluates the NumberLiteral ast, which returns the stored number value.
     */
    private BigDecimal eval(Ast.NumberLiteral ast) {
        return  ast.getValue();
      //  throw new UnsupportedOperationException(); //TODO
    }

    /**
     * Evaluates the StringLiteral ast, which returns the stored string value.
     */
    private String eval(Ast.StringLiteral ast) {
        return ast.getValue();
       // throw new UnsupportedOperationException(); //TODO
    }

    /**
     * Initializes the given scope with fields and functions in the standard
     * library.
     */
    private void init(Scope scope) {
        scope.define("print", (Function<List<Ast>, Object>) args -> {
            List<Object> evaluated = args.stream().map(this::eval).collect(Collectors.toList());
            evaluated.forEach(out::print);
            out.println();
            return VOID;
        });
        scope.define("+", (Function<List<Ast>, Object>) args -> {
            List<Object> evaluated = args.stream().map(this::eval).collect(Collectors.toList());
            BigDecimal result = BigDecimal.ZERO;
            for (Object obj : evaluated) {
                result = result.add(requireType(BigDecimal.class, obj));
            }
            return result;
        });
        scope.define("-", (Function<List<Ast>, Object>) args -> {
                List<Object> evaluated = args.stream().map(this::eval).collect(Collectors.toList());
                if(!evaluated.isEmpty()) {
                    BigDecimal result = evaluated.size() > 1 ? requireType(BigDecimal.class, evaluated.get(0)) : BigDecimal.ZERO.subtract(requireType(BigDecimal.class , evaluated.get(0)));
                    evaluated.remove(0);

                    for (Object obj : evaluated) {
                        result = result.subtract(requireType(BigDecimal.class, obj));
                    }
                    return result;
                }else {
                    throw new EvalException("nah");
                }
        });
        scope.define("*" , (Function<List<Ast> , Object>) args -> {
            List<Object> evaluated = args.stream().map(this::eval).collect(Collectors.toList());
            BigDecimal result = BigDecimal.ONE;

            for(Object obj : evaluated) {
                result = result.multiply(requireType(BigDecimal.class , obj));
            }
            return result;

        });
        scope.define("/" , (Function<List<Ast> , Object>) args -> {
            List<Object> evaluated = args.stream().map(this::eval).collect(Collectors.toList());
            if(!evaluated.isEmpty()) {
                BigDecimal result;

                if (evaluated.size() > 1){
                    result = requireType(BigDecimal.class, evaluated.get(0));
                    evaluated = evaluated.subList(1, evaluated.size());
                }
                else result = BigDecimal.ONE;

                for (Object obj : evaluated) {
                    result = result.divide(requireType(BigDecimal.class, obj), RoundingMode.HALF_EVEN);
                }

                return result;
            }else {
                throw new EvalException("nah");
            }
        });
        scope.define("and" , (Function<List<Ast> , Object>) args -> {
            try{
                for(Ast arg : args ) {
                    if (!requireType(Boolean.class, eval(arg))) {
                        return false;
                }
            }
                return true;
            }catch (Exception e) {
                throw new EvalException(e.getMessage());
            }
        });
        scope.define("or" , (Function<List<Ast> , Object>) args -> {
            for(Ast arg : args ) {
                if (requireType(Boolean.class, eval(arg))) {
                    return true;
                }
            }
            return false;
        });
        scope.define("not" , (Function<List<Ast> , Object>) args -> {
            try {
                if(args.size() > 1 ){
                    throw new EvalException("More than one argument");
                }
                boolean value = requireType(Boolean.class, eval(args.get(0)));
                return !value;
            }catch (Exception e) {
                throw new EvalException(e.getMessage());
            }
        });
        scope.define("equals?" , (Function<List<Ast> , Object>) args -> {
            try {
                if(args.size() > 2 ){
                    throw new EvalException("More than two arguments");
                }
                return Objects.deepEquals(eval(args.get(0)), eval(args.get(1)));

            }catch (Exception e) {
                throw new EvalException(e.getMessage());
            }
        });
        scope.define("list" , (Function<List<Ast> , Object>) args -> {
            try {
                LinkedList<Object> ll = new LinkedList<>();

                for (Ast arg : args) {
                    Object next = ll.isEmpty() ?  eval(arg) : requireType(eval(args.get(0)).getClass() , eval(arg));
                    ll.add(next);
                }

                return ll;
            } catch (Exception e) {
            throw new EvalException(e.getMessage());
        }
        });
        scope.define("range" , (Function<List<Ast> , Object>) args -> {
            try {
                if (args.size() > 2) {
                    throw new EvalException("too many arguments for range.");
                }else if(args.isEmpty()){
                    throw new EvalException("too little arguments for range");
                }
                LinkedList<BigDecimal> ll = new LinkedList<>();

                BigDecimal first = requireType(BigDecimal.class, eval(args.get(0)));
                BigDecimal second = requireType(BigDecimal.class, eval(args.get(1)));

                if(first.scale() > 0 | second.scale() > 0 ){
                    throw new EvalException("received a decimal, was expecting integer value for bounds.");
                }else if (second.compareTo(first)  == -1){
                    throw new EvalException("second bound is smaller than first.");
                }

                for (int i = first.intValue(); i < second.intValue(); i++) {
                    ll.add(BigDecimal.valueOf(i));
                }

                return ll;
            }catch (Exception e) {
            throw new EvalException(e.getMessage());
        }
        });
        scope.define("define" , (Function<List<Ast> , Object>) args -> {
            try {
                if(args.size() != 2) {
                    throw new EvalException("Invalid number of arguments");
                }

                if (args.get(0) instanceof Ast.Identifier) {
                    String ident = ((Ast.Identifier) args.get(0)).getName();
                    this.scope.define(ident, eval(args.get(1)));
                    return VOID;
                } else if (args.get(0) instanceof  Ast.Term) {
                    String name = ((Ast.Term) args.get(0)).getName();
                    List<String> params = ((Ast.Term) args.get(0)).getArgs().stream()
                            .map(a -> requireType(Ast.Identifier.class, a).getName())
                            .collect(Collectors.toList());

                    Scope parent = this.scope;

                    this.scope.define(name , (Function<List<Ast> , Object>) arguments -> {
                        List<Object> eval = arguments.stream().map(this::eval).collect(Collectors.toList());

                        if(params.size() != eval.size()){
                            throw new EvalException("Invalid Number of arguments");
                        }

                        Scope current  = this.scope;
                        this.scope = new Scope(parent);

                        for (int i = 0 ; i < params.size() ; i++){
                            this.scope.define(params.get(i), eval.get(i));
                        }
                        Object result = eval(args.get(1));


                        this.scope = current;
                        return  result;
                    });
                }else{
                    throw new EvalException("Expected 2 or more arguments");
                }

                throw new EvalException("Invalid arguments");


            }
            catch (Exception e){
                throw new EvalException(e.getMessage());
            }
        });
        scope.define("set!" , (Function<List<Ast> , Object>) args -> {
            if(args.size() == 2) {
                this.scope.set(args.get(0).toString(), eval(args.get(1)));
            }else {
                throw new EvalException("Was expecting two arguments, got " + args.size());
            }

            return VOID;
        });
        scope.define(">" , (Function<List<Ast> , Object>) args -> {
            try {
            List<Comparable> list = new ArrayList<>();
            for (Ast arg : args){
                list.add(requireType(Comparable.class, eval(arg)));
            }
            for (int i = 0 ; i < args.size() - 1; i++){
               if(list.get(i).compareTo(list.get(i+1)) <= 0){
                   return false;
               }
            }
            return true;
            }catch (Exception e) {
                throw new EvalException(e.getMessage());
            }

        });
        scope.define(">=" , (Function<List<Ast> , Object>) args -> {
            try {
            List<Comparable> list = new ArrayList<>();
            for (Ast arg : args){
                list.add(requireType(Comparable.class, eval(arg)));
            }
            for (int i = 0 ; i < args.size() - 1; i++){
                if(list.get(i).compareTo(list.get(i+1)) < 0){
                    return false;
                }
            }
            return true;
        }catch (Exception e) {
            throw new EvalException(e.getMessage());
        }

        });
        scope.define("<" , (Function<List<Ast> , Object>) args -> {
            try {
            List<Comparable> list = new ArrayList<>();
            for (Ast arg : args){
                list.add(requireType(Comparable.class, eval(arg)));
            }
            for (int i = 0 ; i < args.size() - 1; i++){
                if(list.get(i).compareTo(list.get(i+1)) >= 0){
                    return false;
                }
            }
            return true;
        }catch (Exception e) {
            throw new EvalException(e.getMessage());
        }
        });
        scope.define("<=" , (Function<List<Ast> , Object>) args -> {
            try {
                List<Comparable> list = new ArrayList<>();
                for (Ast arg : args) {
                    list.add(requireType(Comparable.class, eval(arg)));
                }
                for (int i = 0; i < args.size() - 1; i++) {
                    if (list.get(i).compareTo(list.get(i + 1)) > 0) {
                        return false;
                    }
                }
                return true;
            }catch (Exception e) {
                throw new EvalException(e.getMessage());
            }
        });
        scope.define("do" , (Function<List<Ast> , Object>) args -> {
           try {

               this.scope = new Scope(scope);
               Object result = null;

               for (Ast arg : args) {
                   result = eval(arg);
               }

               this.scope = this.scope.getParent();

               if (result == null)
                   return VOID;

               return result;
           }catch (Exception e) {
               throw new EvalException(e.getMessage());
           }

        });
        scope.define("while" , (Function<List<Ast> , Object>) args -> {
            if(args.size() < 1){
                throw new EvalException("not enough arguments for while loop");
            }
            this.scope = new Scope(scope);
//            Object result = null;


            while(requireType(Boolean.class , eval(args.get(0))) == Boolean.TRUE) {
                for (Ast arg : args.subList(1, args.size())) {
                    eval(arg);
                }
            }

            this.scope = this.scope.getParent();

            return VOID;

        });
        scope.define("for" , (Function<List<Ast>, Object>) args -> {
//            this.scope = new Scope(scope);
            try {
                if ( args.size() != 2){
                     throw new EvalException("missing arguments");
                }


                scope.define(args.get(0).toString() , eval(args.get(0)) ) ;

                Ast.Term variable = requireType(Ast.Term.class, eval(args.get(0)));
                LinkedList ll = requireType(LinkedList.class , variable.getArgs().get(0));
                this.scope = new Scope(scope);

                scope.define(variable.getName() , ll.get(0));

                for (Object dec : ll) {
                    this.scope.set(variable.getName(), dec);
                    eval(args.get(2));

                }

                this.scope = scope.getParent();


                return VOID;
            }catch (Exception e) {
                throw new EvalException(e.getMessage());
            }




        });


        scope.define("true" , Boolean.TRUE);
        scope.define("false" , Boolean.FALSE);
//        scope.define("x", BigDecimal.valueOf(2));
//        scope.define("y", BigDecimal.ONE);
//        scope.define("z", BigDecimal.TEN);
        

        //TODO: Additional standard library functions
    }

    /**
     * A helper function for type checking, taking in a type and an object and
     * throws an exception if the object does not have the required type.
     *
     * This function does a poor job of actually identifying where the issue
     * occurs - in a real interpreter, we would have a stacktrace to provide
     * that implementation. For now, this is the simple-but-not-ideal solution.
     */
    private static <T> T requireType(Class<T> type, Object value) {
        if (type.isInstance(value)) {
            return type.cast(value);
        } else {
            throw new EvalException("Expected " + value + " to have type " + type.getSimpleName() + ".");
        }
    }

}
