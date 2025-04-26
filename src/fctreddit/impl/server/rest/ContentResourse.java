package fctreddit.impl.server.rest;

import fctreddit.api.Post;
import fctreddit.api.java.Content;
import fctreddit.api.java.Result;
import fctreddit.api.rest.RestContent;
import fctreddit.impl.server.java.JavaContent;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.logging.Logger;


public class ContentResourse implements RestContent {
    private static Logger Log = Logger.getLogger(ContentResourse.class.getName());
    final Content impl;

    public ContentResourse(String serverURI) throws Exception {
        impl = new JavaContent(serverURI);
    }
    @Override
    public String createPost(Post post, String userPassword) throws Exception {
        Log.info("createPost : " + post);

        Result<String> res = impl.createPost(post, userPassword);
        if(!res.isOK()) {
            throw new WebApplicationException(errorCodeToStatus(res.error()));
        }
        return res.value();
    }

    @Override
    public List<String> getPosts(long timestamp, String sortOrder) {
        Log.info("getPosts : " + timestamp);
        Result<List<String>> res = impl.getPosts(timestamp, sortOrder);
        if(!res.isOK()) {
            throw new WebApplicationException(errorCodeToStatus(res.error()));
        }
        return res.value();
    }

    @Override
    public Post getPost(String postId) {
        Log.info("getPost : " + postId);
        Result<Post> res = impl.getPost(postId);
        if(!res.isOK()) {
            throw new WebApplicationException(errorCodeToStatus(res.error()));
        }
        return res.value();
    }

    @Override
    public List<String> getPostAnswers(String postId, long timeout) {
        Log.info("getPostAnswers : " + postId);
        Result<List<String>> res = impl.getPostAnswers(postId, timeout);
        if(!res.isOK()) {
            throw new WebApplicationException(errorCodeToStatus(res.error()));
        }
        return res.value();
    }

    @Override
    public Post updatePost(String postId, String userPassword, Post post) throws Exception {
        Log.info("updatePost : " + postId);
        Result<Post> res = impl.updatePost(postId, userPassword, post);
        if(!res.isOK()) {
            throw new WebApplicationException(errorCodeToStatus(res.error()));
        }
        return res.value();
    }

    @Override
    public void deletePost(String postId, String userPassword) throws Exception {
        Log.info("deletePost : " + postId);
        Result<Void> res = impl.deletePost(postId, userPassword);
        if(!res.isOK()) {
            throw new WebApplicationException(errorCodeToStatus(res.error()));
        }


    }

    @Override
    public void upVotePost(String postId, String userId, String userPassword) throws Exception {
        Log.info("upVotePost : " + postId);
        Result<Void> res = impl.upVotePost(postId, userId, userPassword);
        if(!res.isOK()) {
            throw new WebApplicationException(errorCodeToStatus(res.error()));
        }


    }

    @Override
    public void removeUpVotePost(String postId, String userId, String userPassword) throws Exception {
        Log.info("removeUpVotePost : " + postId);
        Result<Void> res = impl.removeUpVotePost(postId, userId, userPassword);
        if(!res.isOK()) {
            throw new WebApplicationException(errorCodeToStatus(res.error()));
        }

    }

    @Override
    public void downVotePost(String postId, String userId, String userPassword) throws Exception {
        Log.info("downVotePost : " + postId);
        Result<Void> res = impl.downVotePost(postId, userId, userPassword);
        if(!res.isOK()) {
            throw new WebApplicationException(errorCodeToStatus(res.error()));
        }

    }

    @Override
    public void removeDownVotePost(String postId, String userId, String userPassword) throws Exception {
        Log.info("removeDownVotePost : " + postId);
        Result<Void> res = impl.removeDownVotePost(postId, userId, userPassword);
        if(!res.isOK()) {
            throw new WebApplicationException(errorCodeToStatus(res.error()));
        }

    }

    @Override
    public Integer getupVotes(String postId) {
        Log.info("getupVotes : " + postId);
        Result<Integer> res = impl.getupVotes(postId);
        if(!res.isOK()) {
            throw new WebApplicationException(errorCodeToStatus(res.error()));
        }
        return res.value();
    }

    @Override
    public Integer getDownVotes(String postId) {
        Log.info("getDownVotes : " + postId);
        Result<Integer> res = impl.getDownVotes(postId);
        if(!res.isOK()) {
            throw new WebApplicationException(errorCodeToStatus(res.error()));
        }
        return res.value();
    }

    @Override
    public void setNullAuthor(String userId) {
        Log.info("setNullAuthor : " + userId);
        Result<Void> res = impl.setNullAuthor(userId);
        if (!res.isOK()) {
            throw new WebApplicationException(errorCodeToStatus(res.error()));
        }
    }


    protected static Response.Status errorCodeToStatus(Result.ErrorCode error ) {
        Response.Status status =  switch( error) {
            case NOT_FOUND -> Response.Status.NOT_FOUND;
            case CONFLICT -> Response.Status.CONFLICT;
            case FORBIDDEN -> Response.Status.FORBIDDEN;
            case NOT_IMPLEMENTED -> Response.Status.NOT_IMPLEMENTED;
            case BAD_REQUEST -> Response.Status.BAD_REQUEST;
            default -> Response.Status.INTERNAL_SERVER_ERROR;
        };

        return status;
    }

}
