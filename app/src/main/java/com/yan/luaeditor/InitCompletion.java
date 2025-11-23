package com.yan.luaeditor;

import android.content.Context;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.yan.luaeditor.tools.ClassMethodScanner;
import com.yan.luaeditor.tools.PackageUtil;
import com.yan.luaide.LuaUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class InitCompletion {
    Context context;
    public InitCompletion(Context context){
        this.context=context;
    }
    @RequiresApi(api = Build.VERSION_CODES.P)
    public HashMap<String,Object> getClassTree(List<String> namelist){

        HashMap<String,Object> treemap=new HashMap<>();
        HashMap<String, HashMap<String, CompletionName>> scannerResult= new ClassMethodScanner().scanClassesAndMethods(namelist,null);
        treemap.putAll(scannerResult);
        //LuaUtil.save2("/sdcard/Luaide/basemap.log", scannerResult.toString());
        for (String className: scannerResult.keySet()){
            if (className.contains("$")){
                //System.out.println(className);
                try {
                    String[] strings = className.split("\\$");
                    String cachename=strings[0];
                    for (int i = 1; i < strings.length; i++) {
                        cachename+="$"+strings[i];
                        HashMap<String,Object> cachemap2=getMap(treemap,cachename.split("\\$"));
                        if (cachemap2.isEmpty()){
                            if (treemap.get(cachename)!=null)
                                getMap(treemap, Arrays.copyOf(strings, i + 1)).putAll((HashMap) treemap.get(cachename));

                        }
                        Map<String, Object> targetMap = (Map<String, Object>) getMap(treemap, cachename.split("\\$")).get(strings[i]);
                        if (targetMap != null) {
                            Iterator<Map.Entry<String, Object>> iterator = targetMap.entrySet().iterator();
                            while (iterator.hasNext()) {
                                Map.Entry<String, Object> entry = iterator.next();
                                if (entry.getValue() == null) {
                                    iterator.remove();
                                }
                            }
                        }
                        //treemap.remove(className);
                    }

                    //treemap.remove(className);
                    //System.out.println(strings[1]);
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            }else {
                //System.out.println(className);
            }
        }
        treemap.keySet().removeIf(className -> className.contains("$"));
        return treemap;
    }

    public HashMap<String,List<String>> getCM(){
        HashMap<String, List<String>> basemap = PackageUtil.load(context);
        HashMap<String, List<String>> cachemap = new HashMap<>();

        for (String key : basemap.keySet()) {
            if (!key.contains("$")) {
                cachemap.computeIfAbsent(key, k -> new ArrayList<>()).addAll(basemap.get(key));
            }
        }

        for (String key : basemap.keySet()) {
            if (key.contains("$")) {
                String suffix = key.substring(key.lastIndexOf('$') + 1);
                cachemap.computeIfAbsent(suffix, k -> new ArrayList<>()).addAll(basemap.get(key));
            }
        }

        return cachemap;
    }
    public List<String> getClassNameList(HashMap<String,List<String>> cachemap){
        List<String> namelist=new ArrayList<>();
        for (String n:cachemap.keySet()){
            namelist.addAll(cachemap.get(n));
        }
        return namelist;
    }
    public HashMap getMap(HashMap mmap,String[] key){
        HashMap<String,Object> map=mmap;
        for (int i=0;i<key.length;i++){
            if (map.containsKey(key[i])) {
                map = (HashMap<String, Object>) map.get(key[i]);
            }else {
                map.put(key[i],new HashMap<String,Object>());
                map=(HashMap<String, Object>) map.get(key[i]);
            }
        }
        return map;
    }
}
