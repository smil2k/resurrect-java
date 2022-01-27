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
    private long[] objectIdArray;

    public String findReferenceHolder(ObjectId ref) {
        for (int i = 0; i < objectIdArray.length; i++) {
            long l = objectIdArray[i];
            if (l == ref.getObjectId()) {
                return "[" + i + "]";
            }
        }
        return  null;
    }

    public List<Object> getBackReferences() {
        return ShadowFactory.getInstance().getBackReferences(objectId);
    }

    public List<ObjectId> getBackReferenceIds() {
        return ShadowFactory.getInstance().getBackReferenceIds(objectId);
    }

    @Override
    public Object get(int index) {
        return ShadowFactory.getInstance().getObject(new ObjectId(objectIdArray[index]));
    }

    public Object getRaw(int index) {
        return ShadowFactory.getInstance().getRawObject(new ObjectId(objectIdArray[index]));
    }

    @Override
    public int size() {
        return objectIdArray.length;
    }

    @Override
    public void write(Kryo kryo, Output output) {
        kryo.writeObject(output, objectId);
        kryo.writeObject(output, objectIdArray);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        objectId = kryo.readObject(input, ObjectId.class);
        objectIdArray = kryo.readObject(input, long[].class);
    }
}
