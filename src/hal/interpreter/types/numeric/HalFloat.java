package hal.interpreter.types.numeric;

import hal.interpreter.core.ReferenceRecord;

public class HalFloat extends HalNumber {
    
    private static final String classId = "Float";
    
    public HalFloat(int i) {
        super((double) i);
    }
    
    public HalFloat(float f) {
        super((double) f);
    }
    
    public HalFloat(double d) {
        super(d);
    }
    
    private static final ReferenceRecord record = new ReferenceRecord(classId, HalNumber.record);
    
    public ReferenceRecord getRecord() {
        return record;
    }

    @Override
    public boolean isZero() {
        return toFloat() == 0.0;
    }

    @Override
    public HalNumber neg() {
        return new HalFloat(-toFloat());
    }

    @Override
    public HalNumber add(HalNumber n) {
        return new HalFloat(toFloat() + n.toFloat());
    }

    @Override
    public HalNumber sub(HalNumber n) {
        return new HalFloat(toFloat() - n.toFloat());
    }

    @Override
    public HalNumber mul(HalNumber n) {
        return new HalFloat(toFloat() * n.toFloat());
    }

    @Override
    public HalNumber div(HalNumber n) {
        return new HalFloat(toFloat() / n.toFloat());
    }
    
}
