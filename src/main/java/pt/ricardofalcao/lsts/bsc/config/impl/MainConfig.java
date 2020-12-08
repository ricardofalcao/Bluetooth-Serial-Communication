package pt.ricardofalcao.lsts.bsc.config.impl;

import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import pt.ricardofalcao.lsts.bsc.config.Config;

public class MainConfig implements Config {

    public Map<String, Device> devices = new HashMap<>();

    /*

     */

    public boolean deviceExists(String address) {
        return devices.containsKey(address);
    }

    public void deviceCreate(String address) {
        devices.put(address, new Device(address));
    }

    /*

     */

    @RequiredArgsConstructor
    public static class Device {

        private transient final String address;

        //

    }

}
