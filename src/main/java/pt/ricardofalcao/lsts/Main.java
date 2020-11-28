package pt.ricardofalcao.lsts;

import java.io.File;
import java.io.IOException;
import javafx.application.Application;
import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import pt.ricardofalcao.lsts.config.ConfigHandler;
import pt.ricardofalcao.lsts.config.ConfigLoadException;
import pt.ricardofalcao.lsts.config.ConfigSaveException;

public class Main {

    public static GuiHandler gui;

    public static ServerHandler server;

    public static ConfigHandler config;

    public static void main(String[] args) throws BluetoothStateException {
        server = new ServerHandler();

        config = new ConfigHandler(new File("config"));

        try {
            System.out.println("Loading config files...");

            config.load();
        } catch(ConfigLoadException ex) {
            ex.printStackTrace();
            System.exit(1);
            return;
        }

        Application.launch(GuiHandler.class, args);

        try {
            System.out.println("Saving config files...");

            config.save();
        } catch(ConfigSaveException ex) {
            ex.printStackTrace();
            System.exit(1);
            return;
        }
    }
}
