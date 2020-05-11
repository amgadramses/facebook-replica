package Entities;

import com.arangodb.entity.BaseEdgeDocument;

import java.util.ArrayList;

public class Followers {
    private ArrayList<BaseEdgeDocument> followers = new ArrayList<BaseEdgeDocument>();

    public void add(BaseEdgeDocument doc){
        followers.add(doc);
    }
}
