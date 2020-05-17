package CommandDesign.ConcreteCommands;

import CommandDesign.Command;
import CommandDesign.CommandsHelp;
import io.minio.MinioClient;
import io.minio.Result;

import io.minio.messages.Item;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.logging.Logger;

public class GetAllCoverPicturesCommand extends Command {
    private final Logger log = Logger.getLogger(GetAllCoverPicturesCommand.class.getName());
    private String user_id;

    @Override
    protected void execute() {
        MinioClient minioClient = null;

        try {
            minioClient = new MinioClient("http://miniodb:9000", "minioadmin", "minioadmin");
            if (minioClient != null) {
                user_id = parameters.get("user_id");
                String bucketName = "coverpicturebucket" + user_id;
                boolean bucketExists = minioClient.bucketExists("coverpicturebucket" + user_id);
                if (bucketExists) {
                    Iterable<Result<Item>> results = minioClient.listObjects(bucketName);
                    ArrayList<String> pictures = new ArrayList<String>();
                    for (Result<Item> result : results) {
                        Item item = result.get();
                        System.out.println(item.lastModified() + ", " + item.size() + ", " + item.objectName());
                        InputStream is = minioClient.getObject(bucketName, item.objectName());
                        byte[] imageBytes = new byte[(int) item.size()];
                        is.read(imageBytes, 0, imageBytes.length);
                        is.close();

                        byte[] encoded = Base64.getEncoder().encode(imageBytes);
                        String imgBase64 = new String(encoded);
                        pictures.add(imgBase64);
                    }
                    responseJson.put("app", parameters.get("app"));
                    responseJson.put("method", parameters.get("method"));
                    responseJson.put("status", "ok");
                    responseJson.put("code", "200");
                    responseJson.set("pictures", nf.pojoNode(pictures));
                    CommandsHelp.submit(parameters.get("app"), mapper.writeValueAsString(responseJson), parameters.get("correlation_id"), log);
                } else {
                    //no pictures found
                    responseJson.put("app", parameters.get("app"));
                    responseJson.put("method", parameters.get("method"));
                    responseJson.put("status", "ok");
                    responseJson.put("code", "200");
                    responseJson.put("message", "You have no pictures.");
                    CommandsHelp.submit(parameters.get("app"), mapper.writeValueAsString(responseJson), parameters.get("correlation_id"), log);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
