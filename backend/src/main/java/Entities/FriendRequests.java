package Entities;

import com.arangodb.entity.BaseEdgeDocument;

import java.util.ArrayList;

public class FriendRequests {
    private ArrayList<BaseEdgeDocument> friendRequests = new ArrayList<BaseEdgeDocument>();

    public void add(BaseEdgeDocument doc){
        friendRequests.add(doc);
    }
}
