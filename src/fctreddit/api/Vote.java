package fctreddit.api;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class Vote {

    @Id
    private String userId;
    @Id
    private String postId;
    private boolean IsUpVote;

    public Vote(String userId, String postId, boolean isUpVote) {
        this.userId = userId;
        this.postId = postId;
        IsUpVote = isUpVote;
    }

    public Vote() {

    }

    public String getUserId() {
        return userId;
    }

    public String getPostId() {
        return postId;
    }

    public boolean isUpVote() {
        return IsUpVote;
    }

    public void setVote(boolean vote) {
        IsUpVote = vote;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((userId == null) ? 0 : userId.hashCode());
        result = prime * result + ((postId == null) ? 0 : postId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Vote other = (Vote) obj;
        if (userId == null) {
            if (other.userId != null)
                return false;
        } else if (!userId.equals(other.userId))
            return false;
        if (postId == null) {
            if (other.postId != null)
                return false;
        } else if (!postId.equals(other.postId))
            return false;
        return true;
    }

}
