import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;

import java.util.ArrayList;

/**
 * Created by shahmeernavid on 2017-04-03.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public abstract class BaseTest {
    ArrayList<String> accounts;
    TestServer server;

    @Before
    public void setup() {
        accounts = new ArrayList<>();
        Main.start(8080);
        Main.await();
    }

    @After
    public void teardown() throws Exception {
        Main.kill();
        // Very sketch but need to wait for server to properly shut down.
        Thread.sleep(1000);
    }
}
