package CommandDesign;

import CommandDesign.ConcreteCommands.*;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class CommandsMap {
    private static ConcurrentMap<String, Class<?>> cmdMap;

    public static void instantiate() {
        cmdMap = new ConcurrentHashMap<>();
        cmdMap.put("login", LoginCommand.class);
        cmdMap.put("sendFriendRequest", SendFriendRequestCommand.class);
        cmdMap.put("retrieveFriendRequests", RetrieveFriendRequestsCommand.class);
        cmdMap.put("acceptFriendRequest", AcceptFriendRequestCommand.class);
        cmdMap.put("rejectFriendRequest", RejectFriendRequestCommand.class);
        cmdMap.put("followOrUnfollow", FollowOrUnfollowCommand.class);
        cmdMap.put("deactivate", DeactivateCommand.class);
        cmdMap.put("register", RegisterCommand.class);
    }

    public static Class<?> queryClass(String cmd) {
        return cmdMap.get(cmd);
    }
}
