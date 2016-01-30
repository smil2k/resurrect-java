/*
 *  Copyright Lufthansa Systems.
 */
package necromancer.data.kryo;

import necromancer.data.ShadowObjectArray;
import edu.tufts.eaftan.hprofparser.parser.datastructures.Type;
import java.io.Serializable;
import java.util.ArrayList;
import lombok.Value;
import necromancer.data.ClassId;
import necromancer.data.ObjectId;
import necromancer.data.ShadowClass;
import necromancer.data.ShadowObject;

@Value
public class KryoObject implements Serializable {

    private ClassId classId;
    private ObjectId objectId;
    private boolean array;
    private Type type;
    private long offset;

    public Class getClassForPersistence() {
        if (array) {
            switch (type) {
                case OBJ:
                    return ShadowObjectArray.class;
                case CHAR:
                    return String.class;
                default:
                    return ArrayList.class;
            }
        }

        switch (type) {

            case OBJ:
                if (classId == null) {
                    return ShadowClass.class;
                } else {
                    return array ? ShadowObjectArray.class : ShadowObject.class;
                }

            default:
                throw new IllegalStateException("unexpected non array type " + type);
        }
    }
}
