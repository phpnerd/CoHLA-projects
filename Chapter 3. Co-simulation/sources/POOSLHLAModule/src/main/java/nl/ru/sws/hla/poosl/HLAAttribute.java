
package nl.ru.sws.hla.poosl;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Created by thomas on 6-4-16.
 */
public class HLAAttribute<T> {

    private final String name;
    private boolean isSynced;
    private final Class<T> clazz;
    private final boolean doWrite;
    private final Consumer<T> setter;
    private final Supplier<T> getter;

    private HLAAttribute(String name, Class<T> clazz, boolean doWrite, Consumer<T> setter, Supplier<T> getter) {
        this.name = name;
        this.clazz = clazz;
        this.doWrite = doWrite;
        this.setter = setter;
        this.getter = getter;
        isSynced = true;

    }

    public HLAAttribute(String name, Class<T> clazz, Consumer<T> setter) {
        this(name, clazz, false, setter, null);
    }

    public HLAAttribute(String name, Class<T> clazz, Supplier<T> getter) {
        this(name, clazz, true, null, getter);
    }

    public String getName() {
        return name;
    }

    public boolean isSynced() {
        return isSynced;
    }

    public void setSynced(boolean synced) {
        isSynced = synced;
    }

    public Class getClazz() {
        return clazz;
    }

    public boolean doWrite() {
        return doWrite;
    }

    public Consumer<T> getSetter() {
        return setter;
    }

    public Supplier<T> getGetter() {
        return getter;
    }

}
