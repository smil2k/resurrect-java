function listAllClientConnections() {
  var c = factory.findAll("org.apache.activemq.ActiveMQConnection");

  _printRaHeader();

  for (var i = 0; i < c.size(); i++) {
    var conn = c[i];
    _printRaLine(conn);

  }
  logger.println(c.size() + " connections.");
}

function listClientConnections() {
  var c = factory.findAll("org.apache.activemq.ActiveMQConnection");

  _printRaHeader();

  var cx = 0;
  for (var i = 0; i < c.size(); i++) {
    var conn = c[i];
    if (conn.started === "true") {
      cx++;
      _printRaLine(conn);
    }
  }
  logger.println(cx + " connections.");
}

function showClient(id) {
  var c = factory.findAll("org.apache.activemq.ActiveMQConnection");
  for (var i = 0; i < c.size(); i++) {
    var conn = c[i];
    if (conn.info.clientId === id) {
      logger.println("\nBroker Info:");
      printObject(conn.brokerInfo);

      logger.println("\nConnection Info:");
      printObject(conn.info);

      logger.println("\Connection:");
      var s = new TreeSet();
      s.add("dispatchAsync");
      s.add("useRetroactiveConsumer");
      s.add("transportFailed");
      s.add("closing");
      s.add("isConnectionInfoSentToBroker");
      s.add("queueOnlyConnection");
      s.add("disableTimeStampsByDefault");
      s.add("watchTopicAdvisories");
      s.add("exclusiveConsumer");
      s.add("copyMessageOnSend");
      s.add("transactedIndividualAck");
      s.add("rmIdFromConnectionId");
      s.add("nonBlockingRedelivery");
      s.add("checkForDuplicates");
      s.add("optimizeAcknowledge");
      s.add("optimizedMessageDispatch");
      s.add("alwaysSessionAsync");
      s.add("closed");
      s.add("sendAcksAsync");

      printObject(conn, s);

      logger.println("\nSessions:");
      for (var x = 0; x < conn.sessions.size(); x++) {
        logger.println("\nSession " + (x + 1) + ":");
        printObject(conn.sessions[x]);
      }
      break;
    }
  }
}

function listClientConnections() {
  var c = factory.findAll("org.apache.activemq.ActiveMQConnection");

  _printRaHeader();

  var cx = 0;
  for (var i = 0; i < c.size(); i++) {
    var conn = c[i];
    if (conn.started === "true") {
      cx++;
      _printRaLine(conn);
    }
  }
  logger.println(cx + " connections.");
}

function _printRaHeader() {
  logger.printf("\n\n%15s | %65s | %3s | %3s | %3s | %5s | %5s | %5s | %5s\n",
          "ObjId", "Client ID", "Dis", "Ses", "Con", "Start", "Closd", "Faild", "Created");
}

function _printRaLine(conn) {
  logger.printf("%15d | %-65s | %3d | %3d | %3d | %5s | %5s | %5s | %s\n",
          conn.objectId.objectId, conn.info.clientId, conn.dispatchers.size(), conn.sessions.size(),
          conn.connectionConsumers.size(),
          conn.started, conn.closed, conn.transportFailed, formatTimeUnixMs(conn.timeCreated));
}


function listAllMessageDispatch() {
  var c = factory.findAll("org.apache.activemq.command.MessageDispatch");

  for (var i = 0; i < c.size(); i++) {
    var md = c[i];

    logger.println("\n\n-------------------------------------------");
    logger.print("Consumer: " + md.consumerId);
    logger.print(" Destination: " + md.destination.physicalName);
    logger.println(" Redelivery counter: " + md.redeliveryCounter);
    logger.println("\nMessage{"+md.message.objectId.objectId+"}:");
    logger.println("messageId: " + md.message.messageId.key);
    logger.println("brokerInTime: " + formatTimeUnixMs(md.message.brokerInTime));
    logger.println("brokerOutTime: " + formatTimeUnixMs(md.message.brokerOutTime));
    logger.println("timestamp: " + formatTimeUnixMs(md.message.timestamp));
    logger.println("jmsXGroupFirstForConsumer: " + md.message.jmsXGroupFirstForConsumer);
    logger.println("groupID: " + md.message.groupID);
    
    
    
    
    
    
     
    
  }
}

