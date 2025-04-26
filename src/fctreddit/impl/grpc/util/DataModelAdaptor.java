package fctreddit.impl.grpc.util;

import fctreddit.api.Post;
import fctreddit.api.User;
import fctreddit.impl.grpc.generated_java.ContentProtoBuf.GrpcPost;
import fctreddit.impl.grpc.generated_java.UsersProtoBuf.GrpcUser;
import fctreddit.impl.grpc.generated_java.UsersProtoBuf.GrpcUser.Builder;

public class DataModelAdaptor {

    public static User GrpcUser_to_User( GrpcUser from )  {
        String userId = from.getUserId();
        String fullName = from.getFullName();
        String email = from.getEmail();
        String password = from.getPassword();
        String avatarUrl = from.getAvatarUrl();
        if(userId.isBlank())
            userId = null;
        if(fullName.isBlank())
            fullName = null;
        if(email.isBlank())
            email = null;
        if(password.isBlank())
            password = null;
        if(avatarUrl.isBlank())
            avatarUrl = null;


        return new User(userId, fullName, email, password, avatarUrl);
    }

    public static GrpcUser User_to_GrpcUser( User from )  {
        Builder b = GrpcUser.newBuilder()
                .setUserId( from.getUserId())
                .setPassword( from.getPassword())
                .setEmail( from.getEmail())
                .setFullName( from.getFullName());

        if(from.getAvatarUrl() != null)
            b.setAvatarUrl( from.getAvatarUrl());

        return b.build();
    }

    public static Post GrpcPost_to_Post (GrpcPost from){
        String postId = from.getPostId();
        String authorId = from.getAuthorId();
        long creationTimestamp = from.getCreationTimestamp();
        String content = from.getContent();
        String mediaUrl = from.getMediaUrl();
        String parentUrl = from.getParentUrl();
        int upVote = from.getUpVote();
        int downVote = from.getDownVote();
        if (postId.isBlank()) {
            postId = null;
        }
        if (authorId.isBlank()) {
            authorId = null;
        }
        if(content.isBlank()){
            content = null;
        }
        if (mediaUrl.isBlank()) {
            mediaUrl = null;
        }
        if (parentUrl.isBlank()) {
            parentUrl = null;
        }

        return new Post(postId, authorId, creationTimestamp, content, mediaUrl, parentUrl, upVote, downVote);
    }

    public static GrpcPost Post_to_GrpcPost( Post from){
        GrpcPost.Builder b = GrpcPost.newBuilder();

        b.setCreationTimestamp(from.getCreationTimestamp());
        b.setUpVote(from.getUpVote());
        b.setDownVote(from.getDownVote());

        if (from.getPostId() != null)
            b.setPostId(from.getPostId());
        if(from.getAuthorId() != null)
            b.setAuthorId(from.getAuthorId());
        if (from.getMediaUrl() != null)
            b.setMediaUrl(from.getMediaUrl());
        if (from.getParentUrl() != null)
            b.setParentUrl(from.getParentUrl());
        if(from.getContent() != null)
            b.setContent(from.getContent());



        return b.build();
    }

}
