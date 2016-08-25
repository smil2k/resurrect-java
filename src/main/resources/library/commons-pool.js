RegisterHandler("org.apache.commons.pool.impl.CursorableLinkedList", function (obj) {
  var res = new java.util.ArrayList();

  var x = obj._head;

  while ( x._next ) {
    res.add(x._val);
    x = x._next;
  }

  return res;
});
