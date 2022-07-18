package com.mastfrog.jarmerge.moduleinfo;

import com.mastfrog.jarmerge.JarMerge;
import static com.mastfrog.jarmerge.moduleinfo.ModuleInfoSynthesizer.PROP_MODULE_NAME;
import static com.mastfrog.jarmerge.moduleinfo.ModuleInfoSynthesizer.PROP_OPEN_MODULE;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Paths;
import static java.util.Collections.singletonMap;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author Tim Boudreau
 */
public class ModuleInfoSynthesizerTest {

    @Test
    public void testSomeMethod() throws Exception {
        if (true) {
            // PENDING - move test projects under this project and set them up as
            // test dependencies so they get built first, but ensure they do not
            // wind up deployed to maven central
            return;
        }

        Path tmpJar = Paths.get(System.getProperty("java.io.tmpdir")).resolve("wookies-" + System.currentTimeMillis() + ".jar");

        Path a = Paths.get("/home/tim/work/trivial/modularthing/target/modularthing-1.0-SNAPSHOT.jar");
        Path b = Paths.get("/home/tim/work/trivial/othermodularthing/target/othermodularthing-1.0-SNAPSHOT.jar");
        Path c = Paths.get("/home/tim/work/trivial/nonmodularthing/target/nonmodularthing-1.0-SNAPSHOT.jar");

        JarMerge merge = JarMerge.builder().addJar(a).addJar(b).addJar(c)
                .omitMavenMetadata()
                .withExtensionProperties(singletonMap(PROP_OPEN_MODULE, "false"))
                .withExtensionProperty(PROP_MODULE_NAME, "wishful.thinking")
                .verbose()
                .finalJarName(tmpJar.toString());
        merge.run();

        URLClassLoader ldr = new URLClassLoader(new URL[]{tmpJar.toUri().toURL()});
        Class<?> cl = ldr.loadClass("com.mastfrog.modularthing.Modularthing");
        System.out.println("CL " + cl);
        Module mod = cl.getModule();
        System.out.println("MOD " + mod);
    }
}
