package me.litefine.supervisor.utils.mojang;

public class CashedProfile {

    private final String UUID, textures, signature;

    CashedProfile(String UUID, String textures, String signature) {
        this.UUID = UUID;
        this.textures = textures;
        this.signature = signature;
    }

    public String getUUID() {
        return UUID;
    }

    public boolean hasTextures() {
        return textures != null && signature != null;
    }

    public String getTextures() {
        return textures;
    }

    public String getSignature() {
        return signature;
    }

    @Override
    public String toString() {
        return "CashedProfile{" + "UUID='" + UUID + '\'' + ", textures='" + textures + '\'' + ", signature='" + signature + '\'' + '}';
    }

}