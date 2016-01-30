/*
 *  Copyright Lufthansa Systems.
 */
package necromancer.data.kryo;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Kryo.DefaultInstantiatorStrategy;
import edu.tufts.eaftan.hprofparser.parser.datastructures.Type;
import java.util.ArrayList;
import java.util.HashMap;
import necromancer.data.ClassId;
import necromancer.data.ObjectId;
import necromancer.data.ShadowClass;
import necromancer.data.ShadowObject;
import necromancer.data.ShadowObjectArray;
import org.objenesis.strategy.StdInstantiatorStrategy;

public class NecromancerKryo {

    public static Kryo getInstance() {
        Kryo instance = new Kryo();
        instance.register(ShadowClass.class, 24);
        instance.register(ShadowObject.class, 10);
        instance.register(ShadowObjectArray.class, 11);
        instance.register(ObjectId.class, 12);
        instance.register(ClassId.class, 13);
        instance.register(KryoObject.class, 14);

        instance.register(Integer.class, 15);
        instance.register(Short.class, 16);
        instance.register(Long.class, 17);
        instance.register(Character.class, 18);
        instance.register(Float.class, 19);
        instance.register(Double.class, 20);
        instance.register(Byte.class, 21);
        instance.register(HashMap.class, 22);
        instance.register(ArrayList.class, 23);
        instance.register(Type.class, 25);

        instance.setInstantiatorStrategy(new DefaultInstantiatorStrategy(new StdInstantiatorStrategy()));

        instance.setAutoReset(false);
        instance.setReferences(false);
        instance.setRegistrationRequired(true);
        
        return instance;
    }
}
