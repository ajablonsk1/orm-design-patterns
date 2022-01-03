package orm.utils;

import org.junit.jupiter.api.Test;
import orm.utils.Config;

public class ConfigTest {
    @Test
    public void getInstance() throws Exception {
        Config config = Config.getInstance();
    }
}
