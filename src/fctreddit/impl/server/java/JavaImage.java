package fctreddit.impl.server.java;

import fctreddit.api.User;
import fctreddit.api.java.Image;
import fctreddit.api.java.Result;
import fctreddit.clients.ProxyClient;
import fctreddit.clients.java.UsersClient;
import fctreddit.multicast.Discovery;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

public class JavaImage implements Image {

    private static final String FILESPACKAGE = "files";
    private static Logger Log = Logger.getLogger(JavaImage.class.getName());
    private int counterImage;
    private String url;
    private UsersClient javaUsers = null;
    private static Discovery discovery;
    private ProxyClient proxyClient;
    static {
        try {
            discovery = new Discovery(Discovery.DISCOVERY_ADDR);
            discovery.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public JavaImage(String url) {
        this.url = url;
        counterImage = 0;
        proxyClient = new ProxyClient(discovery);
    }

    @Override
    public Result<String> createImage(String userId, byte[] imageContents, String password) throws Exception {
        createOrExistsJavaUsers();
        Result<User> resUser = javaUsers.getUser(userId, password);
        if (!resUser.isOK()) {
            return Result.error(resUser.error());
        }
        User user = resUser.value();

        if (imageContents == null || imageContents.length == 0) {
            return Result.error(Result.ErrorCode.BAD_REQUEST);
        }

        int imageId = counterImage;
        counterImage++;
        File path = new File(FILESPACKAGE + "/" + user.getUserId() + "/" + imageId);
        path.getParentFile().mkdirs();
        try{
            Files.write(path.toPath(), imageContents);
            String fullPath = url + "/" + "image" + "/" + user.getUserId() + "/" + imageId;
            return Result.ok(fullPath);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(Result.ErrorCode.INTERNAL_ERROR);
        }
    }

    @Override
    public Result<byte[]> getImage(String userId, String imageId) {
        Log.info("getImage " + imageId);

        File path = new File(FILESPACKAGE + "/" + userId + "/" + imageId);
        if (!Files.exists(path.toPath())) {
            return Result.error(Result.ErrorCode.NOT_FOUND);
        }
        try {
            return Result.ok(Files.readAllBytes(path.toPath()));
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(Result.ErrorCode.INTERNAL_ERROR);
        }
    }

    @Override
    public Result<Void> deleteImage(String userId, String imageId, String password) throws Exception {
        Log.info("deleteImage " + imageId);
        createOrExistsJavaUsers();
        User user = javaUsers.getUser(userId, password).value();
        Path pathToFile = Paths.get(FILESPACKAGE + File.separator + userId + File.separator + imageId);
        if (!Files.exists(pathToFile)) {
            return Result.error(Result.ErrorCode.NOT_FOUND);
        }

        try{
            Files.delete(pathToFile);
            return Result.ok();
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(Result.ErrorCode.INTERNAL_ERROR);
        }

    }

    private void createOrExistsJavaUsers(){
        if(javaUsers == null){
            javaUsers = proxyClient.getUsersClient();
        }
    }
}
