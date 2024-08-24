package CSE_Machine;

import java.util.HashMap;
import java.util.Map;

import PARSER.ASTNode;

// Environment class representing a lexical environment for variable bindings
public class Environment {
    // Parent environment
    private Environment parent;
    // Map to store variable bindings
    private Map<String, ASTNode> name_Value_Map;

    // Constructor to initialize an Environment object
    public Environment() {
        name_Value_Map = new HashMap<String, ASTNode>();
    }

    // Getter method for the parent environment
    public Environment getParent() {
        return parent;
    }

    // Setter method for the parent environment
    public void setParent(Environment parent) {
        this.parent = parent;
    }

    // Method to look up a variable binding in the environment
    public ASTNode lookup(String key) {
        ASTNode retValue = null;
        Map<String, ASTNode> map = name_Value_Map;

        retValue = map.get(key);

        if (retValue != null)
            return retValue.accept(new NodeCopier());

        if (parent != null)
            return parent.lookup(key);
        else
            return null;
    }

    // Method to add a variable binding to the environment
    public void addMapping(String key, ASTNode value) {
        name_Value_Map.put(key, value);
    }
}
