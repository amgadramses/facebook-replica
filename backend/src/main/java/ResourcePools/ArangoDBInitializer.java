package ResourcePools;

import com.arangodb.ArangoDB;
import com.arangodb.ArangoDBException;
import com.arangodb.entity.*;
import com.arangodb.model.CollectionCreateOptions;
import com.arangodb.model.GraphCreateOptions;

import java.util.Arrays;

public class ArangoDBInitializer {
    final static String dbName = "SocialDB";
    static ArangoDB arangoDB = null;
    static final String usersCollection = "Users";
    static final String reportsCollection = "Reports";
    static final String followsCollection = "Follows";
    static final String blocksCollection = "Blocks";
    static final String friendRequestsCollection = "FriendRequests";
    static final String friendsCollection = "Friends";

    public static void initArangoDB() {
        ArangoDBConnectionPool.initSource();
        arangoDB = ArangoDBConnectionPool.getDriver();

        try {
            arangoDB.createDatabase(dbName);
            System.out.println("Database created: " + dbName);
        } catch (ArangoDBException e) {
            System.err.println("Failed to create database: " + dbName + "; " + e.getMessage());
        }

        try {
            CollectionEntity myArangoCollection = arangoDB.db(dbName).createCollection(usersCollection);
            System.out.println("Collection created: " + myArangoCollection.getName());
        } catch (ArangoDBException e) {
            System.err.println("Failed to create collection: " + usersCollection + "; " + e.getMessage());
        }

        try {
            CollectionEntity myArangoCollection = arangoDB.db(dbName).createCollection(reportsCollection);
            System.out.println("Collection created: " + myArangoCollection.getName());
        } catch (ArangoDBException e) {
            System.err.println("Failed to create collection: " + reportsCollection + "; " + e.getMessage());
        }

        try {
            CollectionEntity myArangoCollection = arangoDB.db(dbName).createCollection(followsCollection, new CollectionCreateOptions().type(CollectionType.EDGES));
            System.out.println("Collection created: " + myArangoCollection.getName());
        } catch (ArangoDBException e) {
            System.err.println("Failed to create collection: " + followsCollection + "; " + e.getMessage());
        }

        try {
            CollectionEntity myArangoCollection = arangoDB.db(dbName).createCollection(friendsCollection, new CollectionCreateOptions().type(CollectionType.EDGES));
            System.out.println("Collection created: " + myArangoCollection.getName());
        } catch (ArangoDBException e) {
            System.err.println("Failed to create collection: " + friendsCollection + "; " + e.getMessage());
        }

        try {
            CollectionEntity myArangoCollection = arangoDB.db(dbName).createCollection(friendRequestsCollection, new CollectionCreateOptions().type(CollectionType.EDGES));
            System.out.println("Collection created: " + myArangoCollection.getName());
        } catch (ArangoDBException e) {
            System.err.println("Failed to create collection: " + friendRequestsCollection + "; " + e.getMessage());
        }

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

        try {
            insertions(arangoDB);
        }
        catch (Exception e){
            System.out.println("Error while dummy inserting in the ArangoDB");
        }

    }

    public static void insertions(ArangoDB arangoDB){
        //Users collection
        BaseDocument user1 = new BaseDocument();
        user1.setKey("1");
        user1.addAttribute("first_name", "Mario");
        user1.addAttribute("last_name", "Speedwagon");
        arangoDB.db(dbName).collection(usersCollection).insertDocument(user1);

        BaseDocument user2 = new BaseDocument();
        user2.setKey("2");
        user2.addAttribute("first_name", "Petey");
        user2.addAttribute("last_name", "Cruiser");
        arangoDB.db(dbName).collection(usersCollection).insertDocument(user2);

        BaseDocument user3 = new BaseDocument();
        user3.setKey("3");
        user3.addAttribute("first_name", "Anna");
        user3.addAttribute("last_name", "Sthesia");
        arangoDB.db(dbName).collection(usersCollection).insertDocument(user3);

        BaseDocument user4 = new BaseDocument();
        user4.setKey("4");
        user4.addAttribute("first_name", "Paul");
        user4.addAttribute("last_name", "Molive");
        arangoDB.db(dbName).collection(usersCollection).insertDocument(user4);

        BaseDocument user5 = new BaseDocument();
        user5.setKey("5");
        user5.addAttribute("first_name", "Anna");
        user5.addAttribute("last_name", "Mull");
        arangoDB.db(dbName).collection(usersCollection).insertDocument(user5);

        BaseDocument user6 = new BaseDocument();
        user6.setKey("6");
        user6.addAttribute("first_name", "Gail");
        user6.addAttribute("last_name", "Forcewind");
        arangoDB.db(dbName).collection(usersCollection).insertDocument(user6);

        BaseDocument user7 = new BaseDocument();
        user7.setKey("7");
        user7.addAttribute("first_name", "Youtham");
        user7.addAttribute("last_name", "Joseph");
        arangoDB.db(dbName).collection(usersCollection).insertDocument(user7);

        BaseDocument user8 = new BaseDocument();
        user8.setKey("8");
        user8.addAttribute("first_name", "Amgad");
        user8.addAttribute("last_name", "Ashraf");
        arangoDB.db(dbName).collection(usersCollection).insertDocument(user8);

        BaseDocument user9 = new BaseDocument();
        user9.setKey("9");
        user9.addAttribute("first_name", "Akram");
        user9.addAttribute("last_name", "Ashraf");
        arangoDB.db(dbName).collection(usersCollection).insertDocument(user9);

        BaseDocument user10 = new BaseDocument();
        user10.setKey("10");
        user10.addAttribute("first_name", "Shady");
        user10.addAttribute("last_name", "Younan");
        arangoDB.db(dbName).collection(usersCollection).insertDocument(user10);

        //Follows collection
        BaseEdgeDocument u1fu2 = new BaseEdgeDocument("Users/1", "Users/2");
        BaseEdgeDocument u2fu3 = new BaseEdgeDocument("Users/2", "Users/3");
        BaseEdgeDocument u3fu4 = new BaseEdgeDocument("Users/3", "Users/4");
        arangoDB.db(dbName).collection(followsCollection).insertDocument(u1fu2);
        arangoDB.db(dbName).collection(followsCollection).insertDocument(u2fu3);
        arangoDB.db(dbName).collection(followsCollection).insertDocument(u3fu4);

        BaseEdgeDocument u1fu3 = new BaseEdgeDocument("Users/1", "Users/3");
        BaseEdgeDocument u2fu4 = new BaseEdgeDocument("Users/2", "Users/4");
        BaseEdgeDocument u3fu5 = new BaseEdgeDocument("Users/3", "Users/5");
        arangoDB.db(dbName).collection(followsCollection).insertDocument(u1fu3);
        arangoDB.db(dbName).collection(followsCollection).insertDocument(u2fu4);
        arangoDB.db(dbName).collection(followsCollection).insertDocument(u3fu5);


        //Blocks collection
        BaseEdgeDocument u1bu2 = new BaseEdgeDocument("Users/1", "Users/2");
        BaseEdgeDocument u4bu10 = new BaseEdgeDocument("Users/4", "Users/10");
        BaseEdgeDocument u7bu9 = new BaseEdgeDocument("Users/7", "Users/9");
        BaseEdgeDocument u3bu1 = new BaseEdgeDocument("Users/3", "Users/1");
        arangoDB.db(dbName).collection(blocksCollection).insertDocument(u1bu2);
        arangoDB.db(dbName).collection(blocksCollection).insertDocument(u4bu10);
        arangoDB.db(dbName).collection(blocksCollection).insertDocument(u7bu9);
        arangoDB.db(dbName).collection(blocksCollection).insertDocument(u3bu1);

        //FriendRequests Collection
        BaseEdgeDocument u2fru10 = new BaseEdgeDocument("Users/2", "Users/10");
        u2fru10.setKey("322500");
        BaseEdgeDocument u9bu3 = new BaseEdgeDocument("Users/9", "Users/3");
        u9bu3.setKey("242500");
        BaseEdgeDocument u7bu3 = new BaseEdgeDocument("Users/7", "Users/3");
        u7bu3.setKey("232500");
        arangoDB.db(dbName).collection(friendRequestsCollection).insertDocument(u2fru10);
        arangoDB.db(dbName).collection(friendRequestsCollection).insertDocument(u9bu3);
        arangoDB.db(dbName).collection(friendRequestsCollection).insertDocument(u7bu3);


        //Friends Collection
        BaseEdgeDocument u7fu8 = new BaseEdgeDocument("Users/7", "Users/8");
        u7fu8.setKey("560000");
        BaseEdgeDocument u8fu9 = new BaseEdgeDocument("Users/8", "Users/9");
        u8fu9.setKey("720000");
        BaseEdgeDocument u8fu10 = new BaseEdgeDocument("Users/8", "Users/10");
        u8fu10.setKey("802500");
        BaseEdgeDocument u10fu1 = new BaseEdgeDocument("Users/10", "Users/1");
        u10fu1.setKey("260000");

        arangoDB.db(dbName).collection(friendsCollection).insertDocument(u7fu8);
        arangoDB.db(dbName).collection(friendsCollection).insertDocument(u8fu9);
        arangoDB.db(dbName).collection(friendsCollection).insertDocument(u8fu10);
        arangoDB.db(dbName).collection(friendsCollection).insertDocument(u10fu1);


    }
}
