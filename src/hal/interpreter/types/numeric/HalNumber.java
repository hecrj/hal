package hal.interpreter.types.numeric;

import hal.interpreter.Reference;
import hal.interpreter.core.BuiltinMethod;
import hal.interpreter.core.ReferenceRecord;
import hal.interpreter.exceptions.TypeException;
import hal.interpreter.types.HalBoolean;
import hal.interpreter.types.HalObject;
import hal.interpreter.types.HalString;

public abstract class HalNumber extends HalObject<Number> {
    
    public HalNumber(Integer i) {
        super(i);
    }
    
    public HalNumber(Double d) {
        super(d);
    }
    
    public Integer toInteger() {
        return value.intValue();
    }
    
    public Double toFloat() {
        return value.doubleValue();
    }
    
    public abstract HalBoolean bool();
    public abstract HalNumber neg();
    
    public abstract HalNumber add(HalNumber n);
    
    
    
    private static final Reference __str__ = new Reference(new BuiltinMethod("str") {
        @Override
        public HalObject call(HalObject instance, HalObject... args) {
            return new HalString(((Number)instance.value).toString());
        }
    });

    private static final Reference __int__ = new Reference(new BuiltinMethod("int") {
        @Override
        public HalObject call(HalObject instance, HalObject... args) {
            if (args.length != 0)
                throw new TypeException();
            return new HalInteger(((HalNumber)instance).toInteger());
        }
    });
    
    private static final Reference __float__ = new Reference(new BuiltinMethod("float") {
        @Override
        public HalObject call(HalObject instance, HalObject... args) {
            if (args.length != 0)
                throw new TypeException();
            return new HalFloat(((HalNumber)instance).toFloat());
        }
    });

    private static final Reference __bool__ = new Reference(new BuiltinMethod("bool") {
        @Override
        public HalObject call(HalObject instance, HalObject... args) {
            if(args.length > 0)
                throw new TypeException();

            return ((HalNumber)instance).bool();
        }
    });
    
    private static final Reference __neg__ = new Reference(new BuiltinMethod("neg") {
        @Override
        public HalObject call(HalObject instance, HalObject... args) {
            if (args.length != 0)
                throw new TypeException();
            return ((HalNumber)instance).neg();
        }
    });

    private static final Reference __add__ = new Reference(new BuiltinMethod("add") {
        @Override
        public HalObject call(HalObject instance, HalObject... args) {
            if(args.length != 1 || ! (args[0] instanceof HalNumber))
                throw new TypeException();
            
            return ((HalNumber)instance).add((HalNumber)args[0]);
        }
    });

    private static final Reference __sub__ = new Reference(new BuiltinMethod("sub") {
        @Override
        public HalObject call(HalObject instance, HalObject... args) {
            if(args.length != 1)
                throw new TypeException();

            return new HalInteger(((HalNumber)instance).toInteger() - ((HalNumber)args[0]).toInteger());
        }
    });

    private static final Reference __lt__ = new Reference(new BuiltinMethod("lt") {
        @Override
        public HalObject call(HalObject instance, HalObject... args) {
            if(args.length != 1)
                throw new TypeException();

            return new HalBoolean(((HalInteger)instance.value).toInteger()
                    < ((HalInteger)args[0]).toInteger());
        }
    });
    
    
    public static final ReferenceRecord record = new ReferenceRecord(classId, HalObject.record,
        // Conversion
        __str__,
        __bool__,
        __int__,
        __float__,
        // Unary
        __neg__,

        // Binary
        __add__,
        __sub__,

        // Relational
        __lt__

    );
    
    public ReferenceRecord getRecord() {
        return record;
    }
}