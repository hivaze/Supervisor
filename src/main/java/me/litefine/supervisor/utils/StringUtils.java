package me.litefine.supervisor.utils;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.time.Duration;
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
        Duration dur = Duration.ofMillis(millis);
        long daysDur = dur.toDays();
        long hoursDur = dur.minusDays(daysDur).toHours();
        long minutesDur = dur.minusDays(daysDur).minusHours(hoursDur).toMinutes();
        long secondsDur = dur.minusDays(daysDur).minusHours(hoursDur).minusMinutes(minutesDur).getSeconds();
        return String.format("%02d days %02dh %02dm %02ds", daysDur, hoursDur, minutesDur, secondsDur);
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