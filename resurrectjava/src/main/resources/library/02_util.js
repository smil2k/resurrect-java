function formatTimeUnixMs(time) {
  var sec = Math.floor((factory.snapshotTime.time - time) / 1000);
  var min = 0;
  var hrs = 0;

  if (sec >= 60) {
    min = Math.floor(sec / 60);
    sec = sec % 60;
  }

  if (min >= 60) {
    hrs = Math.floor(min / 60);
    min = min % 60;
  }

  return new Date(time) + " (" + hrs + " hrs " + min + " mins " + sec + " sec)";
}

/**
 * properties: collection of whitelist property names
 */
function printObject(object, properties) {
  if ( !properties ) {
    properties = null;
  }
  
  for (var key in object) {
    if ( properties === null || properties.contains(key)) {
      logger.print("     " + key + ": ");
      var v = object[key];
      if (v instanceof java.lang.String) {
        logger.println("\"" + v + "\"");
      } else {
        logger.println(v);
      }
    }
  }
}

/**
 * Describe Object instance by long id
 */
function describeObjectById(obj) {
  describeObject(factory.getObject(new ObjectId(obj)));
}

/**
 * Describe Object instance
 */
function describeObject(obj) {
  logger.print("class ");
  if (obj.className) {
    logger.print(obj.className);
  } else {
    logger.print(obj.class.name);
  }

  if (obj.objectId) {
    logger.print("{" + obj.objectId.objectId + "}");
  }

  logger.println(" {");
  printObject(obj);
  logger.println("}");
}