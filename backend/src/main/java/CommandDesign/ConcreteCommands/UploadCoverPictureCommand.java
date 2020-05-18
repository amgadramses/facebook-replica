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
import java.util.logging.Level;
import java.util.logging.Logger;


public class UploadCoverPictureCommand extends Command {
    private final Logger log = Logger.getLogger(UploadCoverPictureCommand.class.getName());
    private String user_id;
    private String base64Img;
    final String IP_ADDRESS = "http://miniodb:9000"; //Replace with your IP address+ port 9000
    @Override
    protected void execute() {
        user_id = parameters.get("user_id");
        base64Img = parameters.get("img");
        MinioClient minioClient = null;


        try {
            minioClient = new MinioClient(IP_ADDRESS, "minioadmin", "minioadmin");
        } catch (InvalidEndpointException e) {
            log.log(Level.SEVERE, e.getMessage(), e);
        } catch (InvalidPortException e) {
            log.log(Level.SEVERE, e.getMessage(), e);
        }
        if(minioClient != null){
            try {
                boolean bucketExists = minioClient.bucketExists("coverpicturebucket"+user_id);
                if(!bucketExists){
                    minioClient.makeBucket("coverpicturebucket"+user_id);
                }

                String outputFileName = user_id+".png";
                File imageFile = new File(user_id+".png");
                byte[] decodedBytes = Base64.getDecoder().decode(base64Img);
                try {
                    FileUtils.writeByteArrayToFile(imageFile, decodedBytes);
                } catch (IOException e) {
                    log.log(Level.SEVERE, e.getMessage(), e);
                }

                String unique_id = UUID.randomUUID().toString();
                minioClient.putObject("coverpicturebucket"+user_id, unique_id, outputFileName, null);
                imageFile.delete();
                responseJson.put("app", parameters.get("app"));
                responseJson.put("method", parameters.get("method"));
                responseJson.put("status", "ok");
                responseJson.put("code", "200");
                responseJson.put("message", "Your cover picture was updated successfully.");

            } catch (Exception e) {
                log.log(Level.SEVERE, e.getMessage(), e);
            }
            finally {
                try {
                    CommandsHelp.submit(parameters.get("app"), mapper.writeValueAsString(responseJson), parameters.get("correlation_id"), log);
                } catch (JsonProcessingException e) {
                    log.log(Level.SEVERE, e.getMessage(), e);

                }
            }
        }

    }
}
