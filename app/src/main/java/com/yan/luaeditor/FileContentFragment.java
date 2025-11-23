package com.yan.luaeditor;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.luaparser.ErrorLocationHelper;
import com.luaparser.parser.ParseState;
import com.yan.luaeditor.adapter.MyCompletionAdapter;
import com.yan.luaeditor.format.LuaLexer;
import com.yan.luaeditor.format.LuaTokenTypes;
import com.yan.luaeditor.lualanguage.LuaLanguage;
import com.yan.luaeditor.scheme.SchemeAtom;
import com.yan.luaeditor.scheme.SchemeGitHubDark;
import com.yan.luaeditor.scheme.SchemeIntelliJDark;
import com.yan.luaeditor.scheme.SchemeSublime;
import com.yan.luaeditor.scheme.SchemeVimDark;
import com.yan.luaeditor.scheme.SchemeWebStormDark;
import com.yan.luaeditor.scheme.YluaScheme;
import com.yan.luaeditor.tools.SoraDiagnosticUtils;
import com.yan.luaeditor.tools.ThreadManager;
import com.yan.luaeditor.tools.YanDialog;
import com.yan.luaeditor.tools.parser.LuaParser;
import com.yan.luaeditor.tools.parser.Token;
import com.yan.luaide.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.PatternSyntaxException;

import io.github.rosemoe.sora.event.ContentChangeEvent;
import io.github.rosemoe.sora.event.EventManager;
import io.github.rosemoe.sora.event.SelectionChangeEvent;
import io.github.rosemoe.sora.lang.diagnostic.DiagnosticsContainer;
import io.github.rosemoe.sora.widget.CodeEditor;
import io.github.rosemoe.sora.widget.EditorSearcher;
import io.github.rosemoe.sora.widget.SelectionMovement;
import io.github.rosemoe.sora.widget.SymbolInputView;
import io.github.rosemoe.sora.widget.component.EditorAutoCompletion;
import io.github.rosemoe.sora.widget.schemes.EditorColorScheme;
import io.github.rosemoe.sora.widget.schemes.SchemeDarcula;
import io.github.rosemoe.sora.widget.schemes.SchemeEclipse;
import io.github.rosemoe.sora.widget.schemes.SchemeGitHub;
import io.github.rosemoe.sora.widget.schemes.SchemeNotepadXX;
import io.github.rosemoe.sora.widget.schemes.SchemeVS2019;

import com.yan.luaide.databinding.FragmentBinding;

public class FileContentFragment extends Fragment {

    public String fileName;
    public CodeEditor edit;
    SymbolAdapter adapter;
    SymbolInputView sym;
    LinearLayout searchPanel;
    EditText search, replace;
    FragmentBinding binding;
    private RecyclerView recyclerViewSymbols;
    private BottomSheetBehavior<LinearLayout> bottomSheetBehavior;
    LinearLayout root;
    private LinearLayout llBottom;
    SharedPreferences symbol, equivalent, sps;
    List<String> symbolslist = new ArrayList<>();
    List<String> equivalentslist = new ArrayList<>();
    String[] symbols = null;
    String[] equivalents = null;
    LuaLanguage language;
    PopupMenu searchMenu;
    Button search_options, gotonext, gotolast, replaceall, replases;
    EditorSearcher.SearchOptions searchOptions = new EditorSearcher.SearchOptions(false, false);
    io.github.rosemoe.sora.widget.component.EditorTextActionWindow editorTextActionWindow;
    Editor activity;
    HashMap<String, String> importmap = new HashMap<>();
    public static FileContentFragment newInstance() {
        return new FileContentFragment();
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        init();


        //setSearchPanel(true);
        return view;
    }

    public void gotoNext() {
        try {
            edit.getSearcher().gotoNext();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    public void gotoPrev() {
        try {
            edit.getSearcher().gotoPrevious();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    public void replace() {
        try {
            edit
                    .getSearcher()
                    .replaceThis(binding.replaceEditor.getText().toString());
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    public void replaceAll() {
        try {
            edit
                    .getSearcher()
                    .replaceAll(binding.replaceEditor.getText().toString());
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onResume() {
        initsymbol();
        SharedPreferences Scheme = getActivity().getSharedPreferences("EditorSet", Context.MODE_PRIVATE);
        int colorscheme = Scheme.getInt("ColorScheme", 0);
        if (colorscheme == 0) {
            edit.setColorScheme(new s_scheme());
        } else if (colorscheme == 1) {
            edit.setColorScheme(new YluaScheme());
        } else if (colorscheme == 2) {
            edit.setColorScheme(new SchemeDarcula());
        } else if (colorscheme == 3) {
            edit.setColorScheme(new SchemeEclipse());
        } else if (colorscheme == 4) {
            edit.setColorScheme(new SchemeGitHub());
        } else if (colorscheme == 5) {
            edit.setColorScheme(new SchemeNotepadXX());
        } else if (colorscheme == 6) {
            edit.setColorScheme(new SchemeVS2019());
        } else if (colorscheme == 7) {
            edit.setColorScheme(new SchemeAtom());
        } else if (colorscheme == 8) {
            edit.setColorScheme(new SchemeGitHubDark());
        } else if (colorscheme == 9) {
            edit.setColorScheme(new SchemeIntelliJDark());
        } else if (colorscheme == 10) {
            edit.setColorScheme(new SchemeSublime());
        } else if (colorscheme == 11) {
            edit.setColorScheme(new SchemeVimDark());
        } else if (colorscheme == 12) {
            edit.setColorScheme(new SchemeWebStormDark());
        }

        super.onResume();
    }

    private void onSelectChange(SelectionChangeEvent event) {
        //if (event.isSelected()){
        //Toast.makeText(getActivity(),"change",Toast.LENGTH_SHORT).show();
        //}
        editorTextActionWindow.getView().findViewById(R.id.panel_btn_analyze).setVisibility(event.isSelected() ? View.VISIBLE : View.GONE);
        editorTextActionWindow.getView().findViewById(R.id.panel_btn_search).setVisibility(event.isSelected() ? View.VISIBLE : View.GONE);
        //onContentChangeEvent();
    }
    com.luaparser.LuaParser.API api=com.luaparser.LuaParser.create();
    private void onContentChangeEvent(){
        ThreadManager.runOnMainThread(new Runnable() {
            @Override
            public void run() {
                DiagnosticsContainer diagnosticsContainer=new DiagnosticsContainer();

                //System.out.println(edit.getText().toString());

                //
                //ParserState result = com.luaparser.LuaParser.compile(edit.getText().toString(), "Lua", "Lua 5.4", null);
                try {
                    ParseState result = api.compile(edit.getText().toString(), "Lua", "Lua 5.3", null);
                    List<ErrorLocationHelper.ErrorLocation> locations=ErrorLocationHelper.getErrorLocations(result);
                    for (int i = 0; i < result.getErrors().size(); i++) {
                        SoraDiagnosticUtils.addDiagnostic(diagnosticsContainer,result.getErrors().get(i).getStart(),result.getErrors().get(i).getFinish(),result.getErrors().get(i).getType(),locations.get(i).getError().getMessage(),null);
                    }
                    edit.setDiagnostics(diagnosticsContainer);
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }

            }
        });

    }

    int position = 0;

    private void initEditorFun() {
        editorTextActionWindow.getView().findViewById(R.id.panel_btn_analyze).setOnClickListener(v -> {
            LuaLexer lexer = new LuaLexer(edit.getText().toString());
            LuaTokenTypes tokenType;
            position = 0;
            String afterString = null;
            while (true) {
                try {
                    if ((tokenType = lexer.advance()) == null) break;

                    String tokenValue = lexer.yytext();
                    //position += tokenValue.length() + 1;
                    if (afterString != null && afterString.equals("require") && tokenValue.equals("\"import\"")) {
                        position++;
                        break;
                    }
                    if (tokenType == LuaTokenTypes.NAME || tokenType == LuaTokenTypes.STRING)
                        afterString = tokenValue;
                    if (tokenType == LuaTokenTypes.NEW_LINE) position++;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            com.yan.luaeditor.tools.parser.LuaLexer lexer2;
            List<Token> tokens = null;
            try {
                lexer2 = new com.yan.luaeditor.tools.parser.LuaLexer(edit.getText().toString());
                tokens = lexer2.tokenize();
            } catch (RuntimeException e) {
                System.out.println(e.getMessage());
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
            LuaParser parser = new LuaParser(tokens);
            importmap = new HashMap<>();
            //long startTime = System.currentTimeMillis();

            for (String str : parser.parseImports()) {
                String ss = str.replace(".*", "");
                for (String key : new InitCompletion(activity).getClassNameList(activity.classMap2)) {
                    if (key.startsWith(ss)) {
                        importmap.put(key, key);
                    }
                }
            }
            if (edit.getCursor().isSelected()) {
                String selectedText = edit.getText().substring(
                        edit.getCursor().getLeft(),
                        edit.getCursor().getRight()
                );

                // 检查是否已导入
                String foundImport = null;
                for (String imp : importmap.keySet()) {
                    String[] nameParts = imp.split("\\.");
                    if (nameParts[nameParts.length - 1].equals(selectedText)) {
                        foundImport = imp;
                        break;
                    }
                }

                // 处理结果
                MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(getActivity());

                if (foundImport != null) {
                    dialogBuilder.setTitle("分析结果")
                            .setMessage("已导入该类: " + foundImport)
                            .show();
                } else {
                    List<String> availableClasses = activity.classMap2.get(selectedText);
                    if (availableClasses != null && !availableClasses.isEmpty()) {
                        String[] classArray = availableClasses.toArray(new String[0]);
                        dialogBuilder.setTitle("选择要导入的类")
                                .setItems(classArray, (dialog, i) -> {
                                    edit.getText().insert(position, 0, "\nimport \"" + classArray[i] + "\"\n");
                                })
                                .show();
                    } else {
                        Toast.makeText(getActivity(), "无此类", Toast.LENGTH_SHORT).show();
                    }
                }
            } else {
                Toast.makeText(getActivity(), "请先选中要分析的类", Toast.LENGTH_SHORT).show();
            }

            //AlertDialog alertDialog=new AlertDialog(materialAlertDialogBuilder).create();
            //Toast.makeText(getActivity(),activity.classMap2.get(string).get(0),Toast.LENGTH_SHORT).show();
        });
        editorTextActionWindow.getView().findViewById(R.id.panel_btn_search).setOnClickListener(v -> {
            if (edit.getCursor().isSelected()) {
                setSearchPanel(true);
            }
        });
        EditorAutoCompletion component = edit.getComponent(EditorAutoCompletion.class);
        MyCompletionAdapter adapter1 = new MyCompletionAdapter();
        adapter1.setOnItemClickListener((label, desc, pos) -> {
            com.yan.luaeditor.tools.parser.LuaLexer lexer2;
            List<Token> tokens = null;
            try {
                lexer2 = new com.yan.luaeditor.tools.parser.LuaLexer(edit.getText().toString());
                tokens = lexer2.tokenize();
            } catch (RuntimeException e) {
                System.out.println(e.getMessage());
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
            LuaParser parser = new LuaParser(tokens);
            importmap = new HashMap<>();
            //long startTime = System.currentTimeMillis();
            for (String str : parser.parseImports()) {
                String ss = str.replace(".*", "");
                for (String key : new InitCompletion(activity).getClassNameList(activity.classMap2)) {
                    if (key.startsWith(ss)) {
                        importmap.put(key, key);
                    }
                }
            }
            component.select(pos);

            if (importmap != null && importmap.get(desc) == null) {
                LuaLexer lexer = new LuaLexer(edit.getText().toString());
                LuaTokenTypes tokenType;
                position = 0;
                String afterString = null;
                while (true) {
                    try {
                        if ((tokenType = lexer.advance()) == null) break;

                        String tokenValue = lexer.yytext();
                        //position += tokenValue.length() + 1;
                        if (afterString != null && afterString.equals("require") && tokenValue.equals("\"import\"")) {
                            position++;
                            break;
                        }
                        if (tokenType == LuaTokenTypes.NAME || tokenType == LuaTokenTypes.STRING)
                            afterString = tokenValue;
                        if (tokenType == LuaTokenTypes.NEW_LINE) position++;
                    } catch (IOException e) {
                        //throw new RuntimeException(e);
                    }
                }
                edit.getText().insert(position, 0, "\nimport \"" + desc + "\"\n");
                //component.select(pos);

            }

            //edit.insertText(label, label.length());
        });
        component.setAdapter(adapter1);
    }

    public void init() {
        activity = (Editor) getActivity();

        edit = binding.activityMainioGithubRosemoeSoraWidgetCodeEditor;
        //sym = binding.symbolInput;
        searchPanel = binding.searchPanel;
        search = binding.searchEditor;
        replace = binding.replaceEditor;
        recyclerViewSymbols = binding.recyclerViewSymbols;
        llBottom = binding.llBottom;
        root = binding.root;
        search_options = binding.searchOptions;
        replases = binding.replace;
        gotolast = binding.gotoLast;
        gotonext = binding.gotoNext;
        replaceall = binding.replaceAll;
        editorTextActionWindow = edit.getComponent(io.github.rosemoe.sora.widget.component.EditorTextActionWindow.class);
        //System.out.println(activity.classMap2);

        edit.subscribeAlways(SelectionChangeEvent.class, event -> onSelectChange(event));
        edit.subscribeAlways(ContentChangeEvent.class, event -> onContentChangeEvent());
        initEditorFun();
        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                tryCommitSearch();
            }
        });
        searchMenu = new PopupMenu(activity, search_options);
        searchMenu.inflate(R.menu.menu_search_options);
        searchMenu.setOnMenuItemClickListener(
                item -> {
                    // Update option states
                    item.setChecked(!item.isChecked());
                    if (item.isChecked()) {
                        // Regex and whole word mode can not be both chose
                        int iid = item.getItemId();
                        if (iid == R.id.search_option_regex) {
                            searchMenu.getMenu().findItem(R.id.search_option_whole_word).setChecked(false);
                        } else if (iid == R.id.search_option_whole_word) {
                            searchMenu.getMenu().findItem(R.id.search_option_regex).setChecked(false);
                        }
                    }
                    computeSearchOptions();
                    tryCommitSearch();
                    return true;
                });
        search_options.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchMenu.show();
            }
        });
        gotonext.setOnClickListener(view -> {
            gotoNext();
        });
        gotolast.setOnClickListener(view -> {
            gotoPrev();
        });
        replases.setOnClickListener(view -> {
            replace();
        });
        replaceall.setOnClickListener(view -> {
            replaceAll();
        });
        //sym.bindEditor(edit);
        sps = getActivity().getSharedPreferences("EditorSet", Context.MODE_PRIVATE);

        initsymbol();

        bottomSheetBehavior = BottomSheetBehavior.from(llBottom);
        searchPanel.setVisibility(View.GONE);
        recyclerViewSymbols.post(() -> {
            int displayHeight = calculateFirstItemHeight(recyclerViewSymbols);
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) root.getLayoutParams();
            params.height = ViewGroup.LayoutParams.MATCH_PARENT;
            params.bottomMargin = displayHeight;
            root.setLayoutParams(params);
            bottomSheetBehavior.setPeekHeight(displayHeight);
            bottomSheetBehavior.setFitToContents(true);
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        });

        if (activity.base == null) {
            try {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    InitCompletion initCompletion = new InitCompletion(activity);
                    activity.classMap2 = initCompletion.getCM();
                    activity.base = new InitCompletion(activity).getClassTree(initCompletion.getClassNameList(activity.classMap2));
                }

                language = new LuaLanguage(activity.base, activity.classMap2, new InitCompletion(activity).getClassNameList(activity.classMap2));
                language.setLayoutPath(new File(activity.mdir).getParent() + "/layout");
                var typeface = Typeface.createFromAsset(activity.getAssets(), "JetBrainsMono-Regular.ttf");
                edit.setTypefaceText(typeface);
                edit.setTypefaceLineNumber(typeface);
                edit.getProps().stickyScroll = true;
                edit.setLineSpacing(2f, 1.1f);
                edit.setEditorLanguage(language);
                edit.getComponent(EditorAutoCompletion.class).setEnabledAnimation(true);
            } catch (Exception e) {
                //YanDialog.show(activity, "error", "初始化错误：" + e.getMessage());
                System.out.println(e.getMessage());
            }
            //System.out.println(activity.base);


        } else {
            try {
                language = new LuaLanguage(activity.base, activity.classMap2, new InitCompletion(activity).getClassNameList(activity.classMap2));
                language.setLayoutPath(new File(activity.mdir).getParent() + "/layout");
                var typeface = Typeface.createFromAsset(activity.getAssets(), "JetBrainsMono-Regular.ttf");
                edit.setTypefaceText(typeface);
                //edit.setTypefaceLineNumber(typeface);
                edit.getProps().stickyScroll = true;
                edit.setLineSpacing(2f, 1.1f);
                edit.setEditorLanguage(language);
                edit.getComponent(EditorAutoCompletion.class).setEnabledAnimation(true);
            } catch (Exception e) {
                YanDialog.show(activity, "error", "初始化错误：" + e.getMessage());
            }
        }
        try {
            SharedPreferences Scheme = activity.getSharedPreferences("EditorSet", Context.MODE_PRIVATE);
            int colorscheme = Scheme.getInt("ColorScheme", 0);
            if (colorscheme == 0) {
                edit.setColorScheme(new s_scheme());
            } else if (colorscheme == 1) {
                edit.setColorScheme(new YluaScheme());
            } else if (colorscheme == 2) {
                edit.setColorScheme(new SchemeDarcula());
            } else if (colorscheme == 3) {
                edit.setColorScheme(new SchemeEclipse());
            } else if (colorscheme == 4) {
                edit.setColorScheme(new SchemeGitHub());
            } else if (colorscheme == 5) {
                edit.setColorScheme(new SchemeNotepadXX());
            } else if (colorscheme == 6) {
                edit.setColorScheme(new SchemeVS2019());
            } else if (colorscheme == 7) {
                edit.setColorScheme(new SchemeAtom());
            } else if (colorscheme == 8) {
                edit.setColorScheme(new SchemeGitHubDark());
            } else if (colorscheme == 9) {
                edit.setColorScheme(new SchemeIntelliJDark());
            } else if (colorscheme == 10) {
                edit.setColorScheme(new SchemeSublime());
            } else if (colorscheme == 11) {
                edit.setColorScheme(new SchemeVimDark());
            } else if (colorscheme == 12) {
                edit.setColorScheme(new SchemeWebStormDark());
            }
            //PackageUtil.load(getActivity());

            edit.setNonPrintablePaintingFlags(
                    CodeEditor.FLAG_DRAW_WHITESPACE_LEADING
                            | CodeEditor.FLAG_DRAW_LINE_SEPARATOR
                            | CodeEditor.FLAG_DRAW_WHITESPACE_IN_SELECTION);
            edit.setWordwrap(sps.getBoolean("Wordwarp", true));
            edit.setLineNumberEnabled(sps.getBoolean("linenumber", true));
            edit.setPinLineNumber(sps.getBoolean("pin", false));
            edit.setBlockLineEnabled(true);
        } catch (Exception e) {
            YanDialog.show(getActivity(), "", e.getMessage());
        }
        ThreadManager.runOnMainThread(
                new Runnable() {
                    @Override
                    public void run() {
                        ThreadManager.runOnUiThread(
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            edit.setText(OpenFile(fileName));

                                        } catch (Exception e) {
                                        }

                                    }
                                });

                    }
                });

    }

    private void initsymbol() {
        symbol = getActivity().getSharedPreferences("EditorSymbol", Context.MODE_PRIVATE);
        equivalent = getActivity().getSharedPreferences("Equivalents", Context.MODE_PRIVATE);
        symbolslist = new ArrayList<>();
        equivalentslist = new ArrayList<>();
        if (symbol != null) {
            Map<String, ?> symbol_list = symbol.getAll();
            Set<String> keys = symbol_list.keySet();
            List<Integer> sortedKeys = new ArrayList<>();
            for (String key : keys) {
                sortedKeys.add(Integer.parseInt(key));
            }
            Collections.sort(sortedKeys);

            for (Integer sortedKey : sortedKeys) {
                String keyStr = String.valueOf(sortedKey);
                symbolslist.add(symbol.getString(keyStr, null));
                equivalentslist.add(equivalent.getString(keyStr, null));
            }
            //System.out.println(equivalentslist.toString());
            symbols = new String[symbol_list.size()];
            equivalents = new String[symbol_list.size()];
            for (int i = 0; i < symbol_list.size(); i++) {
                symbols[i] = symbolslist.get(i);
                equivalents[i] = equivalentslist.get(i);
            }
        }
        adapter = new SymbolAdapter(getActivity(), symbols);
        adapter.setOnItemClickListener(new SymbolAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                switch (equivalents[position]) {
                    case "UP":
                        edit.moveSelection(SelectionMovement.UP);
                        break;
                    case "DOWN":
                        edit.moveSelection(SelectionMovement.DOWN);
                        break;
                    case "RIGHT":
                        edit.moveSelection(SelectionMovement.RIGHT);
                        break;
                    case "LEFT":
                        edit.moveSelection(SelectionMovement.LEFT);
                        break;
                    case "LINE_START":
                        edit.moveSelection(SelectionMovement.LINE_START);
                        break;
                    case "LINE_END":
                        edit.moveSelection(SelectionMovement.LINE_END);
                        break;
                    default:
                        edit.insertText(equivalents[position], equivalents[position].length());
                        break;
                }

            }
        });
        int spanCount = calculateSpanCount();
        GridLayoutManager layoutManager = new GridLayoutManager(getActivity(), spanCount - 5);
        recyclerViewSymbols.setLayoutManager(layoutManager);
        recyclerViewSymbols.setAdapter(adapter);
    }


    private static int getLineNumber(String text, int index) {
        int lineNumber = 1;
        int lineStartIndex = 0;
        while (index > lineStartIndex && lineStartIndex < text.length()) {
            if (text.charAt(lineStartIndex) == '\n') {
                lineNumber++;
            }
            lineStartIndex++;
        }
        return lineNumber;
    }


    public String getFileName() {
        return new File(fileName).getName();
    }

    public String OpenFile(String filename) throws IOException {
        if (!new File(filename).exists()) {
            return "";
        }

        BufferedReader reader = new BufferedReader(new FileReader(filename));
        StringBuilder buf = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) buf.append(line).append("\n");
        if (buf.length() > 1) buf.setLength(buf.length() - 1);
        return buf.toString();
    }

    private void computeSearchOptions() {
        MenuItem matchCaseItem = searchMenu.getMenu().findItem(R.id.search_option_match_case);
        boolean caseInsensitive = matchCaseItem.isChecked();

        int type = EditorSearcher.SearchOptions.TYPE_NORMAL;
        MenuItem regexItem = searchMenu.getMenu().findItem(R.id.search_option_regex);
        boolean regex = regexItem.isChecked();
        if (regex) {
            type = EditorSearcher.SearchOptions.TYPE_REGULAR_EXPRESSION;
        }

        MenuItem wholeWordItem = searchMenu.getMenu().findItem(R.id.search_option_whole_word);
        boolean wholeWord = wholeWordItem.isChecked();
        if (wholeWord) {
            type = EditorSearcher.SearchOptions.TYPE_WHOLE_WORD;
        }
        MenuItem close = searchMenu.getMenu().findItem(R.id.search_option_close);
        close.setOnMenuItemClickListener(
                new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem arg0) {
                        //if (arg0.getItemId() == R.id.search_option_close) {
                        //setSearchPanel(false);
                        //}
                        binding.searchEditor.setText("");
                        binding.replaceEditor.setText("");
                        edit.getSearcher().stopSearch();
                        setSearchPanel(false);
                        return false;
                    }
                });
        searchOptions = new EditorSearcher.SearchOptions(type, caseInsensitive);
    }

    private void tryCommitSearch() {
        Editable query = binding.searchEditor.getEditableText();
        if (query.length() > 0) {
            try {
                edit.getSearcher().search(query.toString(), searchOptions);
                //binding.searchEditor.requestFocus();
            } catch (PatternSyntaxException e) {
                // Regex error
            }
        } else {
            edit.getSearcher().stopSearch();
        }
    }


    public void redo() {
        edit.redo();
    }

    public boolean canredo() {
        return edit.canRedo();
    }

    public void undo() {
        edit.undo();
    }

    public boolean canundo() {
        return edit.canUndo();
    }

    public void setSearchPanel(boolean isshow) {
        if (isshow) {
            searchPanel.setVisibility(View.VISIBLE);
            llBottom.setVisibility(View.GONE);
            if (edit.getCursor().isSelected()) {
                binding.searchEditor.setText(edit.getText().substring(edit.getCursor().getLeft(), edit.getCursor().getRight()));
            }
            computeSearchOptions();
            tryCommitSearch();
            searchPanel.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    searchPanel.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                    int height = searchPanel.getHeight();
                    //System.out.println(height);
                    if (height > 0) {
                        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) root.getLayoutParams();
                        params.bottomMargin = height;
                        root.setLayoutParams(params);
                    }
                }
            });
        } else {
            searchPanel.setVisibility(View.GONE);
            llBottom.setVisibility(View.VISIBLE);
            //computeSearchOptions();
            //edit.beginSearchMode();
            recyclerViewSymbols.post(() -> {
                int displayHeight = calculateFirstItemHeight(recyclerViewSymbols);
                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) root.getLayoutParams();
                params.height = ViewGroup.LayoutParams.MATCH_PARENT;
                params.bottomMargin = displayHeight;
                root.setLayoutParams(params);
                bottomSheetBehavior.setPeekHeight(displayHeight);
                bottomSheetBehavior.setFitToContents(true);
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            });
        }
    }

    private int calculateSpanCount() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        requireActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int screenWidth = displayMetrics.widthPixels;
        View itemView = getLayoutInflater().inflate(R.layout.symbol_item, null, false);
        int widthSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        int heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        itemView.measure(widthSpec, heightSpec);
        int itemWidth = itemView.getMeasuredWidth();
        return screenWidth / itemWidth;
    }

    private int calculateFirstItemHeight(RecyclerView recyclerView) {
        if (recyclerView == null) {
            return 0;
        }
        RecyclerView.Adapter adapter = recyclerView.getAdapter();
        if (adapter == null || adapter.getItemCount() == 0) {
            return 0;
        }

        int width = recyclerView.getWidth();
        if (width == 0) {
            return 0;
        }

        return calculateItemHeight(adapter, width);
    }

    private int calculateItemHeight(RecyclerView.Adapter adapter, int width) {
        int viewType = adapter.getItemViewType(0);
        RecyclerView.ViewHolder viewHolder = adapter.onCreateViewHolder(recyclerViewSymbols, viewType);
        adapter.onBindViewHolder(viewHolder, 0);
        View itemView = viewHolder.itemView;

        RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) itemView.getLayoutParams();
        if (layoutParams == null) {
            layoutParams = new RecyclerView.LayoutParams(RecyclerView.LayoutParams.WRAP_CONTENT, RecyclerView.LayoutParams.WRAP_CONTENT);
        }

        int widthSpec = View.MeasureSpec.makeMeasureSpec(width - layoutParams.leftMargin - layoutParams.rightMargin, View.MeasureSpec.EXACTLY);
        int heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        itemView.measure(widthSpec, heightSpec);

        return itemView.getMeasuredHeight() + layoutParams.topMargin + layoutParams.bottomMargin;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    public class s_scheme extends EditorColorScheme {
        public s_scheme() {
            super(false);
        }

        @Override
        public void applyDefault() {
            super.applyDefault();
            SharedPreferences sps = getActivity().getSharedPreferences("EditorSet", Context.MODE_PRIVATE);
            setColor(EditorColorScheme.FUNCTION_NAME, sps.getInt("FUNCTION_NAME", 0xff2196f3));
            setColor(EditorColorScheme.IDENTIFIER_VAR, sps.getInt("IDENTIFIER_VAR", 0xffffa200));
            setColor(
                    EditorColorScheme.LINE_NUMBER_BACKGROUND, sps.getInt("LINE_NUMBER_BACKGROUND", 0xffffffff));
            setColor(EditorColorScheme.LINE_NUMBER, sps.getInt("LINE_NUMBER", 0xFF000000));
            setColor(EditorColorScheme.LINE_DIVIDER, sps.getInt("LINE_DIVIDER", 0xffffffff));
            setColor(EditorColorScheme.WHOLE_BACKGROUND, sps.getInt("WHOLE_BACKGROUND", 0xffffffff));
            setColor(EditorColorScheme.TEXT_NORMAL, sps.getInt("TEXT_NORMAL", 0xFF333333));
            setColor(EditorColorScheme.KEYWORD, sps.getInt("KEYWORD", 0xFFe03e3e));
            setColor(
                    EditorColorScheme.LINE_NUMBER_CURRENT, sps.getInt("LINE_NUMBER_CURRENT", 0xFF505050));
            setColor(EditorColorScheme.CURRENT_LINE, sps.getInt("CURRENT_LINE", 0x10000000));
            setColor(
                    EditorColorScheme.BLOCK_LINE_CURRENT, sps.getInt("BLOCK_LINE_CURRENT", 0xff999999));
            setColor(EditorColorScheme.BLOCK_LINE, sps.getInt("BLOCK_LINE", 0xffdddddd));
            setColor(
                    EditorColorScheme.HIGHLIGHTED_DELIMITERS_FOREGROUND,
                    sps.getInt("HIGHLIGHTED_DELIMITERS_FOREGROUND", 0xdd000000));
            setColor(EditorColorScheme.SIDE_BLOCK_LINE, sps.getInt("SIDE_BLOCK_LINE", 0xff999999));
            setColor(EditorColorScheme.COMMENT, sps.getInt("COMMENT", 0xffa8a8a8));
            setColor(EditorColorScheme.OPERATOR, sps.getInt("OPERATOR", 0xFF0066D6));
            setColor(EditorColorScheme.LITERAL, sps.getInt("LITERAL", 0xFF008080));
        }
    }
}