package fctreddit.impl.server.java;

import fctreddit.api.Post;
import fctreddit.api.User;
import fctreddit.api.Vote;
import fctreddit.api.java.Content;
import fctreddit.api.java.Image;
import fctreddit.api.java.Result;
import fctreddit.api.rest.RestContent;
import fctreddit.api.rest.RestUsers;
import fctreddit.clients.ProxyClient;
import fctreddit.clients.java.ContentClient;
import fctreddit.clients.java.ImageClient;
import fctreddit.clients.java.UsersClient;
import fctreddit.clients.rest.RestUsersClient;
import fctreddit.impl.server.persistence.Hibernate;
import fctreddit.multicast.Discovery;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import static fctreddit.api.rest.RestContent.PATH;

public class JavaContent implements Content {

    private static Logger Log = Logger.getLogger(JavaContent.class.getName());
    private Hibernate hibernate;
    private final Map<String, Object> postMonitors = new ConcurrentHashMap<>();
    private String URL;
    private ProxyClient proxyClient;
    private UsersClient javaUsers = null;
    private ImageClient javaImages = null;
    private static Discovery discovery;
    static {
        try {
            discovery = new Discovery(Discovery.DISCOVERY_ADDR);
            discovery.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public JavaContent(String serverURI) {
        hibernate = Hibernate.getInstance();
        proxyClient = new ProxyClient(discovery);
        URL = serverURI + PATH + "/";
    }

    @Override
    public Result<String> createPost(Post post, String userPassword) throws Exception {
        Log.info("createPost : " + post);

        createOrExistsJavaUsers();
        Result<User> resUser = javaUsers.getUser(post.getAuthorId(), userPassword);
        if (!resUser.isOK()){
            return Result.error(resUser.error());
        }
        List<Post> list;
        if (post.getParentUrl() != null) {
            String idParent = post.getParentUrl().replace(URL, "");
            Log.info("idParent : " + idParent);
            String query = "SELECT p FROM Post p WHERE p.postId = '" + idParent + "'";
            list = hibernate.jpql(query, Post.class);

            if (list.isEmpty()) {
                return Result.error(Result.ErrorCode.NOT_FOUND);
            }
        }
        post.setCreationTimestamp(System.currentTimeMillis());
        post.setPostId(Long.toString(post.getCreationTimestamp()));
        try{
            hibernate.persist(post);

            if (post.getParentUrl() != null) {
                Object monitor = postMonitors.get(post.getParentUrl());
                if (monitor != null) {
                    synchronized (monitor) {
                        monitor.notifyAll();
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(Result.ErrorCode.CONFLICT);
        }

        return Result.ok(post.getPostId());
    }

    @Override
    public Result<List<String>> getPosts(long timestamp, String sortOrder) {
        Log.info("getPosts" + timestamp);
        List<String> list;
        String query = "SELECT p.postId FROM Post p where p.parentUrl is null";

        if (timestamp !=0 || (sortOrder != null && !sortOrder.isEmpty())) {
            if(timestamp != 0){
                query += " and p.creationTimestamp >= " + timestamp;
            }
            if (sortOrder != null && !sortOrder.isEmpty()) {
                if (sortOrder.equals(RestContent.MOST_UP_VOTES)) {
                    query += " ORDER BY p.upVote DESC, p.postId ASC";
                } else if (sortOrder.equals(RestContent.MOST_REPLIES)) {
                    String subQuery = "(SELECT COUNT(c) FROM Post c WHERE c.parentUrl = CONCAT('" + URL + "', p.postId))";
                    query += " ORDER BY "+ subQuery + " DESC, p.postId ASC";
                }
            }
        }
         else {
            query += " ORDER BY p.creationTimestamp ASC";
        }

        try{
            list = hibernate.jpql(query, String.class);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(Result.ErrorCode.CONFLICT);
        }

        return Result.ok(list);
    }

    @Override
    public Result<Post> getPost(String postId) {
        Log.info("getPost" + postId);
        Post post;
        try{
            post = hibernate.get(Post.class, postId);
            if (post == null) {
                return Result.error(Result.ErrorCode.NOT_FOUND);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(Result.ErrorCode.CONFLICT);
        }

        return Result.ok(post);
    }

    @Override
    public Result<List<String>> getPostAnswers(String postId, long maxTimeout) {
        Log.info("getPostAnswers" + postId);
        List<String> currentList;
        Result<Post> resPost = getPost(postId);
        if (!resPost.isOK()) {
            return Result.error(resPost.error());
        }
        Post post = resPost.value();
        String parentUrl = URL + post.getPostId();
        try {
            currentList = hibernate.jpql(
                    "SELECT p.postId FROM Post p WHERE p.parentUrl = '" + parentUrl
                            + "' ORDER BY p.creationTimestamp ASC",
                    String.class);

            if(maxTimeout != 0) {

                Object monitor = postMonitors.computeIfAbsent(parentUrl, k -> new Object());

                synchronized (monitor) {
                    monitor.wait(maxTimeout);

                    List<String> newAnswers = hibernate.jpql(
                            "SELECT p.postId FROM Post p WHERE p.parentUrl = '" + parentUrl
                                    + "' ORDER BY p.creationTimestamp ASC",
                            String.class);

                    if (newAnswers.size() > currentList.size()) {
                        return Result.ok(newAnswers);
                    }
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return Result.error(Result.ErrorCode.INTERNAL_ERROR);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(Result.ErrorCode.CONFLICT);
        }

        return Result.ok(currentList);
    }

    @Override
    public Result<Post> updatePost(String postId, String userPassword, Post post) throws Exception {
        Log.info("updatePost" + postId);
        Result<Post> resPost = getPost(postId);
        if (!resPost.isOK()) {
            return Result.error(resPost.error());
        }
        Post oldPost = resPost.value();
        createOrExistsJavaUsers();
        Result<User> resuser = javaUsers.getUser(oldPost.getAuthorId(), userPassword);
        if (!resuser.isOK()){
            return Result.error(resuser.error());
        }

        List<Post> list = hibernate.jpql("SELECT p from Post p where p.parentUrl = '"
                + URL + oldPost.getPostId() + "'", Post.class);
        if (!list.isEmpty()) {
            return Result.error(Result.ErrorCode.BAD_REQUEST);
        }
        if (post.getPostId() != null || post.getAuthorId() != null ||
                post.getCreationTimestamp() != 0 || post.getParentUrl() != null
                || oldPost.getUpVote() != 0 || oldPost.getDownVote() != 0) {
            return Result.error(Result.ErrorCode.BAD_REQUEST);
        }

        if (post.getMediaUrl() != null) {
            oldPost.setMediaUrl(post.getMediaUrl());
        }
        if (post.getContent() != null) {
            oldPost.setContent(post.getContent());
        }
        try{
            hibernate.update(oldPost);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(Result.ErrorCode.BAD_REQUEST);
        }

        return Result.ok(oldPost);
    }

    @Override
    public Result<Void> deletePost(String postId, String userPassword) throws Exception {
        Log.info("deletePost" + postId);
        Result<Post> resPost = getPost(postId);
        if (!resPost.isOK()) {
            return Result.error(resPost.error());
        }
        Post post = resPost.value();
        createOrExistsJavaUsers();
        Result<User> resuser = javaUsers.getUser(post.getAuthorId(), userPassword);
        if (!resuser.isOK()){
            return Result.error(resuser.error());
        }
        if (post.getMediaUrl() != null) {
            String imageId = extractImageIdFromUrl(post.getMediaUrl());
            createOrExistsJavaImage();
            Result<Void> res = javaImages.deleteImage(post.getAuthorId(), imageId, userPassword);
        }

        try{
            deletePostAndReplies(postId);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(Result.ErrorCode.CONFLICT);
        }
        return Result.ok();
    }

    private static String extractImageIdFromUrl(String mediaUrl) {
        String prefix = "image/";
        return mediaUrl.substring( mediaUrl.lastIndexOf(prefix) + prefix.length()).split("/")[1];
    }

    private void deletePostAndReplies(String postId) {
        Post post = getPost(postId).value();
        List<Post> replies = hibernate.jpql(
                "SELECT p FROM Post p WHERE p.parentUrl = '" + URL+postId + "'", Post.class);
        for (Post reply : replies) {
            deletePostAndReplies(reply.getPostId());
        }
        hibernate.delete(post);
    }


    @Override
    public Result<Void> upVotePost(String postId, String userId, String userPassword) throws Exception {
        Log.info("upVotePost" + postId);

        Result<Post> resPost = getPost(postId);
        if (!resPost.isOK()) {
            return Result.error(resPost.error());
        }
        Post post = resPost.value();
        createOrExistsJavaUsers();
        Result<User> resuser = javaUsers.getUser(userId, userPassword);
        if (!resuser.isOK()){
            return Result.error(resuser.error());
        }
        List<Vote> vote = hibernate.jpql("SELECT v FROM Vote v where v.userId = '" + userId
                + "'and v.postId = '" + postId + "'", Vote.class);
        if(!vote.isEmpty()){
            return Result.error(Result.ErrorCode.CONFLICT);
        }
        try {
            post.setUpVote(post.getUpVote() + 1);
            hibernate.update(post);
            Vote newVote = new Vote(userId, postId,true);
            hibernate.persist(newVote);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(Result.ErrorCode.CONFLICT);
        }
        return Result.ok();
    }

    @Override
    public Result<Void> removeUpVotePost(String postId, String userId, String userPassword) throws Exception {
        Log.info("removeUpVotePost" + postId);
        createOrExistsJavaUsers();
        Result<User> resuser = javaUsers.getUser(userId, userPassword);
        if (!resuser.isOK()){
            return Result.error(resuser.error());
        }

        Result<Post> resPost = getPost(postId);
        if (!resPost.isOK()) {
            return Result.error(resPost.error());
        }

        Post post = resPost.value();
        List<Vote> vote = hibernate.jpql("SELECT v FROM Vote v where v.userId = '" + userId
                + "'and v.postId = '" + postId + "'", Vote.class);
        if (vote.isEmpty()) {
            return Result.error(Result.ErrorCode.CONFLICT);
        }
        try{
            post.setUpVote(post.getUpVote() - 1);
            hibernate.update(post);
            hibernate.delete(vote.get(0));
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(Result.ErrorCode.CONFLICT);
        }
        return Result.ok();
    }

    @Override
    public Result<Void> downVotePost(String postId, String userId, String userPassword) throws Exception {
        Log.info("downVotePost" + postId);
        Result<Post> resPost = getPost(postId);
        if (!resPost.isOK()) {
            return Result.error(resPost.error());
        }
        Post post = resPost.value();
        createOrExistsJavaUsers();
        Result<User> resuser = javaUsers.getUser(userId, userPassword);
        if (!resuser.isOK()){
            return Result.error(resuser.error());
        }
        List<Vote> vote = hibernate.jpql("SELECT v FROM Vote v where v.userId = '" + userId
                + "'and v.postId = '" + postId + "'", Vote.class);
        if(!vote.isEmpty()){
            return Result.error(Result.ErrorCode.CONFLICT);
        }
        try {
            post.setDownVote(post.getDownVote() + 1);
            hibernate.update(post);
            Vote newVote = new Vote(userId, postId,false);
            hibernate.persist(newVote);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.ok();
    }

    @Override
    public Result<Void> removeDownVotePost(String postId, String userId, String userPassword) throws Exception {
        Log.info("removeDownVotePost" + postId);
        createOrExistsJavaUsers();
        Result<User> resuser = javaUsers.getUser(userId, userPassword);
        if (!resuser.isOK()){
            return Result.error(resuser.error());
        }

        Result<Post> resPost = getPost(postId);
        if (!resPost.isOK()) {
            return Result.error(resPost.error());
        }

        Post post = resPost.value();
        List<Vote> vote = hibernate.jpql("SELECT v FROM Vote v where v.userId = '" + userId
                + "'and v.postId = '" + postId + "'", Vote.class);
        if (vote.isEmpty()) {
            return Result.error(Result.ErrorCode.CONFLICT);
        }
        try{
            post.setDownVote(post.getDownVote() - 1);
            hibernate.update(post);
            hibernate.delete(vote.get(0));
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(Result.ErrorCode.CONFLICT);
        }
        return Result.ok();
    }

    @Override
    public Result<Integer> getupVotes(String postId) {
        Log.info("getUpVotes" + postId);
        Result<Post> resPost = getPost(postId);
        if (!resPost.isOK()) {
            return Result.error(resPost.error());
        }
        Post post = resPost.value();
        return Result.ok(post.getUpVote());
    }

    @Override
    public Result<Integer> getDownVotes(String postId) {
        Log.info("getDownVotes" + postId);
        Result<Post> resPost = getPost(postId);
        if (!resPost.isOK()) {
            return Result.error(resPost.error());
        }
        Post post = resPost.value();
        return Result.ok(post.getDownVote());
    }

    @Override
    public Result<Void> setNullAuthor(String userId) {
        hibernate.executeUpdate("UPDATE Post p SET p.authorId = null WHERE p.authorId = '" + userId + "'");

        hibernate.executeUpdate("UPDATE Post p SET p.upVote = p.upVote - 1 WHERE p.postId IN " +
                "(SELECT v.postId FROM Vote v WHERE v.userId = '" + userId + "' AND v.IsUpVote = true)");

        hibernate.executeUpdate("UPDATE Post p SET p.downVote = p.downVote - 1 WHERE p.postId IN " +
                "(SELECT v.postId FROM Vote v WHERE v.userId = '" + userId + "' AND v.IsUpVote = false)");

        hibernate.executeUpdate("DELETE FROM Vote v WHERE v.userId = '" + userId + "'");
        return Result.ok();
    }

    private void createOrExistsJavaUsers(){
        if(javaUsers == null){
            javaUsers = proxyClient.getUsersClient();
        }
    }

    private void createOrExistsJavaImage(){
        if(javaImages == null){
            javaImages = proxyClient.getImageClient();
        }
    }
}
