package ResourcePools;

import ResourcePools.ArangoDBConnectionPool;
import com.arangodb.ArangoDB;
import com.arangodb.ArangoDBException;
import com.arangodb.entity.CollectionEntity;
import com.arangodb.entity.CollectionType;
import com.arangodb.entity.EdgeDefinition;
import com.arangodb.entity.GraphEntity;
import com.arangodb.model.CollectionCreateOptions;
import com.arangodb.model.GraphCreateOptions;

import java.util.Arrays;

public class ArangoDBInitializer {

    public static void initArangoDB() {
        ArangoDBConnectionPool.initSource();
        ArangoDB arangoDB = ArangoDBConnectionPool.getDriver();

        String dbName = "SocialDB";
        try {
            arangoDB.createDatabase(dbName);
            System.out.println("Database created: " + dbName);
        } catch (ArangoDBException e) {
            System.err.println("Failed to create database: " + dbName + "; " + e.getMessage());
        }

        String usersCollection = "Users";
        try {
            CollectionEntity myArangoCollection = arangoDB.db(dbName).createCollection(usersCollection);
            System.out.println("Collection created: " + myArangoCollection.getName());
        } catch (ArangoDBException e) {
            System.err.println("Failed to create collection: " + usersCollection + "; " + e.getMessage());
        }

        String reportsCollection = "Reports";
        try {
            CollectionEntity myArangoCollection = arangoDB.db(dbName).createCollection(reportsCollection);
            System.out.println("Collection created: " + myArangoCollection.getName());
        } catch (ArangoDBException e) {
            System.err.println("Failed to create collection: " + reportsCollection + "; " + e.getMessage());
        }

        String followsCollection = "Follows";
        try {
            CollectionEntity myArangoCollection = arangoDB.db(dbName).createCollection(followsCollection, new CollectionCreateOptions().type(CollectionType.EDGES));
            System.out.println("Collection created: " + myArangoCollection.getName());
        } catch (ArangoDBException e) {
            System.err.println("Failed to create collection: " + followsCollection + "; " + e.getMessage());
        }

        String friendsCollection = "Friends";
        try {
            CollectionEntity myArangoCollection = arangoDB.db(dbName).createCollection(friendsCollection, new CollectionCreateOptions().type(CollectionType.EDGES));
            System.out.println("Collection created: " + myArangoCollection.getName());
        } catch (ArangoDBException e) {
            System.err.println("Failed to create collection: " + friendsCollection + "; " + e.getMessage());
        }

        String friendRequestsCollection = "FriendRequests";
        try {
            CollectionEntity myArangoCollection = arangoDB.db(dbName).createCollection(friendRequestsCollection, new CollectionCreateOptions().type(CollectionType.EDGES));
            System.out.println("Collection created: " + myArangoCollection.getName());
        } catch (ArangoDBException e) {
            System.err.println("Failed to create collection: " + friendRequestsCollection + "; " + e.getMessage());
        }

        String blocksCollection = "Blocks";
        try {
            CollectionEntity myArangoCollection = arangoDB.db(dbName).createCollection(blocksCollection, new CollectionCreateOptions().type(CollectionType.EDGES));
            System.out.println("Collection created: " + myArangoCollection.getName());
        } catch (ArangoDBException e) {
            System.err.println("Failed to create collection: " + blocksCollection + "; " + e.getMessage());
        }

        EdgeDefinition edgeDefinition1 = new EdgeDefinition().collection("Blocks").from("Users").to("Users");
        EdgeDefinition edgeDefinition2 = new EdgeDefinition().collection("Follows").from("Users").to("Users");
        EdgeDefinition edgeDefinition3 = new EdgeDefinition().collection("Friends").from("Users").to("Users");
        EdgeDefinition edgeDefinition4 = new EdgeDefinition().collection("FriendRequests").from("Users").to("Users");

        EdgeDefinition array[] = new EdgeDefinition[]{edgeDefinition1, edgeDefinition2, edgeDefinition3, edgeDefinition4};
        GraphEntity graph = arangoDB.db(dbName).createGraph("SocialDBGraph", Arrays.asList(array), new GraphCreateOptions());
        System.out.println("Graph created: " + graph.getName());

    }
}
