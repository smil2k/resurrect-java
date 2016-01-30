RegisterHandler("java.lang.String", function (obj) {
  return obj.value;
});

RegisterHandler("java.util.concurrent.atomic.AtomicLong", function (obj) {
  return obj.value;
});

RegisterHandler("java.util.concurrent.atomic.AtomicInteger", function (obj) {
  return obj.value;
});

RegisterHandler("java.util.concurrent.atomic.AtomicBoolean", function (obj) {
  return obj.value === 0 ? "false" : "true";
});
