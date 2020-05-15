package CommandDesign.ConcreteCommands;

import CommandDesign.Command;
import CommandDesign.CommandsHelp;
import Redis.UserCache;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.minio.MinioClient;
import io.minio.errors.*;
import org.apache.commons.io.FileUtils;
import java.util.UUID;
import java.io.File;
import java.io.IOException;
import java.util.Base64;
import java.util.logging.Logger;


public class UploadProfilePictureCommand extends Command {
    private final Logger log = Logger.getLogger(UploadProfilePictureCommand.class.getName());
    private String user_id;
    private String base64Img;
    @Override
    protected void execute() {
        user_id = parameters.get("user_id");
        base64Img = parameters.get("img");
//        base64Img = "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";
        MinioClient minioClient = null;
//        UserCache.userCache.del("showProfile"+":"+parameters.get("user_id"));


        try {
            minioClient = new MinioClient("http://localhost:9000", "minioadmin", "minioadmin");
        } catch (InvalidEndpointException e) {
            e.printStackTrace();
        } catch (InvalidPortException e) {
            e.printStackTrace();
        }
        if(minioClient != null){
            try {
                boolean bucketExists = minioClient.bucketExists("profilepicturebucket"+user_id);
                if(!bucketExists){
                    minioClient.makeBucket("profilepicturebucket"+user_id);
                }

                String outputFileName = user_id+".png";
                File imageFile = new File(user_id+".png");
                byte[] decodedBytes = Base64.getDecoder().decode(base64Img);
                try {
                    FileUtils.writeByteArrayToFile(imageFile, decodedBytes);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                String unique_id = UUID.randomUUID().toString();
                minioClient.putObject("profilepicturebucket"+user_id, unique_id, outputFileName, null);
                imageFile.delete();
                responseJson.put("app", parameters.get("app"));
                responseJson.put("method", parameters.get("method"));
                responseJson.put("status", "ok");
                responseJson.put("code", "200");
                responseJson.put("message", "Your profile picture was updated successfully.");
                System.out.println("PROFILEEEEEEEEEEEE");
            } catch (Exception e) {
                e.printStackTrace();
            }
            finally {
                try {
                    CommandsHelp.submit(parameters.get("app"), mapper.writeValueAsString(responseJson), parameters.get("correlation_id"), log);
                } catch (JsonProcessingException e) {

                }
            }
        }

    }
}
