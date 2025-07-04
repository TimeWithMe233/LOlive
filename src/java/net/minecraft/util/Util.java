package net.minecraft.util;

import org.apache.logging.log4j.Logger;
import dev.olive.event.impl.EventCancelledException;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

public class Util {
    public static Util.EnumOS getOSType() {
        String s = System.getProperty("os.name").toLowerCase();
        return s.contains("win") ? Util.EnumOS.WINDOWS : (s.contains("mac") ? Util.EnumOS.OSX : (s.contains("solaris") ? Util.EnumOS.SOLARIS : (s.contains("sunos") ? Util.EnumOS.SOLARIS : (s.contains("linux") ? Util.EnumOS.LINUX : (s.contains("unix") ? Util.EnumOS.LINUX : Util.EnumOS.UNKNOWN)))));
    }

    public static <V> V runTask(FutureTask<V> task, Logger logger) {
        try {
            task.run();
            return task.get();
        } catch (ExecutionException executionexception) {
            if (!(executionexception.getCause() instanceof EventCancelledException)) {
                logger.fatal((String) "Error executing task", (Throwable) executionexception);
//                executionexception.getCause().printStackTrace();

                if (executionexception.getCause() instanceof OutOfMemoryError) {
                    OutOfMemoryError outofmemoryerror = (OutOfMemoryError) executionexception.getCause();
                    throw outofmemoryerror;
                }
            }
        } catch (InterruptedException interruptedexception) {
            logger.fatal((String) "Error executing task", (Throwable) interruptedexception);
        }

        return (V) ((Object) null);
    }

    public static enum EnumOS {
        LINUX,
        SOLARIS,
        WINDOWS,
        OSX,
        UNKNOWN;
    }
}
