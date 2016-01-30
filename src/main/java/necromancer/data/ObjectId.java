/*
 *  Copyright Lufthansa Systems.
 */

package necromancer.data;

import java.io.Serializable;
import lombok.Value;


@Value
public class ObjectId implements Serializable  {
    private long objectId;
}
