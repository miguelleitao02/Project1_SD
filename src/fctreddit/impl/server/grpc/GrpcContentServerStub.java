package fctreddit.impl.server.grpc;

import fctreddit.api.Post;
import fctreddit.api.java.Content;
import fctreddit.api.java.Result;
import fctreddit.impl.grpc.generated_java.ContentGrpc;
import fctreddit.impl.grpc.generated_java.ContentProtoBuf;
import fctreddit.impl.grpc.util.DataModelAdaptor;
import fctreddit.impl.server.java.JavaContent;
import io.grpc.BindableService;
import io.grpc.ServerServiceDefinition;
import io.grpc.stub.StreamObserver;

import java.util.List;

public class GrpcContentServerStub implements ContentGrpc.AsyncService, BindableService {
    Content impl;

    public GrpcContentServerStub(String serverUri) {
        this.impl = new JavaContent(serverUri);
    }

    @Override
    public final ServerServiceDefinition bindService() {
        return ContentGrpc.bindService(this);
    }

    @Override
    public void createPost(ContentProtoBuf.CreatePostArgs request, StreamObserver<ContentProtoBuf.CreatePostResult> responseObserver) {
        Result<String> res = null;
        try {
            res = impl.createPost(DataModelAdaptor.GrpcPost_to_Post(request.getPost()), request.getPassword());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        if ( ! res.isOK() )
            responseObserver.onError(errorCodeToStatus(res.error()));
        else{
            responseObserver.onNext( ContentProtoBuf.CreatePostResult.newBuilder().setPostId( res.value() ).build());
            responseObserver.onCompleted();
        }
    }

    @Override
    public void getPost(ContentProtoBuf.GetPostArgs request, StreamObserver<ContentProtoBuf.GrpcPost> responseObserver) {
        Result<Post> res = impl.getPost(request.getPostId());
        if ( ! res.isOK() )
            responseObserver.onError(errorCodeToStatus(res.error()));
        else{
            responseObserver.onNext(DataModelAdaptor.Post_to_GrpcPost(res.value()));
            responseObserver.onCompleted();
        }
    }

    @Override
    public void getPosts(ContentProtoBuf.GetPostsArgs request, StreamObserver<ContentProtoBuf.GetPostsResult> responseObserver) {
        Result<List<String>> res = impl.getPosts(request.getTimestamp(), request.getSortOrder());
        if (! res.isOK()){
            responseObserver.onError(errorCodeToStatus(res.error()));
        }
        else{
            responseObserver.onNext(ContentProtoBuf.GetPostsResult.newBuilder().addAllPostId( res.value() ).build());
            responseObserver.onCompleted();
        }

    }

    @Override
    public void getPostAnswers(ContentProtoBuf.GetPostAnswersArgs request, StreamObserver<ContentProtoBuf.GetPostsResult> responseObserver) {
        Result<List<String>> res = impl.getPostAnswers(request.getPostId(), request.getTimeout());
        if (! res.isOK()){
            responseObserver.onError(errorCodeToStatus(res.error()));
        }
        else{
            responseObserver.onNext(ContentProtoBuf.GetPostsResult.newBuilder().addAllPostId( res.value() ).build());
            responseObserver.onCompleted();
        }
    }

    @Override
    public void updatePost(ContentProtoBuf.UpdatePostArgs request, StreamObserver<ContentProtoBuf.GrpcPost> responseObserver) {
        Result<Post> res = null;
        try {
            res = impl.updatePost(request.getPostId(), request.getPassword(), DataModelAdaptor.GrpcPost_to_Post(request.getPost()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        if (!res.isOK()) {
            responseObserver.onError(errorCodeToStatus(res.error()));
        }
        else{
            responseObserver.onNext(DataModelAdaptor.Post_to_GrpcPost(res.value()));
            responseObserver.onCompleted();
        }

    }

    @Override
    public void deletePost(ContentProtoBuf.DeletePostArgs request, StreamObserver<ContentProtoBuf.EmptyMessage> responseObserver) {
        Result<Void> res = null;
        try {
            res = impl.deletePost(request.getPostId(), request.getPassword());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        if (!res.isOK()) {
            responseObserver.onError(errorCodeToStatus(res.error()));
        }
        else{
            responseObserver.onNext(ContentProtoBuf.EmptyMessage.newBuilder().build());
            responseObserver.onCompleted();
        }


    }

    @Override
    public void upVotePost(ContentProtoBuf.ChangeVoteArgs request, StreamObserver<ContentProtoBuf.EmptyMessage> responseObserver) {
        Result<Void> res = null;
        try {
            res = impl.upVotePost(request.getPostId(), request.getUserId(), request.getPassword());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        if (! res.isOK()) {
            responseObserver.onError(errorCodeToStatus(res.error()));
        }
        else{
            responseObserver.onNext(ContentProtoBuf.EmptyMessage.newBuilder().build());
            responseObserver.onCompleted();
        }

    }

    @Override
    public void removeUpVotePost(ContentProtoBuf.ChangeVoteArgs request, StreamObserver<ContentProtoBuf.EmptyMessage> responseObserver) {
        Result<Void> res = null;
        try {
            res = impl.removeUpVotePost(request.getPostId(), request.getUserId(), request.getPassword());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        if (! res.isOK()) {
            responseObserver.onError(errorCodeToStatus(res.error()));
        }
        else{
            responseObserver.onNext(ContentProtoBuf.EmptyMessage.newBuilder().build());
            responseObserver.onCompleted();
        }
    }

    @Override
    public void downVotePost(ContentProtoBuf.ChangeVoteArgs request, StreamObserver<ContentProtoBuf.EmptyMessage> responseObserver) {
        Result<Void> res = null;
        try {
            res = impl.downVotePost(request.getPostId(), request.getUserId(), request.getPassword());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        if (! res.isOK()) {
            responseObserver.onError(errorCodeToStatus(res.error()));
        }
        else{
            responseObserver.onNext(ContentProtoBuf.EmptyMessage.newBuilder().build());
            responseObserver.onCompleted();
        }
    }

    @Override
    public void removeDownVotePost(ContentProtoBuf.ChangeVoteArgs request, StreamObserver<ContentProtoBuf.EmptyMessage> responseObserver) {
        Result<Void> res = null;
        try {
            res = impl.removeDownVotePost(request.getPostId(), request.getUserId(), request.getPassword());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        if (! res.isOK()) {
            responseObserver.onError(errorCodeToStatus(res.error()));
        }
        else{
            responseObserver.onNext(ContentProtoBuf.EmptyMessage.newBuilder().build());
            responseObserver.onCompleted();
        }
    }

    @Override
    public void getUpVotes(ContentProtoBuf.GetPostArgs request, StreamObserver<ContentProtoBuf.VoteCountResult> responseObserver) {
        Result<Integer> res = impl.getupVotes(request.getPostId());
        if (! res.isOK()) {
            responseObserver.onError(errorCodeToStatus(res.error()));
        }
        else{
            responseObserver.onNext(ContentProtoBuf.VoteCountResult.newBuilder().setCount(res.value()).build());
            responseObserver.onCompleted();
        }
    }

    @Override
    public void getDownVotes(ContentProtoBuf.GetPostArgs request, StreamObserver<ContentProtoBuf.VoteCountResult> responseObserver) {
        Result<Integer> res = impl.getDownVotes(request.getPostId());
        if (! res.isOK()) {
            responseObserver.onError(errorCodeToStatus(res.error()));
        }
        else{
            responseObserver.onNext(ContentProtoBuf.VoteCountResult.newBuilder().setCount(res.value()).build());
            responseObserver.onCompleted();
        }
    }

    @Override
    public void setNullAuthor(ContentProtoBuf.SetNullAuthorArgs request, StreamObserver<ContentProtoBuf.EmptyMessage> responseObserver) {
        Result<Void> res = impl.setNullAuthor(request.getUserId());
        if (! res.isOK()) {
            responseObserver.onError(errorCodeToStatus(res.error()));
        }
        else{
            responseObserver.onNext(ContentProtoBuf.EmptyMessage.newBuilder().build());
            responseObserver.onCompleted();
        }
    }




    protected static Throwable errorCodeToStatus(Result.ErrorCode error ) {
        var status =  switch( error) {
            case NOT_FOUND -> io.grpc.Status.NOT_FOUND;
            case CONFLICT -> io.grpc.Status.ALREADY_EXISTS;
            case FORBIDDEN -> io.grpc.Status.PERMISSION_DENIED;
            case NOT_IMPLEMENTED -> io.grpc.Status.UNIMPLEMENTED;
            case BAD_REQUEST -> io.grpc.Status.INVALID_ARGUMENT;
            default -> io.grpc.Status.INTERNAL;
        };

        return status.asException();
    }
}
