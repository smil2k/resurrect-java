/**
 * Registers a handler for a given class. In case you retrieve this object using methods here, 
 * objects will be converted by the func you provided here.
 */

var handlers = {};

function RegisterHandler(type, func) {
  var classId = factory.getClassByName(type).classId;
  handlers["" + classId.classId] = func;
}

factory.setObjectResolver(function (obj) {
  if (obj.classId && handlers["" + obj.classId.classId]) {
    return handlers["" + obj.classId.classId](obj);
  } else {
    return obj;
  }
});

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
  logger.println(" {");
  for (var key in obj) {
    //  if (obj.containsKey(key)) {
    logger.print("     " + key + ": ");
    var v = obj[key];
    if (v instanceof java.lang.String) {
      logger.println("\"" + v + "\"");
    } else {
      logger.println(v);
    }
  }
  logger.println("}");
}
