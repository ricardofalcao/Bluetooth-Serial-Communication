package pt.ricardofalcao.lsts.bsc;

import java.time.format.DateTimeFormatter;
import javax.bluetooth.UUID;

public class Constants {

    public static final UUID BLUETOOTH_SPP_SERVICE_UUID = new UUID("1101", true);

    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    public static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

}
