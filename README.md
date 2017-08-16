# resurrect-java

This project is able to resurrect Java objects from a standard HPROF format dump. The heart of it is a Javascript REPL, which can find and inspect objects and their relations.

Additionally you can write handlers, which can remove semantically irrelevant or hard to traverse structures into standard types. Eg. AtomicLong is rewritten to a simple long or TreeMap into a Javascript map.

## REPL

REPL is GNU Readline compatible with history.

Commands:
- *load \<dump\>*: Loads a dump. (This clears the javascript memory implicitly, therefore you need to load libraries again.)
- *loadlibrary \<javascript file\>*: Loads a library javascript file.
- *grepobj [string]*: Gives back the list of classes which has at least one living instance. The optional argument is a case insensitive string contains match
- *grepclass [string]*: Gives back the list of known classes. The list can be restricted optionally.
- *.* or *source*: Executes a REPL script. (Can load libraries and dump also)
- *rebuildcache*: Rebuilds the cache structures on disk. (in case it is corrupt)
- *reset*: Creates a new emply REPL.
- *exit*: Go figure

Anything else is evaulated as javascipt. eg. function invocation must be ended with ().  The result of the functions will be printed.

### Accessing static properties

You can access statics using the class of the object.
The attributes are stored under the "statics" attribute of the class.

eg.
```
   logger.println(var.type.statics.variable);
```

Staitics behaves like an object:

```
   describeObject(var.type.statics.variable);
```

### Accessing the incoming references

There are two methods to access the back-references:

* obj.*getBackReferences()* : Returns all objects referencing the given object.
* obj.*getBackReferenceIds()* : Returns all object *ID* to the given object. 
* obj.*findReferenceHolder(ObjectId)* : Returns the name of the holder field  

## Library

Default library contains some handy functions for interaction with the given dump:

### factory

The factory reference represents the loaded dump. You can use the following methods:
- *factory.getSnapshotTime() : Date*: Time when the dump was taken
- *factory.getObject(long) : Object*: Returns an object reference by objectid
- *factory.getRawObject(long) : Object*: Returns an object reference by objectid without post processing.
- *factory.findAll(string) : List\<Object\>*: Finds all instances of a class

### RegisterHandler()

Register handler can help resolving complex objects. 

eg.
```
RegisterHandler("org.apache.activemq.command.BrokerId", function (obj) {
  return obj.value;
});
```

### formatTimeUnixMs( time ) : string

Formats time as time difference from the snapshot time.

### printObject( object, properties )

Prints all attributes of an object

### describeObjectById( long )

Prints all attributes of an object by object id.

### describeObject( object )

Prints all attributes of an object by object reference

### ShowBackTrack( long or objectId)

Shows a swing dialog with a tree the incoming references.

### ShowRelation( holder:objectId, ref:objectId )

Displays the relation between the holder and the referenced object.