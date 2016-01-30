/*
 *  Copyright Lufthansa Systems.
 */
package necromancer.data;

import java.util.Collection;
import java.util.function.Function;
import lombok.Getter;
import lombok.Setter;

public class ShadowFactory {

    @Getter @Setter
    private static ShadowFactorySPI instance = null;
    
    public interface ShadowFactorySPI {
        ShadowClass getClass(ClassId type);
        ShadowClass getClassByName(String type);
        Object getObject(ObjectId id);
        Collection<Object> findAll(String name);
        Collection<String> grepClassName(String name);
        
        void setObjectResolver(Function f);
    }
}
