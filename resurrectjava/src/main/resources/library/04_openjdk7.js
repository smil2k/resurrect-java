var HashMap = Java.type("java.util.HashMap");
var Date = Java.type("java.util.Date");
var Arrays = Java.type("java.util.Arrays");

RegisterHandler("sun.security.x509.CertificateSubjectName", function (obj) {
  return obj.dnName.rfc2253Dn;
});

RegisterHandler("java.util.concurrent.ConcurrentHashMap", function (obj) {
  var result = new HashMap();

  for (var key in obj.segments) {
    var seg = obj.segments[key];
    if (seg) {
      for (var t in seg.table) {
        var table = seg.table[t];

        while (table) {
          result.put(table.key, table.value);
          table = table.next;
        }
      }
    }
  }
  return result;
});

RegisterHandler("java.util.Collections$SynchronizedRandomAccessList", function (obj) {
  return obj.c;
});

RegisterHandler("java.util.HashMap", function (obj) {
  var result = new HashMap();

  for (var key in obj.table) {
    var table = obj.table[key];

    while (table) {
      result.put(table.key, table.value);
      table = table.next;
    }
  }
  return result;
});

RegisterHandler("java.util.LinkedList", function (obj) {
  var res = new java.util.ArrayList();

  var x = obj.header.next;
  var s = obj.size;
  while ( s != 0 ) {
    res.add(x.element);
    x = x.next;
    s--;
  }

  return res;
});

RegisterHandler("java.util.concurrent.ConcurrentLinkedQueue", function (obj) {
  var res = new java.util.ArrayList();

  var x = obj.head;  
  while ( x ) {
    res.add(x.item);
    x = x.next;
  }

  return res;
});
