package com.yan.luaeditor.tools;

import com.Day.Studio.Function.axmleditor.decode.AXMLDoc;
import com.Day.Studio.Function.axmleditor.decode.BTagNode;
import com.Day.Studio.Function.axmleditor.decode.BXMLNode;
import com.Day.Studio.Function.axmleditor.decode.StringBlock;
import com.Day.Studio.Function.axmleditor.editor.BaseEditor;
import com.Day.Studio.Function.axmleditor.editor.XEditor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class PermissionTagEditor extends BaseEditor<PermissionTagEditor.EditorInfo> {
    private int permission;

    public static class EditorInfo {
        private List<PermissionOperation> operations = new ArrayList<>();

        public final EditorInfo with(PermissionOperation operation) {
            this.operations.add(operation);
            return this;
        }
    }

    public static class PermissionOperation {
        private static final int ADD = 1;
        private static final int REMOVE = 2;
        private int operationType = 0;
        private String permissionName;
        private int permissionNameIndex;

        public PermissionOperation(String str) {
            this.permissionName = str;
        }

        public final PermissionOperation add() {
            this.operationType = (this.operationType & -3) | 1;
            return this;
        }

        public final PermissionOperation remove() {
            this.operationType = (this.operationType & -2) | 2;
            return this;
        }

        final boolean isAdd() {
            return (this.operationType & 1) == 1;
        }

        final boolean isRemove() {
            return (this.operationType & 2) == 2;
        }
    }

    @Override
    protected void editor() {
        List<BXMLNode> children = findNode().getChildren();
        for (PermissionOperation operation : ((EditorInfo) this.editorInfo).operations) {
            BTagNode bTagNode;
            if (operation.isRemove()) {
                Iterator<BXMLNode> it = children.iterator();
                while (it.hasNext()) {
                    bTagNode = (BTagNode) it.next();
                    if (bTagNode.getAttrStringForKey(this.attr_name) == operation.permissionNameIndex) {
                        //System.out.println(new PermissionOperation("删除  -->>> ").add(operation.permissionName).toString());
                        it.remove();
                        break;
                    }
                }
            } else if (operation.isAdd()) {
                BTagNode.Attribute attribute = new BTagNode.Attribute(this.namespace, this.attr_name, 3);
                attribute.setString(operation.permissionNameIndex);
                bTagNode = new BTagNode(-1, this.permission);
                bTagNode.setAttribute(attribute);
                children.add(bTagNode);
                //System.out.println(new PermissionOperation("添加 -->>  ").add(operation.permissionName).toString());
                this.doc.getStringBlock().setString(operation.permissionNameIndex, operation.permissionName);
            }
        }
    }

    @Override
    protected void registStringBlock(StringBlock stringBlock) {
        this.namespace = stringBlock.putString(XEditor.NAME_SPACE);
        this.permission = stringBlock.putString("permission");
        this.attr_name = stringBlock.putString("name");
        Iterator<PermissionOperation> it = ((EditorInfo) this.editorInfo).operations.iterator();
        while (it.hasNext()) {
            PermissionOperation operation = it.next();
            if (operation.isAdd()) {
                if (stringBlock.containsString(operation.permissionName)) {
                    it.remove();
                } else {
                    operation.permissionNameIndex = stringBlock.addString(operation.permissionName);
                }
            } else if (operation.isRemove()) {
                if (stringBlock.containsString(operation.permissionName)) {
                    operation.permissionNameIndex = stringBlock.getStringMapping(operation.permissionName);
                } else {
                    it.remove();
                }
            }
        }
    }

    public PermissionTagEditor(AXMLDoc aXMLDoc) {
        super(aXMLDoc);
    }

    @Override
    protected BXMLNode findNode() {
        return this.doc.getManifestNode();
    }

    @Override
    public String getEditorName() {
        return "permission";
    }
}