package ResourcePools;

import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class JedisConnectionPool {


    private static JedisPool pool;

    public static void initSource() {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(128);
        poolConfig.setMaxIdle(128);
        poolConfig.setMinIdle(16);
        pool = new JedisPool(poolConfig,"redis");
    }

    public static JedisPool getPool() {
        return pool;
    }
}
