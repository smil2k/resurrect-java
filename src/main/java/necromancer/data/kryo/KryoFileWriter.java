/*
 *  Copyright Lufthansa Systems.
 */
package necromancer.data.kryo;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Output;
import edu.tufts.eaftan.hprofparser.parser.datastructures.Type;
import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import necromancer.data.ObjectId;
import necromancer.data.ShadowClass;
import necromancer.data.ShadowObject;
import necromancer.data.ShadowObjectArray;

public class KryoFileWriter implements Closeable {

    private Output cindex;
    private Output oindex;

    private Output objectStore;

    private Kryo kryo;

    public KryoFileWriter(File file) throws FileNotFoundException {
        file.mkdirs();
        cindex = new Output(new FileOutputStream(new File(file, "cindex.db")));
        oindex = new Output(new FileOutputStream(new File(file, "oindex.db")));
        objectStore = new Output(new FileOutputStream(new File(file, "objects.db")));

        kryo = NecromancerKryo.getInstance();
    }

    public void addObject(ShadowObject obj) {
        KryoObject kobj = new KryoObject(obj.getClassId(), obj.getObjectId(), false, Type.OBJ, objectStore.total());
        addKryoObject(kobj, obj);
    }

    public void addArray(ShadowObjectArray obj) {
        KryoObject kobj = new KryoObject(null, obj.getObjectId(), true, Type.OBJ, objectStore.total());
        addKryoObject(kobj, obj);
    }

    public void addPrimitiveArray(ObjectId id, Type type, Collection<?> list) {
        KryoObject obj = new KryoObject(null, id, true, type, objectStore.total());

        if (type == Type.CHAR) {
            StringBuilder s = new StringBuilder(list.size());
            list.stream().forEach((chr) -> {
                        s.append((char)(Character)chr);
            });
            addKryoObject(obj, s.toString());
        } else {
            addKryoObject(obj, new ArrayList(list));
        }
    }

    private void addKryoObject(KryoObject kobj, Object obj) {
        kryo.writeObject(objectStore, obj);
        kryo.writeObject(oindex, kobj);

    }

    public void addClass(ShadowClass type) {
        KryoObject kobj = new KryoObject(null, new ObjectId(type.getClassId().getClassId()), false,
                Type.OBJ, objectStore.total());
        addKryoObject(kobj, type);
        kryo.writeObject(cindex, type);
    }

    @Override
    public void close() throws IOException {
        cindex.close();
        oindex.close();
        objectStore.close();
    }
}
