package net.adamcin.geoip;

import javax.swing.*;

public final class Main {

    static void createAndShowGUI() {
        final GeoIPPanel panel = new GeoIPPanel();
        JFrame frame = new JFrame("GeoIP Lookup");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        frame.addWindowListener(panel);

        frame.add(panel);

        frame.pack();
        frame.setVisible(true);
    }

    public static void main(String[] args) {

        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                createAndShowGUI();
            }
        });

    }
}
