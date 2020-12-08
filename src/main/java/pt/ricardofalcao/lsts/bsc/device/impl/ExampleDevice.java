package pt.ricardofalcao.lsts.bsc.device.impl;

import pt.ricardofalcao.lsts.bsc.Main;
import pt.ricardofalcao.lsts.bsc.controller.device.impl.ExampleDeviceController;
import pt.ricardofalcao.lsts.bsc.controller.main.MainController;
import pt.ricardofalcao.lsts.bsc.device.AbstractDevice;
import pt.ricardofalcao.lsts.bsc.controller.device.AbstractDeviceController;

public class ExampleDevice extends AbstractDevice {

    public ExampleDevice() {
        super(
            Main.class.getResource("fxml/main/device_sidebar.fxml"),
            Main.class.getResource("fxml/devices/example.fxml")
        );
    }

    @Override
    public AbstractDeviceController buildController(MainController mainController, String name, String address) {
        return new ExampleDeviceController(mainController, name, address);
    }
}
