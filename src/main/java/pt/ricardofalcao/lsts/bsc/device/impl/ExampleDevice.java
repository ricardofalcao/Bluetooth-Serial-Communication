package pt.ricardofalcao.lsts.bsc.device.impl;

import java.net.URL;
import pt.ricardofalcao.lsts.bsc.Main;
import pt.ricardofalcao.lsts.bsc.device.AbstractDevice;

public class ExampleDevice extends AbstractDevice {

    public ExampleDevice() {
        super(Main.class.getResource("fxml/devices/example.fxml"));
    }
}
