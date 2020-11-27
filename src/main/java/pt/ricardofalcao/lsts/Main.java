package pt.ricardofalcao.lsts;

import javafx.application.Application;
import lombok.Getter;

public class Main {

    public static GuiHandler gui;

    public static ServerHandler server;

    public static void main(String[] args) {
        server = new ServerHandler();

        Application.launch(GuiHandler.class, args);
    }
}
