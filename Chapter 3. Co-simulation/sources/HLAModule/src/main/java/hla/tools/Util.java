package hla.tools;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;

/**
 * Created by thomas on 31-3-16.
 */
public class Util {

    public static void waitForUser() {
        System.out.print(" >>>>>>>>>>>>>>> Press [Enter] to continue <<<<<<<<<<<<<<<\n");
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        try {
            br.readLine();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static<T> T convert(Object o, Class<T> clazz) {
        if (!(o instanceof String)) {
            if (clazz == o.getClass())
                return (T) o;
            else
                return clazz.cast(o);
        } else {
            if (clazz == Double.class)
                return clazz.cast(Double.parseDouble((String)o));
            else if (clazz == Integer.class)
                return clazz.cast(Integer.parseInt((String)o));
            else if (clazz == Float.class)
                return clazz.cast(Float.parseFloat((String)o));
            else if (clazz == Boolean.class)
                return clazz.cast(Boolean.parseBoolean((String)o));
            else
                return clazz.cast(o);
        }
    }

    public static PrintStream createLoggingProxy(final Logger logger, final PrintStream realPrintStream, final Level level) {
        return new PrintStream(realPrintStream) {
            private final boolean consolePrint = level.isMoreSpecificThan(Level.WARN);
            public void print(final String string) {
                realPrintStream.print(string);
                logger.log(level, string);
            }

            public void println(final String string) {
                if (consolePrint)
                    realPrintStream.print(string + "\n");
                logger.log(level, string);
            }
        };
    }
}
