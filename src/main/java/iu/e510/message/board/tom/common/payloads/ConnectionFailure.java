package iu.e510.message.board.tom.common.payloads;

import iu.e510.message.board.cluster.data.DataManager;
import iu.e510.message.board.tom.common.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConnectionFailure extends Payload<String> implements BlockingPayload {
    private static final Logger logger = LoggerFactory.getLogger( ConnectionFailure.class );
    public ConnectionFailure(String cause) {
        super(cause);
    }

    @Override
    public Message process(DataManager dataManager) {
        logger.warn("Lost connection with Zookeeper, Network partitioned. Hence becoming " +
                "inconsistent until connection!");
        dataManager.setConsistency(false);

        logger.warn("Truncating data tables! ");
        dataManager.getDatabase().truncateTables();

        return null;
    }
}
