/*
 *  Copyright Lufthansa Systems.
 */
package necromancer;

import edu.tufts.eaftan.hprofparser.parser.HprofParser;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import jline.TerminalFactory;
import jline.console.ConsoleReader;
import jline.console.completer.StringsCompleter;
import jline.console.history.FileHistory;
import jline.console.history.History;
import jline.console.history.PersistentHistory;
import necromancer.data.ShadowFactory;
import necromancer.data.ShadowFactory.ShadowFactorySPI;
import necromancer.data.kryo.KryoReadonlyShadowFactory;
import necromancer.handler.DumpObjectDataHandler;
import org.apache.commons.io.FileUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

/**
 *
 * @author smil
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        try {
            Main m = new Main();
            String userdir = System.getProperty("user.home", System.getProperty("java.io.tmpdir"));

            ConsoleReader reader = new ConsoleReader();
            reader.setHistory(new FileHistory(new File(userdir + "/.rjhistory")));

            reader.setPrompt("resurrect java> ");

            m.prompt(reader);
            System.out.println("Exit\nbye...");
        } finally {
            TerminalFactory.get().restore();
        }
    }

    private ScriptEngine engine;

    private void prompt(ConsoleReader reader) throws IOException {
        reader.addCompleter(new StringsCompleter("load ", "eval ", "loadlibrary ", "grepclass ", "source ",
                "rebuildcache ", "reset", "exit"));

        while (true) {

            String line = reader.readLine();
            if (line == null) {
                break;
            }

            History h = reader.getHistory();
            if (h instanceof PersistentHistory) {
                ((PersistentHistory) h).flush();
            }

            try {
                if (line.startsWith("load ")) {
                    loadFile(line.substring(5));
                } else if (line.startsWith("eval ")) {
                    eval(line.substring(5));
                } else if (line.startsWith("loadlibrary ")) {
                    evalScript(new FileReader(line.substring(12)));
                } else if (line.startsWith("grepclass ")) {
                    grep(line.substring(10));
                } else if (line.startsWith(". ")) {
                    importScript(line.substring(2));
                } else if (line.startsWith("source ")) {
                    importScript(line.substring(7));
                } else if (line.startsWith("rebuildcache ")) {
                    createCache(line.substring(13));
                } else if (line.equals("reset")) {
                    newEngine();
                    loadJsLibrary();
                } else if (line.startsWith("exit")) {
                    break;
                }
            } catch (IOException ex) {
                System.out.println("IO error:" + ex.getMessage());
            }
        }
    }

    private void loadFile(String file) throws IOException {
        file = file.trim();
        
        System.out.println("Loading hprof " + file);
        File hprof = new File(file);
        if (hprof.exists() == false || hprof.isDirectory()) {
            throw new IOException("File must exists and should be a regular file!");
        }

        File dbdir = new File(file + ".cache");
        if (dbdir.exists()) {
            if (dbdir.isFile()) {
                throw new IOException("Cache directory " + dbdir + "cannot be created due to existing file.");
            }
        } else {
            createCache(file);
        }

        KryoReadonlyShadowFactory factory = new KryoReadonlyShadowFactory(dbdir);
        ShadowFactory.setInstance(factory);
        newEngine();
        loadJsLibrary();
    }

    private void createCache(String hprofFile) throws IOException {
        hprofFile = hprofFile.trim();
        File dbdir = new File(hprofFile + ".cache");
        FileUtils.deleteDirectory(dbdir);

        HprofParser parser = new HprofParser(new DumpObjectDataHandler(dbdir));
        parser.parse(new File(hprofFile));
        System.out.println();
        loadFile(hprofFile);
    }

    private void newEngine() throws IOException {
        assertFactory();
        ScriptEngineManager factory = new ScriptEngineManager();
        engine = factory.getEngineByName("JavaScript");

        engine.put("factory", ShadowFactory.getInstance());
        engine.put("engine", new EngineShadow());
    }

    private void loadJsLibrary() throws IOException {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        for (Resource r : resolver.getResources("classpath:/library/*.js")) {
            Reader rd = new InputStreamReader(r.getInputStream(), "UTF-8");
            System.out.println("Loading lib " + r.getFilename());
            evalScript(rd);
        }
    }

    private void evalScript(Reader rd) throws IOException {
        assertFactory();
        try {
            engine.eval(rd);
        } catch (ScriptException ex) {
            System.out.println("Parse error: " + ex.getMessage());
        } catch (Exception ex) {
            System.out.println("RT error: ");
            ex.printStackTrace();
        }
    }

    private void importScript(String script) throws IOException {
        ConsoleReader r = new ConsoleReader(new FileInputStream(script.trim()), System.out);
        prompt(r);
    }

    private void eval(String script) throws IOException {
        assertFactory();
        try {
            Object o = engine.eval(script);
            if (o != null) {
                System.out.println(o);
            }
        } catch (ScriptException ex) {
            System.out.println("Parse error: " + ex.getMessage());
            //} catch (InvocationTargetException ex) {
            //     System.out.println("Error: " + ex.getMessage());
        } catch (Exception ex) {
            System.out.println("RT error: ");
            ex.printStackTrace();
        }
    }

    private void grep(String substring) throws IOException {
        assertFactory();
        ShadowFactorySPI f = ShadowFactory.getInstance();

        for (String type : f.grepClassName(substring)) {
            System.out.println(type + ":" + f.findAll(type).size());
        }
    }

    private void assertFactory() throws IOException {
        ShadowFactorySPI f = ShadowFactory.getInstance();
        if (f == null) {
            throw new IOException("Please load a dump first!");
        }
    }

    public class EngineShadow {

        public void importFile(String file) throws IOException {
            File script = new File(file.trim());
            System.out.println("Using script file : " + script.getAbsolutePath());
            evalScript(new FileReader(file));
        }
    }
}
