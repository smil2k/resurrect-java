
RegisterHandler("org.jboss.modules.ModuleIdentifier", function (obj) {
  return obj.slot + "::" + obj.name;
});