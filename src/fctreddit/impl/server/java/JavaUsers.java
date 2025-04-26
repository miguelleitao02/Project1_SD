package fctreddit.impl.server.java;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;
import fctreddit.api.User;
import fctreddit.api.java.Result;
import fctreddit.api.java.Result.ErrorCode;
import fctreddit.api.java.Users;
import fctreddit.clients.ProxyClient;
import fctreddit.clients.java.ContentClient;
import fctreddit.clients.java.ImageClient;
import fctreddit.impl.server.persistence.Hibernate;
import fctreddit.multicast.Discovery;

public class JavaUsers implements Users {

    private static Logger Log = Logger.getLogger(JavaUsers.class.getName());

    private Hibernate hibernate;
    private static Discovery discovery;
    private ProxyClient proxyClient;
    private ImageClient javaImages;

    static {
        try {
            discovery = new Discovery(Discovery.DISCOVERY_ADDR);
            discovery.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public JavaUsers() {
        hibernate = Hibernate.getInstance();
        proxyClient = new ProxyClient(discovery);
    }

    @Override
    public Result<String> createUser(User user) {
        Log.info("createUser : " + user);

        // Check if user data is valid
        if (user.getUserId() == null || user.getPassword() == null || user.getFullName() == null
                || user.getEmail() == null || user.getPassword().isEmpty()) {
            Log.info("User object invalid.");
            return Result.error(ErrorCode.BAD_REQUEST);
        }

        try {
            hibernate.persist(user);
        } catch (Exception e) {
            e.printStackTrace(); //Most likely the exception is due to the user already existing...
            Log.info("User already exists.");
            return Result.error(ErrorCode.CONFLICT);
        }

        return Result.ok(user.getUserId());
    }

    @Override
    public Result<User> getUser(String userId, String password) {
        Log.info("getUser : user = " + userId + "; pwd = " + password);

        // Check if user is valid
        if (userId == null) {
            Log.info("UserId or password null.");
            return Result.error(ErrorCode.BAD_REQUEST);
        }

        User user;
        try {
            user = hibernate.get(User.class, userId);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(ErrorCode.INTERNAL_ERROR);
        }

        // Check if user exists
        if (user == null) {
            Log.info("User does not exist.");
            return Result.error(ErrorCode.NOT_FOUND);
        }

        // Check if the password is correct
        if (!user.getPassword().equals(password) ) {
            Log.info("Password is incorrect");
            return Result.error(ErrorCode.FORBIDDEN);
        }

        return Result.ok(user);

    }

    @Override
    public Result<User> updateUser(String userId, String password, User user) {
        Log.info("updateUser : user = " + userId + "; pwd = " + password + " ; userData = " + user);
        Result<User> resOldUser = getUser(userId, password);
        if (!resOldUser.isOK()){
            return Result.error(resOldUser.error());
        }
        User oldUser = resOldUser.value();

        if (user.getEmail() != null){
            oldUser.setEmail(user.getEmail());
        }
        if (user.getFullName() != null){
            oldUser.setFullName(user.getFullName());
        }
        if (user.getPassword() != null){
            oldUser.setPassword(user.getPassword());
        }
        if(user.getAvatarUrl() != null){
            oldUser.setAvatarUrl(user.getAvatarUrl());
        }
        try {
            hibernate.update(oldUser);
        } catch (Exception e) {
            e.printStackTrace();
            Log.info("User already exists.");
            return Result.error(ErrorCode.CONFLICT);
        }
        return Result.ok(oldUser);
    }

    @Override
    public Result<User> deleteUser(String userId, String password) throws Exception {
        Result<User> resOldUser = getUser(userId, password);
        if (!resOldUser.isOK()){
            return Result.error(resOldUser.error());
        }
        User user = resOldUser.value();
        if(user.getAvatarUrl() != null){
            String id = extractImageIdFromUrl(user.getAvatarUrl());
            user.setAvatarUrl(null);
            createOrExistsJavaImage();
            Result<Void> res = javaImages.deleteImage(userId, id, password);
            if (!res.isOK()) {
                return Result.error(res.error());
            }
        }
        ContentClient javaContents = proxyClient.getContentClient();
        javaContents.setNullAuthor(userId);
        Log.info("User deleted.");
        try {
            hibernate.delete(user);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(ErrorCode.CONFLICT);
        }
        return Result.ok(user);
    }

    private static String extractImageIdFromUrl(String mediaUrl) {
        String prefix = "image/";
        return mediaUrl.substring( mediaUrl.lastIndexOf(prefix) + prefix.length()).split("/")[1];
    }


    @Override
    public Result<List<User>> searchUsers(String pattern) {
        List<User> list;
        try {
            list = hibernate.jpql("SELECT u FROM User u WHERE u.userId LIKE '%" + pattern +"%'", User.class);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(ErrorCode.CONFLICT);
        }
        return Result.ok(list);
    }

    private void createOrExistsJavaImage(){
        if(javaImages == null){
            javaImages = proxyClient.getImageClient();
        }
    }

}

