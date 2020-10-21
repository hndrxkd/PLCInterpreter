package plc.interpreter;

import java.util.HashMap;
import java.util.Map;

public final class Scope {

    private final Scope parent;
    private final Map<String, Object> map = new HashMap<>();

    public Scope(Scope parent) {
        this.parent = parent;
    }

    public Scope getParent() {
        return parent;
    }

    public void define(String name, Object value) {
        if (map.containsKey(name)) {
            throw new EvalException("The identifier " + name + " is already defined in this scope.");
        } else {
            map.put(name, value);
        }
    }

    public void set(String name, Object value) throws EvalException {
        if (map.containsKey(name)) {
            map.put(name, value);
        } else if (parent != null) {
            parent.set(name, value);
        } else {
            throw new EvalException("The identifier " + name + " is not defined.");
        }
    }

    public Object lookup(String name) throws EvalException {
        if (map.containsKey(name)) {
            return map.get(name);
        } else if (parent != null) {
            return parent.lookup(name);
        } else {
            throw new EvalException("The identifier " + name + " is not defined.");
        }
    }

}
