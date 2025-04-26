package fctreddit.impl.server.rest;

import fctreddit.api.java.Image;
import fctreddit.api.java.Result;
import fctreddit.api.rest.RestImage;
import fctreddit.impl.server.java.JavaImage;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import java.util.logging.Logger;


public class ImageResource implements RestImage {
    private static Logger Log = Logger.getLogger(ImageResource.class.getName());

    final Image impl;

    public ImageResource(String url) throws Exception {
        impl = new JavaImage(url);
    }


    @Override
    public String createImage(String userId, byte[] imageContents, String password) throws Exception {
        Log.info("createImage : " + userId);

        Result<String> res = impl.createImage(userId, imageContents, password);
        if(!res.isOK()) {
            throw new WebApplicationException(errorCodeToStatus(res.error()));
        }
        return res.value();
    }

    @Override
    public byte[] getImage(String userId, String imageId) {
        Log.info("getImage : " + imageId);
        Result<byte[]> res = impl.getImage(userId, imageId);
        if(!res.isOK()) {
            throw new WebApplicationException(errorCodeToStatus(res.error()));
        }
        return res.value();
    }

    @Override
    public void deleteImage(String userId, String imageId, String password) throws Exception {
        Log.info("deleteImage : " + imageId);
        Result<Void> res = impl.deleteImage(userId, imageId, password);
        if(!res.isOK()) {
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
