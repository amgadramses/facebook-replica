package CommandDesign;

import CommandDesign.ConcreteCommands.LoginCommand;
import CommandDesign.ConcreteCommands.ReceiveFriendRequestCommand;
import CommandDesign.ConcreteCommands.SendFriendRequestCommand;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class CommandsMap {
    private static ConcurrentMap<String, Class<?>> cmdMap;

    public static void instantiate() {
        cmdMap = new ConcurrentHashMap<>();
        cmdMap.put("login", LoginCommand.class);
        cmdMap.put("sendFriendRequest", SendFriendRequestCommand.class);
        cmdMap.put("receiveFriendRequest", ReceiveFriendRequestCommand.class);

    }

    public static Class<?> queryClass(String cmd) {
        return cmdMap.get(cmd);
    }
}
