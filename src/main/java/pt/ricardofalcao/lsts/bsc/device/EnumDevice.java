package pt.ricardofalcao.lsts.bsc.device;

import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import pt.ricardofalcao.lsts.bsc.device.impl.ExampleDevice;

@RequiredArgsConstructor
public enum EnumDevice {

    EXAMPLE("BTSPPServer", new ExampleDevice());

    private final String serviceName;

    private final AbstractDevice device;

    /*

     */

    private static final Map<String, AbstractDevice> deviceRegistry = new HashMap<>(EnumDevice.values().length);
    static {
        for(EnumDevice edevice : EnumDevice.values()) {
            deviceRegistry.put(edevice.serviceName, edevice.device);
        }
    }

    public static AbstractDevice getDevice(String serviceName) {
        return deviceRegistry.get(serviceName);
    }

}
