package hal.interpreter.core;

import hal.interpreter.DataType;
import hal.interpreter.Reference;

import java.util.HashMap;


public class ReferenceRecord
{
    protected HashMap<String, Reference> record;

    public ReferenceRecord() {
        record = new HashMap<String, Reference>();
    }

    public void setRecord(ReferenceRecord r) {
        record = r.record;
    }

    public void defineReference(String name, Reference ref) {
        record.put(name, ref);
    }

    /** Defines the value of a variable. If the variable does not
     * exist, it is created. If it exists, the value and type of
     * the variable are re-defined.
     * @param name The name of the variable
     * @param value The value of the variable
     */
    public void defineVariable(String name, DataType value) {
        Reference r = record.get(name);
        if (r == null) record.put(name, new Reference(value)); // New definition
        else r.data = value; // Use the previous data
    }

    public void defineBuiltin(Reference builtin) {
        defineReference((String)builtin.data.getValue(), builtin);
    }

    public Reference getReference(String name) {
        Reference r = record.get(name);
        if (r == null) {
            throw new RuntimeException ("Method " + name + " not defined");
        }
        return r;
    }

    /** Gets the value of the variable. The value is represented as
     * a Data object. In this way, any modification of the object
     * implicitly modifies the value of the variable.
     * @param name The name of the variable
     * @return The value of the variable
     */
    public DataType getVariable(String name) {
        return getReference(name).data;
    }
}
