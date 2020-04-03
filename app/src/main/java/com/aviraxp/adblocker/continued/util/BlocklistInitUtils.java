package com.aviraxp.adblocker.continued.util;

import android.content.res.Resources;
import android.content.res.XModuleResources;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashSet;

import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XposedHelpers;

public class BlocklistInitUtils {

    public void init(IXposedHookZygoteInit.StartupParam startupParam, String resName, HashSet blocklistName) throws IOException {
        String MODULE_PATH = startupParam.modulePath;
        Resources res = XModuleResources.createInstance(MODULE_PATH, null);
        byte[] array = XposedHelpers.assetAsByteArray(res, resName);
        String decoded = decodeString(resName, array);
        String[] sUrls = decoded.split("\n");
        Collections.addAll(blocklistName, sUrls);
    }

    private String decodeString(String resName, byte[] array) {
        String decoded = new String(array, StandardCharsets.UTF_8);
        if (resName.equals("blocklist/hosts_yhosts")) {
            decoded = decoded.replace("127.0.0.1 ", "").replace("localhost", "workaround");
        }
        return decoded;
    }
}
