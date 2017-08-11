/*
 *  Copyright Lufthansa Systems.
 */
package necromancer.data.kryo;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.google.common.collect.Lists;
import edu.tufts.eaftan.hprofparser.parser.datastructures.Type;
import necromancer.data.*;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.*;

public class FileOperationsTest {

    @Test
    public void testKryo() {
        Map<String, Type> fields = new HashMap<>();
        fields.put("some", Type.BYTE);

        runKryo(new ShadowClass(new ClassId(8), null, null, "clazz", 2, 12, fields, null));

        Map<String, Object> f = new HashMap<>();
        f.put("some", (byte) 1);

        ShadowObject so = new ShadowObject(new ClassId(8), new ObjectId(1), f);

        runKryo(so);
    }

    private void runKryo(Object obj) {
        Kryo kryo = NecromancerKryo.getInstance();
//        Object v = kryo.copy(obj);
        //   Assert.assertEquals(obj, v);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Output o = new Output(baos);

        kryo.writeObject(o, obj);
        o.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        Input input = new Input(bais);
        Object v2 = kryo.readObject(input, obj.getClass());

        Assert.assertEquals(obj, v2);
    }

    @Test
    public void testSimpleRW() throws Exception {
        File tmp = new File(System.getProperty("java.io.tmpdir"));

        File tmpDir = new File(tmp, "hprof");
        FileUtils.deleteDirectory(tmpDir);
        tmpDir.mkdirs();

        KryoFileWriter kf = new KryoFileWriter(tmpDir);

        Map<String, Type> fields = new HashMap<>();
        fields.put("some", Type.BYTE);
        ShadowClass type = new ShadowClass(new ClassId(8), null, null, "clazz", 12, 24, fields, null);

        Map<String, Object> f = new HashMap<>();
        f.put("some", (byte) 1);

        ShadowObject so = new ShadowObject(new ClassId(8), new ObjectId(1), f);

        kf.addClass(type);
        kf.addObject(so);
        kf.addPrimitiveArray(new ObjectId(2), Type.BYTE, Collections.singleton((byte) 1));
        kf.addArray(new ShadowObjectArray(new ObjectId(3), Lists.newArrayList(new ObjectId(8), new ObjectId(2))));

        kf.close();

        KryoReadonlyShadowFactory sf = new KryoReadonlyShadowFactory(tmpDir);
        ShadowFactory.setInstance(sf);

        Collection<Object> objList = sf.findAll("clazz");

        ShadowObject b = (ShadowObject) objList.iterator().next();
        Assert.assertEquals((byte) 1, b.get("some"));

        Collection c = (Collection) sf.getObject(new ObjectId(2));
        Assert.assertEquals((byte) 1, c.iterator().next());

        Iterator<Object> oi = ((Collection) sf.getObject(new ObjectId(3))).iterator();
        Assert.assertEquals(type, oi.next());
        Assert.assertEquals(c, oi.next());
    }

    @Test
    public void testJavascriptBindings() throws Exception {
        Map<String, Type> fields = new HashMap<>();
        fields.put("some", Type.BYTE);
        ShadowClass type = new ShadowClass(new ClassId(8), null, null, "clazz", 12, 34, fields, null);

        Map<String, Object> f = new HashMap<>();
        f.put("some", (byte) 1);

        ShadowObject so = new ShadowObject(new ClassId(8), new ObjectId(1), f);

        ScriptEngineManager factory = new ScriptEngineManager();
        ScriptEngine engine = factory.getEngineByName("JavaScript");
        engine.put("obj", so);
        Assert.assertEquals((byte)1, engine.eval("obj.some"));

    }
}
