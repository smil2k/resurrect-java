/*
 *  Copyright Lufthansa Systems.
 */
package necromancer.data;

import com.google.common.collect.Iterators;
import edu.tufts.eaftan.hprofparser.parser.datastructures.Type;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.*;

@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class ShadowClass {

    @Getter
    private ClassId classId;
    private ClassId superClassId;

    @Getter
    private ObjectId classLoaderId;

    @Getter
    private String className;

    @Getter
    private int instanceSize;

    @Getter
    private int instanceCount;

    // name -> type
    private Map<String, Type> fields = new HashMap<>();

    @Getter
    private ShadowObject statics;

    public Collection<Object> getBackReferences() {
        return ShadowFactory.getInstance().getBackReferences(new ObjectId(classId.getClassId()));
    }

    public Set<ObjectId> getBackReferenceIds() {
        return ShadowFactory.getInstance().getBackReferenceIds(new ObjectId(classId.getClassId()));
    }

    public ShadowClass getSuperType() {
        return ShadowFactory.getInstance().getClass(superClassId);
    }

    public Iterator<Map.Entry<String, Type>> allFields() {
        if (getSuperType() != null) {
            return Iterators.concat(declaredFields(), getSuperType().allFields());
        } else {
            return declaredFields();
        }
    }

    public Iterator<Map.Entry<String, Type>> declaredFields() {
        return fields.entrySet().iterator();
    }
}
