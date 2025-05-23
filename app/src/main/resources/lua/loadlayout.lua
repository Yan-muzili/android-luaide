luajava.ids = luajava.ids or { id = 0x7f000000 }
local ids = luajava.ids
local insert = table.insert
local bindClass = luajava.bindClass
local newInstance = luajava.newInstance
local context = activity
local ltrs={}
local luadir = context.getLuaDir()

local TooltipCompat, ContextThemeWrapper, DisplayMetrics, Context, NineBitmapDrawable, File, ViewGroup, ArrayPageAdapter, AppBarLayoutLayoutParams, BasePagerAdapter, ArrayListAdapter, ArrayList, String, Typeface, Gravity, OnClickListener, OnLongClickListener, TypedValue, BitmapDrawable, LuaBitmapDrawable, LuaAdapter, AdapterView, ScaleType, TruncateAt, Glide =
bindClass "androidx.appcompat.widget.TooltipCompat", bindClass "androidx.appcompat.view.ContextThemeWrapper", bindClass "android.util.DisplayMetrics", bindClass "android.content.Context", bindClass "com.yan.luaide.NineBitmapDrawable", bindClass "java.io.File", bindClass "android.view.ViewGroup", bindClass "android.widget.ArrayPageAdapter", bindClass "com.google.android.material.appbar.AppBarLayout$LayoutParams", bindClass "android.widget.LuaBasePagerAdapter", bindClass "android.widget.ArrayListAdapter", bindClass "java.util.ArrayList", bindClass "java.lang.String", bindClass "android.graphics.Typeface", bindClass "android.view.Gravity", bindClass "android.view.View$OnClickListener", bindClass "android.view.View$OnLongClickListener", bindClass "android.util.TypedValue", bindClass "android.graphics.drawable.BitmapDrawable", bindClass "com.yan.luaide.LuaBitmapDrawable", bindClass "com.yan.luaide.LuaAdapter", bindClass "android.widget.AdapterView", bindClass "android.widget.ImageView$ScaleType", bindClass "android.text.TextUtils$TruncateAt", bindClass "com.bumptech.glide.Glide"
local getMetrics = function(context)
  local wm = context.getSystemService(Context.WINDOW_SERVICE);
  local outMetrics = DisplayMetrics();
  wm.getDefaultDisplay().getMetrics(outMetrics);
  return outMetrics.widthPixels, outMetrics.heightPixels
end

local dm, W, H = context.getResources().getDisplayMetrics(), getMetrics(context)
local alyloader = function(path)
  local alypath = package.path:gsub("%.lua;", ".aly;")
  local path, msg = package.searchpath(path, alypath)
  if msg return msg end
  local f = io.open(path)
  local s = f:read("*a")
  f:close()
  if string.sub(s, 1, 4) == "\27Lua"
    return assert(loadfile(path)), path
   else
    local f, st = loadstring("return " .. s, path:match("[^/]+/[^/]+$"), "bt")
    if st error(st:gsub("%b[]", path, 1), 0) end
    return f, st
  end
end
table.insert(package.searchers, alyloader)
android_R = bindClass("android.R")
android = {R = android_R}
local ST = ScaleType.values()
local toint, scaleType, rules, types =
{

  auto=0,
  low=1,
  high=2,

  auto=0,
  yes=1,
  no=2,

  none=0,
  software=1,
  hardware=2,

  ltr=0,
  rtl=1,
  inherit=2,
  locale=3,

  insideOverlay=0x0,
  insideInset=0x01000000,
  outsideOverlay=0x02000000,
  outsideInset=0x03000000,

  visible=0,
  invisible=4,
  gone=8,

  wrap_content=-2,
  fill_parent=-1,
  match_parent=-1,
  wrap=-2,
  fill=-1,
  match=-1,

  none=0x00,
  web=0x01,
  email=0x02,
  phon=0x04,
  map=0x08,
  all=0x0f,

  vertical=1,
  horizontal= 0,

  axis_clip = 8,
  axis_pull_after = 4,
  axis_pull_before = 2,
  axis_specified = 1,
  axis_x_shift = 0,
  axis_y_shift = 4,
  bottom = 80,
  center = 17,
  center_horizontal = 1,
  center_vertical = 16,
  clip_horizontal = 8,
  clip_vertical = 128,
  display_clip_horizontal = 16777216,
  display_clip_vertical = 268435456,

  fill_horizontal = 7,
  fill_vertical = 112,
  horizontal_gravity_mask = 7,
  left = 3,
  no_gravity = 0,
  relative_horizontal_gravity_mask = 8388615,
  relative_layout_direction = 8388608,
  right = 5,
  start = 8388611,
  top = 48,
  vertical_gravity_mask = 112,
  ["end"] = 8388613,

  inherit=0,
  gravity=1,
  textStart=2,
  textEnd=3,
  textCenter=4,
  viewStart=5,
  viewEnd=6,

  none=0x00000000,
  text=0x00000001,
  textCapCharacters=0x00001001,
  textCapWords=0x00002001,
  textCapSentences=0x00004001,
  textAutoCorrect=0x00008001,
  textAutoComplete=0x00010001,
  textMultiLine=0x00020001,
  textImeMultiLine=0x00040001,
  textNoSuggestions=0x00080001,
  textUri=0x00000011,
  textEmailAddress=0x00000021,
  textEmailSubject=0x00000031,
  textShortMessage=0x00000041,
  textLongMessage=0x00000051,
  textPersonName=0x00000061,
  textPostalAddress=0x00000071,
  textPassword=0x00000081,
  textVisiblePassword=0x00000091,
  textWebEditText=0x000000a1,
  textFilter=0x000000b1,
  textPhonetic=0x000000c1,
  textWebEmailAddress=0x000000d1,
  textWebPassword=0x000000e1,
  number=0x00000002,
  numberSigned=0x00001002,
  numberDecimal=0x00002002,
  numberPassword=0x00000012,
  phone=0x00000003,
  datetime=0x00000004,
  date=0x00000014,
  time=0x00000024,

  normal=0x00000000,
  actionUnspecified=0x00000000,
  actionNone=0x00000001,
  actionGo=0x00000002,
  actionSearch=0x00000003,
  actionSend=0x00000004,
  actionNext=0x00000005,
  actionDone=0x00000006,
  actionPrevious=0x00000007,
  flagNoFullscreen=0x2000000,
  flagNavigatePrevious=0x4000000,
  flagNavigateNext=0x8000000,
  flagNoExtractUi=0x10000000,
  flagNoAccessoryAction=0x20000000,
  flagNoEnterAction=0x40000000,
  flagForceAscii=0x80000000,

  scroll=1,
  exitUntilCollapsed=2,
  enterAlways=4,
  enterAlwaysCollapsed=8,
  snap=16,
  snapMargins = 32,

  pin=1,
  parallax=2,
}
,
{
  matrix = 0, fitXY = 1, fitStart = 2, fitCenter = 3, fitEnd = 4, center = 5, centerCrop = 6, centerInside = 7
}
,
{
  layout_above = 2, layout_alignBaseline = 4, layout_alignBottom = 8, layout_alignEnd = 19, layout_alignLeft = 5,
  layout_alignParentBottom = 12, layout_alignParentEnd = 21, layout_alignParentLeft = 9, layout_alignParentRight = 11,
  layout_alignParentStart = 20, layout_alignParentTop = 10, layout_alignRight = 7, layout_alignStart = 18,
  layout_alignTop = 6, layout_alignWithParentIfMissing = 0, layout_below = 3, layout_centerHorizontal = 14,
  layout_centerInParent = 13, layout_centerVertical = 15, layout_toEndOf = 17, layout_toLeftOf = 0,
  layout_toRightOf = 1, layout_toStartOf = 16
}
,
{
  px=0,dp=1,sp=2,pt=3,["in"]=4,mm=5
}

local function checkType(v)
  local n, ty = string.match(v, "^(%-?[%.%d]+)(%a%a)$")
  return tonumber(n), types[ty]
end

local function checkPercent(v)
  local n, ty = v:match("^(%-?[%d%.]+)%%([wh])$")
  return (ty and tonumber(n) * ((ty == "w") and W or H) / 100) or nil
end

local function split(s, t)
  local idx = 1
  local l = #s
  return function()
    local i = s:find(t, idx)
    if idx >= l return nil end
    if not i i = l + 1 end
    local sub = s:sub(idx, i - 1)
    idx = i + 1
    return sub
  end
end

local function checkint(s)
  local ret = 0
  for n in s:gmatch("[^|]+")
    if toint[n]
      ret = ret | toint[n]
     else
      return nil
    end
  end
  return ret
end

local function checkNumber(var)
  if type(var) == "string"
    if var=="true"
      return true
     elseif var=="false"
      return false
    end
    if toint[var]
      return toint[var]
    end
    local p = checkPercent(var)
    if p
      return p
    end
    local i = checkint(var)
    if i
      return i
    end
    local h = string.match(var,"^#(%x+)$")
    if h
      local c = tonumber(h,16)
      if c
        if #h<=6
          return c-0x1000000
         elseif #h<=8
          if c>0x7fffffff
            return c-0x100000000
           else
            return c
          end
        end
      end
    end
    local n,ty = checkType(var)
    if ty
      return TypedValue.applyDimension(ty,n,dm)
    end
  end
end

local function checkValue(var)
  return tonumber(var) or checkNumber(var) or var
end

local function checkValues(...)
  local vars = { ... }
  for n = 1, #vars
    vars[n] = checkValue(vars[n])
  end
  return unpack(vars)
end

local function getattr(s)
  return android_R.attr[s]
end

local function checkattr(s)
  local e, s = pcall(getattr, s)
  if e
    return s
  end
  return nil
end

local function getIdentifier(name)
  return context.getResources().getIdentifier(name,nil,nil)
end

local function dump2(t)
  local _t = {tostring(t), "\t{"}
  for k, v in pairs(t)
    if type(v) == "table"
      table.insert(_t, "\t\t" .. tostring(k) .. "={" .. tostring(v[1]) .. " ...}")
     else
      table.insert(_t, "\t\t" .. tostring(k) .. "=" .. tostring(v))
    end
  end
  table.insert(_t, "\t}")
  return table.concat(_t, "\n")
end

local ver = bindClass "android.os.Build".VERSION.SDK_INT

local function setBackground(view, bg)
  if ver < 16
    view.setBackgroundDrawable(bg)
   else
    view.setBackground(bg)
  end
end

local function setattribute(root, view, params, k, v, ids)
  if k == "layout_x"
    params.x = checkValue(v)

   elseif k == "layout_y"
    params.y = checkValue(v)

   elseif k=="w"
    params.width=checkValue(v)

   elseif k=="h"
    params.height=checkValue(v)

   elseif k == "layout_scrollFlags"
    params.setScrollFlags(checkValue(v))

   elseif k == "layout_weight"
    params.weight = checkValue(v)

   elseif k == "layout_gravity"
    params.gravity = checkValue(v)

   elseif k == "layout_marginStart"
    params.setMarginStart(checkValue(v))

   elseif k == "layout_marginEnd"
    params.setMarginEnd(checkValue(v))

   elseif k=="behavior_hideable"
    if params.getBehavior()
      params.getBehavior().setHideable(checkValue(v))
     else
      task(1,function()
        params.getBehavior().setHideable(checkValue(v))
      end)
    end

   elseif k=="behavior_skipCollapsed"
    if params.getBehavior()
      params.getBehavior().setSkipCollapsed(checkValue(v))
     else
      task(1,function()
        params.getBehavior().setSkipCollapsed(checkValue(v))
      end)
    end

   elseif k=="layout_collapseMode"
    params.setCollapseMode(checkValue(v))

   elseif k=="layout_collapseParallaxMultiplier"
    params.setParallaxMultiplier(checkValue(v))

   elseif k=="layout_anchor"
    params.setAnchorId(ids[v])

   elseif k=="layout_behavior"
    if tostring(v) == "@string/appbar_scrolling_view_behavior" or tostring(v) == "appbar_scrolling_view_behavior"
      local ScrollingViewBehavior = newInstance "com.google.android.material.appbar.AppBarLayout$ScrollingViewBehavior"
      params.setBehavior(ScrollingViewBehavior)
     elseif tostring(v) == "@string/bottom_sheet_behavior" or tostring(v) == "bottom_sheet_behavior"
      local mBottomSheetBehavior = newInstance "com.google.android.material.bottomsheet.BottomSheetBehavior"
      params.setBehavior(mBottomSheetBehavior)
     else
      params.setBehavior(checkValue(v))
    end

   elseif rules[k] and (v == true or v == "true")
    params.addRule(rules[k])

   elseif rules[k]
    params.addRule(rules[k], ids[v])

   elseif k == "TooltipText" or k == "tooltipText"
    TooltipCompat.setTooltipText(view, v)

   elseif k == "items"
    if type(v) == "table"

      if view.adapter
        view.adapter.addAll(v)
       else
        local adapter = ArrayListAdapter(context, android_R.layout.simple_list_item_1, String(v))
        view.setAdapter(adapter)
      end

     elseif type(v) == "function"

      if view.adapter
        view.adapter.addAll(v())
       else
        local adapter = ArrayListAdapter(context, android_R.layout.simple_list_item_1, String(v()))
        view.setAdapter(adapter)
      end

     elseif type(v) == "string"

      local v = rawget(root, v) or rawget(_G, v)
      if view.adapter
        view.adapter.addAll(v())
       else
        local adapter = ArrayListAdapter(context, android_R.layout.simple_list_item_1, String(v()))
        view.setAdapter(adapter)
      end
    end

   elseif k == "pages" and type(v) == "table"
    local success, err =pcall(function()
      local views = ArrayList()
      for n, o in ipairs(v)
        local tp = type(o)
        if tp == "string" or tp == "table"
          views.add(loadlayout(o, root))
         else
          views.add(o)
        end
      end
      view.setAdapter(BasePagerAdapter(views))
      end)
      if not success then
      local ps = {}
      for n,o in ipairs(v)
        local tp = type(o)
        if tp == "string" or tp == "table"
          table.insert(ps,loadlayout(o,root))
         else
          table.insert(ps,o)
        end
      end
      local adapter = ArrayPageAdapter(View(ps))
      view.setAdapter(adapter)
    end

   elseif k=="pagesWithTitle" and type(v)=="table"
    local list={}
    for n,o in ipairs(v[1])
      local tp=type(o)
      if tp=="string" or tp=="table"
        list[n]=loadlayout(o,root)
       else
        list[n]=o
      end
    end
    view.setAdapter(BasePagerAdapter(list,v[2]))

   elseif k == "textSize"
    if tonumber(v)
      view.setTextSize(tonumber(v))
     elseif type(v) == "string"
      local n, ty = checkType(v)
      if ty
        view.setTextSize(ty, n)
       else
        view.setTextSize(v)
      end
     else
      view.setTextSize(v)
    end

   elseif k == "textStyle"

    local Typeface = bindClass "android.graphics.Typeface"

    if v=="bold"
      local bold = Typeface.defaultFromStyle(Typeface.BOLD)
      view.setTypeface(bold)
     elseif v=="normal"
      local normal = Typeface.defaultFromStyle(Typeface.NORMAL)
      view.setTypeface(normal)
     elseif v=="italic"
      local italic = Typeface.defaultFromStyle(Typeface.ITALIC)
      view.setTypeface(italic)
     elseif v=="italic|bold" or v=="bold|italic"
      local bold_italic = Typeface.defaultFromStyle(Typeface.BOLD_ITALIC)
      view.setTypeface(bold_italic)
    end

   elseif k == "textAppearance"
    view.setTextAppearance(context, checkattr(v))

   elseif k == "ellipsize"
    view.setEllipsize(TruncateAt[string.upper(v)])

   elseif k == "url"
    view.loadUrl(url)

   elseif k == "src"
    local path = v
    if not path:find("^/") and path:sub(1, 4) ~= "http"
      local _path = luadir.."/"..path
      if (_path ~= nil)
        path = _path
      end
    end
    Glide.with(this).load(path).into(view)

   elseif k == "scaleType"
    view.setScaleType(ST[scaleType[v]])

   elseif k == "background"
    if type(v)=="string"
      if v:find("^%?")
        view.setBackgroundResource(getIdentifier(v:sub(2,-1)))

       elseif v:find("^#")
        view.setBackgroundColor(checkNumber(v))

       elseif rawget(root,v) or rawget(_G,v)
        v=rawget(root,v) or rawget(_G,v)
        if type(v)=="function"
          view.setBackground(LuaDrawable(v))

         elseif type(v)=="userdata"
          view.setBackground(v)

        end
       else
        if (not v:find("^/")) and luadir
          v=luadir..v
        end
        if v:find("%.9%.png")
          view.setBackground(NineBitmapDrawable(loadbitmap(v)))
         else
          view.setBackground(LuaBitmapDrawable(context,v))
        end
      end

     elseif type(v)=="userdata"
      view.setBackground(v)

     elseif type(v)=="number"
      view.setBackground(v)

    end

   elseif k == "onClick"
    local listener
    if type(v) == "function"
      listener = OnClickListener { onClick = v }

     elseif type(v) == "userdata"
      listener = v

     elseif type(v) == "string"
      if ltrs[v]
        listener = ltrs[v]
       else
        local l = rawget(root, v) or rawget(_G, v)
        if type(l) == "function"
          listener = OnClickListener { onClick = l }

         elseif type(l) == "userdata"
          listener = l

         else
          listener = OnClickListener { onClick = function(a) (root[v] or _G[v])(a) end }
        end
        ltrs[v] = listener
      end
    end
    view.setOnClickListener(listener)

   elseif k=="onLongClick"
    local listener
    if type(v)=="function"
      listener=OnLongClickListener{ onLongClick=v }

     elseif type(v)=="userdata"
      listener=v

     elseif type(v)=="string"
      if ltrs[v]
        listener=ltrs[v]
       else
        local l=rawget(root,v) or rawget(_G,v)
        if type(l)=="function"
          listener=OnLongClickListener{ onLongClick=l }

         elseif type(l)=="userdata"
          listener=l

         else
          listener=OnLongClickListener{ onLongClick=function(a) (root[v] or _G[v])(a) end }
        end
        ltrs[v]=listener
      end
    end
    view.setOnLongClickListener(listener)

   elseif k == "password" and (v == "true" or v == true)
    view.setInputType(0x81)

   elseif type(k) == "string" and not (k:find("layout_")) and not (k:find("padding")) and k ~= "style"
    k = string.gsub(k, "^(%w)", function(s) return string.upper(s) end)
    if k == "Text" or k == "Title" or k == "Subtitle"
      view["set" .. k](v)
     elseif not k:find("^On") and not k:find("^Tag") and type(v) == "table"
      view["set" .. k](checkValues(unpack(v)))
     else
      view["set" .. k](checkValue(v))
    end
  end

end

local function setstyle(c, t, root, view, params, ids)
  if not t or type(t) ~= "table"
    return
  end
  local mt = getmetatable(t)
  if not mt or not mt.__index
    return
  end
  local m = mt.__index
  if c[m]
    return
  end
  c[m] = true
  for k, v in pairs(m)
    if not rawget(c, k)
      pcall(setattribute, root, view, params, k, v, ids)
    end
    c[k] = true
  end
  setstyle(c, m, root, view, params, ids)
end

local function loadlayout(t, root, group)
  if type(t) == "string"
    t = require(t)
   elseif type(t) ~= "table"
    error(string.format(
    "loadlayout error: First value must be a table, checked import layout.", 0
    ))
  end

  root = root or _G

  local view, style

  if t.style
    if type(t.style) == "number"
      style = t.style
     elseif t.style:find("^%?")
      style = getIdentifier(t.style:sub(2, -1))
     else
      local st, sty = pcall(require, t.style)
      if st
        setmetatable(t, { __index = sty })
       else
        style = checkattr(t.style)
      end
    end
  end

  if not t[1]
    error(string.format(
    "loadlayout error: First value must be a class, checked import package.\n\tat %s", dump2(t)
    ), 0)
  end

  if luajava.instanceof(t[1], View)
    view = t[1]
   elseif type(t[1]) == "number"
    view = activity.layoutInflater.inflate(t[1], nil)
   else
    if style
      view = t[1](ContextThemeWrapper(context, style), nil, style)
     else
      view = t[1](context)
    end
  end

  local params = ViewGroup.LayoutParams(
  checkValue(t.layout_width) or checkValue(t.w) or -2,
  checkValue(t.layout_height) or checkValue(t.h) or -2
  )

  if group
    params = group.LayoutParams(params)
  end

  if t.layout_margin or t.layout_marginStart or t.layout_marginEnd or
    t.layout_marginLeft or t.layout_marginTop or t.layout_marginRight or
    t.layout_marginBottom
    params.setMargins(checkValues(
    t.layout_marginLeft or t.layout_margin or 0,
    t.layout_marginTop or t.layout_margin or 0,
    t.layout_marginRight or t.layout_margin or 0,
    t.layout_marginBottom or t.layout_margin or 0
    ))
  end

  if t.padding and type(t.padding) == "table"
    view.setPadding(checkValues(unpack(t.padding)))
   elseif t.padding or t.paddingLeft or t.paddingTop or t.paddingRight or
    t.paddingBottom
    view.setPadding(checkValues(
    t.paddingLeft or t.padding or 0,
    t.paddingTop or t.padding or 0,
    t.paddingRight or t.padding or 0,
    t.paddingBottom or t.padding or 0
    ))
  end

  if t.paddingStart or t.paddingEnd
    view.setPaddingRelative(checkValues(
    t.paddingStart or t.padding or 0,
    t.paddingTop or t.padding or 0,
    t.paddingEnd or t.padding or 0,
    t.paddingBottom or t.padding or 0
    ))
  end

  local c = {}
  setmetatable(c, { __index = t })
  setstyle(c, t, root, view, params, ids)

  for k, v in pairs(t) do
    if tonumber(k) and (type(v) == "table" or type(v) == "string")
      if luajava.instanceof(view, AdapterView)
        if type(v) == "string"
          v = require(v)
        end
        view.adapter = LuaAdapter(context, v)
       else
        view.addView(loadlayout(v, root, view.getClass()))
      end
     elseif k == "id"
      rawset(root, v, view)
      local id = ids.id
      ids.id = ids.id + 1
      view.setId(id)
      ids[v] = id
     else
      local e, s = pcall(setattribute, root, view, params, k, v, ids)
      if not e
        local _, i = s:find(":%d+:")
        s = s:sub(i or 1, -1)
        local t, du = pcall(dump2, t)
        print(string.format(
        "loadlayout error %s \n\tat %s\n\tat key=%s value=%s\n\tat %s",
        s, view.toString(), k, v, du or ""
        ), 0)
      end
    end
  end

  view.setLayoutParams(params)
  return view
end

return loadlayout