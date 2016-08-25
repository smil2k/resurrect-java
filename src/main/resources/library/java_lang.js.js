RegisterHandler("java.lang.String", function (obj) {
  return obj.value;
});

RegisterHandler("java.lang.Integer", function (obj) {
  return obj.value;
});

RegisterHandler("java.lang.Long", function (obj) {
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

RegisterHandler("java.util.concurrent.atomic.AtomicReference", function (obj) {
  return obj.value;
});

RegisterHandler("java.util.concurrent.CopyOnWriteArrayList", function (obj) {
  return obj.array;
});

RegisterHandler("java.util.concurrent.CopyOnWriteArraySet", function (obj) {
  return obj.al;
});

RegisterHandler("java.net.URI", function (obj) {
  return obj.string;
});

RegisterHandler("java.util.ArrayList", function (obj) {
  return obj.elementData;
});


RegisterHandler("java.util.Date", function (obj) {
  return new Date(obj.fastTime);
});