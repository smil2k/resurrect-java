/*
 *  Copyright Lufthansa Systems.
 */
package necromancer.data;

import com.esotericsoftware.kryo.DefaultSerializer;
import com.esotericsoftware.kryo.serializers.FieldSerializer;
import lombok.Value;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

@Value
@DefaultSerializer(FieldSerializer.class)
public class ShadowObject implements Map<String, Object> {

    private ClassId classId;
    private ObjectId objectId;

    private Map<String, Object> fields;

    public String toString() {
        return "object " + getType().getClassName() + "{" + objectId.getObjectId() + "}{" + fields.keySet() + "}";

    }

    public Collection<Object> getBackReferences() {
        return ShadowFactory.getInstance().getBackReferences(objectId);
    }

    public Set<ObjectId> getBackReferenceIds() {
        return ShadowFactory.getInstance().getBackReferenceIds(objectId);
    }

    public Set<String> getFieldNames() {
        return fields.keySet();
    }

    public ShadowClass getType() {
        return ShadowFactory.getInstance().getClass(classId);
    }

    public String getClassName() {
        return getType().getClassName();
    }

    public Object put(String name, Object value) {
        throw new UnsupportedOperationException();
    }

    public void putAll(
            Map<? extends String, ? extends Object> toMerge) {
        throw new UnsupportedOperationException();
    }

    public boolean containsKey(Object key) {
        return fields.containsKey(key);
    }

    public Object get(Object key) {
        Object o = fields.get(key);

        if (o instanceof ObjectId) {
            o = ShadowFactory.getInstance().getObject((ObjectId) o);
        }

        if (o instanceof ClassId) {
            o = ShadowFactory.getInstance().getClass((ClassId) o);
        }

        return o;
    }

    public Object remove(Object key) {
        throw new UnsupportedOperationException();
    }

    public int size() {
        return fields.size();
    }

    public boolean isEmpty() {
        return fields.isEmpty();
    }

    public boolean containsValue(Object value) {
        return fields.containsValue(value);
    }

    public void clear() {
        throw new UnsupportedOperationException();
    }

    public Set<String> keySet() {
        return fields.keySet();
    }

    public Collection<Object> values() {
        return fields.values();
    }

    public Set<Entry<String, Object>> entrySet() {
        return fields.entrySet();
    }

}
