package me.litefine.supervisor.utils.mojang;

import me.litefine.supervisor.main.Settings;
import me.litefine.supervisor.main.Supervisor;
import me.litefine.supervisor.utils.StringUtils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

public class MojangAPI {

    private static final String UUID_URL = "https://api.mojang.com/users/profiles/minecraft/%s";
    private static final String SKIN_URL = "https://sessionserver.mojang.com/session/minecraft/profile/%s?unsigned=false";
    private static final List<Proxy> PROXY_LIST = new ArrayList<>();

    private static final Map<String, CashedProfile> cashedProfiles = new ConcurrentHashMap<>(4000);

    public static void loadProxyList() {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(Settings.getProxyFile()));
            while (reader.ready()) {
                String address = reader.readLine();
                String[] split = address.split(":");
                InetSocketAddress socketAddress = new InetSocketAddress(split[0], Integer.parseInt(split[1]));
                PROXY_LIST.add(new Proxy(Proxy.Type.HTTP, socketAddress));
            }
            Supervisor.getLogger().debug("[SETTINGS] " + PROXY_LIST.size() + " proxy loaded");
        } catch (IOException ex) {
            Supervisor.getLogger().warn("Can't load proxy list from 'http-proxy-list.txt' " + ex);
        }
    }

    public static CashedProfile fetchProfile(String playerName, String defaultUUID) {
        CashedProfile cashedProfile = cashedProfiles.get(playerName);
        if (cashedProfile != null) return cashedProfile;
        else {
            try {
                String uuid = StringUtils.findJSONElementIn(executeResponse(UUID_URL, playerName), 2);
                if (uuid != null) {
                    String skinProfileResponse = executeResponse(SKIN_URL, uuid);
                    String textures = StringUtils.findJSONElementIn(skinProfileResponse, 9), signature = StringUtils.findJSONElementIn(skinProfileResponse, 11);
                    cashedProfile = new CashedProfile(uuid, textures, signature);
                    cashedProfiles.put(playerName, cashedProfile);
                } else throw new Exception("Mojang returned null UUID, maybe not premium?");
            } catch (Exception ex) {
                Supervisor.getLogger().debug("Can't load '" + playerName + "' profile from mojang servers - " + ex);
                cashedProfile = new CashedProfile(defaultUUID.replace("-", ""), null, null);
            }
        }
        return cashedProfile;
    }

    private static String executeResponse(String URL, String replacement) throws IOException {
        HttpURLConnection connection;
        if (Settings.useMojangBypass() && !PROXY_LIST.isEmpty()) {
            Proxy randomProxy = PROXY_LIST.get(ThreadLocalRandom.current().nextInt(PROXY_LIST.size()));
            connection = (HttpURLConnection) new URL(String.format(URL, replacement)).openConnection(randomProxy);
        } else connection = (HttpURLConnection) new URL(String.format(URL, replacement)).openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(3000);
        connection.setReadTimeout(3000);
        if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) return new Scanner(connection.getInputStream()).nextLine();
        else return null;
    }

    public static Map<String, CashedProfile> getCashedProfiles() {
        return cashedProfiles;
    }

}