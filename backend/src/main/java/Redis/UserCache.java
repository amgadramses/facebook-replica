package Redis;

import ResourcePools.JedisConnectionPool;
import redis.clients.jedis.Jedis;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class UserCache {
    public static Jedis userCache = JedisConnectionPool.getPool().getResource();

    public static void userBgSave(){
        Runnable runnable = () -> {
            String res;
            res = userCache.bgsave();
        };
        ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
        service.scheduleAtFixedRate(runnable, 0, 15, TimeUnit.MINUTES);
    }

}
