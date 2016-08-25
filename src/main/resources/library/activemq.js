var TreeSet = Java.type("java.util.TreeSet");
var Date = Java.type("java.util.Date");

RegisterHandler("org.apache.activemq.command.BrokerId", function (obj) {
  return obj.value;
});

RegisterHandler("org.apache.activemq.command.ConnectionId", function (obj) {
  return obj.value;
});

RegisterHandler("org.apache.activemq.command.ConsumerId", function (obj) {
  return obj.connectionId + ":" + obj.sessionId + ":" + obj.value;
});

RegisterHandler("org.apache.activemq.command.ProducerId", function (obj) {
  return obj.connectionId + ":" + obj.sessionId + ":" + obj.value;
});


RegisterHandler("org.apache.activemq.util.LongSequenceGenerator", function (obj) {
  return "LongGen{lastId=" + obj.lastSequenceId + "}";
});


/**
 * list Active MQ brokers
 */
function lsBrokers() {
  logger.println(factory.findAll("org.apache.activemq.broker.BrokerRegistry")[0].brokers.keySet())
}

/**
 * Broker info
 */
function lsBroker(broker) {
  var broker = factory.findAll("org.apache.activemq.broker.BrokerRegistry")[0].brokers[broker];
  var s = new TreeSet();
  s.add("persistent");
  s.add("currentConnections");
  s.add("stopping");
  s.add("stopped");
  s.add("brokerName");
  s.add("startDate");
  s.add("defaultSocketURIString");
  s.add("started");
  s.add("totalConnections");
  s.add("restartRequested");

  printObject(broker, s);
}

function listAllQueueSubscribers(broker, queue) {
  if (!queue) {
    queue = "";
  }
  var msg = 0;
  _printSubHeader();
  _visitSubscribers(broker,
          function (sub) {
            if (sub.subscription.destinationFilter.destination.physicalName.contains(queue)) {
              _printSub(sub);
              msg += (sub.subscription.enqueueCounter - sub.subscription.consumedCount);
            }
          });

  logger.println("Message count=" + msg);
}

function listSlowQueueSubscribers(broker, queue) {
  logger.println("Snapshot taken: " + factory.snapshotTime);

  if (!queue) {
    queue = "";
  }
  var msg = 0;

  _printSubHeader();
  _visitSubscribers(broker,
          function (sub) {
            if (sub.subscription.destinationFilter.destination.physicalName.contains(queue)
                    && sub.subscription.slowConsumer) {
              _printSub(sub);
              msg += (sub.subscription.enqueueCounter - sub.subscription.consumedCount);
              logger.println(sub.objectId + " Last ack: " + formatTimeUnixMs(sub.subscription.lastAckTime));
              var d = sub.subscription.dispatched;
              if (d.length > 0) {
                logger.println("\n--- Dispatched queue:");
                for (var i = 0; i < d.length; i++) {
                  if (d[i]) {
                    _printMessageRef(d[i]);
                  }
                }
                logger.println("\n");
              }
            }
          });
  logger.println("Message count=" + msg);
}

function listProblemQueueSubscribers(broker, queue) {
  if (!queue) {
    queue = "";
  }
  var msg = 0;

  _printSubHeader();
  _visitSubscribers(broker,
          function (sub) {
            if (sub.subscription.destinationFilter.destination.physicalName.contains(queue)
                    && (sub.subscription.slowConsumer
                            || sub.subscription.enqueueCounter != sub.subscription.consumedCount)) {
              _printSub(sub);
              msg += (sub.subscription.enqueueCounter - sub.subscription.consumedCount);
            }
          });
  logger.println("Message count=" + msg);
}

function listMessagesByConnection(con) {
  var a = factory.findAll("org.apache.activemq.broker.region.IndirectMessageReference");
  for (var i = 0; i < a.length; i++) {
    var ref = a[i];
    if (ref.messageId.producerId.connectionId == con) {
      _printMessageRef(ref);
    }
  }
}

function _visitSubscribers(broker, func) {
  var broker = factory.findAll("org.apache.activemq.broker.BrokerRegistry")[0].brokers[broker].regionBroker;

  for (var i = broker.queueSubscribers.values().iterator(); i.hasNext(); ) {
    var sub = i.next();

    func(sub);
  }
}

function _printMessageRef(msg) {
  logger.print("Ref: {" + msg.objectId.objectId + "}");
  logger.println(" dropped:" + msg.dropped + " acked:" + msg.acked +
          " cnx:{" + msg.messageId.producerId.connectionId + "}#" + msg.messageId.producerId.sessionId);
  logger.println("time:" + formatTimeUnixMs(msg.message.timestamp) + " Msg: {" + msg.message.objectId.objectId + "}"
          + " group: " + msg.message.groupID
          + " bytes: " + msg.message.size
          + " retry: " + msg.message.redeliveryCounter
          + "\n  in:" + formatTimeUnixMs(msg.message.brokerInTime) + " id:" + msg.message.messageId.key
          + "\n out:" + formatTimeUnixMs(msg.message.brokerOutTime) + " cls:" + msg.message.className
          + "\n"
          );
}

function _printSubHeader() {
  logger.printf("\n\n%45s | %65s | %10s | %5s | %7s | %7s |\n",
          "Queue", "Client ID", "User Name", "Slow?", "Enc", "Cons");
}

function _printSub(sub) {
  logger.printf("%45s | %65s | %10s | %5s | %7s | %7s |\n",
          sub.subscription.destinationFilter.destination.physicalName,
          sub.clientId,
          sub.userName,
          sub.subscription.slowConsumer,
          sub.subscription.enqueueCounter,
          sub.subscription.consumedCount);
}

RegisterHandler("org.apache.activemq.util.ByteSequence", function (obj) {
  return obj.data;
});
