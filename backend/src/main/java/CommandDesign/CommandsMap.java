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
        cmdMap.put("edit_user", EditUserCommand.class);
        cmdMap.put("add_work", AddWorkCommand.class);
        cmdMap.put("add_education", AddEducationCommand.class);
        cmdMap.put("remove_work", RemoveWorkCommand.class);
        cmdMap.put("remove_education", RemoveEducationCommand.class);
        cmdMap.put("get_educations", GetEducationsCommand.class);
        cmdMap.put("get_works", GetWorksCommand.class);
        cmdMap.put("getFriends", GetFriendsCommand.class);
        cmdMap.put("getFollowers", GetFollowersCommand.class);
        cmdMap.put("getFollowing", GetFollowingCommand.class);
        cmdMap.put("blockOrUnblock", BlockOrUnblockCommand.class);
        cmdMap.put("getBlockedUsers", GetBlockedUsersCommand.class);
        cmdMap.put("reportUser", ReportUserCommand.class);
        cmdMap.put("uploadProfilePicture", UploadProfilePictureCommand.class);
        cmdMap.put("uploadCoverPicture", UploadCoverPictureCommand.class);
        cmdMap.put("showProfile", ShowProfile.class);
        cmdMap.put("deleteAccount", DeleteAccountCommand.class);
        cmdMap.put("getAllProfilePictures", GetAllProfilePicturesCommand.class);
        cmdMap.put("getAllCoverPictures", GetAllCoverPicturesCommand.class);
        cmdMap.put("getCurrentProfilePicture", GetCurrentProfilePictureCommand.class);
        cmdMap.put("getCurrentCoverPicture", GetCurrentCoverPictureCommand.class);
    }

    public static Class<?> queryClass(String cmd) {
        return cmdMap.get(cmd);
    }
}
