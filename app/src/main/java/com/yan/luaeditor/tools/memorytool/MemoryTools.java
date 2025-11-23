package com.yan.luaeditor.tools.memorytool;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class MemoryTools {
	public static void parseMaps(byte[] bytes) {
		BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(bytes)));
		try {
			ArrayList <String> lineList = new ArrayList<>();
			String line;
			while ((line = reader.readLine()) != null) {

			}
		} catch (Exception e) {

		}

	}
}