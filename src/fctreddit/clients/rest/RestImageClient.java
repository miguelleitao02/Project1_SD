package fctreddit.clients.rest;

import fctreddit.api.java.Result;
import fctreddit.api.java.Result.ErrorCode;
import fctreddit.api.rest.RestImage;
import fctreddit.clients.java.ImageClient;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;


import java.net.URI;
import java.util.logging.Logger;

public class RestImageClient extends ImageClient {
    private static Logger Log = Logger.getLogger(RestUsersClient.class.getName());

    final URI serverURI;
    final Client client;
    final ClientConfig config;

    final WebTarget target;

    public RestImageClient( URI serverURI) {
        this.serverURI = serverURI;

        this.config = new ClientConfig();

        config.property( ClientProperties.READ_TIMEOUT, READ_TIMEOUT);
        config.property( ClientProperties.CONNECT_TIMEOUT, CONNECT_TIMEOUT);


        this.client = ClientBuilder.newClient(config);

        target = client.target( serverURI ).path( RestImage.PATH );
    }


    @Override
    public Result<String> createImage(String userId, byte[] imageContents, String password) {
        for(int i = 0; i < MAX_RETRIES ; i++) {
            try {
                Response r = target.path(userId).queryParam(RestImage.PASSWORD, password).request()
                        .post(Entity.entity(imageContents, MediaType.APPLICATION_OCTET_STREAM_TYPE));

                int status = r.getStatus();
                if (status != Response.Status.OK.getStatusCode()) {
                    return Result.error( getErrorCodeFrom(status));
                }
                else {
                    return Result.ok( r.readEntity( String.class ));
                }

            } catch( ProcessingException x ) {
                Log.info(x.getMessage());

                try {
                    Thread.sleep(RETRY_SLEEP);
                } catch (InterruptedException e) {
                    //Nothing to be done here.
                }
            }
            catch( Exception x ) {
                x.printStackTrace();
            }
        }
        return Result.error(  ErrorCode.TIMEOUT );

    }

    @Override
    public Result<byte[]> getImage(String userId, String imageId) {
        for(int i = 0; i < MAX_RETRIES ; i++) {
            try {
                Response r = target.path(userId).path(imageId).request()
                        .accept(MediaType.APPLICATION_OCTET_STREAM_TYPE)
                        .get();

                int status = r.getStatus();
                if (status != Response.Status.OK.getStatusCode()) {
                    return Result.error( getErrorCodeFrom(status));
                }
                else {
                    return Result.ok( r.readEntity( byte[].class ));
                }
            } catch( ProcessingException x ) {
                Log.info(x.getMessage());

                try {
                    Thread.sleep(RETRY_SLEEP);
                } catch (InterruptedException e) {
                    //Nothing to be done here.
                }
            }
            catch( Exception x ) {
                x.printStackTrace();
            }
        }
        return Result.error(  ErrorCode.TIMEOUT );
    }

    @Override
    public Result<Void> deleteImage(String userId, String imageId, String password) {
        for(int i = 0; i < MAX_RETRIES ; i++) {
            try {
                Response r = target.path(userId).path(imageId)
                        .queryParam(RestImage.PASSWORD, password).request()
                        .delete();

                int status = r.getStatus();
                if (status != Response.Status.OK.getStatusCode()) {
                    return Result.error( getErrorCodeFrom(status));
                }
                else {
                    return Result.ok( r.readEntity( Void.class ));
                }

            } catch( ProcessingException x ) {
                Log.info(x.getMessage());

                try {
                    Thread.sleep(RETRY_SLEEP);
                } catch (InterruptedException e) {
                    //Nothing to be done here.
                }
            }
            catch( Exception x ) {
                x.printStackTrace();
            }
        }
        return Result.error(  ErrorCode.TIMEOUT );
    }

    public static Result.ErrorCode getErrorCodeFrom(int status) {
        return switch (status) {
            case 200, 209 -> Result.ErrorCode.OK;
            case 409 -> Result.ErrorCode.CONFLICT;
            case 403 -> Result.ErrorCode.FORBIDDEN;
            case 404 -> Result.ErrorCode.NOT_FOUND;
            case 400 -> Result.ErrorCode.BAD_REQUEST;
            case 500 -> Result.ErrorCode.INTERNAL_ERROR;
            case 501 -> Result.ErrorCode.NOT_IMPLEMENTED;
            default -> Result.ErrorCode.INTERNAL_ERROR;
        };
    }
}
