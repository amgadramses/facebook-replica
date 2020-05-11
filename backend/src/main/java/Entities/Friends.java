package Entities;

import com.arangodb.entity.BaseEdgeDocument;

import java.util.ArrayList;

public class Friends {
    private ArrayList<BaseEdgeDocument> friends = new ArrayList<BaseEdgeDocument>();

    public void add(BaseEdgeDocument doc){
        friends.add(doc);
    }
}
