package me.litefine.supervisor.utils.mojang;

import me.litefine.supervisor.main.Supervisor;
import me.litefine.supervisor.utils.StringUtils;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Scanner;

public class MojangAPI {

    private static final String UUID_URL = "https://api.mojang.com/users/profiles/minecraft/%s";
    private static final String SKIN_URL = "https://sessionserver.mojang.com/session/minecraft/profile/%s?unsigned=false";

    private static final HashMap<String, CashedProfile> cashedProfiles = new HashMap<>(4000);

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
        HttpURLConnection connection = (HttpURLConnection) new URL(String.format(URL, replacement)).openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(3000);
        connection.setReadTimeout(3000);
        if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) return new Scanner(connection.getInputStream()).nextLine();
        else return null;
    }

    public static HashMap<String, CashedProfile> getCashedProfiles() {
        return cashedProfiles;
    }

}