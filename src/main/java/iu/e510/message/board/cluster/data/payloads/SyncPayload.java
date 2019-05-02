package iu.e510.message.board.cluster.data.payloads;

import iu.e510.message.board.cluster.data.DataManager;
import iu.e510.message.board.tom.common.payloads.NonBlockingPayload;
import iu.e510.message.board.tom.common.payloads.Payload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Set;

public class SyncPayload extends Payload<Set<String>> implements NonBlockingPayload {
    private static Logger logger = LoggerFactory.getLogger(SyncPayload.class);

    public SyncPayload(Set<String> content) {
        super(content);
    }

    @Override
    public void process(DataManager dataManager) {
        logger.info("Processing SYNC payload: " + this);

        // topics set which needs to be grabbed from the peers
        Set<String> topics = getContent();

        for (String topic : topics) {
            if (dataManager.hasData(topic)) {
                logger.info("Nothing to do! Data available locally for: " + topic);
            } else {
                try {
                    logger.info("Requesting data from the cluster for: " + topic);
                    byte[] data = dataManager.requestData(topic);
                    logger.debug("Received data: " + Arrays.toString(data));

                    // add data to topics set and zk
                    logger.info("Adding topic: " + topic);
                    dataManager.addTopicData(topic);
                    dataManager.getDatabase().addPostsDataFromByteArray(data);
                } catch (Exception e) {
                    throw new RuntimeException("unable to process SYNC payload", e);
                }

            }
        }

        logger.info("Sync complete. Current topics: " + dataManager.getAllTopics());
    }

    @Override
    public String toString() {
        return "SyncPayload{" +
                "content=" + getContent() +
                '}';
    }
}
