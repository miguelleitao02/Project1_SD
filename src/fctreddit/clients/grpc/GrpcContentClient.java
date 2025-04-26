package fctreddit.clients.grpc;

import fctreddit.api.Post;
import fctreddit.api.java.Result;
import fctreddit.clients.java.ContentClient;
import fctreddit.impl.grpc.generated_java.ContentGrpc;
import fctreddit.impl.grpc.generated_java.ContentProtoBuf;
import fctreddit.impl.grpc.util.DataModelAdaptor;
import io.grpc.*;
import io.grpc.internal.PickFirstLoadBalancerProvider;

import java.net.URI;
import java.util.List;
import java.util.logging.Logger;

public class GrpcContentClient extends ContentClient {

    private static Logger Log = Logger.getLogger(GrpcContentClient.class.getName());
    static {
        LoadBalancerRegistry.getDefaultRegistry().register(new PickFirstLoadBalancerProvider());
    }

    final ContentGrpc.ContentBlockingStub stub;

    public GrpcContentClient(URI serverURI) {
        Channel channel = ManagedChannelBuilder.forAddress(serverURI.getHost(), serverURI.getPort())
                .enableRetry().usePlaintext().build();
        stub = ContentGrpc.newBlockingStub( channel );
    }
    @Override
    public Result<String> createPost(Post post, String userPassword) {
        try{
            ContentProtoBuf.CreatePostResult res = stub.createPost(ContentProtoBuf.CreatePostArgs.newBuilder()
                    .setPost(DataModelAdaptor.Post_to_GrpcPost(post))
                    .setPassword(userPassword)
                    .build());
            return Result.ok(res.getPostId());
        }catch (StatusRuntimeException sre){
            return Result.error( statusToErrorCode(sre.getStatus()));
        }
    }

    @Override
    public Result<List<String>> getPosts(long timestamp, String sortOrder) {
        try{
            ContentProtoBuf.GetPostsResult res = stub.getPosts(ContentProtoBuf.GetPostsArgs.newBuilder()
                    .setTimestamp(timestamp)
                    .setSortOrder(sortOrder)
                    .build());
            return Result.ok(res.getPostIdList());
        }catch (StatusRuntimeException sre){
            return Result.error( statusToErrorCode(sre.getStatus()));
        }
    }

    @Override
    public Result<Post> getPost(String postId) {
        try {
            ContentProtoBuf.GrpcPost res = stub.getPost(ContentProtoBuf.GetPostArgs.newBuilder()
                    .setPostId(postId)
                    .build());
            return Result.ok(DataModelAdaptor.GrpcPost_to_Post(res));
        }catch (StatusRuntimeException sre){
            return Result.error( statusToErrorCode(sre.getStatus()));
        }
    }

    @Override
    public Result<List<String>> getPostAnswers(String postId, long maxTimeout) {
        try{
            ContentProtoBuf.GetPostsResult res = stub.getPostAnswers(ContentProtoBuf.GetPostAnswersArgs.newBuilder()
                    .setPostId(postId)
                    .setTimeout(maxTimeout)
                    .build());
            return Result.ok(res.getPostIdList());
        }catch (StatusRuntimeException sre){
            return Result.error( statusToErrorCode(sre.getStatus()));
        }
    }

    @Override
    public Result<Post> updatePost(String postId, String userPassword, Post post) {
        try{
            ContentProtoBuf.GrpcPost res = stub.updatePost(ContentProtoBuf.UpdatePostArgs.newBuilder()
                    .setPostId(postId)
                    .setPassword(userPassword)
                    .setPost(DataModelAdaptor.Post_to_GrpcPost(post))
                    .build());

            return Result.ok(DataModelAdaptor.GrpcPost_to_Post(res));
        }catch (StatusRuntimeException sre){
            return Result.error( statusToErrorCode(sre.getStatus()));
        }
    }

    @Override
    public Result<Void> deletePost(String postId, String userPassword) {
        try{
            stub.deletePost(ContentProtoBuf.DeletePostArgs.newBuilder()
                    .setPostId(postId)
                    .setPassword(userPassword)
                    .build());
            return Result.ok();
        }catch (StatusRuntimeException sre){
            return Result.error( statusToErrorCode(sre.getStatus()));
        }

    }

    @Override
    public Result<Void> upVotePost(String postId, String userId, String userPassword) {
        try{
            stub.upVotePost(ContentProtoBuf.ChangeVoteArgs.newBuilder()
                    .setPostId(postId)
                    .setUserId(userId)
                    .setPassword(userPassword)
                    .build());
            return Result.ok();
        }catch (StatusRuntimeException sre){
            return Result.error( statusToErrorCode(sre.getStatus()));
        }
    }

    @Override
    public Result<Void> removeUpVotePost(String postId, String userId, String userPassword) {
        try{
            stub.removeUpVotePost(ContentProtoBuf.ChangeVoteArgs.newBuilder()
                    .setPostId(postId)
                    .setUserId(userId)
                    .setPassword(userPassword)
                    .build());
            return Result.ok();
        }catch (StatusRuntimeException sre){
            return Result.error( statusToErrorCode(sre.getStatus()));
        }
    }

    @Override
    public Result<Void> downVotePost(String postId, String userId, String userPassword) {
        try{
            stub.downVotePost(ContentProtoBuf.ChangeVoteArgs.newBuilder()
                    .setPostId(postId)
                    .setUserId(userId)
                    .setPassword(userPassword)
                    .build());
            return Result.ok();
        }catch (StatusRuntimeException sre){
            return Result.error( statusToErrorCode(sre.getStatus()));
        }
    }

    @Override
    public Result<Void> removeDownVotePost(String postId, String userId, String userPassword) {
        try{
            stub.removeDownVotePost(ContentProtoBuf.ChangeVoteArgs.newBuilder()
                    .setPostId(postId)
                    .setUserId(userId)
                    .setPassword(userPassword)
                    .build());
            return Result.ok();
        }catch (StatusRuntimeException sre){
            return Result.error( statusToErrorCode(sre.getStatus()));
        }
    }

    @Override
    public Result<Integer> getupVotes(String postId) {
        try{
            ContentProtoBuf.VoteCountResult res = stub.getUpVotes(ContentProtoBuf.GetPostArgs.newBuilder()
                    .setPostId(postId)
                    .build());
            return Result.ok(res.getCount());
        }catch (StatusRuntimeException sre){
            return Result.error( statusToErrorCode(sre.getStatus()));
        }

    }

    @Override
    public Result<Integer> getDownVotes(String postId) {
        try{
            ContentProtoBuf.VoteCountResult res = stub.getDownVotes(ContentProtoBuf.GetPostArgs.newBuilder()
                    .setPostId(postId)
                    .build());
            return Result.ok(res.getCount());
        }catch (StatusRuntimeException sre){
            return Result.error( statusToErrorCode(sre.getStatus()));
        }
    }

    @Override
    public Result<Void> setNullAuthor(String userId) {
        try {
            stub.setNullAuthor(ContentProtoBuf.SetNullAuthorArgs.newBuilder()
                    .setUserId(userId)
                    .build());
            return Result.ok();
        } catch (StatusRuntimeException sre) {
            return Result.error(statusToErrorCode(sre.getStatus()));
        }
    }


    static Result.ErrorCode statusToErrorCode(Status status ) {
        return switch( status.getCode() ) {
            case OK -> Result.ErrorCode.OK;
            case NOT_FOUND -> Result.ErrorCode.NOT_FOUND;
            case ALREADY_EXISTS -> Result.ErrorCode.CONFLICT;
            case PERMISSION_DENIED -> Result.ErrorCode.FORBIDDEN;
            case INVALID_ARGUMENT -> Result.ErrorCode.BAD_REQUEST;
            case UNIMPLEMENTED -> Result.ErrorCode.NOT_IMPLEMENTED;
            default -> Result.ErrorCode.INTERNAL_ERROR;
        };
    }
}
