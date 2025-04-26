package fctreddit.clients.rest;

import fctreddit.api.Post;
import fctreddit.api.java.Result;
import fctreddit.api.rest.RestContent;
import fctreddit.clients.java.ContentClient;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.client.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;

import java.net.URI;
import java.util.List;
import java.util.logging.Logger;

import static fctreddit.api.rest.RestContent.*;
import static fctreddit.clients.rest.RestUsersClient.getErrorCodeFrom;

public class RestContentClient extends ContentClient {
    private static Logger Log = Logger.getLogger(RestContentClient.class.getName());
    final URI serverURI;
    final Client client;
    final ClientConfig config;
    final WebTarget target;


    public RestContentClient(URI serverURI) {
        this.serverURI = serverURI;

        this.config = new ClientConfig();

        config.property( ClientProperties.READ_TIMEOUT, READ_TIMEOUT);
        config.property( ClientProperties.CONNECT_TIMEOUT, CONNECT_TIMEOUT);


        this.client = ClientBuilder.newClient(config);

        target = client.target( serverURI ).path( RestContent.PATH );
    }


    @Override
    public Result<String> createPost(Post post, String userPassword) {
        Response r = executeOperationPost(target.queryParam(RestContent.PASSWORD, userPassword).request()
                .accept(MediaType.APPLICATION_JSON), Entity.entity(post, MediaType.APPLICATION_JSON));
        if (r == null){
            return  Result.error(  Result.ErrorCode.TIMEOUT );
        }
        int status = r.getStatus();
        if (status != Response.Status.OK.getStatusCode()) {
            return Result.error( getErrorCodeFrom(status));
        }
        else {
            return Result.ok( r.readEntity( String.class ));
        }
    }

    @Override
    public Result<List<String>> getPosts(long timestamp, String sortOrder) {
        Response r = executeOperationGet(target.queryParam(RestContent.TIMESTAMP, timestamp).queryParam(RestContent.SORTBY, sortOrder).request()
                .accept(MediaType.APPLICATION_JSON));
        if (r == null){
            return  Result.error(  Result.ErrorCode.TIMEOUT );
        }
        int status = r.getStatus();
        if (status != Response.Status.OK.getStatusCode()) {
            return Result.error( getErrorCodeFrom(status));
        }
        else {
            return Result.ok( r.readEntity(List.class));
        }
    }

    @Override
    public Result<Post> getPost(String postId) {
        Response r = executeOperationGet(target.path(postId).request()
                .accept(MediaType.APPLICATION_JSON));
        if (r == null){
            return  Result.error(  Result.ErrorCode.TIMEOUT );
        }
        int status = r.getStatus();
        if (status != Response.Status.OK.getStatusCode()) {
            return Result.error( getErrorCodeFrom(status));
        }
        else {
            return Result.ok( r.readEntity(Post.class));
        }
    }

    @Override
    public Result<List<String>> getPostAnswers(String postId, long maxTimeout) {
        Response r = executeOperationGet(target.path(postId).path(MOST_REPLIES).queryParam(RestContent.TIMEOUT, maxTimeout).request()
                .accept(MediaType.APPLICATION_JSON));
        if (r == null){
            return  Result.error(  Result.ErrorCode.TIMEOUT );
        }
        int status = r.getStatus();
        if (status != Response.Status.OK.getStatusCode()) {
            return Result.error( getErrorCodeFrom(status));
        }
        else {
            return Result.ok( r.readEntity(List.class));
        }
    }

    @Override
    public Result<Post> updatePost(String postId, String userPassword, Post post) {
        Response r = executeOperationPost(target.path(postId).queryParam(RestContent.PASSWORD, userPassword).request()
                .accept(MediaType.APPLICATION_JSON) , Entity.entity(post, MediaType.APPLICATION_JSON));

        int status = r.getStatus();
        if (status != Response.Status.OK.getStatusCode()) {
            return Result.error( getErrorCodeFrom(status));
        }
        else {
            return Result.ok( r.readEntity(Post.class));
        }
    }

    @Override
    public Result<Void> deletePost(String postId, String userPassword) {
        Response r = executeOperationDelete(target.path(postId).queryParam(RestContent.PASSWORD, userPassword).request());
        if (r == null){
            return  Result.error(  Result.ErrorCode.TIMEOUT );
        }
        int status = r.getStatus();
        if (status != Response.Status.OK.getStatusCode()) {
            return Result.error( getErrorCodeFrom(status));
        }
        else {
            return Result.ok( r.readEntity(Void.class));
        }
    }

    @Override
    public Result<Void> upVotePost(String postId, String userId, String userPassword) {
        Post post = getPost(postId).value();
        Response r = executeOperationPost(target.path(postId).path(UPVOTE).path(userId).queryParam(RestContent.PASSWORD, userPassword).request()
                .accept(MediaType.APPLICATION_JSON), Entity.entity(post, MediaType.APPLICATION_JSON));
        if (r == null){
            return  Result.error(  Result.ErrorCode.TIMEOUT );
        }
        int status = r.getStatus();
        if (status != Response.Status.OK.getStatusCode()) {
            return Result.error( getErrorCodeFrom(status));
        }
        else {
            return Result.ok( r.readEntity(Void.class));
        }
    }

    @Override
    public Result<Void> removeUpVotePost(String postId, String userId, String userPassword) {
        Response r = executeOperationDelete(target.path(postId).path(UPVOTE).path(userId).queryParam(RestContent.PASSWORD, userPassword).request()
                .accept(MediaType.APPLICATION_JSON));
        if (r == null){
            return  Result.error(  Result.ErrorCode.TIMEOUT );
        }
        int status = r.getStatus();
        if (status != Response.Status.OK.getStatusCode()) {
            return Result.error( getErrorCodeFrom(status));
        }
        else {
            return Result.ok( r.readEntity(Void.class));
        }
    }

    @Override
    public Result<Void> downVotePost(String postId, String userId, String userPassword) {
        Post post = getPost(postId).value();
        Response r = executeOperationPost(target.path(postId).path(DOWNVOTE).path(userId).queryParam(RestContent.PASSWORD, userPassword).request()
                .accept(MediaType.APPLICATION_JSON), Entity.entity(post, MediaType.APPLICATION_JSON));

        if (r == null){
            return  Result.error(  Result.ErrorCode.TIMEOUT );
        }
        int status = r.getStatus();
        if (status != Response.Status.OK.getStatusCode()) {
            return Result.error( getErrorCodeFrom(status));
        }
        else {
            return Result.ok( r.readEntity(Void.class));
        }
    }

    @Override
    public Result<Void> removeDownVotePost(String postId, String userId, String userPassword) {
        Response r = executeOperationDelete(target.path(postId).path(DOWNVOTE).path(userId).queryParam(RestContent.PASSWORD, userPassword).request()
                .accept(MediaType.APPLICATION_JSON));
        if (r == null){
            return  Result.error(  Result.ErrorCode.TIMEOUT );
        }
        int status = r.getStatus();
        if (status != Response.Status.OK.getStatusCode()) {
            return Result.error( getErrorCodeFrom(status));
        }
        else {
            return Result.ok( r.readEntity(Void.class));
        }
    }

    @Override
    public Result<Integer> getupVotes(String postId) {
        Response r = executeOperationGet(target.path(postId).path(UPVOTE)
                .request());
        if (r == null){
            return  Result.error(  Result.ErrorCode.TIMEOUT );
        }
        int status = r.getStatus();
        if (status != Response.Status.OK.getStatusCode()) {
            return Result.error( getErrorCodeFrom(status));
        }
        else {
            return Result.ok( r.readEntity(Integer.class));
        }
    }

    @Override
    public Result<Integer> getDownVotes(String postId) {
        Response r = executeOperationGet(target.path(postId).path(DOWNVOTE)
                .request());

        if (r == null){
            return  Result.error(  Result.ErrorCode.TIMEOUT );
        }
        int status = r.getStatus();
        if (status != Response.Status.OK.getStatusCode()) {
            return Result.error( getErrorCodeFrom(status));
        }
        else {
            return Result.ok( r.readEntity(Integer.class));
        }
    }

    @Override
    public Result<Void> setNullAuthor(String userId) {
        Response r = executeOperationDelete(target.path("setNullAuthor")
                    .path(userId)
                    .request());

        if (r == null){
            return  Result.error(  Result.ErrorCode.TIMEOUT );
        }
        int status = r.getStatus();
        if (status != Response.Status.OK.getStatusCode()) {
            return Result.error( getErrorCodeFrom(status));
        }
        else {
            return Result.ok();
        }

    }

    private Response executeOperationGet(Invocation.Builder req) {
        for (int i = 0; i < MAX_RETRIES; i++) {
            try {
                return req.get();
            }
            catch (ProcessingException x) {
                Log.info(x.getMessage());
                try {
                    Thread.sleep(RETRY_SLEEP);
                } catch (InterruptedException ex) {
                    // Nothing to be done here.
                }
            } catch (Exception x) {
                x.printStackTrace();
            }
        }
        return null;
    }

    private Response executeOperationPost(Invocation.Builder req, Entity<?> e) {
        for (int i = 0; i < MAX_RETRIES; i++) {
            try {
                return req.post(e);
            }
            catch (ProcessingException x) {
                Log.info(x.getMessage());
                try {
                    Thread.sleep(RETRY_SLEEP);
                } catch (InterruptedException ex) {
                    // Nothing to be done here.
                }
            } catch (Exception x) {
                x.printStackTrace();
            }
        }
        return null;
    }

    private Response executeOperationDelete(Invocation.Builder req) {
        for (int i = 0; i < MAX_RETRIES; i++) {
            try {
                return req.delete();
            }
            catch (ProcessingException x) {
                Log.info(x.getMessage());
                try {
                    Thread.sleep(RETRY_SLEEP);
                } catch (InterruptedException ex) {
                    // Nothing to be done here.
                }
            } catch (Exception x) {
                x.printStackTrace();
            }
        }
        return null;
    }
}
