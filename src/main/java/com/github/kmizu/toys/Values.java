package com.github.kmizu.toys;

import java.util.List;
import java.util.Map;

public class Values {
    public sealed interface Value permits Int, Bool, Array, Dictionary {
        default Int asInt() {
            return (Int)this;
        }
        default Array asArray() {
            return (Array)this;
        }
        default Dictionary asDictionary() {
            return (Dictionary)this;
        }
        default Bool asBool() {
            return (Bool)this;
        }
    }
    public final static record Int(int value) implements Value {}
    public final static record Array(List<? extends Value> values) implements Value {}
    public final static record Dictionary(Map<? extends Value, ? super Value> entries) implements Value {}
    public final static record Bool(boolean value) implements Value {}
    public static Value wrap(Object javaValue) {
        if(javaValue instanceof Integer v) return new Int(v);
        if(javaValue instanceof Boolean v) return new Bool(v);
        if(javaValue instanceof List<?> v) return new Array((List<Value>)v);
        if(javaValue instanceof Map<?, ?> v) return new Dictionary((Map<Value, Value>)v);
        throw new LanguageException("must not reach here");
    }
}
