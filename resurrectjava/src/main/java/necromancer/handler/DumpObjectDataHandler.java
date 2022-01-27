/*
 *  Copyright Lufthansa Systems.
 */
package necromancer.handler;

import com.google.common.collect.Maps;
import edu.tufts.eaftan.hprofparser.handler.NullRecordHandler;
import edu.tufts.eaftan.hprofparser.parser.datastructures.*;
import necromancer.data.*;
import necromancer.data.kryo.KryoFileWriter;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.*;
import java.util.stream.Collectors;

public class DumpObjectDataHandler extends NullRecordHandler {

    private int objectCount;

    private Map<Long, String> stringMap = new HashMap<>();
    private Map<Long, String> classNameMap = new HashMap<>();
    private Map<Long, OwnClassInfo> classMap = new HashMap<>();

    private long stringClass;
    
    private KryoFileWriter writer;

    public DumpObjectDataHandler(File dir) throws IOException {
        writer = new KryoFileWriter(dir);
    }

    @Override
    public void header(String format, int idSize, long time) {
        writer.setTimestamp(time);
    }

    @Override
    public void finished() {
        if (writer != null) {
            try {
                for (OwnClassInfo value : classMap.values()) {

                    Map<String, Type> fieldmap = new HashMap<>();
                    /* TODO:  Solve invalid field names (obfuscators can assign the same name to many times to different types)
                Arrays.stream(instanceFields).
                collect(Collectors.toMap(
                        i -> i.type + stringMap.get(i.fieldNameStringId),
                        i -> i.type));*/

                    Map<String, Object> staticsMap = new HashMap<>();
                    for (Static aStatic : value.statics) {
                        Object obj = extractObjectFromValue(aStatic.value);
                        staticsMap.put(stringMap.get(aStatic.staticFieldNameStringId), obj);
                    }

                    ShadowObject statics=new ShadowObject(new ClassId(value.classObjId), new ObjectId(value.classObjId), staticsMap);

                    ShadowClass cz = new ShadowClass(
                            new ClassId(value.classObjId), new ClassId(value.superClassObjId),
                            new ObjectId(value.classLoaderObjId),
                            classNameMap.get(value.classObjId).replace('/', '.'), value.instanceSize, value.instances,
                            fieldmap, statics);

                    writer.addClass(cz);
                }

                writer.close();
                writer = null;
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
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
        writer.addArray(new ShadowObjectArray(new ObjectId(objId), elems));
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
                    Object obj = extractObjectFromValue(instanceFieldValues[i]);
                    String fieldName = stringMap.get(field.fieldNameStringId);
                    fieldmap.put(fieldName, obj);
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
    
    private Object extractObjectFromValue(Value<?> instanceFieldValue) {
        Value val = instanceFieldValue;
        Object obj = val.value;
        if (val.type == Type.OBJ) {
            obj = new ObjectId((Long) val.value);
        }
        return obj;
    }

    @Override
    public void classDump(long classObjId, int stackTraceSerialNum, long superClassObjId, long classLoaderObjId,
                          long signersObjId, long protectionDomainObjId, long reserved1, long reserved2,
                          int instanceSize, Constant[] constants, Static[] statics, InstanceField[] instanceFields) {

        try {
            // store class info in a hashmap for later access
            classMap.put(classObjId, new OwnClassInfo(classLoaderObjId, classObjId, superClassObjId, instanceSize,
                    instanceFields, statics));

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
        String className = stringMap.get(classNameStringId);
        
        classNameMap.put(classObjId, className);
    }

    @Override
    public void stringInUTF8(long id, String data) {
        stringMap.put(id, data);
    }

    public static class OwnClassInfo extends ClassInfo {

        public int instances;
        public final long classLoaderObjId;
        public final Static[] statics;

        public OwnClassInfo(long classLoaderObjId, long classObjId, long superClassObjId, int instanceSize,
                            InstanceField[] instanceFields, Static[] statics) {
            super(classObjId, superClassObjId, instanceSize, instanceFields);
            this.classLoaderObjId = classLoaderObjId;
            this.statics = statics;
        }

    }
}
