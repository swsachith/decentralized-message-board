# decentralized-message-board

## How to Run

#### Pre-requisites
Download Zookeeper __3.5.x__ from the [Zookeeper website](https://zookeeper.apache.org/releases.html#download)
and configure the ZK cluster. 
Configure the location of the ZK cluster in the conf/config.properties file

#### Steps

##### Servers
From the <source_dir>/bin, run
```bash
java -jar iu.e510.message.board-server.jar
```

You can provide custom configurations via,
```bash
java -jar -Dconfig.file=<CONFIG_FILE> iu.e510.message.board-server.jar
```

##### Client
From the <source_dir>/bin, run
```bash
java -jar iu.e510.message.board-client.jar
```

#### Commands supported through the client stdin
-   Posting: 
    ```post, <topic>, <title>, <content>```

#### Todo:
##### Cluster configuration
    -   Node joins, re-distribute data
    -   Node Leaves, re-replciate data
    
##### Consistency
    -   Handle consistency when posting