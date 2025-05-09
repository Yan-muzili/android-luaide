package com.yan.luaeditor;

import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.yan.luaeditor.tools.ClassMethodScanner;
import com.yan.luaeditor.tools.parser.LuaLexer;
import com.yan.luaeditor.tools.parser.LuaParser;
import com.yan.luaeditor.tools.parser.Token;
import com.yan.luaide.LuaUtil;

import io.github.rosemoe.sora.lang.completion.CompletionItem;
import io.github.rosemoe.sora.lang.completion.CompletionItemKind;
import io.github.rosemoe.sora.lang.completion.CompletionPublisher;
import io.github.rosemoe.sora.lang.completion.Comparators;
import io.github.rosemoe.sora.lang.completion.FuzzyScore;
import io.github.rosemoe.sora.lang.completion.SimpleCompletionItem;
import io.github.rosemoe.sora.lang.completion.Filters;
import io.github.rosemoe.sora.lang.completion.FuzzyScoreOptions;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import java.lang.String;
import java.lang.Object;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import io.github.rosemoe.sora.lang.Language;
import io.github.rosemoe.sora.text.CharPosition;
import io.github.rosemoe.sora.text.ContentReference;
import io.github.rosemoe.sora.text.TextUtils;
import io.github.rosemoe.sora.util.MutableInt;

/**
 * Identifier auto-completion.
 *
 * <p>You can use it to provide identifiers, but you can't update the given {@link
 * CompletionPublisher} if it is used. If you have to mix the result, then you should call {@link
 * CompletionPublisher#setComparator(Comparator)} with null first. Otherwise, your completion list
 * may be corrupted. And in that case, you must do the sorting work by yourself and then add your
 * items.
 *
 * @author Rosemoe
 */
public class MyIdentifierAutoComplete {

    /**
     * @deprecated Use {@link Comparators}
     */
    @Deprecated
    private static final Comparator<CompletionItem> COMPARATOR =
            (p1, p2) -> {
                var cmp1 = asString(p1.desc).compareTo(asString(p2.desc));
                if (cmp1 < 0) {
                    return 1;
                } else if (cmp1 > 0) {
                    return -1;
                }
                return asString(p1.label).compareTo(asString(p2.label));
            };

    private String[] keywords;
    private boolean keywordsAreLowCase;
    private Map<String, Object> keywordMap;
    HashMap<String, HashMap<String, CompletionName>> basemap;
    HashMap<String, List<String>> classmap;
    HashMap<String, String> mmap;
    HashMap<String, HashMap<String, CompletionName>> importlist;
    public MyIdentifierAutoComplete() {
    }

    public MyIdentifierAutoComplete(String[] keywords, HashMap<String, HashMap<String, CompletionName>> basemap) {
        this();
        setKeywords(keywords, true);
        this.basemap = basemap;
    }

    private static String asString(CharSequence str) {
        return (str instanceof String ? (String) str : str.toString());
    }

    public void setBasemap(HashMap<String, HashMap<String, CompletionName>> map) {
        this.basemap = map;
    }
    public void setImportlist(HashMap<String, HashMap<String, CompletionName>> im){
        this.importlist=im;
    }

    public void setClassmap(HashMap<String, List<String>> classmap) {
        this.classmap = classmap;
    }

    public void setMmap(HashMap<String, String> map) {
        this.mmap = map;
    }

    public void setKeywords(String[] keywords, boolean lowCase) {
        this.keywords = keywords;
        keywordsAreLowCase = lowCase;
        var map = new HashMap<String, Object>();
        if (keywords != null) {
            for (var keyword : keywords) {
                map.put(keyword, true);
            }
        }
        keywordMap = map;
    }

    public String[] getKeywords() {
        return keywords;
    }

    public void write(String path, String cs) {
        try {
            FileOutputStream fos = new FileOutputStream(path, true);
            fos.write(cs.getBytes());
            fos.flush();
            fos.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Make completion items for the given arguments. Provide the required arguments passed by {@link
     * Language#requireAutoComplete(ContentReference, CharPosition, CompletionPublisher, Bundle)}
     *
     * @param prefix The prefix to make completions for.
     */
    public void requireAutoComplete(
            @NonNull ContentReference reference,
            @NonNull CharPosition position,
            @NonNull String prefix,
            @NonNull CompletionPublisher publisher,
            @Nullable Identifiers userIdentifiers) {

        var completionItemList = createCompletionItemList(prefix, userIdentifiers);

        var comparator =
                Comparators.getCompletionItemComparator(reference, position, completionItemList);
        publisher.addItems(completionItemList);

        publisher.setComparator(comparator);
    }

    private String extractLastIdentifier(String prefix) {
        int lastDotIndex = prefix.lastIndexOf('.');
        if (lastDotIndex != -1) {
            return prefix.substring(0, lastDotIndex);
        }
        return prefix;
    }

    private boolean meetsFuzzyScore(String prefix, String prefixLower, String kw) {
        FuzzyScore fuzzyScore = Filters.fuzzyScoreGracefulAggressive(
                prefix,
                prefixLower,
                0,
                kw,
                kw.toLowerCase(Locale.ROOT),
                0,
                FuzzyScoreOptions.getDefault());
        int score = fuzzyScore == null ? -100 : fuzzyScore.getScore();
        return score >= -20;
    }

    public List<CompletionItem> createCompletionItemList(
            @NonNull String prefix, @Nullable Identifiers userIdentifiers) {
        int prefixLength = prefix.length();
        //System.out.println(prefix);
        if (prefixLength == 0) {
            return Collections.emptyList();
        }
        var result = new ArrayList<CompletionItem>();
        final var keywordMap = this.keywordMap;
        final String match = prefix;
        final String matchLower = prefix.toLowerCase(Locale.ROOT);

        if (keywords != null) {
            Set<String> filteredKeywords = new HashSet<>();
            for (String kw : keywords) {
                if (keywordsAreLowCase) {
                    if (kw.startsWith(match) || meetsFuzzyScore(prefix, matchLower, kw)) {
                        filteredKeywords.add(kw);
                    }
                } else {
                    String kwLower = kw.toLowerCase(Locale.ROOT);
                    if (kwLower.startsWith(matchLower) || meetsFuzzyScore(prefix, matchLower, kw)) {
                        filteredKeywords.add(kw);
                    }
                }
            }
            for (String kw : filteredKeywords) {
                result.add(new SimpleCompletionItem(kw, "Keyword", prefixLength, kw).kind(CompletionItemKind.Keyword));
            }
        }

        if (userIdentifiers != null) {
            List<String> dest = new ArrayList<>();
            userIdentifiers.filterIdentifiers(prefix, dest);
            for (String word : dest) {
                if (keywordMap == null || !keywordMap.containsKey(word)) {
                    result.add(new SimpleCompletionItem(word, "Identifier", prefixLength, word).kind(CompletionItemKind.Identifier));
                }
            }
        }
        //System.out.println(importlist);
        boolean foundInImportList = false;


        for (String imp : importlist.keySet()) {
            String[] name = imp.split("\\.");
            if (name[name.length - 1].startsWith(prefix)) {
                result.add(
                        new SimpleCompletionItem(name[name.length - 1], ":import", prefixLength, name[name.length - 1])
                                .kind(CompletionItemKind.Class));
                foundInImportList = true;
            }
        }
        
        if (!foundInImportList) {
            for (String str : classmap.keySet()) {
                if (str.startsWith(prefix) && !str.matches(".*\\.\\d+$")) {
                    for (String cl : classmap.get(str)) {
                        result.add(
                                new SimpleCompletionItem(str, cl, prefixLength, str)
                                        .kind(CompletionItemKind.Class));

                    }
                }
            }
        }

        //LuaUtil.save2("/sdcard/Luaide/yyy.log", basemap.toString());
        mmap.put("activity", "LuaActivity");
        //LuaUtil.save2("/sdcard/Luaide/yyy.log", basemap.toString());
        //mmap.put("Toast.makeText","");
        //long startTime = System.currentTimeMillis();
        try {
            LuaLexer lexer = new LuaLexer(prefix);
            List<Token> tokens = lexer.tokenize();
            LuaParser parser = new LuaParser(tokens);
            //System.out.println(tokens);
            String filtered = parser.filterParentheses(prefix);
            String prefixtype = ClassMethodScanner.getReturnType(classmap, basemap, filtered, mmap);

            if (!prefixtype.equals("nullclass") && !prefixtype.equals("void") && filtered.endsWith(".")) {
                try {
                    for (String key : basemap.get(prefixtype).keySet()) {
                        result.add(
                                new SimpleCompletionItem(key, basemap.get(prefixtype).get(key).getDescription(), prefixLength, prefix + key)
                                        .kind(basemap.get(prefixtype).get(key).getType()));
                    }
                } catch (Exception e) {
                    LuaUtil.save2("/sdcard/Luaide/yyy.log", e.getMessage());
                }
            } else if (prefixtype.equals("nullclass")) {
                if (filtered == null || prefix == null || filtered.isEmpty() || prefix.isEmpty()) {
                    return null;
                }
                String[] allstr = filtered.split("\\.");
                String[] mystr = prefix.split("\\.");
                StringBuilder sc = new StringBuilder();
                StringBuilder complete = new StringBuilder();
                for (int i = 0; i < allstr.length - 1; i++) {
                    sc.append(allstr[i]).append(".");
                    complete.append(mystr[i]).append(".");
                }
                //System.out.println(sc);
                try {
                    String classname = ClassMethodScanner.getReturnType(classmap, basemap, sc.toString(), mmap);
                    if (!classname.equals("nullclass") && !classname.equals("void")) {
                        for (String key : basemap.get(classname).keySet()) {
                            if ((sc + key).startsWith(filtered)) {
                                result.add(
                                        new SimpleCompletionItem(key, basemap.get(classname).get(key).getDescription(), prefixLength, complete + key)
                                                .kind(basemap.get(classname).get(key).getType()));
                            }
                        }
                    }
                } catch (Exception e) {
                    LuaUtil.save2("/sdcard/Luaide/yyy.log", e.getMessage());
                }
            }
        } catch (Exception e) {
            LuaUtil.save2("/sdcard/Luaide/yyy.log", e.getMessage());
        }
        //long endTime = System.currentTimeMillis();
        //long duration = endTime - startTime;
        //System.out.println("代码运行时长: " + duration + " 毫秒");
        return result;
    }


    /**
     * Make completion items for the given arguments. Provide the required arguments passed by {@link
     * Language#requireAutoComplete(ContentReference, CharPosition, CompletionPublisher, Bundle)}
     *
     * @param prefix The prefix to make completions for.
     */
    @Deprecated
    public void requireAutoComplete(
            @NonNull String prefix,
            @NonNull CompletionPublisher publisher,
            @Nullable Identifiers userIdentifiers) {
        publisher.setComparator(COMPARATOR);
        publisher.setUpdateThreshold(0);
        publisher.addItems(createCompletionItemList(prefix, userIdentifiers));
    }

    /**
     * Interface for saving identifiers
     *
     * @author Rosemoe
     * @see IdentifierAutoComplete.DisposableIdentifiers
     */
    public interface Identifiers {

        /**
         * Filter identifiers with the given prefix
         *
         * @param prefix The prefix to filter
         * @param dest   Result list
         */
        void filterIdentifiers(@NonNull String prefix, @NonNull List<String> dest);
    }

    /**
     * This object is used only once. In other words, the object is generated every time the text
     * changes, and is abandoned when next time the text change.
     *
     * <p>In this case, the frequent allocation of memory is unavoidable. And also, this class is not
     * thread-safe.
     *
     * @author Rosemoe
     */
    public static class DisposableIdentifiers implements Identifiers {

        private static final Object SIGN = new Object();
        private final List<String> identifiers = new ArrayList<>(128);
        private HashMap<String, Object> cache;

        public void addIdentifier(String identifier) {
            if (cache == null) {
                throw new IllegalStateException("begin() has not been called");
            }
            if (cache.put(identifier, SIGN) == SIGN) {
                return;
            }
            identifiers.add(identifier);
        }

        /**
         * Start building the identifiers
         */
        public void beginBuilding() {
            cache = new HashMap<>();
        }

        /**
         * Free memory and finish building
         */
        public void finishBuilding() {
            cache.clear();
            cache = null;
        }

        @Override
        public void filterIdentifiers(@NonNull String prefix, @NonNull List<String> dest) {
            for (String identifier : identifiers) {
                var fuzzyScore =
                        Filters.fuzzyScoreGracefulAggressive(
                                prefix,
                                prefix.toLowerCase(Locale.ROOT),
                                0,
                                identifier,
                                identifier.toLowerCase(Locale.ROOT),
                                0,
                                FuzzyScoreOptions.getDefault());

                var score = fuzzyScore == null ? -100 : fuzzyScore.getScore();

                if ((TextUtils.startsWith(identifier, prefix, true) || score >= -20)
                        && !(prefix.length() == identifier.length()
                        && TextUtils.startsWith(prefix, identifier, false))) {
                    dest.add(identifier);
                }
            }
        }
    }

    public static class SyncIdentifiers implements Identifiers {

        private final Lock lock = new ReentrantLock(true);
        private final Map<String, MutableInt> identifierMap = new HashMap<>();

        public void clear() {
            lock.lock();
            try {
                identifierMap.clear();
            } finally {
                lock.unlock();
            }
        }

        public void identifierIncrease(@NonNull String identifier) {
            lock.lock();
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    identifierMap.computeIfAbsent(identifier, (x) -> new MutableInt(0)).increase();
                } else {
                    var counter = identifierMap.get(identifier);
                    if (counter == null) {
                        counter = new MutableInt(0);
                        identifierMap.put(identifier, counter);
                    }
                    counter.increase();
                }
            } finally {
                lock.unlock();
            }
        }

        public void identifierDecrease(@NonNull String identifier) {
            lock.lock();
            try {
                var count = identifierMap.get(identifier);
                if (count != null) {
                    if (count.decreaseAndGet() <= 0) {
                        identifierMap.remove(identifier);
                    }
                }
            } finally {
                lock.unlock();
            }
        }

        @Override
        public void filterIdentifiers(@NonNull String prefix, @NonNull List<String> dest) {
            filterIdentifiers(prefix, dest, false);
        }

        public void filterIdentifiers(
                @NonNull String prefix, @NonNull List<String> dest, boolean waitForLock) {
            boolean acquired;
            if (waitForLock) {
                lock.lock();
                acquired = true;
            } else {
                try {
                    acquired = lock.tryLock(3, TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                    acquired = false;
                }
            }
            if (acquired) {
                try {
                    for (String s : identifierMap.keySet()) {
                        var fuzzyScore =
                                Filters.fuzzyScoreGracefulAggressive(
                                        prefix,
                                        prefix.toLowerCase(Locale.ROOT),
                                        0,
                                        s,
                                        s.toLowerCase(Locale.ROOT),
                                        0,
                                        FuzzyScoreOptions.getDefault());

                        var score = fuzzyScore == null ? -100 : fuzzyScore.getScore();

                        if ((TextUtils.startsWith(s, prefix, true) || score >= -20)
                                && !(prefix.length() == s.length() && TextUtils.startsWith(prefix, s, false))) {
                            dest.add(s);
                        }
                    }
                } finally {
                    lock.unlock();
                }
            }
        }
    }
}
