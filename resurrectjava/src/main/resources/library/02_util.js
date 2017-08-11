var ArrayList = Java.type("java.util.ArrayList");
var HashMap = Java.type("java.util.HashMap");
var Date = Java.type("java.util.Date");
var String = Java.type("java.lang.String");
var SimpleDateFormat = Java.type("java.text.SimpleDateFormat");
var Arrays = Java.type("java.util.Arrays");

var ISOFormat=new SimpleDateFormat("MMMdd HH:mm:SS ");

function formatTimeUnixMs(time) {
  if ( typeof time === 'object' ) {
    time = time.time;
  }

  var offset = factory.snapshotTime.time - time;
  var sig = offset >= 0 ? '+' : '-';

  if ( sig === '-') {
    offset = -offset;
  }

  var sec = Math.floor(offset / 1000);
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



  return ISOFormat.format(new Date(time)) + String.format("(%s%02.0f:%02.0f:%02.0f)", sig, hrs, min,sec);
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