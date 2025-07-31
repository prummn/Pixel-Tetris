package com.bit.ui;

import javax.swing.*;

public class GameLauncher {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(StartMenu::new);
    }
}
