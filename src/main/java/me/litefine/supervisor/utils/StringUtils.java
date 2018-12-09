package me.litefine.supervisor.utils;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils {

    private static final Pattern jsonPattern = Pattern.compile("\"(.*?)\""), formattedDataPattern = Pattern.compile("(?!`)(.*?)(?=`)");

    public static String getStringRepresentation(SocketAddress socketAddress) {
        return ((InetSocketAddress) socketAddress).getHostString();
    }

    public static String millisToPattern(long millis) {
        return DateTimeFormatter.ofPattern("HH:mm:ss").format(Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC));
    }

    public static String removeExtraSpaces(String string) {
        return string.trim().replaceAll("\\s+", " ");
    }

    public static String[] getArguments(String string) {
        String[] split = string.split("\\s");
        return Arrays.copyOfRange(split, 1, split.length);
    }

    public static String[] splitFormattedData(String formattedDataString) {
        List<String> list = new ArrayList<>(4);
        Matcher matcher = formattedDataPattern.matcher(formattedDataString);
        while (matcher.find()) list.add(matcher.group());
        return list.toArray(new String[0]);
    }

    public static String findJSONElementIn(String text, int matchNumber) {
        if (text == null) return null;
        Matcher matcher = jsonPattern.matcher(text);
        while (matcher.find()) {
            if (matchNumber-- == 1) return matcher.group().replace("\"", "");
            else if (matchNumber < 1) return null;
        }
        return null;
    }

}