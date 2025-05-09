package com.yan.luaeditor.tools;

import com.Day.Studio.Function.axmleditor.decode.AXMLDoc;
import com.Day.Studio.Function.axmleditor.decode.BTagNode;
import com.Day.Studio.Function.axmleditor.decode.BTagNode.Attribute;
import com.Day.Studio.Function.axmleditor.decode.BXMLNode;
import com.Day.Studio.Function.axmleditor.decode.StringBlock;
import com.Day.Studio.Function.axmleditor.editor.BaseEditor;
import com.Day.Studio.Function.axmleditor.editor.XEditor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class PermissionEditor extends BaseEditor<PermissionEditor.EditorInfo> {
    private int user_permission;

    public static class EditorInfo {
        private List<PermissionOpera> editors = new ArrayList();

        public final EditorInfo with(PermissionOpera permissionOpera) {
            this.editors.add(permissionOpera);
            return this;
        }
    }

    public static class PermissionOpera {
        private static final int ADD = 1;
        private static final int REMOVE = 2;
        private int opera = 0;
        private String permission;
        private int permissionValue_Index;

        public PermissionOpera(String str) {
            this.permission = str;
        }

        public final PermissionOpera add() {
            this.opera = (this.opera & -3) | 1;
            return this;
        }

        public final PermissionOpera remove() {
            this.opera = (this.opera & -2) | 2;
            return this;
        }

        final boolean isAdd() {
            return (this.opera & 1) == 1;
        }

        final boolean isRemove() {
            return (this.opera & 2) == 2;
        }
    }

    @Override
    protected void editor() {
        List children = findNode().getChildren();
        for (PermissionOpera permissionOpera : ((EditorInfo) this.editorInfo).editors) {
            BTagNode bTagNode;
            if (permissionOpera.isRemove()) {
                Iterator it = children.iterator();
                while (it.hasNext()) {
                    bTagNode = (BTagNode) ((BXMLNode) it.next());
                    if ( bTagNode.getAttrStringForKey(this.attr_name) == permissionOpera.permissionValue_Index) {
                        //System.out.println(new PermissionOpera("删除  -->>> ").append(permissionOpera.permission).toString());
                        it.remove();
                        break;
                    }
                }
            } else if (permissionOpera.isAdd()) {
                Attribute attribute = new Attribute(this.namespace, this.attr_name, 3);
                attribute.setString(permissionOpera.permissionValue_Index);
                bTagNode = new BTagNode(-1, this.user_permission);
                bTagNode.setAttribute(attribute);
                children.add(bTagNode);
                //System.out.println(new PermissionOpera("添加 -->>  ").append(permissionOpera.permission).toString());
                this.doc.getStringBlock().setString(permissionOpera.permissionValue_Index, permissionOpera.permission);
            }
        }
    }

    @Override
    protected void registStringBlock(StringBlock stringBlock) {
        this.namespace = stringBlock.putString(XEditor.NAME_SPACE);
        this.user_permission = stringBlock.putString(XEditor.NODE_USER_PREMISSION);
        this.attr_name = stringBlock.putString("name");
        Iterator it = ((EditorInfo) this.editorInfo).editors.iterator();
        while (it.hasNext()) {
            PermissionOpera permissionOpera = (PermissionOpera) it.next();
            if (permissionOpera.isAdd()) {
                if (stringBlock.containsString(permissionOpera.permission)) {
                    it.remove();
                } else {
                    permissionOpera.permissionValue_Index = stringBlock.addString(permissionOpera.permission);
                }
            } else if (permissionOpera.isRemove()) {
                if (stringBlock.containsString(permissionOpera.permission)) {
                    permissionOpera.permissionValue_Index = stringBlock.getStringMapping(permissionOpera.permission);
                } else {
                    it.remove();
                }
            }
        }
    }

    public PermissionEditor(AXMLDoc aXMLDoc) {
        super(aXMLDoc);
    }

    @Override
    protected BXMLNode findNode() {
        return this.doc.getManifestNode();
    }

    @Override
    public String getEditorName() {
        return XEditor.NODE_USER_PREMISSION;
    }
}