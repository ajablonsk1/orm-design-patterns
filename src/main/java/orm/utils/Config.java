package orm.utils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.lang.reflect.Array;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class Config {
    private int connectionPoolSize;
    private List<String> packages;

    private static Config instance;

    private Config() throws Exception {
        URL configUrl = getClass().getClassLoader().getResource("orm_config.json");
        if (configUrl == null){
            throw new Exception("orm_config.json not found");
        }
        Gson gson = new Gson();
        File configFile = new File(configUrl.toURI());
        Reader reader = new FileReader(configFile);
        JsonElement json = JsonParser.parseReader(reader);
        JsonElement packagesJson = json.getAsJsonObject().get("packages");
        packages = gson.fromJson(packagesJson, ArrayList.class);
        JsonElement connectionPoolSizeJson = json.getAsJsonObject().get("connection_pool_size");
        connectionPoolSize = connectionPoolSizeJson.getAsInt();
        reader.close();
    }

    public static synchronized Config getInstance() throws Exception {
        if (instance != null){
            return instance;
        } else {
            if (instance != null){
                return instance;
            } else {
                return new Config();
            }
        }
    };

    public int getConnectionPoolSize() {
        return connectionPoolSize;
    }

    public List<String> getPackages() {
        return packages;
    }
}
