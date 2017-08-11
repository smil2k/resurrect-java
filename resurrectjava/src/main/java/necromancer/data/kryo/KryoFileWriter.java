/*
 *  Copyright Lufthansa Systems.
 */
package necromancer.data.kryo;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Output;
import edu.tufts.eaftan.hprofparser.parser.datastructures.Type;
import necromancer.data.ObjectId;
import necromancer.data.ShadowClass;
import necromancer.data.ShadowObject;
import necromancer.data.ShadowObjectArray;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.Options;
import org.iq80.leveldb.impl.Iq80DBFactory;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import static org.iq80.leveldb.impl.Iq80DBFactory.bytes;

public class KryoFileWriter implements Closeable {

    private Output cindex;
    private DB oindex;

    private Output bref;
    private Output cgroup;

    private Output objectStore;

    private Kryo kryo;

    public KryoFileWriter(File file) throws IOException {
        file.mkdirs();
        cindex = new Output(new FileOutputStream(new File(file, "cindex.db")));
        cgroup = new Output(new FileOutputStream(new File(file, "cgroup.db")));
        bref = new Output(new FileOutputStream(new File(file, "bref.db")));
        objectStore = new Output(new FileOutputStream(new File(file, "objects.db")));

        Options options = new Options();
        options.createIfMissing(true);

        oindex = Iq80DBFactory.factory.open(new File(file, "oindex.db"), options);

        kryo = NecromancerKryo.getInstance();
    }

    public void setTimestamp(long taken) {
        oindex.put(bytes("now"), bytes(Long.toHexString(taken)));
    }

    public void addObject(ShadowObject obj) {
        addKryoObject(obj.getObjectId(), obj);

        addObjectToBRef(obj);

        kryo.writeObject(cgroup,
            new TwoLong(obj.getClassId().getClassId(), obj.getObjectId().getObjectId()));
    }

    private void addObjectToBRef(ShadowObject obj) {
        for (Object value : obj.getFields().values()) {
            if (value instanceof ObjectId ) {
                long target = ((ObjectId) value).getObjectId();
                // Skip null values
                if (target != 0) {
                    kryo.writeObject(bref,
                        new TwoLong(target, obj.getObjectId().getObjectId()));
                }
            }
        }
    }

    public void addArray(ShadowObjectArray obj) {
        addKryoObject(obj.getObjectId(), obj);

        for (ObjectId object : obj.getObjectIdArray()) {
            if ( object.getObjectId() != 0 ) {
                kryo.writeObject(bref,
                                 new TwoLong(object.getObjectId(), obj.getObjectId().getObjectId()));
            }
        }
    }

    public void addPrimitiveArray(ObjectId id, Type type, Collection<?> list) {
        if (type == Type.CHAR) {
            StringBuilder s = new StringBuilder(list.size());
            list.stream().forEach((chr) -> {
                s.append((char) (Character) chr);
            });
            addKryoObject(id, s.toString());
        } else {
            addKryoObject(id, new ArrayList(list));
        }
    }

    private void addKryoObject(ObjectId id, Object obj) {
        oindex.put(bytes(Long.toHexString(id.getObjectId())), bytes(Long.toHexString(objectStore.total())));
        kryo.writeClassAndObject(objectStore, obj);
    }

    public void addClass(ShadowClass type) {
        addKryoObject(new ObjectId(type.getClassId().getClassId()), type);
        kryo.writeObject(cindex, type);

        if ( type.getStatics() != null ) {
            addObjectToBRef(type.getStatics());
        }
    }

    @Override
    public void close() throws IOException {
        cindex.close();
        oindex.close();
        objectStore.close();
        bref.close();
        cgroup.close();

        objectStore = null;
        oindex = null;
        cindex = null;
        bref = null;
        cgroup = null;
    }

}
