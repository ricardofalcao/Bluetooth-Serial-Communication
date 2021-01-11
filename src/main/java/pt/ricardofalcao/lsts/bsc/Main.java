package pt.ricardofalcao.lsts.bsc;

import java.io.File;

import java.util.Set;
import javafx.application.Application;
import javax.bluetooth.BluetoothStateException;

import javax.bluetooth.LocalDevice;
import pt.ricardofalcao.lsts.bsc.config.ConfigLoadException;
import pt.ricardofalcao.lsts.bsc.config.ConfigSaveException;
import pt.ricardofalcao.lsts.bsc.config.ConfigHandler;

public class Main {

    public static GuiHandler gui;

    public static ConfigHandler config;

    public static void main(String[] args) throws BluetoothStateException {
        config = new ConfigHandler(new File("config"));

        try {
            System.out.println("Loading config files...");

            config.load();
        } catch(ConfigLoadException ex) {
            ex.printStackTrace();
            System.exit(1);
            return;
        }

        String stack = LocalDevice.getProperty("bluecove.stack");
        System.out.println(String.format("Running on bluetooth stack: %s", stack));

        Application.launch(GuiHandler.class, args);

        try {
            System.out.println("Saving config files...");

            config.save();
        } catch(ConfigSaveException ex) {
            ex.printStackTrace();
            System.exit(1);
            return;
        }

        System.out.println("Goodbye");
    }
}
