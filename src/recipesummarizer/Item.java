/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package recipesummarizer;

import java.util.Objects;

/**
 *
 * @author nadel
 */
public class Item {
    public static enum Type {
        ITEM,
        FLUID;
              
        public String getUnit() {
            return this == FLUID ? " mB" : "";
        }
    }
    
    
    Type type;
    String name;

    public Item(Type type, String name) {
        this.type = type;
        this.name = name;
    }

    @Override
    public String toString() {
        return name + " (" + type + ")";
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + Objects.hashCode(this.type);
        hash = 29 * hash + Objects.hashCode(this.name);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Item other = (Item) obj;
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        if (this.type != other.type) {
            return false;
        }
        return true;
    }
    
    
}
