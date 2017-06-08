
RegisterHandler("javax.management.ObjectName", function (obj) {
  return obj._canonicalName;
});
