/*
 *  Copyright Lufthansa Systems.
 */
package necromancer.data;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;
import java.util.*;

@AllArgsConstructor
public class ShadowObjectArray extends AbstractList<Object> implements KryoSerializable, Serializable {

    public String getClassName() {
        return "objarray";
    }

    @Getter
    private ObjectId objectId;

    @Getter
    private List<ObjectId> objectIdArray;

    public String findReferenceHolder(ObjectId ref) {
        int idx = objectIdArray.indexOf(ref);

        return idx == -1 ? null : "[" + idx + "]";
    }

    public List<Object> getBackReferences() {
        return ShadowFactory.getInstance().getBackReferences(objectId);
    }

    public List<ObjectId> getBackReferenceIds() {
        return ShadowFactory.getInstance().getBackReferenceIds(objectId);
    }

    @Override
    public Object get(int index) {
        return ShadowFactory.getInstance().getObject(objectIdArray.get(index));
    }

    public Object getRaw(int index) {
        return ShadowFactory.getInstance().getRawObject(objectIdArray.get(index));
    }

    @Override
    public int size() {
        return objectIdArray.size();
    }

    @Override
    public void write(Kryo kryo, Output output) {
        kryo.writeObject(output, objectIdArray);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        objectIdArray = kryo.readObject(input, ArrayList.class);
    }
}
