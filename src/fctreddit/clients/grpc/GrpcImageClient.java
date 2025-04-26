package fctreddit.clients.grpc;

import com.google.protobuf.ByteString;
import fctreddit.api.java.Result;
import fctreddit.clients.java.ImageClient;
import fctreddit.impl.grpc.generated_java.ImageGrpc;
import fctreddit.impl.grpc.generated_java.ImageProtoBuf;
import io.grpc.*;
import io.grpc.internal.PickFirstLoadBalancerProvider;

import java.net.URI;
import java.util.Iterator;
import java.util.logging.Logger;

public class GrpcImageClient extends ImageClient {

    private static Logger Log = Logger.getLogger(GrpcImageClient.class.getName());
    static {
        LoadBalancerRegistry.getDefaultRegistry().register(new PickFirstLoadBalancerProvider());
    }

    final ImageGrpc.ImageBlockingStub stub;

    public GrpcImageClient(URI serverURI) {
        Channel channel = ManagedChannelBuilder.forAddress(serverURI.getHost(), serverURI.getPort())
                .enableRetry().usePlaintext().build();
        stub = ImageGrpc.newBlockingStub( channel );
    }

    @Override
    public Result<String> createImage(String userId, byte[] imageContents, String password) {
        try {
            ImageProtoBuf.CreateImageResult res = stub.createImage(ImageProtoBuf.CreateImageArgs.newBuilder()
                    .setUserId(userId)
                    .setImageContents(ByteString.copyFrom(imageContents))
                    .setPassword(password)
                    .build());
            return Result.ok(res.getImageId());
        }catch (StatusRuntimeException sre){
            return Result.error( statusToErrorCode(sre.getStatus()));
        }
    }

    @Override
    public Result<byte[]> getImage(String userId, String imageId) {
        try{
            Iterator<ImageProtoBuf.GetImageResult> res = stub.getImage(
                    ImageProtoBuf.GetImageArgs.newBuilder()
                            .setUserId(userId)
                            .setImageId(imageId)
                            .build());

            return Result.ok(res.next().getData().toByteArray());
        }catch (StatusRuntimeException sre){
            return Result.error( statusToErrorCode(sre.getStatus()));
        }
    }

    @Override
    public Result<Void> deleteImage(String userId, String imageId, String password) {
        try {
            ImageProtoBuf.DeleteImageResult res = stub.deleteImage(ImageProtoBuf.DeleteImageArgs.newBuilder()
                    .setUserId(userId)
                    .setImageId(imageId)
                    .setPassword(password)
                    .build());
            return Result.ok();
        }catch (StatusRuntimeException sre){
            return Result.error( statusToErrorCode(sre.getStatus()));
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
