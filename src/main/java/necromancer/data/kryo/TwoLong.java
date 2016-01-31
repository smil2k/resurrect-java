/*
 *  Copyright Lufthansa Systems.
 */

package necromancer.data.kryo;

import lombok.Value;


@Value
public class TwoLong {
    private long key;
    private long value;
}
