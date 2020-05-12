package Microservices;

import CommandDesign.Command;
import CommandDesign.CommandsHelp;
import CommandDesign.CommandsMap;
import RabbitMQ.RabbitMQConfig;
import RabbitMQ.Sender;
import Redis.UserCache;
import ResourcePools.WorkerPool;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RunnableClasses {
    protected static void handleMsg(String msg, String correlationID, String subclass, Logger log, WorkerPool pool) {
        HashMap<String, String> map = null;
        try {
            ObjectMapper mapper = new ObjectMapper();
            map = mapper.readValue(msg, new TypeReference<HashMap<String, String>>() {
            });
        } catch (Exception err) {
            log.log(Level.SEVERE, err.getMessage(), err);
        }
        if (map != null) {
            map.put("app", subclass.toLowerCase());
            if (map.containsKey("method")) {
                map.put("correlation_id", correlationID);
                Class<?> cmdClass = CommandsMap.queryClass(map.get("method"));
                if (cmdClass == null) {
                    log.log(Level.SEVERE,
                            "Invalid Request. Class \"" + map.get("method") + "\" Not Found");
                } else {
                    String cacheEntry = UserCache.userCache.get(map.get("method") +":"+ map.get("user_id"));
                    if (cacheEntry != null) {
                        System.out.println("CACHE HIT");
                        CommandsHelp.submit(map.get("app"), cacheEntry, map.get("correlation_id"), log);
                    } else {
                        System.out.println("CACHE MISS");
                        try {
                            Command c = (Command) cmdClass.newInstance();
                            c.init(map);
                            //System.out.println(c.getParameters().keySet());
                            pool.execute(c);
                        } catch (Exception e) {
                            log.log(Level.SEVERE, e.getMessage(), e);
                        }
                    }
                }
            }
        }
    }


}