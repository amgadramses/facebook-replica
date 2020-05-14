package CommandDesign.ConcreteCommands;

import CommandDesign.Command;
import Redis.UserCache;
import io.minio.MinioClient;
import io.minio.errors.*;


public class UploadProfilePictureCommand extends Command {
    private String user_id;

    @Override
    protected void execute() {
        user_id = parameters.get("user_id");
        MinioClient minioClient = null;
//        UserCache.userCache.del("showProfile"+":"+parameters.get("user_id"));

        try {
            minioClient = new MinioClient("http://192.168.1.6:9000", "minioadmin", "minioadmin");
        } catch (InvalidEndpointException e) {
            e.printStackTrace();
        } catch (InvalidPortException e) {
            e.printStackTrace();
        }

        if(minioClient != null){

            try {
                boolean bucketExists = minioClient.bucketExists("bucket"+user_id);
                if(!bucketExists){
                    minioClient.makeBucket("bucket"+user_id);
                }

                minioClient.putObject("bucket"+user_id, "ProfilePicture"+user_id, "src/main/java/CommandDesign/ConcreteCommands/image.png", null);
                System.out.println("Added");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
}
