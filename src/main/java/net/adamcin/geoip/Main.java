package net.adamcin.geoip;

import javax.swing.*;
import java.io.File;
import java.io.IOException;

public final class Main {

    static void createAndShowGUI(File initialDb) {
        final GeoIPPanel panel = new GeoIPPanel();
        try {
            panel.updateDatFile(initialDb);
        } catch (IOException e) {
            e.printStackTrace(System.err);
        }
        JFrame frame = new JFrame("GeoIP Lookup");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        frame.addWindowListener(panel);

        frame.add(panel);

        frame.pack();
        frame.setVisible(true);
    }

    public static void main(String[] args) {

        final File pathArg = args.length > 0 ? new File(args[0]) : null;
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                createAndShowGUI(pathArg);
            }
        });

    }
}
