package example;

import java.io.IOException;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

/**
 * A simple application that loads {@code String} beans from a context created by configuration contained within a jar
 * file. The configuration uses relative paths to import other xml files.
 * 
 * This will work with Spring Boot 1.4.1. With Spring Boot 1.5.1 it works when started using maven or from IDE, however
 * it will fail when started standalone.
 * 
 * The issue seems to be different behaviour of sun.net.www.protocol.jar.JarURLConnection.connect.JarURLConnection which
 * is used for loading from files and org.springframework.boot.loader.jar.JarURLConnection.connect.JarURLConnection.java
 * which is used when loaded from Spring Boot fat jars.
 * 
 * Works: java -jar jar-load-example-1.4.1.RELEASE.jar
 * 
 * Fails: java -jar jar-load-example-1.5.1.RELEASE.jar
 * 
 * To create different output jars change dependency in pom.xml
 * 
 * @author Jan-Philipp Kappmeier
 */
@SpringBootApplication
public class JarLoad implements CommandLineRunner {

    @Override
    public void run(String... args) throws IOException {
        String configJar = getConfigUri();

        System.out.println("Expected: a test value");
        String a = loadFrom(configJar, "/config/default/some-config.xml");
        System.out.println(a);

        System.out.println("Expected: another value");
        String c = loadFrom(configJar, "/config/default/another-config.xml");
        System.out.println(c);

        // Will fail when compiled with Spring Boot 1.5.1
        System.out.println("Expected: custom value");
        String b = loadFrom(configJar, "/config/custom/some-config.xml");
        System.out.println(b);
    }

    /**
     * For simplicity the configuration is stored within the resources directory and it will get packed
     * into the jar. The same error also occurs when the configuration jar file is accessed directly on the file
     * system.
     * 
     * @return URI to the file 'configuration.jar' located in the fat jar
     * @throws IOException if loading fails
     */
    private String getConfigUri() throws IOException {
        // The name of the config file we are looking for
        final String configurationFile = "/configuration.jar";

        // Get the URI to the file
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        String resPath = "classpath:" + configurationFile;
        Resource[] rs = resolver.getResources(resPath);
        String originalUri = rs[0].getURI().toString();

        // Remove the ! before /configuration.jar
        return originalUri.substring(0, originalUri.length() - configurationFile.length() - 1) + configurationFile;
    }

    /**
     * Tries to set up an {@code ApplicationContext} from a Spring configuration stored in a jar file.
     * Returns a {@code String} bean called 'test' from the context.
     * 
     * @param configJar the jar file containing the xml configuration
     * @param configName the xml configuration loaded from within the jar file
     * @return the 'test' bean
     */
    private String loadFrom(String configJar, String configName) {
        // This is the URI of the resource we are trying to load
        String configLocation = configJar + "!" + configName;

        // Create an application context
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext();        
        context.setConfigLocation(configLocation);
        context.refresh();

        // Return the test bean
        return (String) context.getBean("test");
    }

    public static void main(String[] args) throws Exception {
        SpringApplication.run(JarLoad.class, args);
    }

};
