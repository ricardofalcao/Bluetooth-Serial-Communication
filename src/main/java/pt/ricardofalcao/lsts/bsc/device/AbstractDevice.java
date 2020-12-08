package pt.ricardofalcao.lsts.bsc.device;

import java.net.URL;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import pt.ricardofalcao.lsts.bsc.controller.device.AbstractDeviceController;
import pt.ricardofalcao.lsts.bsc.controller.main.MainController;

@RequiredArgsConstructor
public abstract class AbstractDevice {

    @Getter
    private final URL sidebarUiPath;

    @Getter
    private final URL contentUiPath;

    public abstract AbstractDeviceController buildController(MainController mainController, String name, String address);

}
