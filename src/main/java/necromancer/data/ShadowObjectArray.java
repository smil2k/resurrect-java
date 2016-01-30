/*
 *  Copyright Lufthansa Systems.
 */
package necromancer.data;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import java.io.Serializable;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class ShadowObjectArray extends AbstractList<Object> implements KryoSerializable, Serializable {

    public String getClassName() {
        return "objarray";
    }

    @Getter
    private ObjectId objectId;
    @Getter
    private List<ObjectId> objectIdArray;

    @Override
    public Object get(int index) {
        return ShadowFactory.getInstance().getObject(objectIdArray.get(index));
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
