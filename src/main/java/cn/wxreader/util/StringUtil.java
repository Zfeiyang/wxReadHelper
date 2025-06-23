package cn.wxreader.util;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.stream.Collectors;

public class StringUtil {
    public static String encodeData(Map<String, Object> data) {
        return data.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> {
                    try {
                        return URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8.toString()) +
                                "=" +
                                URLEncoder.encode(entry.getValue().toString(), StandardCharsets.UTF_8.toString());
                    } catch (Exception e) {
                        throw new RuntimeException("Error encoding key or value", e);
                    }
                })
                .collect(Collectors.joining("&"));
    }

    public static String calHash(String inputStr) {
        long _7032f5 = 0x15051505L;
        long _cc1055 = _7032f5;
        int length = inputStr.length();
        int _19094e = length - 1;

        while (_19094e > 0) {
            _7032f5 = 0x7fffffffL & (_7032f5 ^ (long) inputStr.charAt(_19094e) << (length - _19094e) % 30);
            _cc1055 = 0x7fffffffL & (_cc1055 ^ (long) inputStr.charAt(_19094e - 1) << _19094e % 30);
            _19094e -= 2;
        }

        return Long.toHexString(_7032f5 + _cc1055).toLowerCase();
    }

    public static String calSha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error calculating SHA-256", e);
        }
    }
}