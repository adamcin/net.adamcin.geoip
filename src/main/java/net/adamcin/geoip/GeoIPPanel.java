package net.adamcin.geoip;

import com.maxmind.geoip.Country;
import com.maxmind.geoip.LookupService;
import org.apache.commons.io.IOUtils;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class GeoIPPanel extends JPanel implements WindowListener {

    File datFile = null;
    LookupService lookupService = null;
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

        browseButton = new JButton("Browse...");
        browseButton.addActionListener(new BrowseListener());
        browseLabel = new JLabel("Choose database");
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

        SpringUtilities.makeCompactGrid(this, 5, 2, 6, 6, 6, 6);
    }

    private void updateDatFile(File file) throws IOException {
        if (file != null && file.exists()) {
            File toDelete = null;
            if (datFile != null) {
                toDelete = datFile;
            }

            datFile = File.createTempFile("geoip", "dat");

            InputStream is = null;
            OutputStream os = null;
            try {
                is = new FileInputStream(file);
                os = new FileOutputStream(datFile);
                IOUtils.copy(is, os);
            } finally {
                IOUtils.closeQuietly(is);
                IOUtils.closeQuietly(os);
            }

            lookupService = new LookupService(datFile,
                                              LookupService.GEOIP_MEMORY_CACHE);

            this.enableLookup();

            if (toDelete != null) {
                toDelete.delete();
            }
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

    @Override public void windowOpened(WindowEvent e) { }
    @Override public void windowClosing(WindowEvent e) { }

    @Override
    public void windowClosed(WindowEvent e) {
        if (this.datFile != null) {
            this.datFile.delete();
        }
    }

    @Override public void windowIconified(WindowEvent e) { }
    @Override public void windowDeiconified(WindowEvent e) { }
    @Override public void windowActivated(WindowEvent e) { }
    @Override public void windowDeactivated(WindowEvent e) { }
}
