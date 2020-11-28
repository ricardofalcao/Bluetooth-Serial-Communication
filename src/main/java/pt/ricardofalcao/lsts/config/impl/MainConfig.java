package pt.ricardofalcao.lsts.config.impl;

import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import pt.ricardofalcao.lsts.config.Config;

public class MainConfig implements Config {

    public boolean filterFavorite;

    public Map<String, Device> devices = new HashMap<>();

    /*

     */

    public boolean deviceExists(String address) {
        return devices.containsKey(address);
    }

    public void deviceCreate(String address) {
        devices.put(address, new Device(address));
    }

    public boolean deviceIsFavorite(String address) {
        if (!deviceExists(address)) {
            deviceCreate(address);
        }

        return devices.get(address).favorite;
    }

    public void deviceSetFavorite(String address, boolean value) {
        if (!deviceExists(address)) {
            deviceCreate(address);
        }

        devices.get(address).favorite = value;
    }

    /*

     */

    @RequiredArgsConstructor
    public static class Device {

        private transient final String address;

        //

        private boolean favorite;

    }

}
