/**
 * Registers a handler for a given class. In case you retrieve this object using methods here, 
 * objects will be converted by the func you provided here.
 */

var handlers = {};

function RegisterHandler(type, func) {
  try {
  var classId = factory.getClassByName(type).classId;
    handlers["" + classId.classId] = func;
  } catch (e) {
  }
}

factory.setObjectResolver(function (obj) {
  if (obj.classId && handlers["" + obj.classId.classId]) {
    return handlers["" + obj.classId.classId](obj);
  } else {
    return obj;
  }
});

