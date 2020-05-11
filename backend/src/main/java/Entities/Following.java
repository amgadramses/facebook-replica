package Entities;

import com.arangodb.entity.BaseEdgeDocument;

import java.util.ArrayList;

public class Following {
    private ArrayList<BaseEdgeDocument> following = new ArrayList<BaseEdgeDocument>();

    public void add(BaseEdgeDocument doc){
        following.add(doc);
    }
}
