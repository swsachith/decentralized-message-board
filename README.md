# decentralized-message-board

## How to Run

#### Pre-requisites
Download Zookeeper __3.5.x__ from the [Zookeeper website](https://zookeeper.apache.org/releases.html#download)
and configure the ZK cluster. 
Configure the location of the ZK cluster in the conf/config.properties file

#### Steps
##### RMI Registry
For the cluster to run, a RMI registry has to be running.
For the ease of use, we've added a jar that can be used to start a RMI registry. You can run the RMI registry using:
```bash
cd <source_dir>/bin/lib
java -jar iu.e510.message.board-rmiRegistry.jar
```

##### Servers
From the <source_dir>/bin/lib, run
```bash
java -jar iu.e510.message.board-server.jar -host <localhost> -port <port>
```

You can provide custom configurations via,
```bash
java -jar -Dconfig.file=<CONFIG_FILE> iu.e510.message.board-server.jar
```

##### Client
From the <source_dir>/bin/lib, run
```bash
java -jar iu.e510.message.board-client.jar
```

Optionally you can provide a client id by using the ```-clientID``` commandline argument. 
#### Commands supported through the client stdin
-   Posting: 
    ```
    post, <topic>, <title>, <content>
    ```
    
-   Replying: 
    ```
    reply, <topic>, <post_id>, <content>
    ```
    
-   Get posts for a particular topic: 
    ```
    getposts, <topic>
    ```
    
-   Get all the information for a post: 
    ```
    getpost, <topic>, <post_id>
    ```
    
-   Upvote a post: 
    ```
    uppost, <topic>, <post_id>
    ```
    
-   Down vote a post: 
    ```
    downpost, <topic>, <post_id>
    ```
    
-   Up vote a reply: 
    ```
    upreply, <topic>, <post_id>, <reply_id>
    ```
    
-   Down vote a post: 
    ```
    downreply, <topic>, <post_id>, <reply_id>
    ```