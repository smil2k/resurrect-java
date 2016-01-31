/*
 *  Copyright Lufthansa Systems.
 */
package necromancer.data.kryo;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.Channels;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.stream.Collectors;
import necromancer.data.ClassId;
import necromancer.data.ObjectId;
import necromancer.data.ShadowClass;
import necromancer.data.ShadowFactory.ShadowFactorySPI;
import necromancer.data.ShadowObjectArray;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.Options;

import static org.iq80.leveldb.impl.Iq80DBFactory.asString;
import static org.iq80.leveldb.impl.Iq80DBFactory.bytes;
import static org.iq80.leveldb.impl.Iq80DBFactory.factory;

public class KryoReadonlyShadowFactory implements ShadowFactorySPI {

    private static final Object NULL = new Object();

    private Map<ClassId, ShadowClass> loadedClasses = new HashMap<>();
    private Map<String, ShadowClass> types = new HashMap();

    private File bref;
    private File cgroup;

    private DB oindex;
    

    private LoadingCache<ClassId, Set<ObjectId>> objectsByClass
            = CacheBuilder.newBuilder().
            maximumSize(100000).
            build(new CacheLoader<ClassId, Set<ObjectId>>() {
                @Override
                public synchronized Set<ObjectId> load(ClassId k) throws Exception {
                    Kryo kryo = NecromancerKryo.getInstance();
                    Input cgInput = new Input(new FileInputStream(cgroup));
                    Set<ObjectId> result = new HashSet<>();
                    while (cgInput.eof() == false) {
                        TwoLong tl = kryo.readObject(cgInput, TwoLong.class);
                        if (tl.getKey() == k.getClassId()) {
                            result.add(new ObjectId(tl.getValue()));
                        }
                    }

                    cgInput.close();

                    return result;
                }

            });

    private Function<Object, Object> resolver;

    private LoadingCache<ObjectId, Object> objectCache
            = CacheBuilder.newBuilder().
            maximumSize(100000).
            build(new CacheLoader<ObjectId, Object>() {
                @Override
                public synchronized Object load(ObjectId k) throws Exception {
                    if (k == null || k.getObjectId() == 0) {
                        return NULL;
                    }

                    String offsetHex = asString(oindex.get(bytes(Long.toHexString(k.getObjectId()))));
                    if (offsetHex == null) {
                        throw new IllegalStateException(k.getObjectId() + " has no offset.");
                    }

                    long offset = Long.parseLong(offsetHex, 16);

                    objectStore.seek(offset);
                    Kryo kryo = NecromancerKryo.getInstance();

                    Input input = new Input(Channels.newInputStream(objectStore.getChannel()));

                    Object o = kryo.readClassAndObject(input);
                    return o;
                }
            });

    private final RandomAccessFile objectStore;

    public KryoReadonlyShadowFactory(File dbdir) throws IOException {
        objectStore = new RandomAccessFile(new File(dbdir, "objects.db"), "r");

        loadClassIndex(new File(dbdir, "cindex.db"));

        bref = new File(dbdir, "bref.db");
        cgroup = new File(dbdir, "cgroup.db");
        
        Options options = new Options();
        options.createIfMissing(true);
        oindex = factory.open(new File(dbdir, "oindex.db"), options);
    }

    @Override
    public ShadowClass getClassByName(String type) {
        ShadowClass s = types.get(type);
        if (s == null) {
            throw new IllegalArgumentException("Class not found " + type);
        }
        return s;
    }

    public ShadowClass getClass(ClassId type) {
        ShadowClass s = loadedClasses.get(type);
        if (s == null) {
            throw new IllegalArgumentException("Class not found " + type);
        }

        return s;
    }

    public Object getObject(ObjectId id) {
        try {
            Object o = objectCache.get(id);
            if (o == NULL) {
                return null;
            }
            if (resolver != null) {
                o = resolver.apply(o);
            }
            return o;
        } catch (ExecutionException ex) {
            throw new IllegalStateException(ex);
        }
    }

    public Collection<Object> findAll(String name) {
        try {
            ShadowClass type = types.get(name);
            if (type == null) {
                throw new IllegalArgumentException("Class not found : " + name);
            }

            Set<ObjectId> list = (Set<ObjectId>) objectsByClass.get(type.getClassId());
            return new ShadowObjectArray(null, new ArrayList(list));
        } catch (ExecutionException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private void loadClassIndex(File file) throws FileNotFoundException {
        System.out.print("Loading class data...");
        int c = 0;
        try (Input input = new Input(new FileInputStream(file))) {

            Kryo kryo = NecromancerKryo.getInstance();
            while (input.eof() == false) {
                ShadowClass s = kryo.readObject(input, ShadowClass.class);
                types.put(s.getClassName(), s);
                loadedClasses.put(s.getClassId(), s);

                if (c % 100 == 0) {
                    System.out.print(".");
                }
                c++;
            }
        }
        System.out.println();
    }

    @Override
    public Collection<String> grepClassName(String name) {
        return types.keySet().stream().
                filter(s -> s.contains(name)).
                collect(Collectors.toCollection(TreeSet::new));
    }

    @Override
    public void setObjectResolver(Function f) {
        resolver = f;
    }

    @Override
    public void close() throws IOException{
        oindex.close();
    }

}
