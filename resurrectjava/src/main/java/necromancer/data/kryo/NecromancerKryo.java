/*
 *  Copyright Lufthansa Systems.
 */
package necromancer.data.kryo;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Kryo.DefaultInstantiatorStrategy;
import edu.tufts.eaftan.hprofparser.parser.datastructures.Type;
import necromancer.data.*;
import org.objenesis.strategy.StdInstantiatorStrategy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class NecromancerKryo {

    public static Kryo getInstance() {
        Kryo instance = new Kryo();
        instance.register(ShadowObject.class, 10);
        instance.register(ShadowObjectArray.class, 11);
        instance.register(ShadowClass.class, 12);

        instance.register(ObjectId.class, 13);
        instance.register(ClassId.class, 14);

        instance.register(HashMap.class, 16);
        instance.register(HashSet.class, 17);
        instance.register(ArrayList.class, 18);
        instance.register(Type.class, 19);
        instance.register(TwoLong.class, 20);
        instance.register(Long.class, 21);

        instance.setInstantiatorStrategy(new DefaultInstantiatorStrategy(new StdInstantiatorStrategy()));

        instance.setAutoReset(false);
        instance.setReferences(false);
        instance.setRegistrationRequired(true);

        return instance;
    }
}
