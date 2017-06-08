/*
 *  Copyright Lufthansa Systems.
 */

package necromancer.handler;

import edu.tufts.eaftan.hprofparser.handler.NullRecordHandler;


public class StacktracePrintHandler extends NullRecordHandler{

    @Override
    public void finished() {
    }

    @Override
    public void controlSettings(int bitMaskFlags, short stackTraceDepth) {
    }

    @Override
    public void endThread(int threadSerialNum) {
    }

    @Override
    public void startThread(int threadSerialNum, long threadObjectId, int stackTraceSerialNum, long threadNameStringId,
                            long threadGroupNameId, long threadParentGroupNameId) {
    }

    @Override
    public void stackTrace(int stackTraceSerialNum, int threadSerialNum, int numFrames, long[] stackFrameIds) {
    }

    @Override
    public void stackFrame(long stackFrameId, long methodNameStringId, long methodSigStringId,
                           long sourceFileNameStringId, int classSerialNum, int location) {
    }
    

}
