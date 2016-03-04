package Application;

import java.util.Collections;
import java.util.Set;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

/**
 * DO NOT DELETE
 * Replace web.xml
 * @author anais
 *
 */
@ApplicationPath("")
public class BaseApplication extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        return Collections.emptySet();
    }
}