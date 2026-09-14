package com.stirlang.runtime;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Runtime representation of dynamic, homogeneous arrays in Stirlang.
 * Backed by ArrayList<Object>, tracking element type dynamically.
 */
public class StirlangArray {
    private final List<Object> elements = new ArrayList<>();
    private String elementType = null; // null for empty array until first element is added

    public StirlangArray() {
    }

    public static StirlangArray of(Object... items) {
        StirlangArray arr = new StirlangArray();
        for (Object item : items) {
            arr.addToEnd(item);
        }
        return arr;
    }

    public String getElementType() {
        return elementType;
    }

    public void setElementType(String elementType) {
        this.elementType = elementType;
    }

    public int size() {
        return elements.size();
    }

    public boolean isEmpty() {
        return elements.isEmpty();
    }

    public static String typeOf(Object value) {
        if (value == null) return "null";
        if (value instanceof Integer || value instanceof Long || value instanceof Short || value instanceof Byte) {
            return "INT";
        }
        if (value instanceof Double || value instanceof Float) {
            return "DECIMAL";
        }
        if (value instanceof String) {
            return "STRING";
        }
        if (value instanceof Boolean) {
            return "BOOLEAN";
        }
        if (value instanceof StirlangArray) {
            StirlangArray arr = (StirlangArray) value;
            if (arr.getElementType() == null) {
                return "ARRAY";
            }
            return "ARRAY<" + arr.getElementType() + ">";
        }
        return value.getClass().getSimpleName();
    }

    public static boolean isCompatible(String expected, String actual) {
        if (expected == null) return true;
        if (expected.equals(actual)) return true;
        if (expected.startsWith("ARRAY<") && actual.equals("ARRAY")) return true;
        if (expected.equals("ARRAY") && actual.startsWith("ARRAY<")) return true;
        if (expected.startsWith("ARRAY<") && actual.startsWith("ARRAY<")) {
            String innerExpected = expected.substring(6, expected.length() - 1);
            String innerActual = actual.substring(6, actual.length() - 1);
            return isCompatible(innerExpected, innerActual);
        }
        return false;
    }

    private void checkTypeForAdd(Object value) {
        String valType = typeOf(value);
        if (elementType == null) {
            elementType = valType;
        } else {
            if (!isCompatible(elementType, valType)) {
                throw new StirlangRuntimeError("Type mismatch: cannot add value of type " + valType + " to array of type " + elementType);
            }
            if (value instanceof StirlangArray && elementType.startsWith("ARRAY<")) {
                StirlangArray subArr = (StirlangArray) value;
                if (subArr.getElementType() == null) {
                    subArr.setElementType(elementType.substring(6, elementType.length() - 1));
                }
            }
        }
    }

    public void addToEnd(Object value) {
        checkTypeForAdd(value);
        elements.add(value);
    }

    public void addToFront(Object value) {
        checkTypeForAdd(value);
        elements.add(0, value);
    }

    public void add(Object value, int index) {
        checkTypeForAdd(value);
        int actual = index < 0 ? elements.size() + index : index;
        if (actual < 0 || actual > elements.size()) {
            throw new StirlangRuntimeError("Array index out of range: " + index + " (size: " + elements.size() + ")");
        }
        elements.add(actual, value);
    }

    public Object get(int index) {
        int actual = index < 0 ? elements.size() + index : index;
        if (actual < 0 || actual >= elements.size()) {
            throw new StirlangRuntimeError("Array index out of range: " + index + " (size: " + elements.size() + ")");
        }
        return elements.get(actual);
    }

    public void set(int index, Object value) {
        int actual = index < 0 ? elements.size() + index : index;
        if (actual < 0 || actual >= elements.size()) {
            throw new StirlangRuntimeError("Array index out of range: " + index + " (size: " + elements.size() + ")");
        }
        String valType = typeOf(value);
        if (elementType != null && !isCompatible(elementType, valType)) {
            throw new StirlangRuntimeError("Type mismatch: cannot assign value of type " + valType + " to array of type " + elementType);
        }
        elements.set(actual, value);
    }

    public void remove(Object value) {
        String valType = typeOf(value);
        if (elementType != null && !isCompatible(elementType, valType)) {
            throw new StirlangRuntimeError("Type mismatch: cannot remove value of type " + valType + " from array of type " + elementType);
        }
        elements.removeIf(e -> Objects.equals(e, value));
    }

    public void removeIndex(int index) {
        int actual = index < 0 ? elements.size() + index : index;
        if (actual < 0 || actual >= elements.size()) {
            throw new StirlangRuntimeError("Array index out of range: " + index + " (size: " + elements.size() + ")");
        }
        elements.remove(actual);
    }

    public int getInt(int index) {
        Object val = get(index);
        if (val instanceof Number) {
            return ((Number) val).intValue();
        }
        throw new StirlangRuntimeError("Expected integer element at index " + index + ", found " + typeOf(val));
    }

    public double getDouble(int index) {
        Object val = get(index);
        if (val instanceof Number) {
            return ((Number) val).doubleValue();
        }
        throw new StirlangRuntimeError("Expected decimal element at index " + index + ", found " + typeOf(val));
    }

    public String getString(int index) {
        Object val = get(index);
        if (val instanceof String) {
            return (String) val;
        }
        throw new StirlangRuntimeError("Expected string element at index " + index + ", found " + typeOf(val));
    }

    public boolean getBoolean(int index) {
        Object val = get(index);
        if (val instanceof Boolean) {
            return (Boolean) val;
        }
        throw new StirlangRuntimeError("Expected boolean element at index " + index + ", found " + typeOf(val));
    }

    public StirlangArray getArray(int index) {
        Object val = get(index);
        if (val instanceof StirlangArray) {
            return (StirlangArray) val;
        }
        throw new StirlangRuntimeError("Expected array element at index " + index + ", found " + typeOf(val));
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("{");
        for (int i = 0; i < elements.size(); i++) {
            if (i > 0) sb.append(", ");
            Object elem = elements.get(i);
            if (elem instanceof String) {
                sb.append("\"").append(elem).append("\"");
            } else {
                sb.append(elem);
            }
        }
        sb.append("}");
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StirlangArray)) return false;
        StirlangArray that = (StirlangArray) o;
        return Objects.equals(elements, that.elements);
    }

    @Override
    public int hashCode() {
        return Objects.hash(elements);
    }
}
