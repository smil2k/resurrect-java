/*
 *  Copyright Lufthansa Systems.
 */
package necromancer.data.kryo;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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

public class KryoReadonlyShadowFactory implements ShadowFactorySPI {

    private static final Object NULL = new Object();

    private Map<ClassId, ShadowClass> loadedClasses = new HashMap<>();
    private Map<String, ShadowClass> types = new HashMap();

    private Map<ObjectId, KryoObject> objects = new HashMap<>();
    private Multimap<ClassId, ObjectId> objectsByClass
            = Multimaps.newSetMultimap(new HashMap<ClassId, Collection<ObjectId>>(), HashSet::new);

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

                    KryoObject obj = getKryoObject(k);

                    objectStore.seek(obj.getOffset());
                    Kryo kryo = NecromancerKryo.getInstance();

                    Input input = new Input(Channels.newInputStream(objectStore.getChannel()));

                    Object o = kryo.readObject(input, obj.getClassForPersistence());
                    return o;
                }
            });

    private final RandomAccessFile objectStore;

    public KryoReadonlyShadowFactory(File dbdir) throws FileNotFoundException {
        objectStore = new RandomAccessFile(new File(dbdir, "objects.db"), "r");

        loadClassIndex(new File(dbdir, "cindex.db"));
        loadObjectIndex(new File(dbdir, "oindex.db"));
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

    private KryoObject getKryoObject(ObjectId id) throws IllegalArgumentException {
        KryoObject obj = objects.get(id);
        if (obj == null) {
            throw new IllegalArgumentException("Class not found " + id);
        }

        return obj;
    }

    public Collection<Object> findAll(String name) {
        ShadowClass type = types.get(name);
        if (type == null) {
            throw new IllegalArgumentException("Class not found : " + name);
        }

        Set<ObjectId> list = (Set<ObjectId>) objectsByClass.get(type.getClassId());
        return new ShadowObjectArray(null, new ArrayList(list));
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

    private void loadObjectIndex(File file) throws FileNotFoundException {
        System.out.print("Loading object data...");
        int c = 0;
        try (Input input = new Input(new FileInputStream(file))) {
            Kryo kryo = NecromancerKryo.getInstance();
            while (input.eof() == false) {
                KryoObject s = kryo.readObject(input, KryoObject.class);
                objects.put(s.getObjectId(), s);
                objectsByClass.put(s.getClassId(), s.getObjectId());
                if (c % 100000 == 0) {
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

}
