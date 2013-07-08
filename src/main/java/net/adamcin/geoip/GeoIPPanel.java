package net.adamcin.geoip;

import com.maxmind.geoip.Country;
import com.maxmind.geoip.LookupService;
import org.apache.commons.io.IOUtils;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.zip.GZIPInputStream;

public class GeoIPPanel extends JPanel {

    public static final File DEFAULT_DAT_FILE = new File(System.getProperty("user.home"), "GeoIP.dat");
    private static final String LATEST_DAT_FILE = "http://geolite.maxmind.com/download/geoip/database/GeoLiteCountry/GeoIP.dat.gz";

    LookupService lookupService = null;
    JLabel latestLabel;
    JButton latestButton;
    JLabel browseLabel;
    JButton browseButton;
    JLabel addressLabel;
    JTextField addressField;
    JLabel lookupLabel;
    JButton lookupButton;
    JLabel countryNameLabel;
    JTextField countryNameField;
    JLabel countryCodeLabel;
    JTextField countryCodeField;

    public GeoIPPanel() {
        super(new SpringLayout());


        latestButton = new JButton("Download");
        latestButton.addActionListener(new LatestListener());
        latestLabel = new JLabel("Get latest database");
        latestLabel.setLabelFor(latestButton);
        add(latestLabel);
        add(latestButton);

        browseButton = new JButton("Browse...");
        browseButton.addActionListener(new BrowseListener());
        browseLabel = new JLabel("Open database file");
        browseLabel.setLabelFor(browseButton);
        add(browseLabel);
        add(browseButton);

        addressField = new JTextField(15);
        addressField.setEnabled(false);
        addressLabel = new JLabel("IP address");
        addressLabel.setLabelFor(addressField);
        addressLabel.setEnabled(false);
        add(addressLabel);
        add(addressField);

        lookupButton = new JButton("Lookup");
        lookupButton.setEnabled(false);
        lookupButton.addActionListener(new LookupListener());
        lookupLabel = new JLabel("Do lookup");
        lookupLabel.setLabelFor(lookupButton);
        lookupLabel.setEnabled(false);
        add(lookupLabel);
        add(lookupButton);

        countryNameField = new JTextField(30);
        countryNameField.setEnabled(false);
        countryNameField.setEditable(false);
        countryNameLabel = new JLabel("Country name");
        countryNameLabel.setLabelFor(countryNameField);
        countryNameLabel.setEnabled(false);
        add(countryNameLabel);
        add(countryNameField);

        countryCodeField = new JTextField(3);
        countryCodeField.setEnabled(false);
        countryCodeField.setEditable(false);
        countryCodeLabel = new JLabel("Country code");
        countryCodeLabel.setLabelFor(countryCodeField);
        countryCodeLabel.setEnabled(false);
        add(countryCodeLabel);
        add(countryCodeField);

        SpringUtilities.makeCompactGrid(this, 6, 2, 6, 6, 6, 6);

    }

    public void updateDatFromLatest() throws IOException {

        URL url = new URL(LATEST_DAT_FILE);

        File latest = DEFAULT_DAT_FILE;
        File latestParent = latest.getParentFile();

        if (latestParent.isDirectory() || latestParent.mkdirs()) {
            File temp = File.createTempFile("geoip", ".latest.dat", latest.getParentFile());

            InputStream is = null;
            OutputStream os = null;
            LookupService testLookupService = null;
            try {
                is = new GZIPInputStream(url.openStream());
                os = new FileOutputStream(temp);
                IOUtils.copy(is, os);

                // try to create a new lookup service

                testLookupService = new LookupService(temp, LookupService.GEOIP_MEMORY_CACHE);
                System.out.println(
                        "Successfully downloaded latest database: "
                                + testLookupService.getDatabaseInfo()
                );

            } finally {
                IOUtils.closeQuietly(is);
                IOUtils.closeQuietly(os);
                if (testLookupService != null) {
                    testLookupService.close();
                }
            }

            if (latest.exists()) {
                latest.delete();
            }

            if (temp.renameTo(latest)) {
                if (this.lookupService != null) {
                    this.lookupService.close();
                }
                this.lookupService = new LookupService(latest, LookupService.GEOIP_MEMORY_CACHE);
                this.enableLookup();
            } else {
                temp.delete();
            }
        }
    }

    public void updateDatFile(File file) throws IOException {
        if (file != null && file.exists()) {

            LookupService temp = new LookupService(file,
                                              LookupService.GEOIP_MEMORY_CACHE);

            if (lookupService != null) {
                lookupService.close();
            }
            lookupService = temp;

            this.enableLookup();
        }

    }

    private void enableLookup() {

        this.addressLabel.setEnabled(true);
        this.addressField.setEnabled(true);
        this.lookupLabel.setEnabled(true);
        this.lookupButton.setEnabled(true);
        this.countryNameLabel.setEnabled(true);
        this.countryNameField.setEnabled(true);
        this.countryCodeLabel.setEnabled(true);
        this.countryCodeField.setEnabled(true);
    }

    private void doLookup() {
        if (this.lookupService != null) {
            String address = this.addressField.getText();
            Country country = this.lookupService.getCountry(address);
            if (country != null) {
                this.countryNameField.setText(country.getName());
                this.countryCodeField.setText(country.getCode());
            } else {
                this.countryNameField.setText("");
                this.countryCodeField.setText("");
            }
        }
    }

    class LatestListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent event) {
            try {
                GeoIPPanel.this.setEnabled(false);
                GeoIPPanel.this.updateDatFromLatest();
            } catch (IOException e) {
                e.printStackTrace(System.err);
            } finally {
                GeoIPPanel.this.setEnabled(true);
            }
        }
    }

    class BrowseListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent event) {

            JFileChooser chooser = new JFileChooser();
            FileNameExtensionFilter filter =
                    new FileNameExtensionFilter("GeoIP databases (*.dat)", "dat");

            chooser.setFileFilter(filter);
            int returnVal = chooser.showOpenDialog(GeoIPPanel.this);

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                try {
                    GeoIPPanel.this.updateDatFile(chooser.getSelectedFile());
                } catch (IOException e) {
                    e.printStackTrace(System.err);
                }
            }
        }
    }

    class LookupListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            GeoIPPanel.this.doLookup();
        }
    }

}
