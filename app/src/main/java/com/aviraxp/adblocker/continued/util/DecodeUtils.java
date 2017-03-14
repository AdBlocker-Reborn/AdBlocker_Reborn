package com.aviraxp.adblocker.continued.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

public class DecodeUtils {
    public static String decode(String string, String encodingType) {
        if (string != null) {
            try {
                if (encodingType != null) {
                    return URLDecoder.decode(string, encodingType);
                } else {
                    return URLDecoder.decode(string, "UTF-8");
                }
            } catch (UnsupportedEncodingException | IllegalArgumentException ignored) {
            }
        }
        return null;
    }
}
