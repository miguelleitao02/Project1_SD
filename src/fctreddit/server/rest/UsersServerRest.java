package fctreddit.server.rest;

import java.net.InetAddress;
import java.net.URI;
import java.util.logging.Logger;
import fctreddit.multicast.Discovery;
import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import fctreddit.impl.server.rest.UsersResource;

public class UsersServerRest {

    private static Logger Log = Logger.getLogger(UsersServerRest.class.getName());

    static {
        System.setProperty("java.net.preferIPv4Stack", "true");
        System.setProperty("java.util.logging.SimpleFormatter.format", "%4$s: %5$s\n");
    }

    public static final int PORT = 8080;
    public static final String SERVICE = "Users";
    private static final String SERVER_URI_FMT = "http://%s:%s/rest";

    public static void main(String[] args) {
        try {

            ResourceConfig config = new ResourceConfig();
            config.register(UsersResource.class);

            String ip = InetAddress.getLocalHost().getHostAddress();
            String serverURI = String.format(SERVER_URI_FMT, ip, PORT);
            JdkHttpServerFactory.createHttpServer( URI.create(serverURI), config);

            Log.info(String.format("%s Server ready @ %s\n",  SERVICE, serverURI));
            //More code can be executed here...
            Discovery discovery = new Discovery(Discovery.DISCOVERY_ADDR, SERVICE, serverURI);
            discovery.start();
            Log.info("Discovery iniciado para serviço: " + SERVICE);

        } catch( Exception e) {
            Log.severe(e.getMessage());
        }
    }
}
