package Entities;

import com.arangodb.entity.BaseEdgeDocument;

import java.util.ArrayList;

public class BlockedUsers {
    private ArrayList<BaseEdgeDocument> blockedUsers = new ArrayList<BaseEdgeDocument>();

    public void add(BaseEdgeDocument doc){
        blockedUsers.add(doc);
    }
}
