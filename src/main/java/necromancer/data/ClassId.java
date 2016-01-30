/*
 *  Copyright Lufthansa Systems.
 */

package necromancer.data;

import java.io.Serializable;
import lombok.Value;


@Value
public class ClassId implements Serializable  {
    private long classId;
}
