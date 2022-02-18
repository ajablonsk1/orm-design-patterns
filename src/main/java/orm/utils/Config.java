package orm.utils;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import orm.session.SessionFactory;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class Config {
    private int connectionPoolSize;
    private final List<String> packages;

    private static Config instance;
    private final String user;
    private final String password;
    private final String databaseUrl;
    private final String databaseName;

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

        try {
            connectionPoolSize = json.getAsJsonObject().get("connection_pool_size").getAsInt();
        } catch (Exception e){
            connectionPoolSize = 16;
        }
        user = json.getAsJsonObject().get("user").getAsString();
        password = json.getAsJsonObject().get("password").getAsString();
        databaseUrl = json.getAsJsonObject().get("database_url").getAsString();
        databaseName = json.getAsJsonObject().get("database_name").getAsString();
        reader.close();
    }

    public static synchronized Config getInstance() throws Exception {
        // lazy-loading, double null-check, thread-safe Singleton
        if (instance == null){
            synchronized (SessionFactory.class){
                if (instance == null){
                    instance = new Config();
                }
            }
        }
        return instance;
    }

    public int getConnectionPoolSize() {
        return connectionPoolSize;
    }

    public List<String> getPackages() {
        return packages;
    }

    public String getDatabaseUrl() {
        return databaseUrl;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }

    public String getDatabaseName() {
        return databaseName;
    }
}
