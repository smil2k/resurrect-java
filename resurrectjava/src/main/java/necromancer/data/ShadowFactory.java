/*
 *  Copyright Lufthansa Systems.
 */
package necromancer.data;

import lombok.Getter;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.function.Function;

public class ShadowFactory {

    @Getter
    private static ShadowFactorySPI instance = null;

    public static void setInstance(ShadowFactorySPI i) {
        if (instance != null )  {
            try {
                instance.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        instance = i;
    }

    public interface ShadowFactorySPI {

        Date getSnapshotTime();
        int getObjectCount();
        int getArrayCount();

        ShadowClass getClass(ClassId type);

        ShadowClass getClassByName(String type);

        List<ObjectId> getBackReferenceIds(ObjectId obj);

        List<Object> getBackReferences(ObjectId obj);

        Object getObject(ObjectId id);
        Object getRawObject(ObjectId id);

        Collection<Object> findAll(String name);

        Collection<String> grepClassName(String name);

        void setObjectResolver(Function f);

        void close() throws IOException;
    }
}
