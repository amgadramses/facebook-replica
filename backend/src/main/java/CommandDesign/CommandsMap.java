package CommandDesign;

import CommandDesign.ConcreteCommands.LoginCommand;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class CommandsMap {
    private static ConcurrentMap<String, Class<?>> cmdMap;

    public static void instantiate() {
        cmdMap = new ConcurrentHashMap<>();
        cmdMap.put("login", LoginCommand.class);

    }

    public static Class<?> queryClass(String cmd) {
        return cmdMap.get(cmd);
    }
}
