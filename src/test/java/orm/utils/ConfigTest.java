package orm.utils;

import org.junit.jupiter.api.Test;
import orm.utils.Config;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ConfigTest {
    @Test
    public void getInstance() throws Exception {
        Config config = Config.getInstance();
        assertNotNull(config);
        assertNotNull(config.getPackages());
    }
}
