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

public class ProviderEditor extends BaseEditor<ProviderEditor.EditorInfo> {
    private int provider;

    public ProviderEditor(AXMLDoc aXMLDoc) {
        super(aXMLDoc);
        setEditor("name", "authorities");
    }

    @Override
    public String getEditorName() {
        return "provider";
    }

    @Override
    protected void editor() {
        List<BXMLNode> children = this.doc.getApplicationNode().getChildren();
        for (ProviderOpera providerOpera : ((EditorInfo) this.editorInfo).editors) {
            if (providerOpera.isUpdate()) {
                Iterator<BXMLNode> it = children.iterator();
                while (it.hasNext()) {
                    BTagNode bTagNode = (BTagNode) it.next();
                    if (this.provider == bTagNode.getName() && bTagNode.getAttrStringForKey(this.attr_name) == providerOpera.providerName_Index) {
                        bTagNode.setAttrStringForKey(this.attr_value, providerOpera.providerAuthorities_Index);
                        //System.out.println("更新 -->>  " + providerOpera.providerName);
                        this.doc.getStringBlock().setString(providerOpera.providerAuthorities_Index, providerOpera.providerAuthorities);
                        break;
                    }
                }
            }
        }
    }

    @Override
    protected BXMLNode findNode() {
        return this.doc.getApplicationNode();
    }

    @Override
    protected void registStringBlock(StringBlock stringBlock) {
        this.namespace = stringBlock.putString(XEditor.NAME_SPACE);
        this.provider = stringBlock.putString("provider");
        this.attr_name = stringBlock.putString("name");
        this.attr_value = stringBlock.putString("authorities");
        Iterator it = ((EditorInfo) this.editorInfo).editors.iterator();
        while (it.hasNext()) {
            ProviderOpera providerOpera = (ProviderOpera) it.next();
            if (providerOpera.isUpdate()) {
                if (!stringBlock.containsString(providerOpera.providerName)) {
                    it.remove();
                } else {
                    providerOpera.providerName_Index = stringBlock.getStringMapping(providerOpera.providerName);
                    providerOpera.providerAuthorities_Index = stringBlock.addString(providerOpera.providerAuthorities);
                }
            }
        }
    }

    public static class EditorInfo {
        private List<ProviderOpera> editors = new ArrayList<>();

        public final EditorInfo with(ProviderOpera providerOpera) {
            this.editors.add(providerOpera);
            return this;
        }
    }

    public static class ProviderOpera {
        private static final int UPDATE = 3;
        private int opera = 0;
        private String providerName;
        private int providerName_Index;
        private String providerAuthorities;
        private int providerAuthorities_Index;

        public ProviderOpera(String providerName, String providerAuthorities) {
            this.providerName = providerName;
            this.providerAuthorities = providerAuthorities;
        }

        public final ProviderOpera update() {
            this.opera &= ~UPDATE;
            this.opera |= UPDATE;
            return this;
        }

        final boolean isUpdate() {
            return (this.opera & UPDATE) == UPDATE;
        }
    }
}