package com.jaredrummler.android.colorpicker;

import android.graphics.Color;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MyColorUtils {


  public static String hexToRgbaString(String hexColor) {
    // 删除开头的#号
    hexColor = hexColor.replaceFirst("#", "");

    int red, green, blue, alpha;
    if (hexColor.length() == 6) {
      // 如果没有透明度信息，则默认为不透明
      red = Integer.parseInt(hexColor.substring(0, 2), 16);
      green = Integer.parseInt(hexColor.substring(2, 4), 16);
      blue = Integer.parseInt(hexColor.substring(4, 6), 16);
      alpha = 255;
    } else if (hexColor.length() == 8) {
      // 如果有透明度信息
      red = Integer.parseInt(hexColor.substring(2, 4), 16);
      green = Integer.parseInt(hexColor.substring(4, 6), 16);
      blue = Integer.parseInt(hexColor.substring(6, 8), 16);
      alpha = Integer.parseInt(hexColor.substring(0, 2), 16);
    } else {
      return "";
    }
    // 将RGBA分量格式化为字符串
    return "rgba("
        + red
        + ","
        + green
        + ","
        + blue
        + ","
        + String.format("%.2f", (alpha / 255.0))
        + ")";
  }

  public static String intToRgbaString(int color) {
    // 提取alpha, red, green, blue分量
    int alpha = (color >> 24) & 0xFF;
    int red = (color >> 16) & 0xFF;
    int green = (color >> 8) & 0xFF;
    int blue = color & 0xFF;

    // 将RGBA分量格式化为字符串
    return "rgba("
        + red
        + ","
        + green
        + ","
        + blue
        + ","
        + String.format("%.2f", (alpha / 255.0))
        + ")";
  }



  public static int rgbaStringToInt(String rgbaString) {
    rgbaString = rgbaString.toLowerCase();
    // 正则表达式用于匹配rgba(r,g,b,a)和rgb(r,g,b)格式的字符串
    Pattern pattern = Pattern.compile("rgba?\\((\\d+),\\s*(\\d+),\\s*(\\d+)(?:,\\s*([\\d.]+))?\\)");

    Matcher matcher = pattern.matcher(rgbaString);

    if (matcher.matches()) {
      // 解析红色、绿色、蓝色分量
      int red = Integer.parseInt(matcher.group(1));
      int green = Integer.parseInt(matcher.group(2));
      int blue = Integer.parseInt(matcher.group(3));

      // 检查是否匹配到了透明度分量
      int alphaComponent;
      if (matcher.groupCount() == 5) {
        // 解析透明度分量，并将其转换为0-255的范围
        float alpha = Float.parseFloat(matcher.group(4));
        alphaComponent = Math.round(alpha * 255);
      } else {
        // 如果没有透明度分量，则默认为255（完全不透明）
        alphaComponent = 255;
      }

      // 组合RGBA分量到一个整数中
      int color = (alphaComponent << 24) | (red << 16) | (green << 8) | blue;
      return color;
    } else {
      return 0;
    }
  }


    
}
