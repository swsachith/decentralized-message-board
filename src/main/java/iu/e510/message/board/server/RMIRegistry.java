package iu.e510.message.board.server;

import iu.e510.message.board.util.Config;
import iu.e510.message.board.util.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Arrays;

public class RMIRegistry {
    private static Logger logger = LoggerFactory.getLogger(RMIRegistry.class);

    public static void main(String[] args) throws InterruptedException {
        Registry registry;
        try {
            registry = startRegistry();
            logger.info("RMI Registry Started");
            while (true) {
                logger.info("Current RMI Binds: " + Arrays.asList(registry.list()));
                Thread.sleep(5000);
            }
        } catch (RemoteException e) {
            System.out.println("Error Starting RMI Registry");
        }
    }

    private static Registry startRegistry() throws RemoteException {
        Config config = new Config();
        String host = config.getConfig(Constants.RMI_REGISTRY_HOST);
        int port = Integer.parseInt(config.getConfig(Constants.RMI_REGISTRY_PORT));
        Registry registry;
        try {
            // check if a registry already exists at the port
            registry = LocateRegistry.getRegistry(host, port);
            registry.list();
        } catch (RemoteException e) {
            // create registry if one is not found
            registry = LocateRegistry.createRegistry(port);
        }
        return registry;
    }
}
