var JDialog = Java.type("javax.swing.JDialog");
var JTree = Java.type("javax.swing.JTree");
var Arrays = Java.type("java.util.Arrays");
var BorderLayout = Java.type("java.awt.BorderLayout");
var JScrollPane = Java.type("javax.swing.JScrollPane");
var TreeModel = Java.type("javax.swing.tree.TreeModel");
var WindowConstants = Java.type("javax.swing.WindowConstants");
var DefaultTreeCellRenderer = Java.type("javax.swing.tree.DefaultTreeCellRenderer");
var ToolTipManager = Java.type("javax.swing.ToolTipManager");

/*
var backtrackHandlers = {};

function RegisterBacktrackHandler(type, func) {
  try {
  var classId = factory.getClassByName(type).classId;
    handlers["" + classId.classId] = func;
  } catch (e) {
  }
}*/

function ShowBackTrack(obj) {
    var root;

    if ( typeof obj === 'number') {
        root = new ObjectId(obj);
    } else {
        root = obj;
    }

    var ObjectModel = Java.extend(TreeModel, {
        getRoot: function() {
            return {
                objectId: root,
                parent: null,
                path: []
            };
        },

        getChild:function( objId, idx ) {
            var path = objId.path.slice();
            path.push(objId.objectId);
            return {
                objectId: factory.getBackReferenceIds(objId.objectId)[idx],
                parent: objId.objectId,
                path: path
            };
        },

        getChildCount: function( objId ) {
            if ( objId.path.indexOf(objId.objectId) !== -1 ) {
                return 0;
            } else {
                return factory.getBackReferenceIds(objId.objectId).size();
            }
        },

        isLeaf: function( objId ) {
            return objId.path.indexOf(objId.objectId) !== -1;
        },

        valueForPathChanged: function() {},

        getIndexOfChild: function( parent, child) {
            return factory.getBackReferenceIds(parent.objectId).indexOf(child.objectId);
        },

        addTreeModelListener:function() {},
        removeTreeModelListener:function() {},
    });

    var ObjectRenderer = Java.extend(DefaultTreeCellRenderer);
    var renderer = new ObjectRenderer() {
        getTreeCellRendererComponent: function(jtree, value, sel, expanded, leaf,  row, hasFocus) {
            var ret = Java.super(renderer).getTreeCellRendererComponent(jtree, value, sel, expanded, leaf,  row, hasFocus);

            if ( value.objectId ) {
                ret.setText(ShowRelation(value.parent, value.objectId ));
                var s = "";
                for ( var i = 0 ; i < value.path.length ; i++ ) {
                    s += "->" + value.path[i].objectId;
                }

                ret.setToolTipText(s);
            }
            return ret;
        }
    };


    var model = new ObjectModel();
    var tree=  new JTree(model);
    var dialog = new JDialog();

    ToolTipManager.sharedInstance().registerComponent(tree);
    tree.setCellRenderer(renderer);

    dialog.setLayout(new BorderLayout());
    dialog.getContentPane().add(new JScrollPane(tree), BorderLayout.CENTER);
    dialog.setSize(800,600);
    dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    dialog.show();
}

function ShowRelation(holder, ref) {
    var obj = factory.getRawObject(ref);
    var clz = obj.getClassName();

    var refText = "";
    if ( holder ) {
        refText = " :: " + obj.findReferenceHolder(holder);
    }

    return clz + "@" + ref.objectId + refText;
}
