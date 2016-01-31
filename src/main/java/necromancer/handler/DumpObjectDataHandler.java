/*
 *  Copyright Lufthansa Systems.
 */
package necromancer.handler;

import com.google.common.collect.Maps;
import edu.tufts.eaftan.hprofparser.handler.NullRecordHandler;
import edu.tufts.eaftan.hprofparser.parser.datastructures.ClassInfo;
import edu.tufts.eaftan.hprofparser.parser.datastructures.Constant;
import edu.tufts.eaftan.hprofparser.parser.datastructures.InstanceField;
import edu.tufts.eaftan.hprofparser.parser.datastructures.Static;
import edu.tufts.eaftan.hprofparser.parser.datastructures.Type;
import edu.tufts.eaftan.hprofparser.parser.datastructures.Value;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import necromancer.data.ClassId;
import necromancer.data.ObjectId;
import necromancer.data.ShadowClass;
import necromancer.data.ShadowObject;
import necromancer.data.ShadowObjectArray;
import necromancer.data.kryo.KryoFileWriter;

public class DumpObjectDataHandler extends NullRecordHandler {

    private int objectCount;

    private Map<Long, String> stringMap = new HashMap<>();
    private Map<Long, String> classNameMap = new HashMap<>();
    private Map<Long, OwnClassInfo> classMap = new HashMap<>();

    private KryoFileWriter writer;

    public DumpObjectDataHandler(File dir) throws FileNotFoundException {
        writer = new KryoFileWriter(dir);
    }

    @Override
    public void finished() {
        try {
            writer.close();
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    @Override
    public void primArrayDump(long objId, int stackTraceSerialNum, byte elemType,
                              Value<?>[] elems) {
        List<Object> prim = Arrays.stream(elems).map(e -> e.value).collect(Collectors.toCollection(ArrayList::new));

        writer.addPrimitiveArray(new ObjectId(objId), Type.hprofTypeToEnum(elemType), prim);
        tick("A", 10000);
    }

    private void tick(String tick, int size) {
        objectCount++;
        if (objectCount % size == 0) {
            System.out.print(tick);// + "[" + (System.currentTimeMillis() - lastTick)+ "]");
        }
    }

    @Override
    public void objArrayDump(long objId, int stackTraceSerialNum, long elemClassObjId, long[] elems) {
        List<ObjectId> result = Arrays.stream(elems).
                mapToObj(i -> new ObjectId(i)).
                collect(Collectors.toCollection(ArrayList::new));

        writer.addArray(new ShadowObjectArray(new ObjectId(objId), result));
        tick("A", 10000);
    }

    @Override
    public void instanceDump(long objId, int stackTraceSerialNum, long classObjId,
                             Value<?>[] instanceFieldValues) {
        Map<String, Object> fieldmap = Maps.newHashMap();

        if (instanceFieldValues.length > 0) {
            // superclass of Object is 0
            int i = 0;
            long nextClass = classObjId;
            while (nextClass != 0) {
                ClassInfo ci = classMap.get(nextClass);
                nextClass = ci.superClassObjId;
                for (InstanceField field : ci.instanceFields) {
                    Value val = instanceFieldValues[i];
                    Object obj = val.value;
                    if (val.type == Type.OBJ) {
                        obj = new ObjectId((Long) val.value);
                    }

                    fieldmap.put(stringMap.get(field.fieldNameStringId), obj);
                    i++;
                }
            }
            assert i == instanceFieldValues.length;
        }

        OwnClassInfo ci = classMap.get(classObjId);
        ci.instances++;
        
        ShadowObject obj = new ShadowObject(new ClassId(classObjId), new ObjectId(objId), fieldmap);
        writer.addObject(obj);

        tick(".", 50000);
    }

    @Override
    public void classDump(long classObjId, int stackTraceSerialNum, long superClassObjId, long classLoaderObjId,
                          long signersObjId, long protectionDomainObjId, long reserved1, long reserved2,
                          int instanceSize, Constant[] constants, Static[] statics, InstanceField[] instanceFields) {

        try {
            Map<String, Type> fieldmap = new HashMap<>();
            /* TODO:  Solve invalid field names (obfuscators can assign the same name to many times to different types)
                Arrays.stream(instanceFields).
                collect(Collectors.toMap(
                        i -> i.type + stringMap.get(i.fieldNameStringId),
                        i -> i.type));*/

            ShadowClass cz = new ShadowClass(
                    new ClassId(classObjId), new ClassId(superClassObjId),
                    new ObjectId(classLoaderObjId),
                    classNameMap.get(classObjId).replace('/', '.'), instanceSize,
                    fieldmap);

            writer.addClass(cz);

            // store class info in a hashmap for later access
            classMap.put(classObjId, new OwnClassInfo(classObjId, superClassObjId, instanceSize,
                    instanceFields));

            tick("C", 1000);
        } catch (Exception ex) {
            System.out.println("Wrong class: " + classObjId);
            for (InstanceField field : instanceFields) {
                System.out.print("    " + stringMap.get(field.fieldNameStringId));
                System.out.println(" " + field.type);
            }
            throw ex;
        }
    }

    @Override
    public void loadClass(int classSerialNum, long classObjId, int stackTraceSerialNum, long classNameStringId) {
        classNameMap.put(classObjId, stringMap.get(classNameStringId));
    }

    @Override
    public void stringInUTF8(long id, String data) {
        stringMap.put(id, data);
    }

    public static class OwnClassInfo extends ClassInfo {

        public int instances;

        public OwnClassInfo(long classObjId, long superClassObjId, int instanceSize, InstanceField[] instanceFields) {
            super(classObjId, superClassObjId, instanceSize, instanceFields);
        }
    }
}
