package fctreddit.server.rest;

import fctreddit.impl.server.rest.ContentResourse;
import fctreddit.multicast.Discovery;
import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import java.net.InetAddress;
import java.net.URI;
import java.util.logging.Logger;

public class ContentServerRest {
    private static Logger Log = Logger.getLogger(ContentServerRest.class.getName());

    static {
        System.setProperty("java.net.preferIPv4Stack", "true");
        System.setProperty("java.util.logging.SimpleFormatter.format", "%4$s: %5$s\n");
    }

    public static final int PORT = 8082;
    public static final String SERVICE = "Content";
    private static final String SERVER_URI_FMT = "http://%s:%s/rest";

    public static void main(String[] args) {
        try {

            ResourceConfig config = new ResourceConfig();

            String ip = InetAddress.getLocalHost().getHostAddress();
            String serverURI = String.format(SERVER_URI_FMT, ip, PORT);
            config.register(new ContentResourse(serverURI));
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
