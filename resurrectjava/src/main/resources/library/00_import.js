var logger = java.lang.System.out;

var necromancer = Packages.necromancer;
var ObjectId = necromancer.data.ObjectId;
var ClassId = necromancer.data.ClassId;

/**
 * Finds flass by name
 */
function getClassByName(name) {
  return factory.getClassByName(name);
}

/**
 * Finds object directly by Id
 */
function getObject(name) {
  return factory.getObject(new ObjectId(name));
}

/**
 * Imports more javascript files. Relative from the working dir.
 */
function importFile(name) {
  return factory.importFile(name);
}
