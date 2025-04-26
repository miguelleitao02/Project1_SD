package fctreddit.clients;

import java.io.IOException;
import java.util.Hashtable;
import java.util.logging.Logger;
import fctreddit.api.java.Result;
import fctreddit.clients.grpc.GrpcContentClient;
import fctreddit.clients.grpc.GrpcImageClient;
import fctreddit.clients.java.ContentClient;
import fctreddit.clients.java.ImageClient;
import fctreddit.clients.java.UsersClient;
import fctreddit.clients.grpc.GrpcUsersClient;
import fctreddit.clients.rest.RestContentClient;
import fctreddit.clients.rest.RestUsersClient;
import fctreddit.clients.rest.RestImageClient;
import fctreddit.multicast.Discovery;

import java.net.URI;
import java.net.InetSocketAddress;

public class ProxyClient {
    private static final Logger Log = Logger.getLogger(ProxyClient.class.getName());
    private static final String USERS_SERVICE = "Users";
    private static final String IMAGE_SERVICE = "Image";
    private static final String CONTENT_SERVICE = "Content";
    private static final int MIN_REPLIES = 10;

    private final Discovery discovery;

    public ProxyClient(Discovery discovery) {
        this.discovery = discovery;
    }

    public UsersClient getUsersClient(){
        URI[] uris = discovery.knownUrisOf(USERS_SERVICE, MIN_REPLIES);
        return buildUsersClient(uris[0]);
    }

    public ImageClient getImageClient() {
        URI[] uris = discovery.knownUrisOf(IMAGE_SERVICE, MIN_REPLIES);
        return buildImageClient(uris[0]);
    }

    public ContentClient getContentClient() {
        URI[] uris = discovery.knownUrisOf(CONTENT_SERVICE, MIN_REPLIES);
        return buildContentClient(uris[0]);
    }

    private ContentClient buildContentClient(URI uri) {
        if (isGrpc(uri)) {
            return new GrpcContentClient(uri);
        } else {
            return new RestContentClient(uri);
        }
    }


    private UsersClient buildUsersClient(URI uri) {
        Log.info("Creating UsersClient for URI: " + uri);
        if (isGrpc(uri)) {
            return new GrpcUsersClient(uri);
        } else {
            return new RestUsersClient(uri);
        }
    }

    private ImageClient buildImageClient(URI uri) {
        Log.info("Creating ImageClient for URI: " + uri);
        if (isGrpc(uri)) {
            return new GrpcImageClient(uri);
        } else {
            return new RestImageClient(uri);
        }
    }

    private boolean isGrpc(URI uri) {
        String scheme = uri.getScheme();
        return scheme.equalsIgnoreCase("grpc");
    }
}
