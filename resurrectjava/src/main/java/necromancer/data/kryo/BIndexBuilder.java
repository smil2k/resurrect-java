package necromancer.data.kryo;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.koloboke.collect.map.hash.HashLongObjMaps;
import com.koloboke.collect.set.hash.HashLongSets;
import lombok.RequiredArgsConstructor;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.Options;
import org.iq80.leveldb.WriteBatch;
import org.iq80.leveldb.impl.Iq80DBFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import static org.iq80.leveldb.impl.Iq80DBFactory.bytes;

@RequiredArgsConstructor
public class BIndexBuilder extends Thread {

    private final File brefFile;
    private final File bindexFile;

    private final Consumer<DB> ready;
    private DB bindex;

    @Override
    public void run() {
        try {
            System.out.println("Starting backref index...");
            Kryo kryo = NecromancerKryo.getInstance();

            Options options = new Options();
            options.createIfMissing(true);

            bindex = Iq80DBFactory.factory.open(bindexFile, options);

            Set<Long> processed = HashLongSets.newMutableSet();
            long filepos = 0;
            int pass = 0;
            for (;;) {
                Map<Long, Set<Long>> processing = HashLongObjMaps.newMutableMap();
                boolean firstMiss = false;
                pass++;
                System.out.print("Pass " + pass + " from " + filepos + " done " + processed.size());
                long start = System.currentTimeMillis();
                FileInputStream fis = new FileInputStream(brefFile);
                fis.skip(filepos);

                try (Input input = new Input(fis)) {
                    while (input.eof() == false) {
                        long beforeObject = input.total();
                        TwoLong s = kryo.readObject(input, TwoLong.class);

                        if (processed.contains(s.getKey())) {
                            continue;
                        }

                        if (processing.containsKey(s.getKey()) == false) {
                            if (processing.size() < 2000000) {
                                processing.put(s.getKey(), HashLongSets.newMutableSet());
                            } else if (firstMiss == false) {
                                // Mark the first skipped entry.
                                filepos += beforeObject;
                                firstMiss = true;
                            }
                        }

                        Set<Long> set = processing.get(s.getKey());
                        if (set != null) {
                            set.add(s.getValue());
                        }
                    }

                    WriteBatch batch = bindex.createWriteBatch();
                    processed.addAll(processing.keySet());
                    for (Map.Entry<Long, Set<Long>> data : processing.entrySet()) {
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        Output out = new Output(baos);
                        out.writeInt(data.getValue().size());
                        for (Long aLong : data.getValue()) {
                            out.writeLong(aLong);
                        }
                        out.close();

                        batch.put(bytes(Long.toHexString(data.getKey())), baos.toByteArray());
                    }
                    bindex.write(batch);

                    System.out.println(" took " + (System.currentTimeMillis() - start) + " ms ");

                    if (processing.isEmpty()) {
                        ready.accept(bindex);
                        return;
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
            try {
                Iq80DBFactory.factory.destroy(bindexFile, null);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }
}
