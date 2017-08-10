/*
 *  Copyright Lufthansa Systems.
 */
package necromancer.data;

import lombok.Getter;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.Set;
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
        
        ShadowClass getClass(ClassId type);

        ShadowClass getClassByName(String type);

        Set<ObjectId> getBackReferenceIds(ObjectId obj);

        Collection<Object> getBackReferences(ObjectId obj);

        Object getObject(ObjectId id);

        Collection<Object> findAll(String name);

        Collection<String> grepClassName(String name);

        void setObjectResolver(Function f);

        void close() throws IOException;
    }
}
