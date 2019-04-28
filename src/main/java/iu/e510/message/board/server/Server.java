package iu.e510.message.board.server;

import iu.e510.message.board.cluster.ClusterManager;
import iu.e510.message.board.cluster.data.DataManager;
import iu.e510.message.board.cluster.data.DataManagerImpl;
import iu.e510.message.board.tom.MessageService;
import iu.e510.message.board.tom.MessageServiceImpl;
import iu.e510.message.board.util.Config;
import iu.e510.message.board.util.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Server {
    private static Logger logger = LoggerFactory.getLogger(Server.class);

    private String id;
    private Config config;
    private ClusterManager clusterManager;
    private DataManager dataManager;
    private MessageService messageService;
    private Registry registry;

    public Server(String nodeID) throws Exception {
        this.id = nodeID;
        this.config = new Config();
        String RMI_HOST = config.getConfig(Constants.RMI_REGISTRY_HOST);
        int RMI_PORT = Integer.parseInt(config.getConfig(Constants.RMI_REGISTRY_PORT));

        this.messageService = new MessageServiceImpl("tcp://" + id, id);
        this.clusterManager = new ClusterManager(id, messageService);
        this.dataManager = new DataManagerImpl(id);

        // Binding the RMI client stubs
        configRMIRegistry(RMI_HOST, RMI_PORT);
        ClientAPI clientAPI = new ClientAPIImpl(dataManager);
        Naming.bind("//" + RMI_HOST + ":" + RMI_PORT + "/MessageBoardServer" , clientAPI);
        logger.info("The client api is ready to accept requests");
    }

    public void run() throws Exception {

        dataManager.addData("hapoi", "hi");

        dataManager.addData("IN", "hi");
        System.out.println();
        System.out.println();
        int i = 0;
        while (i < Integer.MAX_VALUE) {
            Thread.sleep(3000);
            i++;
        }
    }

    private void configRMIRegistry(String host, int port) throws RemoteException {
        try {
            // check if a registry already exists at the port
            registry = LocateRegistry.getRegistry(host, port);
            registry.list();
            logger.info("Found the RMI Registry at host: " + host + " and port: "+ port);
        } catch (RemoteException e) {
            // create registry if one is not found
            logger.info("A registry cannot be found at host: " + host + " and port: "+ port);
            registry = LocateRegistry.createRegistry(port);
            logger.info("Created a new registry at at host: " + host + " and port: "+ port);
        }
    }
}
